package mctmods.immersivetechnology.common.blocks.metal;

import com.immersiveconvergence.api.block.ModEntityBlock;
import mctmods.immersivetechnology.common.blocks.metal.logic.RotorCreativeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;
import java.util.function.BiFunction;

import javax.annotation.Nonnull;

public class RotorCreativeBlock extends ModEntityBlock<RotorCreativeBlockEntity> {
    public static final net.minecraft.world.level.block.state.properties.Property<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public RotorCreativeBlock(BiFunction<BlockPos, BlockState, RotorCreativeBlockEntity> makeEntity, Properties p) { super(makeEntity, p); registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH)); }

    @Override protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) { super.createBlockStateDefinition(builder); builder.add(FACING); }

    @Override @Nonnull public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nonnull CollisionContext context) { return getRotorShape(state); }

    @Override @Nonnull public VoxelShape getCollisionShape(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nonnull CollisionContext context) { return getRotorShape(state); }

    @SuppressWarnings("deprecation")
    @Override public @Nonnull VoxelShape getOcclusionShape(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos) { return getRotorShape(state); }

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

    @Override @Nonnull public InteractionResult use(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof MenuProvider menuProvider) { NetworkHooks.openScreen((ServerPlayer) player, menuProvider, buf -> buf.writeBlockPos(pos)); }
        return InteractionResult.CONSUME;
    }

    @SuppressWarnings("deprecation")
    @OnlyIn(Dist.CLIENT) @Override public @Nonnull RenderShape getRenderShape(@Nonnull BlockState state) { return RenderShape.ENTITYBLOCK_ANIMATED; }
}
