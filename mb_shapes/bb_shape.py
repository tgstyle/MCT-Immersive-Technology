# bb_shape.py
import sys
import os
import json
import argparse
import time
import threading
import multiprocessing as mp
import numpy as np
import torch
from tqdm import tqdm
from bb_voxelization import process_block
from bb_postprocess import process_post, fill_gaps_along_axis, remove_protrusions
from bb_utils import find_max_box, merge_aabbs, merge_along_dim, parse_thresh_val, init_worker
from bb_model_parser import parse_bbmodel, list_bbmodel_files, select_file, normalize_offsets
try:
    import torch_directml
except ImportError:
    torch_directml = None
def extract_aabbs_from_occupied(occupied_np, res=16):
    if occupied_np.sum() == 0:
        return []
    if occupied_np.all():
        return [(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)]
    occupied = occupied_np.tolist()
    visited = [[[False for _ in range(res)] for _ in range(res)] for _ in range(res)]
    block_aabbs = []
    for i in range(res):
        for j in range(res):
            for k in range(res):
                if occupied[i][j][k] and not visited[i][j][k]:
                    dx, dy, dz = find_max_box(occupied, visited, i, j, k, res)
                    minx = i / res
                    miny = j / res
                    minz = k / res
                    maxx = (i + dx) / res
                    maxy = (j + dy) / res
                    maxz = (k + dz) / res
                    block_aabbs.append((minx, miny, minz, maxx, maxy, maxz))
    block_aabbs = [a for a in block_aabbs if (a[3] - a[0]) * (a[4] - a[1]) * (a[5] - a[2]) > 0.001]
    pre_merge = list(block_aabbs)
    block_aabbs = merge_aabbs(block_aabbs)
    vox_vol = occupied_np.sum() / (res ** 3)
    aabb_vol = sum((a[3] - a[0]) * (a[4] - a[1]) * (a[5] - a[2]) for a in block_aabbs)
    if abs(aabb_vol - vox_vol) > 0.01 * vox_vol:
        block_aabbs = pre_merge
    return block_aabbs
