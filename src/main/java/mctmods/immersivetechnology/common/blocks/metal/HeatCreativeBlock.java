package mctmods.immersivetechnology.common.blocks.metal;

import com.immersiveconvergence.api.block.ModEntityBlock;
import mctmods.immersivetechnology.common.blocks.metal.logic.HeatCreativeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiFunction;

public class HeatCreativeBlock extends ModEntityBlock<HeatCreativeBlockEntity> {
    public HeatCreativeBlock(BiFunction<BlockPos, BlockState, HeatCreativeBlockEntity> makeEntity, Properties blockProps) { super(makeEntity, blockProps); }
}
