import numpy as np
import argparse
import os
from collections import deque
import torch

EPSILON = 1e-5

try:
    import torch_directml
    device = torch_directml.device()
except ImportError:
    device = torch.device('cpu')
print(f"Using device: {device}")

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

def intersects_box_vec(triangles, voxel_centers, box_extents):
    N_vox = voxel_centers.shape[0]
    N_tri = triangles.shape[0]
    if N_tri == 0:
        return torch.zeros(N_vox, dtype=torch.bool, device=device)
    v0 = triangles[:, 0].unsqueeze(0) - voxel_centers.unsqueeze(1)  # (N_vox, N_tri, 3)
    v1 = triangles[:, 1].unsqueeze(0) - voxel_centers.unsqueeze(1)
    v2 = triangles[:, 2].unsqueeze(0) - voxel_centers.unsqueeze(1)
    f0 = triangles[:, 1] - triangles[:, 0]  # (N_tri, 3)
    f1 = triangles[:, 2] - triangles[:, 1]
    f2 = triangles[:, 0] - triangles[:, 2]
    f0 = f0.unsqueeze(0)  # (1, N_tri, 3)
    f1 = f1.unsqueeze(0)
    f2 = f2.unsqueeze(0)

    def axis_sep(a, f, ext0, ext1):
        p0 = (v0 * a).sum(-1)
        p1 = (v1 * a).sum(-1)
        p2 = (v2 * a).sum(-1)
        min_p = torch.minimum(torch.minimum(p0, p1), p2)
        max_p = torch.maximum(torch.maximum(p0, p1), p2)
        rad = box_extents[ext0] * torch.abs(f[:, :, ext1]) + box_extents[ext1] * torch.abs(f[:, :, ext0])
        sep = torch.max(-max_p, min_p) > rad
        return sep

    # a00 group (yz)
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

    # a10 group (xz)
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

    # a20 group (xy)
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

    # AABB sep
    eps = EPSILON
    vx = torch.stack([v0[:, :, 0], v1[:, :, 0], v2[:, :, 0]], dim=2)  # (v, t, 3)
    min_vx = vx.min(2)[0]
    max_vx = vx.max(2)[0]
    sep_x = (max_vx < -box_extents[0] - eps) | (min_vx > box_extents[0] + eps)

    vy = torch.stack([v0[:, :, 1], v1[:, :, 1], v2[:, :, 1]], dim=2)
    min_vy = vy.min(2)[0]
    max_vy = vy.max(2)[0]
    sep_y = (max_vy < -box_extents[1] - eps) | (min_vy > box_extents[1] + eps)

    vz = torch.stack([v0[:, :, 2], v1[:, :, 2], v2[:, :, 2]], dim=2)
    min_vz = vz.min(2)[0]
    max_vz = vz.max(2)[0]
    sep_z = (max_vz < -box_extents[2] - eps) | (min_vz > box_extents[2] + eps)

    # Plane sep
    # normal = torch.cross(f0[0], f1[0], dim=-1).unsqueeze(0)  # (1, N_tri, 3)
    # Manual cross product to avoid unsupported op
    normal_x = f0[0,:,1] * f1[0,:,2] - f0[0,:,2] * f1[0,:,1]
    normal_y = f0[0,:,2] * f1[0,:,0] - f0[0,:,0] * f1[0,:,2]
    normal_z = f0[0,:,0] * f1[0,:,1] - f0[0,:,1] * f1[0,:,0]
    normal = torch.stack([normal_x, normal_y, normal_z], dim=-1).unsqueeze(0)  # (1, N_tri, 3)
    plane_d = (v0 * normal).sum(-1)  # (v, t)
    plane_r = box_extents[0] * torch.abs(normal[:, :, 0]) + box_extents[1] * torch.abs(normal[:, :, 1]) + box_extents[2] * torch.abs(normal[:, :, 2])
    sep_plane = torch.abs(plane_d) > plane_r + eps

    all_sep = sep00 | sep01 | sep02 | sep10 | sep11 | sep12 | sep20 | sep21 | sep22 | sep_x | sep_y | sep_z | sep_plane
    intersects_per_pair = ~all_sep
    intersects_per_vox = intersects_per_pair.any(dim=1)
    return intersects_per_vox

