package mctmods.immersivetechnology.common.blocks.metal;

import mctmods.immersivetechnology.common.blocks.helper.ITEntityBlock;
import mctmods.immersivetechnology.common.blocks.helper.ITProperties;
import mctmods.immersivetechnology.common.blocks.metal.logic.TrashFluidBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public class TrashFluidBlock extends ITEntityBlock<TrashFluidBlockEntity> {
    public TrashFluidBlock(BiFunction<BlockPos, BlockState, TrashFluidBlockEntity> makeEntity, Properties blockProps) { super(makeEntity, blockProps); }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ITProperties.FACING_HORIZONTAL, BlockStateProperties.WATERLOGGED);
    }
}
