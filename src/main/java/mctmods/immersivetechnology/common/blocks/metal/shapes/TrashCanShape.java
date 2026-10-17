package mctmods.immersivetechnology.common.blocks.metal.shapes;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class TrashCanShape {
    public static final VoxelShape BOUNDS = Block.box(2, 0, 2, 14, 16, 14);

    private TrashCanShape() {}
}
