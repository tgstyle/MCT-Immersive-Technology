package mctmods.immersivetechnology.common.blocks.metal;

import mctmods.immersivetechnology.common.blocks.helper.ITEntityBlock;
import mctmods.immersivetechnology.common.blocks.helper.ITClientTickableBE;
import mctmods.immersivetechnology.core.registration.ITBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import blusunrize.immersiveengineering.api.IEProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public class CokeOvenHeaterBlock extends ITEntityBlock<CokeOvenHeaterBlockEntity> {
    public CokeOvenHeaterBlock(BiFunction<BlockPos, BlockState, CokeOvenHeaterBlockEntity> makeEntity, Properties blockProps) { super(makeEntity, blockProps); }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(IEProperties.FACING_HORIZONTAL, IEProperties.MULTIBLOCKSLAVE);
    }

    @Override
    public boolean canIEBlockBePlaced(BlockState newState, BlockPlaceContext context) {
        BlockPos start = context.getClickedPos();
        return areAllReplaceable(start, start.north(2), context);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(@NotNull Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) { return pBlockEntityType == ITBlockEntities.COKE_OVEN_HEATER.get() && pLevel.isClientSide ? ITClientTickableBE.makeTicker() : null; }
}