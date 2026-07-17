import json
import math
import os
import time
import numpy as np

from bb_voxelization import rasterize_triangles, flood_fill_solid, split_blocks, center_in_block
from bb_postprocess import fill_gaps


def log(message):
    with open('log.txt', 'a', encoding='utf-8') as f:
        print(message, file=f, flush=True)


def load_triangles(file_path, debug=False):
    with open(file_path, 'r', encoding='utf-8') as f:
        data = json.load(f)
    tris = []
    grid_size = 1.0 / 16.0
    misaligned = 0
    for el in data.get('elements', []):
        el_type = el.get('type', 'cube')
        if el_type == 'mesh':
            verts = el.get('vertices', {})
            origin = el.get('origin', [0, 0, 0])
            world = {}
            for k, v in verts.items():
                p = [v[i] + origin[i] for i in range(3)]
                for c in p:
                    if abs(c / grid_size - round(c / grid_size)) > 1e-6:
                        misaligned += 1
                world[k] = p
            for f in el.get('faces', {}).values():
                ks = f.get('vertices', [])
                if len(ks) < 3:
                    continue
                pts = [world[k] for k in ks]
                for i in range(1, len(pts) - 1):
                    tris.append([pts[0], pts[i], pts[i + 1]])
        elif el_type == 'cube' or 'from' in el:
            fr = el.get('from')
            to = el.get('to')
            if fr is None or to is None:
                continue
            if el.get('rotation') and any(abs(r) > 1e-9 for r in el['rotation']):
                raise ValueError(f"Rotated cube elements are not supported: {file_path}")
            x0, y0, z0 = fr
            x1, y1, z1 = to
            c = [[x0, y0, z0], [x1, y0, z0], [x0, y1, z0], [x1, y1, z0], [x0, y0, z1], [x1, y0, z1], [x0, y1, z1], [x1, y1, z1]]
            quads = [(0, 2, 3, 1, (0, 0, -1)), (4, 5, 7, 6, (0, 0, 1)), (0, 4, 6, 2, (-1, 0, 0)), (1, 3, 7, 5, (1, 0, 0)), (0, 1, 5, 4, (0, -1, 0)), (2, 6, 7, 3, (0, 1, 0))]
            for a, b, cc, d, _ in quads:
                tris.append([c[a], c[b], c[cc]])
                tris.append([c[a], c[cc], c[d]])
    if not tris:
        raise ValueError(f"No faces found in {file_path}")
    if debug:
        log(f"{os.path.basename(file_path)}: {len(tris)} triangles, misaligned coords: {misaligned}")
    return np.array(tris, dtype=np.float64)


def fit_bounds(tris, target_grid=None, grid_anchor=('min', 'min', 'min'), clamp_slack=1.0, auto_center=False):
    flat = tris.reshape(-1, 3)
    lo = flat.min(axis=0).copy()
    hi = flat.max(axis=0).copy()
    if target_grid:
        for axis in range(3):
            tgt = target_grid[axis] if axis < len(target_grid) else None
            if tgt is None:
                continue
            anchor = grid_anchor[axis] if axis < len(grid_anchor) else 'min'
            span = hi[axis] - lo[axis]
            total = tgt * 16
            if span > total:
                excess = span - total
                if excess > clamp_slack + 1e-9:
                    raise ValueError(f"Axis {axis}: model span {span} exceeds target {total} by {excess} units (> clamp slack {clamp_slack})")
                if anchor == 'max':
                    lo[axis] = hi[axis] - total
                elif anchor == 'center':
                    lo[axis] += excess / 2
                    hi[axis] = lo[axis] + total
                else:
                    hi[axis] = lo[axis] + total
            elif span < total:
                pad = total - span
                if anchor == 'max':
                    lo[axis] -= pad
                elif anchor == 'center':
                    lo[axis] -= pad / 2
                    hi[axis] += pad / 2
                else:
                    hi[axis] += pad
    elif auto_center:
        for axis in (0, 2):
            span = hi[axis] - lo[axis]
            blocks = math.ceil(span / 16)
            overhang = (blocks * 16 - span) / 2
            lo[axis] -= overhang
            hi[axis] += overhang
    return lo, hi


def parse_bbmodel(file_path, gap_thresholds=(-1, -1, -1), gap_passes=3, axis_order=(0, 2, 1), do_center=False, auto_center=False, target_grid=None, grid_anchor=('min', 'min', 'min'), clamp_slack=1.0, no_fill=False, debug=False):
    t0 = time.time()
    tris = load_triangles(file_path, debug=debug)
    lo, hi = fit_bounds(tris, target_grid=target_grid, grid_anchor=grid_anchor, clamp_slack=clamp_slack, auto_center=auto_center)
    num_blocks = [math.ceil((hi[a] - lo[a]) / 16 - 1e-9) for a in range(3)]
    dims = [num_blocks[a] * 16 for a in range(3)]
    grid = rasterize_triangles(tris, lo, dims)
    shell = int(grid.sum())
    solid = grid if no_fill else flood_fill_solid(grid)
    if debug:
        log(f"{os.path.basename(file_path)}: grid {num_blocks}, shell {shell}, filled {int(solid.sum())}, {time.time() - t0:.2f}s")
    blocks = split_blocks(solid, num_blocks)
    if any(t >= 0 for t in gap_thresholds):
        for b in list(blocks):
            blocks[b] = fill_gaps(blocks[b], gap_passes, axis_order, gap_thresholds)
    if do_center:
        for b in list(blocks):
            blocks[b] = center_in_block(blocks[b])
    print(f"{os.path.basename(file_path)}: {num_blocks[0]}x{num_blocks[1]}x{num_blocks[2]} blocks, {int(solid.sum())} voxels, {time.time() - t0:.2f}s")
    return blocks


def list_bbmodel_files(directory):
    return [f for f in os.listdir(directory) if f.lower().endswith('.bbmodel')]


def select_file(bbmodel_files, prompt="Select a file by number: "):
    if not bbmodel_files:
        raise ValueError("No BBModel files found in the directory.")
    print("Available BBModel files:")
    for i, file in enumerate(bbmodel_files, 1):
        print(f"{i}. {file}")
    while True:
        try:
            choice = int(input(prompt))
            if 1 <= choice <= len(bbmodel_files):
                return bbmodel_files[choice - 1]
            print("Invalid selection. Try again.")
        except ValueError:
            print("Please enter a number.")


def normalize_offsets(aabbs, debug=False):
    if not aabbs:
        return aabbs
    min_bx = min(bx for bx, _, _, _ in aabbs)
    min_by = min(by for _, by, _, _ in aabbs)
    min_bz = min(bz for _, _, bz, _ in aabbs)
    if debug:
        log(f"Normalization mins: bx={min_bx}, by={min_by}, bz={min_bz}")
    return [(bx - min_bx, by - min_by, bz - min_bz, block_aabbs) for bx, by, bz, block_aabbs in aabbs]