def main():
    start_time = time.time()
    parser = argparse.ArgumentParser()
    
    # Input and output paths
    parser.add_argument('path', nargs='?', help='Path to bbmodel file or directory containing bbmodel files')
    parser.add_argument('--main', type=str, help='Specify the main model file (bypasses selection prompt)')
    parser.add_argument('--output', choices=['java', 'json'], help='Output format: java or json')
    
    # Post-processing control flags
    parser.add_argument('--no-postprocess', action='store_true', help='Disable all post-processing steps')
    parser.add_argument('--no-gpp', action='store_true', help='Disable global post-processing on the full model (enabled by default)')
    parser.add_argument('--no-holes', action='store_true', help='Disable filling of holes using binary_fill_holes')
    parser.add_argument('--no-gaps', action='store_true', help='Disable gap filling along axes (overrides thresholds)')
    parser.add_argument('--no-small-voids', action='store_true', help='Disable removal of small voids and occupied clusters')
    parser.add_argument('--fill-all-voids', action='store_true', help='Fill all internal voids regardless of size (useful for large hollow models)')
    parser.add_argument('--no-supplementary', action='store_true', help='Disable processing of supplementary models')
    
    # Threshold and value settings
    parser.add_argument('--thresh', type=str, default='2,4,2', help='Comma-separated gap thresholds for x,y,z; use 0 for unlimited, d for default, x to disable')
    parser.add_argument('--ex-thresh', type=str, default='d,d,d', help='Comma-separated gap thresholds for excluded blocks along x,y,z; d uses --thresh value, x to disable')
    parser.add_argument('--mi', type=str, default='3,4,4', help='Comma-separated max intrusion into excluded blocks along x,y,z; d for default, x for no intrusion')
    parser.add_argument('--gap-passes', type=int, default=3, help='Number of passes for gap filling per axis')
    parser.add_argument('--void-thresh', type=int, default=4, help='Max voxel count for small voids to fill (fills if size < threshold)')
    parser.add_argument('--occ-thresh', type=int, default=4, help='Max voxel count for small occupied clusters to remove (removes if size < threshold)')
    
    # Block and region specifications
    parser.add_argument('--pbg', type=str, default='', help='Comma-separated axes for per-block gap filling (e.g., x,y,z)')
    parser.add_argument('--rpp', action='append', default=[], help='Regional post-processing: "bx,by,bz bx,by,bz ... : x,y,z" where thresholds use d for main thresh, x to disable')
    parser.add_argument('--solid-blocks', type=str, default='', help='Space-separated bx,by,bz to force as solid before post-processing (e.g., "0,0,0 1,0,0")')
    parser.add_argument('--exclude-global', type=str, default='', help='Space-separated bx,by,bz to exclude from global post-processing (e.g., "0,0,0 1,0,0")')
    
    # Order and configuration options
    parser.add_argument('--fill-order', type=str, default='x,z,y', help='Order of axes for gap filling (comma-separated x,y,z in any order)')
    parser.add_argument('--pp-order', type=str, default='per-block,regional,global,per-block-gaps,protrusions', help='Comma-separated order of main post-processing steps: per-block,regional,global,per-block-gaps,protrusions')
    parser.add_argument('--sub-pp-order', type=str, default='remove-small,fill-holes,fill-voids,fill-gaps', help='Comma-separated order of sub-post-processing steps: remove-small,fill-holes,fill-voids,fill-gaps')
    
    # Supplementary model configurations
    parser.add_argument('--supp-config', nargs='+', action='append', default=[], help='Supplementary model config: model.bbmodel num_times offset1 offset2... (e.g., model.bbmodel 2 0,0,0 1,0,0)')
    
    # Device and performance options
    parser.add_argument('--dml-index', type=int, default=None, help='DirectML device index to use (overrides automatic enumeration)')
    parser.add_argument('--single-thread', action='store_true', help='Force single-threaded processing even on CPU')
    
    args = parser.parse_args()
    solid_set = set()
    if args.solid_blocks:
        for s in args.solid_blocks.split():
            bx, by, bz = map(int, s.split(','))
            solid_set.add((bx, by, bz))
    exclude_set = set()
    if args.exclude_global:
        for s in args.exclude_global.split():
            bx, by, bz = map(int, s.split(','))
            exclude_set.add((bx, by, bz))
    order_str = args.fill_order.lower().split(',')
    axis_order = []
    for a in order_str:
        if a == 'x':
            axis_order.append(0)
        elif a == 'y':
            axis_order.append(1)
        elif a == 'z':
            axis_order.append(2)
        else:
            raise ValueError(f"Invalid axis '{a}' in --fill-order")
    if len(axis_order) != 3 or set(axis_order) != {0,1,2}:
        raise ValueError("--fill-order must specify unique x,y,z in comma-separated format")
    global_postprocess = not args.no_gpp
    fill_all_voids = args.fill_all_voids
    device = torch.device('cpu')
    if torch.cuda.is_available():
        device = torch.device('cuda')
        if torch.cuda.device_count() > 1:
            best_index = 0
            best_memory = 0
            for i in range(torch.cuda.device_count()):
                prop = torch.cuda.get_device_properties(i)
                if prop.total_memory > best_memory:
                    best_memory = prop.total_memory
                    best_index = i
            device = torch.device(f'cuda:{best_index}')
        print(f"Using CUDA device: {torch.cuda.get_device_name(device.index)}")
    elif torch_directml is not None and torch_directml.is_available():
        dml_index = 0
        if args.dml_index is not None:
            dml_index = args.dml_index
            print(f"Using specified DirectML device index: {dml_index}")
        else:
            try:
                num_devices = torch_directml.device_count()
                dml_names = [torch_directml.device_name(i) for i in range(num_devices)]
                for i, name in enumerate(dml_names):
                    print(f"DML Device {i}: {name}")
                dml_index = 0
                for i in range(num_devices):
                    name = dml_names[i]
                    if "(TM) Graphics" not in name:
                        dml_index = i
                        break
            except Exception as e:
                print(f"Auto-selection failed: {e}. Using default device 0.")
        device = torch_directml.device(dml_index)
        print(f"Using DirectML device: {torch_directml.device_name(dml_index)}")
    else:
        print("Using CPU")
    thresh_parts = args.thresh.split(',')
    x_threshold = parse_thresh_val(thresh_parts[0], 2)
    y_threshold = parse_thresh_val(thresh_parts[1], 4)
    z_threshold = parse_thresh_val(thresh_parts[2], 2)
    ex_thresh_parts = args.ex_thresh.split(',')
    ex_x_threshold = parse_thresh_val(ex_thresh_parts[0], x_threshold)
    ex_y_threshold = parse_thresh_val(ex_thresh_parts[1], y_threshold)
    ex_z_threshold = parse_thresh_val(ex_thresh_parts[2], z_threshold)
    mi_parts = args.mi.split(',')
    max_intrude_x = parse_thresh_val(mi_parts[0], 3)
    max_intrude_y = parse_thresh_val(mi_parts[1], 4)
    max_intrude_z = parse_thresh_val(mi_parts[2], 4)
    regions = []
    all_region_blocks = set()
    for rpp_str in args.rpp:
        parts = rpp_str.split(':')
        block_strs = parts[0].strip().split()
        thresh_str = parts[1].strip() if len(parts) > 1 else 'd,d,d'
        blocks = set(tuple(map(int, b.split(','))) for b in block_strs)
        thresh_parts = thresh_str.split(',')
        reg_x = parse_thresh_val(thresh_parts[0], x_threshold)
        reg_y = parse_thresh_val(thresh_parts[1], y_threshold)
        reg_z = parse_thresh_val(thresh_parts[2], z_threshold)
        for b in blocks:
            if b in all_region_blocks:
                raise ValueError(f"Overlapping block {b} in regions")
            all_region_blocks.add(b)
        regions.append({'blocks': blocks, 'thresholds': (reg_x, reg_y, reg_z)})
    main_path = None
    directory = '.'
    if args.path:
        if os.path.isfile(args.path) and args.path.lower().endswith('.bbmodel'):
            main_path = args.path
            directory = os.path.dirname(main_path) or '.'
        elif os.path.isdir(args.path):
            directory = args.path
    if args.main:
        main_path = os.path.join(directory, args.main)
        if not os.path.exists(main_path):
            raise ValueError(f"Main model {args.main} not found in {directory}")
    bbmodel_files = list_bbmodel_files(directory)
    main_file = None
    if main_path:
        main_file = os.path.basename(main_path)
        if main_file in bbmodel_files:
            bbmodel_files.remove(main_file)
    else:
        main_file = select_file(bbmodel_files, "Select the main model by number: ")
        main_path = os.path.join(directory, main_file)
        bbmodel_files.remove(main_file)
    supp_list = []
    if not args.no_supplementary and bbmodel_files:
        if args.supp_config:
            for config_parts in args.supp_config:
                if len(config_parts) == 1:
                    config_parts = config_parts[0].split()
                model = config_parts[0]
                num_times = int(config_parts[1])
                offsets = []
                for off_str in config_parts[2:2+num_times]:
                    bx, by, bz = map(int, off_str.split(','))
                    offsets.append((bx, by, bz))
                supp_list.append((model, offsets))
        else:
            print("Additional BBModel files detected:")
            for i, file in enumerate(bbmodel_files, 1): print(f"{i}. {file}")
            selected_supps = set()
            while True:
                remaining = len(bbmodel_files) - len(selected_supps)
                if remaining == 0: break
                add_supp = input("Do you want to add a supplementary model? (y/n): ").strip().lower()
                if add_supp != 'y': break
                available = [f for f in bbmodel_files if f not in selected_supps]
                if not available:
                    print("No more supplementary models available.")
                    break
                supp_file = select_file(available, "Select a supplementary model by number: ") if remaining > 1 else available[0]
                selected_supps.add(supp_file)
                num_times = int(input("How many times to add this supplementary? "))
                offsets = []
                for _ in range(num_times):
                    offset_str = input("Enter offset (bx,by,bz): ")
                    offset_bx, offset_by, offset_bz = map(int, offset_str.split(','))
                    offsets.append((offset_bx, offset_by, offset_bz))
                supp_list.append((supp_file, offsets))
    print("Processing main model...")
    overall_voxels = parse_bbmodel(main_path, args.thresh, args.no_postprocess, args.no_holes, args.no_gaps, args.no_small_voids, args.gap_passes, args.void_thresh, args.occ_thresh, global_postprocess, set(args.pbg.lower().split(',') if args.pbg else ''), device, args.single_thread, solid_set, args.fill_all_voids, exclude_set, axis_order, args.mi, args.ex_thresh, args.rpp, return_voxels=True, pp_order=args.pp_order, sub_order=args.sub_pp_order)
    cache = {}
    unique_supps = set(s_file for s_file, _ in supp_list)
    for s_file in unique_supps:
        print(f"Processing supplementary model: {s_file}")
        s_path = os.path.join(directory, s_file)
        s_voxels = parse_bbmodel(s_path, args.thresh, args.no_postprocess, args.no_holes, args.no_gaps, args.no_small_voids, args.gap_passes, args.void_thresh, args.occ_thresh, global_postprocess, set(args.pbg.lower().split(',') if args.pbg else ''), device, args.single_thread, solid_set, args.fill_all_voids, exclude_set, axis_order, args.mi, args.ex_thresh, args.rpp, return_voxels=True, pp_order=args.pp_order, sub_order=args.sub_pp_order)
        cache[s_file] = s_voxels
    placements = []
    for s_file, offsets in supp_list:
        s_voxels = cache[s_file]
        for off_bx, off_by, off_bz in offsets:
            placements.append((s_voxels, off_bx, off_by, off_bz))
    placements.sort(key=lambda p: p[3])
    overall_dict = {(bx, by, bz): occupied_np for bx, by, bz, occupied_np in overall_voxels}
    for s_voxels, off_bx, off_by, off_bz in placements:
        current_min_bz = min(overall_dict, key=lambda k: k[2])[2] if overall_dict else 0
        current_max_bz = max(overall_dict, key=lambda k: k[2])[2] if overall_dict else 0
        if off_bz < current_min_bz:
            shift = current_min_bz - off_bz
            new_dict = {}
            for (bx, by, bz), occupied_np in overall_dict.items():
                new_dict[(bx, by, bz + shift)] = occupied_np
            overall_dict = new_dict
        elif off_bz == current_min_bz:
            new_dict = {}
            for (bx, by, bz), occupied_np in overall_dict.items():
                new_dict[(bx, by, bz + 1)] = occupied_np
            overall_dict = new_dict
            off_bz = current_min_bz
        for sx, sy, sz, occupied_np in s_voxels:
            new_bx = off_bx + sx
            new_by = off_by + sy
            new_bz = off_bz + sz
            if (new_bx, new_by, new_bz) in overall_dict:
                overall_dict[(new_bx, new_by, new_bz)] |= occupied_np  # merge if overlap
            else:
                overall_dict[(new_bx, new_by, new_bz)] = occupied_np
    # Apply global post-process on combined if enabled
    if not args.no_postprocess and global_postprocess:
        min_bx = min(bx for bx, _, _ in overall_dict) if overall_dict else 0
        min_by = min(by for _, by, _ in overall_dict) if overall_dict else 0
        min_bz = min(bz for _, _, bz in overall_dict) if overall_dict else 0
        max_bx = max(bx for bx, _, _ in overall_dict) if overall_dict else 0
        max_by = max(by for _, by, _ in overall_dict) if overall_dict else 0
        max_bz = max(bz for _, _, bz in overall_dict) if overall_dict else 0
        num_bx = max_bx - min_bx + 1
        num_by = max_by - min_by + 1
        num_bz = max_bz - min_bz + 1
        res = 16
        full_occupied = np.zeros((num_bx * res, num_by * res, num_bz * res), dtype=bool)
        for (bx, by, bz), occupied_np in overall_dict.items():
            off_x = (bx - min_bx) * res
            off_y = (by - min_by) * res
            off_z = (bz - min_bz) * res
            full_occupied[off_x:off_x + res, off_y:off_y + res, off_z:off_z + res] |= occupied_np
        # Handle excludes for combined
        excluded_processed = {}
        for ex_b in exclude_set:
            if ex_b not in overall_dict:
                continue
            occupied_np_copy = overall_dict[ex_b].copy()
            thresh_dict_ex = (ex_x_threshold, ex_y_threshold, ex_z_threshold)
            occupied_np_copy = process_post(occupied_np_copy, args.no_holes, args.no_gaps, args.no_small_voids, args.gap_passes, axis_order, thresh_dict_ex, args.void_thresh, args.occ_thresh, args.fill_all_voids, sub_order=args.sub_pp_order)
            excluded_processed[ex_b] = occupied_np_copy
        # Build is_excluded_full
        is_excluded_full = np.zeros_like(full_occupied, dtype=bool)
        for ex_b in exclude_set:
            if ex_b in overall_dict:
                bx, by, bz = ex_b
                off_x = (bx - min_bx) * res
                off_y = (by - min_by) * res
                off_z = (bz - min_bz) * res
                is_excluded_full[off_x:off_x + res, off_y:off_y + res, off_z:off_z + res] = True
        for region in regions:
            for b in region['blocks']:
                if b in overall_dict:
                    bx, by, bz = b
                    off_x = (bx - min_bx) * res
                    off_y = (by - min_by) * res
                    off_z = (bz - min_bz) * res
                    is_excluded_full[off_x:off_x + res, off_y:off_y + res, off_z:off_z + res] = True
        # Now call process_post with excludes
        max_intrude_dict = {0: max_intrude_x, 1: max_intrude_y, 2: max_intrude_z}
        ex_thresholds = (ex_x_threshold, ex_y_threshold, ex_z_threshold)
        full_occupied = process_post(full_occupied, args.no_holes, args.no_gaps, args.no_small_voids, args.gap_passes, axis_order, (x_threshold, y_threshold, z_threshold), args.void_thresh, args.occ_thresh, args.fill_all_voids, is_excluded=is_excluded_full, max_intrude_dict=max_intrude_dict, ex_thresholds=ex_thresholds, sub_order=args.sub_pp_order)
        # Then overwrite excluded
        for ex_b in excluded_processed:
            bx, by, bz = ex_b
            off_x = (bx - min_bx) * res
            off_y = (by - min_by) * res
            off_z = (bz - min_bz) * res
            full_occupied[off_x:off_x + res, off_y:off_y + res, off_z:off_z + res] = excluded_processed[ex_b]
        # Handle regions
        for region in regions:
            reg_blocks = region['blocks']
            if not reg_blocks:
                continue
            min_bx_r = min(b[0] for b in reg_blocks)
            max_bx_r = max(b[0] for b in reg_blocks)
            min_by_r = min(b[1] for b in reg_blocks)
            max_by_r = max(b[1] for b in reg_blocks)
            min_bz_r = min(b[2] for b in reg_blocks)
            max_bz_r = max(b[2] for b in reg_blocks)
            sub_shape = ((max_bx_r - min_bx_r + 1) * res, (max_by_r - min_by_r + 1) * res, (max_bz_r - min_bz_r + 1) * res)
            sub_occupied = np.zeros(sub_shape, dtype=bool)
            for b in reg_blocks:
                off_x = (b[0] - min_bx_r) * res
                off_y = (b[1] - min_by_r) * res
                off_z = (b[2] - min_bz_r) * res
                sub_occupied[off_x:off_x + res, off_y:off_y + res, off_z:off_z + res] = full_occupied[(b[0] - min_bx) * res:(b[0] - min_bx + 1) * res, (b[1] - min_by) * res:(b[1] - min_by + 1) * res, (b[2] - min_bz) * res:(b[2] - min_bz + 1) * res]
            sub_occupied = process_post(sub_occupied, args.no_holes, args.no_gaps, args.no_small_voids, args.gap_passes, axis_order, region['thresholds'], args.void_thresh, args.occ_thresh, args.fill_all_voids, sub_order=args.sub_pp_order)
            for b in reg_blocks:
                off_x = (b[0] - min_bx_r) * res
                off_y = (b[1] - min_by_r) * res
                off_z = (b[2] - min_bz_r) * res
                full_occupied[(b[0] - min_bx) * res:(b[0] - min_bx + 1) * res, (b[1] - min_by) * res:(b[1] - min_by + 1) * res, (b[2] - min_bz) * res:(b[2] - min_bz + 1) * res] = sub_occupied[off_x:off_x + res, off_y:off_y + res, off_z:off_z + res]
        overall_dict = {}
        for bx in range(num_bx):
            for by in range(num_by):
                for bz in range(num_bz):
                    off_x = bx * res
                    off_y = by * res
                    off_z = bz * res
                    occupied_np = full_occupied[off_x:off_x + res, off_y:off_y + res, off_z:off_z + res].copy()
                    if occupied_np.sum() > 0:
                        overall_dict[(bx + min_bx, by + min_by, bz + min_bz)] = occupied_np
    # Extract AABBs
    overall_aabbs = []
    for (bx, by, bz), occupied_np in sorted(overall_dict.items(), key=lambda k: k[0]):
        block_aabbs = extract_aabbs_from_occupied(occupied_np)
        if block_aabbs:
            overall_aabbs.append((bx, by, bz, block_aabbs))
    overall_aabbs = normalize_offsets(overall_aabbs)
    base_name = os.path.splitext(main_file)[0]
    do_java = args.output != 'json'
    do_json = args.output != 'java'
    if do_java:
        out_file = f"{base_name}_java.txt"
        with open(out_file, 'w') as f:
            for bx, by, bz, block_aabbs in sorted(overall_aabbs, key=lambda x: (x[0], x[1], x[2])):
                f.write(f'if (bX == {bx} && bY == {by} && bZ == {bz}) {{\n')
                for minx, miny, minz, maxx, maxy, maxz in sorted(block_aabbs, key=lambda x: (x[0], x[1], x[2])):
                    f.write(f' main.add(new AABB({minx:.4f}D, {miny:.4f}D, {minz:.4f}D, {maxx:.4f}D, {maxy:.4f}D, {maxz:.4f}D));\n')
                f.write('}\n')
        print(f"JAVA output written to {out_file}")
    if do_json:
        if not overall_aabbs: print("No AABBs to output."); return
        max_bx = max(bx for bx, _, _, _ in overall_aabbs)
        max_by = max(by for _, by, _, _ in overall_aabbs)
        max_bz = max(bz for _, _, bz, _ in overall_aabbs)
        width = max_bx + 1
        height = max_by + 1
        length = max_bz + 1
        aabb_json = [None] * (height * length * width)
        for bx, by, bz, block_aabbs in overall_aabbs:
            index = by * (width * length) + bz * width + bx
            if not block_aabbs: continue
            if len(block_aabbs) == 1:
                minx, miny, minz, maxx, maxy, maxz = block_aabbs[0]
                if (abs(minx) < 1e-5 and abs(miny) < 1e-5 and abs(minz) < 1e-5 and abs(maxx - 1) < 1e-5 and abs(maxy - 1) < 1e-5 and abs(maxz - 1) < 1e-5):
                    aabb_json[index] = []
                    continue
            boxes = []
            for minx, miny, minz, maxx, maxy, maxz in sorted(block_aabbs, key=lambda x: (x[0], x[1], x[2])):
                boxes.append([minx, miny, minz, maxx, maxy, maxz])
            aabb_json[index] = boxes
        out_file = f"{base_name}_json.txt"
        with open(out_file, 'w') as f:
            json.dump({"shapeAABB": aabb_json}, f, indent=None, separators=(',', ':'))
        print(f"JSON output written to {out_file}")
    end_time = time.time()
    duration = end_time - start_time
    print(f"Started: {time.ctime(start_time)}")
    print(f"Finished: {time.ctime(end_time)}")
    print(f"Duration: {duration:.2f} seconds")
if __name__ == "__main__":
    mp.set_start_method('spawn', force=True)
    main()