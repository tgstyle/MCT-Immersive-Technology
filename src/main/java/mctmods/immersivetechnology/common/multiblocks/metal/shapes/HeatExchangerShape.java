package mctmods.immersivetechnology.common.multiblocks.metal.shapes;

import mctmods.immersivetechnology.core.util.multiblock.MultiblockData;
import mctmods.immersivetechnology.core.util.multiblock.MultiblockDataLoader;

import mctmods.immersivetechnology.core.util.multiblock.ShapeData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;
import java.util.function.Function;

public class HeatExchangerShape {
    public static final MultiblockData DATA = MultiblockDataLoader.loadMultiblockData("heat_exchanger");
    private static final ShapeData SHAPE = ShapeData.load("heat_exchanger", "metal");
    public static final Function<BlockPos, VoxelShape> GETTER = SHAPE.getter;
    public static final int WIDTH = SHAPE.width, HEIGHT = SHAPE.height, LENGTH = SHAPE.length;
    public static final BlockPos MASTER_POS = SHAPE.masterPos;
    public static final BlockPos TRIGGER_POS = SHAPE.triggerPos;
    public static final BlockPos CLIENT_OFFSET = SHAPE.clientOffset;
    public static final float MANUAL_SCALE = SHAPE.manualScale;
}
