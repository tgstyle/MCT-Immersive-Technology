package mctmods.immersivetechnology.common.blocks.connectors.logic;

import com.immersiveconvergence.api.block.BlockToolGates;
import com.immersiveconvergence.api.energy.ICConnectionPoint;
import com.immersiveconvergence.api.energy.ICConnectorRedstoneBlockEntity;
import com.immersiveconvergence.api.energy.ICWireType;
import com.immersiveconvergence.api.multiblock.util.SafeChunkUtils;
import com.immersiveconvergence.core.lib.ICLib;
import mctmods.immersivetechnology.common.blocks.connectors.gui.ConnectorTimerMenu;
import com.immersiveconvergence.api.block.BlockInterfaces;
import com.immersiveconvergence.api.block.IServerTickableBE;
import mctmods.immersivetechnology.core.registration.BlockEntities;
import mctmods.immersivetechnology.core.registration.MenuTypes;
import mctmods.immersivetechnology.core.util.Reach;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import static mctmods.immersivetechnology.common.blocks.connectors.ConnectorTimerBlock.ROTATION;
import com.immersiveconvergence.api.network.ITileSyncReceiver;

import javax.annotation.Nonnull;

public class ConnectorTimerBlockEntity extends ICConnectorRedstoneBlockEntity implements IServerTickableBE, BlockInterfaces.IRedstoneInputOutput, BlockInterfaces.IScrewdriverInteraction,BlockInterfaces.IBlockOverlayText, MenuProvider , ITileSyncReceiver {
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
            currentInput = redstoneNetworkValue(redstoneChannel.getId());
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
            redstoneChanged();
        }
    }

    @Override public final void tickServer() {
        if (rsDirty) { updateRedstoneNetwork(); }
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

    @Override public void redstoneInput(byte[] signals, @Nonnull ICConnectionPoint cp) {
        if (isRSInput() || isRSOutput()) { signals[redstoneChannel.getId()] = (byte) Math.max(lastOutput, signals[redstoneChannel.getId()]); }
        rsDirty = false;
    }

    @Override @Nonnull public InteractionResult screwdriverUseSide(@Nonnull Direction side, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull Vec3 hitVec) {
        if (level != null && level.isClientSide) { return InteractionResult.SUCCESS; }
        if (player.isShiftKeyDown()) { redstoneChannel = DyeColor.byId((redstoneChannel.getId() + 1) % 16); }
        else { ioMode = (ioMode == 0) ? 1 : 0; }
        setChanged();
        updateRedstoneNetwork();
        redstoneChanged();
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

    @Override public void receiveMessageFromClient(CompoundTag nbt) {
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
            updateRedstoneNetwork();
            redstoneChanged();
        }
        if (needsBlockUpdate) {
            markContainingBlockForUpdate(null);
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                level.blockEvent(getBlockPos(), getBlockState().getBlock(), 254, 0);
            }
        }
    }

    @Override public void writeCustomNBT(@Nonnull CompoundTag nbt, boolean descPacket) {
        nbt.putInt("target", target);
        nbt.putInt("rotation", rotation);
        nbt.putInt("lastOutput", lastOutput);
        nbt.putInt("ioMode", ioMode);
        nbt.putInt("redstoneChannel", redstoneChannel.getId());
        nbt.putInt("output", lastOutput);
    }

    @Override public void readCustomNBT(@Nonnull CompoundTag nbt, boolean descPacket) {
        target = nbt.getInt("target");
        if (nbt.contains("rotation")) { rotation = nbt.getInt("rotation"); }
        lastOutput = nbt.getInt("lastOutput");
        if (nbt.contains("ioMode")) { ioMode = nbt.getInt("ioMode"); }
        if (nbt.contains("redstoneChannel")) { redstoneChannel = DyeColor.byId(nbt.getInt("redstoneChannel")); }
        if (nbt.contains("output")) { outputClient = nbt.getInt("output"); }
    }

    @Override @Nonnull public Vec3 connectionOffset(@Nonnull ICConnectionPoint here, @Nonnull ICWireType cable) {
        Direction wireSide = getInputSide();
        double conRadius = cable.getRenderRadius();
        boolean onGround = getFacing().getAxis().isVertical();
        double offset = onGround ? (0.025 - conRadius) : (0.4 - conRadius);
        double yBase = onGround ? 0.95 : 0.5;
        return new Vec3(0.5 + wireSide.getStepX() * offset, yBase + wireSide.getStepY() * offset, 0.5 + wireSide.getStepZ() * offset);
    }

    @Override @Nonnull public Component[] getOverlayText(Player player, HitResult mop, boolean hammer) {
        if (!hammer) {
            ItemStack held = player.getMainHandItem();
            if (held.isEmpty() || !BlockToolGates.isScrewdriver.test(held)) { return new Component[0]; }
        }
        float time = (float) this.target / 20;
        DyeColor color = redstoneChannel;
        Component channelInfo = Component.translatable(ICLib.DESC_INFO + "redstoneChannel.send", color.getName());
        String ioKey = (ioMode == 0) ? "input" : "output";
        Component modeInfo = Component.translatable(ICLib.DESC_INFO + "blockSide.io." + ioKey);
        Component delayInfo = Component.literal(String.format("%.1f Sec.", time));
        return new Component[]{channelInfo, modeInfo, delayInfo};
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

    @Override public boolean acceptsCable(@Nonnull ICWireType cable, @Nonnull ICConnectionPoint target, @Nonnull Vec3i offset) {
        return ICWireType.REDSTONE_CATEGORY.equals(cable.getCategory());
    }

    @Nonnull public Direction getFacing() { return getBlockState().getValue(BlockStateProperties.FACING); }

    @Override public void redstoneChanged() {
        if (level != null && !level.isClientSide && !SafeChunkUtils.isChunkUnsafe(level, worldPosition)) {
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

    public boolean stillValid(Player player) { return level != null && level.getBlockEntity(worldPosition) == this && Reach.within(this, player); }

    @Override public AbstractContainerMenu createMenu(int id, @Nonnull Inventory inv, @Nonnull Player player) {
        return ConnectorTimerMenu.makeServer(MenuTypes.CONNECTOR_TIMER.getType(), id, inv, this);
    }

    @Override @Nonnull public Component getDisplayName() {
        return Component.translatable(TranslationKey.GUI_TIMER.location);
    }
}
