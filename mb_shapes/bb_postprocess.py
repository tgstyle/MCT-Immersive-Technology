# bb_postprocess.py
from scipy import ndimage
import numpy as np

# Gap filling along specific axis
def fill_gaps_along_axis(occupied_np, axis, threshold, is_excluded=None, max_intrude=-1, ex_threshold=None):
    if threshold < 0:
        return occupied_np
    shape = occupied_np.shape
    if ex_threshold is None:
        ex_threshold = threshold
    if axis == 0: # x
        for y in range(shape[1]):
            for z in range(shape[2]):
                for x in range(1, shape[0] - 1):
                    if occupied_np[x, y, z]:
                        continue
                    curr_thresh = threshold
                    if is_excluded is not None and is_excluded[x, y, z]:
                        if ex_threshold < 0:
                            continue
                        curr_thresh = ex_threshold
                    left = None
                    max_dx = shape[0] if curr_thresh == 0 else curr_thresh + 1
                    intrude_count = 0
                    for dx in range(1, max_dx):
                        if x - dx < 0:
                            break
                        check_x = x - dx
                        check_excluded = is_excluded[check_x, y, z] if is_excluded is not None else False
                        if check_excluded:
                            intrude_count += 1
                        else:
                            intrude_count = 0
                        if 0 <= max_intrude < intrude_count:
                            break
                        if occupied_np[check_x, y, z]:
                            left = check_x
                            break
                    right = None
                    intrude_count = 0
                    for dx in range(1, max_dx):
                        if x + dx >= shape[0]:
                            break
                        check_x = x + dx
                        check_excluded = is_excluded[check_x, y, z] if is_excluded is not None else False
                        if check_excluded:
                            intrude_count += 1
                        else:
                            intrude_count = 0
                        if 0 <= max_intrude < intrude_count:
                            break
                        if occupied_np[check_x, y, z]:
                            right = check_x
                            break
                    if left is not None and right is not None and (curr_thresh == 0 or (right - left - 1) <= curr_thresh):
                        for fill_x in range(left + 1, right):
                            if is_excluded is None or not is_excluded[fill_x, y, z]:
                                occupied_np[fill_x, y, z] = True
    elif axis == 1: # y
        for x in range(shape[0]):
            for z in range(shape[2]):
                for y in range(1, shape[1] - 1):
                    if occupied_np[x, y, z]:
                        continue
                    curr_thresh = threshold
                    if is_excluded is not None and is_excluded[x, y, z]:
                        if ex_threshold < 0:
                            continue
                        curr_thresh = ex_threshold
                    floor_y = None
                    max_dy = shape[1] if curr_thresh == 0 else curr_thresh + 1
                    intrude_count = 0
                    for dy in range(1, max_dy):
                        if y - dy < 0:
                            break
                        check_y = y - dy
                        check_excluded = is_excluded[x, check_y, z] if is_excluded is not None else False
                        if check_excluded:
                            intrude_count += 1
                        else:
                            intrude_count = 0
                        if 0 <= max_intrude < intrude_count:
                            break
                        if occupied_np[x, check_y, z]:
                            floor_y = check_y
                            break
                    ceiling_y = None
                    intrude_count = 0
                    for dy in range(1, max_dy):
                        if y + dy >= shape[1]:
                            break
                        check_y = y + dy
                        check_excluded = is_excluded[x, check_y, z] if is_excluded is not None else False
                        if check_excluded:
                            intrude_count += 1
                        else:
                            intrude_count = 0
                        if 0 <= max_intrude < intrude_count:
                            break
                        if occupied_np[x, check_y, z]:
                            ceiling_y = check_y
                            break
                    if floor_y is not None and ceiling_y is not None and (curr_thresh == 0 or (ceiling_y - floor_y - 1) <= curr_thresh):
                        for fill_y in range(floor_y + 1, ceiling_y):
                            if is_excluded is None or not is_excluded[x, fill_y, z]:
                                occupied_np[x, fill_y, z] = True
    elif axis == 2: # z
        for x in range(shape[0]):
            for y in range(shape[1]):
                for z in range(1, shape[2] - 1):
                    if occupied_np[x, y, z]:
                        continue
                    curr_thresh = threshold
                    if is_excluded is not None and is_excluded[x, y, z]:
                        if ex_threshold < 0:
                            continue
                        curr_thresh = ex_threshold
                    front = None
                    max_dz = shape[2] if curr_thresh == 0 else curr_thresh + 1
                    intrude_count = 0
                    for dz in range(1, max_dz):
                        if z - dz < 0:
                            break
                        check_z = z - dz
                        check_excluded = is_excluded[x, y, check_z] if is_excluded is not None else False
                        if check_excluded:
                            intrude_count += 1
                        else:
                            intrude_count = 0
                        if 0 <= max_intrude < intrude_count:
                            break
                        if occupied_np[x, y, check_z]:
                            front = check_z
                            break
                    back = None
                    intrude_count = 0
                    for dz in range(1, max_dz):
                        if z + dz >= shape[2]:
                            break
                        check_z = z + dz
                        check_excluded = is_excluded[x, y, check_z] if is_excluded is not None else False
                        if check_excluded:
                            intrude_count += 1
                        else:
                            intrude_count = 0
                        if 0 <= max_intrude < intrude_count:
                            break
                        if occupied_np[x, y, check_z]:
                            back = check_z
                            break
                    if front is not None and back is not None and (curr_thresh == 0 or (back - front - 1) <= curr_thresh):
                        for fill_z in range(front + 1, back):
                            if is_excluded is None or not is_excluded[x, y, fill_z]:
                                occupied_np[x, y, fill_z] = True
    return occupied_np

