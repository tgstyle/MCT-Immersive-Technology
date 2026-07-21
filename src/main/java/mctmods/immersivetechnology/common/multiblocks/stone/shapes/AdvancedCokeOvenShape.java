package mctmods.immersivetechnology.common.multiblocks.stone.shapes;

import mctmods.immersivetechnology.common.util.multiblock.GenericShape;
import mctmods.immersivetechnology.common.util.multiblock.MultiblockJSONSchema;
import mctmods.immersivetechnology.common.util.multiblock.ShapeData;

import net.minecraft.util.math.BlockPos;

public class AdvancedCokeOvenShape {
    private static final ShapeData SHAPE = ShapeData.load("advanced_coke_oven");
    public static final MultiblockJSONSchema DATA = SHAPE.data;
    public static final GenericShape GETTER = SHAPE;
    public static final int WIDTH = SHAPE.width, HEIGHT = SHAPE.height, LENGTH = SHAPE.length;
    public static final BlockPos MASTER_GRID_POS = SHAPE.masterPos;
}
