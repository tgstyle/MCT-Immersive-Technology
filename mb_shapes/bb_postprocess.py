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
                        if max_intrude >= 0 and intrude_count > max_intrude:
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
                        if max_intrude >= 0 and intrude_count > max_intrude:
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
                        if max_intrude >= 0 and intrude_count > max_intrude:
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
                        if max_intrude >= 0 and intrude_count > max_intrude:
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
                        if max_intrude >= 0 and intrude_count > max_intrude:
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
                        if max_intrude >= 0 and intrude_count > max_intrude:
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
def remove_protrusions(np_arr, is_excluded=None):
    shape = np_arr.shape
    directions = [(1,0,0), (-1,0,0), (0,1,0), (0,-1,0), (0,0,1), (0,0,-1)]
    
    # Neighbor counts via convolution
    kernel = np.array([[[0,0,0],[0,1,0],[0,0,0]],
                       [[0,1,0],[1,0,1],[0,1,0]],
                       [[0,0,0],[0,1,0],[0,0,0]]])
    neigh_counts = ndimage.convolve(np_arr.astype(int), kernel, mode='constant', cval=0)
    
    # Candidates: occupied with 1-2 neighbors
    candidates = np.where((np_arr) & (neigh_counts > 0) & (neigh_counts <= 2))
    
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
        
        # All unique axes? (different directions, no same-axis multiples/opposites)
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

# Main post-processing function
def process_post(np_arr, no_holes, no_gaps, no_small_voids, gap_passes, axis_order, thresholds, small_void_threshold, small_occupied_threshold, fill_all_voids, is_excluded=None, max_intrude_dict=None, ex_thresholds=None, sub_order='remove-small,fill-holes,fill-voids,fill-gaps'):
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
    return np_arr