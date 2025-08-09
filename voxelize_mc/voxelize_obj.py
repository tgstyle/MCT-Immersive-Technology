import numpy as np
import argparse
import os
from collections import deque
EPSILON = 1e-5
def intersects_box(triangle, box_center, box_extents):
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
    if max(-max([p0, p1, p2]), min([p0, p1, p2])) > r:
        return False
    a01 = np.array([0, -f1[Z], f1[Y]])
    p0 = np.dot(v0, a01)
    p1 = np.dot(v1, a01)
    p2 = np.dot(v2, a01)
    r = box_extents[Y] * np.abs(f1[Z]) + box_extents[Z] * np.abs(f1[Y])
    if max(-max([p0, p1, p2]), min([p0, p1, p2])) > r:
        return False
    a02 = np.array([0, -f2[Z], f2[Y]])
    p0 = np.dot(v0, a02)
    p1 = np.dot(v1, a02)
    p2 = np.dot(v2, a02)
    r = box_extents[Y] * np.abs(f2[Z]) + box_extents[Z] * np.abs(f2[Y])
    if max(-max([p0, p1, p2]), min([p0, p1, p2])) > r:
        return False
    a10 = np.array([f0[Z], 0, -f0[X]])
    p0 = np.dot(v0, a10)
    p1 = np.dot(v1, a10)
    p2 = np.dot(v2, a10)
    r = box_extents[X] * np.abs(f0[Z]) + box_extents[Z] * np.abs(f0[X])
    if max(-max([p0, p1, p2]), min([p0, p1, p2])) > r:
        return False
    a11 = np.array([f1[Z], 0, -f1[X]])
    p0 = np.dot(v0, a11)
    p1 = np.dot(v1, a11)
    p2 = np.dot(v2, a11)
    r = box_extents[X] * np.abs(f1[Z]) + box_extents[Z] * np.abs(f1[X])
    if max(-max([p0, p1, p2]), min([p0, p1, p2])) > r:
        return False
    a12 = np.array([f2[Z], 0, -f2[X]])
    p0 = np.dot(v0, a12)
    p1 = np.dot(v1, a12)
    p2 = np.dot(v2, a12)
    r = box_extents[X] * np.abs(f2[Z]) + box_extents[Z] * np.abs(f2[X])
    if max(-max([p0, p1, p2]), min([p0, p1, p2])) > r:
        return False
    a20 = np.array([-f0[Y], f0[X], 0])
    p0 = np.dot(v0, a20)
    p1 = np.dot(v1, a20)
    p2 = np.dot(v2, a20)
    r = box_extents[X] * np.abs(f0[Y]) + box_extents[Y] * np.abs(f0[X])
    if max(-max([p0, p1, p2]), min([p0, p1, p2])) > r:
        return False
    a21 = np.array([-f1[Y], f1[X], 0])
    p0 = np.dot(v0, a21)
    p1 = np.dot(v1, a21)
    p2 = np.dot(v2, a21)
    r = box_extents[X] * np.abs(f1[Y]) + box_extents[Y] * np.abs(f1[X])
    if max(-max([p0, p1, p2]), min([p0, p1, p2])) > r:
        return False
    a22 = np.array([-f2[Y], f2[X], 0])
    p0 = np.dot(v0, a22)
    p1 = np.dot(v1, a22)
    p2 = np.dot(v2, a22)
    r = box_extents[X] * np.abs(f2[Y]) + box_extents[Y] * np.abs(f2[X])
    if max(-max([p0, p1, p2]), min([p0, p1, p2])) > r:
        return False
    if max([v0[X], v1[X], v2[X]]) < -box_extents[X] - EPSILON or min([v0[X], v1[X], v2[X]]) > box_extents[X] + EPSILON:
        return False
    if max([v0[Y], v1[Y], v2[Y]]) < -box_extents[Y] - EPSILON or min([v0[Y], v1[Y], v2[Y]]) > box_extents[Y] + EPSILON:
        return False
    if max([v0[Z], v1[Z], v2[Z]]) < -box_extents[Z] - EPSILON or min([v0[Z], v1[Z], v2[Z]]) > box_extents[Z] + EPSILON:
        return False
    plane_normal = np.cross(f0, f1)
    plane_distance = np.dot(plane_normal, v0)
    r = box_extents[X] * np.abs(plane_normal[X]) + box_extents[Y] * np.abs(plane_normal[Y]) + box_extents[Z] * np.abs(plane_normal[Z])
    if abs(plane_distance) > r + EPSILON:
        return False
    return True
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
def mark_surface(bx, by, bz, triangles, res, extents_base, voxel_size, flip_axis, mid_value, global_filled, size_x, size_y, size_z):
    model_min = np.array([bx, by, bz])
    block_center = model_min + 0.5
    block_extents = np.array([0.5, 0.5, 0.5])
    block_intersect = any(intersects_box(tri, block_center, block_extents) for tri in triangles)
    if not block_intersect:
        return
    filled = np.zeros((res, res, res), dtype=bool)
    ix, iy, iz = np.meshgrid(np.arange(res), np.arange(res), np.arange(res), indexing='ij')
    local_centers = np.stack([ix, iy, iz], axis=-1).astype(np.float64) + 0.5
    local_centers *= voxel_size
    voxel_centers_flat = local_centers.reshape(-1, 3) + model_min
    if flip_axis is not None and mid_value is not None:
        voxel_centers_flat[:, flip_axis] = 2 * mid_value - voxel_centers_flat[:, flip_axis]
    for k in range(voxel_centers_flat.shape[0]):
        voxel_center = voxel_centers_flat[k]
        for tri in triangles:
            if intersects_box(tri, voxel_center, extents_base):
                ix = k // (res * res)
                iy = (k // res) % res
                iz = k % res
                filled[ix, iy, iz] = True
                break
    local_positions = np.where(filled)
    for i in range(len(local_positions[0])):
        ix = local_positions[0][i]
        iy = local_positions[1][i]
        iz = local_positions[2][i]
        gx = bx * res + ix
        gy = by * res + iy
        gz = bz * res + iz
        if 0 <= gx < size_x * res and 0 <= gy < size_y * res and 0 <= gz < size_z * res:
            global_filled[gx, gy, gz] = True
parser = argparse.ArgumentParser(description='Voxelize OBJ for AABB collision')
parser.add_argument('filename', type=str, help='OBJ filename or directory')
parser.add_argument('--res', type=int, default=8, help='Voxel resolution')
parser.add_argument('--size_x', type=int, default=None, help='X size (auto if None)')
parser.add_argument('--size_y', type=int, default=None, help='Y size (auto if None)')
parser.add_argument('--size_z', type=int, default=None, help='Z size (auto if None)')
parser.add_argument('--offset_x', type=float, default=None, help='X offset (auto if None)')
parser.add_argument('--offset_y', type=float, default=None, help='Y offset (auto if None)')
parser.add_argument('--offset_z', type=float, default=None, help='Z offset (auto if None)')
parser.add_argument('--fill_interior', type=bool, default=True, help='Fill interior spaces (default: True)')
parser.add_argument('--min_vox', type=int, default=1, help='Minimum voxel count per AABB and component (default: 1)')
parser.add_argument('--fill_method', type=str, default='flood', choices=['flood', 'ray'], help='Fill method: flood or ray (default: flood)')
parser.add_argument('--ray_logic', type=str, default='or', choices=['or', 'majority'], help='Ray logic for interior: or or majority (default: or)')
parser.add_argument('--ray_directions', type=str, default='positive', choices=['positive', 'both'], help='Ray directions: positive or both (default: positive)')
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
        bX = int(input("bX position: "))
        bY = int(input("bY position: "))
        bZ = int(input("bZ position: "))
        additional_objs.append((selected_file, bX, bY, bZ))
        other_objs = [f for f in other_objs if f != selected_file]
output_file = main_file.replace(".obj", ".txt")
output_lines = []
print(f"Processing main file: {main_file}")
with open(os.path.join(dir_path, main_file), 'r') as f:
    lines = f.readlines()
verts = []
faces = []
for line in lines:
    if line.startswith('v '):
        parts = line.split()[1:]
        verts.append(np.array([float(p) for p in parts]))
    elif line.startswith('f '):
        parts = line.split()[1:]
        face = [int(p.split('/')[0]) - 1 for p in parts]
        faces.append(face)
verts_np = np.array(verts)
verts_np = np.round(verts_np * 16.0) / 16.0
bb_min = np.min(verts_np, axis=0)
bb_max = np.max(verts_np, axis=0)
bb_size = bb_max - bb_min
size_x = args.size_x if args.size_x is not None else np.ceil(bb_size[0]).astype(int)
size_y = args.size_y if args.size_y is not None else np.ceil(bb_size[1]).astype(int)
size_z = args.size_z if args.size_z is not None else np.ceil(bb_size[2]).astype(int)
offset = np.array([
    args.offset_x if args.offset_x is not None else np.floor(bb_min[0]),
    args.offset_y if args.offset_y is not None else np.floor(bb_min[1]),
    args.offset_z if args.offset_z is not None else np.floor(bb_min[2])
])
triangles = []
for face in faces:
    if len(face) == 4:
        tri1 = [verts_np[face[0]], verts_np[face[1]], verts_np[face[2]]]
        v0, v1, v2 = tri1
        cross = np.cross(v1 - v0, v2 - v0)
        area = np.linalg.norm(cross) / 2
        if area > 0.01:
            triangles.append(tri1)
        tri2 = [verts_np[face[0]], verts_np[face[2]], verts_np[face[3]]]
        v0, v1, v2 = tri2
        cross = np.cross(v1 - v0, v2 - v0)
        area = np.linalg.norm(cross) / 2
        if area > 0.01:
            triangles.append(tri2)
    elif len(face) == 3:
        tri = [verts_np[face[0]], verts_np[face[1]], verts_np[face[2]]]
        v0, v1, v2 = tri
        cross = np.cross(v1 - v0, v2 - v0)
        area = np.linalg.norm(cross) / 2
        if area > 0.01:
            triangles.append(tri)
triangles = [np.array(t) - offset for t in triangles]
main_centroid = np.mean(verts_np, axis=0)
mirrored_triangles = None
mirrored_flip_axis = None
mirrored_mid_value = None
if mirrored_file:
    print(f"Processing mirrored file: {mirrored_file}")
    with open(os.path.join(dir_path, mirrored_file), 'r') as f:
        lines = f.readlines()
    mirrored_verts = []
    mirrored_faces = []
    for line in lines:
        if line.startswith('v '):
            parts = line.split()[1:]
            mirrored_verts.append(np.array([float(p) for p in parts]))
        elif line.startswith('f '):
            parts = line.split()[1:]
            face = [int(p.split('/')[0]) - 1 for p in parts]
            mirrored_faces.append(face)
    mirrored_verts_np = np.array(mirrored_verts)
    mirrored_verts_np = np.round(mirrored_verts_np * 16.0) / 16.0
    mirrored_bb_min = np.min(mirrored_verts_np, axis=0)
    mirrored_bb_max = np.max(mirrored_verts_np, axis=0)
    mid = (mirrored_bb_min + mirrored_bb_max) / 2
    mirror_centroid = np.mean(mirrored_verts_np, axis=0)
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
        axis_name = ['X', 'Y', 'Z'][best_axis]
        print(f"Detected mirror flip on {axis_name} axis for mirrored file")
        mirrored_flip_axis = best_axis
        mirrored_mid_value = mid[best_axis] - offset[best_axis]
    else:
        print("No mirror flip detected for mirrored file")
additional_triangles = []
for additional_file, bX, bY, bZ in additional_objs:
    print(f"Processing additional file: {additional_file} at bX={bX}, bY={bY}, bZ={bZ}")
    with open(os.path.join(dir_path, additional_file), 'r') as f:
        lines = f.readlines()
    verts = []
    faces = []
    for line in lines:
        if line.startswith('v '):
            parts = line.split()[1:]
            verts.append(np.array([float(p) for p in parts]))
        elif line.startswith('f '):
            parts = line.split()[1:]
            face = [int(p.split('/')[0]) - 1 for p in parts]
            faces.append(face)
    verts_np = np.array(verts)
    verts_np = np.round(verts_np * 16.0) / 16.0
    add_bb_min = np.min(verts_np, axis=0)
    add_bb_max = np.max(verts_np, axis=0)
    add_mid = (add_bb_min + add_bb_max) / 2
    add_centroid = np.mean(verts_np, axis=0)
    dist_no_flip = np.linalg.norm(add_centroid - main_centroid)
    best_dist = dist_no_flip
    best_axis = None
    for axis in range(3):
        flipped_centroid = add_centroid.copy()
        flipped_centroid[axis] = 2 * add_mid[axis] - add_centroid[axis]
        dist = np.linalg.norm(flipped_centroid - main_centroid)
        if dist < best_dist - EPSILON:
            best_dist = dist
            best_axis = axis
    flip_axis = None
    mid_value = None
    if best_axis is not None:
        axis_name = ['X', 'Y', 'Z'][best_axis]
        print(f"Detected mirror flip on {axis_name} axis for additional file {additional_file}")
        flip_axis = best_axis
        add_offset_axis = np.floor(add_bb_min[best_axis])
        shift = [bX, bY, bZ][best_axis]
        mid_value = add_mid[best_axis] - add_offset_axis + shift
    else:
        print(f"No mirror flip detected for additional file {additional_file}")
    add_offset = np.floor(add_bb_min)
    verts_np -= add_offset
    verts_np += np.array([bX, bY, bZ])
    triangles_add = []
    for face in faces:
        if len(face) == 4:
            tri1 = [verts_np[face[0]], verts_np[face[1]], verts_np[face[2]]]
            v0, v1, v2 = tri1
            cross = np.cross(v1 - v0, v2 - v0)
            area = np.linalg.norm(cross) / 2
            if area > 0.01:
                triangles_add.append(tri1)
            tri2 = [verts_np[face[0]], verts_np[face[2]], verts_np[face[3]]]
            v0, v1, v2 = tri2
            cross = np.cross(v1 - v0, v2 - v0)
            area = np.linalg.norm(cross) / 2
            if area > 0.01:
                triangles_add.append(tri2)
        elif len(face) == 3:
            tri = [verts_np[face[0]], verts_np[face[1]], verts_np[face[2]]]
            v0, v1, v2 = tri
            cross = np.cross(v1 - v0, v2 - v0)
            area = np.linalg.norm(cross) / 2
            if area > 0.01:
                triangles_add.append(tri)
    additional_triangles.append((bX, bY, bZ, triangles_add, flip_axis, mid_value))
res = args.res
voxel_size = 1.0 / res
extents_base = np.array([0.5 / res] * 3)
global_shape = (size_x * res, size_y * res, size_z * res)
global_filled = np.zeros(global_shape, dtype=bool)
for bx in range(size_x):
    for by in range(size_y):
        for bz in range(size_z):
            mark_surface(bx, by, bz, triangles, res, extents_base, voxel_size, None, None, global_filled, size_x, size_y, size_z)
for bX, bY, bZ, add_triangles, flip_axis, mid_value in additional_triangles:
    mark_surface(bX, bY, bZ, add_triangles, res, extents_base, voxel_size, flip_axis, mid_value, global_filled, size_x, size_y, size_z)
print("Surface voxels marked:", np.sum(global_filled))
if args.fill_interior:
    visited = np.zeros(global_shape, dtype=bool)
    air_queue = deque()
    directions = []
    for dx in range(-1, 2):
        for dy in range(-1, 2):
            for dz in range(-1, 2):
                if (dx, dy, dz) != (0, 0, 0):
                    directions.append((dx, dy, dz))
    for dim in range(3):
        low = 0
        high = global_shape[dim] - 1
        for i in range(global_shape[(dim + 1) % 3]):
            for j in range(global_shape[(dim + 2) % 3]):
                for val in [low, high]:
                    pos = [0, 0, 0]
                    pos[dim] = val
                    pos[(dim + 1) % 3] = i
                    pos[(dim + 2) % 3] = j
                    pos_tuple = tuple(pos)
                    if not global_filled[pos_tuple]:
                        air_queue.append(pos_tuple)
                        visited[pos_tuple] = True
    while air_queue:
        pos = air_queue.popleft()
        for d in directions:
            npos = tuple(np.array(pos) + np.array(d))
            if all(0 <= npos[k] < global_shape[k] for k in range(3)) and not visited[npos] and not global_filled[npos]:
                visited[npos] = True
                air_queue.append(npos)
    for gx in range(global_shape[0]):
        for gy in range(global_shape[1]):
            for gz in range(global_shape[2]):
                if not visited[gx, gy, gz] and not global_filled[gx, gy, gz]:
                    global_filled[gx, gy, gz] = True
print("Total voxels filled after interior:", np.sum(global_filled))
if args.min_vox > 1:
    visited = np.zeros(global_shape, dtype=bool)
    directions = [(1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)] # 6-neighbor
    for gx in range(global_shape[0]):
        for gy in range(global_shape[1]):
            for gz in range(global_shape[2]):
                if global_filled[gx, gy, gz] and not visited[gx, gy, gz]:
                    component = []
                    queue = deque([(gx, gy, gz)])
                    visited[gx, gy, gz] = True
                    while queue:
                        pos = queue.popleft()
                        component.append(pos)
                        for d in directions:
                            npos = tuple(np.array(pos) + d)
                            if all(0 <= npos[k] < global_shape[k] for k in range(3)) and global_filled[npos] and not visited[npos]:
                                visited[npos] = True
                                queue.append(npos)
                    if len(component) < args.min_vox:
                        for pos in component:
                            global_filled[pos] = False
for bx in range(size_x):
    for by in range(size_y):
        for bz in range(size_z):
            local_filled = global_filled[bx * res:(bx + 1) * res, by * res:(by + 1) * res, bz * res:(bz + 1) * res]
            aabbs = merge_voxels(local_filled, res, min_vox=args.min_vox)
            if aabbs:
                lines = [f'if (bX == {bx} && bY == {by} && bZ == {bz}) {{']
                for minx, miny, minz, maxx, maxy, maxz in aabbs:
                    lines.append(f' main.add(new AABB({minx:.4f}D, {miny:.4f}D, {minz:.4f}D, {maxx:.4f}D, {maxy:.4f}D, {maxz:.4f}D));')
                lines.append('}')
                output_lines.append('\n'.join(lines))
with open(output_file, 'w') as f:
    f.write('\n'.join(output_lines))
print(f"Output written to {output_file}")