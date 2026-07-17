import itertools
import numpy as np


def parse_thresh_val(s, default=-1):
    s = s.strip().lower()
    if s == 'd':
        return default
    if s in ('x', '0', ''):
        return -1
    if s == 'u':
        return 0
    return int(s)


def parse_thresh_triple(s, defaults=(-1, -1, -1)):
    parts = s.split(',')
    return tuple(parse_thresh_val(parts[i] if i < len(parts) else 'd', defaults[i]) for i in range(3))


def find_max_box(occupied, visited, i, j, k, res):
    best = (0, 1, 1, 1)
    for order in itertools.permutations([0, 1, 2]):
        dx, dy, dz = 1, 1, 1
        for dim in order:
            if dim == 0:
                while i + dx < res and occupied[i + dx, j:j + dy, k:k + dz].all() and not visited[i + dx, j:j + dy, k:k + dz].any():
                    dx += 1
            elif dim == 1:
                while j + dy < res and occupied[i:i + dx, j + dy, k:k + dz].all() and not visited[i:i + dx, j + dy, k:k + dz].any():
                    dy += 1
            else:
                while k + dz < res and occupied[i:i + dx, j:j + dy, k + dz].all() and not visited[i:i + dx, j:j + dy, k + dz].any():
                    dz += 1
        volume = dx * dy * dz
        if volume > best[0]:
            best = (volume, dx, dy, dz)
    _, dx, dy, dz = best
    visited[i:i + dx, j:j + dy, k:k + dz] = True
    return dx, dy, dz


def merge_along_dim(aabbs, dim):
    epsilon = 1e-3
    if dim == 0:
        sort_key = lambda x: (x[1], x[4], x[2], x[5], x[0])
        min_idx, max_idx = 0, 3
        fixed_dims = [(1, 4), (2, 5)]
    elif dim == 1:
        sort_key = lambda x: (x[0], x[3], x[2], x[5], x[1])
        min_idx, max_idx = 1, 4
        fixed_dims = [(0, 3), (2, 5)]
    else:
        sort_key = lambda x: (x[0], x[3], x[1], x[4], x[2])
        min_idx, max_idx = 2, 5
        fixed_dims = [(0, 3), (1, 4)]
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
    if not aabbs:
        return []
    previous_len = -1
    while len(aabbs) != previous_len:
        previous_len = len(aabbs)
        for dim in range(3):
            aabbs = merge_along_dim(aabbs, dim)
    return aabbs


def extract_aabbs_from_occupied(occupied_np, res=16, force_merge=False):
    if occupied_np.sum() == 0:
        return []
    if occupied_np.all():
        return [(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)]
    occupied = np.asarray(occupied_np)
    visited = np.zeros_like(occupied, dtype=bool)
    block_aabbs = []
    for i in range(res):
        for j in range(res):
            for k in range(res):
                if occupied[i, j, k] and not visited[i, j, k]:
                    dx, dy, dz = find_max_box(occupied, visited, i, j, k, res)
                    block_aabbs.append((i / res, j / res, k / res, (i + dx) / res, (j + dy) / res, (k + dz) / res))
    block_aabbs = [a for a in block_aabbs if (a[3] - a[0]) * (a[4] - a[1]) * (a[5] - a[2]) > 0.001]
    pre_merge = list(block_aabbs)
    block_aabbs = merge_aabbs(block_aabbs)
    if not force_merge:
        vox_vol = occupied_np.sum() / (res ** 3)
        aabb_vol = sum((a[3] - a[0]) * (a[4] - a[1]) * (a[5] - a[2]) for a in block_aabbs)
        if abs(aabb_vol - vox_vol) > 0.01 * vox_vol:
            block_aabbs = pre_merge
    return block_aabbs
