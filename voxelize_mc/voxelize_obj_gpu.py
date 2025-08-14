import numpy as np
import argparse
import os
from collections import deque
import torch
import multiprocessing as mp
from multiprocessing.sharedctypes import RawArray
from typing import Tuple, List
import ctypes
EPSILON = 1e-5
# Device selection: Prefer CUDA/ROCm if available, then DirectML, then CPU
if torch.cuda.is_available():
    device = torch.device('cuda') # Supports NVIDIA CUDA and AMD ROCm
else:
    try:
        import torch_directml
        device = torch_directml.device()
    except ImportError:
        device = torch.device('cpu')
print(f"Using device: {device}")
def custom_cross(a, b):
    return torch.stack([
        a[..., 1] * b[..., 2] - a[..., 2] * b[..., 1],
        a[..., 2] * b[..., 0] - a[..., 0] * b[..., 2],
        a[..., 0] * b[..., 1] - a[..., 1] * b[..., 0]
    ], dim=-1)
def intersects_box(triangle, box_center, box_extents):
    eps = EPSILON
    X, Y, Z = 0, 1, 2
    v0 = triangle[0] - box_center
    v1 = triangle[1] - box_center
    v2 = triangle[2] - box_center
    f0 = triangle[1] - triangle[0]
    f1 = triangle[2] - triangle[1]
    f2 = triangle[0] - triangle[2]
    a00 = np.array([0, -f0[Z], f0[Y]])
    p0 = np.dot(v0, a00)
    p1 = np.dot(v1, a00)
    p2 = np.dot(v2, a00)
    r = box_extents[Y] * np.abs(f0[Z]) + box_extents[Z] * np.abs(f0[Y])
    if max(-max([p0, p1, p2]), min([p0, p1, p2])) > r + eps: return False
    a01 = np.array([0, -f1[Z], f1[Y]])
    p0 = np.dot(v0, a01)
    p1 = np.dot(v1, a01)
    p2 = np.dot(v2, a01)
    r = box_extents[Y] * np.abs(f1[Z]) + box_extents[Z] * np.abs(f1[Y])
    if max(-max([p0, p1, p2]), min([p0, p1, p2])) > r + eps: return False
    a02 = np.array([0, -f2[Z], f2[Y]])
    p0 = np.dot(v0, a02)
    p1 = np.dot(v1, a02)
    p2 = np.dot(v2, a02)
    r = box_extents[Y] * np.abs(f2[Z]) + box_extents[Z] * np.abs(f2[Y])
    if max(-max([p0, p1, p2]), min([p0, p1, p2])) > r + eps: return False
    a10 = np.array([f0[Z], 0, -f0[X]])
    p0 = np.dot(v0, a10)
    p1 = np.dot(v1, a10)
    p2 = np.dot(v2, a10)
    r = box_extents[X] * np.abs(f0[Z]) + box_extents[Z] * np.abs(f0[X])
    if max(-max([p0, p1, p2]), min([p0, p1, p2])) > r + eps: return False
    a11 = np.array([f1[Z], 0, -f1[X]])
    p0 = np.dot(v0, a11)
    p1 = np.dot(v1, a11)
    p2 = np.dot(v2, a11)
    r = box_extents[X] * np.abs(f1[Z]) + box_extents[Z] * np.abs(f1[X])
    if max(-max([p0, p1, p2]), min([p0, p1, p2])) > r + eps: return False
    a12 = np.array([f2[Z], 0, -f2[X]])
    p0 = np.dot(v0, a12)
    p1 = np.dot(v1, a12)
    p2 = np.dot(v2, a12)
    r = box_extents[X] * np.abs(f2[Z]) + box_extents[Z] * np.abs(f2[X])
    if max(-max([p0, p1, p2]), min([p0, p1, p2])) > r + eps: return False
    a20 = np.array([-f0[Y], f0[X], 0])
    p0 = np.dot(v0, a20)
    p1 = np.dot(v1, a20)
    p2 = np.dot(v2, a20)
    r = box_extents[X] * np.abs(f0[Y]) + box_extents[Y] * np.abs(f0[X])
    if max(-max([p0, p1, p2]), min([p0, p1, p2])) > r + eps: return False
    a21 = np.array([-f1[Y], f1[X], 0])
    p0 = np.dot(v0, a21)
    p1 = np.dot(v1, a21)
    p2 = np.dot(v2, a21)
    r = box_extents[X] * np.abs(f1[Y]) + box_extents[Y] * np.abs(f1[X])
    if max(-max([p0, p1, p2]), min([p0, p1, p2])) > r + eps: return False
    a22 = np.array([-f2[Y], f2[X], 0])
    p0 = np.dot(v0, a22)
    p1 = np.dot(v1, a22)
    p2 = np.dot(v2, a22)
    r = box_extents[X] * np.abs(f2[Y]) + box_extents[Y] * np.abs(f2[X])
    if max(-max([p0, p1, p2]), min([p0, p1, p2])) > r + eps: return False
    if max([v0[X], v1[X], v2[X]]) < -box_extents[X] - eps or min([v0[X], v1[X], v2[X]]) > box_extents[X] + eps: return False
    if max([v0[Y], v1[Y], v2[Y]]) < -box_extents[Y] - eps or min([v0[Y], v1[Y], v2[Y]]) > box_extents[Y] + eps: return False
    if max([v0[Z], v1[Z], v2[Z]]) < -box_extents[Z] - eps or min([v0[Z], v1[Z], v2[Z]]) > box_extents[Z] + eps: return False
    plane_normal = np.cross(f0, f1)
    plane_distance = np.dot(plane_normal, v0)
    r = box_extents[X] * np.abs(plane_normal[X]) + box_extents[Y] * np.abs(plane_normal[Y]) + box_extents[Z] * np.abs(plane_normal[Z])
    if abs(plane_distance) > r + eps: return False
    return True

