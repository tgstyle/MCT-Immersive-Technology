# bb_voxelization.py
import torch
import numpy as np
import math
def process_block(args):
    bx, by, bz, minx, miny, minz, verts, triangles, edges, res, x_threshold, y_threshold, z_threshold, is_watertight, has_thin_features, no_postprocess, no_holes, no_gaps, no_small_voids, gap_passes, small_void_threshold, small_occupied_threshold, device, solid_set, directions_t, offsets_t, fill_all_voids, axis_order = args
    if (bx, by, bz) in solid_set:
        occupied_np = np.ones((res, res, res), dtype=bool)
        return (bx, by, bz, occupied_np)
    dtype = torch.float32
    small = 1e-6
    block_min = [minx + bx * 16, miny + by * 16, minz + bz * 16]
    block_max = [block_min[0] + 16, block_min[1] + 16, block_min[2] + 16]
    block_center = [(block_min[d] + block_max[d]) / 2 for d in range(3)]
    model_min = [min(v[d] for v in verts) for d in range(3)]
    model_max = [max(v[d] for v in verts) for d in range(3)]
    if any(model_max[d] < block_min[d] or model_min[d] >= block_max[d] for d in range(3)):
        return None
    points = []
    for v in verts:
        if all(block_min[j] <= v[j] < block_max[j] for j in range(3)): points.append(v)
    has_intersection = len(points) > 0
    for i1, i2 in edges:
        v1 = verts[i1]
        v2 = verts[i2]
        for dim in range(3):
            for side in [block_min[dim], block_max[dim]]:
                denom = v2[dim] - v1[dim]
                if abs(denom) < small: continue
                t = (side - v1[dim]) / denom
                if 0 <= t <= 1:
                    inter = [v1[k] + t * (v2[k] - v1[k]) for k in range(3)]
                    if all(block_min[k] <= inter[k] < block_max[k] for k in range(3) if k != dim):
                        points.append(inter)
                        has_intersection = True
    voxel_size = 16 / res
    delta = voxel_size * 0.49
    if has_thin_features:
        num_off = 27
    else:
        num_off = 7
    offsets_t = offsets_t[:num_off]
    if is_watertight:
        directions_t = directions_t[:3]
        required = 2
        epsilon = small
    else:
        directions_t = directions_t
        required = 3
        epsilon = -small
    num_dir = len(directions_t)
    if has_intersection:
        centers_t = torch.tensor(block_min, dtype=dtype, device=device).view(1,1,1,3)
        i = torch.arange(res, dtype=dtype, device=device)
        ii, jj, kk = torch.meshgrid(i, i, i, indexing='ij')
        centers = centers_t + (torch.stack([ii, jj, kk], dim=-1) + 0.5) * voxel_size
        centers_flat = centers.view(-1, 3)
        num_vox = res ** 3
    else:
        centers_flat = torch.tensor([block_center], dtype=dtype, device=device)
        num_vox = 1
        offsets_t = offsets_t[:1]
    all_points = centers_flat[:, None, :] + offsets_t[None, :, :]
    all_points_flat = all_points.view(-1, 3)
    num_points = len(all_points_flat)
    all_origins = all_points_flat[:, None, :] .repeat(1, num_dir, 1).view(-1, 3)
    all_dirs = directions_t[None, :, :] .repeat(num_points, 1, 1).view(-1, 3)
    num_rays = len(all_origins)
    T = len(triangles)
    if T == 0:
        inside = torch.zeros(num_points, dtype=torch.bool, device=device)
    else:
        tris_t = torch.tensor(triangles, dtype=dtype, device=device)
        v0 = tris_t[:, 0, :]
        v1 = tris_t[:, 1, :]
        v2 = tris_t[:, 2, :]
        edge1 = v1 - v0
        edge2 = v2 - v0
        target_elements = 50000000
        batch_size_r = max(1, int(target_elements // T)) * 2
        batch_size_t = max(1, int(target_elements // num_rays)) * 2
        counts = torch.zeros(num_rays, dtype=torch.int32, device=device)
        with torch.no_grad():
            full_try_threshold = 20000000
            if T * num_rays < full_try_threshold:
                try:
                    v0_b = v0[None, :, :]
                    edge1_b = edge1[None, :, :]
                    edge2_b = edge2[None, :, :]
                    o_batch = all_origins[:, None, :]
                    d_batch = all_dirs[:, None, :]
                    h = torch.stack([
                        d_batch[..., 1] * edge2_b[..., 2] - d_batch[..., 2] * edge2_b[..., 1],
                        d_batch[..., 2] * edge2_b[..., 0] - d_batch[..., 0] * edge2_b[..., 2],
                        d_batch[..., 0] * edge2_b[..., 1] - d_batch[..., 1] * edge2_b[..., 0]
                    ], dim=-1)
                    a = torch.einsum('btj,btj->bt', edge1_b, h)
                    mask = torch.abs(a) >= small
                    f = torch.zeros_like(a)
                    f[mask] = 1.0 / a[mask]
                    s = o_batch - v0_b
                    u = f * torch.einsum('btj,btj->bt', s, h)
                    mask &= (u >= 0.0) & (u <= 1.0)
                    q = torch.stack([
                        s[..., 1] * edge1_b[..., 2] - s[..., 2] * edge1_b[..., 1],
                        s[..., 2] * edge1_b[..., 0] - s[..., 0] * edge1_b[..., 2],
                        s[..., 0] * edge1_b[..., 1] - s[..., 1] * edge1_b[..., 0]
                    ], dim=-1)
                    v = f * torch.einsum('btj,btj->bt', d_batch, q)
                    mask &= (v >= 0.0) & (u + v <= 1.0)
                    t = f * torch.einsum('btj,btj->bt', edge2_b, q)
                    mask &= (t > epsilon)
                    intersects = mask
                    counts = intersects.sum(dim=1, dtype=torch.int32)
                    del o_batch, d_batch, h, a, mask, f, s, u, q, v, t, intersects, v0_b, edge1_b, edge2_b
                except RuntimeError as e:
                    if 'out of memory' in str(e) or 'parameter is incorrect' in str(e):
                        if device.type == 'cuda':
                            torch.cuda.empty_cache()
                    else:
                        raise e
                else:
                    del v0, v1, v2, edge1, edge2, tris_t
                    odd = (counts % 2) == 1
                    odd_per_dir = odd.view(num_points, num_dir)
                    inside_dirs = odd_per_dir.sum(dim=1, dtype=torch.int32)
                    inside = inside_dirs >= required
                    del counts, odd, odd_per_dir, inside_dirs
                    inside_per_vox = inside.view(num_vox, len(offsets_t)).any(dim=1)
                    del inside, all_points, all_points_flat, all_origins, all_dirs, centers_flat
                    if has_intersection:
                        occupied_np = inside_per_vox.view(res, res, res).cpu().numpy().astype(bool)
                    else:
                        occupied_np = inside_per_vox.view(1, 1, 1).cpu().numpy().astype(bool)
                    del inside_per_vox
                    if not has_intersection:
                        if occupied_np[0, 0, 0]:
                            occupied_np = np.ones((res, res, res), dtype=bool)
                            return (bx, by, bz, occupied_np)
                        return None
                    return (bx, by, bz, occupied_np)
            for start_t in range(0, T, batch_size_t):
                end_t = min(start_t + batch_size_t, T)
                current_batch_t = end_t - start_t
                v0_slice = v0[start_t:end_t]
                edge1_slice = edge1[start_t:end_t]
                edge2_slice = edge2[start_t:end_t]
                for start_r in range(0, num_rays, batch_size_r):
                    end_r = min(start_r + batch_size_r, num_rays)
                    current_batch_r = end_r - start_r
                    success = False
                    while not success and current_batch_r > 0 and current_batch_t > 0:
                        try:
                            v0_b = v0_slice[None, :, :]
                            edge1_b = edge1_slice[None, :, :]
                            edge2_b = edge2_slice[None, :, :]
                            o_batch = all_origins[start_r:start_r+current_batch_r, None, :]
                            d_batch = all_dirs[start_r:start_r+current_batch_r, None, :]
                            h = torch.stack([
                                d_batch[..., 1] * edge2_b[..., 2] - d_batch[..., 2] * edge2_b[..., 1],
                                d_batch[..., 2] * edge2_b[..., 0] - d_batch[..., 0] * edge2_b[..., 2],
                                d_batch[..., 0] * edge2_b[..., 1] - d_batch[..., 1] * edge2_b[..., 0]
                            ], dim=-1)
                            a = torch.einsum('btj,btj->bt', edge1_b, h)
                            mask = torch.abs(a) >= small
                            f = torch.zeros_like(a)
                            f[mask] = 1.0 / a[mask]
                            s = o_batch - v0_b
                            u = f * torch.einsum('btj,btj->bt', s, h)
                            mask &= (u >= 0.0) & (u <= 1.0)
                            q = torch.stack([
                                s[..., 1] * edge1_b[..., 2] - s[..., 2] * edge1_b[..., 1],
                                s[..., 2] * edge1_b[..., 0] - s[..., 0] * edge1_b[..., 2],
                                s[..., 0] * edge1_b[..., 1] - s[..., 1] * edge1_b[..., 0]
                            ], dim=-1)
                            v = f * torch.einsum('btj,btj->bt', d_batch, q)
                            mask &= (v >= 0.0) & (u + v <= 1.0)
                            t = f * torch.einsum('btj,btj->bt', edge2_b, q)
                            mask &= (t > epsilon)
                            intersects = mask
                            counts[start_r:start_r+current_batch_r] += intersects.sum(dim=1, dtype=torch.int32)
                            del o_batch, d_batch, h, a, mask, f, s, u, q, v, t, intersects, v0_b, edge1_b, edge2_b
                            success = True
                        except RuntimeError as e:
                            error_str = str(e).lower()
                            if 'out of memory' in error_str or 'parameter is incorrect' in error_str:
                                current_batch_r //= 2
                                current_batch_t //= 2
                                batch_size_r //= 2
                                batch_size_t //= 2
                                if device.type == 'cuda':
                                    torch.cuda.empty_cache()
                            else:
                                raise e
                    if not success:
                        raise RuntimeError("Failed to process batch due to persistent error")
                del v0_slice, edge1_slice, edge2_slice
            del v0, v1, v2, edge1, edge2, tris_t
        odd = (counts % 2) == 1
        odd_per_dir = odd.view(num_points, num_dir)
        inside_dirs = odd_per_dir.sum(dim=1, dtype=torch.int32)
        inside = inside_dirs >= required
        del counts, odd, odd_per_dir, inside_dirs
    inside_per_vox = inside.view(num_vox, len(offsets_t)).any(dim=1)
    del inside, all_points, all_points_flat, all_origins, all_dirs, centers_flat
    if has_intersection:
        occupied_np = inside_per_vox.view(res, res, res).cpu().numpy().astype(bool)
    else:
        occupied_np = inside_per_vox.view(1, 1, 1).cpu().numpy().astype(bool)
    del inside_per_vox
    if not has_intersection:
        if occupied_np[0, 0, 0]:
            occupied_np = np.ones((res, res, res), dtype=bool)
            return (bx, by, bz, occupied_np)
        return None
    return (bx, by, bz, occupied_np)