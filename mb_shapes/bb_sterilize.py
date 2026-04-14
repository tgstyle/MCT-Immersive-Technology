import json
import sys
import os

def snap_to_grid(value, grid_size=1/16):
    """Snap value to nearest grid increment (1/16 = 0.0625)."""
    return round(value / grid_size) * grid_size

def sterilize_bbmodel(input_path):
    """Load .bbmodel JSON, snap vertex coordinates to 1/16 grid, save compressed to derived output."""
    with open(input_path, 'r') as f:
        data = json.load(f)
    
    # Process each element
    for element in data.get('elements', []):
        vertices = element.get('vertices', {})
        for key, coord in vertices.items():
            # Snap x, y, z to nearest 1/16
            vertices[key] = [snap_to_grid(c) for c in coord]
    
    # Derive output path: same dir, same name + _sterilized + extension
    base, ext = os.path.splitext(input_path)
    output_path = f"{base}_sterilized{ext}"
    
    # Save sterilized JSON compressed
    with open(output_path, 'w') as f:
        json.dump(data, f, indent=None, separators=(',', ':'))
    
    print(f"Sterilized model saved to: {output_path}")

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python sterilize_bbmodel.py input.bbmodel")
        sys.exit(1)
    sterilize_bbmodel(sys.argv[1])