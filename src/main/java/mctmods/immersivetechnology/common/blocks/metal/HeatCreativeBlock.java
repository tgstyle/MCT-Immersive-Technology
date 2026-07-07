package mctmods.immersivetechnology.common.blocks.metal;

import mctmods.immersivetechnology.common.blocks.helper.ITIEntityBlock;
import mctmods.immersivetechnology.common.blocks.metal.logic.HeatCreativeIBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiFunction;

public class HeatCreativeBlock extends ITIEntityBlock<HeatCreativeIBlockEntity> {
    public HeatCreativeBlock(BiFunction<BlockPos, BlockState, HeatCreativeIBlockEntity> makeEntity, Properties blockProps) { super(makeEntity, blockProps); }
}