# Protrusion removal function
def remove_protrusions(np_arr):
    shape = np_arr.shape
    directions = [(1,0,0), (-1,0,0), (0,1,0), (0,-1,0), (0,0,1), (0,0,-1)]

    # Neighbor counts via convolution
    kernel = np.array([[[0,0,0],[0,1,0],[0,0,0]],
                       [[0,1,0],[1,0,1],[0,1,0]],
                       [[0,0,0],[0,1,0],[0,0,0]]])
    neigh_counts = ndimage.convolve(np_arr.astype(int), kernel, mode='constant', cval=0)

    # Candidates: occupied with 1-2 neighbors
    candidates = np.where(np_arr & (neigh_counts > 0) & (neigh_counts <= 2))

    for idx in range(len(candidates[0])):
        x, y, z = candidates[0][idx], candidates[1][idx], candidates[2][idx]

        # Get neighbor positions and directions
        neigh_pos_list = []
        neigh_dirs = []
        for dx, dy, dz in directions:
            nx, ny, nz = x + dx, y + dy, z + dz
            if 0 <= nx < shape[0] and 0 <= ny < shape[1] and 0 <= nz < shape[2] and np_arr[nx, ny, nz]:
                neigh_pos_list.append((nx, ny, nz))
                neigh_dirs.append((dx, dy, dz))

        num_neigh = len(neigh_dirs)
        if num_neigh == 0:
            continue  # Isolated, but shouldn't happen

        # Get unique axes (0:x,1:y,2:z) for directions
        axes = []
        for d in neigh_dirs:
            axis = next(i for i, v in enumerate(map(abs, d)) if v > 0)
            axes.append(axis)

        # All unique axes? (different directions, no same same-axis multiples/opposites)
        if len(set(axes)) != num_neigh:
            continue  # Skip lines/sheets

        # Check each neighbor well-connected (>3 neighbors)
        if all(neigh_counts[nx, ny, nz] > 3 for nx, ny, nz in neigh_pos_list):
            np_arr[x, y, z] = False

    return np_arr

