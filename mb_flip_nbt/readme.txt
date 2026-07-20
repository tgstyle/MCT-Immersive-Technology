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
