import sys
import os
import json
import math
import numpy as np
from scipy import ndimage
from tqdm import tqdm
import argparse
import time
import torch
import threading
import multiprocessing as mp
import itertools
try:
    import torch_directml
except ImportError:
    torch_directml = None
# Comment section:
# - This script voxelizes .bbmodel files to generate AABB lists for multiblock structures.
# - Script runs at MC compatible resolution of 16 (0.0625 pixel size).
# - JSON output format: {"shapeAABB": [null or [[minx, miny, minz, maxx, maxy, maxz], ...]]}
# - JSON null: Represents air (no collision box, empty space).
# - List of boxes: Collisions, with values as floats (0.0-1.0); full block is [[0.0, 0.0, 0.0, 1.0, 1.0, 1.0]].
# - The flat array index is calculated as by * (width * length) + bz * width + bx, matching Java positional order (bX, bY, bZ starting from 0,0,0).
# - No flips or reversals applied; output matches raw Java if-else order and values for direct loading in shape class.
# - Supplementary models are added with offsets and normalized to min 0.
# - Java output is if-else AABB, JSON is an array, both for manual copy to respective files.
def init_worker():
    os.environ['OMP_NUM_THREADS'] = '1'
    torch.set_num_threads(1)
def find_max_box(occupied, visited, i, j, k, res):
    best_volume = 0
    best_dx, best_dy, best_dz = 1, 1, 1
    for order in itertools.permutations([0, 1, 2]):
        dx, dy, dz = 1, 1, 1
        for dim in order:
            if dim == 0:
                while i + dx < res:
                    if all(occupied[i + dx][y][z] and not visited[i + dx][y][z] for y in range(j, j + dy) for z in range(k, k + dz)):
                        dx += 1
                    else: break
            elif dim == 1:
                while j + dy < res:
                    if all(occupied[x][j + dy][z] and not visited[x][j + dy][z] for x in range(i, i + dx) for z in range(k, k + dz)):
                        dy += 1
                    else: break
            else:
                while k + dz < res:
                    if all(occupied[x][y][k + dz] and not visited[x][y][k + dz] for x in range(i, i + dx) for y in range(j, j + dy)):
                        dz += 1
                    else: break
        volume = dx * dy * dz
        if volume > best_volume:
            best_volume = volume
            best_dx, best_dy, best_dz = dx, dy, dz
    for x in range(i, i + best_dx):
        for y in range(j, j + best_dy):
            for z in range(k, k + best_dz):
                visited[x][y][z] = True
    return best_dx, best_dy, best_dz
def merge_along_dim(aabbs, dim):
    epsilon = 1e-3
    if dim == 0:
        sort_key = lambda x: (x[1], x[4], x[2], x[5], x[0])
        min_idx, max_idx = 0, 3
        fixed_dims = [(1,4), (2,5)]
    elif dim == 1:
        sort_key = lambda x: (x[0], x[3], x[2], x[5], x[1])
        min_idx, max_idx = 1, 4
        fixed_dims = [(0,3), (2,5)]
    else:
        sort_key = lambda x: (x[0], x[3], x[1], x[4], x[2])
        min_idx, max_idx = 2, 5
        fixed_dims = [(0,3), (1,4)]
    aabbs = sorted(aabbs, key=sort_key)
    merged = []
    i = 0
    while i < len(aabbs):
        current = list(aabbs[i])
        i += 1
        while i < len(aabbs):
            next_aabb = aabbs[i]
            can_merge = abs(current[max_idx] - next_aabb[min_idx]) < epsilon
            for minf, maxf in fixed_dims:
                can_merge &= abs(current[minf] - next_aabb[minf]) < epsilon and abs(current[maxf] - next_aabb[maxf]) < epsilon
            if can_merge:
                current[max_idx] = max(current[max_idx], next_aabb[max_idx])
                i += 1
            else:
                break
        merged.append(tuple(current))
    return merged
def merge_aabbs(aabbs):
    if not aabbs: return []
    previous_len = -1
    while len(aabbs) != previous_len:
        previous_len = len(aabbs)
        for dim in range(3):
            aabbs = merge_along_dim(aabbs, dim)
    return aabbs