# Sub-post-processing functions
def remove_small_occupied(np_arr, small_occupied_threshold, is_excluded=None):
    original = np.copy(np_arr) if is_excluded is not None else None
    labels, num_labels = ndimage.label(np_arr)
    if num_labels > 0:
        sizes = ndimage.sum(np_arr, labels, range(1, num_labels + 1))
        small_mask = sizes < small_occupied_threshold
        remove_mask = np.isin(labels, np.where(small_mask)[0] + 1)
        np_arr[remove_mask] = False
    if is_excluded is not None:
        np_arr[is_excluded] = original[is_excluded]
    return np_arr

def fill_holes(np_arr, is_excluded=None):
    original = np.copy(np_arr) if is_excluded is not None else None
    np_arr = ndimage.binary_fill_holes(np_arr)
    if is_excluded is not None:
        np_arr[is_excluded] = original[is_excluded]
    return np_arr

def fill_voids(np_arr, small_void_threshold, fill_all_voids, is_excluded=None):
    original = np.copy(np_arr) if is_excluded is not None else None
    unoccupied = ~np_arr
    labels, num_labels = ndimage.label(unoccupied)
    if num_labels > 0:
        slices = ndimage.find_objects(labels)
        for lab in range(1, num_labels + 1):
            sl = slices[lab - 1]
            touches_boundary = any(s.start == 0 or s.stop == np_arr.shape[i] for i, s in enumerate(sl))
            if not touches_boundary:
                size = np.sum(labels == lab)
                if fill_all_voids or size < small_void_threshold:
                    np_arr[labels == lab] = True
    if is_excluded is not None:
        np_arr[is_excluded] = original[is_excluded]
    return np_arr

def fill_gaps(np_arr, gap_passes, axis_order, thresholds, is_excluded=None, max_intrude_dict=None, ex_thresholds=None):
    (x_th, y_th, z_th) = thresholds
    for _ in range(gap_passes):
        for axis in axis_order:
            thresh = {0: x_th, 1: y_th, 2: z_th}[axis]
            max_intrude = max_intrude_dict[axis] if max_intrude_dict else -1
            ex_thresh = ex_thresholds[axis] if ex_thresholds is not None else thresh
            if thresh >= 0:
                original = np.copy(np_arr) if is_excluded is not None else None
                np_arr = fill_gaps_along_axis(np_arr, axis, thresh, is_excluded=is_excluded, max_intrude=max_intrude, ex_threshold=ex_thresh)
                if is_excluded is not None:
                    np_arr[is_excluded] = original[is_excluded]
    return np_arr

