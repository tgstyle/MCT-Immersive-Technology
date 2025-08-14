Voxelize MC multiblock obj!

It detects the block size of the obj automatically (can be adjusted manually).
It only processes the outside visible faces, inside is full block size.
All files relevant to the shape should be in the same folder.
Increase the res if you don't like how it turned out, or adjust locations manually (warning increasing res makes a larger file).
If it doesn't work right check that your obj is built correctly!

Current output is pastable to a shapes class (txt file).

Install python and the addons needed, then enjoy!
	https://www.python.org/downloads/windows/
	or Python from the MS Store
	
Addons (only needed once) (type commands in powershell/bash) -
	All -
	pip install numpy

	GPU only -
	pip install torch-directml

cpu single-threaded (recommended res 8-16) -
	python voxelize_obj.py path\file.obj(s)

cpu multi-threaded (recommended res 16-32) -
	python voxelize_obj_par.py path\file.obj(s)

gpu (recommended res 32+) (currently windows only, tries CUDA, and fails back to directml, this "should work" on any GPU) -
	python voxelize_obj_gpu.py path\file.obj(s)

parser = argparse.ArgumentParser(description='Voxelize OBJ for AABB collision with multi-threading')
    parser.add_argument('filename', type=str, help='OBJ filename or directory')
    parser.add_argument('--res', type=int, default=16, help='Voxel resolution')
    parser.add_argument('--min-voxel', type=int, default=1, help='Min voxels for AABB and component')

Adapted for MC multiblocks from https://github.com/leanderthiele/voxelize/blob/master/voxelize/voxelize.py

Example in powershell (if all needed obj files are in the path) -

python .\voxelize_obj_gpu.py .\SteamTurbine\steam_turbine.obj --res 32
Selected main file: gas_turbine.obj
Detected OBJ files:
0: steam_turbine_mirrored.obj
1: steam_turbine_rotor.obj
2: steam_turbine_rotor_west_east.obj
Select mirrored OBJ index (-1 for none): 0
Add animation/additional OBJ? y/n: y
Select OBJ index: 1
bX position: 1
bY position: 1
bZ position: 0
Is mirrored (y/n): n
Add animation/additional OBJ? y/n: y
Select OBJ index: 2
bX position: 1
bY position: 1
bZ position: 0
Is mirrored (y/n): y
Add animation/additional OBJ? y/n: y
Select OBJ index: 1
bX position: 1
bY position: 1
bZ position: 5
Is mirrored (y/n): n
Add animation/additional OBJ? y/n: y
Select OBJ index: 2
bX position: 1
bY position: 1
bZ position: 5
Is mirrored (y/n): y
Add animation/additional OBJ? y/n: n
Processing main file: steam_turbine.obj
Processing mirrored file: steam_turbine_mirrored.obj
Processing additional file: steam_turbine_rotor.obj at bX=1, bY=1, bZ=0, is_mirrored=False
Processing additional file: steam_turbine_rotor_west_east.obj at bX=1, bY=1, bZ=0, is_mirrored=True
Processing additional file: steam_turbine_rotor.obj at bX=1, bY=1, bZ=5, is_mirrored=False
Processing additional file: steam_turbine_rotor_west_east.obj at bX=1, bY=1, bZ=5, is_mirrored=True
Output written to steam_turbine.txt