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
    --no-holes	Default: False	Description: Disable binary_fill_holes.
    --no-gaps	Default: False	Description: Disable gap filling along axes (overrides thresholds).
    --no-small-voids	Default: False	Description: Disable small void removal.
    --fill-all-voids	Default: False	Description: Fill all internal voids regardless of size (for large hollow models).
    --no-supplementary	Default: False	Description: Disable processing of supplementary models.

    # Threshold and value settings
    --thresh <str>	Default: '2,4,2'	Description: Comma-separated thresholds for x,y,z; 0 for unlimited, d for default, x for disable.
    --ex-thresh <str>	Default: 'd,d,d'	Description: Comma-separated gap thresholds along x,y,z for excluded blocks; d uses --thresh, x for disable.
    --mi <str>	Default: '3,4,4'	Description: Comma-separated max intrusion for global fill into excluded blocks along x,y,z; d for default, x for no intrusion.
    --gap-passes <int>	Default: 3	Description: Number of passes for gap filling per axis.
    --void-thresh <int>	Default: 4	Description: Maximum voxel count for small voids to fill (fills if size < threshold).
    --occ-thresh <int>	Default: 4	Description: Maximum voxel count for small occupied clusters to remove (removes if size < threshold).

    # Block and region specifications
    --pbg <str>	Default: ''	Description: Comma-separated axes for per-block gap filling (e.g. "x,y,z").
    --rpp <str> (append)	Default: []	Description: Regional post-processing: "bx,by,bz bx,by,bz ... : x,y,z" thresholds d for main thresh, x disable.
    --solid-blocks <str>	Default: ''	Description: Space-separated bx,by,bz to force solid before postprocess (e.g. "0,0,0 1,0,0").
    --exclude-global <str>	Default: ''	Description: Space-separated bx,by,bz to exclude from global postprocess (e.g. "0,0,0 1,0,0").

    # Order and configuration options
    --fill-order <str>	Default: 'x,z,y'	Description: Order of axes for gap filling (comma-separated x,y,z in any order).
    --pp-order <str>	Default: 'per-block,regional,global,per-block-gaps,protrusions'	Description: Comma-separated order of post-processing steps: per-block,regional,global,per-block-gaps,protrusions.
    --sub-pp-order <str>	Default: 'remove-small,fill-holes,fill-voids,fill-gaps'	Description: Comma-separated order of sub-post-processing steps: remove-small,fill-holes,fill-voids,fill-gaps.

    # Supplementary model configurations
    --supp-config <str...> (append)	Default: []	Description: Auto supp: model num_times offset1 offset2... e.g. model.bbmodel 2 0,0,0 1,0,0 [next_model ...].

    # Device and performance options
    --dml-index <int>	Default: None	Description: DirectML device index to use (overrides enumeration).
    --single-thread	Default: False	Description: Force single-threaded processing even on CPU.

	Example:
		bb_shape.py ..\Boiler\single\boiler_new_sterilized.bbmodel --thresh '8,4,4' --pbg 'x' --gap-passes 4