def intersects_box_vec(triangles, voxel_centers, box_extents):
    N_vox = voxel_centers.shape[0]
    N_tri = triangles.shape[0]
    if N_tri == 0: return torch.zeros(N_vox, dtype=torch.bool, device=device)
    epsilon = torch.tensor(EPSILON, dtype=torch.float32, device=device)
    v0 = triangles[:, 0].unsqueeze(0) - voxel_centers.unsqueeze(1)
    v1 = triangles[:, 1].unsqueeze(0) - voxel_centers.unsqueeze(1)
    v2 = triangles[:, 2].unsqueeze(0) - voxel_centers.unsqueeze(1)
    f0 = triangles[:, 1] - triangles[:, 0]
    f1 = triangles[:, 2] - triangles[:, 1]
    f2 = triangles[:, 0] - triangles[:, 2]
    f0 = f0.unsqueeze(0)
    f1 = f1.unsqueeze(0)
    f2 = f2.unsqueeze(0)
    def axis_sep(a, f, ext0, ext1):
        p0 = (v0 * a).sum(-1)
        p1 = (v1 * a).sum(-1)
        p2 = (v2 * a).sum(-1)
        min_p = torch.minimum(torch.minimum(p0, p1), p2)
        max_p = torch.maximum(torch.maximum(p0, p1), p2)
        rad = box_extents[ext0] * torch.abs(f[:, :, ext1]) + box_extents[ext1] * torch.abs(f[:, :, ext0])
        sep = torch.max(-max_p, min_p) > rad + epsilon
        return sep
    a00 = torch.zeros((1, N_tri, 3), device=device)
    a00[:, :, 1] = -f0[:, :, 2]
    a00[:, :, 2] = f0[:, :, 1]
    sep00 = axis_sep(a00, f0, 1, 2)
    a01 = torch.zeros((1, N_tri, 3), device=device)
    a01[:, :, 1] = -f1[:, :, 2]
    a01[:, :, 2] = f1[:, :, 1]
    sep01 = axis_sep(a01, f1, 1, 2)
    a02 = torch.zeros((1, N_tri, 3), device=device)
    a02[:, :, 1] = -f2[:, :, 2]
    a02[:, :, 2] = f2[:, :, 1]
    sep02 = axis_sep(a02, f2, 1, 2)
    a10 = torch.zeros((1, N_tri, 3), device=device)
    a10[:, :, 0] = f0[:, :, 2]
    a10[:, :, 2] = -f0[:, :, 0]
    sep10 = axis_sep(a10, f0, 0, 2)
    a11 = torch.zeros((1, N_tri, 3), device=device)
    a11[:, :, 0] = f1[:, :, 2]
    a11[:, :, 2] = -f1[:, :, 0]
    sep11 = axis_sep(a11, f1, 0, 2)
    a12 = torch.zeros((1, N_tri, 3), device=device)
    a12[:, :, 0] = f2[:, :, 2]
    a12[:, :, 2] = -f2[:, :, 0]
    sep12 = axis_sep(a12, f2, 0, 2)
    a20 = torch.zeros((1, N_tri, 3), device=device)
    a20[:, :, 0] = -f0[:, :, 1]
    a20[:, :, 1] = f0[:, :, 0]
    sep20 = axis_sep(a20, f0, 0, 1)
    a21 = torch.zeros((1, N_tri, 3), device=device)
    a21[:, :, 0] = -f1[:, :, 1]
    a21[:, :, 1] = f1[:, :, 0]
    sep21 = axis_sep(a21, f1, 0, 1)
    a22 = torch.zeros((1, N_tri, 3), device=device)
    a22[:, :, 0] = -f2[:, :, 1]
    a22[:, :, 1] = f2[:, :, 0]
    sep22 = axis_sep(a22, f2, 0, 1)
    vx = torch.stack([v0[:, :, 0], v1[:, :, 0], v2[:, :, 0]], dim=2)
    min_vx = vx.min(2)[0]
    max_vx = vx.max(2)[0]
    sep_x = (max_vx < -box_extents[0] - epsilon) | (min_vx > box_extents[0] + epsilon)
    vy = torch.stack([v0[:, :, 1], v1[:, :, 1], v2[:, :, 1]], dim=2)
    min_vy = vy.min(2)[0]
    max_vy = vy.max(2)[0]
    sep_y = (max_vy < -box_extents[1] - epsilon) | (min_vy > box_extents[1] + epsilon)
    vz = torch.stack([v0[:, :, 2], v1[:, :, 2], v2[:, :, 2]], dim=2)
    min_vz = vz.min(2)[0]
    max_vz = vz.max(2)[0]
    sep_z = (max_vz < -box_extents[2] - epsilon) | (min_vz > box_extents[2] + epsilon)
    normal = custom_cross(f0[0], f1[0]).unsqueeze(0)
    plane_d = (v0 * normal).sum(-1)
    plane_r = box_extents[0] * torch.abs(normal[:, :, 0]) + box_extents[1] * torch.abs(normal[:, :, 1]) + box_extents[2] * torch.abs(normal[:, :, 2])
    sep_plane = torch.abs(plane_d) > plane_r + epsilon
    all_sep = sep00 | sep01 | sep02 | sep10 | sep11 | sep12 | sep20 | sep21 | sep22 | sep_x | sep_y | sep_z | sep_plane
    intersects_per_pair = ~all_sep
    intersects_per_vox = intersects_per_pair.any(dim=1)
    return intersects_per_vox

