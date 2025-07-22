package mctmods.immersivetechnology.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import mctmods.immersivetechnology.common.blocks.helper.ITEntityBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

import java.util.function.BiFunction;

public class CokeOvenPreheaterBlock extends ITEntityBlock<CokeOvenPreheaterBlockEntity> {
    public CokeOvenPreheaterBlock(BiFunction<BlockPos, BlockState, CokeOvenPreheaterBlockEntity> makeEntity, Properties blockProps) {
        super(makeEntity, blockProps);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(IEProperties.FACING_HORIZONTAL, IEProperties.MULTIBLOCKSLAVE);
    }

    @Override
    public boolean canIEBlockBePlaced(BlockState newState, BlockPlaceContext context) {
        BlockPos start = context.getClickedPos();
        Level w = context.getLevel();
        return areAllReplaceable(start, start.north(2), context);
    }
}
