package mctmods.immersivetechnology.common.multiblocks.metal.shapes;

import com.immersiveconvergence.api.multiblock.GenericShape;
import com.immersiveconvergence.api.multiblock.MultiblockJSONSchema;
import com.immersiveconvergence.api.multiblock.ShapeData;

import mctmods.immersivetechnology.ImmersiveTechnology;

import net.minecraft.util.math.BlockPos;

public class MeltingCrucibleShape {
    private static final ShapeData SHAPE = ShapeData.load(ImmersiveTechnology.MODID, "melting_crucible");
    public static final MultiblockJSONSchema DATA = SHAPE.data;
    public static final GenericShape GETTER = SHAPE;
    public static final int WIDTH = SHAPE.width, HEIGHT = SHAPE.height, LENGTH = SHAPE.length;
    public static final BlockPos MASTER_GRID_POS = SHAPE.masterPos;
}
