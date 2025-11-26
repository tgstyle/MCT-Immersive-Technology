package mctmods.immersivetechnology.common.blocks.metal.logic;

import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.wires.*;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler.EnergyConnector;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler.IEnergyWire;
import com.google.common.collect.ImmutableList;
import mctmods.immersivetechnology.common.blocks.helper.ITProperties;
import mctmods.immersivetechnology.common.blocks.helper.ITBlockInterfaces;
import mctmods.immersivetechnology.common.blocks.helper.ITServerTickableBE;
import mctmods.immersivetechnology.common.blocks.metal.gui.ValveLoadMenu;
import mctmods.immersivetechnology.common.util.TranslationKey;
import mctmods.immersivetechnology.core.registration.ITBlockEntities;
import mctmods.immersivetechnology.core.registration.ITMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;

import static mctmods.immersivetechnology.common.blocks.metal.ValveLoadBlock.ROTATION;
import static mctmods.immersivetechnology.common.blocks.metal.ValveLoadBlock.OPEN;
import static net.minecraftforge.common.capabilities.ForgeCapabilities.ENERGY;

public class ValveLoadBlockEntity extends ValveCommonBlockEntity implements ITServerTickableBE, IImmersiveConnectable, EnergyConnector, ITBlockInterfaces.IMirrorAble {
    protected static final int RIGHT_INDEX = 0;
    protected static final int LEFT_INDEX = 1;
    protected WireType leftType;
    protected WireType rightType;
    private long bufferedEnergy = 0L;
    public int rotation = 0;
    private boolean isUnloading = false;

    public ValveLoadBlockEntity(BlockPos pos, BlockState state) { super(ITBlockEntities.VALVE_LOAD.get(), pos, state, TranslationKey.OVERLAY_OSD_VALVE_LOAD_NORMAL_FIRST_LINE, TranslationKey.OVERLAY_OSD_VALVE_LOAD_SNEAKING_FIRST_LINE, TranslationKey.OVERLAY_OSD_VALVE_LOAD_SNEAKING_SECOND_LINE, 1); }

    @Override public @Nonnull BlockState getState() { return getBlockState(); }

    @Override public void setState(@Nonnull BlockState state) { assert level != null; level.setBlock(worldPosition, state, 3); }

    @Override
    public void onLoad() {
        super.onLoad();
        assert level != null;
        facing = getBlockState().getValue(ITProperties.FACING_ALL);
        rotation = getBlockState().getValue(ROTATION);
        if (!level.isClientSide) {
            GlobalWireNetwork.getNetwork(level).onConnectorLoad(this, level);
            efficientSetChanged();
            for (Direction d : Direction.values()) { level.neighborChanged(worldPosition.relative(d), getBlockState().getBlock(), worldPosition); }
            markContainingBlockForUpdate(null);
            updateRedstoneState();
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && !level.isClientSide && !isUnloading) {
            GlobalWireNetwork global = GlobalWireNetwork.getNetwork(level);
            for (ConnectionPoint cp : getConnectionPoints()) {
                LocalWireNetwork net = global.getNullableLocalNet(cp);
                if (net != null) {
                    for (Connection conn : new ArrayList<>(net.getConnections(cp))) {
                        ConnectionPoint otherEnd = conn.getOtherEnd(cp);
                        if (net.getConnector(otherEnd) != null) {
                            if (level.isLoaded(otherEnd.position())) global.removeAndDropConnection(conn, worldPosition, level);
                            else global.removeConnection(conn);
                        }
                    }
                }
            }
            global.removeConnector(this);
        }
    }

    @Override
    public void onChunkUnloaded() {
        if (level != null && !level.isClientSide) {
            GlobalWireNetwork.getNetwork(level).onConnectorUnload(this);
            isUnloading = true;
        }
        super.onChunkUnloaded();
    }

    public void onNeighborBlockChange(BlockPos otherPos) { updateRedstoneState(); }

