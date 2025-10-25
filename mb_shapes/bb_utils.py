# bb_utils.py
import os
import torch
import itertools

# Worker initialization function
def init_worker():
    os.environ['OMP_NUM_THREADS'] = '1'
    torch.set_num_threads(1)

# Function to find the maximum box in voxel grid
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

# Function to merge AABBs along a specific dimension
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

# Function to merge AABBs iteratively across dimensions
def merge_aabbs(aabbs):
    if not aabbs: return []
    previous_len = -1
    while len(aabbs) != previous_len:
        previous_len = len(aabbs)
        for dim in range(3):
            aabbs = merge_along_dim(aabbs, dim)
    return aabbs

# Threshold value parsing function
def parse_thresh_val(s, default=None):
    s = s.lower()
    if s == 'd' and default is not None:
        return default
    if s == 'x':
        return -1
    return int(s)