def merge_voxels(filled, res, min_vox=1):
    aabbs = []
    visited = np.zeros_like(filled, dtype=bool)
    axis_perms = [(0,1,2), (0,2,1), (1,0,2), (1,2,0), (2,0,1), (2,1,0)]
    for x in range(res):
        for y in range(res):
            for z in range(res):
                if filled[x, y, z] and not visited[x, y, z]:
                    seed = (x, y, z)
                    best_volume = 0
                    best_mins = None
                    best_maxs = None
                    for perm in axis_perms:
                        mins = list(seed)
                        maxs = list(seed)
                        for ax in perm:
                            while maxs[ax] + 1 < res:
                                new_max = maxs[ax] + 1
                                slic = [slice(mins[0], maxs[0] + 1), slice(mins[1], maxs[1] + 1), slice(mins[2], maxs[2] + 1)]
                                slic[ax] = new_max
                                if np.all(filled[tuple(slic)]):
                                    maxs[ax] = new_max
                                else:
                                    break
                            while mins[ax] - 1 >= 0:
                                new_min = mins[ax] - 1
                                slic = [slice(mins[0], maxs[0] + 1), slice(mins[1], maxs[1] + 1), slice(mins[2], maxs[2] + 1)]
                                slic[ax] = new_min
                                if np.all(filled[tuple(slic)]):
                                    mins[ax] = new_min
                                else:
                                    break
                        volume = (maxs[0] - mins[0] + 1) * (maxs[1] - mins[1] + 1) * (maxs[2] - mins[2] + 1)
                        if volume > best_volume:
                            best_volume = volume
                            best_mins = tuple(mins)
                            best_maxs = tuple(maxs)
                    if best_volume < min_vox: continue
                    visited[best_mins[0]:best_maxs[0] + 1, best_mins[1]:best_maxs[1] + 1, best_mins[2]:best_maxs[2] + 1] = True
                    min_aabb = (best_mins[0] / res, best_mins[1] / res, best_mins[2] / res)
                    max_aabb = ((best_maxs[0] + 1) / res, (best_maxs[1] + 1) / res, (best_maxs[2] + 1) / res)
                    aabbs.append(min_aabb + max_aabb)
    return aabbs