def merge_voxels(filled, res, is_mirrored=False):
    aabbs = []
    visited = np.zeros_like(filled, dtype=bool)
    for x in range(res):
        for y in range(res):
            for z in range(res):
                if filled[x, y, z] and not visited[x, y, z]:
                    x_min, y_min, z_min = x, y, z
                    x_max, y_max, z_max = x, y, z
                    while x_max + 1 < res and np.all(filled[x_max + 1, y_min:y_max + 1, z_min:z_max + 1]):
                        x_max += 1
                    while y_max + 1 < res and np.all(filled[x_min:x_max + 1, y_max + 1, z_min:z_max + 1]):
                        y_max += 1
                    while z_max + 1 < res and np.all(filled[x_min:x_max + 1, y_min:y_max + 1, z_max + 1]):
                        z_max += 1
                    visited[x_min:x_max + 1, y_min:y_max + 1, z_min:z_max + 1] = True
                    min_aabb = (x_min / res, y_min / res, z_min / res)
                    max_aabb = ((x_max + 1) / res, (y_max + 1) / res, (z_max + 1) / res)
                    if is_mirrored:
                        temp = min_aabb[2]
                        min_aabb = (min_aabb[0], min_aabb[1], 1 - max_aabb[2])
                        max_aabb = (max_aabb[0], max_aabb[1], 1 - temp)
                    aabbs.append(min_aabb + max_aabb)
    return aabbs

def mark_surface(bx, by, bz, triangles, res, extents_base, voxel_size, is_mirrored, global_filled, size_x, size_y, size_z):
    model_min = np.array([bx, by, bz])
    block_center = model_min + 0.5
    block_extents = np.array([0.5, 0.5, 0.5])
    block_intersect = any(intersects_box(tri, block_center, block_extents) for tri in triangles)
    if not block_intersect:
        return
    # Generate voxel centers
    ix, iy, iz = np.meshgrid(np.arange(res), np.arange(res), np.arange(res), indexing='ij')
    local_centers = np.stack([ix, iy, iz], axis=-1).astype(np.float64) + 0.5
    local_centers *= voxel_size
    voxel_centers_flat = local_centers.reshape(-1, 3) + model_min
    # To torch
    triangles_t = torch.from_numpy(np.stack(triangles)).to(device=device, dtype=torch.float64)
    voxel_centers_t = torch.from_numpy(voxel_centers_flat).to(device=device, dtype=torch.float64)
    extents_base_t = torch.from_numpy(extents_base).to(device=device, dtype=torch.float64)
    intersects = intersects_box_vec(triangles_t, voxel_centers_t, extents_base_t)
    filled_flat = intersects.cpu().numpy()
    filled = filled_flat.reshape((res, res, res))
    if is_mirrored:
        filled = filled[:, :, ::-1]
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

parser = argparse.ArgumentParser(description='Voxelize OBJ for AABB collision with GPU support')
parser.add_argument('filename', type=str, help='OBJ filename')
parser.add_argument('--res', type=int, default=8, help='Voxel resolution')
parser.add_argument('--size_x', type=int, default=None, help='X size (auto if None)')
parser.add_argument('--size_y', type=int, default=None, help='Y size (auto if None)')
parser.add_argument('--size_z', type=int, default=None, help='Z size (auto if None)')
parser.add_argument('--offset_x', type=float, default=None, help='X offset (auto if None)')
parser.add_argument('--offset_y', type=float, default=None, help='Y offset (auto if None)')
parser.add_argument('--offset_z', type=float, default=None, help='Z offset (auto if None)')
args = parser.parse_args()

dir_path = os.path.dirname(os.path.abspath(args.filename))
obj_files = [f for f in os.listdir(dir_path) if f.endswith('.obj')]
print("Detected OBJ files:")
for i, f in enumerate(obj_files):
    print(f"{i}: {f}")

main_index = int(input("Select main OBJ index: "))
main_file = obj_files[main_index]
print(f"Selected main file: {main_file}")

mirrored_index = int(input("Select mirrored OBJ index (-1 for none): "))
mirrored_file = obj_files[mirrored_index] if mirrored_index >= 0 else None

additional_objs = []
while input("Add animation/additional OBJ? y/n: ") == 'y':
    index = int(input("Select OBJ index: "))
    bX = int(input("bX position: "))
    bY = int(input("bY position: "))
    bZ = int(input("bZ position: "))
    is_mirrored = input("Is mirrored (y/n): ") == 'y'
    additional_objs.append((obj_files[index], bX, bY, bZ, is_mirrored))

output_file = main_file.replace(".obj", ".txt")
output_lines = []

# Load main
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
        triangles.append([verts[face[0]], verts[face[1]], verts[face[2]]])
        triangles.append([verts[face[0]], verts[face[2]], verts[face[3]]])
    elif len(face) == 3:
        triangles.append([verts[face[0]], verts[face[1]], verts[face[2]]])
triangles = [np.array(t) - offset for t in triangles]

