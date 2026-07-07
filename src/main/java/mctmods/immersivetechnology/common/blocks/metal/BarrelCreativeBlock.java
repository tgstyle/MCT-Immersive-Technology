package mctmods.immersivetechnology.common.blocks.metal;

import mctmods.immersivetechnology.common.blocks.helper.ITIEntityBlock;
import mctmods.immersivetechnology.common.blocks.metal.logic.BarrelCreativeIBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiFunction;

public class BarrelCreativeBlock extends ITIEntityBlock<BarrelCreativeIBlockEntity> {
    public BarrelCreativeBlock(BiFunction<BlockPos, BlockState, BarrelCreativeIBlockEntity> makeEntity, Properties blockProps) { super(makeEntity, blockProps); }
}