def fill_local_interior(filled, shape):
    visited = np.zeros(shape, dtype=bool)
    air_queue = deque()
    directions = []
    for dx in range(-1, 2):
        for dy in range(-1, 2):
            for dz in range(-1, 2):
                if (dx, dy, dz) != (0, 0, 0):
                    directions.append((dx, dy, dz))
    for dim in range(3):
        low = 0
        high = shape[dim] - 1
        for i in range(shape[(dim + 1) % 3]):
            for j in range(shape[(dim + 2) % 3]):
                for val in [low, high]:
                    pos = [0, 0, 0]
                    pos[dim] = val
                    pos[(dim + 1) % 3] = i
                    pos[(dim + 2) % 3] = j
                    pos_tuple = tuple(pos)
                    if not filled[pos_tuple]:
                        air_queue.append(pos_tuple)
                        visited[pos_tuple] = True
    while air_queue:
        pos = air_queue.popleft()
        for d in directions:
            npos = tuple(np.array(pos) + np.array(d))
            if all(0 <= npos[k] < shape[k] for k in range(3)) and not visited[npos] and not filled[npos]:
                visited[npos] = True
                air_queue.append(npos)
    for gx in range(shape[0]):
        for gy in range(shape[1]):
            for gz in range(shape[2]):
                if not visited[gx, gy, gz] and not filled[gx, gy, gz]:
                    filled[gx, gy, gz] = True
    return filled

def remove_small_components(filled, min_vox):
    shape = filled.shape
    visited = np.zeros(shape, dtype=bool)
    directions = [(1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)]
    for gx in range(shape[0]):
        for gy in range(shape[1]):
            for gz in range(shape[2]):
                if filled[gx, gy, gz] and not visited[gx, gy, gz]:
                    component = []
                    queue = deque([(gx, gy, gz)])
                    visited[gx, gy, gz] = True
                    while queue:
                        pos = queue.popleft()
                        component.append(pos)
                        for d in directions:
                            npos = tuple(np.array(pos) + d)
                            if all(0 <= npos[k] < shape[k] for k in range(3)) and filled[npos] and not visited[npos]:
                                visited[npos] = True
                                queue.append(npos)
                    if len(component) < min_vox:
                        for pos in component:
                            filled[pos] = False
    return filled

def init_pool(tri_shared, tri_shape, gf_shared, gf_shape):
    global triangles_shared, triangles_shape, global_filled_shared, global_shape
    triangles_shared = tri_shared
    triangles_shape = tri_shape
    global_filled_shared = gf_shared
    global_shape = gf_shape

