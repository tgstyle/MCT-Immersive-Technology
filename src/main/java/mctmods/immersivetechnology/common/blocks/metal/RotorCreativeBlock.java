package mctmods.immersivetechnology.common.blocks.metal;

import mctmods.immersivetechnology.common.blocks.helper.ITIEntityBlock;
import mctmods.immersivetechnology.common.blocks.metal.logic.RotorCreativeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public class RotorCreativeBlock extends ITIEntityBlock<RotorCreativeBlockEntity> {
    public static final net.minecraft.world.level.block.state.properties.Property<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public RotorCreativeBlock(BiFunction<BlockPos, BlockState, RotorCreativeBlockEntity> makeEntity, Properties p) { super(makeEntity, p); registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH)); }

    @Override protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) { super.createBlockStateDefinition(builder); builder.add(FACING); }

    @Override @NotNull public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) { return getRotorShape(state); }

    @Override @NotNull public VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) { return getRotorShape(state); }

    @Override public @NotNull VoxelShape getOcclusionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) { return getRotorShape(state); }

    private VoxelShape getRotorShape(BlockState state) {
        Direction facing = state.getValue(FACING);
        double minAlong = 0.0D;
        double maxAlong = 1.0D;
        double minPerp = 0.125D;
        double maxPerp = 0.875D;
        if (facing.getAxis() == Direction.Axis.X) { return Shapes.box(minAlong, minPerp, minPerp, maxAlong, maxPerp, maxPerp); }
        return Shapes.box(minPerp, minPerp, minAlong, maxPerp, maxPerp, maxAlong);
    }

    @Override public BlockState getStateForPlacement(BlockPlaceContext context) { return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()); }

    @Override @NotNull public InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
        if (level.isClientSide) { return InteractionResult.SUCCESS; }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof MenuProvider menuProvider) { player.openMenu(menuProvider, buf -> buf.writeBlockPos(pos)); }
        return InteractionResult.CONSUME;
    }

    @Override public @NotNull RenderShape getRenderShape(@NotNull BlockState state) { return RenderShape.ENTITYBLOCK_ANIMATED; }
}
