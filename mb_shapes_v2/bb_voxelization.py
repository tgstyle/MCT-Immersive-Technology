import numpy as np
from scipy import ndimage

EPS = 1e-9
HALF = 0.5 - 1e-6


def _tri_box_batch(centers, tri):
    v = tri[None, :, :] - centers[:, None, :]
    keep = np.ones(len(centers), dtype=bool)
    for a in range(3):
        keep &= ~((v[:, :, a].min(axis=1) >= HALF) | (v[:, :, a].max(axis=1) <= -HALF))
    if not keep.any():
        return keep
    e = [tri[1] - tri[0], tri[2] - tri[1], tri[0] - tri[2]]
    for i in range(3):
        for a in range(3):
            b1, b2 = (a + 1) % 3, (a + 2) % 3
            ax = np.zeros(3)
            ax[b1] = -e[i][b2]
            ax[b2] = e[i][b1]
            ln = np.linalg.norm(ax)
            if ln < EPS:
                continue
            p = v @ ax
            r = HALF * (abs(ax[b1]) + abs(ax[b2]))
            keep &= ~((p.min(axis=1) >= r - EPS * ln) | (p.max(axis=1) <= -r + EPS * ln))
            if not keep.any():
                return keep
    n = np.cross(e[0], e[1])
    d = -(v[:, 0, :] @ n)
    r = HALF * np.abs(n).sum()
    keep &= np.abs(d) < r + EPS
    return keep


def _tri2d_box_batch(centers2, tri2):
    v = tri2[None, :, :] - centers2[:, None, :]
    keep = np.ones(len(centers2), dtype=bool)
    for a in range(2):
        keep &= ~((v[:, :, a].min(axis=1) >= HALF) | (v[:, :, a].max(axis=1) <= -HALF))
    if not keep.any():
        return keep
    for i in range(3):
        e = tri2[(i + 1) % 3] - tri2[i]
        ax = np.array([-e[1], e[0]])
        ln = np.hypot(ax[0], ax[1])
        if ln < EPS:
            continue
        p = v @ ax
        r = HALF * (abs(ax[0]) + abs(ax[1]))
        keep &= ~((p.min(axis=1) >= r - EPS * ln) | (p.max(axis=1) <= -r + EPS * ln))
        if not keep.any():
            return keep
    return keep


def rasterize_triangles(tris, lo, dims):
    grid = np.zeros(dims, dtype=bool)
    for tri in tris:
        t = tri - lo
        n = np.cross(t[1] - t[0], t[2] - t[0])
        nl = np.linalg.norm(n)
        if nl < EPS:
            continue
        n = n / nl
        axis_plane = None
        for a in range(3):
            if abs(abs(n[a]) - 1.0) < 1e-9:
                c = t[0][a]
                if abs(c - round(c)) < 1e-9:
                    axis_plane = (a, int(round(c)))
                break
        if axis_plane is not None:
            a, c = axis_plane
            idx = c if n[a] < 0 else c - 1
            if idx < 0 or idx >= dims[a]:
                continue
            b1, b2 = (a + 1) % 3, (a + 2) % 3
            p2 = t[:, [b1, b2]]
            lo2 = np.maximum(np.floor(p2.min(axis=0)).astype(int), 0)
            hi2 = np.minimum(np.ceil(p2.max(axis=0)).astype(int), [dims[b1], dims[b2]])
            if lo2[0] >= hi2[0] or lo2[1] >= hi2[1]:
                continue
            uu, vv = np.meshgrid(np.arange(lo2[0], hi2[0]), np.arange(lo2[1], hi2[1]), indexing='ij')
            uu = uu.ravel()
            vv = vv.ravel()
            centers2 = np.stack([uu + 0.5, vv + 0.5], axis=1)
            hit = _tri2d_box_batch(centers2, p2)
            if hit.any():
                coords = [None, None, None]
                coords[a] = np.full(hit.sum(), idx)
                coords[b1] = uu[hit]
                coords[b2] = vv[hit]
                grid[coords[0], coords[1], coords[2]] = True
        else:
            tmin = np.maximum(np.floor(t.min(axis=0)).astype(int), 0)
            tmax = np.minimum(np.ceil(t.max(axis=0)).astype(int), dims)
            if (tmin >= tmax).any():
                continue
            xx, yy, zz = np.meshgrid(np.arange(tmin[0], tmax[0]), np.arange(tmin[1], tmax[1]), np.arange(tmin[2], tmax[2]), indexing='ij')
            xx = xx.ravel()
            yy = yy.ravel()
            zz = zz.ravel()
            centers = np.stack([xx + 0.5, yy + 0.5, zz + 0.5], axis=1)
            hit = _tri_box_batch(centers, t)
            if hit.any():
                grid[xx[hit], yy[hit], zz[hit]] = True
    return grid


def flood_fill_solid(grid):
    padded = np.pad(grid, 1, constant_values=False)
    air = ~padded
    lbl, _ = ndimage.label(air, structure=ndimage.generate_binary_structure(3, 1))
    interior = air & (lbl != lbl[0, 0, 0])
    return (padded | interior)[1:-1, 1:-1, 1:-1]


def split_blocks(solid, num_blocks, res=16):
    out = {}
    for bx in range(num_blocks[0]):
        for by in range(num_blocks[1]):
            for bz in range(num_blocks[2]):
                sub = solid[bx * res:(bx + 1) * res, by * res:(by + 1) * res, bz * res:(bz + 1) * res]
                if sub.any():
                    out[(bx, by, bz)] = sub.copy()
    return out


def center_in_block(arr, res=16):
    if arr.sum() == 0 or arr.all():
        return arr
    coords = np.nonzero(arr)
    mins = [int(c.min()) for c in coords]
    maxs = [int(c.max()) for c in coords]
    widths = [maxs[d] - mins[d] + 1 for d in range(3)]
    pads = [(res - widths[d]) // 2 for d in range(3)]
    out = np.zeros_like(arr)
    out[tuple(slice(pads[d], pads[d] + widths[d]) for d in range(3))] = arr[tuple(slice(mins[d], maxs[d] + 1) for d in range(3))]
    return out


def fill_combined_voids(block_occupied, res=16):
    if not block_occupied:
        return block_occupied
    min_b = [min(k[d] for k in block_occupied) for d in range(3)]
    max_b = [max(k[d] for k in block_occupied) for d in range(3)]
    num = [max_b[d] - min_b[d] + 1 for d in range(3)]
    full = np.zeros((num[0] * res, num[1] * res, num[2] * res), dtype=bool)
    for (bx, by, bz), arr in block_occupied.items():
        full[(bx - min_b[0]) * res:(bx - min_b[0] + 1) * res, (by - min_b[1]) * res:(by - min_b[1] + 1) * res, (bz - min_b[2]) * res:(bz - min_b[2] + 1) * res] = arr
    full = flood_fill_solid(full)
    out = {}
    for bx in range(num[0]):
        for by in range(num[1]):
            for bz in range(num[2]):
                sub = full[bx * res:(bx + 1) * res, by * res:(by + 1) * res, bz * res:(bz + 1) * res]
                if sub.any():
                    out[(bx + min_b[0], by + min_b[1], bz + min_b[2])] = sub.copy()
    return out
