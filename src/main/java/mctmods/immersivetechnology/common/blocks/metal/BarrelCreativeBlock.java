package mctmods.immersivetechnology.common.blocks.metal;

import mctmods.immersivetechnology.common.blocks.helper.ModEntityBlock;
import mctmods.immersivetechnology.common.blocks.metal.logic.BarrelCreativeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiFunction;

public class BarrelCreativeBlock extends ModEntityBlock<BarrelCreativeBlockEntity> {
    public BarrelCreativeBlock(BiFunction<BlockPos, BlockState, BarrelCreativeBlockEntity> makeEntity, Properties blockProps) { super(makeEntity, blockProps); }
}
