import numpy as np
import argparse
import multiprocessing as mp
import os
from collections import deque
import ctypes
EPSILON = 1e-5
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
        return None, 0
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
    return None, 0
def intersects_box(triangle, box_center, block_extents):
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
    r = block_extents[Y] * np.abs(f0[Z]) + block_extents[Z] * np.abs(f0[Y])
    if max(-max(p0, p1, p2), min(p0, p1, p2)) > r:
        return False
    a01 = np.array([0, -f1[Z], f1[Y]])
    p0 = np.dot(v0, a01)
    p1 = np.dot(v1, a01)
    p2 = np.dot(v2, a01)
    r = block_extents[Y] * np.abs(f1[Z]) + block_extents[Z] * np.abs(f1[Y])
    if max(-max(p0, p1, p2), min(p0, p1, p2)) > r:
        return False
    a02 = np.array([0, -f2[Z], f2[Y]])
    p0 = np.dot(v0, a02)
    p1 = np.dot(v1, a02)
    p2 = np.dot(v2, a02)
    r = block_extents[Y] * np.abs(f2[Z]) + block_extents[Z] * np.abs(f2[Y])
    if max(-max(p0, p1, p2), min(p0, p1, p2)) > r:
        return False
    a10 = np.array([f0[Z], 0, -f0[X]])
    p0 = np.dot(v0, a10)
    p1 = np.dot(v1, a10)
    p2 = np.dot(v2, a10)
    r = block_extents[X] * np.abs(f0[Z]) + block_extents[Z] * np.abs(f0[X])
    if max(-max(p0, p1, p2), min(p0, p1, p2)) > r:
        return False
    a11 = np.array([f1[Z], 0, -f1[X]])
    p0 = np.dot(v0, a11)
    p1 = np.dot(v1, a11)
    p2 = np.dot(v2, a11)
    r = block_extents[X] * np.abs(f1[Z]) + block_extents[Z] * np.abs(f1[X])
    if max(-max(p0, p1, p2), min(p0, p1, p2)) > r:
        return False
    a12 = np.array([f2[Z], 0, -f2[X]])
    p0 = np.dot(v0, a12)
    p1 = np.dot(v1, a12)
    p2 = np.dot(v2, a12)
    r = block_extents[X] * np.abs(f2[Z]) + block_extents[Z] * np.abs(f2[X])
    if max(-max(p0, p1, p2), min(p0, p1, p2)) > r:
        return False
    a20 = np.array([-f0[Y], f0[X], 0])
    p0 = np.dot(v0, a20)
    p1 = np.dot(v1, a20)
    p2 = np.dot(v2, a20)
    r = block_extents[X] * np.abs(f0[Y]) + block_extents[Y] * np.abs(f0[X])
    if max(-max(p0, p1, p2), min(p0, p1, p2)) > r:
        return False
    a21 = np.array([-f1[Y], f1[X], 0])
    p0 = np.dot(v0, a21)
    p1 = np.dot(v1, a21)
    p2 = np.dot(v2, a21)
    r = block_extents[X] * np.abs(f1[Y]) + block_extents[Y] * np.abs(f1[X])
    if max(-max(p0, p1, p2), min(p0, p1, p2)) > r:
        return False
    a22 = np.array([-f2[Y], f2[X], 0])
    p0 = np.dot(v0, a22)
    p1 = np.dot(v1, a22)
    p2 = np.dot(v2, a22)
    r = block_extents[X] * np.abs(f2[Y]) + block_extents[Y] * np.abs(f2[X])
    if max(-max(p0, p1, p2), min(p0, p1, p2)) > r:
        return False
    if max(v0[X], v1[X], v2[X]) < -block_extents[X] or min(v0[X], v1[X], v2[X]) > block_extents[X]:
        return False
    if max(v0[Y], v1[Y], v2[Y]) < -block_extents[Y] or min(v0[Y], v1[Y], v2[Y]) > block_extents[Y]:
        return False
    if max(v0[Z], v1[Z], v2[Z]) < -block_extents[Z] or min(v0[Z], v1[Z], v2[Z]) > block_extents[Z]:
        return False
    plane_normal = np.cross(f0, f1)
    plane_distance = np.dot(plane_normal, v0)
    r = block_extents[X] * np.abs(plane_normal[X]) + block_extents[Y] * np.abs(plane_normal[Y]) + block_extents[Z] * np.abs(plane_normal[Z])
    if abs(plane_distance) > r:
        return False
    return True
