package mctmods.immersivetechnology.common.multiblocks.metal.shapes;

import mctmods.immersivetechnology.core.util.multiblock.MultiblockData;
import mctmods.immersivetechnology.core.util.multiblock.MultiblockDataLoader;

import mctmods.immersivetechnology.core.util.multiblock.ShapeData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;
import java.util.List;
import java.util.function.Function;

public class BoilerTankShape {
    public static final MultiblockData DATA = MultiblockDataLoader.loadMultiblockData("boiler_tank");
    private static final ShapeData SHAPE = ShapeData.load("boiler_tank", "metal");
    public static final Function<BlockPos, VoxelShape> GETTER = SHAPE.getter;
    public static final int WIDTH = SHAPE.width, HEIGHT = SHAPE.height, LENGTH = SHAPE.length;
    public static final BlockPos MASTER_POS = SHAPE.masterPos;
    public static final BlockPos TRIGGER_POS = SHAPE.triggerPos;
    public static final BlockPos CLIENT_OFFSET = SHAPE.clientOffset;
    public static final float MANUAL_SCALE = SHAPE.manualScale;
    public static final List<BlockPos> SYMMETRIC_TRIGGER_OFFSETS = SHAPE.symmetricTriggerOffsets;
}
