package mctmods.immersivetechnology.common.blocks.metal;

import mctmods.immersivetechnology.common.blocks.helper.ITEntityBlock;
import mctmods.immersivetechnology.common.blocks.metal.logic.BarrelCreativeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiFunction;

public class BarrelCreativeBlock extends ITEntityBlock<BarrelCreativeBlockEntity> {
    public BarrelCreativeBlock(BiFunction<BlockPos, BlockState, BarrelCreativeBlockEntity> makeEntity, Properties blockProps) { super(makeEntity, blockProps); }
}