def fill_outside_corner_indents(np_arr, is_excluded=None):
    original = np.copy(np_arr) if is_excluded is not None else None
    to_fill = set()
    sx, sy, sz = np_arr.shape
    for y in range(sy):
        for x in range(sx - 1):
            for z in range(sz - 1):
                # Extract 2x2 XZ block at this height
                p00 = np_arr[x, y, z]
                p10 = np_arr[x+1, y, z]
                p01 = np_arr[x, y, z+1]
                p11 = np_arr[x+1, y, z+1]
                count = int(p00) + int(p10) + int(p01) + int(p11)
                if count == 3:
                    # Relax full_adj for boundary Y (edge starts/ends); else require >=3/4 in adjacent slice
                    is_boundary_y = (y == 0 or y == sy - 1)
                    full_adj = is_boundary_y  # Assume valid for boundaries
                    if not is_boundary_y:
                        full_adj = False
                        if y > 0:
                            adj_count = int(np_arr[x, y-1, z]) + int(np_arr[x+1, y-1, z]) + int(np_arr[x, y-1, z+1]) + int(np_arr[x+1, y-1, z+1])
                            if adj_count >= 3:
                                full_adj = True
                        if not full_adj and y < sy - 1:
                            adj_count = int(np_arr[x, y+1, z]) + int(np_arr[x+1, y+1, z]) + int(np_arr[x, y+1, z+1]) + int(np_arr[x+1, y+1, z+1])
                            if adj_count >= 3:
                                full_adj = True
                    # Exactly one missing → check if it's an outside (convex) corner indent
                    if not p00 and full_adj:
                        # Missing bottom-left: check outside left (-x) and down (-z)
                        left_bottom_open = (x == 0 or not np_arr[x-1, y, z])
                        left_top_open = (x == 0 or not np_arr[x-1, y, z+1])
                        outside_left = left_bottom_open and left_top_open
                        down_left_open = (z == 0 or not np_arr[x, y, z-1])
                        down_right_open = (z == 0 or not np_arr[x+1, y, z-1])
                        outside_down = down_left_open and down_right_open
                        if outside_left and outside_down:
                            to_fill.add((x, y, z))
                    if not p10 and full_adj:
                        # Missing bottom-right: check outside right (+x) and down (-z)
                        right_bottom_open = (x + 2 >= sx or not np_arr[x+2, y, z])
                        right_top_open = (x + 2 >= sx or not np_arr[x+2, y, z+1])
                        outside_right = right_bottom_open and right_top_open
                        down_left_open = (z == 0 or not np_arr[x, y, z-1])
                        down_right_open = (z == 0 or not np_arr[x+1, y, z-1])
                        outside_down = down_left_open and down_right_open
                        if outside_right and outside_down:
                            to_fill.add((x+1, y, z))
                    if not p01 and full_adj:
                        # Missing top-left: check outside left (-x) and up (+z)
                        left_bottom_open = (x == 0 or not np_arr[x-1, y, z])
                        left_top_open = (x == 0 or not np_arr[x-1, y, z+1])
                        outside_left = left_bottom_open and left_top_open
                        up_left_open = (z + 2 >= sz or not np_arr[x, y, z+2])
                        up_right_open = (z + 2 >= sz or not np_arr[x+1, y, z+2])
                        outside_up = up_left_open and up_right_open
                        if outside_left and outside_up:
                            to_fill.add((x, y, z+1))
                    if not p11 and full_adj:
                        # Missing top-right: check outside right (+x) and up (+z)
                        right_bottom_open = (x + 2 >= sx or not np_arr[x+2, y, z])
                        right_top_open = (x + 2 >= sx or not np_arr[x+2, y, z+1])
                        outside_right = right_bottom_open and right_top_open
                        up_left_open = (z + 2 >= sz or not np_arr[x, y, z+2])
                        up_right_open = (z + 2 >= sz or not np_arr[x+1, y, z+2])
                        outside_up = up_left_open and up_right_open
                        if outside_right and outside_up:
                            to_fill.add((x+1, y, z+1))
    for px, py, pz in to_fill:
        np_arr[px, py, pz] = True
    if is_excluded is not None:
        np_arr[is_excluded] = original[is_excluded]
    return np_arr

# Main post-processing function
def process_post(np_arr, no_holes, no_gaps, no_small_voids, gap_passes, axis_order, thresholds, small_void_threshold, small_occupied_threshold, fill_all_voids, is_excluded=None, max_intrude_dict=None, ex_thresholds=None, sub_order='remove-small,fill-holes,fill-voids,fill-gaps', y_corner_passes=0):
    sub_order_list = [s.strip() for s in sub_order.split(',')]
    for sub_step in sub_order_list:
        if sub_step == 'remove-small' and not no_small_voids:
            np_arr = remove_small_occupied(np_arr, small_occupied_threshold, is_excluded)
        elif sub_step == 'fill-holes' and not no_holes:
            np_arr = fill_holes(np_arr, is_excluded)
        elif sub_step == 'fill-voids' and not no_small_voids:
            np_arr = fill_voids(np_arr, small_void_threshold, fill_all_voids, is_excluded)
        elif sub_step == 'fill-gaps' and not no_gaps:
            np_arr = fill_gaps(np_arr, gap_passes, axis_order, thresholds, is_excluded, max_intrude_dict, ex_thresholds)
        # fill-y-corners removed from here; handled explicitly in apply_postprocessing per-block only
    return np_arr

