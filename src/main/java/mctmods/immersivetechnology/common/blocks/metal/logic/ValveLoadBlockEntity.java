package mctmods.immersivetechnology.common.blocks.metal.logic;

import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler.EnergyConnector;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

import java.util.Collection;

import static mctmods.immersivetechnology.common.blocks.metal.ValveLoadBlock.ROTATION;

import static mctmods.immersivetechnology.common.blocks.metal.ValveLoadBlock.OPEN;

public class ValveLoadBlockEntity extends ValveCommonBlockEntity implements ITServerTickableBE, IImmersiveConnectable, EnergyConnector, ITBlockInterfaces.IMirrorAble {
    protected static final int RIGHT_INDEX = 0;
    protected static final int LEFT_INDEX = 1;
    protected WireType leftType;
    protected WireType rightType;
    private int bufferedEnergy = 0;
    public int rotation = 0;

    public ValveLoadBlockEntity(BlockPos pos, BlockState state) { super(ITBlockEntities.VALVE_LOAD.get(), pos, state, TranslationKey.OVERLAY_OSD_VALVE_LOAD_NORMAL_FIRST_LINE, TranslationKey.OVERLAY_OSD_VALVE_LOAD_SNEAKING_FIRST_LINE, TranslationKey.OVERLAY_OSD_VALVE_LOAD_SNEAKING_SECOND_LINE, 1); }

    @Override public @Nonnull BlockState getState() { return getBlockState(); }

    @Override public void setState(@Nonnull BlockState state) { assert level != null; level.setBlock(worldPosition, state, 3); }

    @Override
    public void onLoad() {
        super.onLoad();
        assert level != null;
        if (!level.isClientSide) {
            efficientSetChanged();
            for (Direction d : Direction.values()) { level.neighborChanged(worldPosition.relative(d), getBlockState().getBlock(), worldPosition); }
            markContainingBlockForUpdate(null);
            updateRedstoneState();
        }
        facing = getBlockState().getValue(ITProperties.FACING_ALL);
        rotation = getBlockState().getValue(ROTATION);
    }

    public void onNeighborBlockChange(BlockPos otherPos) { updateRedstoneState(); }

    @Override
    public void tickServer() {
        super.tickServer();
        assert level != null;
        long time = level.getGameTime();
        if (time % 12 == ((worldPosition.getX() ^ worldPosition.getZ()) & 11)) updateRedstoneState();
        if (time % 20 == 0) { acceptedAmount = 0; packets = 0; }
    }

    @NotNull
    protected LocalWireNetwork getLocalNet(int cpIndex) {
        assert level != null;
        return GlobalWireNetwork.getNetwork(level).getLocalNet(new ConnectionPoint(worldPosition, cpIndex));
    }

    @Override
    public boolean canConnect() { return true; }

