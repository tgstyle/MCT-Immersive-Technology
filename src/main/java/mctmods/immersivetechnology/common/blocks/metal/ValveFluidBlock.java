package mctmods.immersivetechnology.common.blocks.metal;

import mctmods.immersivetechnology.common.blocks.helper.ITEntityBlock;
import mctmods.immersivetechnology.common.blocks.helper.ITProperties;
import mctmods.immersivetechnology.common.blocks.metal.logic.ValveCommonBlockEntity;
import mctmods.immersivetechnology.common.blocks.metal.logic.ValveFluidBlockEntity;
import mctmods.immersivetechnology.core.registration.ITTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public class ValveFluidBlock extends ITEntityBlock<ValveFluidBlockEntity> {
    public static final BooleanProperty OPEN = BooleanProperty.create("open");
    public static final IntegerProperty ROTATION = IntegerProperty.create("rotation", 0, 3);

    public ValveFluidBlock(BiFunction<BlockPos, BlockState, ValveFluidBlockEntity> makeEntity, Properties p) { super(makeEntity, p); registerDefaultState(stateDefinition.any().setValue(OPEN, true).setValue(ITProperties.FACING_ALL, Direction.NORTH).setValue(ITProperties.MIRRORED, false).setValue(ROTATION, 0)); }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) { super.createBlockStateDefinition(builder); builder.add(ITProperties.FACING_ALL, ITProperties.MIRRORED, OPEN, ROTATION); }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) { return getValveShape(state); }

    @Override
    public @NotNull VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) { return getValveShape(state); }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull VoxelShape getOcclusionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) { return getValveShape(state); }

    private VoxelShape getValveShape(BlockState state) {
        Direction facing = state.getValue(ITProperties.FACING_ALL);
        Direction.Axis axis = facing.getAxis();
        double minX = axis == Direction.Axis.X ? 0 : 2 / 16D;
        double maxX = axis == Direction.Axis.X ? 1 : 14 / 16D;
        double minY = axis == Direction.Axis.Y ? 0 : 2 / 16D;
        double maxY = axis == Direction.Axis.Y ? 1 : 14 / 16D;
        double minZ = axis == Direction.Axis.Z ? 0 : 2 / 16D;
        double maxZ = axis == Direction.Axis.Z ? 1 : 14 / 16D;
        return Shapes.box(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction side) { return true; }

    @Override
    public void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block fromBlock, @NotNull BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, fromBlock, fromPos, isMoving);
        if (level.isClientSide) return;
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ValveCommonBlockEntity valve) { valve.updateRedstoneState(); }
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.is(ITTags.formationTools)) return super.use(state, level, pos, player, hand, hit);
        if (level.isClientSide) return InteractionResult.SUCCESS;
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ValveCommonBlockEntity valve) {
            if (player.isCrouching()) {
                valve.redstoneMode = (byte) (valve.redstoneMode == 1 ? 2 : 1);
                valve.updateRedstoneState();
                valve.efficientSetChanged();
            } else {
                NetworkHooks.openScreen((ServerPlayer) player, valve, buf -> buf.writeBlockPos(pos));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getClickedFace();
        int rotation = 0;
        if (facing.getAxis().isVertical()) {
            assert context.getPlayer() != null;
            float yRot = context.getPlayer().getYRot();
            rotation = Direction.fromYRot(yRot).get2DDataValue();
        }
        return defaultBlockState().setValue(ITProperties.FACING_ALL, facing).setValue(ITProperties.MIRRORED, false).setValue(OPEN, true).setValue(ROTATION, rotation);
    }
}