def merge_voxels(filled, res, min_vox=1):
    aabbs = []
    visited = np.zeros_like(filled, dtype=bool)
    axis_perms = [(0,1,2), (0,2,1), (1,0,2), (1,2,0), (2,0,1), (2,1,0)]
    # Collect all filled positions
    seeds = list(zip(*np.where(filled)))
    if not seeds:
        return aabbs
    # Precompute best box for each seed
    seed_boxes = []
    for seed in seeds:
        best_volume = 0
        best_mins = None
        best_maxs = None
        for perm in axis_perms:
            mins = list(seed)
            maxs = list(seed)
            for ax in perm:
                # Expand max
                while maxs[ax] + 1 < res:
                    new_max = maxs[ax] + 1
                    slic = [slice(mins[0], maxs[0]+1), slice(mins[1], maxs[1]+1), slice(mins[2], maxs[2]+1)]
                    slic[ax] = new_max
                    if np.all(filled[tuple(slic)]):
                        maxs[ax] = new_max
                    else:
                        break
                # Expand min
                while mins[ax] - 1 >= 0:
                    new_min = mins[ax] - 1
                    slic = [slice(mins[0], maxs[0]+1), slice(mins[1], maxs[1]+1), slice(mins[2], maxs[2]+1)]
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
        if best_volume >= min_vox:
            seed_boxes.append((best_volume, seed, best_mins, best_maxs))
    # Sort by volume descending
    seed_boxes.sort(key=lambda x: -x[0])
    for _, seed, best_mins, best_maxs in seed_boxes:
        if not visited[seed]:
            visited[best_mins[0]:best_maxs[0]+1, best_mins[1]:best_maxs[1]+1, best_mins[2]:best_maxs[2]+1] = True
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
def mark_block(block_args):
    bx, by, bz, triangles, res, extents_base, voxel_size, flip_axis, mid_value = block_args
    model_min = np.array([bx, by, bz])
    block_center = model_min + 0.5
    block_extents = np.array([0.5, 0.5, 0.5])
    block_intersect = any(intersects_box(tri, block_center, block_extents) for tri in triangles)
    if not block_intersect:
        return []
    filled = np.zeros((res, res, res), dtype=bool)
    for ix in range(res):
        for iy in range(res):
            for iz in range(res):
                voxel_center = model_min + np.array([ix + 0.5, iy + 0.5, iz + 0.5]) / res
                for tri in triangles:
                    if intersects_box(tri, voxel_center, extents_base):
                        filled[ix, iy, iz] = True
                        break
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
def voxelize(verts_in, faces_in, res, overall_size, skip_fill=False):
    triangles = []
    for f in faces_in:
        if len(f) == 3:
            tri = verts_in[f]
            triangles.append(tri)
        elif len(f) == 4:
            tri1 = verts_in[[f[0], f[1], f[2]]]
            tri2 = verts_in[[f[0], f[2], f[3]]]
            area1 = np.linalg.norm(np.cross(tri1[1] - tri1[0], tri1[2] - tri1[0])) / 2
            area2 = np.linalg.norm(np.cross(tri2[1] - tri2[0], tri2[2] - tri2[0])) / 2
            if area1 > 0:
                triangles.append(tri1)
            if area2 > 0:
                triangles.append(tri2)
    marking_tasks = [(bx, by, bz, triangles, res, np.array([0.5 / res] * 3), 1.0 / res, None, 0) for bx in range(overall_size[0]) for by in range(overall_size[1]) for bz in range(overall_size[2])]
    with mp.Pool() as pool:
        mark_results = pool.map(mark_block, marking_tasks)
    global_shape = tuple((overall_size * res).astype(int))
    global_filled = np.zeros(global_shape, dtype=bool)
    for positions in mark_results:
        for gx, gy, gz in positions:
            global_filled[gx, gy, gz] = True
    if not skip_fill:
        global_filled = fill_local_interior(global_filled, global_shape)
    if args.min_voxel > 1:
        visited = np.zeros(global_shape, dtype=bool)
        directions = [(1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)]
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
                        if len(component) < args.min_voxel:
                            for pos in component:
                                global_filled[pos] = False
    return global_filled
def voxelize_single_block(verts_in, faces_in, res, min_voxel, skip_fill=False):
    triangles = []
    for f in faces_in:
        if len(f) == 3:
            tri = verts_in[f]
            triangles.append(tri)
        elif len(f) == 4:
            tri1 = verts_in[[f[0], f[1], f[2]]]
            tri2 = verts_in[[f[0], f[2], f[3]]]
            area1 = np.linalg.norm(np.cross(tri1[1] - tri1[0], tri1[2] - tri1[0])) / 2
            area2 = np.linalg.norm(np.cross(tri2[1] - tri2[0], tri2[2] - tri2[0])) / 2
            if area1 > 0:
                triangles.append(tri1)
            if area2 > 0:
                triangles.append(tri2)
    filled = np.zeros((res, res, res), dtype=bool)
    extents_base = np.array([0.5 / res] * 3)
    for ix in range(res):
        for iy in range(res):
            for iz in range(res):
                voxel_center = np.array([ix + 0.5, iy + 0.5, iz + 0.5]) / res
                for tri in triangles:
                    if intersects_box(tri, voxel_center, extents_base):
                        filled[ix, iy, iz] = True
                        break
    if not skip_fill:
        filled = fill_local_interior(filled, (res, res, res))
    aabb_list = merge_voxels(filled, res, min_voxel)
    return aabb_list