# Load mirrored if selected
mirrored_triangles = None
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
    mirrored_bb_min = np.min(mirrored_verts_np, axis=0)
    mirrored_bb_max = np.max(mirrored_verts_np, axis=0)
    for vert in mirrored_verts_np:
        vert[2] = mirrored_bb_min[2] + mirrored_bb_max[2] - vert[2]
    mirrored_triangles = []
    for face in mirrored_faces:
        if len(face) == 4:
            mirrored_triangles.append([mirrored_verts_np[face[0]], mirrored_verts_np[face[1]], mirrored_verts_np[face[2]]])
            mirrored_triangles.append([mirrored_verts_np[face[0]], mirrored_verts_np[face[2]], mirrored_verts_np[face[3]]])
        elif len(face) == 3:
            mirrored_triangles.append([mirrored_verts_np[face[0]], mirrored_verts_np[face[1]], mirrored_verts_np[face[2]]])
    mirrored_triangles = [np.array(t) - offset for t in mirrored_triangles]

# Load additional OBJs
additional_triangles = []
for additional_file, bX, bY, bZ, is_mirrored in additional_objs:
    print(f"Processing additional file: {additional_file} at bX={bX}, bY={bY}, bZ={bZ}, is_mirrored={is_mirrored}")
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
    add_bb_min = np.min(verts_np, axis=0)
    add_bb_max = np.max(verts_np, axis=0)
    add_offset = np.floor(add_bb_min)
    if is_mirrored:
        for vert in verts_np:
            vert[2] = add_bb_min[2] + add_bb_max[2] - vert[2]
    verts_np -= add_offset  # Normalize to start from 0
    verts_np += np.array([bX, bY, bZ])  # Place at block position
    triangles_add = []
    for face in faces:
        if len(face) == 4:
            triangles_add.append([verts_np[face[0]], verts_np[face[1]], verts_np[face[2]]])
            triangles_add.append([verts_np[face[0]], verts_np[face[2]], verts_np[face[3]]])
        elif len(face) == 3:
            triangles_add.append([verts_np[face[0]], verts_np[face[1]], verts_np[face[2]]])
    additional_triangles.append((bX, bY, bZ, [np.array(t) for t in triangles_add], is_mirrored))

res = args.res
voxel_size = 1.0 / res
extents_base = np.array([0.5 / res] * 3)

# Global filled for surface
global_shape = (size_x * res, size_y * res, size_z * res)
global_filled = np.zeros(global_shape, dtype=bool)

# Mark main surfaces
for bx in range(size_x):
    for by in range(size_y):
        for bz in range(size_z):
            mark_surface(bx, by, bz, triangles, res, extents_base, voxel_size, False, global_filled, size_x, size_y, size_z)

# Mark mirrored surfaces
if mirrored_triangles is not None:
    for bx in range(size_x):
        for by in range(size_y):
            for bz in range(size_z):
                flipped_bz = size_z - 1 - bz
                mark_surface(bx, by, flipped_bz, mirrored_triangles, res, extents_base, voxel_size, True, global_filled, size_x, size_y, size_z)

# Mark additional surfaces
for bX, bY, bZ, add_triangles, is_mirrored in additional_triangles:
    mark_surface(bX, bY, bZ, add_triangles, res, extents_base, voxel_size, is_mirrored, global_filled, size_x, size_y, size_z)

# Flood fill to mark exterior air
visited = np.zeros(global_shape, dtype=bool)
air_queue = deque()
directions = [(1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)]
# Seed queue with boundary voxels if air
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

# Fill interior (unvisited air) as solid
for gx in range(global_shape[0]):
    for gy in range(global_shape[1]):
        for gz in range(global_shape[2]):
            if not visited[gx, gy, gz] and not global_filled[gx, gy, gz]:
                global_filled[gx, gy, gz] = True

# Generate AABBs per block
for bx in range(size_x):
    for by in range(size_y):
        for bz in range(size_z):
            local_filled = global_filled[bx * res:(bx + 1) * res, by * res:(by + 1) * res, bz * res:(bz + 1) * res]
            aabbs = merge_voxels(local_filled, res)
            if aabbs:
                lines = [f'if (bX == {bx} && bY == {by} && bZ == {bz}) {{']
                for minx, miny, minz, maxx, maxy, maxz in aabbs:
                    lines.append(f'    main.add(new AABB({minx:.4f}D, {miny:.4f}D, {minz:.4f}D, {maxx:.4f}D, {maxy:.4f}D, {maxz:.4f}D));')
                lines.append('}')
                output_lines.append('\n'.join(lines))

with open(output_file, 'w') as f:
    f.write('\n'.join(output_lines))
print(f"Output written to {output_file}")