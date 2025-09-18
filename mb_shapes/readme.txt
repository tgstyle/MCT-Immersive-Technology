Pythyon setup:
	Install python -
		https://www.python.org/downloads/windows/
		or Python from the MS Store
	
	Install needed addons -
		pip install numpy
		pip install scipy
		pip install tqdm
		pip install torch
		pip install torch_directml

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
    --main <filename>	Default: None	Description: Specifies the main .bbmodel file to process (e.g., --main alternator.bbmodel). Bypasses the interactive selection prompt when a directory is provided, enabling fully automated runs.</filename>
    --output {java,json}	Default: None (prompts for both if unspecified)	Description: Sets the output format: java for if-else AABB code snippets (e.g., for manual copy-paste into Java classes); json for a flat array of AABB lists (null for air blocks, suitable for shape loading). Omitting this generates both.
    --ythresh <int>	Default: 4	Description: Threshold (in voxels) for filling small gaps along the Y (vertical) axis during post-processing. 0 = unlimited filling; -1 = disable. Helps bridge minor vertical discontinuities in voxelized geometry.</int>
    --xthresh <int>	Default: 2	Description: Threshold (in voxels) for filling small gaps along the X axis. 0 = unlimited; -1 = disable. Targets horizontal gaps in the east-west direction.</int>
    --zthresh <int>	Default: 2	Description: Threshold (in voxels) for filling small gaps along the Z axis. 0 = unlimited; -1 = disable. Targets horizontal gaps in the north-south direction.</int>
    --gap-passes <int>	Default: 3	Description: Number of iterative passes applied per axis for gap-filling operations. Higher values improve thoroughness but increase computation time.</int>
    --void-thresh <int>	Default: 4	Description: Maximum voxel count for internal voids to automatically fill during post-processing. Voids smaller than this (and not touching boundaries) are treated as enclosed air and filled to create solid regions.</int>
    --occ-thresh <int>	Default: 4	Description: Maximum voxel count for small occupied (solid) clusters to remove during post-processing. Isolated tiny solids smaller than this are discarded as noise.</int>
    --no-gpp	Default: False	Description: Disables global post-processing on the entire assembled model (after per-block processing). Global steps include hole filling across block boundaries, large-scale gap bridging, and void removal. Enabled by default for cleaner multiblock results.
    --no-postprocess	Default: False	Description: Disables all post-processing entirely (per-block and global). Outputs raw ray-casting results without hole filling, gap bridging, or void cleanup. Useful for debugging geometry detection.
    --no-holes	Default: False	Description: Disables binary hole-filling (using SciPy's ndimage.binary_fill_holes). Prevents filling enclosed air pockets within solid regions.
    --no-gaps	Default: False	Description: Disables all gap-filling along axes, overriding threshold values. Preserves detected gaps without bridging.
    --no-small-voids	Default: False	Description: Disables removal of small internal voids and tiny occupied clusters. Skips the labeling and size-based cleanup step.
    --pbg-x	Default: False	Description: Forces gap-filling to occur per-block (not globally) for the X axis, even if global post-processing is enabled. Useful for models where block boundaries need independent horizontal bridging.
    --pbg-y	Default: False	Description: Forces per-block gap-filling for the Y axis during global post-processing. Targets vertical bridging within individual blocks.
    --pbg-z	Default: False	Description: Forces per-block gap-filling for the Z axis during global post-processing. Targets north-south bridging within blocks.
    --solid-blocks <str>	Default: '' (empty)	Description: Space-separated list of block coordinates (bx,by,bz) to force as fully solid before any detection or post-processing (e.g., --solid-blocks "0,0,0 1,0,0"). Skips ray-casting for these blocks, ensuring full AABBs. Coordinates are relative to the model's block grid.</str>
    --supp-config <str...>	Default: None	Description: Automates supplementary model addition: <model> &#x3C;num_times> <offset1> [<offset2> ...]. Example: --supp-config alternator.bbmodel 2 0,0,0 1,0,0 boiler.bbmodel 1 2,0,0. Bypasses interactive prompts; offsets are (bx,by,bz) placements. Repeat for multiple models.</offset2></offset1></model>
    --dml-index <int>	Default: None (auto-selects)	Description: Specifies the DirectML GPU device index (e.g., 0 for first AMD/Intel GPU). Overrides automatic selection based on device name (prefers non-"(TM) Graphics" adapters).</int>
    --use-fp16	Default: False	Description: Enables half-precision (FP16) tensor computations on GPU (CUDA/DirectML). Speeds up ray-casting for large models but may introduce minor precision errors in boundary detection. Not used on CPU.
    --single-thread	Default: False	Description: Forces single-threaded execution, even on CPU. Disables multiprocessing pool for debugging or compatibility; may slow down per-block processing.
    --no-supplementary	Default: False	Description: Disables detection and interactive addition of supplementary .bbmodel files in the directory. Processes only the main model.

	Example:
		bb_shape_mp.py ..\Boiler\single\boiler_new_sterilized.bbmodel --xthresh 8 --zthresh 4 --pbg-x --gap-passes 4