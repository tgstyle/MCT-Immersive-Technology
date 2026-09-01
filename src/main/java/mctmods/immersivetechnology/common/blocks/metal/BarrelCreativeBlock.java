package mctmods.immersivetechnology.common.blocks.metal;

import com.immersiveconvergence.api.block.ModEntityBlock;
import mctmods.immersivetechnology.common.blocks.metal.logic.BarrelCreativeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiFunction;

public class BarrelCreativeBlock extends ModEntityBlock<BarrelCreativeBlockEntity> {
    public BarrelCreativeBlock(BiFunction<BlockPos, BlockState, BarrelCreativeBlockEntity> makeEntity, Properties blockProps) { super(makeEntity, blockProps); }
}
