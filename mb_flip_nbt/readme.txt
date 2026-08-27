Requires the NBT package (twoolie's Named Binary Tag Reader/Writer). PyPI name
is NBT, import name is nbt:

    pip install NBT

# View structure information
python flip_nbt.py structure.nbt --info

# Flip X axis (overwrites the original file)
python flip_nbt.py structure.nbt --axis X

# Flip Y axis (overwrites the original file)
python flip_nbt.py structure.nbt --axis Y

# Flip Z axis (overwrites the original file)
python flip_nbt.py structure.nbt --axis Z

# Flip and save to a new file
python flip_nbt.py structure.nbt --axis Y --output flipped_structure.nbt

# Flip Z axis with verbose output
python flip_nbt.py my_structure.nbt --axis Z -o result.nbt

# Rotate clockwise about Y (90, 180 or 270), overwriting the original
python flip_nbt.py structure.nbt --rotate 90

# Rotate and save to a new file
python flip_nbt.py structure.nbt --rotate 270 -o rotated_structure.nbt

--axis and --rotate are mutually exclusive; give exactly one.

Rotation moves block positions, swaps size X/Z on an odd number of quarter
turns, rotates facing / axis / four-sided boolean properties in the palette, and
rotates IE pipe "connections" bitmasks and "sideConfig" arrays. Both of those are
indexed by Minecraft's Direction ordinal: DOWN 0, UP 1, NORTH 2, SOUTH 3,
WEST 4, EAST 5. The flip path used to carry a table with north/south and
east/west transposed, which was invisible because flipping only ever swaps
within a pair; rotation needs the real order, so the table is now correct for
both.