def mark_surface_worker(args):
    bx, by, bz, res, extents_base, voxel_size, flip_axis, mid_value, size_x, size_y, size_z = args
    model_min = np.array([bx, by, bz], dtype=np.float32)
    block_center = model_min + 0.5
    block_extents = np.array([0.5, 0.5, 0.5], dtype=np.float32)
    triangles_np = np.frombuffer(triangles_shared, dtype=np.float32).reshape(triangles_shape)
    model_intersect = any(intersects_box(tri, block_center, block_extents) for tri in triangles_np)
    if not model_intersect: return
    ix, iy, iz = np.meshgrid(np.arange(res), np.arange(res), np.arange(res), indexing='ij')
    local_centers = np.stack([ix, iy, iz], axis=-1).astype(np.float32) + 0.5
    local_centers *= voxel_size
    voxel_centers_flat = local_centers.reshape(-1, 3) + model_min
    if flip_axis is not None and mid_value is not None:
        voxel_centers_flat[:, flip_axis] = 2 * mid_value - voxel_centers_flat[:, flip_axis]
    triangles_t = torch.from_numpy(triangles_np).to(device=device, dtype=torch.float32)
    extents_base_t = torch.from_numpy(extents_base).to(device=device, dtype=torch.float32)
    batch_size = 1000000
    intersects_list = []
    for i in range(0, voxel_centers_flat.shape[0], batch_size):
        batch_centers = torch.from_numpy(voxel_centers_flat[i:i+batch_size]).to(device=device, dtype=torch.float32)
        intersects_batch = intersects_box_vec(triangles_t, batch_centers, extents_base_t)
        intersects_list.append(intersects_batch)
    intersects = torch.cat(intersects_list)
    filled_flat = intersects.cpu().numpy()
    filled = filled_flat.reshape((res, res, res))
    global_filled_worker = np.frombuffer(global_filled_shared, dtype=np.bool_).reshape(global_shape)
    local_positions = np.where(filled)
    for i in range(len(local_positions[0])):
        ix = local_positions[0][i]
        iy = local_positions[1][i]
        iz = local_positions[2][i]
        gx = bx * res + ix
        gy = by * res + iy
        gz = bz * res + iz
        if 0 <= gx < size_x * res and 0 <= gy < size_y * res and 0 <= gz < size_z * res:
            global_filled_worker[gx, gy, gz] = True

def merge_worker(args):
    bx, by, bz, filled_slice, res, min_vox = args
    aabbs = merge_voxels(filled_slice, res, min_vox)
    lines = []
    if aabbs:
        lines.append(f'if (bX == {bx} && bY == {by} && bZ == {bz}) {{')
        for minx, miny, minz, maxx, maxy, maxz in aabbs:
            lines.append(f' main.add(new AABB({minx:.4f}D, {miny:.4f}D, {minz:.4f}D, {maxx:.4f}D, {maxy:.4f}D, {maxz:.4f}D));')
        lines.append('}')
    return '\n'.join(lines)

def parse_obj(path):
    verts = []
    faces = []
    with open(path, 'r') as f:
        for line in f:
            line = line.strip()
            if line.startswith('v '):
                verts.append(list(map(float, line.split()[1:])))
            elif line.startswith('f '):
                face = [int(p.split('/')[0]) - 1 for p in line.split()[1:]]
                faces.append(face)
    return np.array(verts), faces

def determine_flip(main_verts, mirror_verts):
    if mirror_verts is None:
        return None, None
    mirrored_bb_min = np.min(mirror_verts, axis=0)
    mirrored_bb_max = np.max(mirror_verts, axis=0)
    mid = (mirrored_bb_min + mirrored_bb_max) / 2
    mirror_centroid = np.mean(mirror_verts, axis=0)
    main_centroid = np.mean(main_verts, axis=0)
    dist_no_flip = np.linalg.norm(mirror_centroid - main_centroid)
    best_dist = dist_no_flip
    best_axis = None
    for axis in range(3):
        flipped_centroid = mirror_centroid.copy()
        flipped_centroid[axis] = 2 * mid[axis] - mirror_centroid[axis]
        dist = np.linalg.norm(flipped_centroid - main_centroid)
        if dist < best_dist - EPSILON:
            best_dist = dist
            best_axis = axis
    if best_axis is not None:
        return best_axis, mid[best_axis]
    return None, None

