Pythyon setup:
	Install python -
		https://www.python.org/downloads/windows/
		or Python from the MS Store
	
	Install needed addons -
		pip install numpy
		pip install scipy
		pip install tqdm

Script setup:
	Place all blockbench models in the folder you want to generate a shape from -
		e.g. "C:\SteamTurbine\steam_turbine.bbmodel"
		     "C:\SteamTurbine\rotor.bbmodel"

Execute python:
	python bb_shape_mp.py C:\SteamTurbine\steam_turbine.bbmodel

Example script execution:

	Additional BBModel files detected:
	1. rotor.bbmodel
	Do you want to add a supplementary model? (y/n): y
	How many times to add this supplementary? 2
	Enter offset (bx,by,bz): 1,1,0
	Enter offset (bx,by,bz): 1,1,5
	Processing main model...
	steam_turbine.bbmodel: 100%|█████████████████████████████████████████████████████████| 120/120 [05:14<00:00,  2.62s/it]
	Processing supplementary model: rotor.bbmodel	

Shape output:
	The script will output a txt file with the same name as the main model -
		steam_turbine.txt

Script arguments:
    'path', nargs='?', help='Path to bbmodel or directory'
    '--output', choices=['java', 'json'], help='Output type: java or json'
    '--y-threshold', type=int, default=8, help='Threshold for vertical filling (y axis); 0 for unlimited, -1 to disable'
    '--x-threshold', type=int, default=2, help='Threshold for filling along x axis; 0 for unlimited, -1 to disable'
    '--z-threshold', type=int, default=2, help='Threshold for filling along z axis; 0 for unlimited, -1 to disable'
    '--gap-passes', type=int, default=3, help='Number of passes for gap filling per axis'
    '--small-void-threshold', type=int, default=4, help='Maximum voxel count for small voids to fill (fills if size < threshold)'
    '--no-global-postprocess', help='Disable global post-processing on the full model (default: enabled)'
    '--no-postprocess', help='Disable all post-processing steps'
    '--no-holes', help='Disable binary_fill_holes'
    '--no-gaps', help='Disable gap filling along axes (overrides thresholds)'
    '--no-small-voids', help='Disable small void removal'
    '--per-block-gap-x', help='Force per-block gap filling for X axis when global postprocess is enabled'
    '--per-block-gap-y', help='Force per-block gap filling for Y axis when global postprocess is enabled'
    '--per-block-gap-z', help='Force per-block gap filling for Z axis when global postprocess is enabled'

	Example:
		bb_shape_mp.py ..\Boiler\single\boiler_new_sterilized.bbmodel --x-threshold 8 --z-threshold 4 --per-block-gap-x --gap-passes 4