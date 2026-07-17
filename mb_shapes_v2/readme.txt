Python setup:
	Install python -
		https://www.python.org/downloads/windows/
		or Python from the MS Store

	Install needed addons -
		pip install numpy
		pip install scipy

	Note: torch, torch_directml, and tqdm are no longer used. No GPU is needed.
	Voxelization is now purely geometric (triangle-box overlap + interior flood
	fill), deterministic, and runs in about a second per model on CPU.

Script setup:
	Place all blockbench models in the folder you want to generate a shape from -
		e.g. "C:\SteamTurbine\steam_turbine.bbmodel"
		     "C:\SteamTurbine\rotor.bbmodel"

	Model notes:
		- Mesh vertices must lie on the 1/16 grid (Blockbench default snapping).
		- Models do NOT need to be watertight or stitched. Touching faces,
		  T-junctions, overlaps, and duplicate faces are all fine. The only
		  thing that leaks is a visible gap 1 voxel wide or wider.
		- Unrotated cube elements are supported directly. Rotated cubes are not.
		- Open cavities (bores, chimneys, vents) correctly stay open. Fully
		  sealed interiors are filled solid automatically, so interior blocks
		  become full [] blocks with no flags needed.

Execute python:
	python bb_shape.py C:\SteamTurbine\steam_turbine.bbmodel

	For most models no arguments are needed at all.

Example script execution:

	Additional BBModel files detected:
	1. rotor.bbmodel
	Do you want to add a supplementary model? (y/n): y
	How many times to add this supplementary? 2
	Enter offset (bx,by,bz): 2,1,0
	Enter offset (bx,by,bz): 2,1,6
	Gap-fill thresholds for this model (x,y,z; blank for none):
	Processing main model...
	steam_turbine.bbmodel: 5x5x11 blocks, 327905 voxels, 0.45s
	Processing supplementary model: rotor.bbmodel
	rotor.bbmodel: 1x1x1 blocks, 1164 voxels, 0.01s
	JAVA output written to steam_turbine_java.txt
	JSON output written to steam_turbine_json.txt
	Duration: 0.68 seconds

Shape output:
	The script will output txt files with the same name as the main model -
		steam_turbine_java.txt
		steam_turbine_json.txt

Gap filling (changed from previous version):
	Gap filling is now OFF by default (--thresh "0,0,0") and entirely optional.
	It is no longer needed for correctness; use it only to bridge REAL openings
	you want solid for collision (vents, slats, gaps between rotor blades).

	Threshold values per axis:
		0 or x	off (default)
		N	fill gaps up to N voxels wide
		u	unlimited (any gap with occupied voxels on both sides)
		d	default

	Note: in the previous version 0 meant unlimited; that is now u.

	Per-supplementary thresholds are supported as extra tokens in --supp-config:
		--supp-config "rotor.bbmodel 2 2,1,0 2,1,6 thresh=10,x,10 passes=3"