def fill_gaps_along_axis(occupied_np, axis, threshold):
    if threshold < 0:
        return occupied_np
    shape = occupied_np.shape
    if axis == 0: # x
        for y in range(shape[1]):
            for z in range(shape[2]):
                for x in range(1, shape[0] - 1):
                    if occupied_np[x, y, z]:
                        continue
                    left = None
                    max_dx = threshold + 1 if threshold > 0 else shape[0]
                    for dx in range(1, max_dx):
                        if x - dx < 0:
                            break
                        if occupied_np[x - dx, y, z]:
                            left = x - dx
                            break
                    right = None
                    for dx in range(1, max_dx):
                        if x + dx >= shape[0]:
                            break
                        if occupied_np[x + dx, y, z]:
                            right = x + dx
                            break
                    if left is not None and right is not None and (right - left - 1) <= threshold:
                        occupied_np[left + 1:right, y, z] = True
    elif axis == 1: # y
        for x in range(shape[0]):
            for z in range(shape[2]):
                for y in range(1, shape[1] - 1):
                    if occupied_np[x, y, z]:
                        continue
                    floor_y = None
                    max_dy = threshold + 1 if threshold > 0 else shape[1]
                    for dy in range(1, max_dy):
                        if y - dy < 0:
                            break
                        if occupied_np[x, y - dy, z]:
                            floor_y = y - dy
                            break
                    ceiling_y = None
                    for dy in range(1, max_dy):
                        if y + dy >= shape[1]:
                            break
                        if occupied_np[x, y + dy, z]:
                            ceiling_y = y + dy
                            break
                    if floor_y is not None and ceiling_y is not None and (ceiling_y - floor_y - 1) <= threshold:
                        occupied_np[x, floor_y + 1:ceiling_y, z] = True
    elif axis == 2: # z
        for x in range(shape[0]):
            for y in range(shape[1]):
                for z in range(1, shape[2] - 1):
                    if occupied_np[x, y, z]:
                        continue
                    front = None
                    max_dz = threshold + 1 if threshold > 0 else shape[2]
                    for dz in range(1, max_dz):
                        if z - dz < 0:
                            break
                        if occupied_np[x, y, z - dz]:
                            front = z - dz
                            break
                    back = None
                    for dz in range(1, max_dz):
                        if z + dz >= shape[2]:
                            break
                        if occupied_np[x, y, z + dz]:
                            back = z + dz
                            break
                    if front is not None and back is not None and (back - front - 1) <= threshold:
                        occupied_np[x, y, front + 1:back] = True
    return occupied_np
