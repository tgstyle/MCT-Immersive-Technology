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
from bb_postprocess import process_post, fill_gaps_along_axis, remove_protrusions, apply_postprocessing
from bb_utils import find_max_box, merge_aabbs, merge_along_dim, parse_thresh_val, init_worker, extract_aabbs_from_occupied
from bb_model_parser import parse_bbmodel, list_bbmodel_files, select_file, normalize_offsets
try:
    import torch_directml
except ImportError:
    torch_directml = None
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
    parser.add_argument('--thresh', type=str, default='2,4,2', help='Comma-separated gap thresholds for x,y,z; use 0 for unlimited, d for default, x to disable (use quotes if needed, e.g., "2,4,2")')
    parser.add_argument('--ex-thresh', type=str, default='d,d,d', help='Comma-separated gap thresholds for excluded blocks along x,y,z; d uses --thresh value, x to disable (use quotes if needed, e.g., "d,d,d")')
    parser.add_argument('--mi', type=str, default='3,4,4', help='Comma-separated max intrusion into excluded blocks along x,y,z; d for default, x for no intrusion (use quotes if needed, e.g., "3,4,4")')
    parser.add_argument('--gap-passes', type=int, default=3, help='Number of passes for gap filling per axis')
    parser.add_argument('--void-thresh', type=int, default=4, help='Max voxel count for small voids to fill (fills if size < threshold)')
    parser.add_argument('--occ-thresh', type=int, default=4, help='Max voxel count for small occupied clusters to remove (removes if size < threshold)')
    
    # Block and region specifications
    parser.add_argument('--pbg', type=str, default='', help='Comma-separated axes for per-block gap filling (e.g., x,y,z; use quotes if needed, e.g., "x,y,z")')
    parser.add_argument('--rpp', action='append', default=[], help='Regional post-processing: "bx,by,bz bx,by,bz ... : x,y,z" where thresholds use d for main thresh, x to disable (use quotes if needed, e.g., "0,0,0 1,0,0 : d,d,d")')
    parser.add_argument('--solid-blocks', type=str, default='', help='Space-separated bx,by,bz to force as solid before post-processing (e.g., "0,0,0 1,0,0"; use quotes if needed)')
    parser.add_argument('--empty-blocks', type=str, default='', help='Space-separated bx,by,bz to force as empty before post-processing (e.g., "0,0,0 1,0,0"; use quotes if needed)')
    parser.add_argument('--exclude-global', type=str, default='', help='Space-separated bx,by,bz to exclude from global post-processing (e.g., "0,0,0 1,0,0"; use quotes if needed)')
    parser.add_argument('--sub-solid-block', action='append', default=[], help='Force sub-region solid in final shape: "bx,by,bz minX,minY,minZ,maxX,maxY,maxZ" (e.g., "0,0,0 0,0,0,16,16,16"; use quotes if needed)')
    parser.add_argument('--sub-empty-block', action='append', default=[], help='Force sub-region empty in final shape: "bx,by,bz minX,minY,minZ,maxX,maxY,maxZ" (e.g., "0,0,0 0,0,0,16,16,16"; use quotes if needed)')
    
    # Order and configuration options
    parser.add_argument('--fill-order', type=str, default='x,z,y', help='Order of axes for gap filling (comma-separated x,y,z in any order; use quotes if needed, e.g., "x,z,y")')
    parser.add_argument('--pp-order', type=str, default='per-block,regional,global,per-block-gaps,protrusions,sub-blocks', help='Comma-separated order of main post-processing steps: per-block,regional,global,per-block-gaps,protrusions,sub-blocks (use quotes if needed)')
    parser.add_argument('--sub-pp-order', type=str, default='remove-small,fill-holes,fill-voids,fill-gaps', help='Comma-separated order of sub-post-processing steps: remove-small,fill-holes,fill-voids,fill-gaps (use quotes if needed)')
    
    # Supplementary model configurations
    parser.add_argument('--supp-config', nargs='+', action='append', default=[], help='Supplementary model config: model.bbmodel num_times offset1 offset2... (e.g., model.bbmodel 2 0,0,0 1,0,0; use quotes if needed around the whole config)')
    
    # Device and performance options
    parser.add_argument('--dml-index', type=int, default=None, help='DirectML device index to use (overrides automatic enumeration)')
    parser.add_argument('--single-thread', action='store_true', help='Force single-threaded processing even on CPU')
    
    # Low-res merge option
    parser.add_argument('--low-res-merge', action='store_true', help='Enable low-res merge for angled faces')
    
    # Copy AABB option
    parser.add_argument('--copy-aabb', action='append', default=[], help='Copy AABBs: "from_bx,from_by,from_bz to_bx,to_by,to_bz" (use quotes if needed)')
    
    # Minecraft version option
    parser.add_argument('--mc-version', type=str, default='default', choices=['1.12.2', 'default'], help='Minecraft version for output adjustment')
    
    args = parser.parse_args()
    solid_set = set()
    if args.solid_blocks:
        for s in args.solid_blocks.split():
            bx, by, bz = map(int, s.split(','))
            solid_set.add((bx, by, bz))
    empty_set = set()
    if args.empty_blocks:
        for s in args.empty_blocks.split():
            bx, by, bz = map(int, s.split(','))
            empty_set.add((bx, by, bz))
    exclude_set = set()
    if args.exclude_global:
        for s in args.exclude_global.split():
            bx, by, bz = map(int, s.split(','))
            exclude_set.add((bx, by, bz))
    subs = []
    for s in args.sub_solid_block:
        parts = s.split()
        if len(parts) != 2:
            raise ValueError(f"Invalid --sub-solid-block format: {s}")
        block_str, region_str = parts
        bx, by, bz = map(int, block_str.split(','))
        minx, miny, minz, maxx, maxy, maxz = map(int, region_str.split(','))
        subs.append((bx, by, bz, minx, miny, minz, maxx, maxy, maxz, True))
    for s in args.sub_empty_block:
        parts = s.split()
        if len(parts) != 2:
            raise ValueError(f"Invalid --sub-empty-block format: {s}")
        block_str, region_str = parts
        bx, by, bz = map(int, block_str.split(','))
        minx, miny, minz, maxx, maxy, maxz = map(int, region_str.split(','))
        subs.append((bx, by, bz, minx, miny, minz, maxx, maxy, maxz, False))
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
    overall_voxels = parse_bbmodel(main_path, args.thresh, args.no_postprocess, args.no_holes, args.no_gaps, args.no_small_voids, args.gap_passes, args.void_thresh, args.occ_thresh, global_postprocess, set(args.pbg.lower().split(',') if args.pbg else ''), device, args.single_thread, set(), set(), args.fill_all_voids, exclude_set, axis_order, args.mi, args.ex_thresh, args.rpp, return_voxels=True, pp_order=args.pp_order, sub_order=args.sub_pp_order)
    cache = {}
    unique_supps = set(s_file for s_file, _ in supp_list)
    for s_file in unique_supps:
        print(f"Processing supplementary model: {s_file}")
        s_path = os.path.join(directory, s_file)
        s_voxels = parse_bbmodel(s_path, args.thresh, args.no_postprocess, args.no_holes, args.no_gaps, args.no_small_voids, args.gap_passes, args.void_thresh, args.occ_thresh, global_postprocess, set(args.pbg.lower().split(',') if args.pbg else ''), device, args.single_thread, set(), set(), args.fill_all_voids, exclude_set, axis_order, args.mi, args.ex_thresh, args.rpp, return_voxels=True, pp_order=args.pp_order, sub_order=args.sub_pp_order)
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
    # Force solid and empty blocks after combining
    res = 16
    for b in solid_set:
        overall_dict[b] = np.ones((res, res, res), dtype=bool)
    for b in empty_set:
        overall_dict[b] = np.zeros((res, res, res), dtype=bool)
    # Apply post-processing to the combined model
    block_occupied = overall_dict
    if not args.no_postprocess:
        pp_order_list = [s.strip() for s in args.pp_order.split(',')]
        thresholds = (x_threshold, y_threshold, z_threshold)
        ex_thresholds = (ex_x_threshold, ex_y_threshold, ex_z_threshold)
        max_intrude_dict = {0: max_intrude_x, 1: max_intrude_y, 2: max_intrude_z}
        per_block_gap_axes = set(args.pbg.lower().split(',') if args.pbg else '')
        no_holes = args.no_holes
        no_gaps = args.no_gaps
        no_small_voids = args.no_small_voids
        gap_passes = args.gap_passes
        void_thresh = args.void_thresh
        occ_thresh = args.occ_thresh
        sub_order = args.sub_pp_order
        block_occupied = apply_postprocessing(block_occupied, no_holes, no_gaps, no_small_voids, gap_passes, axis_order, thresholds, void_thresh, occ_thresh, fill_all_voids, regions, exclude_set, ex_thresholds, max_intrude_dict, per_block_gap_axes, sub_order, pp_order_list, subs, res=16)
    if args.mc_version == '1.12.2':
        max_bz = max(bz for bx,by,bz in block_occupied) if block_occupied else 0
        new_dict = {}
        for bx,by,bz in list(block_occupied):
            new_bz = max_bz - bz
            occupied_np = block_occupied[(bx,by,bz)]
            new_dict[(bx,by,new_bz)] = occupied_np
        block_occupied = new_dict
    # Extract AABBs
    overall_aabbs = []
    for (bx, by, bz), occupied_np in sorted(block_occupied.items(), key=lambda k: k[0]):
        if args.low_res_merge:
            low_res = 8
            occupied_low = np.zeros((low_res, low_res, low_res), dtype=bool)
            for i in range(low_res):
                for j in range(low_res):
                    for k in range(low_res):
                        sub = occupied_np[2*i:2*(i+1), 2*j:2*(j+1), 2*k:2*(k+1)]
                        if np.any(sub):
                            occupied_low[i, j, k] = True
            block_aabbs = extract_aabbs_from_occupied(occupied_low, res=low_res, force_merge=True)
        else:
            block_aabbs = extract_aabbs_from_occupied(occupied_np, res=16)
        if block_aabbs:
            overall_aabbs.append((bx, by, bz, block_aabbs))
    # Apply AABB copies
    copies = []
    for c in args.copy_aabb:
        parts = c.split()
        if len(parts) != 2:
            raise ValueError(f"Invalid --copy-aabb format: {c}")
        from_str, to_str = parts
        from_b = tuple(map(int, from_str.split(',')))
        to_b = tuple(map(int, to_str.split(',')))
        copies.append((from_b, to_b))
    for from_b, to_b in copies:
        source_aabbs = None
        for i, (bx, by, bz, block_aabbs) in enumerate(overall_aabbs):
            if (bx, by, bz) == from_b:
                source_aabbs = block_aabbs
                break
        if source_aabbs is None:
            raise ValueError(f"Source block {from_b} not found")
        found = False
        for i, (bx, by, bz, block_aabbs) in enumerate(overall_aabbs):
            if (bx, by, bz) == to_b:
                overall_aabbs[i] = (bx, by, bz, source_aabbs[:])
                found = True
                break
        if not found:
            overall_aabbs.append((to_b[0], to_b[1], to_b[2], source_aabbs[:]))
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