Script arguments:
    # Input and output paths
    path (positional)	Default: None	Description: Path to bbmodel file or directory containing bbmodel files.
    --main <str>	Default: None	Description: Specify the main model file (bypasses selection prompt).
    --output {java,json}	Default: None	Description: Output format: java or json. Default outputs both.

    # Voxelization control flags
    --no-fill	Default: False	Description: Disable interior flood fill; output the surface shell only.
    --fill-combined	Default: False	Description: Re-run interior fill after supplementary models are merged. Fills cavities that only become sealed once the parts are combined (e.g. a rotor plugging a housing bore).
    --no-postprocess	Default: False	Description: Disable all post-processing steps.
    --no-supplementary	Default: False	Description: Disable processing of supplementary models.

    # Gap filling (all optional, off by default)
    --thresh <str>	Default: '0,0,0'	Description: Comma-separated gap thresholds for x,y,z; 0 or x = off, N = fill gaps up to N voxels, u = unlimited, d = default (use quotes if needed, e.g., "1,3,1").
    --ex-thresh <str>	Default: 'd,d,d'	Description: Comma-separated gap thresholds for excluded blocks along x,y,z; d uses --thresh value, 0 or x to disable.
    --mi <str>	Default: '3,4,4'	Description: Comma-separated max gap-fill intrusion into excluded blocks along x,y,z; d for default, x for no intrusion.
    --gap-passes <int>	Default: 3	Description: Number of passes for gap filling per axis.
    --fill-order <str>	Default: 'x,z,y'	Description: Order of axes for gap filling (comma-separated x,y,z in any order).
    --pbg <str>	Default: ''	Description: Comma-separated axes for the per-block-gaps step (e.g., "x,z").
    --rpp <str> (append)	Default: []	Description: Regional gap filling: "bx,by,bz bx,by,bz ... : x,y,z" where thresholds use d for main thresh, 0 or x to disable (e.g., "0,0,0 1,0,0 : d,d,d").
    --exclude-global <str>	Default: ''	Description: Space-separated bx,by,bz to exclude from global gap filling (e.g., "0,0,0 1,0,0").

    # Block and region overrides
    --solid-blocks <str>	Default: ''	Description: Space-separated bx,by,bz to force as solid before post-processing (e.g., "0,0,0 1,0,0").
    --empty-blocks <str>	Default: ''	Description: Space-separated bx,by,bz to force as empty before post-processing (e.g., "0,0,0 1,0,0").
    --sub-solid-block <str> (append)	Default: []	Description: Force sub-region solid in final shape: "bx,by,bz minX,minY,minZ,maxX,maxY,maxZ" (e.g., "0,0,0 0,0,0,16,16,16"). Applied as final override on the combined model. Values >16 clip to 16, <0 to 0.
    --sub-empty-block <str> (append)	Default: []	Description: Force sub-region empty in final shape: "bx,by,bz minX,minY,minZ,maxX,maxY,maxZ". Applied as final override on the combined model.
    --copy-aabb <str> (append)	Default: []	Description: Copy AABBs: "from_bx,from_by,from_bz to_bx,to_by,to_bz". Applied as the final step after AABB extraction.

    # Shape cosmetics (opt-in)
    --protrusions [x,y,z]	Default: off	Description: Remove protruding clusters on the outside of the model whose size fits within the given range or lower. Bare flag = "1,1,1" (single voxels only); e.g. --protrusions "2,2,2" also removes 2x2x2 studs and smaller tabs. A protruding part is anything too thin to contain a solid (x+1)x(y+1)x(z+1) brick; long thin features (rims, rails, walls) exceed the range on some axis and are always kept, as are the corners and edges of solid shapes.
    --squarify <str>	Default: ''	Description: Box out ragged/angled shapes into clean squares. Two token forms, mixable in one string: "bx,by,bz" boxes every connected cluster in the whole block; "bx,by,bz minX,minY,minZ,maxX,maxY,maxZ" boxes only the clusters inside that sub-region of the block, leaving the rest of the block untouched (e.g. "0,1,5 8,0,0,16,16,16 2,1,5"). Use the sub-region form to centralize on a blemish without absorbing nearby detail. Use --report-shapes to find candidate blocks. Runs after protrusions and before sub-block overrides.
    --report-shapes	Default: False	Description: Print per-block AABB counts after extraction, sorted high to low. Blocks with high counts are ragged/angled shapes and the best candidates for --squarify or --low-res-merge.
    --fill-y-corners	Default: False	Description: Fill 1x1x1 outside corner indents to straighten vertical edges.
    --y-corners-passes <int>	Default: 1	Description: Passes for --fill-y-corners.
    --pp-order <str>	Default: 'per-block,regional,global,per-block-gaps,protrusions,sub-blocks'	Description: Comma-separated order of post-processing steps.

    # Supplementary model configurations
    --supp-config <str...> (append)	Default: []	Description: Supplementary model config: model.bbmodel num_times offset1 offset2... followed by optional thresh=x,y,z and passes=N tokens (e.g., "rotor.bbmodel 2 2,1,0 2,1,6 thresh=10,x,10").

    # Grid sizing and fitting options
    --auto-center	Default: False	Description: Pad X/Z overhang symmetrically so the occupied shape is centered in the block grid (main model only). Superseded by --target-grid if both are set.
    --target-grid <str>	Default: ''	Description: Fit the MAIN model into an explicit block grid, e.g. "5,5,11". Leave an axis blank to auto-compute it, e.g. "5,,11". Use with --grid-anchor to control which side of each axis stays flush. Takes precedence over --auto-center.
    --grid-anchor <str>	Default: 'min,min,min'	Description: Per-axis anchor for --target-grid: min (flush at the min end, pad/trim at the max end), max (flush at the max end, pad/trim at the min end), center (pad/trim equally on both sides). E.g. "center,min,min".
    --clamp-slack <float>	Default: 1.0	Description: Units (out of 16 per block) of real geometry allowed to overhang past --target-grid; that sliver is trimmed. Overhang beyond this raises an error instead of silently trimming.
    --force-grid-dims <str>	Default: ''	Description: Force the output JSON grid to at least this size, e.g. "5,5,12" (widens only, never shrinks). Pads any new cells as null/air. Useful when a supplementary model's placement legitimately extends the occupied grid beyond the main model's own bounds.

    # Low-res merge option
    --low-res-merge	Default: False	Description: Enable low-res merge for angled faces.

    # Minecraft version option
    --mc-version {1.12.2,default}	Default: 'default'	Description: Minecraft version for output adjustment. The script runs by default with the northeast corner 0,0,0. For 1.12.2, it builds the shape from the northwest corner 0,0,0 by flipping the Z-axis after post-processing. Post-processing coordinates (--rpp, --solid-blocks, etc.) need to account for this flip when entered for 1.12.2 mode.

    # Debug option
    --debug-log	Default: False	Description: Write debug details (block keys, voxel counts, placements) to log.txt.

	Example (typical model, no flags needed):
		bb_shape.py ..\SolarTower\solar_tower.bbmodel

	Example (bridging real openings so an open lattice becomes solid collision):
		bb_shape.py ..\Boiler\boiler.bbmodel --thresh "4,10,4" --gap-passes 4 --no-supplementary

	Example (explicit grid fit, centered X overhang, trimmed Z sliver, supplementary widening the grid):
		bb_shape.py .\SteamTurbine\steam_turbine.bbmodel --supp-config "rotor.bbmodel 2 2,1,0 2,1,6" --target-grid "5,5,11" --grid-anchor "center,min,min" --clamp-slack 1 --force-grid-dims "5,5,12"

	Example (same, but the rotor filled into a solid drum for collision while the housing stays exact):
		bb_shape.py .\SteamTurbine\steam_turbine.bbmodel --supp-config "rotor.bbmodel 2 2,1,0 2,1,6 thresh=10,x,10" --target-grid "5,5,11" --grid-anchor "center,min,min" --clamp-slack 1 --force-grid-dims "5,5,12"

	Example (force an open-topped cavity solid anyway, and carve out a walkway):
		bb_shape.py .\SolarTower\solar_tower.bbmodel --solid-blocks "1,1,1 1,2,1" --empty-blocks "0,2,1 2,2,1"