    @Override
    public void tickServer() {
        super.tickServer();
        assert level != null;
        long time = level.getGameTime();
        if (time % 12 == ((worldPosition.getX() ^ worldPosition.getZ()) & 11)) updateRedstoneState();
        if (!level.isClientSide && getBlockState().getValue(OPEN)) handleDirectTransfers();
    }

    private void handleDirectTransfers() {
        boolean inputWired = isSideWired(true);
        boolean outputWired = isSideWired(false);
        if (inputWired && outputWired) return;
        IEnergyStorage inputStorage = getInputEnergy();
        IEnergyStorage outputStorage = getOutputEnergy();
        if (!inputWired && inputStorage == null) return;
        if (!outputWired && outputStorage == null) return;
        int canAccept = getTransferLimit(outputWired, outputStorage);
        if (canAccept <= 0) return;
        int extracted;
        if (inputWired) {
            extracted = Math.min(canAccept, (int) Math.min(bufferedEnergy, Integer.MAX_VALUE));
            int inserted = outputStorage.receiveEnergy(extracted, false);
            bufferedEnergy -= inserted;
            acceptedAmount += inserted;
            packets++;
        } else if (outputWired) {
            extracted = inputStorage.extractEnergy(canAccept, false);
            bufferedEnergy += extracted;
            // Do not add to acceptedAmount here; it will be added in extractEnergy when the wire pulls
        } else {
            extracted = inputStorage.extractEnergy(canAccept, true);
            int inserted = outputStorage.receiveEnergy(extracted, false);
            inputStorage.extractEnergy(inserted, false);
            acceptedAmount += inserted;
            packets++;
        }
        efficientSetChanged();
    }

    private int getTransferLimit(boolean outputWired, IEnergyStorage outputStorage) {
        int canAccept = Integer.MAX_VALUE;
        canAccept = timeLimit > 0 ? Math.min(Math.max(timeLimit - longToInt(acceptedAmount), 0), canAccept) : canAccept;
        if (outputWired) {
            canAccept = keepSize > 0 ? Math.min(Math.max(keepSize - (int) bufferedEnergy, 0), canAccept) : canAccept;
            canAccept = Math.min(canAccept, getWireRate(false));
        } else {
            if (outputStorage == null) { canAccept = 0; }
            else { canAccept = keepSize > 0 ? Math.min(Math.max(keepSize - outputStorage.getEnergyStored(), 0), canAccept) : canAccept; }
        }
        canAccept = packetLimit > 0 ? Math.min(canAccept, packetLimit) : canAccept;
        if (redstoneMode > 0) canAccept = (int) (canAccept * ((redstoneMode == 1 ? 15 - getRSPower() : getRSPower()) / 15.0));
        boolean inputWired = isSideWired(true);
        if (inputWired) canAccept = Math.min(canAccept, getWireRate(true));
        return canAccept;
    }

    private int getWireRate(boolean isInput) {
        int index = isInput ? getInputWireIndex() : getOutputWireIndex();
        WireType wt = index == LEFT_INDEX ? leftType : rightType;
        if (wt instanceof IEnergyWire ew) return ew.getTransferRate();
        return Integer.MAX_VALUE;
    }

    private int getInputWireIndex() {
        boolean mirrored = getIsMirrored();
        boolean flip = !facing.getAxis().isVertical();
        int index = mirrored ? LEFT_INDEX : RIGHT_INDEX;
        if (flip) index = 1 - index;
        return index;
    }

    private int getOutputWireIndex() {
        boolean mirrored = getIsMirrored();
        boolean flip = !facing.getAxis().isVertical();
        int index = mirrored ? RIGHT_INDEX : LEFT_INDEX;
        if (flip) index = 1 - index;
        return index;
    }

    private boolean isSideWired(boolean isInput) {
        boolean mirrored = getIsMirrored();
        boolean flip = !facing.getAxis().isVertical();
        int index = isInput ? (mirrored ? LEFT_INDEX : RIGHT_INDEX) : (mirrored ? RIGHT_INDEX : LEFT_INDEX);
        if (flip) index = 1 - index;
        return index == LEFT_INDEX ? leftType != null : rightType != null;
    }

