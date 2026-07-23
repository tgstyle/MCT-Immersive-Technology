package mctmods.immersivetechnology.common.blocks.connectors.logic;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.utils.SafeChunkUtils;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.api.wires.redstone.IRedstoneConnector;
import blusunrize.immersiveengineering.api.wires.redstone.RedstoneNetworkHandler;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.generic.ImmersiveConnectableBlockEntity;
import blusunrize.immersiveengineering.common.items.ScrewdriverItem;
import mctmods.immersivetechnology.common.blocks.connectors.gui.ConnectorTimerMenu;
import mctmods.immersivetechnology.common.blocks.helper.BlockInterfaces;
import mctmods.immersivetechnology.common.blocks.helper.ModProperties;
import mctmods.immersivetechnology.common.blocks.helper.IServerTickableBE;
import mctmods.immersivetechnology.core.registration.BlockEntities;
import mctmods.immersivetechnology.core.registration.MenuTypes;
import mctmods.immersivetechnology.core.util.TranslationKey;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static mctmods.immersivetechnology.common.blocks.connectors.ConnectorTimerBlock.ROTATION;

public class ConnectorTimerBlockEntity extends ImmersiveConnectableBlockEntity implements IServerTickableBE, IStateBasedDirectional, BlockInterfaces.IRedstoneInputOutput, IScrewdriverInteraction, BlockInterfaces.IBlockBounds, BlockInterfaces.IBlockOverlayText, IRedstoneConnector, MenuProvider {
    private static final int PULSE_LENGTH = 2;
    private static final int MIN_TARGET = 10;
    private static final int MAX_TARGET = 600;

    private int target = 40;

    protected int lastOutput = 0;
    protected int rotation = 0;
    protected int ioMode = 0;
    protected int outputClient = 0;
    protected int pulseRemaining = 0;

    public DyeColor redstoneChannel = DyeColor.WHITE;
    public boolean rsDirty = false;

    public ConnectorTimerBlockEntity(BlockPos pos, BlockState state) {
        this(BlockEntities.CONNECTOR_TIMER.get(), pos, state);
    }

    public ConnectorTimerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override public void onLoad() {
        super.onLoad();
        if (level != null && level.isClientSide) {
            BlockState state = level.getBlockState(worldPosition);
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
        rotation = getBlockState().getValue(ROTATION);
    }

    protected void tickServerLogic() {
        if (level == null) { return; }

        int currentInput;
        Direction inputSideForRead = getInputSideForRead();
        if (ioMode == 0) {
            BlockPos neighborPos = worldPosition.relative(inputSideForRead);
            BlockState neighborState = level.getBlockState(neighborPos);
            int power1 = neighborState.getSignal(level, neighborPos, inputSideForRead);
            int power2 = neighborState.getDirectSignal(level, neighborPos, inputSideForRead);
            currentInput = Math.max(power1, power2);
            if (neighborState.hasProperty(RedStoneWireBlock.POWER)) { currentInput = Math.max(currentInput, neighborState.getValue(RedStoneWireBlock.POWER)); }
        } else {
            RedstoneNetworkHandler handler = globalNet.getLocalNet(worldPosition).getHandler(RedstoneNetworkHandler.ID, RedstoneNetworkHandler.class);
            currentInput = (handler != null) ? handler.getValue(redstoneChannel.getId()) : 0;
        }

        long worldTime = level.getGameTime();
        int syncedTick = (int) (worldTime % target);

        if (currentInput > 0) {
            if (syncedTick == 0) {
                pulseRemaining = PULSE_LENGTH;
            }
        }

        int desiredOutput = (pulseRemaining > 0) ? 15 : 0;
        if (pulseRemaining > 0) { pulseRemaining--; }

        if (desiredOutput != lastOutput) {
            lastOutput = desiredOutput;
            rsDirty = true;
            onChange(null, null);
        }
    }

    @Override public final void tickServer() {
        if (rsDirty) {
            RedstoneNetworkHandler handler = globalNet.getLocalNet(worldPosition).getHandler(RedstoneNetworkHandler.ID, RedstoneNetworkHandler.class);
            if (handler != null) { handler.updateValues(); }
        }
        tickServerLogic();
    }

    protected Direction computeInputSide() {
        Direction facing = getFacing();
        if (facing.getAxis().isVertical()) { return Direction.from2DDataValue(rotation); }
        return facing;
    }

    public Direction getInputSide() { return computeInputSide(); }

    private Direction getInputSideForRead() {
        return getInputSide();
    }

    private Direction getRSOutputFace() {
        Direction facing = getFacing();
        if (facing.getAxis().isVertical()) { return getInputSide().getOpposite(); }
        return Direction.DOWN;
    }

    public int getIoMode() { return ioMode; }
    public void setIoMode(int ioMode) { this.ioMode = ioMode; }

    public void setRotation(int value) { this.rotation = value; }

    @Override public boolean isRSInput() { return ioMode == 0; }

    @Override public boolean isRSOutput() { return ioMode == 1; }

    @Override public void updateInput(byte[] signals, ConnectionPoint cp) {
        if (isRSInput() || isRSOutput()) { signals[redstoneChannel.getId()] = (byte) Math.max(lastOutput, signals[redstoneChannel.getId()]); }
        rsDirty = false;
    }

    @Override @NotNull public InteractionResult screwdriverUseSide(@NotNull Direction side, @NotNull Player player, @NotNull InteractionHand hand, @NotNull Vec3 hitVec) {
        if (level != null && level.isClientSide) { return InteractionResult.SUCCESS; }
        if (player.isShiftKeyDown()) { redstoneChannel = DyeColor.byId((redstoneChannel.getId() + 1) % 16); }
        else { ioMode = (ioMode == 0) ? 1 : 0; }
        setChanged();
        RedstoneNetworkHandler handler = globalNet.getLocalNet(worldPosition).getHandler(RedstoneNetworkHandler.ID, RedstoneNetworkHandler.class);
        if (handler != null) { handler.updateValues(); }
        onChange(null, null);
        markContainingBlockForUpdate(null);
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            level.blockEvent(getBlockPos(), getBlockState().getBlock(), 254, 0);
        }
        return InteractionResult.SUCCESS;
    }

