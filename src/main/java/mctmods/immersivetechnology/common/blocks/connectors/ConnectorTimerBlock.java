package mctmods.immersivetechnology.common.blocks.connectors;

import blusunrize.immersiveengineering.common.items.ScrewdriverItem;
import blusunrize.immersiveengineering.common.items.WireCoilItem;
import mctmods.immersivetechnology.common.blocks.connectors.logic.ConnectorTimerBlockEntity;
import mctmods.immersivetechnology.common.blocks.helper.ITEntityBlock;
import mctmods.immersivetechnology.common.blocks.helper.ITProperties;
import mctmods.immersivetechnology.core.registration.ITBlockEntities;
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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public class ConnectorTimerBlock extends ITEntityBlock<ConnectorTimerBlockEntity> {
    public static final IntegerProperty ROTATION = IntegerProperty.create("rotation", 0, 3);

    public ConnectorTimerBlock(BiFunction<BlockPos, BlockState, ConnectorTimerBlockEntity> makeEntity, Properties p) {
        super(makeEntity, p);
        registerDefaultState(stateDefinition.any().setValue(ITProperties.FACING_ALL, Direction.NORTH).setValue(ROTATION, 0));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ITProperties.FACING_ALL, ROTATION);
    }

    @Override public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return ITBlockEntities.CONNECTOR_TIMER.get().create(pos, state);
    }

    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return type == ITBlockEntities.CONNECTOR_TIMER.get() ? (l, p, s, be) -> ((ConnectorTimerBlockEntity) be).tickServer() : null;
    }

    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getClickedFace();
        float yRot = context.getPlayer() != null ? context.getPlayer().getYRot() : 0f;
        int rotation = Direction.fromYRot(yRot).get2DDataValue();
        return defaultBlockState().setValue(ITProperties.FACING_ALL, facing).setValue(ROTATION, rotation);
    }

    @Override public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @org.jetbrains.annotations.Nullable net.minecraft.world.entity.LivingEntity placer, @NotNull net.minecraft.world.item.ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ConnectorTimerBlockEntity timer) {
            int rot = state.getValue(ROTATION);
            timer.setRotation(rot);
            timer.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    @Override public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @org.jetbrains.annotations.Nullable Direction side) {
        if (side == null) { return false; }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ConnectorTimerBlockEntity timer) { return timer.canConnectRedstone(side); }
        return super.canConnectRedstone(state, level, pos, side);
    }

    @Override public int getSignal(@NotNull BlockState state, BlockGetter level, @NotNull BlockPos pos, @NotNull Direction direction) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ConnectorTimerBlockEntity timer) { return timer.getWeakRSOutput(direction); }
        return 0;
    }

    @Override public int getDirectSignal(@NotNull BlockState state, BlockGetter level, @NotNull BlockPos pos, @NotNull Direction direction) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ConnectorTimerBlockEntity timer) { return timer.getStrongRSOutput(direction); }
        return 0;
    }

    @Override public boolean isSignalSource(@NotNull BlockState state) { return true; }

    @Override public void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block block, @NotNull BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (level.isClientSide) return;
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ConnectorTimerBlockEntity timer) { timer.rsDirty = true; }
    }

    @Override @NotNull public InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);

        if (!held.isEmpty() && held.getItem() instanceof ScrewdriverItem) {
            if (level.isClientSide) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof ConnectorTimerBlockEntity timer) {
                    net.minecraft.client.Minecraft.getInstance().setScreen(new mctmods.immersivetechnology.client.gui.ConnectorConfigScreen(timer));
                }
            }
            return InteractionResult.SUCCESS;
        }

        if (!held.isEmpty() && held.getItem() instanceof WireCoilItem) { return InteractionResult.PASS; }

        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ConnectorTimerBlockEntity timer) {
            if (player.isCrouching()) {
                timer.setIoMode((timer.getIoMode() == 0) ? 1 : 0);
                timer.setChanged();
                timer.markContainingBlockForUpdate(null);
                level.blockEvent(pos, this, 254, 0);
            } else if (held.isEmpty()) {
                NetworkHooks.openScreen((ServerPlayer) player, timer, buf -> buf.writeBlockPos(pos));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
