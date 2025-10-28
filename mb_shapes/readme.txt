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

	Note: Model coordinates must always be in the positive space; negative coordinates are not supported.

Execute python:
	python bb_shape.py C:\SteamTurbine\steam_turbine.bbmodel

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
    # Input and output paths
    path (positional)	Default: None	Description: Path to bbmodel file or directory containing bbmodel files.
    --main <str>	Default: None	Description: Specify the main model file (bypasses selection prompt).
    --output {java,json}	Default: None	Description: Output format: java or json.

    # Post-processing control flags
    --no-postprocess	Default: False	Description: Disable all post-processing steps.
    --no-gpp	Default: False	Description: Disable global post-processing on the full model (enabled by default).
    --no-holes	Default: False	Description: Disable filling of holes using binary_fill_holes.
    --no-gaps	Default: False	Description: Disable gap filling along axes (overrides thresholds).
    --no-small-voids	Default: False	Description: Disable removal of small voids and occupied clusters.
    --fill-all-voids	Default: False	Description: Fill all internal voids regardless of size (useful for large hollow models).
    --no-supplementary	Default: False	Description: Disable processing of supplementary models.

    # Threshold and value settings
    --thresh <str>	Default: '2,4,2'	Description: Comma-separated gap thresholds for x,y,z; use 0 for unlimited, d for default, x to disable (use quotes if needed, e.g., "2,4,2").
    --ex-thresh <str>	Default: 'd,d,d'	Description: Comma-separated gap thresholds for excluded blocks along x,y,z; d uses --thresh value, x to disable (use quotes if needed, e.g., "d,d,d").
    --mi <str>	Default: '3,4,4'	Description: Comma-separated max intrusion into excluded blocks along x,y,z; d for default, x for no intrusion (use quotes if needed, e.g., "3,4,4").
    --gap-passes <int>	Default: 3	Description: Number of passes for gap filling per axis.
    --void-thresh <int>	Default: 4	Description: Max voxel count for small voids to fill (fills if size < threshold).
    --occ-thresh <int>	Default: 4	Description: Max voxel count for small occupied clusters to remove (removes if size < threshold).

    # Block and region specifications
    --pbg <str>	Default: ''	Description: Comma-separated axes for per-block gap filling (e.g., x,y,z; use quotes if needed, e.g., "x,y,z").
    --rpp <str> (append)	Default: []	Description: Regional post-processing: "bx,by,bz bx,by,bz ... : x,y,z" where thresholds use d for main thresh, x to disable (use quotes if needed, e.g., "0,0,0 1,0,0 : d,d,d").
    --solid-blocks <str>	Default: ''	Description: Space-separated bx,by,bz to force as solid before post-processing (e.g., "0,0,0 1,0,0"; use quotes if needed).
    --empty-blocks <str>	Default: ''	Description: Space-separated bx,by,bz to force as empty before post-processing (e.g., "0,0,0 1,0,0"; use quotes if needed).
    --exclude-global <str>	Default: ''	Description: Space-separated bx,by,bz to exclude from global post-processing (e.g., "0,0,0 1,0,0"; use quotes if needed).
    --sub-solid-block <str> (append)	Default: []	Description: Force sub-region solid in final shape: "bx,by,bz minX,minY,minZ,maxX,maxY,maxZ" (e.g., "0,0,0 0,0,0,16,16,16"; use quotes if needed). Applied as final override on the combined model. Values >16 clip to 16, <0 to 0.
    --sub-empty-block <str> (append)	Default: []	Description: Force sub-region empty in final shape: "bx,by,bz minX,minY,minZ,maxX,maxY,maxZ" (e.g., "0,0,0 0,0,0,16,16,16"; use quotes if needed). Applied as final override on the combined model. Values >16 clip to 16, <0 to 0.

    # Order and configuration options
    --fill-order <str>	Default: 'x,z,y'	Description: Order of axes for gap filling (comma-separated x,y,z in any order; use quotes if needed, e.g., "x,z,y").
    --pp-order <str>	Default: 'per-block,regional,global,per-block-gaps,protrusions'	Description: Comma-separated order of main post-processing steps: per-block,regional,global,per-block-gaps,protrusions (use quotes if needed).
    --sub-pp-order <str>	Default: 'remove-small,fill-holes,fill-voids,fill-gaps'	Description: Comma-separated order of sub-post-processing steps: remove-small,fill-holes,fill-voids,fill-gaps (use quotes if needed).

    # Supplementary model configurations
    --supp-config <str...> (append)	Default: []	Description: Supplementary model config: model.bbmodel num_times offset1 offset2... (e.g., model.bbmodel 2 0,0,0 1,0,0; use quotes if needed around the whole config).

    # Device and performance options
    --dml-index <int>	Default: None	Description: DirectML device index to use (overrides automatic enumeration).
    --single-thread	Default: False	Description: Force single-threaded processing even on CPU.

    # Low-res merge option
    --low-res-merge	Default: False	Description: Enable low-res merge for angled faces.

    # Copy AABB option
    --copy-aabb <str> (append)	Default: []	Description: Copy AABBs: "from_bx,from_by,from_bz to_bx,to_by,to_bz" (use quotes if needed). Applied as the final step after AABB extraction.

	Example:
		bb_shape.py ..\Boiler\single\boiler.bbmodel ..\Boiler\boiler.bbmodel --thresh "4,10,4" --gap-passes 4 --no-supplementary