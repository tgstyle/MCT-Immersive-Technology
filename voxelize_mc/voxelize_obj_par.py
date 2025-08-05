import numpy as np
import argparse
import multiprocessing as mp
import os
from collections import deque
import ctypes

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
    if max([v0[X], v1[X], v2[X]]) < -box_extents[X] or min([v0[X], v1[X], v2[X]]) > box_extents[X]:
        return False
    if max([v0[Y], v1[Y], v2[Y]]) < -box_extents[Y] or min([v0[Y], v1[Y], v2[Y]]) > box_extents[Y]:
        return False
    if max([v0[Z], v1[Z], v2[Z]]) < -box_extents[Z] or min([v0[Z], v1[Z], v2[Z]]) > box_extents[Z]:
        return False
    plane_normal = np.cross(f0, f1)
    plane_distance = np.dot(plane_normal, v0)
    r = box_extents[X] * np.abs(plane_normal[X]) + box_extents[Y] * np.abs(plane_normal[Y]) + box_extents[Z] * np.abs(plane_normal[Z])
    if abs(plane_distance) > r:
        return False
    return True

def merge_voxels(filled, res):
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
                    aabbs.append(min_aabb + max_aabb)
    return aabbs

def mark_block(block_args):
    bx, by, bz, triangles, res, offset, extents_base, voxel_size, is_mirrored = block_args
    filled = np.zeros((res, res, res), dtype=bool)
    model_min = np.array([bx, by, bz]) + offset
    block_center = model_min + 0.5
    block_extents = np.array([0.5, 0.5, 0.5])
    block_intersect = any(intersects_box(tri, block_center, block_extents) for tri in triangles)
    if not block_intersect:
        return []
    for ix in range(res):
        for iy in range(res):
            for iz in range(res):
                local_center = np.array([(ix + 0.5) * voxel_size, (iy + 0.5) * voxel_size, (iz + 0.5) * voxel_size])
                voxel_center = model_min + local_center
                for tri in triangles:
                    if intersects_box(tri, voxel_center, extents_base):
                        filled[ix, iy, iz] = True
                        break
    if is_mirrored:
        filled = filled[:, :, ::-1]
    local_positions = np.where(filled)
    global_positions = []
    for i in range(len(local_positions[0])):
        ix = local_positions[0][i]
        iy = local_positions[1][i]
        iz = local_positions[2][i]
        gx = bx * res + ix
        gy = by * res + iy
        gz = bz * res + iz
        global_positions.append((gx, gy, gz))
    return global_positions

def init_pool(shared_arr_, global_shape_):
    global shared_arr, global_shape
    shared_arr = shared_arr_
    global_shape = global_shape_

def process_block(block_args):
    bx, by, bz, _, res, _, _, _, _ = block_args
    global_filled = np.ctypeslib.as_array(shared_arr).reshape(global_shape)
    local_filled = global_filled[bx * res:(bx + 1) * res, by * res:(by + 1) * res, bz * res:(bz + 1) * res]
    aabbs = merge_voxels(local_filled, res)
    if aabbs:
        lines = [f'if (bX == {bx} && bY == {by} && bZ == {bz}) {{']
        for minx, miny, minz, maxx, maxy, maxz in aabbs:
            lines.append(f'    main.add(new AABB({minx:.4f}D, {miny:.4f}D, {minz:.4f}D, {maxx:.4f}D, {maxy:.4f}D, {maxz:.4f}D));')
        lines.append('}')
        return '\n'.join(lines)
    return None

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Voxelize OBJ for AABB collision')
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
    triangles = np.array(triangles) - offset

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
        mirrored_triangles = np.array(mirrored_triangles) - offset

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
        triangles_add = np.array(triangles_add)
        additional_triangles.append((bX, bY, bZ, triangles_add, is_mirrored))

    res = args.res
    voxel_size = 1.0 / res
    extents_base = np.array([0.5 / res] * 3)

    # Prepare marking tasks
    marking_tasks = [(bx, by, bz, triangles, res, np.array([0,0,0]), extents_base, voxel_size, False) for bx in range(size_x) for by in range(size_y) for bz in range(size_z)]
    if mirrored_triangles is not None:
        mirrored_tasks = [(bx, by, size_z - 1 - bz, mirrored_triangles, res, np.array([0,0,0]), extents_base, voxel_size, True) for bx in range(size_x) for by in range(size_y) for bz in range(size_z)]
        marking_tasks += mirrored_tasks
    for bX, bY, bZ, add_triangles, is_mirrored in additional_triangles:
        marking_tasks.append((bX, bY, bZ, add_triangles, res, np.array([0,0,0]), extents_base, voxel_size, is_mirrored))

    # Mark surface in parallel
    with mp.Pool() as pool:
        mark_results = pool.map(mark_block, marking_tasks)

    # Global filled for surface
    global_shape = (size_x * res, size_y * res, size_z * res)
    global_filled = np.zeros(global_shape, dtype=bool)
    for positions in mark_results:
        for gx, gy, gz in positions:
            global_filled[gx, gy, gz] = True

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

    # Create shared array for read-only access
    shared_arr = mp.Array(ctypes.c_bool, global_filled.flatten(), lock=False)

    # Prepare process tasks for AABB generation
    process_tasks = [(bx, by, bz, None, res, None, None, None, None) for bx in range(size_x) for by in range(size_y) for bz in range(size_z)]

    # Generate AABBs in parallel
    with mp.Pool(initializer=init_pool, initargs=(shared_arr, global_shape)) as pool:
        results = pool.map(process_block, process_tasks)

    output_lines = [r for r in results if r]

    with open(output_file, 'w') as f:
        f.write('\n'.join(output_lines))
    print(f"Output written to {output_file}")