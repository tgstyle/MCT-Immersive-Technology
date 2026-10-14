package mctmods.immersivetechnology.common.blocks.metal.logic;

import com.immersiveconvergence.api.block.ModProperties;
import com.immersiveconvergence.api.block.BlockInterfaces;
import com.immersiveconvergence.api.block.IServerTickableBE;
import com.immersiveconvergence.api.energy.ICConnectionPoint;
import com.immersiveconvergence.api.energy.ICTargetingInfo;
import com.immersiveconvergence.api.energy.ICWireType;
import com.immersiveconvergence.api.energy.IICEnergyConnector;
import mctmods.immersivetechnology.common.blocks.metal.gui.ValveLoadMenu;
import mctmods.immersivetechnology.core.util.TranslationKey;
import mctmods.immersivetechnology.core.registration.BlockEntities;
import mctmods.immersivetechnology.core.registration.MenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.Collection;
import java.util.List;

import static mctmods.immersivetechnology.common.blocks.metal.ValveLoadBlock.ROTATION;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ValveLoadBlockEntity extends ValveCommonBlockEntity implements IServerTickableBE, IICEnergyConnector, BlockInterfaces.IMirrorAble {
    protected static final int RIGHT_INDEX = 0;
    protected static final int LEFT_INDEX = 1;
    protected ICWireType leftType;
    protected ICWireType rightType;
    private long bufferedEnergy = 0L;
    public int rotation = 0;

    public ValveLoadBlockEntity(BlockPos pos, BlockState state) { super(BlockEntities.VALVE_LOAD.get(), pos, state, TranslationKey.OVERLAY_OSD_VALVE_LOAD_NORMAL_FIRST_LINE, TranslationKey.OVERLAY_OSD_VALVE_LOAD_SNEAKING_FIRST_LINE, TranslationKey.OVERLAY_OSD_VALVE_LOAD_SNEAKING_SECOND_LINE, 1); }

    @Override @Nonnull public BlockState getState() { return getBlockState(); }

    @Override public void setState(@Nonnull BlockState state) { if (level != null) level.setBlock(worldPosition, state, 3); }

    @Override public void onLoad() {
        super.onLoad();
        facing = getBlockState().getValue(ModProperties.FACING_ALL);
        rotation = getBlockState().getValue(ROTATION);
        if (level != null && !level.isClientSide) {
            efficientSetChanged();
            for (Direction d : Direction.values()) { level.neighborChanged(worldPosition.relative(d), getBlockState().getBlock(), worldPosition); }
            markContainingBlockForUpdate(null);
            updateRedstoneState();
        }
    }

    public void onNeighborBlockChange(BlockPos otherPos) { updateRedstoneState(); }

    @Override public void tickServer() {
        super.tickServer();
        if (level == null) return;
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
        canAccept = timeLimit > 0 ? Math.clamp(timeLimit - longToInt(acceptedAmount), 0, canAccept) : canAccept;
        if (outputWired) {
            canAccept = keepSize > 0 ? Math.clamp(keepSize - (int) bufferedEnergy, 0, canAccept) : canAccept;
            canAccept = Math.min(canAccept, getWireRate(false));
        } else {
            if (outputStorage == null) { canAccept = 0; }
            else { canAccept = keepSize > 0 ? Math.clamp(keepSize - outputStorage.getEnergyStored(), 0, canAccept) : canAccept; }
        }
        canAccept = packetLimit > 0 ? Math.min(canAccept, packetLimit) : canAccept;
        if (redstoneMode > 0) canAccept = (int) (canAccept * ((redstoneMode == 1 ? 15 - getRSPower() : getRSPower()) / 15.0));
        boolean inputWired = isSideWired(true);
        if (inputWired) canAccept = Math.min(canAccept, getWireRate(true));
        return canAccept;
    }

    private int getWireRate(boolean isInput) {
        int index = isInput ? getInputWireIndex() : getOutputWireIndex();
        ICWireType wt = index == LEFT_INDEX ? leftType : rightType;
        return wt != null ? wt.getTransferRate() : Integer.MAX_VALUE;
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
        if (level == null) return null;
        Direction inputDir = getInputDir();
        BlockPos srcPos = worldPosition.relative(inputDir);
        return level.getCapability(Capabilities.EnergyStorage.BLOCK, srcPos, inputDir.getOpposite());
    }

    public IEnergyStorage getOutputEnergy() {
        if (level == null) return null;
        Direction outputDir = getOutputDir();
        BlockPos dstPos = worldPosition.relative(outputDir);
        return level.getCapability(Capabilities.EnergyStorage.BLOCK, dstPos, outputDir.getOpposite());
    }

    @Override public boolean canConnect() { return true; }

    @Override public boolean acceptsCable(@Nonnull ICWireType cable, @Nonnull ICConnectionPoint target, @Nonnull Vec3i offset) {
        if (!offset.equals(Vec3i.ZERO)) return false;
        ICWireType atConn = target.index() == LEFT_INDEX ? leftType : rightType;
        return canAttach(cable, atConn);
    }

    protected boolean canAttach(ICWireType toAttach, @Nullable ICWireType atConn) { return atConn == null || toAttach.equals(atConn); }

    @Override public void cableConnected(@Nonnull ICWireType cable, @Nonnull ICConnectionPoint target) {
        if (target.index() == LEFT_INDEX) leftType = cable; else rightType = cable;
        updateMirrorState();
        markContainingBlockForUpdate(null);
    }

    @Override public void cableRemoved(@Nullable ICWireType type, @Nonnull ICConnectionPoint attachedPoint) {
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
        if (ICWireType.LV_CATEGORY.equals(cat)) return 1;
        if (ICWireType.MV_CATEGORY.equals(cat)) return 2;
        if (ICWireType.HV_CATEGORY.equals(cat)) return 3;
        return 0;
    }

    @Override @Nonnull public Vec3 connectionOffset(@Nonnull ICConnectionPoint here, @Nonnull ICConnectionPoint other, @Nonnull ICWireType cable) { return getConnectionOffset(cable, here.index() == RIGHT_INDEX); }

    protected Vec3 getConnectionOffset(ICWireType type, boolean right) {
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

    @Override @Nonnull public Collection<ICConnectionPoint> connectionPoints() { return List.of(new ICConnectionPoint(worldPosition, LEFT_INDEX), new ICConnectionPoint(worldPosition, RIGHT_INDEX)); }

    @Override @Nullable public ICConnectionPoint targetedPoint(@Nonnull ICTargetingInfo target, @Nonnull Vec3i offset) {
        if (!offset.equals(Vec3i.ZERO)) return null;
        ICConnectionPoint leftCP = new ICConnectionPoint(worldPosition, LEFT_INDEX);
        ICConnectionPoint rightCP = new ICConnectionPoint(worldPosition, RIGHT_INDEX);
        boolean leftEmpty = !hasExternalConnections(leftCP);
        boolean rightEmpty = !hasExternalConnections(rightCP);
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

    private double getHitPos(ICTargetingInfo target, Direction perpDir) {
        Direction.Axis perpAxis = perpDir.getAxis();
        double hitPos = perpAxis == Direction.Axis.X ? target.hitX() : perpAxis == Direction.Axis.Y ? target.hitY() : target.hitZ();
        boolean positive = perpDir.getAxisDirection() == Direction.AxisDirection.POSITIVE;
        if (!positive) hitPos = 1 - hitPos;
        return hitPos;
    }

    @Override public void readCustomNBT(@Nonnull CompoundTag nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        if (nbt.contains("leftType")) leftType = ICWireType.byName(nbt.getString("leftType")); else leftType = null;
        if (nbt.contains("rightType")) rightType = ICWireType.byName(nbt.getString("rightType")); else rightType = null;
        rotation = nbt.getInt("rotation");
        bufferedEnergy = nbt.getLong("bufferedEnergy");
        updateMirrorState();
    }

    @Override public void writeCustomNBT(CompoundTag nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        if (leftType != null) nbt.putString("leftType", leftType.getUniqueName());
        if (rightType != null) nbt.putString("rightType", rightType.getUniqueName());
        nbt.putInt("rotation", rotation);
        nbt.putLong("bufferedEnergy", bufferedEnergy);
    }

    @Override public void setMirrored(boolean mirrored) {
        BlockState state = getBlockState();
        if (state.getValue(ModProperties.MIRRORED) != mirrored) {
            if (level != null) level.setBlock(worldPosition, state.setValue(ModProperties.MIRRORED, mirrored), 3);
        }
    }

    @Override public boolean getIsMirrored() { return getBlockState().getValue(ModProperties.MIRRORED); }

    @Override public AbstractContainerMenu createMenu(int id, @Nonnull Inventory inv, @Nonnull Player player) { return ValveLoadMenu.makeServer(MenuTypes.VALVE_LOAD.getType(), id, inv, this); }

    @Override @Nonnull public Component getDisplayName() { return Component.translatable(TranslationKey.GUI_VALVE_LOAD.location); }

    @Override public boolean hammerUseSide(@Nonnull Direction side, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull Vec3 hit) {
        if (level == null || level.isClientSide) return false;
        if (leftType != null || rightType != null) return false;
        boolean counter = player.isShiftKeyDown() != (side == Direction.DOWN);
        Direction oldFacing = facing;
        Direction newFacing = counter ? oldFacing.getCounterClockWise(side.getAxis()) : oldFacing.getClockWise(side.getAxis());
        setFacing(newFacing);
        return true;
    }

    @Override public boolean isEnergySource(ICConnectionPoint cp) {
        boolean mirrored = getIsMirrored();
        boolean flip = !facing.getAxis().isVertical();
        int outputIndex = mirrored ? RIGHT_INDEX : LEFT_INDEX;
        if (flip) outputIndex = 1 - outputIndex;
        return cp.index() == outputIndex;
    }

    @Override public boolean isEnergySink(ICConnectionPoint cp) {
        boolean mirrored = getIsMirrored();
        boolean flip = !facing.getAxis().isVertical();
        int inputIndex = mirrored ? LEFT_INDEX : RIGHT_INDEX;
        if (flip) inputIndex = 1 - inputIndex;
        return cp.index() == inputIndex;
    }

    @Override public int energyAvailable() {
        if (!getBlockState().getValue(OPEN)) return 0;
        long avail = bufferedEnergy;
        boolean outputWired = isSideWired(false);
        if (outputWired) avail = Math.min(avail, getWireRate(false));
        return (int) Math.min(avail, Integer.MAX_VALUE);
    }

    @Override public void energyExtracted(int amount) {
        if (amount > 0) {
            long drain = Math.min(amount, bufferedEnergy);
            bufferedEnergy -= drain;
            acceptedAmount += drain;
            packets++;
        }
        efficientSetChanged();
    }

    @Override public int energyRequested() {
        if (!getBlockState().getValue(OPEN)) return 0;
        boolean outputWired = isSideWired(false);
        IEnergyStorage outputStorage = outputWired ? null : getOutputEnergy();
        int req = getTransferLimit(outputWired, outputStorage);
        boolean inputWired = isSideWired(true);
        if (inputWired) req = Math.min(req, getWireRate(true));
        return req;
    }

    @Override public void energyInserted(int amount) {
        int max = timeLimit > 0 ? Math.max(timeLimit - longToInt(acceptedAmount), 0) : amount;
        amount = Math.min(amount, max);
        bufferedEnergy += amount;
        efficientSetChanged();
    }

    @Override public void energyPassedThrough(double amount) {
        acceptedAmount += (long)amount;
        packets++;
    }
}