    private Direction getPortDirection() {
        boolean isVertical = facing.getAxis().isVertical();
        Direction base = isVertical ? Direction.from2DDataValue(rotation) : facing;
        return isVertical ? base : facing.getClockWise();
    }

    private Direction getInputDir() {
        Direction portDir = getPortDirection();
        boolean reverse = !facing.getAxis().isVertical();
        return (getIsMirrored() ^ reverse) ? portDir.getOpposite() : portDir;
    }

    private Direction getOutputDir() { return getInputDir().getOpposite(); }

    public IEnergyStorage getInputEnergy() {
        assert level != null;
        Direction inputDir = getInputDir();
        BlockPos srcPos = worldPosition.relative(inputDir);
        BlockEntity src = level.getBlockEntity(srcPos);
        if (src != null) {
            LazyOptional<IEnergyStorage> cap = src.getCapability(ENERGY, inputDir.getOpposite());
            return cap.resolve().orElse(null);
        }
        return null;
    }

    public IEnergyStorage getOutputEnergy() {
        assert level != null;
        Direction outputDir = getOutputDir();
        BlockPos dstPos = worldPosition.relative(outputDir);
        BlockEntity dst = level.getBlockEntity(dstPos);
        if (dst != null) {
            LazyOptional<IEnergyStorage> cap = dst.getCapability(ENERGY, outputDir.getOpposite());
            return cap.resolve().orElse(null);
        }
        return null;
    }

    @NotNull
    protected LocalWireNetwork getLocalNet(int cpIndex) {
        assert level != null;
        return GlobalWireNetwork.getNetwork(level).getLocalNet(new ConnectionPoint(worldPosition, cpIndex));
    }

    @Override public boolean canConnect() { return true; }

