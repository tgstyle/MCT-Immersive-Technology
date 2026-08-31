package mctmods.immersivetechnology.common.multiblocks.metal.shapes;

import mctmods.immersivetechnology.core.lib.Reference;

import com.immersiveconvergence.api.multiblock.MultiblockData;
import com.immersiveconvergence.api.multiblock.MultiblockDataLoader;
import com.immersiveconvergence.api.multiblock.ShapeData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;
import java.util.function.Function;

public class RadiatorShape {
    public static final MultiblockData DATA = MultiblockDataLoader.loadMultiblockData(Reference.class, Reference.MODID, "radiator");
    private static final ShapeData SHAPE = ShapeData.load(Reference.class, Reference.MODID, "radiator", "metal");
    public static final Function<BlockPos, VoxelShape> GETTER = SHAPE.getter;
    public static final int WIDTH = SHAPE.width, HEIGHT = SHAPE.height, LENGTH = SHAPE.length;
    public static final BlockPos MASTER_POS = SHAPE.masterPos;
    public static final BlockPos TRIGGER_POS = SHAPE.triggerPos;
    public static final BlockPos CLIENT_OFFSET = SHAPE.clientOffset;
    public static final float MANUAL_SCALE = SHAPE.manualScale;
}