    public int getTarget() { return target; }

    public void setTarget(int increment) {
        if (increment < 0) {
            if (target != MIN_TARGET) {
                if (target < 200 && target > 100) { this.target -= 20; } else if (target < 100) { this.target -= 10; } else { this.target -= 40; }
            }
        } else if (increment > 0) {
            if (target != MAX_TARGET) {
                if (target < 200 && target > 100) { this.target += 20; } else if (target < 100) { this.target += 10; } else { this.target += 40; }
            }
        }
    }

    public void receiveMessageFromClient(CompoundTag nbt) {
        boolean needsNetworkUpdate = false;
        boolean needsBlockUpdate = false;

        if (nbt.contains("increment")) {
            setTarget(nbt.getInt("increment"));
            needsBlockUpdate = true;
        }
        if (nbt.contains("ioMode")) {
            ioMode = nbt.getInt("ioMode");
            needsNetworkUpdate = true;
            needsBlockUpdate = true;
        }
        if (nbt.contains("redstoneChannel")) {
            redstoneChannel = DyeColor.byId(nbt.getInt("redstoneChannel"));
            needsNetworkUpdate = true;
            needsBlockUpdate = true;
        }

        if (needsNetworkUpdate) {
            setChanged();
            RedstoneNetworkHandler handler = globalNet.getLocalNet(worldPosition).getHandler(RedstoneNetworkHandler.ID, RedstoneNetworkHandler.class);
            if (handler != null) { handler.updateValues(); }
            onChange(null, null);
        }
        if (needsBlockUpdate) {
            markContainingBlockForUpdate(null);
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                level.blockEvent(getBlockPos(), getBlockState().getBlock(), 254, 0);
            }
        }
    }

    @Override public void writeCustomNBT(@NotNull CompoundTag nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.putInt("target", target);
        nbt.putInt("rotation", rotation);
        nbt.putInt("lastOutput", lastOutput);
        nbt.putInt("ioMode", ioMode);
        nbt.putInt("redstoneChannel", redstoneChannel.getId());
        nbt.putInt("output", lastOutput);
    }

    @Override public void readCustomNBT(@Nonnull CompoundTag nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        target = nbt.getInt("target");
        if (nbt.contains("rotation")) { rotation = nbt.getInt("rotation"); }
        lastOutput = nbt.getInt("lastOutput");
        if (nbt.contains("ioMode")) { ioMode = nbt.getInt("ioMode"); }
        if (nbt.contains("redstoneChannel")) { redstoneChannel = DyeColor.byId(nbt.getInt("redstoneChannel")); }
        if (nbt.contains("output")) { outputClient = nbt.getInt("output"); }
    }

    @Override @Nonnull public Vec3 getConnectionOffset(ConnectionPoint here, ConnectionPoint other, WireType type) {
        Direction wireSide = getInputSide();
        double conRadius = type.getRenderDiameter() / 2;
        boolean onGround = getFacing().getAxis().isVertical();
        double offset = onGround ? (0.025 - conRadius) : (0.4 - conRadius);
        double yBase = onGround ? 0.95 : 0.5;
        return new Vec3(0.5 + wireSide.getStepX() * offset, yBase + wireSide.getStepY() * offset, 0.5 + wireSide.getStepZ() * offset);
    }

    @Override @Nonnull public Component[] getOverlayText(Player player, HitResult mop, boolean hammer) {
        if (!hammer) {
            ItemStack held = player.getMainHandItem();
            if (held.isEmpty() || !(held.getItem() instanceof ScrewdriverItem)) { return new Component[0]; }
        }
        float time = (float) this.target / 20;
        DyeColor color = redstoneChannel;
        Component channelInfo = Component.translatable(Lib.DESC_INFO + "redstoneChannel.send", color.getName());
        String ioKey = (ioMode == 0) ? "input" : "output";
        Component modeInfo = Component.translatable(Lib.DESC_INFO + "blockSide.io." + ioKey);
        Component delayInfo = Component.literal(String.format("%.1f Sec.", time));
        return new Component[]{channelInfo, modeInfo, delayInfo};
    }

    @Override @Nonnull public VoxelShape getBlockBounds(@Nullable CollisionContext context) {
        Direction facing = getFacing();
        return switch (facing) {
            case UP, DOWN -> Shapes.box(0.25, 0, 0.25, 0.75, 1, 0.75);
            case NORTH, SOUTH -> Shapes.box(0.25, 0, 0, 0.75, 0.75, 1);
            case EAST, WEST -> Shapes.box(0, 0.25, 0.25, 1, 0.75, 0.75);
        };
    }

    @Override public int getStrongRSOutput(Direction side) {
        Direction rsFace = getRSOutputFace();
        Direction facing = getFacing();
        boolean match = (ioMode == 1) && (side == rsFace || (!facing.getAxis().isVertical() && side == Direction.UP));
        if (!match) { return 0; }
        if (level != null && level.isClientSide) { return outputClient; }
        return lastOutput;
    }

    @Override public int getWeakRSOutput(Direction side) {
        Direction rsFace = getRSOutputFace();
        Direction facing = getFacing();
        boolean match = (ioMode == 1) && (side == rsFace || (!facing.getAxis().isVertical() && side == Direction.UP));
        if (!match) { return 0; }
        if (level != null && level.isClientSide) { return outputClient; }
        return lastOutput;
    }

    @Override public boolean canConnectRedstone(Direction side) {
        Direction rsFace = getRSOutputFace();
        Direction facing = getFacing();
        boolean result;
        if (facing.getAxis().isVertical()) {
            result = (side != null) && (side == rsFace);
        } else {
            result = (side != null) && (side == rsFace || side == Direction.UP);
        }
        return result;
    }

    @Override public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vec3i offset) {
        return WireType.REDSTONE_CATEGORY.equals(cableType.getCategory());
    }

    @Override @NotNull public Property<Direction> getFacingProperty() { return ModProperties.FACING_ALL; }

    @Override @NotNull public blusunrize.immersiveengineering.common.blocks.PlacementLimitation getFacingLimitation() {
        return blusunrize.immersiveengineering.common.blocks.PlacementLimitation.SIDE_CLICKED;
    }

    @Override public boolean mirrorFacingOnPlacement(@NotNull net.minecraft.world.entity.LivingEntity placer) { return true; }

    @Override public boolean canHammerRotate(@NotNull Direction side, @NotNull Vec3 hit, @NotNull net.minecraft.world.entity.LivingEntity entity) { return false; }

    @Override public void onChange(ConnectionPoint cp, RedstoneNetworkHandler handler) {
        if (level != null && !level.isClientSide && SafeChunkUtils.isChunkSafe(level, worldPosition)) {
            setChanged();
            markContainingBlockForUpdate(getBlockState());
            BlockPos offsetPos = worldPosition.relative(getRSOutputFace());
            level.updateNeighborsAt(offsetPos, level.getBlockState(offsetPos).getBlock());
            if (!getFacing().getAxis().isVertical()) {
                BlockPos upPos = worldPosition.relative(Direction.UP);
                level.updateNeighborsAt(upPos, level.getBlockState(upPos).getBlock());
            }
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
    }

    @Override public int getMaxRSInput() { return 15; }

    public boolean stillValid(Player player) {
        if (level == null || level.getBlockEntity(worldPosition) != this) { return false; }
        return player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return ConnectorTimerMenu.makeServer(MenuTypes.CONNECTOR_TIMER.getType(), id, inv, this);
    }

    @Override @NotNull public Component getDisplayName() {
        return Component.translatable(TranslationKey.GUI_TIMER.location);
    }
}