    @Override
    public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vec3i offset) {
        if (!offset.equals(Vec3i.ZERO)) return false;
        WireType atConn = target.index() == LEFT_INDEX ? leftType : rightType;
        return canAttach(cableType, atConn);
    }

    protected boolean canAttach(WireType toAttach, @Nullable WireType atConn) { return atConn == null || toAttach.equals(atConn); }

    @Override
    public void connectCable(WireType cableType, ConnectionPoint target, IImmersiveConnectable other, ConnectionPoint otherTarget) {
        if (target.index() == LEFT_INDEX) leftType = cableType; else rightType = cableType;
        updateMirrorState();
        markContainingBlockForUpdate(null);
    }

    @Override
    public void removeCable(Connection connection, ConnectionPoint attachedPoint) {
        WireType type = connection != null ? connection.type : null;
        if (type == null) { leftType = rightType = null; }
        else { if (attachedPoint.index() == LEFT_INDEX) leftType = null; else rightType = null; }
        updateMirrorState();
        this.markContainingBlockForUpdate(null);
    }

    protected void updateMirrorState() {
        if (leftType != null && rightType != null) {
            int leftL = getLevel(leftType.getCategory());
            int rightL = getLevel(rightType.getCategory());
            if (leftL != rightL) {
                setMirrored(rightL > leftL);
            }
        }
    }

    private int getLevel(String cat) {
        if (WireType.LV_CATEGORY.equals(cat)) return 1;
        if (WireType.MV_CATEGORY.equals(cat)) return 2;
        if (WireType.HV_CATEGORY.equals(cat)) return 3;
        return 0;
    }

    @Override
    public Vec3 getConnectionOffset(ConnectionPoint here, ConnectionPoint other, WireType type) { return getConnectionOffset(type, here.index() == RIGHT_INDEX); }

    protected Vec3 getConnectionOffset(WireType type, boolean right) {
        double conRadius = type.getRenderDiameter() / 2;
        Direction facing = getFacing();
        boolean isVertical = facing.getAxis().isVertical();
        Direction hFacing = isVertical ? Direction.from2DDataValue(rotation) : facing;
        boolean mirrored = getIsMirrored();
        if (mirrored) right = !right;
        Direction perpDir = isVertical ? hFacing : facing.getClockWise();
        boolean perpPositive = perpDir.getAxisDirection() == Direction.AxisDirection.POSITIVE;
        double high = 0.8125;
        double low = 0.1875;
        double perpOff = right ? low : high;
        if (perpPositive) perpOff = 1 - perpOff;
        Direction.Axis perpAxis = perpDir.getAxis();
        boolean alongPositive = facing.getAxisDirection() == Direction.AxisDirection.POSITIVE;
        double alongOff = alongPositive ? 1 - conRadius : conRadius;
        double xOff = 0.5;
        double yOff = 0.5;
        double zOff = 0.5;
        Direction.Axis alongAxis = facing.getAxis();
        switch (alongAxis) {
            case X: xOff = alongOff; break;
            case Y: yOff = alongOff; break;
            case Z: zOff = alongOff; break;
        }
        switch (perpAxis) {
            case X: xOff = perpOff; break;
            case Y: yOff = perpOff; break;
            case Z: zOff = perpOff; break;
        }
        if (!isVertical) yOff = 0.5;
        return new Vec3(xOff, yOff, zOff);
    }

    @Override
    public Collection<ConnectionPoint> getConnectionPoints() { return ImmutableList.of(new ConnectionPoint(worldPosition, LEFT_INDEX), new ConnectionPoint(worldPosition, RIGHT_INDEX)); }

    @Override
    public Iterable<? extends Connection> getInternalConnections() { return ImmutableList.of(); }

    @Override
    public @Nullable ConnectionPoint getTargetedPoint(TargetingInfo target, Vec3i offset) {
        if (!offset.equals(Vec3i.ZERO)) return null;
        ConnectionPoint leftCP = new ConnectionPoint(worldPosition, LEFT_INDEX);
        ConnectionPoint rightCP = new ConnectionPoint(worldPosition, RIGHT_INDEX);
        boolean leftEmpty = getLocalNet(LEFT_INDEX).getConnections(leftCP).stream().allMatch(Connection::isInternal);
        boolean rightEmpty = getLocalNet(RIGHT_INDEX).getConnections(rightCP).stream().allMatch(Connection::isInternal);
        if (leftEmpty && !rightEmpty) return leftCP;
        else if (!leftEmpty && rightEmpty) return rightCP;
        Direction facing = getFacing();
        boolean isVertical = facing.getAxis().isVertical();
        Direction hFacing = isVertical ? Direction.from2DDataValue(rotation) : facing;
        Direction perpDir = isVertical ? hFacing : facing.getClockWise();
        double hitPos = getHitPos(target, perpDir);
        if (hitPos < .5) return leftCP;
        else return rightCP;
    }

    private double getHitPos(TargetingInfo target, Direction perpDir) {
        Direction.Axis perpAxis = perpDir.getAxis();
        double hitPos = perpAxis == Direction.Axis.X ? target.hitX : perpAxis == Direction.Axis.Y ? target.hitY : target.hitZ;
        boolean positive = perpDir.getAxisDirection() == Direction.AxisDirection.POSITIVE;
        if (!positive) hitPos = 1 - hitPos;
        return hitPos;
    }

    @Override
    public BlockPos getConnectionMaster(@Nullable WireType cableType, TargetingInfo target) { return worldPosition; }

    @Override
    public BlockPos getPosition() { return worldPosition; }

    @Override
    public Collection<ResourceLocation> getRequestedHandlers() { return ImmutableList.of(EnergyTransferHandler.ID); }

    @Override
    public void readCustomNBT(@NotNull CompoundTag nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        if (nbt.contains("leftType")) leftType = WireType.getValue(nbt.getString("leftType")); else leftType = null;
        if (nbt.contains("rightType")) rightType = WireType.getValue(nbt.getString("rightType")); else rightType = null;
        rotation = nbt.getInt("rotation");
        bufferedEnergy = nbt.getLong("bufferedEnergy");
        updateMirrorState();
    }

    @Override
    public void writeCustomNBT(CompoundTag nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        if (leftType != null) nbt.putString("leftType", leftType.getUniqueName());
        if (rightType != null) nbt.putString("rightType", rightType.getUniqueName());
        nbt.putInt("rotation", rotation);
        nbt.putLong("bufferedEnergy", bufferedEnergy);
    }

    @Override
    public void setMirrored(boolean mirrored) {
        BlockState state = getBlockState();
        if (state.getValue(ITProperties.MIRRORED) != mirrored) {
            assert level != null;
            level.setBlock(worldPosition, state.setValue(ITProperties.MIRRORED, mirrored), 3);
        }
    }

    @Override public boolean getIsMirrored() { return getBlockState().getValue(ITProperties.MIRRORED); }

    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) { return ValveLoadMenu.makeServer(ITMenuTypes.VALVE_LOAD.getType(), id, inv, this); }

    @Override public @NotNull Component getDisplayName() { return Component.translatable(TranslationKey.GUI_VALVE_LOAD.location); }

    @Override public boolean stillValid(Player player) { return !isRemoved() && player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D) <= 64.0D; }

    @Override
    public boolean hammerUseSide(@NotNull Direction side, @NotNull Player player, @NotNull InteractionHand hand, @NotNull Vec3 hit) {
        assert level != null;
        if (level.isClientSide) return false;
        if (leftType != null || rightType != null) return false;
        boolean counter = player.isShiftKeyDown() != (side == Direction.DOWN);
        Direction oldFacing = facing;
        Direction newFacing = counter ? oldFacing.getCounterClockWise(side.getAxis()) : oldFacing.getClockWise(side.getAxis());
        setFacing(newFacing);
        return true;
    }

    @Override
    public boolean isSource(ConnectionPoint cp) {
        boolean mirrored = getIsMirrored();
        boolean flip = !facing.getAxis().isVertical();
        int outputIndex = mirrored ? RIGHT_INDEX : LEFT_INDEX;
        if (flip) outputIndex = 1 - outputIndex;
        return cp.index() == outputIndex;
    }

    @Override
    public boolean isSink(ConnectionPoint cp) {
        boolean mirrored = getIsMirrored();
        boolean flip = !facing.getAxis().isVertical();
        int inputIndex = mirrored ? LEFT_INDEX : RIGHT_INDEX;
        if (flip) inputIndex = 1 - inputIndex;
        return cp.index() == inputIndex;
    }

    @Override public int getAvailableEnergy() {
        if (!getBlockState().getValue(OPEN)) return 0;
        long avail = bufferedEnergy;
        boolean outputWired = isSideWired(false);
        if (outputWired) avail = Math.min(avail, getWireRate(false));
        return (int) Math.min(avail, Integer.MAX_VALUE);
    }

    @Override
    public void extractEnergy(int amount) {
        if (amount > 0) {
            long drain = Math.min(amount, bufferedEnergy);
            bufferedEnergy -= drain;
            acceptedAmount += drain;
            packets++;
        }
        efficientSetChanged();
    }

    @Override
    public int getRequestedEnergy() {
        if (!getBlockState().getValue(OPEN)) return 0;
        boolean outputWired = isSideWired(false);
        IEnergyStorage outputStorage = outputWired ? null : getOutputEnergy();
        int req = getTransferLimit(outputWired, outputStorage);
        boolean inputWired = isSideWired(true);
        if (inputWired) req = Math.min(req, getWireRate(true));
        return req;
    }

    @Override
    public void insertEnergy(int amount) {
        int max = timeLimit > 0 ? Math.max(timeLimit - longToInt(acceptedAmount), 0) : amount;
        amount = Math.min(amount, max);
        bufferedEnergy += amount;
        efficientSetChanged();
    }

    @Override public void onEnergyPassedThrough(double amount) {
        acceptedAmount += (long)amount;
        packets++;
    }
}
