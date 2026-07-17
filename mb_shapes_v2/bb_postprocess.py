import numpy as np
from scipy import ndimage


def fill_gaps_along_axis(occupied_np, axis, threshold, is_excluded=None, max_intrude=-1, ex_threshold=None):
    if threshold < 0:
        return occupied_np
    shape = occupied_np.shape
    if ex_threshold is None:
        ex_threshold = threshold
    perm = {0: (0, 1, 2), 1: (1, 0, 2), 2: (2, 0, 1)}[axis]
    inv = np.argsort(perm)
    occ = np.transpose(occupied_np, perm)
    exc = np.transpose(is_excluded, perm) if is_excluded is not None else None
    n = occ.shape[0]
    for b in range(occ.shape[1]):
        for c in range(occ.shape[2]):
            for x in range(1, n - 1):
                if occ[x, b, c]:
                    continue
                curr_thresh = threshold
                if exc is not None and exc[x, b, c]:
                    if ex_threshold < 0:
                        continue
                    curr_thresh = ex_threshold
                max_d = n if curr_thresh == 0 else curr_thresh + 1
                left = None
                intrude_count = 0
                for d in range(1, max_d):
                    if x - d < 0:
                        break
                    ck = x - d
                    if exc is not None and exc[ck, b, c]:
                        intrude_count += 1
                    else:
                        intrude_count = 0
                    if 0 <= max_intrude < intrude_count:
                        break
                    if occ[ck, b, c]:
                        left = ck
                        break
                right = None
                intrude_count = 0
                for d in range(1, max_d):
                    if x + d >= n:
                        break
                    ck = x + d
                    if exc is not None and exc[ck, b, c]:
                        intrude_count += 1
                    else:
                        intrude_count = 0
                    if 0 <= max_intrude < intrude_count:
                        break
                    if occ[ck, b, c]:
                        right = ck
                        break
                if left is not None and right is not None and (curr_thresh == 0 or (right - left - 1) <= curr_thresh):
                    for fx in range(left + 1, right):
                        if exc is None or not exc[fx, b, c]:
                            occ[fx, b, c] = True
    return np.transpose(occ, inv)


def fill_gaps(np_arr, gap_passes, axis_order, thresholds, is_excluded=None, max_intrude_dict=None, ex_thresholds=None):
    if all(t < 0 for t in thresholds):
        return np_arr
    for _ in range(gap_passes):
        for axis in axis_order:
            thresh = thresholds[axis]
            max_intrude = max_intrude_dict[axis] if max_intrude_dict else -1
            ex_thresh = ex_thresholds[axis] if ex_thresholds is not None else thresh
            if thresh >= 0:
                original = np.copy(np_arr) if is_excluded is not None else None
                np_arr = fill_gaps_along_axis(np_arr, axis, thresh, is_excluded=is_excluded, max_intrude=max_intrude, ex_threshold=ex_thresh)
                if is_excluded is not None:
                    np_arr[is_excluded] = original[is_excluded]
    return np_arr


def _remove_protrusions_scale(np_arr, prange):
    kx, ky, kz = prange[0] + 1, prange[1] + 1, prange[2] + 1
    sx, sy, sz = np_arr.shape
    if kx > sx or ky > sy or kz > sz:
        return np_arr
    sw = np.lib.stride_tricks.sliding_window_view(np_arr, (kx, ky, kz))
    anchors = sw.all(axis=(3, 4, 5))
    covered = np.zeros_like(np_arr)
    ax, ay, az = anchors.shape
    for dx in range(kx):
        for dy in range(ky):
            for dz in range(kz):
                covered[dx:dx + ax, dy:dy + ay, dz:dz + az] |= anchors
    candidates = np_arr & ~covered
    if not candidates.any():
        return np_arr
    lbl, n = ndimage.label(candidates, structure=np.ones((3, 3, 3), dtype=int))
    for i, comp in enumerate(ndimage.find_objects(lbl)):
        if comp is None:
            continue
        size = tuple(s.stop - s.start for s in comp)
        if size[0] <= prange[0] and size[1] <= prange[1] and size[2] <= prange[2]:
            np_arr[comp][lbl[comp] == i + 1] = False
    return np_arr


def remove_protrusions(np_arr, prange=(1, 1, 1)):
    for s in range(1, max(prange) + 1):
        scaled = tuple(min(prange[d], s) for d in range(3))
        np_arr = _remove_protrusions_scale(np_arr, scaled)
    return np_arr


