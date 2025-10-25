# bb_model_parser.py
import json
import math
import numpy as np
import torch
import sys
import os
from tqdm import tqdm
import threading
import multiprocessing as mp
from bb_postprocess import process_post, fill_gaps_along_axis, remove_protrusions
from bb_utils import find_max_box, merge_aabbs, parse_thresh_val
from bb_voxelization import process_block
from bb_utils import init_worker
try:
    import torch_directml
except ImportError:
    torch_directml = None

# Main model parsing function
def parse_bbmodel(file_path, thresh_str, no_postprocess, no_holes, no_gaps, no_small_voids, gap_passes, small_void_threshold, small_occupied_threshold, global_postprocess, per_block_gap_axes, device=None, single_thread=False, solid_set=set(), fill_all_voids=False, exclude_set=set(), axis_order=[0,2,1], mi_str="3,4,4", ex_thresh_str="d,d,d", rpp_list=[], return_voxels=False, pp_order='per-block,regional,global,per-block-gaps,protrusions', sub_order='remove-small,fill-holes,fill-voids,fill-gaps'):
    # Parse thresholds and settings
    thresh_parts = thresh_str.split(',')
    x_threshold = parse_thresh_val(thresh_parts[0], 2)
    y_threshold = parse_thresh_val(thresh_parts[1], 4)
    z_threshold = parse_thresh_val(thresh_parts[2], 2)
    default_mi = (3, 4, 4)
    mi_parts = mi_str.split(',')
    max_intrude_x = parse_thresh_val(mi_parts[0], default_mi[0])
    max_intrude_y = parse_thresh_val(mi_parts[1], default_mi[1])
    max_intrude_z = parse_thresh_val(mi_parts[2], default_mi[2])
    ex_thresh_parts = ex_thresh_str.split(',')
    ex_x_threshold = parse_thresh_val(ex_thresh_parts[0], x_threshold)
    ex_y_threshold = parse_thresh_val(ex_thresh_parts[1], y_threshold)
    ex_z_threshold = parse_thresh_val(ex_thresh_parts[2], z_threshold)
    
    # Process regional post-processing arguments
    regions = []
    all_region_blocks = set()
    for rpp_str in rpp_list:
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
    
    # Load and validate BBModel data
    with open(file_path, 'r') as f:
        data = json.load(f)
    elements = data.get('elements', [])
    if not elements: raise ValueError("No elements found in BBModel file.")
    grid_size = 1/16
    for element in elements:
        vertices = element.get('vertices', {})
        for key, coord in vertices.items():
            for c in coord:
                snapped = round(c / grid_size) * grid_size
                if abs(c - snapped) > 1e-6:
                    print(f"Error: Vertex {key} in {file_path} has coordinate {c} not aligned to 1/16 grid.")
                    print("Please run bb_sterilize.py to snap vertices to the grid:")
                    print(f" python bb_sterilize.py {file_path}")
                    print("This will create a sterilized version of the model with grid-aligned vertices.")
                    sys.exit(1)
    
    # Process elements: vertices, triangles, edges
    all_verts = []
    all_triangles = []
    all_edges = set()
    all_edge_freq = {}
    vert_offset = 0
    min_feature_size = float('inf')
    has_fraction = False
    for element in elements:
        verts_dict = element.get('vertices', {})
        if not verts_dict: continue
        local_vert_keys = list(verts_dict.keys())
        local_verts = [verts_dict[k] for k in local_vert_keys]
        all_verts.extend(local_verts)
        for coord in local_verts:
            for c in coord:
                if c % 1 != 0:
                    has_fraction = True
        faces = element.get('faces', {})
        local_triangles = []
        for face in faces.values():
            fv_keys = face.get('vertices', [])
            if len(fv_keys) < 3: continue
            fv_indices = [local_vert_keys.index(k) + vert_offset for k in fv_keys]
            for i in range(1, len(fv_indices) - 1):
                local_triangles.append((fv_indices[0], fv_indices[i], fv_indices[i+1]))
        all_triangles.extend(local_triangles)
        for tri in local_triangles:
            for pair in [(tri[0], tri[1]), (tri[1], tri[2]), (tri[2], tri[0])]:
                e = tuple(sorted(pair))
                all_edge_freq[e] = all_edge_freq.get(e, 0) + 1
        for i in range(len(local_verts)):
            for j in range(i+1, len(local_verts)):
                dist = math.sqrt(sum((local_verts[i][k] - local_verts[j][k])**2 for k in range(3)))
                if dist > 0: min_feature_size = min(min_feature_size, dist)
        for face in faces.values():
            fv_keys = face.get('vertices', [])
            for i in range(len(fv_keys)):
                k1 = fv_keys[i]
                k2 = fv_keys[(i + 1) % len(fv_keys)]
                i1 = local_vert_keys.index(k1) + vert_offset
                i2 = local_vert_keys.index(k2) + vert_offset
                if i1 > i2: i1, i2 = i2, i1
                all_edges.add((i1, i2))
        vert_offset += len(local_verts)
    if not all_triangles: raise ValueError("No faces found.")
    verts = all_verts
    triangles = [(all_verts[a], all_verts[b], all_verts[c]) for a, b, c in all_triangles]
    edges = list(all_edges)
    is_watertight = all(v == 2 for v in all_edge_freq.values())
    has_thin_features = has_fraction or min_feature_size < 0.5 if min_feature_size != float('inf') else False
    is_watertight = not has_thin_features
    minx = min(v[0] for v in verts)
    maxx = max(v[0] for v in verts)
    miny = min(v[1] for v in verts)
    maxy = max(v[1] for v in verts)
    minz = min(v[2] for v in verts)
    maxz = max(v[2] for v in verts)
    num_bx = math.ceil((maxx - minx) / 16)
    num_by = math.ceil((maxy - miny) / 16)
    num_bz = math.ceil((maxz - minz) / 16)
    res = 16
    dtype = torch.float32
    if is_watertight:
        directions = [[1.0, 1e-5, 1e-5], [1e-5, 1.0, 1e-5], [1e-5, 1e-5, 1.0]]
    else:
        directions = [[1.0, 1e-5, 1e-5], [-1.0, -1e-5, -1e-5], [1e-5, 1.0, 1e-5], [1e-5, -1.0, 1e-5], [1e-5, 1e-5, 1.0], [1e-5, 1e-5, -1.0]]
    directions_t = torch.tensor(directions, dtype=dtype, device=device)
    voxel_size = 16 / res
    delta = voxel_size * 0.49
    if has_thin_features:
        offsets = [[0, 0, 0], [delta, 0, 0], [-delta, 0, 0], [0, delta, 0], [0, -delta, 0], [0, 0, delta], [0, 0, -delta], [delta, delta, 0], [delta, -delta, 0], [-delta, delta, 0], [-delta, -delta, 0], [delta, 0, delta], [delta, 0, -delta], [-delta, 0, delta], [-delta, 0, -delta], [0, delta, delta], [0, delta, -delta], [0, -delta, delta], [0, -delta, -delta], [delta, delta, delta], [delta, delta, -delta], [delta, -delta, delta], [delta, -delta, -delta], [-delta, delta, delta], [-delta, delta, -delta], [-delta, -delta, delta], [-delta, -delta, -delta]]
    else:
        offsets = [[0, 0, 0], [delta, 0, 0], [-delta, 0, 0], [0, delta, 0], [0, -delta, 0], [0, 0, delta], [0, 0, -delta]]
    offsets_t = torch.tensor(offsets, dtype=dtype, device=device)
    if device.type == 'cpu':
        directions_t.share_memory_()
        offsets_t.share_memory_()
    args_list = []
    for bx in range(num_bx):
        for by in range(num_by):
            for bz in range(num_bz):
                args_list.append((bx, by, bz, minx, miny, minz, verts, triangles, edges, res, x_threshold, y_threshold, z_threshold, is_watertight, has_thin_features, no_postprocess, no_holes, no_gaps, no_small_voids, gap_passes, small_void_threshold, small_occupied_threshold, device, solid_set, directions_t, offsets_t, fill_all_voids, axis_order))
    def refresher(pbar, stop_event):
        while not stop_event.wait(1):
            pbar.refresh()
    results = []
    stop_event = threading.Event()
    with tqdm(total=len(args_list), desc=os.path.basename(file_path)) as pbar:
        thread = threading.Thread(target=refresher, args=(pbar, stop_event))
        thread.start()
        if device.type == 'cpu' and not single_thread:
            num_processes = max(1, mp.cpu_count() - 1)
            chunksize = max(1, len(args_list) // num_processes)
            with mp.Pool(processes=num_processes, initializer=init_worker) as pool:
                for result in pool.imap_unordered(process_block, args_list, chunksize=chunksize):
                    if result is not None:
                        results.append(result)
                    pbar.update(1)
        else:
            for args in args_list:
                result = process_block(args)
                if result is not None:
                    results.append(result)
                pbar.update(1)
                if device.type == 'directml':
                    torch_directml.gc()
        stop_event.set()
        thread.join()
    raw_results = [r for r in results if r is not None]
    block_occupied = {(bx, by, bz): occupied_np for bx, by, bz, occupied_np in raw_results}
    if not no_postprocess:
        pp_order_list = [s.strip() for s in pp_order.split(',')]
        max_intrude_dict = {0: max_intrude_x, 1: max_intrude_y, 2: max_intrude_z}
        for step in pp_order_list:
            if step == 'per-block':
                for b in block_occupied:
                    thresh = (x_threshold, y_threshold, z_threshold)
                    block_occupied[b] = process_post(block_occupied[b], no_holes, no_gaps, no_small_voids, gap_passes, axis_order, thresh, small_void_threshold, small_occupied_threshold, fill_all_voids, sub_order=sub_order)
            elif step == 'regional':
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
                        if b not in block_occupied:
                            continue
                        off_x = (b[0] - min_bx_r) * res
                        off_y = (b[1] - min_by_r) * res
                        off_z = (b[2] - min_bz_r) * res
                        sub_occupied[off_x:off_x + res, off_y:off_y + res, off_z:off_z + res] = block_occupied[b]
                    sub_occupied = process_post(sub_occupied, no_holes, no_gaps, no_small_voids, gap_passes, axis_order, region['thresholds'], small_void_threshold, small_occupied_threshold, fill_all_voids, sub_order=sub_order)
                    for b in reg_blocks:
                        off_x = (b[0] - min_bx_r) * res
                        off_y = (b[1] - min_by_r) * res
                        off_z = (b[2] - min_bz_r) * res
                        block_occupied[b] = sub_occupied[off_x:off_x + res, off_y:off_y + res, off_z:off_z + res].copy()
            elif step == 'global':
                excluded_processed = {}
                for ex_b in exclude_set:
                    if ex_b not in block_occupied:
                        continue
                    occupied_np_copy = block_occupied[ex_b].copy()
                    thresh_dict_ex = (ex_x_threshold, ex_y_threshold, ex_z_threshold)
                    occupied_np_copy = process_post(occupied_np_copy, no_holes, no_gaps, no_small_voids, gap_passes, axis_order, thresh_dict_ex, small_void_threshold, small_occupied_threshold, fill_all_voids, sub_order=sub_order)
                    excluded_processed[ex_b] = occupied_np_copy
                min_bx = min(bx for bx, _, _ in block_occupied) if block_occupied else 0
                min_by = min(by for _, by, _ in block_occupied) if block_occupied else 0
                min_bz = min(bz for _, _, bz in block_occupied) if block_occupied else 0
                max_bx = max(bx for bx, _, _ in block_occupied) if block_occupied else 0
                max_by = max(by for _, by, _ in block_occupied) if block_occupied else 0
                max_bz = max(bz for _, _, bz in block_occupied) if block_occupied else 0
                num_bx = max_bx - min_bx + 1
                num_by = max_by - min_by + 1
                num_bz = max_bz - min_bz + 1
                full_occupied = np.zeros((num_bx * res, num_by * res, num_bz * res), dtype=bool)
                for b in block_occupied:
                    bx, by, bz = b
                    off_x = (bx - min_bx) * res
                    off_y = (by - min_by) * res
                    off_z = (bz - min_bz) * res
                    full_occupied[off_x:off_x + res, off_y:off_y + res, off_z:off_z + res] = block_occupied[b]
                is_excluded_full = np.zeros_like(full_occupied, dtype=bool)
                for ex_b in exclude_set:
                    if ex_b in block_occupied:
                        bx, by, bz = ex_b
                        off_x = (bx - min_bx) * res
                        off_y = (by - min_by) * res
                        off_z = (bz - min_bz) * res
                        is_excluded_full[off_x:off_x + res, off_y:off_y + res, off_z:off_z + res] = True
                for region in regions:
                    for b in region['blocks']:
                        if b in block_occupied:
                            bx, by, bz = b
                            off_x = (bx - min_bx) * res
                            off_y = (by - min_by) * res
                            off_z = (bz - min_bz) * res
                            is_excluded_full[off_x:off_x + res, off_y:off_y + res, off_z:off_z + res] = True
                full_occupied = process_post(full_occupied, no_holes, no_gaps, no_small_voids, gap_passes, axis_order, (x_threshold, y_threshold, z_threshold), small_void_threshold, small_occupied_threshold, fill_all_voids, is_excluded=is_excluded_full, max_intrude_dict=max_intrude_dict, ex_thresholds=(ex_x_threshold, ex_y_threshold, ex_z_threshold), sub_order=sub_order)
                for ex_b in excluded_processed:
                    bx, by, bz = ex_b
                    off_x = (bx - min_bx) * res
                    off_y = (by - min_by) * res
                    off_z = (bz - min_bz) * res
                    full_occupied[off_x:off_x + res, off_y:off_y + res, off_z:off_z + res] = excluded_processed[ex_b]
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
                        bx, by, bz = b
                        off_x = (b[0] - min_bx_r) * res
                        off_y = (b[1] - min_by_r) * res
                        off_z = (b[2] - min_bz_r) * res
                        sub_occupied[off_x:off_x + res, off_y:off_y + res, off_z:off_z + res] = full_occupied[(b[0] - min_bx) * res:(b[0] - min_bx + 1) * res, (b[1] - min_by) * res:(b[1] - min_by + 1) * res, (b[2] - min_bz) * res:(b[2] - min_bz + 1) * res]
                    sub_occupied = process_post(sub_occupied, no_holes, no_gaps, no_small_voids, gap_passes, axis_order, region['thresholds'], small_void_threshold, small_occupied_threshold, fill_all_voids, sub_order=sub_order)
                    for b in reg_blocks:
                        bx, by, bz = b
                        off_x = (b[0] - min_bx_r) * res
                        off_y = (b[1] - min_by_r) * res
                        off_z = (b[2] - min_bz_r) * res
                        full_occupied[(b[0] - min_bx) * res:(b[0] - min_bx + 1) * res, (b[1] - min_by) * res:(b[1] - min_by + 1) * res, (b[2] - min_bz) * res:(b[2] - min_bz + 1) * res] = sub_occupied[off_x:off_x + res, off_y:off_y + res, off_z:off_z + res]
                for b in block_occupied:
                    bx, by, bz = b
                    off_x = (bx - min_bx) * res
                    off_y = (by - min_by) * res
                    off_z = (bz - min_bz) * res
                    block_occupied[b] = full_occupied[off_x:off_x + res, off_y:off_y + res, off_z:off_z + res].copy()
            elif step == 'per-block-gaps':
                for b in block_occupied:
                    occupied_np = block_occupied[b]
                    if not no_gaps:
                        for _ in range(gap_passes):
                            for axis in axis_order:
                                if axis == 0 and 'x' in per_block_gap_axes or axis == 1 and 'y' in per_block_gap_axes or axis == 2 and 'z' in per_block_gap_axes:
                                    thresh = {0: x_threshold, 1: y_threshold, 2: z_threshold}[axis]
                                    if thresh >= 0:
                                        occupied_np = fill_gaps_along_axis(occupied_np, axis, thresh)
                    block_occupied[b] = occupied_np
            elif step == 'protrusions':
                min_bx = min(bx for bx, _, _ in block_occupied) if block_occupied else 0
                min_by = min(by for _, by, _ in block_occupied) if block_occupied else 0
                min_bz = min(bz for _, _, bz in block_occupied) if block_occupied else 0
                max_bx = max(bx for bx, _, _ in block_occupied) if block_occupied else 0
                max_by = max(by for _, by, _ in block_occupied) if block_occupied else 0
                max_bz = max(bz for _, _, bz in block_occupied) if block_occupied else 0
                num_bx = max_bx - min_bx + 1
                num_by = max_by - min_by + 1
                num_bz = max_bz - min_bz + 1
                full_occupied = np.zeros((num_bx * res, num_by * res, num_bz * res), dtype=bool)
                for b in block_occupied:
                    bx, by, bz = b
                    off_x = (bx - min_bx) * res
                    off_y = (by - min_by) * res
                    off_z = (bz - min_bz) * res
                    full_occupied[off_x:off_x + res, off_y:off_y + res, off_z:off_z + res] = block_occupied[b]
                full_occupied = remove_protrusions(full_occupied)
                for b in block_occupied:
                    bx, by, bz = b
                    off_x = (bx - min_bx) * res
                    off_y = (by - min_by) * res
                    off_z = (bz - min_bz) * res
                    block_occupied[b] = full_occupied[off_x:off_x + res, off_y:off_y + res, off_z:off_z + res].copy()
    if return_voxels:
        overall_voxels = []
        for bx in range(num_bx):
            for by in range(num_by):
                for bz in range(num_bz):
                    b = (bx, by, bz)
                    if b in block_occupied:
                        occupied_np = block_occupied[b]
                        if occupied_np.sum() > 0:
                            overall_voxels.append((bx, by, bz, occupied_np))
        return normalize_offsets(overall_voxels)
    aabbs = []
    for bx in range(num_bx):
        for by in range(num_by):
            for bz in range(num_bz):
                b = (bx, by, bz)
                if b not in block_occupied:
                    continue
                occupied_np = block_occupied[b].copy()
                if occupied_np.sum() == res**3:
                    aabbs.append((bx, by, bz, [(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)]))
                    continue
                occupied = occupied_np.tolist()
                num_occupied = np.sum(occupied_np)
                block_aabbs = []
                visited = [[[False for _ in range(res)] for _ in range(res)] for _ in range(res)]
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
                block_aabbs = [a for a in block_aabbs if (a[3]-a[0])*(a[4]-a[1])*(a[5]-a[2]) > 0.001]
                pre_merge = list(block_aabbs)
                block_aabbs = merge_aabbs(block_aabbs)
                vox_vol = num_occupied / res**3
                aabb_vol = sum((a[3] - a[0]) * (a[4] - a[1]) * (a[5] - a[2]) for a in block_aabbs)
                if abs(aabb_vol - vox_vol) > 0.01 * vox_vol:
                    block_aabbs = pre_merge
                if block_aabbs:
                    aabbs.append((bx, by, bz, block_aabbs))
    if num_bx == 1 and num_by == 1 and num_bz == 1 and aabbs:
        block_aabbs = aabbs[0][3]
        overall_minx = min(minx for minx, _, _, _, _, _ in block_aabbs)
        overall_maxx = max(maxx for _, _, _, maxx, _, _ in block_aabbs)
        overall_miny = min(miny for _, miny, _, _, _, _ in block_aabbs)
        overall_maxy = max(maxy for _, _, _, _, maxy, _ in block_aabbs)
        overall_minz = min(minz for _, _, minz, _, _, _ in block_aabbs)
        overall_maxz = max(maxz for _, _, _, _, _, maxz in block_aabbs)
        center_x = (overall_minx + overall_maxx) / 2
        center_y = (overall_miny + overall_maxy) / 2
        center_z = (overall_minz + overall_maxz) / 2
        shift_x = 0.5 - center_x
        shift_y = 0.5 - center_y
        shift_z = 0.5 - center_z
        shifted_aabbs = []
        for minx, miny, minz, maxx, maxy, maxz in block_aabbs:
            shifted_aabbs.append((minx + shift_x, miny + shift_y, minz + shift_z, maxx + shift_x, maxy + shift_y, maxz + shift_z))
        aabbs[0] = (0, 0, 0, shifted_aabbs)
    return normalize_offsets(aabbs)

# Utility functions for file handling and selection
def list_bbmodel_files(directory):
    return [f for f in os.listdir(directory) if f.lower().endswith('.bbmodel')]

def select_file(bbmodel_files, prompt="Select a file by number: "):
    if not bbmodel_files: raise ValueError("No BBModel files found in the directory.")
    print("Available BBModel files:")
    for i, file in enumerate(bbmodel_files, 1): print(f"{i}. {file}")
    while True:
        try:
            choice = int(input(prompt))
            if 1 <= choice <= len(bbmodel_files): return bbmodel_files[choice - 1]
            else: print("Invalid selection. Try again.")
        except ValueError: print("Please enter a number.")

# Normalization function
def normalize_offsets(aabbs):
    if not aabbs: return aabbs
    min_bx = min(bx for bx, _, _, _ in aabbs)
    min_by = min(by for _, by, _, _ in aabbs)
    min_bz = min(bz for _, _, bz, _ in aabbs)
    shift_bx = -min_bx
    shift_by = -min_by
    shift_bz = -min_bz
    normalized = [(bx + shift_bx, by + shift_by, bz + shift_bz, block_aabbs) for bx, by, bz, block_aabbs in aabbs]
    return normalized