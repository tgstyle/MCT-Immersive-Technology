package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import mctmods.immersivetechnology.common.multiblocks.ITShapes;

import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import com.immersiveconvergence.api.multiblock.MultiblockPOIHelper;
import com.immersiveconvergence.api.multiblock.PoIJSONSchema;
import net.minecraft.core.BlockPos;
import java.util.List;

public class RadiatorLogic extends RadiatorBaseLogic {
    public static List<BlockPos> INPUT_FLUID_POIS;
    public static List<BlockPos> OUTPUT_FLUID_POIS;
    public static BlockPos REDSTONE_POI;
    public static List<BlockPos> COMPARATOR_POSITIONS;
    public static BlockPos SOUND_POI;
    private static RelativeBlockFace INPUT_FLUID_FACING;
    private static RelativeBlockFace OUTPUT_FLUID_FACING;

    static { ITShapes.readPois("radiator", RadiatorLogic::loadPois); }

    @Override protected String shapeName() { return "radiator"; }
    @Override protected List<BlockPos> inputPois() { return INPUT_FLUID_POIS; }
    @Override protected RelativeBlockFace inputFacing() { return INPUT_FLUID_FACING; }
    @Override protected RelativeBlockFace outputFacing() { return OUTPUT_FLUID_FACING; }
    @Override protected List<BlockPos> comparatorPositions() { return COMPARATOR_POSITIONS; }
    @Override protected BlockPos soundPoi() { return SOUND_POI; }
    @Override protected BlockPos redstonePoi() { return REDSTONE_POI; }
    @Override protected BlockPos columnPos(int offset, int depth, int sideStep) { return new BlockPos(sideStep, offset, depth); }

    @Override public List<BlockPos> getOutputPositions() { return OUTPUT_FLUID_POIS; }

    private static void loadPois(List<PoIJSONSchema> pois) {
        INPUT_FLUID_POIS = MultiblockPOIHelper.getPosList(pois, "fluid_input0");
        OUTPUT_FLUID_POIS = MultiblockPOIHelper.getPosList(pois, "fluid_output0");
        REDSTONE_POI = MultiblockPOIHelper.getPosList(pois, "redstone0").get(0);
        COMPARATOR_POSITIONS = MultiblockPOIHelper.getPosList(pois, "comparator0");
        SOUND_POI = MultiblockPOIHelper.getPosList(pois, "sound0").get(0);
        INPUT_FLUID_FACING = MultiblockPOIHelper.getFacing(pois, "fluid_input0");
        OUTPUT_FLUID_FACING = MultiblockPOIHelper.getFacing(pois, "fluid_output0");
    }
}