def fill_outside_corner_indents(np_arr, is_excluded=None):
    original = np.copy(np_arr) if is_excluded is not None else None
    to_fill = set()
    sx, sy, sz = np_arr.shape
    for y in range(sy):
        for x in range(sx - 1):
            for z in range(sz - 1):
                p00 = np_arr[x, y, z]
                p10 = np_arr[x + 1, y, z]
                p01 = np_arr[x, y, z + 1]
                p11 = np_arr[x + 1, y, z + 1]
                count = int(p00) + int(p10) + int(p01) + int(p11)
                if count != 3:
                    continue
                is_boundary_y = (y == 0 or y == sy - 1)
                full_adj = is_boundary_y
                if not is_boundary_y:
                    full_adj = False
                    if y > 0:
                        adj_count = int(np_arr[x, y - 1, z]) + int(np_arr[x + 1, y - 1, z]) + int(np_arr[x, y - 1, z + 1]) + int(np_arr[x + 1, y - 1, z + 1])
                        if adj_count >= 3:
                            full_adj = True
                    if not full_adj and y < sy - 1:
                        adj_count = int(np_arr[x, y + 1, z]) + int(np_arr[x + 1, y + 1, z]) + int(np_arr[x, y + 1, z + 1]) + int(np_arr[x + 1, y + 1, z + 1])
                        if adj_count >= 3:
                            full_adj = True
                if not full_adj:
                    continue
                outside_left = (x == 0 or not np_arr[x - 1, y, z]) and (x == 0 or not np_arr[x - 1, y, z + 1])
                outside_right = (x + 2 >= sx or not np_arr[x + 2, y, z]) and (x + 2 >= sx or not np_arr[x + 2, y, z + 1])
                outside_down = (z == 0 or not np_arr[x, y, z - 1]) and (z == 0 or not np_arr[x + 1, y, z - 1])
                outside_up = (z + 2 >= sz or not np_arr[x, y, z + 2]) and (z + 2 >= sz or not np_arr[x + 1, y, z + 2])
                if not p00 and outside_left and outside_down:
                    to_fill.add((x, y, z))
                if not p10 and outside_right and outside_down:
                    to_fill.add((x + 1, y, z))
                if not p01 and outside_left and outside_up:
                    to_fill.add((x, y, z + 1))
                if not p11 and outside_right and outside_up:
                    to_fill.add((x + 1, y, z + 1))
    for px, py, pz in to_fill:
        np_arr[px, py, pz] = True
    if is_excluded is not None:
        np_arr[is_excluded] = original[is_excluded]
    return np_arr


def _blocks_to_full(block_occupied, res, extra_blocks=()):
    keys = list(block_occupied) + list(extra_blocks)
    if not keys:
        return None, (0, 0, 0), (0, 0, 0)
    min_b = tuple(min(k[d] for k in keys) for d in range(3))
    max_b = tuple(max(k[d] for k in keys) for d in range(3))
    num = tuple(max_b[d] - min_b[d] + 1 for d in range(3))
    full = np.zeros((num[0] * res, num[1] * res, num[2] * res), dtype=bool)
    for (bx, by, bz), arr in block_occupied.items():
        full[(bx - min_b[0]) * res:(bx - min_b[0] + 1) * res, (by - min_b[1]) * res:(by - min_b[1] + 1) * res, (bz - min_b[2]) * res:(bz - min_b[2] + 1) * res] = arr
    return full, min_b, num


def _full_to_blocks(full, min_b, num, res):
    out = {}
    for bx in range(num[0]):
        for by in range(num[1]):
            for bz in range(num[2]):
                sub = full[bx * res:(bx + 1) * res, by * res:(by + 1) * res, bz * res:(bz + 1) * res]
                if sub.any():
                    out[(bx + min_b[0], by + min_b[1], bz + min_b[2])] = sub.copy()
    return out