    @Override
    public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vec3i offset) {
        if (!offset.equals(Vec3i.ZERO)) return false;
        WireType atConn = target.index() == LEFT_INDEX ? leftType : rightType;
        WireType other = target.index() == LEFT_INDEX ? rightType : leftType;
        return canAttach(cableType, atConn, other);
    }

    @SuppressWarnings("unused")
    protected boolean canAttach(WireType toAttach, @Nullable WireType atConn, @Nullable WireType other) { return atConn == null; }

    @Override
    public void connectCable(WireType cableType, ConnectionPoint target, IImmersiveConnectable other, ConnectionPoint otherTarget) {
        if (target.index() == LEFT_INDEX) leftType = cableType; else rightType = cableType;
        updateMirrorState();
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
        if (rightType != null || leftType != null) {
            String higher = getHigherWiretype();
            boolean intendedState = (rightType != null && higher.equals(rightType.getCategory())) || (leftType != null && !higher.equals(leftType.getCategory()));
            setMirrored(intendedState);
        }
    }

    protected String getHigherWiretype() {
        if (leftType == null) return rightType == null ? WireType.LV_CATEGORY : rightType.getCategory();
        if (rightType == null) return leftType.getCategory();
        int left = getLevel(leftType.getCategory());
        int right = getLevel(rightType.getCategory());
        return left > right ? leftType.getCategory() : rightType.getCategory();
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
        double offset = getHigherWiretype().equals(type.getCategory()) ? getHigherOffset() : getLowerOffset();
        Direction facing = getFacing();
        boolean isVertical = facing.getAxis().isVertical();
        Direction hFacing = isVertical ? Direction.from2DDataValue(rotation) : facing;
        boolean mirrored = getIsMirrored();
        if (mirrored) right = !right;
        double yOff;
        if (isVertical) {
            if (facing == Direction.UP) { yOff = offset - conRadius; } else { yOff = 1 - offset + conRadius; }
        } else { yOff = offset - conRadius; }
        double xOff = 0.5;
        double zOff = 0.5;
        switch (hFacing) {
            case NORTH -> xOff = right ? .8125 : .1875;
            case SOUTH -> xOff = right ? .1875 : .8125;
            case WEST -> zOff = right ? .1875 : .8125;
            case EAST -> zOff = right ? .8125 : .1875;
        }
        return new Vec3(xOff, yOff, zOff);
    }

    protected float getLowerOffset() { return 0.9375F; }

    protected float getHigherOffset() { return 1F; }

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
        double hitPos;
        if (hFacing.getAxis() == Direction.Axis.X) hitPos = target.hitZ;
        else hitPos = 1 - target.hitX;
        boolean positive = hFacing.getAxisDirection() == Direction.AxisDirection.POSITIVE;
        if ((hitPos < .5) == positive) return leftCP;
        else return rightCP;
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
    }

    @Override
    public void writeCustomNBT(CompoundTag nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        if (leftType != null) nbt.putString("leftType", leftType.getUniqueName());
        if (rightType != null) nbt.putString("rightType", rightType.getUniqueName());
        nbt.putInt("rotation", rotation);
    }

    @Override
    public void setMirrored(boolean mirrored) {
        BlockState state = getBlockState();
        if (state.getValue(ITProperties.MIRRORED) != mirrored) {
            assert level != null;
            level.setBlock(worldPosition, state.setValue(ITProperties.MIRRORED, mirrored), 3);
        }
    }

    @Override
    public boolean getIsMirrored() { return getBlockState().getValue(ITProperties.MIRRORED); }

    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) { return ValveLoadMenu.makeServer(ITMenuTypes.VALVE_LOAD.getType(), id, inv, this); }

    @Override
    public @NotNull Component getDisplayName() { return Component.translatable(TranslationKey.GUI_VALVE_LOAD.location); }

    @Override
    public boolean stillValid(Player player) { return !isRemoved() && player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D) <= 64.0D; }

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
        int outputIndex = mirrored ? LEFT_INDEX : RIGHT_INDEX;
        return cp.index() == outputIndex;
    }

    @Override
    public boolean isSink(ConnectionPoint cp) {
        boolean mirrored = getIsMirrored();
        int inputIndex = mirrored ? RIGHT_INDEX : LEFT_INDEX;
        return cp.index() == inputIndex;
    }

    @Override
    public int getAvailableEnergy() { return bufferedEnergy; }

    @Override
    public void extractEnergy(int amount) { bufferedEnergy -= amount; if (bufferedEnergy < 0) bufferedEnergy = 0; }

    @Override
    public int getRequestedEnergy() {
        if (!getBlockState().getValue(OPEN)) return 0;
        int canAccept = Integer.MAX_VALUE;
        canAccept = timeLimit > 0 ? Math.min(Math.max(timeLimit - longToInt(acceptedAmount), 0), canAccept) : canAccept;
        canAccept = keepSize > 0 ? Math.min(Math.max(keepSize - bufferedEnergy, 0), canAccept) : canAccept;
        canAccept = packetLimit > 0 ? Math.min(canAccept, packetLimit) : canAccept;
        if (redstoneMode > 0) canAccept = (int) (canAccept * ((redstoneMode == 1 ? 15 - getRSPower() : getRSPower()) / 15.0));
        return canAccept;
    }

    @Override
    public void insertEnergy(int amount) {
        int max = timeLimit > 0 ? Math.max(timeLimit - longToInt(acceptedAmount), 0) : amount;
        amount = Math.min(amount, max);
        bufferedEnergy += amount;
        acceptedAmount += amount;
        packets++;
    }

    @Override
    public void onEnergyPassedThrough(double amount) { }
}