# === Alternator, Boiler, Solar Tower Detection ===
# This section processes block-level geometry for Alternator, Boiler, and Solar Tower models.
# It voxelizes the model, detecting intersections with vertices and edges, and determines occupancy using ray-casting.
# For Alternator: Successfully generates accurate AABBs, correctly capturing all surfaces.
# For Boiler: Detects most surfaces but struggles with corner detection, leading to missing AABBs on one side despite symmetry.
# For Solar Tower: Detects some faces but misses many open to the outside inside faces and outside faces, indicating insufficient directional sampling.
# Updates: Added corner offsets (now 27 total for thin features) to better capture corner details in models like Boiler.
# Increased intersection count for non-watertight meshes to 4 (from 3) to reduce artifacts in open internal spaces for models like Solar Tower by making inside detection more strict.
# Change: Added solid_set param to force specific blocks full before ray-casting/postprocess, skipping detection.
def process_block(args):
    bx, by, bz, minx, miny, minz, verts, triangles, edges, res, x_threshold, y_threshold, z_threshold, is_watertight, has_thin_features, no_postprocess, no_holes, no_gaps, no_small_voids, gap_passes, small_void_threshold, small_occupied_threshold, global_postprocess, device, use_fp16, solid_set = args
    # Force solid blocks before any detection/postprocess
    if solid_set and (bx, by, bz) in solid_set:
        if global_postprocess:
            occupied_np = np.ones((res, res, res), dtype=bool)
            return (bx, by, bz, occupied_np)
        else:
            return (bx, by, bz, [(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)])
    dtype = torch.float16 if use_fp16 and device.type != 'cpu' else torch.float32
    small = 1e-4 if dtype == torch.float16 else 1e-6
    block_min = [minx + bx * 16, miny + by * 16, minz + bz * 16]
    block_max = [block_min[0] + 16, block_min[1] + 16, block_min[2] + 16]
    block_center = [(block_min[d] + block_max[d]) / 2 for d in range(3)]
    points = []
    for v in verts:
        if all(block_min[j] <= v[j] < block_max[j] for j in range(3)): points.append(v)
    has_intersection = len(points) > 0
    for i1, i2 in edges:
        v1 = verts[i1]
        v2 = verts[i2]
        for dim in range(3):
            for side in [block_min[dim], block_max[dim]]:
                denom = v2[dim] - v1[dim]
                if abs(denom) < small: continue
                t = (side - v1[dim]) / denom
                if 0 <= t <= 1:
                    inter = [v1[k] + t * (v2[k] - v1[k]) for k in range(3)]
                    if all(block_min[k] <= inter[k] < block_max[k] for k in range(3) if k != dim):
                        points.append(inter)
                        has_intersection = True
    voxel_size = 16 / res
    delta = voxel_size * 0.49
    if has_thin_features:
        offsets = [
            [0, 0, 0],
            [delta, 0, 0], [-delta, 0, 0],
            [0, delta, 0], [0, -delta, 0],
            [0, 0, delta], [0, 0, -delta],
            [delta, delta, 0], [delta, -delta, 0], [-delta, delta, 0], [-delta, -delta, 0],
            [delta, 0, delta], [delta, 0, -delta], [-delta, 0, delta], [-delta, 0, -delta],
            [0, delta, delta], [0, delta, -delta], [0, -delta, delta], [0, -delta, -delta],
            [delta, delta, delta], [delta, delta, -delta], [delta, -delta, delta], [delta, -delta, -delta],
            [-delta, delta, delta], [-delta, delta, -delta], [-delta, -delta, delta], [-delta, -delta, -delta]
        ]
    else:
        offsets = [[0, 0, 0], [delta, 0, 0], [-delta, 0, 0], [0, delta, 0], [0, -delta, 0], [0, 0, delta], [0, 0, -delta]]
    if is_watertight:
        directions = [[1.0, 1e-5, 1e-5], [1e-5, 1.0, 1e-5], [1e-5, 1e-5, 1.0]]
        required = 2
        epsilon = small
    else:
        directions = [[1.0, 1e-5, 1e-5], [-1.0, -1e-5, -1e-5], [1e-5, 1.0, 1e-5], [1e-5, -1.0, 1e-5], [1e-5, 1e-5, 1.0], [1e-5, 1e-5, -1.0]]
        required = 4
        epsilon = -small
    directions_t = torch.tensor(directions, dtype=dtype, device=device)
    num_dir = len(directions)
    if has_intersection:
        centers_t = torch.stack([
            torch.tensor(block_min[0], dtype=dtype, device=device),
            torch.tensor(block_min[1], dtype=dtype, device=device),
            torch.tensor(block_min[2], dtype=dtype, device=device)
        ], dim=0)
        i = torch.arange(res, dtype=dtype, device=device)
        ii, jj, kk = torch.meshgrid(i, i, i, indexing='ij')
        centers = centers_t.view(1,1,1,3) + (torch.stack([ii, jj, kk], dim=-1) + 0.5) * voxel_size
        centers_flat = centers.view(-1, 3)
        num_vox = res ** 3
        offsets_t = torch.tensor(offsets, dtype=dtype, device=device)
        num_off = len(offsets)
    else:
        centers_flat = torch.tensor([block_center], dtype=dtype, device=device)
        num_vox = 1
        offsets_t = torch.tensor([[0.0, 0.0, 0.0]], dtype=dtype, device=device)
        num_off = 1
    all_points = centers_flat[:, None, :] + offsets_t[None, :, :]
    all_points_flat = all_points.view(-1, 3)
    num_points = len(all_points_flat)
    all_origins = all_points_flat[:, None, :].repeat(1, num_dir, 1).view(-1, 3)
    all_dirs = directions_t[None, :, :].repeat(num_points, 1, 1).view(-1, 3)
    num_rays = len(all_origins)
    T = len(triangles)
    if T == 0:
        inside = torch.zeros(num_points, dtype=torch.bool, device=device)
    else:
        tris_t = torch.tensor(triangles, dtype=dtype, device=device)
        v0 = tris_t[:, 0, :]
        v1 = tris_t[:, 1, :]
        v2 = tris_t[:, 2, :]
        edge1 = v1 - v0
        edge2 = v2 - v0
        v0_b = v0[None, :, :]
        edge1_b = edge1[None, :, :]
        edge2_b = edge2[None, :, :]
        target_elements = 50000000
        batch_size = max(1, int(target_elements // T))
        counts = torch.zeros(num_rays, dtype=torch.int32, device=device)
        with torch.no_grad():
            for start in range(0, num_rays, batch_size):
                end = min(start + batch_size, num_rays)
                o_batch = all_origins[start:end, None, :]
                d_batch = all_dirs[start:end, None, :]
                h = torch.stack([
                    d_batch[..., 1] * edge2_b[..., 2] - d_batch[..., 2] * edge2_b[..., 1],
                    d_batch[..., 2] * edge2_b[..., 0] - d_batch[..., 0] * edge2_b[..., 2],
                    d_batch[..., 0] * edge2_b[..., 1] - d_batch[..., 1] * edge2_b[..., 0]
                ], dim=-1)
                a = torch.einsum('btj,btj->bt', edge1_b, h)
                mask = torch.abs(a) >= small
                f = torch.zeros_like(a)
                f[mask] = 1.0 / a[mask]
                s = o_batch - v0_b
                u = f * torch.einsum('btj,btj->bt', s, h)
                mask &= (u >= 0.0) & (u <= 1.0)
                q = torch.stack([
                    s[..., 1] * edge1_b[..., 2] - s[..., 2] * edge1_b[..., 1],
                    s[..., 2] * edge1_b[..., 0] - s[..., 0] * edge1_b[..., 2],
                    s[..., 0] * edge1_b[..., 1] - s[..., 1] * edge1_b[..., 0]
                ], dim=-1)
                v = f * torch.einsum('btj,btj->bt', d_batch, q)
                mask &= (v >= 0.0) & (u + v <= 1.0)
                t = f * torch.einsum('btj,btj->bt', edge2_b, q)
                mask &= (t > epsilon)
                intersects = mask
                counts[start:end] = intersects.sum(dim=1, dtype=torch.int32)
                del o_batch, d_batch, h, a, mask, f, s, u, q, v, t, intersects
        odd = (counts % 2) == 1
        odd_per_dir = odd.view(num_points, num_dir)
        inside_dirs = odd_per_dir.sum(dim=1, dtype=torch.int32)
        inside = inside_dirs >= required
        del counts, odd, odd_per_dir, inside_dirs, tris_t, v0, v1, v2, edge1, edge2, v0_b, edge1_b, edge2_b
    inside_per_vox = inside.view(num_vox, num_off).any(dim=1)
    del inside, all_points, all_points_flat, all_origins, all_dirs, centers_flat, offsets_t, directions_t
    if has_intersection:
        occupied_np = inside_per_vox.view(res, res, res).cpu().numpy().astype(bool)
    else:
        occupied_np = inside_per_vox.view(1, 1, 1).cpu().numpy().astype(bool)
    del inside_per_vox
    if not has_intersection:
        if occupied_np[0, 0, 0]:
            if global_postprocess:
                occupied_np = np.ones((res, res, res), dtype=bool)
                return (bx, by, bz, occupied_np)
            return (bx, by, bz, [(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)])
        return None
    if global_postprocess:
        return (bx, by, bz, occupied_np)
    if no_postprocess:
        pass
    else:
        if not no_holes:
            occupied_np = ndimage.binary_fill_holes(occupied_np)
        if not no_gaps:
            for _ in range(gap_passes):
                occupied_np = fill_gaps_along_axis(occupied_np, 0, x_threshold) # x
            for _ in range(gap_passes):
                occupied_np = fill_gaps_along_axis(occupied_np, 2, z_threshold) # z
            for _ in range(gap_passes):
                occupied_np = fill_gaps_along_axis(occupied_np, 1, y_threshold) # y
        if not no_small_voids:
            unoccupied = ~occupied_np
            labels, num_labels = ndimage.label(unoccupied)
            if num_labels > 0:
                slices = ndimage.find_objects(labels)
                for lab in range(1, num_labels + 1):
                    sl = slices[lab - 1]
                    touches_boundary = any(s.start == 0 or s.stop == res for s in sl)
                    if not touches_boundary:
                        size = np.sum(labels == lab)
                        if size < small_void_threshold:
                            occupied_np[labels == lab] = True
            labels, num_labels = ndimage.label(occupied_np)
            if num_labels > 0:
                sizes = ndimage.sum(occupied_np, labels, range(1, num_labels + 1))
                small_mask = sizes < small_occupied_threshold
                remove_mask = np.isin(labels, np.where(small_mask)[0] + 1)
                occupied_np[remove_mask] = False
    occupied = occupied_np.tolist()
    num_occupied = sum(1 for i in range(res) for j in range(res) for k in range(res) if occupied[i][j][k])
    if num_occupied > res**3 * 0.95:
        return (bx, by, bz, [(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)])
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
    block_aabbs = merge_aabbs(block_aabbs)
    if block_aabbs: return (bx, by, bz, block_aabbs)
    return None
# === Alternator, Boiler, Solar Tower Parsing ===
# This section parses .bbmodel files for Alternator, Boiler, and Solar Tower, extracting vertices, triangles, and edges.
# For Alternator: Correctly parses geometry, leading to accurate AABB generation.
# For Boiler: Parses geometry but results in missing corner AABBs on one side, despite symmetry being detected correctly on the mirror side.
# For Solar Tower: Parses geometry but fails to capture many inside and outside faces, suggesting issues with directional detection in process_block.
# Change: Added solid_set param, appended to each block's args tuple for process_block.
def parse_bbmodel(file_path, x_threshold, y_threshold, z_threshold, no_postprocess, no_holes, no_gaps, no_small_voids, gap_passes, small_void_threshold, small_occupied_threshold, global_postprocess, per_block_gap_x=False, per_block_gap_y=False, per_block_gap_z=False, device=None, use_fp16=False, single_thread=False, solid_set=set()):
    with open(file_path, 'r') as f:
        data = json.load(f)
    elements = data.get('elements', [])
    if not elements: raise ValueError("No elements found in BBModel file.")
    # Check for non-grid-aligned vertices
    grid_size = 1/16 # 0.0625
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
    args_list = []
    for bx in range(num_bx):
        for by in range(num_by):
            for bz in range(num_bz):
                args_list.append((bx, by, bz, minx, miny, minz, verts, triangles, edges, res, x_threshold, y_threshold, z_threshold, is_watertight, has_thin_features, no_postprocess, no_holes, no_gaps, no_small_voids, gap_passes, small_void_threshold, small_occupied_threshold, global_postprocess, device, use_fp16, solid_set))
    def refresher(pbar, stop_event):
        while not stop_event.wait(1):
            pbar.refresh()
    results = []
    stop_event = threading.Event()
    with tqdm(total=len(args_list), desc=os.path.basename(file_path)) as pbar:
        thread = threading.Thread(target=refresher, args=(pbar, stop_event))
        thread.start()
        if device.type == 'cpu' and not single_thread:
            with mp.Pool(initializer=init_worker) as pool:
                for result in pool.imap_unordered(process_block, args_list):
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
    aabbs = results
    if global_postprocess:
        full_occupied = np.zeros((num_bx * res, num_by * res, num_bz * res), dtype=bool)
        for bx, by, bz, result in aabbs:
            full_occupied[bx*res:(bx+1)*res, by*res:(by+1)*res, bz*res:(bz+1)*res] = result
        if not no_postprocess:
            if not no_holes:
                full_occupied = ndimage.binary_fill_holes(full_occupied)
            if not no_gaps:
                axis_order = [0, 2, 1] # x, z, y
                thresholds = [x_threshold, z_threshold, y_threshold]
                per_blocks = [per_block_gap_x, per_block_gap_z, per_block_gap_y]
                for i in range(3):
                    axis = axis_order[i]
                    thresh = thresholds[i]
                    if thresh >= 0 and not per_blocks[i]:
                        for _ in range(gap_passes):
                            full_occupied = fill_gaps_along_axis(full_occupied, axis, thresh)
            if not no_small_voids:
                unoccupied = ~full_occupied
                labels, num_labels = ndimage.label(unoccupied)
                if num_labels > 0:
                    slices = ndimage.find_objects(labels)
                    for lab in range(1, num_labels + 1):
                        sl = slices[lab - 1]
                        touches_boundary = any(s.start == 0 or s.stop == full_occupied.shape[i] for i, s in enumerate(sl))
                        if not touches_boundary:
                            size = np.sum(labels == lab)
                            if size < small_void_threshold:
                                full_occupied[labels == lab] = True
                labels, num_labels = ndimage.label(full_occupied)
                if num_labels > 0:
                    sizes = ndimage.sum(full_occupied, labels, range(1, num_labels + 1))
                    small_mask = sizes < small_occupied_threshold
                    remove_mask = np.isin(labels, np.where(small_mask)[0] + 1)
                    full_occupied[remove_mask] = False
        aabbs = []
        for bx in range(num_bx):
            for by in range(num_by):
                for bz in range(num_bz):
                    occupied_np = full_occupied[bx*res:(bx+1)*res, by*res:(by+1)*res, bz*res:(bz+1)*res]
                    if not no_postprocess and not no_gaps:
                        axis_order = [0, 2, 1]
                        thresholds = [x_threshold, z_threshold, y_threshold]
                        per_blocks = [per_block_gap_x, per_block_gap_z, per_block_gap_y]
                        for i in range(3):
                            axis = axis_order[i]
                            thresh = thresholds[i]
                            if thresh >= 0 and per_blocks[i]:
                                for _ in range(gap_passes):
                                    occupied_np = fill_gaps_along_axis(occupied_np, axis, thresh)
                    occupied = occupied_np.tolist()
                    num_occupied = np.sum(occupied_np)
                    if num_occupied > res**3 * 0.95:
                        aabbs.append((bx, by, bz, [(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)]))
                        continue
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
                    block_aabbs = merge_aabbs(block_aabbs)
                    if block_aabbs:
                        aabbs.append((bx, by, bz, block_aabbs))
    if num_bx == 1 and num_by == 1 and num_bz == 1 and aabbs:
        # Apply centering shift for single-block models
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
    return aabbs
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
def main():
    start_time = time.time()
    parser = argparse.ArgumentParser()
    parser.add_argument('path', nargs='?', help='Path to bbmodel or directory')
    parser.add_argument('--main', type=str, help='Main model file (bypasses prompt)')
    parser.add_argument('--output', choices=['java', 'json'], help='Output type: java or json')
    parser.add_argument('--ythresh', type=int, default=4, help='Threshold for vertical filling (y axis); 0 for unlimited, -1 to disable')
    parser.add_argument('--xthresh', type=int, default=2, help='Threshold for filling along x axis; 0 for unlimited, -1 to disable')
    parser.add_argument('--zthresh', type=int, default=2, help='Threshold for filling along z axis; 0 for unlimited, -1 to disable')
    parser.add_argument('--gap-passes', type=int, default=3, help='Number of passes for gap filling per axis')
    parser.add_argument('--void-thresh', type=int, default=4, help='Maximum voxel count for small voids to fill (fills if size < threshold)')
    parser.add_argument('--occ-thresh', type=int, default=4, help='Maximum voxel count for small occupied clusters to remove (removes if size < threshold)')
    parser.add_argument('--no-gpp', action='store_true', help='Disable global post-processing on the full model (default: enabled)')
    parser.add_argument('--no-postprocess', action='store_true', help='Disable all post-processing steps')
    parser.add_argument('--no-holes', action='store_true', help='Disable binary_fill_holes')
    parser.add_argument('--no-gaps', action='store_true', help='Disable gap filling along axes (overrides thresholds)')
    parser.add_argument('--no-small-voids', action='store_true', help='Disable small void removal')
    parser.add_argument('--pbg-x', action='store_true', help='Force per-block gap filling for X axis when global postprocess is enabled')
    parser.add_argument('--pbg-y', action='store_true', help='Force per-block gap filling for Y axis when global postprocess is enabled')
    parser.add_argument('--pbg-z', action='store_true', help='Force per-block gap filling for Z axis when global postprocess is enabled')
    parser.add_argument('--solid-blocks', type=str, default='', help='Space-separated bx,by,bz to force solid before postprocess (e.g. "0,0,0 1,0,0")')
    parser.add_argument('--supp-config', nargs='*', help='Auto supp: model num_times offset1 offset2... e.g. model.bbmodel 2 0,0,0 1,0,0 [next_model ...]')
    parser.add_argument('--dml-index', type=int, default=None, help='DirectML device index to use (overrides enumeration)')
    parser.add_argument('--use-fp16', action='store_true', help='Use FP16 for GPU computations (may speed up but risk precision issues)')
    parser.add_argument('--single-thread', action='store_true', help='Force single-threaded processing even on CPU')
    parser.add_argument('--no-supplementary', action='store_true', help='Disable processing of supplementary models')
    args = parser.parse_args()
    # Parse solid_set
    solid_set = set()
    if args.solid_blocks:
        for s in args.solid_blocks.split():
            bx, by, bz = map(int, s.split(','))
            solid_set.add((bx, by, bz))
    global_postprocess = not args.no_gpp
    # Device selection
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
            # Parse auto supp config
            i = 0
            while i < len(args.supp_config):
                model = args.supp_config[i]
                i += 1
                if i >= len(args.supp_config):
                    break
                num_times = int(args.supp_config[i])
                i += 1
                offsets = []
                for _ in range(num_times):
                    if i >= len(args.supp_config):
                        break
                    off_str = args.supp_config[i]
                    bx, by, bz = map(int, off_str.split(','))
                    offsets.append((bx, by, bz))
                    i += 1
                if offsets:
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
    cache = {}
    print("Processing main model...")
    overall_aabbs = parse_bbmodel(main_path, args.xthresh, args.ythresh, args.zthresh, args.no_postprocess, args.no_holes, args.no_gaps, args.no_small_voids, args.gap_passes, args.void_thresh, args.occ_thresh, global_postprocess, args.pbg_x, args.pbg_y, args.pbg_z, device, args.use_fp16, args.single_thread, solid_set)
    cache[main_file] = overall_aabbs
    unique_supps = set(s_file for s_file, _ in supp_list)
    for s_file in unique_supps:
        print(f"Processing supplementary model: {s_file}")
        s_path = os.path.join(directory, s_file)
        s_aabbs = parse_bbmodel(s_path, args.xthresh, args.ythresh, args.zthresh, args.no_postprocess, args.no_holes, args.no_gaps, args.no_small_voids, args.gap_passes, args.void_thresh, args.occ_thresh, global_postprocess, args.pbg_x, args.pbg_y, args.pbg_z, device, args.use_fp16, args.single_thread, solid_set)
        cache[s_file] = s_aabbs
    placements = []
    for s_file, offsets in supp_list:
        s_aabbs = cache[s_file]
        for off_bx, off_by, off_bz in offsets:
            placements.append((s_aabbs, off_bx, off_by, off_bz))
    placements.sort(key=lambda p: p[3])
    overall_dict = {(bx, by, bz): block_aabbs for bx, by, bz, block_aabbs in overall_aabbs}
    for s_aabbs, off_bx, off_by, off_bz in placements:
        current_min_bz = min(overall_dict, key=lambda k: k[2])[2] if overall_dict else 0
        current_max_bz = max(overall_dict, key=lambda k: k[2])[2] if overall_dict else 0
        if off_bz < current_min_bz:
            shift = current_min_bz - off_bz
            new_dict = {}
            for (bx, by, bz), ba in overall_dict.items():
                new_dict[(bx, by, bz + shift)] = ba
            overall_dict = new_dict
        elif off_bz == current_min_bz:
            new_dict = {}
            for (bx, by, bz), ba in overall_dict.items():
                new_dict[(bx, by, bz + 1)] = ba
            overall_dict = new_dict
            off_bz = current_min_bz
        for sx, sy, sz, ba in s_aabbs:
            new_bx = off_bx + sx
            new_by = off_by + sy
            new_bz = off_bz + sz
            overall_dict[(new_bx, new_by, new_bz)] = ba
    overall_aabbs = [(bx, by, bz, overall_dict[(bx, by, bz)]) for bx, by, bz in sorted(overall_dict)]
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
                    aabb_json[index] = [[0,0,0,1,1,1]]
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