def apply_postprocessing(block_occupied, gap_passes, axis_order, thresholds, regions, exclude_set, ex_thresholds, max_intrude_dict, per_block_gap_axes, pp_order_list, subs, do_global=True, res=16, y_corner_passes=0, protrusion_range=None, squarify_set=None):
    if do_global:
        pp_order_list = [s for s in pp_order_list if s.strip() != 'regional']
    for step in pp_order_list:
        if step == 'per-block':
            for b in list(block_occupied):
                block_occupied[b] = fill_gaps(block_occupied[b], gap_passes, axis_order, thresholds)
                for _ in range(y_corner_passes):
                    block_occupied[b] = fill_outside_corner_indents(block_occupied[b], None)
        elif step == 'regional':
            for region in regions:
                reg_blocks = region['blocks']
                if not reg_blocks:
                    continue
                reg_dict = {b: block_occupied[b] for b in reg_blocks if b in block_occupied}
                full, min_b, num = _blocks_to_full(reg_dict, res, extra_blocks=reg_blocks)
                if full is None:
                    continue
                full = fill_gaps(full, gap_passes, axis_order, region['thresholds'])
                for b in reg_blocks:
                    off = tuple((b[d] - min_b[d]) * res for d in range(3))
                    sl = full[off[0]:off[0] + res, off[1]:off[1] + res, off[2]:off[2] + res]
                    if sl.sum() > 0:
                        block_occupied[b] = sl.copy()
                    elif b in block_occupied:
                        del block_occupied[b]
        elif step == 'global':
            if all(t < 0 for t in thresholds) and not regions and not exclude_set:
                continue
            pre_region_blocks = {}
            for region in regions:
                for b in region['blocks']:
                    if b in block_occupied:
                        pre_region_blocks[b] = block_occupied[b].copy()
            excluded_processed = {}
            for ex_b in exclude_set:
                if ex_b not in block_occupied:
                    continue
                excluded_processed[ex_b] = fill_gaps(block_occupied[ex_b].copy(), gap_passes, axis_order, ex_thresholds)
            extra = set()
            for region in regions:
                extra |= set(region['blocks'])
            extra |= set(exclude_set)
            full, min_b, num = _blocks_to_full(block_occupied, res, extra_blocks=extra)
            if full is None:
                continue
            is_excluded_full = np.zeros_like(full, dtype=bool)
            for ex_b in list(exclude_set) + [b for region in regions for b in region['blocks']]:
                off = tuple((ex_b[d] - min_b[d]) * res for d in range(3))
                is_excluded_full[off[0]:off[0] + res, off[1]:off[1] + res, off[2]:off[2] + res] = True
            full = fill_gaps(full, gap_passes, axis_order, thresholds, is_excluded=is_excluded_full, max_intrude_dict=max_intrude_dict, ex_thresholds=ex_thresholds)
            for ex_b, arr in excluded_processed.items():
                off = tuple((ex_b[d] - min_b[d]) * res for d in range(3))
                full[off[0]:off[0] + res, off[1]:off[1] + res, off[2]:off[2] + res] = arr
            for region in regions:
                reg_blocks = region['blocks']
                if not reg_blocks:
                    continue
                reg_dict = {b: pre_region_blocks[b] for b in reg_blocks if b in pre_region_blocks}
                sub_full, sub_min, sub_num = _blocks_to_full(reg_dict, res, extra_blocks=reg_blocks)
                if sub_full is None:
                    continue
                sub_full = fill_gaps(sub_full, gap_passes, axis_order, region['thresholds'])
                for b in reg_blocks:
                    soff = tuple((b[d] - sub_min[d]) * res for d in range(3))
                    foff = tuple((b[d] - min_b[d]) * res for d in range(3))
                    full[foff[0]:foff[0] + res, foff[1]:foff[1] + res, foff[2]:foff[2] + res] = sub_full[soff[0]:soff[0] + res, soff[1]:soff[1] + res, soff[2]:soff[2] + res]
            block_occupied = _full_to_blocks(full, min_b, num, res)
        elif step == 'per-block-gaps':
            for b in block_occupied:
                occupied_np = block_occupied[b]
                for _ in range(gap_passes):
                    for axis in axis_order:
                        if (axis == 0 and 'x' in per_block_gap_axes) or (axis == 1 and 'y' in per_block_gap_axes) or (axis == 2 and 'z' in per_block_gap_axes):
                            thresh = thresholds[axis]
                            if thresh >= 0:
                                occupied_np = fill_gaps_along_axis(occupied_np, axis, thresh)
                block_occupied[b] = occupied_np
        elif step == 'protrusions':
            if protrusion_range is None:
                continue
            full, min_b, num = _blocks_to_full(block_occupied, res)
            if full is None:
                continue
            full = remove_protrusions(full, protrusion_range)
            block_occupied = _full_to_blocks(full, min_b, num, res)
        elif step == 'squarify':
            if not squarify_set:
                continue
            for b, region in squarify_set:
                if b not in block_occupied:
                    continue
                arr = block_occupied[b]
                if region is None:
                    r0 = (0, 0, 0)
                    sub = arr
                else:
                    minx, miny, minz, maxx, maxy, maxz = region
                    r0 = (max(0, minx), max(0, miny), max(0, minz))
                    sub = arr[r0[0]:min(res, maxx), r0[1]:min(res, maxy), r0[2]:min(res, maxz)]
                if sub.size == 0 or not sub.any():
                    continue
                lbl, n = ndimage.label(sub, structure=np.ones((3, 3, 3), dtype=int))
                for comp in ndimage.find_objects(lbl):
                    if comp is None:
                        continue
                    sub[comp] = True
                block_occupied[b] = arr
        elif step == 'sub-blocks':
            if not subs:
                continue
            extra = set((s[0], s[1], s[2]) for s in subs)
            full, min_b, num = _blocks_to_full(block_occupied, res, extra_blocks=extra)
            if full is None:
                continue
            for sub in subs:
                bx, by, bz, minx, miny, minz, maxx, maxy, maxz, is_solid = sub
                off = ((bx - min_b[0]) * res + minx, (by - min_b[1]) * res + miny, (bz - min_b[2]) * res + minz)
                end = ((bx - min_b[0]) * res + maxx, (by - min_b[1]) * res + maxy, (bz - min_b[2]) * res + maxz)
                off = tuple(max(0, o) for o in off)
                end = (min(full.shape[0], end[0]), min(full.shape[1], end[1]), min(full.shape[2], end[2]))
                if off[0] < end[0] and off[1] < end[1] and off[2] < end[2]:
                    full[off[0]:end[0], off[1]:end[1], off[2]:end[2]] = is_solid
            block_occupied = _full_to_blocks(full, min_b, num, res)
    return block_occupied
