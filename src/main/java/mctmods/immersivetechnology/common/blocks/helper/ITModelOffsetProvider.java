package mctmods.immersivetechnology.common.blocks.helper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;

public interface ITModelOffsetProvider {
    BlockPos getModelOffset(BlockState state, Vec3i size);
}