def init_pool_main(tri_shared, tri_shape, gf_shared, gf_shape):
    init_pool(tri_shared, tri_shape, gf_shared, gf_shape)

def init_pool_add(tri_shared, tri_shape, gf_shared, gf_shape):
    init_pool(tri_shared, tri_shape, gf_shared, gf_shape)

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Voxelize OBJ for AABB collision with optional ray-based interior fill')
    parser.add_argument('filename', type=str, help='OBJ filename or directory')
    parser.add_argument('--res', type=int, default=16, help='Voxel resolution')
    parser.add_argument('--min_vox', type=int, default=1, help='Minimum voxel count per AABB and component (default: 1)')
    args = parser.parse_args()

    if os.path.isdir(args.filename):
        dir_path = os.path.abspath(args.filename)
        obj_files = [f for f in os.listdir(dir_path) if f.lower().endswith('.obj')]
        if not obj_files:
            raise ValueError("No OBJ files found in the directory.")
        print("Detected OBJ files:")
        for i, f in enumerate(obj_files):
            print(f"{i}: {f}")
        main_index = int(input("Select main OBJ index: "))
        main_file = obj_files[main_index]
        print(f"Selected main file: {main_file}")
    elif os.path.isfile(args.filename) and args.filename.lower().endswith('.obj'):
        dir_path = os.path.dirname(os.path.abspath(args.filename))
        main_file = os.path.basename(args.filename)
        obj_files = [f for f in os.listdir(dir_path) if f.lower().endswith('.obj')]
        if main_file not in obj_files:
            raise ValueError("Specified file not found in directory.")
        print(f"Selected main file: {main_file}")
    else:
        raise ValueError("Invalid input: must be an OBJ file or a directory containing OBJ files.")
    other_objs = [f for f in obj_files if f != main_file]
    mirrored_file = None
    if other_objs:
        print("Detected other OBJ files:")
        for i, f in enumerate(other_objs):
            print(f"{i}: {f}")
        mirrored_index = int(input("Select mirrored OBJ index (-1 for none): "))
        if mirrored_index >= 0:
            mirrored_file = other_objs[mirrored_index]
            other_objs = [f for f in other_objs if f != mirrored_file]
    additional_objs = []
    if other_objs:
        while input("Add animation/additional OBJ? y/n: ") == 'y':
            print("Remaining OBJ files:")
            for i, f in enumerate(other_objs):
                print(f"{i}: {f}")
            index = int(input("Select OBJ index: "))
            selected_file = other_objs[index]
            pos_str = input("Enter position bX,bY,bZ: ")
            try:
                bX, bY, bZ = map(int, pos_str.split(','))
            except ValueError:
                print("Invalid position input. Skipping.")
                continue
            type_str = input("Internal or External (i/e): ")
            is_internal = type_str.lower() == 'i'
            additional_objs.append((selected_file, bX, bY, bZ, is_internal))
    output_file = main_file.replace(".obj", ".txt")
    output_lines = []
    res = args.res
    voxel_size = np.float32(1.0 / res)
    extents_base = np.array([0.5 / res] * 3, dtype=np.float32)
    # Load main
    main_path = os.path.join(dir_path, main_file)
    main_verts, main_faces = parse_obj(main_path)
    main_verts = np.array(main_verts, dtype=np.float32)
    bb_min = np.min(main_verts, axis=0)
    bb_max = np.max(main_verts, axis=0)
    main_centroid = np.mean(main_verts, axis=0)
    min_block_main = np.floor(bb_min + 1e-5).astype(int)
    max_block_main = np.ceil(bb_max - 1e-5).astype(int) - 1
    overall_size_main = max_block_main - min_block_main + 1
    shift_main = -min_block_main.astype(float)
    main_verts += shift_main
    print("Main bb_min:", bb_min, "bb_max:", bb_max, "overall_size_main:", overall_size_main)
    # Mirror
    flip_axis_mirrored = None
    mid_value_mirrored = None
    if mirrored_file:
        mirror_path = os.path.join(dir_path, mirrored_file)
        mirror_verts, mirror_faces = parse_obj(mirror_path)
        mirror_verts = np.array(mirror_verts, dtype=np.float32)
        flip_axis_mirrored, mid_value_mirrored = determine_flip(main_verts, mirror_verts)
        if flip_axis_mirrored is not None:
            axis_name = ['X', 'Y', 'Z'][flip_axis_mirrored]
            print(f"Detected mirror flip on {axis_name} axis")
        else:
            print("No mirror flip detected")
    additional_list = []
    # Additional
    for selected_file, bX, bY, bZ, is_internal in additional_objs:
        add_path = os.path.join(dir_path, selected_file)
        add_verts, add_faces = parse_obj(add_path)
        add_verts = np.array(add_verts, dtype=np.float32)
        bb_min_add = np.min(add_verts, axis=0)
        bb_max_add = np.max(add_verts, axis=0)
        add_centroid = np.mean(add_verts, axis=0)
        flip_axis_add, mid_value_add = determine_flip(main_verts, add_verts)
        if flip_axis_add is not None:
            axis_name = ['X', 'Y', 'Z'][flip_axis_add]
            print(f"Detected mirror flip on {axis_name} axis for additional file {selected_file}")
            mid_add = (bb_min_add + bb_max_add) / 2
            mid_value_add = mid_add[flip_axis_add] - bb_min_add[flip_axis_add] + [bX, bY, bZ][flip_axis_add]
        else:
            print(f"No mirror flip detected for additional file {selected_file}")
            flip_axis_add = None
            mid_value_add = None
        min_block_add = np.floor(bb_min_add + 1e-5).astype(int)
        max_block_add = np.ceil(bb_max_add - 1e-5).astype(int) - 1
        overall_size_add = max_block_add - min_block_add + 1
        shift_add = -min_block_add.astype(float)
        add_verts += shift_add
        additional_list.append((bX, bY, bZ, add_verts, add_faces, flip_axis_add, mid_value_add, is_internal, overall_size_add))
    # Calculate global size
    size_x = overall_size_main[0]
    size_y = overall_size_main[1]
    size_z = overall_size_main[2]
    for bX, bY, bZ, _, _, _, _, _, overall_size_add in additional_list:
        size_x = max(size_x, bX + overall_size_add[0])
        size_y = max(size_y, bY + overall_size_add[1])
        size_z = max(size_z, bZ + overall_size_add[2])
    global_shape = (size_x * res, size_y * res, size_z * res)
    global_filled_shared = RawArray(ctypes.c_bool, int(np.prod(global_shape)))
    # Main triangles
    main_triangles = []
    for face in main_faces:
        if len(face) == 4:
            tri1 = main_verts[[face[0], face[1], face[2]]]
            area1 = np.linalg.norm(np.cross(tri1[1] - tri1[0], tri1[2] - tri1[0])) / 2
            if area1 > 0:
                main_triangles.append(tri1)
            tri2 = main_verts[[face[0], face[2], face[3]]]
            area2 = np.linalg.norm(np.cross(tri2[1] - tri2[0], tri2[2] - tri2[0])) / 2
            if area2 > 0:
                main_triangles.append(tri2)
        elif len(face) == 3:
            tri = main_verts[face]
            area = np.linalg.norm(np.cross(tri[1] - tri[0], tri[2] - tri[0])) / 2
            if area > 0:
                main_triangles.append(tri)
    if main_triangles:
        main_triangles_np = np.stack([np.array(t, dtype=np.float32) for t in main_triangles])
        main_triangles_shape = main_triangles_np.shape
        main_triangles_shared = RawArray(ctypes.c_float, main_triangles_np.ravel())
    else:
        main_triangles_shared = RawArray(ctypes.c_float, 0)
        main_triangles_shape = (0, 0, 0)
    # Mark main parallel
    tasks = [(bx, by, bz, res, extents_base, voxel_size, None, None, size_x, size_y, size_z) 
             for bx in range(overall_size_main[0]) for by in range(overall_size_main[1]) for bz in range(overall_size_main[2])]
    with mp.Pool(processes=4, initializer=init_pool, initargs=(main_triangles_shared, main_triangles_shape, global_filled_shared, global_shape)) as pool:
        pool.map(mark_surface_worker, tasks)
    # Additional
    for bX, bY, bZ, add_verts, add_faces, flip_axis, mid_value, is_internal, overall_size_add in additional_list:
        add_triangles = []
        for face in add_faces:
            if len(face) == 4:
                tri1 = add_verts[[face[0], face[1], face[2]]]
                area1 = np.linalg.norm(np.cross(tri1[1] - tri1[0], tri1[2] - tri1[0])) / 2
                if area1 > 0:
                    add_triangles.append(tri1)
                tri2 = add_verts[[face[0], face[2], face[3]]]
                area2 = np.linalg.norm(np.cross(tri2[1] - tri2[0], tri2[2] - tri2[0])) / 2
                if area2 > 0:
                    add_triangles.append(tri2)
            elif len(face) == 3:
                tri = add_verts[face]
                area = np.linalg.norm(np.cross(tri[1] - tri[0], tri[2] - tri[0])) / 2
                if area > 0:
                    add_triangles.append(tri)
        if is_internal:
            if add_triangles:
                add_triangles_np = np.stack([np.array(t, dtype=np.float32) for t in add_triangles])
                add_triangles_shape = add_triangles_np.shape
                add_triangles_shared = RawArray(ctypes.c_float, add_triangles_np.ravel())
            else:
                add_triangles_shared = RawArray(ctypes.c_float, 0)
                add_triangles_shape = (0, 0, 0)
            tasks_add = [(bX + local_bx, bY + local_by, bZ + local_bz, res, extents_base, voxel_size, flip_axis, mid_value, size_x, size_y, size_z) 
                         for local_bx in range(overall_size_add[0]) for local_by in range(overall_size_add[1]) for local_bz in range(overall_size_add[2])]
            with mp.Pool(processes=4, initializer=init_pool, initargs=(add_triangles_shared, add_triangles_shape, global_filled_shared, global_shape)) as pool:
                pool.map(mark_surface_worker, tasks_add)
        else:
            global_filled = np.frombuffer(global_filled_shared, dtype=np.bool_).reshape(global_shape)
            for local_bx in range(overall_size_add[0]):
                for local_by in range(overall_size_add[1]):
                    for local_bz in range(overall_size_add[2]):
                        gx_start = (bX + local_bx) * res
                        gy_start = (bY + local_by) * res
                        gz_start = (bZ + local_bz) * res
                        global_filled[gx_start:gx_start + res, gy_start:gy_start + res, gz_start:gz_start + res] = True
    global_filled = np.frombuffer(global_filled_shared, dtype=np.bool_).reshape(global_shape)
    print("Surface voxels marked:", np.sum(global_filled))
    global_filled = fill_local_interior(global_filled, global_shape)
    print("Total voxels filled after interior:", np.sum(global_filled))
    if args.min_vox > 1:
        global_filled = remove_small_components(global_filled, args.min_vox)
    # Parallel merge
    tasks_merge = []
    for bx in range(size_x):
        for by in range(size_y):
            for bz in range(size_z):
                slice_x = slice(bx * res, (bx + 1) * res)
                slice_y = slice(by * res, (by + 1) * res)
                slice_z = slice(bz * res, (bz + 1) * res)
                filled_slice = global_filled[slice_x, slice_y, slice_z].copy() # Copy to avoid view issues in worker
                tasks_merge.append((bx, by, bz, filled_slice, res, args.min_vox))
    with mp.Pool(processes=4) as pool:
        output_lines = pool.map(merge_worker, tasks_merge)
    output_lines = [line for line in output_lines if line]
    with open(output_file, 'w') as f:
        f.write('\n'.join(output_lines))
    print(f"Output written to {output_file}")