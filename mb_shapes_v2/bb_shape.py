import argparse
import json
import os
import time
import numpy as np

from bb_model_parser import parse_bbmodel, list_bbmodel_files, select_file, normalize_offsets, log
from bb_postprocess import apply_postprocessing
from bb_utils import parse_thresh_val, parse_thresh_triple, extract_aabbs_from_occupied
from bb_voxelization import fill_combined_voids


def parse_axis_order(s):
    mapping = {'x': 0, 'y': 1, 'z': 2}
    order = []
    for a in s.lower().split(','):
        if a not in mapping:
            raise ValueError(f"Invalid axis '{a}' in --fill-order")
        order.append(mapping[a])
    if len(order) != 3 or set(order) != {0, 1, 2}:
        raise ValueError("--fill-order must specify unique x,y,z")
    return order


def parse_block_set(s):
    out = set()
    if s:
        for part in s.split():
            bx, by, bz = map(int, part.split(','))
            out.add((bx, by, bz))
    return out


def main():
    start_time = time.time()
    parser = argparse.ArgumentParser()

    parser.add_argument('path', nargs='?', help='Path to bbmodel file or directory containing bbmodel files')
    parser.add_argument('--main', type=str, help='Specify the main model file (bypasses selection prompt)')
    parser.add_argument('--output', choices=['java', 'json'], help='Output format: java or json (default: both)')

    parser.add_argument('--no-postprocess', action='store_true', help='Disable all post-processing steps')
    parser.add_argument('--no-supplementary', action='store_true', help='Disable processing of supplementary models')
    parser.add_argument('--no-fill', action='store_true', help='Disable interior flood fill (shell only)')
    parser.add_argument('--fill-combined', action='store_true', help='Re-run interior fill after supplementary models are merged (fills cavities sealed only by the combination)')

    parser.add_argument('--thresh', type=str, default='0,0,0', help='Gap-fill thresholds x,y,z. 0 or x = off (default), N = fill gaps of up to N voxels, u = unlimited')
    parser.add_argument('--ex-thresh', type=str, default='d,d,d', help='Gap thresholds inside excluded blocks; d = use --thresh value')
    parser.add_argument('--mi', type=str, default='3,4,4', help='Max gap-fill intrusion into excluded blocks along x,y,z; x = none')
    parser.add_argument('--gap-passes', type=int, default=3, help='Number of gap-fill passes per axis')
    parser.add_argument('--fill-order', type=str, default='x,z,y', help='Axis order for gap filling')
    parser.add_argument('--pbg', type=str, default='', help='Axes for the per-block-gaps step (e.g. "x,z")')
    parser.add_argument('--rpp', action='append', default=[], help='Regional gap fill: "bx,by,bz bx,by,bz ... : x,y,z"')
    parser.add_argument('--exclude-global', type=str, default='', help='Blocks excluded from global gap filling')

    parser.add_argument('--solid-blocks', type=str, default='', help='Blocks forced solid (e.g. "0,0,0 1,0,0")')
    parser.add_argument('--empty-blocks', type=str, default='', help='Blocks forced empty')
    parser.add_argument('--sub-solid-block', action='append', default=[], help='Force sub-region solid: "bx,by,bz minX,minY,minZ,maxX,maxY,maxZ"')
    parser.add_argument('--sub-empty-block', action='append', default=[], help='Force sub-region empty: "bx,by,bz minX,minY,minZ,maxX,maxY,maxZ"')
    parser.add_argument('--copy-aabb', action='append', default=[], help='Copy AABBs: "from_bx,from_by,from_bz to_bx,to_by,to_bz"')

    parser.add_argument('--protrusions', nargs='?', const='1,1,1', default=None, help='Remove protruding clusters up to this size, e.g. "2,2,2" (bare flag = "1,1,1")')
    parser.add_argument('--squarify', type=str, default='', help='Box out ragged shapes. Tokens: "bx,by,bz" boxes every cluster in the block; "bx,by,bz minX,minY,minZ,maxX,maxY,maxZ" boxes only clusters inside that sub-region (e.g. "0,1,5 8,0,0,16,16,16 2,1,5")')
    parser.add_argument('--report-shapes', action='store_true', help='Print per-block AABB counts after extraction to help locate ragged blocks worth squarifying')
    parser.add_argument('--fill-y-corners', action='store_true', help='Fill 1x1x1 outside corner indents to straighten vertical edges')
    parser.add_argument('--y-corners-passes', type=int, default=1, help='Passes for fill-y-corners')
    parser.add_argument('--pp-order', type=str, default='per-block,regional,global,per-block-gaps,protrusions,squarify,sub-blocks', help='Order of post-processing steps')

    parser.add_argument('--supp-config', nargs='+', action='append', default=[], help='Supplementary model config: model.bbmodel num_times off1 off2 ... [thresh=x,y,z] [passes=N]')

    parser.add_argument('--low-res-merge', action='store_true', help='Enable low-res merge for angled faces')
    parser.add_argument('--mc-version', type=str, default='default', choices=['1.12.2', 'default'], help='Minecraft version for output adjustment')
    parser.add_argument('--debug-log', action='store_true', help='Enable debug logging to log.txt')

    parser.add_argument('--auto-center', action='store_true', help='Pad X/Z overhang symmetrically (main model only)')
    parser.add_argument('--force-grid-dims', type=str, default='', help='Force output grid to at least this size, e.g. "5,5,12"')
    parser.add_argument('--target-grid', type=str, default='', help='Fit the MAIN model into an explicit block grid, e.g. "5,5,11" (blank axis = auto)')
    parser.add_argument('--grid-anchor', type=str, default='min,min,min', help='Per-axis anchor for --target-grid: min, max, center')
    parser.add_argument('--clamp-slack', type=float, default=1.0, help='Units of geometry allowed to overhang past the target grid')

    args = parser.parse_args()
    debug = args.debug_log

    target_grid = None
    if args.target_grid:
        target_grid = tuple(int(p) if p.strip() else None for p in args.target_grid.split(','))
    grid_anchor = tuple((p.strip().lower() or 'min') for p in args.grid_anchor.split(','))
    solid_set = parse_block_set(args.solid_blocks)
    empty_set = parse_block_set(args.empty_blocks)
    exclude_set = parse_block_set(args.exclude_global)
    squarify_set = []
    if args.squarify:
        pending_block = None
        for token in args.squarify.split():
            parts = token.split(',')
            if len(parts) == 3:
                if pending_block is not None:
                    squarify_set.append((pending_block, None))
                pending_block = tuple(map(int, parts))
            elif len(parts) == 6:
                if pending_block is None:
                    raise ValueError(f'--squarify region "{token}" must follow a bx,by,bz block token')
                squarify_set.append((pending_block, tuple(map(int, parts))))
                pending_block = None
            else:
                raise ValueError(f'Invalid --squarify token: {token}')
        if pending_block is not None:
            squarify_set.append((pending_block, None))

    subs = []
    for flag, is_solid in ((args.sub_solid_block, True), (args.sub_empty_block, False)):
        for s in flag:
            parts = s.split()
            if len(parts) != 2:
                raise ValueError(f"Invalid sub-block format: {s}")
            bx, by, bz = map(int, parts[0].split(','))
            minx, miny, minz, maxx, maxy, maxz = map(int, parts[1].split(','))
            subs.append((bx, by, bz, minx, miny, minz, maxx, maxy, maxz, is_solid))

    axis_order = parse_axis_order(args.fill_order)
    thresholds = parse_thresh_triple(args.thresh)
    ex_thresholds = parse_thresh_triple(args.ex_thresh, defaults=thresholds)
    mi_parts = args.mi.split(',')
    max_intrude_dict = {i: parse_thresh_val(mi_parts[i], (3, 4, 4)[i]) for i in range(3)}
    per_block_gap_axes = set(args.pbg.lower().split(',')) if args.pbg else set()

    regions = []
    all_region_blocks = set()
    for rpp_str in args.rpp:
        parts = rpp_str.split(':')
        block_strs = parts[0].strip().split()
        thresh_str = parts[1].strip() if len(parts) > 1 else 'd,d,d'
        blocks = set(tuple(map(int, b.split(','))) for b in block_strs)
        reg_thresh = parse_thresh_triple(thresh_str, defaults=thresholds)
        for b in blocks:
            if b in all_region_blocks:
                raise ValueError(f"Overlapping block {b} in regions")
            all_region_blocks.add(b)
        regions.append({'blocks': blocks, 'thresholds': reg_thresh})

    main_path = None
    directory = '.'
    if args.path:
        if os.path.isfile(args.path) and args.path.lower().endswith('.bbmodel'):
            main_path = args.path
            directory = os.path.dirname(main_path) or '.'
        elif os.path.isdir(args.path):
            directory = args.path
    if args.main:
        main_path = os.path.join(directory, args.main)
        if not os.path.exists(main_path):
            raise ValueError(f"Main model {args.main} not found in {directory}")
    bbmodel_files = list_bbmodel_files(directory)
    if main_path:
        main_file = os.path.basename(main_path)
        if main_file in bbmodel_files:
            bbmodel_files.remove(main_file)
    else:
        main_file = select_file(bbmodel_files, "Select the main model by number: ")
        main_path = os.path.join(directory, main_file)
        bbmodel_files.remove(main_file)

    supp_list = []
    if not args.no_supplementary and bbmodel_files:
        if args.supp_config:
            for config_parts in args.supp_config:
                if len(config_parts) == 1:
                    config_parts = config_parts[0].split()
                model = config_parts[0]
                num_times = int(config_parts[1])
                offsets = []
                for off_str in config_parts[2:2 + num_times]:
                    bx, by, bz = map(int, off_str.split(','))
                    offsets.append((bx, by, bz))
                supp_thresh = (-1, -1, -1)
                supp_passes = args.gap_passes
                for token in config_parts[2 + num_times:]:
                    if token.startswith('thresh='):
                        supp_thresh = parse_thresh_triple(token[len('thresh='):])
                    elif token.startswith('passes='):
                        supp_passes = int(token[len('passes='):])
                    else:
                        raise ValueError(f"Unknown supp-config token: {token}")
                supp_list.append((model, offsets, supp_thresh, supp_passes))
        else:
            print("Additional BBModel files detected:")
            for i, file in enumerate(bbmodel_files, 1):
                print(f"{i}. {file}")
            selected_supps = set()
            while True:
                remaining = len(bbmodel_files) - len(selected_supps)
                if remaining == 0:
                    break
                add_supp = input("Do you want to add a supplementary model? (y/n): ").strip().lower()
                if add_supp != 'y':
                    break
                available = [f for f in bbmodel_files if f not in selected_supps]
                if not available:
                    print("No more supplementary models available.")
                    break
                supp_file = select_file(available, "Select a supplementary model by number: ") if remaining > 1 else available[0]
                selected_supps.add(supp_file)
                num_times = int(input("How many times to add this supplementary? "))
                offsets = []
                for _ in range(num_times):
                    offset_str = input("Enter offset (bx,by,bz): ")
                    bx, by, bz = map(int, offset_str.split(','))
                    offsets.append((bx, by, bz))
                thresh_str = input("Gap-fill thresholds for this model (x,y,z; blank for none): ").strip()
                supp_thresh = parse_thresh_triple(thresh_str) if thresh_str else (-1, -1, -1)
                supp_list.append((supp_file, offsets, supp_thresh, args.gap_passes))

    print("Processing main model...")
    overall_dict = parse_bbmodel(main_path, gap_thresholds=(-1, -1, -1), gap_passes=args.gap_passes, axis_order=axis_order, do_center=False, auto_center=args.auto_center, target_grid=target_grid, grid_anchor=grid_anchor, clamp_slack=args.clamp_slack, no_fill=args.no_fill, debug=debug)
    if debug:
        log(f"Initial overall_dict keys: {sorted(overall_dict.keys())}")

    cache = {}
    for s_file, _, supp_thresh, supp_passes in supp_list:
        key = (s_file, supp_thresh, supp_passes)
        if key in cache:
            continue
        print(f"Processing supplementary model: {s_file}")
        s_path = os.path.join(directory, s_file)
        cache[key] = parse_bbmodel(s_path, gap_thresholds=supp_thresh, gap_passes=supp_passes, axis_order=axis_order, do_center=True, no_fill=args.no_fill, debug=debug)

    placements = []
    for s_file, offsets, supp_thresh, supp_passes in supp_list:
        s_blocks = cache[(s_file, supp_thresh, supp_passes)]
        for off in offsets:
            placements.append((s_blocks, off[0], off[1], off[2]))
    placements.sort(key=lambda p: p[3])
    for s_blocks, off_bx, off_by, off_bz in placements:
        current_min_bz = min(k[2] for k in overall_dict) if overall_dict else 0
        if off_bz < current_min_bz:
            shift = current_min_bz - off_bz
            overall_dict = {(bx, by, bz + shift): a for (bx, by, bz), a in overall_dict.items()}
        elif off_bz == current_min_bz:
            overall_dict = {(bx, by, bz + 1): a for (bx, by, bz), a in overall_dict.items()}
        for (sx, sy, sz), arr in s_blocks.items():
            key = (off_bx + sx, off_by + sy, off_bz + sz)
            if key in overall_dict:
                overall_dict[key] = overall_dict[key] | arr
            else:
                overall_dict[key] = arr.copy()
        if debug:
            log(f"After placement at ({off_bx},{off_by},{off_bz}): {sorted(overall_dict.keys())}")

    res = 16
    for b in solid_set:
        overall_dict[b] = np.ones((res, res, res), dtype=bool)
    for b in empty_set:
        overall_dict[b] = np.zeros((res, res, res), dtype=bool)
    if args.fill_combined:
        overall_dict = fill_combined_voids(overall_dict)

    block_occupied = overall_dict
    if not args.no_postprocess:
        pp_order_list = [s.strip() for s in args.pp_order.split(',')]
        y_corner_passes = args.y_corners_passes if args.fill_y_corners else 0
        protrusion_range = tuple(int(p) for p in args.protrusions.split(',')) if args.protrusions else None
        if protrusion_range and (len(protrusion_range) != 3 or any(p < 1 for p in protrusion_range)):
            raise ValueError('--protrusions must be three values >= 1, e.g. "2,2,2"')
        block_occupied = apply_postprocessing(block_occupied, args.gap_passes, axis_order, thresholds, regions, exclude_set, ex_thresholds, max_intrude_dict, per_block_gap_axes, pp_order_list, subs, do_global=True, res=res, y_corner_passes=y_corner_passes, protrusion_range=protrusion_range, squarify_set=squarify_set)
    if debug:
        log(f"Post-postprocess keys: {sorted(block_occupied.keys())}")

    if args.mc_version == '1.12.2':
        max_bz = max(k[2] for k in block_occupied) if block_occupied else 0
        block_occupied = {(bx, by, max_bz - bz): arr for (bx, by, bz), arr in block_occupied.items()}

    overall_aabbs = []
    for (bx, by, bz), occupied_np in sorted(block_occupied.items()):
        if args.low_res_merge:
            low_res = 8
            occupied_low = np.zeros((low_res, low_res, low_res), dtype=bool)
            for i in range(low_res):
                for j in range(low_res):
                    for k in range(low_res):
                        if occupied_np[2 * i:2 * (i + 1), 2 * j:2 * (j + 1), 2 * k:2 * (k + 1)].any():
                            occupied_low[i, j, k] = True
            block_aabbs = extract_aabbs_from_occupied(occupied_low, res=low_res, force_merge=True)
        else:
            block_aabbs = extract_aabbs_from_occupied(occupied_np, res=res)
        if block_aabbs:
            overall_aabbs.append((bx, by, bz, block_aabbs))
    if args.report_shapes:
        print("Per-block AABB counts (high counts = ragged/angled shapes, squarify candidates):")
        for bx, by, bz, ba in sorted(overall_aabbs, key=lambda x: -len(x[3])):
            tag = ' FULL' if len(ba) == 1 and ba[0] == (0.0, 0.0, 0.0, 1.0, 1.0, 1.0) else ''
            print(f"  {bx},{by},{bz}: {len(ba)}{tag}")

    for c in args.copy_aabb:
        parts = c.split()
        if len(parts) != 2:
            raise ValueError(f"Invalid --copy-aabb format: {c}")
        from_b = tuple(map(int, parts[0].split(',')))
        to_b = tuple(map(int, parts[1].split(',')))
        source_aabbs = None
        for bx, by, bz, block_aabbs in overall_aabbs:
            if (bx, by, bz) == from_b:
                source_aabbs = block_aabbs
                break
        if source_aabbs is None:
            raise ValueError(f"Source block {from_b} not found")
        found = False
        for i, (bx, by, bz, _) in enumerate(overall_aabbs):
            if (bx, by, bz) == to_b:
                overall_aabbs[i] = (bx, by, bz, source_aabbs[:])
                found = True
                break
        if not found:
            overall_aabbs.append((to_b[0], to_b[1], to_b[2], source_aabbs[:]))

    overall_aabbs = normalize_offsets(overall_aabbs, debug=debug)

    base_name = os.path.splitext(main_file)[0]
    version_suffix = '_1.12.2' if args.mc_version == '1.12.2' else ''
    do_java = args.output != 'json'
    do_json = args.output != 'java'
    if do_java:
        out_file = f"{base_name}{version_suffix}_java.txt"
        with open(out_file, 'w') as f:
            for bx, by, bz, block_aabbs in sorted(overall_aabbs, key=lambda x: (x[0], x[1], x[2])):
                f.write(f'if (bX == {bx} && bY == {by} && bZ == {bz}) {{\n')
                for minx, miny, minz, maxx, maxy, maxz in sorted(block_aabbs, key=lambda x: (x[0], x[1], x[2])):
                    f.write(f' main.add(new AABB({minx:.4f}D, {miny:.4f}D, {minz:.4f}D, {maxx:.4f}D, {maxy:.4f}D, {maxz:.4f}D));\n')
                f.write('}\n')
        print(f"JAVA output written to {out_file}")
    if do_json:
        if not overall_aabbs:
            print("No AABBs to output.")
            return
        width = max(bx for bx, _, _, _ in overall_aabbs) + 1
        height = max(by for _, by, _, _ in overall_aabbs) + 1
        length = max(bz for _, _, bz, _ in overall_aabbs) + 1
        if args.force_grid_dims:
            force_w, force_h, force_l = map(int, args.force_grid_dims.split(','))
            width = max(width, force_w)
            height = max(height, force_h)
            length = max(length, force_l)
        aabb_json = [None] * (height * length * width)
        for bx, by, bz, block_aabbs in overall_aabbs:
            index = by * (width * length) + bz * width + bx
            if not block_aabbs:
                continue
            if len(block_aabbs) == 1:
                minx, miny, minz, maxx, maxy, maxz = block_aabbs[0]
                if abs(minx) < 1e-5 and abs(miny) < 1e-5 and abs(minz) < 1e-5 and abs(maxx - 1) < 1e-5 and abs(maxy - 1) < 1e-5 and abs(maxz - 1) < 1e-5:
                    aabb_json[index] = []
                    continue
            aabb_json[index] = [list(a) for a in sorted(block_aabbs, key=lambda x: (x[0], x[1], x[2]))]
        out_file = f"{base_name}{version_suffix}_json.txt"
        with open(out_file, 'w') as f:
            json.dump({"shapeAABB": aabb_json}, f, indent=None, separators=(',', ':'))
        print(f"JSON output written to {out_file}")
    print(f"Duration: {time.time() - start_time:.2f} seconds")


if __name__ == "__main__":
    main()