def apply_postprocessing(block_occupied, no_holes, no_gaps, no_small_voids, gap_passes, axis_order, thresholds, void_thresh, occ_thresh, fill_all_voids, regions, exclude_set, ex_thresholds, max_intrude_dict, per_block_gap_axes, sub_order, pp_order_list, subs, do_global=True, res=16, y_corner_passes=0):
    if do_global:
        pp_order_list = [s for s in pp_order_list if s.strip() != 'regional']
    for step in pp_order_list:
        if step == 'per-block':
            for b in list(block_occupied):
                # Run sub-post without y-corners
                block_occupied[b] = process_post(block_occupied[b], no_holes, no_gaps, no_small_voids, gap_passes, axis_order, thresholds, void_thresh, occ_thresh, fill_all_voids, sub_order=sub_order, y_corner_passes=0)
                # Explicitly run y-corners only here, with full passes (per-block for vertical edges)
                for _ in range(y_corner_passes):
                    block_occupied[b] = fill_outside_corner_indents(block_occupied[b], None)
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
                sub_occupied = process_post(sub_occupied, no_holes, no_gaps, no_small_voids, gap_passes, axis_order, region['thresholds'], void_thresh, occ_thresh, fill_all_voids, sub_order=sub_order, y_corner_passes=0)
                for b in reg_blocks:
                    off_x = (b[0] - min_bx_r) * res
                    off_y = (b[1] - min_by_r) * res
                    off_z = (b[2] - min_bz_r) * res
                    slice_occ = sub_occupied[off_x:off_x + res, off_y:off_y + res, off_z:off_z + res]
                    if slice_occ.sum() > 0:
                        block_occupied[b] = slice_occ.copy()
                    elif b in block_occupied:
                        del block_occupied[b]
        elif step == 'global':
            pre_region_blocks = {}
            for region in regions:
                for b in region['blocks']:
                    if b in block_occupied:
                        pre_region_blocks[b] = block_occupied[b].copy()
            excluded_processed = {}
            for ex_b in exclude_set:
                if ex_b not in block_occupied:
                    continue
                occupied_np_copy = block_occupied[ex_b].copy()
                occupied_np_copy = process_post(occupied_np_copy, no_holes, no_gaps, no_small_voids, gap_passes, axis_order, ex_thresholds, void_thresh, occ_thresh, fill_all_voids, sub_order=sub_order, y_corner_passes=0)
                excluded_processed[ex_b] = occupied_np_copy
            all_bxs = [bx for bx, _, _ in block_occupied] if block_occupied else []
            all_bys = [by for _, by, _ in block_occupied] if block_occupied else []
            all_bzs = [bz for _, _, bz in block_occupied] if block_occupied else []
            for region in regions:
                for bx, by, bz in region['blocks']:
                    all_bxs.append(bx)
                    all_bys.append(by)
                    all_bzs.append(bz)
            for bx, by, bz in exclude_set:
                all_bxs.append(bx)
                all_bys.append(by)
                all_bzs.append(bz)
            if all_bxs:
                min_bx = min(all_bxs)
                max_bx = max(all_bxs)
                min_by = min(all_bys)
                max_by = max(all_bys)
                min_bz = min(all_bzs)
                max_bz = max(all_bzs)
            else:
                min_bx = max_bx = min_by = max_by = min_bz = max_bz = 0
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
                bx, by, bz = ex_b
                off_x = (bx - min_bx) * res
                off_y = (by - min_by) * res
                off_z = (bz - min_bz) * res
                is_excluded_full[off_x:off_x + res, off_y:off_y + res, off_z:off_z + res] = True
            for region in regions:
                for b in region['blocks']:
                    bx, by, bz = b
                    off_x = (bx - min_bx) * res
                    off_y = (by - min_by) * res
                    off_z = (bz - min_bz) * res
                    is_excluded_full[off_x:off_x + res, off_y:off_y + res, off_z:off_z + res] = True
            full_occupied = process_post(full_occupied, no_holes, no_gaps, no_small_voids, gap_passes, axis_order, thresholds, void_thresh, occ_thresh, fill_all_voids, is_excluded=is_excluded_full, max_intrude_dict=max_intrude_dict, ex_thresholds=ex_thresholds, sub_order=sub_order, y_corner_passes=0)
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
                    if b in pre_region_blocks:
                        off_x = (b[0] - min_bx_r) * res
                        off_y = (b[1] - min_by_r) * res
                        off_z = (b[2] - min_bz_r) * res
                        sub_occupied[off_x:off_x + res, off_y:off_y + res, off_z:off_z + res] = pre_region_blocks[b]
                sub_occupied = process_post(sub_occupied, no_holes, no_gaps, no_small_voids, gap_passes, axis_order, region['thresholds'], void_thresh, occ_thresh, fill_all_voids, sub_order=sub_order, y_corner_passes=0)
                for b in reg_blocks:
                    off_x = (b[0] - min_bx_r) * res
                    off_y = (b[1] - min_by_r) * res
                    off_z = (b[2] - min_bz_r) * res
                    slice_occ = sub_occupied[off_x:off_x + res, off_y:off_y + res, off_z:off_z + res]
                    full_x_start = (b[0] - min_bx) * res
                    full_y_start = (b[1] - min_by) * res
                    full_z_start = (b[2] - min_bz) * res
                    full_occupied[full_x_start:full_x_start + res, full_y_start:full_y_start + res, full_z_start:full_z_start + res] = slice_occ
            block_occupied = {}
            for bx in range(num_bx):
                for by in range(num_by):
                    for bz in range(num_bz):
                        off_x = bx * res
                        off_y = by * res
                        off_z = bz * res
                        occupied_np = full_occupied[off_x:off_x + res, off_y:off_y + res, off_z:off_z + res].copy()
                        if occupied_np.sum() > 0:
                            block_occupied[(bx + min_bx, by + min_by, bz + min_bz)] = occupied_np
        elif step == 'per-block-gaps':
            for b in block_occupied:
                occupied_np = block_occupied[b]
                if not no_gaps:
                    for _ in range(gap_passes):
                        for axis in axis_order:
                            if axis == 0 and 'x' in per_block_gap_axes or axis == 1 and 'y' in per_block_gap_axes or axis == 2 and 'z' in per_block_gap_axes:
                                thresh = thresholds[axis]
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
            block_occupied = {}
            for bx in range(num_bx):
                for by in range(num_by):
                    for bz in range(num_bz):
                        off_x = bx * res
                        off_y = by * res
                        off_z = bz * res
                        occupied_np = full_occupied[off_x:off_x + res, off_y:off_y + res, off_z:off_z + res].copy()
                        if occupied_np.sum() > 0:
                            block_occupied[(bx + min_bx, by + min_by, bz + min_bz)] = occupied_np
        elif step == 'sub-blocks':
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
            for sub in subs:
                bx, by, bz, minx, miny, minz, maxx, maxy, maxz, is_solid = sub
                off_x = (bx - min_bx) * res + minx
                off_y = (by - min_by) * res + miny
                off_z = (bz - min_bz) * res + minz
                end_x = (bx - min_bx) * res + maxx
                end_y = (by - min_by) * res + maxy
                end_z = (bz - min_bz) * res + maxz
                off_x = max(0, off_x)
                off_y = max(0, off_y)
                off_z = max(0, off_z)
                end_x = min(full_occupied.shape[0], end_x)
                end_y = min(full_occupied.shape[1], end_y)
                end_z = min(full_occupied.shape[2], end_z)
                if off_x < end_x and off_y < end_y and off_z < end_z:
                    full_occupied[off_x:end_x, off_y:end_y, off_z:end_z] = is_solid
            block_occupied = {}
            for bx in range(num_bx):
                for by in range(num_by):
                    for bz in range(num_bz):
                        off_x = bx * res
                        off_y = by * res
                        off_z = bz * res
                        occupied_np = full_occupied[off_x:off_x + res, off_y:off_y + res, off_z:off_z + res].copy()
                        if occupied_np.sum() > 0:
                            block_occupied[(bx + min_bx, by + min_by, bz + min_bz)] = occupied_np
    return block_occupied