if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('filename', type=str, help='OBJ filename or directory')
    parser.add_argument('--res', type=int, default=16, help='Voxel resolution')
    parser.add_argument('--min-voxel', type=int, default=1, help='Min voxels for AABB and component')
    args = parser.parse_args()
    if os.path.isfile(args.filename):
        dir_path = os.path.dirname(os.path.abspath(args.filename))
        main_file = os.path.basename(args.filename)
        obj_files = [f for f in os.listdir(dir_path) if f.lower().endswith('.obj')]
        if main_file not in obj_files:
            raise ValueError("Specified file not found in directory.")
        print(f"Selected main file: {main_file}")
    else:
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
        while input("Add additional OBJ? y/n: ") == 'y':
            print("Available OBJ files for additional:")
            for i, f in enumerate(other_objs):
                print(f"{i}: {f}")
            index = int(input("Select OBJ index: "))
            selected_file = other_objs[index]
            pos_str = input("Enter position bX,bY,bZ: ")
            try:
                px, py, pz = map(int, pos_str.split(','))
            except ValueError:
                print("Invalid position input. Skipping.")
                continue
            type_str = input("Internal or External (i/e): ")
            is_internal = type_str.lower() == 'i'
            additional_objs.append((selected_file, px, py, pz, is_internal))
    # Load main
    main_path = os.path.join(dir_path, main_file)
    verts, faces = parse_obj(main_path)
    verts = np.array(verts, dtype=np.float32)
    bb_min = np.min(verts, axis=0)
    bb_max = np.max(verts, axis=0)
    bb_size = bb_max - bb_min
    print("bb_min:", bb_min, "bb_max:", bb_max)
    if np.all(bb_size <=1 + EPSILON):
        # Single block main
        verts -= bb_min
        overall_size = np.array([1, 1, 1])
        aabb_list = voxelize_single_block(verts, faces, args.res, args.min_voxel, skip_fill=False)
        main_aabbs = {(0, 0, 0): aabb_list}
        print("Main is single block, shifted min:", np.min(verts, axis=0), "shifted max:", np.max(verts, axis=0))
    else:
        # Multi block main
        min_block = np.floor(bb_min).astype(int)
        max_block = np.ceil(bb_max).astype(int) - 1
        overall_size = max_block - min_block + 1
        shift = -min_block.astype(float)
        verts += shift
        print("min_block:", min_block, "overall_size:", overall_size)
        print("shifted min:", np.min(verts, axis=0), "shifted max:", np.max(verts, axis=0))
        filled = voxelize(verts, faces, args.res, overall_size, skip_fill=False)
        main_aabbs = {}
        for bx in range(overall_size[0]):
            for by in range(overall_size[1]):
                for bz in range(overall_size[2]):
                    sub_filled = filled[bx * args.res:(bx + 1) * args.res, by * args.res:(by + 1) * args.res, bz * args.res:(bz + 1) * args.res]
                    aabb_list = merge_voxels(sub_filled, args.res, args.min_voxel)
                    if aabb_list:
                        main_aabbs[(bx, by, bz)] = aabb_list
    # Mirror - only detect and display, no process
    mirror_verts = None
    if mirrored_file:
        mirror_path = os.path.join(dir_path, mirrored_file)
        mirror_verts, mirror_faces = parse_obj(mirror_path)
        mirror_verts = np.array(mirror_verts, dtype=np.float32)
        flip_axis, mid_value = determine_flip(verts, mirror_verts)
        if flip_axis is not None:
            axis_name = ['X', 'Y', 'Z'][flip_axis]
            print(f"Detected mirror flip on {axis_name} axis")
        else:
            print("No mirror flip detected")
    final_aabbs = main_aabbs.copy()
    # Compute overall_size for output and add placeholders for external
    max_x = overall_size[0]
    max_y = overall_size[1]
    max_z = overall_size[2]
    for selected_file, px, py, pz, is_internal in additional_objs:
        add_path = os.path.join(dir_path, selected_file)
        add_verts, add_faces = parse_obj(add_path)
        add_verts = np.array(add_verts, dtype=np.float32)
        add_bb_min = np.min(add_verts, axis=0)
        add_bb_max = np.max(add_verts, axis=0)
        add_bb_size = add_bb_max - add_bb_min
        if np.all(add_bb_size <=1 + EPSILON):
            add_overall_size = np.array([1, 1, 1])
            key = (px, py, pz)
            final_aabbs[key] = [(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)] if not is_internal else []
            max_x = max(max_x, px + 1)
            max_y = max(max_y, py + 1)
            max_z = max(max_z, pz + 1)
        else:
            add_min_block = np.floor(add_bb_min).astype(int)
            add_max_block = np.ceil(add_bb_max).astype(int) - 1
            add_overall_size = add_max_block - add_min_block + 1
            for bx in range(add_overall_size[0]):
                for by in range(add_overall_size[1]):
                    for bz in range(add_overall_size[2]):
                        key = (px + bx, py + by, pz + bz)
                        final_aabbs[key] = [(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)] if not is_internal else []
            max_x = max(max_x, px + add_overall_size[0])
            max_y = max(max_y, py + add_overall_size[1])
            max_z = max(max_z, pz + add_overall_size[2])
    overall_size = np.array([max_x, max_y, max_z])
    # Write initial output with full blocks
    output_file = main_file.replace(".obj", ".txt")
    with open(output_file, 'w') as f:
        for bx in range(overall_size[0]):
            for by in range(overall_size[1]):
                for bz in range(overall_size[2]):
                    key = (bx, by, bz)
                    if key in final_aabbs and final_aabbs[key]:
                        print(f'if (bX == {bx} && bY == {by} && bZ == {bz}) {{', file=f)
                        for minx, miny, minz, maxx, maxy, maxz in final_aabbs[key]:
                            print(f'    main.add(new AABB({minx:.4f}D, {miny:.4f}D, {minz:.4f}D, {maxx:.4f}D, {maxy:.4f}D, {maxz:.4f}D));', file=f)
                        print('}', file=f)
    print("Initial output with full blocks written to", output_file)
    # Now, process each additional and replace
    for selected_file, px, py, pz, is_internal in additional_objs:
        add_path = os.path.join(dir_path, selected_file)
        add_verts, add_faces = parse_obj(add_path)
        add_verts = np.array(add_verts, dtype=np.float32)
        add_bb_min = np.min(add_verts, axis=0)
        add_bb_max = np.max(add_verts, axis=0)
        add_bb_size = add_bb_max - add_bb_min
        if np.all(add_bb_size <=1 + EPSILON):
            # Single block additional
            add_verts -= add_bb_min
            add_aabb_list = voxelize_single_block(add_verts, add_faces, args.res, args.min_voxel, skip_fill=not is_internal)
            key = (px, py, pz)
            final_aabbs[key] = add_aabb_list
        else:
            # Multi block additional
            add_min_block = np.floor(add_bb_min).astype(int)
            add_shift = -add_min_block.astype(float)
            add_verts += add_shift
            add_bb_max_shifted = np.max(add_verts, axis=0)
            add_overall_size = np.ceil(add_bb_max_shifted).astype(int)
            filled_add = voxelize(add_verts, add_faces, args.res, add_overall_size, skip_fill=not is_internal)
            add_aabbs = {}
            for bx in range(add_overall_size[0]):
                for by in range(add_overall_size[1]):
                    for bz in range(add_overall_size[2]):
                        sub_filled = filled_add[bx * args.res:(bx + 1) * args.res, by * args.res:(by + 1) * args.res, bz * args.res:(bz + 1) * args.res]
                        aabb_list = merge_voxels(sub_filled, args.res, args.min_voxel)
                        if aabb_list:
                            add_aabbs[(bx, by, bz)] = aabb_list
            for (bx, by, bz), aabb_list in add_aabbs.items():
                key = (px + bx, py + by, pz + bz)
                final_aabbs[key] = aabb_list
    # Write final output with replacements
    with open(output_file, 'w') as f:
        for bx in range(overall_size[0]):
            for by in range(overall_size[1]):
                for bz in range(overall_size[2]):
                    key = (bx, by, bz)
                    if key in final_aabbs and final_aabbs[key]:
                        print(f'if (bX == {bx} && bY == {by} && bZ == {bz}) {{', file=f)
                        for minx, miny, minz, maxx, maxy, maxz in final_aabbs[key]:
                            print(f'    main.add(new AABB({minx:.4f}D, {miny:.4f}D, {minz:.4f}D, {maxx:.4f}D, {maxy:.4f}D, {maxz:.4f}D));', file=f)
                        print('}', file=f)
    print("Final output with replacements written to", output_file)