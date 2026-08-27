import argparse
import nbt.nbt as nbt
from nbt.nbt import TAG_List, TAG_Int, TAG_Compound

def read_nbt_file(file_path):
    try:
        nbt_file = nbt.NBTFile(file_path)
        return nbt_file
    except FileNotFoundError:
        print(f"Error: File '{file_path}' not found")
        exit(1)
    except Exception as e:
        print(f"Error reading NBT file: {e}")
        exit(1)

def print_structure_info(nbt_file):
    print("\n=== Structure NBT Info ===")
    if 'size' in nbt_file:
        size = nbt_file['size']
        print(f"Size: X={size[0]}, Y={size[1]}, Z={size[2]}")
    if 'blocks' in nbt_file:
        print(f"Block count: {len(nbt_file['blocks'])}")
    if 'palette' in nbt_file:
        print(f"Palette size: {len(nbt_file['palette'])}")
    print()

# Minecraft Direction.ordinal(): DOWN UP NORTH SOUTH WEST EAST.
# IE stores pipe connections as a bitmask of 1 << ordinal, and sideConfig as an
# array indexed by the same ordinal. Verified against IE's own refinery template,
# where a straight west-east run reads 48 in the middle and 16 / 32 at the ends.
DIRECTION_BITS = {'down': 0, 'up': 1, 'north': 2, 'south': 3, 'west': 4, 'east': 5}

# clockwise seen from above
CW = {'north': 'east', 'east': 'south', 'south': 'west', 'west': 'north'}


