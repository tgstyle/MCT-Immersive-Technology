import os

def calculate_obj_bounds(file_path):
    min_x, min_y, min_z = float('inf'), float('inf'), float('inf')
    max_x, max_y, max_z = float('-inf'), float('-inf'), float('-inf')
    
    with open(file_path, 'r') as f:
        for line in f:
            if line.startswith('v '):
                parts = line.strip().split()[1:]
                if len(parts) == 3:
                    x, y, z = map(float, parts)
                    min_x = min(min_x, x)
                    min_y = min(min_y, y)
                    min_z = min(min_z, z)
                    max_x = max(max_x, x)
                    max_y = max(max_y, y)
                    max_z = max(max_z, z)
    
    return (min_x, max_x, min_y, max_y, min_z, max_z)

def traverse_and_compute_bounds(root_dir):
    for subdir, _, files in os.walk(root_dir):
        for file in files:
            if file.endswith('.obj'):
                file_path = os.path.join(subdir, file)
                bounds = calculate_obj_bounds(file_path)
                print(f"File: {os.path.relpath(file_path, root_dir)}")
                print(f"Bounds: X({bounds[0]:.4f} to {bounds[1]:.4f}), Y({bounds[2]:.4f} to {bounds[3]:.4f}), Z({bounds[4]:.4f} to {bounds[5]:.4f})")
                print()

root_directory = '../src/main/resources/assets/immersivetechnology/models/block/multiblock'
traverse_and_compute_bounds(root_directory)