def rotation_map(degrees):
    steps = (degrees // 90) % 4
    out = {d: d for d in ('down', 'up')}
    for d in ('north', 'east', 'south', 'west'):
        cur = d
        for _ in range(steps):
            cur = CW[cur]
        out[d] = cur
    return out


def rotate_structure_blocks(nbt_file, degrees):
    steps = (degrees // 90) % 4
    if steps == 0 or 'blocks' not in nbt_file:
        return 0
    size = nbt_file['size']
    w, h, l = size[0].value, size[1].value, size[2].value
    for block in nbt_file['blocks']:
        pos = block['pos']
        x, y, z = pos[0].value, pos[1].value, pos[2].value
        for _ in range(steps):
            x, z = l - 1 - z, x
            w, l = l, w
        w, l = size[0].value, size[2].value
        for _ in range(steps):
            w, l = l, w
        pos[0].value, pos[1].value, pos[2].value = x, y, z
    if steps % 2 == 1:
        size[0].value, size[2].value = l, w
    return len(nbt_file['blocks'])


def rotate_palette_properties(nbt_file, degrees):
    rmap = rotation_map(degrees)
    if 'palette' not in nbt_file:
        return 0
    changed_count = 0
    for entry in nbt_file['palette']:
        if 'Properties' not in entry:
            continue
        props = entry['Properties']
        changed = False
        if 'facing' in props and props['facing'].value in rmap:
            props['facing'].value = rmap[props['facing'].value]
            changed = True
        if 'axis' in props and (degrees // 90) % 2 == 1 and props['axis'].value in ('x', 'z'):
            props['axis'].value = 'z' if props['axis'].value == 'x' else 'x'
            changed = True
        sides = [d for d in ('north', 'east', 'south', 'west') if d in props]
        if len(sides) == 4:
            vals = {d: props[d].value for d in sides}
            for d in sides:
                props[rmap[d]].value = vals[d]
            changed = True
        if changed:
            changed_count += 1
    return changed_count


def rotate_block_nbt_directions(nbt_file, degrees):
    rmap = rotation_map(degrees)
    if 'blocks' not in nbt_file:
        return 0
    updated = 0
    for block in nbt_file['blocks']:
        if 'nbt' not in block:
            continue
        block_nbt = block['nbt']
        changed = False
        if 'connections' in block_nbt:
            old_val = block_nbt['connections'].value
            new_val = 0
            for name, bit in DIRECTION_BITS.items():
                if (old_val >> bit) & 1:
                    new_val |= 1 << DIRECTION_BITS[rmap[name]]
            block_nbt['connections'].value = new_val
            changed = True
        if 'sideConfig' in block_nbt:
            cfg = block_nbt['sideConfig']
            items = getattr(cfg, 'value', None)
            if items is None:
                items = getattr(cfg, 'tags', None)
            if items is not None and len(items) == 6:
                src = list(items)
                for name, bit in DIRECTION_BITS.items():
                    items[DIRECTION_BITS[rmap[name]]] = src[bit]
                changed = True
        if changed:
            updated += 1
    return updated


def flip_structure_blocks(nbt_file, axis):
    if 'blocks' not in nbt_file:
        print("Error: No 'blocks' tag found in structure NBT file")
        return 0

    blocks = nbt_file['blocks']
    flipped_count = 0

    for block in blocks:
        if 'pos' in block:
            pos = block['pos']
            x = pos[0].value
            y = pos[1].value
            z = pos[2].value

            if axis.upper() == 'X':
                if 'size' in nbt_file:
                    width = nbt_file['size'][0].value
                    pos[0].value = width - 1 - x
                else:
                    pos[0].value = -x
                print(f"Block at ({x}, {y}, {z}) -> ({pos[0].value}, {y}, {z})")

            elif axis.upper() == 'Y':
                if 'size' in nbt_file:
                    height = nbt_file['size'][1].value
                    pos[1].value = height - 1 - y
                else:
                    pos[1].value = -y
                print(f"Block at ({x}, {y}, {z}) -> ({x}, {pos[1].value}, {z})")

            elif axis.upper() == 'Z':
                if 'size' in nbt_file:
                    depth = nbt_file['size'][2].value
                    pos[2].value = depth - 1 - z
                else:
                    pos[2].value = -z
                print(f"Block at ({x}, {y}, {z}) -> ({x}, {y}, {pos[2].value})")

            flipped_count += 1

    return flipped_count

def flip_palette_properties(nbt_file, axis):
    if 'palette' not in nbt_file:
        return 0

    axis = axis.upper()

    facing_swap = {
        'X': {'east': 'west', 'west': 'east'},
        'Y': {'up': 'down', 'down': 'up'},
        'Z': {'north': 'south', 'south': 'north'},
    }
    prop_pairs = {
        'X': [('east', 'west')],
        'Y': [('up', 'down')],
        'Z': [('north', 'south')],
    }
    half_swap = {
        'Y': {'top': 'bottom', 'bottom': 'top'},
    }
    shape_swap = {
        'inner_left': 'inner_right', 'inner_right': 'inner_left',
        'outer_left': 'outer_right', 'outer_right': 'outer_left',
    }

    swap_map = facing_swap.get(axis, {})
    pairs = prop_pairs.get(axis, [])
    half_map = half_swap.get(axis, {})
    swapped_count = 0

    for entry in nbt_file['palette']:
        if 'Properties' not in entry:
            continue

        props = entry['Properties']
        changed = False

        if 'facing' in props and props['facing'].value in swap_map:
            props['facing'].value = swap_map[props['facing'].value]
            changed = True

        if 'half' in props and props['half'].value in half_map:
            props['half'].value = half_map[props['half'].value]
            changed = True

        if axis in ('X', 'Z') and 'shape' in props and props['shape'].value in shape_swap:
            props['shape'].value = shape_swap[props['shape'].value]
            changed = True

        for a, b in pairs:
            if a in props and b in props:
                props[a].value, props[b].value = props[b].value, props[a].value
                changed = True

        if changed:
            name = entry['Name'].value if 'Name' in entry else '?'
            print(f"Swapped properties for palette entry: {name}")
            swapped_count += 1

    return swapped_count

def flip_block_nbt_directions(nbt_file, axis):
    if 'blocks' not in nbt_file:
        return 0

    axis = axis.upper()

    bit_index = DIRECTION_BITS
    swap_pairs = {
        'X': ('east', 'west'),
        'Y': ('down', 'up'),
        'Z': ('north', 'south'),
    }

    pair = swap_pairs.get(axis)
    if pair is None:
        return 0

    bit_a, bit_b = bit_index[pair[0]], bit_index[pair[1]]
    updated_count = 0

    for block in nbt_file['blocks']:
        if 'nbt' not in block:
            continue

        block_nbt = block['nbt']
        changed = False

        if 'connections' in block_nbt:
            value = block_nbt['connections'].value
            a = (value >> bit_a) & 1
            b = (value >> bit_b) & 1
            value &= ~((1 << bit_a) | (1 << bit_b))
            value |= (a << bit_b) | (b << bit_a)
            block_nbt['connections'].value = value
            changed = True

        if 'sideConfig' in block_nbt:
            config_tag = block_nbt['sideConfig']
            if isinstance(config_tag, TAG_Compound):
                name_a, name_b = pair
                if name_a in config_tag and name_b in config_tag:
                    config_tag[name_a].value, config_tag[name_b].value = config_tag[name_b].value, config_tag[name_a].value
                    changed = True
            else:
                items = getattr(config_tag, 'value', None)
                if items is None:
                    items = getattr(config_tag, 'tags', None)
                if items is not None and bit_a < len(items) and bit_b < len(items):
                    items[bit_a], items[bit_b] = items[bit_b], items[bit_a]
                    changed = True

        if changed:
            updated_count += 1

    return updated_count

def main():
    parser = argparse.ArgumentParser(
        description='Flip Minecraft structure NBT file block positions on X, Y or Z axis'
    )

    parser.add_argument('file', help='Path to the structure NBT file')

    parser.add_argument(
        '-a', '--axis',
        choices=['X', 'Y', 'Z', 'x', 'y', 'z'],
        help='Axis to mirror: X, Y or Z'
    )

    parser.add_argument(
        '-r', '--rotate',
        type=int,
        choices=[90, 180, 270],
        help='Rotate clockwise about Y by 90, 180 or 270 degrees'
    )

    parser.add_argument(
        '-o', '--output',
        help='Output file path (default: overwrites input file)',
        default=None
    )

    parser.add_argument(
        '-i', '--info',
        action='store_true',
        help='Print structure information and exit'
    )

    args = parser.parse_args()

    print(f"Reading NBT file: {args.file}")
    nbt_file = read_nbt_file(args.file)

    print_structure_info(nbt_file)
    if args.info:
        exit(0)

    if not args.axis and not args.rotate:
        parser.error('give --axis to mirror or --rotate to rotate')
    if args.axis and args.rotate:
        parser.error('--axis and --rotate are mutually exclusive')

    if args.rotate:
        print(f"Rotating {args.rotate} degrees clockwise about Y...")
        flipped_count = rotate_structure_blocks(nbt_file, args.rotate)
        swapped_count = rotate_palette_properties(nbt_file, args.rotate)
        updated_count = rotate_block_nbt_directions(nbt_file, args.rotate)
        verb = f"rotated {args.rotate} degrees"
    else:
        print(f"Flipping {args.axis.upper()} axis...")
        flipped_count = flip_structure_blocks(nbt_file, args.axis)
        swapped_count = flip_palette_properties(nbt_file, args.axis)
        updated_count = flip_block_nbt_directions(nbt_file, args.axis)
        verb = f"flipped on {args.axis.upper()} axis"

    output_path = args.output if args.output else args.file

    print(f"Saving to: {output_path}")
    nbt_file.write_file(output_path)

    print(f"✓ Successfully {verb}: {flipped_count} blocks")
    print(f"✓ Swapped properties on {swapped_count} palette entries")
    print(f"✓ Updated directional NBT on {updated_count} blocks")

if __name__ == '__main__':
    main()
