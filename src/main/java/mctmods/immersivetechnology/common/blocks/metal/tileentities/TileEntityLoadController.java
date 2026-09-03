package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.AbstractConnection;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.common.util.Utils;

import mctmods.immersivetechnology.client.ITGUI;
import mctmods.immersivetechnology.client.gui.GuiLoadController;
import mctmods.immersivetechnology.common.shared.tileentities.TileEntityCommonValve;
import mctmods.immersivetechnology.common.util.TranslationKey;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Set;
import javax.annotation.Nullable;
import java.util.Optional;

public class TileEntityLoadController extends TileEntityCommonValve implements IEnergyStorage {

	public static final DummyBattery dummyBattery = new DummyBattery();

	public TileEntityLoadController() {
		super(TranslationKey.OVERLAY_OSD_LOAD_CONTROLLER_NORMAL_FIRST_LINE,
				TranslationKey.OVERLAY_OSD_LOAD_CONTROLLER_SNEAKING_FIRST_LINE,
				TranslationKey.OVERLAY_OSD_LOAD_CONTROLLER_SNEAKING_SECOND_LINE,
				ITGUI.GUIID_Load_Controller);
	}

	private long bufferedEnergy = 0;
	private WireType leftCable;
	private WireType rightCable;
	private BlockPos leftEnd;
	private BlockPos rightEnd;

	private EnumFacing perpDirection() { return facing.getAxis().isVertical() ? EnumFacing.byHorizontalIndex(rotation) : facing.rotateY(); }

	private double getHitPos(TargetingInfo target, EnumFacing perpDir) {
		EnumFacing.Axis perpAxis = perpDir.getAxis();
		double hitPos = perpAxis == EnumFacing.Axis.X ? target.hitX : perpAxis == EnumFacing.Axis.Y ? target.hitY : target.hitZ;
		if (perpDir.getAxisDirection() != EnumFacing.AxisDirection.POSITIVE) { hitPos = 1 - hitPos; }
		return hitPos;
	}

	private boolean targetsRight(TargetingInfo target) {
		if (leftCable == null && rightCable != null) { return false; }
		if (leftCable != null && rightCable == null) { return true; }
		return getHitPos(target, perpDirection()) >= .5;
	}

	private boolean isRightConnection(Connection con) {
		BlockPos other = con.start.equals(pos) ? con.end : con.start;
		if (other.equals(rightEnd)) { return true; }
		if (other.equals(leftEnd)) { return false; }
		return leftCable == null;
	}

	private Vec3d portOffset(WireType type, boolean right) {
		double conRadius = type.getRenderDiameter() / 2;
		boolean vertical = facing.getAxis().isVertical();
		EnumFacing perpDir = vertical ? EnumFacing.byHorizontalIndex(rotation) : facing.rotateY();
		double perpOff = right ? .1875 : .8125;
		if (perpDir.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE) { perpOff = 1 - perpOff; }
		double alongOff = facing.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? 1 - conRadius : conRadius;
		double xOff = .5;
		double yOff = .5;
		double zOff = .5;
		switch (facing.getAxis()) {
			case X: xOff = alongOff; break;
			case Y: yOff = alongOff; break;
			case Z: zOff = alongOff; break;
		}
		switch (perpDir.getAxis()) {
			case X: xOff = perpOff; break;
			case Y: yOff = perpOff; break;
			case Z: zOff = perpOff; break;
		}
		if (!vertical) { yOff = .5; }
		return new Vec3d(xOff, yOff, zOff);
	}

	@Override @Nonnull public Vec3d getConnectionOffset(@Nonnull Connection con) { return portOffset(con.cableType, isRightConnection(con)); }

	@Override @Nonnull public Vec3d getConnectionOffset(@Nonnull Connection con, TargetingInfo target, Vec3i offsetLink) { return portOffset(con.cableType, targetsRight(target)); }

	@Override public boolean canConnectCable(WireType cableType, TargetingInfo target, @Nonnull Vec3i offset) {
		String category = cableType.getCategory();
		if (!WireType.LV_CATEGORY.equals(category) && !WireType.MV_CATEGORY.equals(category) && !WireType.HV_CATEGORY.equals(category)) { return false; }
		return (targetsRight(target) ? rightCable : leftCable) == null;
	}

	@Override public void connectCable(WireType cableType, TargetingInfo target, IImmersiveConnectable other) {
		BlockPos otherPos = other.getConnectionMaster(cableType, target);
		if (targetsRight(target)) { rightCable = cableType; rightEnd = otherPos; }
		else { leftCable = cableType; leftEnd = otherPos; }
	}

	@Override public WireType getCableLimiter(@Nonnull TargetingInfo target) { return targetsRight(target) ? rightCable : leftCable; }

	@Override public void removeCable(Connection connection) {
		if (connection == null) { leftCable = null; rightCable = null; leftEnd = null; rightEnd = null; }
		else if (isRightConnection(connection)) { rightCable = null; rightEnd = null; }
		else { leftCable = null; leftEnd = null; }
		markContainingBlockForUpdate(null);
	}

	@Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
		super.readCustomNBT(nbt, descPacket);
		leftCable = nbt.hasKey("leftCable") ? ApiUtils.getWireTypeFromNBT(nbt, "leftCable") : null;
		rightCable = nbt.hasKey("rightCable") ? ApiUtils.getWireTypeFromNBT(nbt, "rightCable") : null;
		leftEnd = nbt.hasKey("leftEnd") ? BlockPos.fromLong(nbt.getLong("leftEnd")) : null;
		rightEnd = nbt.hasKey("rightEnd") ? BlockPos.fromLong(nbt.getLong("rightEnd")) : null;
		bufferedEnergy = nbt.getLong("bufferedEnergy");
	}

	@Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
		super.writeCustomNBT(nbt, descPacket);
		if (leftCable != null) { nbt.setString("leftCable", leftCable.getUniqueName()); }
		if (rightCable != null) { nbt.setString("rightCable", rightCable.getUniqueName()); }
		if (leftEnd != null) { nbt.setLong("leftEnd", leftEnd.toLong()); }
		if (rightEnd != null) { nbt.setLong("rightEnd", rightEnd.toLong()); }
		nbt.setLong("bufferedEnergy", bufferedEnergy);
	}

	@Override protected boolean canTakeLV() { return true; }

	@Override protected boolean canTakeMV() { return true; }

	@Override protected boolean canTakeHV() { return true; }

	@Override public boolean isEnergyOutput() { return true; }

	@Override public boolean allowEnergyToPass(Connection con) { return false; }

	private int transferLimit(IEnergyStorage outputStorage) {
		int canAccept = Integer.MAX_VALUE;
		if (timeLimit != -1) { canAccept = Math.min(Math.max(timeLimit - longToInt(acceptedAmount), 0), canAccept); }
		if (outputStorage != null) { canAccept = keepSize != -1 ? Math.min(Math.max(keepSize - outputStorage.getEnergyStored(), 0), canAccept) : canAccept; }
		else {
			if (outputCable() == null) { return 0; }
			if (keepSize != -1) { canAccept = Math.min(Math.max(keepSize - longToInt(bufferedEnergy), 0), canAccept); }
			canAccept = Math.min(canAccept, outputCable().getTransferRate());
		}
		if (packetLimit != -1) { canAccept = Math.min(canAccept, packetLimit); }
		if (redstoneMode > 0) { canAccept = (int)(canAccept * ((redstoneMode == 1 ? 15 - getRSPower() : getRSPower()) / 15.0)); }
		return Math.max(canAccept, 0);
	}

	@Override public int outputEnergy(int amount, boolean simulate, int energyType) {
		if (!open) { return 0; }
		int accepted = Math.min(amount, transferLimit(getDestination()));
		if (inputCable() != null) { accepted = Math.min(accepted, inputCable().getTransferRate()); }
		if (accepted <= 0) { return 0; }
		if (!simulate) { bufferedEnergy += accepted; }
		return accepted;
	}

	private void drainBuffer() {
		if (!open || bufferedEnergy <= 0) { return; }
		IEnergyStorage destination = getDestination();
		if (destination != null) {
			int moved = destination.receiveEnergy(longToInt(Math.min(bufferedEnergy, transferLimit(destination))), false);
			if (moved > 0) { bufferedEnergy -= moved; countOut(moved); }
		}
		if (bufferedEnergy <= 0) { return; }
		Set<AbstractConnection> outputs = ImmersiveNetHandler.INSTANCE.getIndirectEnergyConnections(pos, world, true);
		for (AbstractConnection con : outputs) {
			if (bufferedEnergy <= 0) { break; }
			if (!con.isEnergyOutput || con.cableType == null) { continue; }
			if (leavesByInputPort(con)) { continue; }
			IImmersiveConnectable end = ApiUtils.toIIC(con.end, world);
			if (end == null) { continue; }
			int offer = longToInt(Math.min(bufferedEnergy, con.cableType.getTransferRate()));
			int moved = end.outputEnergy(offer, false, 0);
			if (moved > 0) { bufferedEnergy -= moved; countOut(moved); }
		}
	}

	private boolean leavesByInputPort(AbstractConnection con) {
		if (con.subConnections == null || con.subConnections.length == 0) { return false; }
		BlockPos firstHop = con.subConnections[0].end;
		BlockPos inputEnd = inputEnd();
		return inputEnd != null && firstHop.equals(inputEnd);
	}

	private boolean rightIsInput() { return facing.getAxis().isVertical(); }

	private WireType inputCable() { return rightIsInput() ? rightCable : leftCable; }

	private WireType outputCable() { return rightIsInput() ? leftCable : rightCable; }

	private BlockPos inputEnd() { return rightIsInput() ? rightEnd : leftEnd; }

	private int movedThisTick = 0;

	private void fillBuffer() {
		if (!open || inputCable() != null || outputCable() == null) { return; }
		IEnergyStorage source = getSource();
		if (source == null || !source.canExtract()) { return; }
		int room = transferLimit(null) - movedThisTick;
		if (room <= 0) { return; }
		int got = source.extractEnergy(room, false);
		if (got > 0) { bufferedEnergy += got; }
	}

	private void pullDirect() {
		if (!open || inputCable() != null || outputCable() != null) { return; }
		IEnergyStorage source = getSource();
		IEnergyStorage destination = getDestination();
		if (source == null || destination == null || !source.canExtract()) { return; }
		int canAccept = transferLimit(destination) - movedThisTick;
		if (canAccept <= 0) { return; }
		int extracted = source.extractEnergy(canAccept, true);
		if (extracted <= 0) { return; }
		int inserted = destination.receiveEnergy(extracted, false);
		if (inserted <= 0) { return; }
		source.extractEnergy(inserted, false);
		countOut(inserted);
	}

	@Override public void update() {
		super.update();
		if (world.isRemote) { return; }
		fillBuffer();
		pullDirect();
		drainBuffer();
		movedThisTick = 0;
	}

	@SideOnly(Side.CLIENT)
	@Override public void showGui() { Minecraft.getMinecraft().displayGuiScreen(new GuiLoadController(this)); }

	@SideOnly(Side.CLIENT)
	@Override public Optional<TRSRTransformation> applyTransformations(@Nonnull IBlockState object, @Nonnull String group, @Nonnull Optional<TRSRTransformation> transform) { return valveTransform(object, transform, 270, 180, 0, 0, 1); }

	public static class DummyBattery implements IEnergyStorage {

		@Override public int receiveEnergy(int maxReceive, boolean simulate) { return 0; }

		@Override public int extractEnergy(int maxExtract, boolean simulate) { return 0; }

		@Override public int getEnergyStored() { return 0; }

		@Override public int getMaxEnergyStored() { return 0; }

		@Override public boolean canExtract() { return false; }

		@Override public boolean canReceive() { return false; }
	}

	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing side) {
		return capability == CapabilityEnergy.ENERGY
				&& (side == null || side.getAxis() == perpDirection().getAxis())
				|| super.hasCapability(capability, side);
	}

	@SuppressWarnings("unchecked")
	@Override @Nonnull public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing side) {
		if (capability == CapabilityEnergy.ENERGY) {
			if (side == inputDir()) { return (T)this; }
			else if (side == outputDir() || side == null) { return (T)dummyBattery; }
		}
		return super.getCapability(capability, side);
	}

	private EnumFacing inputDir() { return rightIsInput() ? perpDirection() : perpDirection().getOpposite(); }

	private EnumFacing outputDir() { return inputDir().getOpposite(); }

	private IEnergyStorage getSource() {
		EnumFacing in = inputDir();
		TileEntity src = Utils.getExistingTileEntity(world, pos.offset(in));
		if (src != null && src.hasCapability(CapabilityEnergy.ENERGY, in.getOpposite())) { return src.getCapability(CapabilityEnergy.ENERGY, in.getOpposite()); }
		return null;
	}

	boolean busy = false;

	public IEnergyStorage getDestination() {
		EnumFacing out = outputDir();
		TileEntity dst = Utils.getExistingTileEntity(world, pos.offset(out));
		if (dst != null && dst.hasCapability(CapabilityEnergy.ENERGY, out.getOpposite())) { return dst.getCapability(CapabilityEnergy.ENERGY, out.getOpposite()); }
		return null;
	}

	@Override public int receiveEnergy(int maxReceive, boolean simulate) {
		if (!open) { return 0; }
		if (getDestination() != null) {
			int accepted = transfer(maxReceive, simulate);
			if (!simulate && accepted > 0) {
				movedThisTick += accepted;
				countOut(accepted);
			}
			return accepted;
		}
		int accepted = Math.min(maxReceive, transferLimit(null));
		if (accepted <= 0) { return 0; }
		if (!simulate) {
			bufferedEnergy += accepted;
			movedThisTick += accepted;
		}
		return accepted;
	}

	private int transfer(int maxReceive, boolean simulate) {
		if (busy || !open) { return 0; }
		IEnergyStorage destination = getDestination();
		if (destination == null) { return 0; }
		int canAccept = maxReceive;
		canAccept = timeLimit != -1 ? Math.min(Math.max(timeLimit - longToInt(acceptedAmount), 0), canAccept) : canAccept;
		canAccept = keepSize != -1 ? Math.min(Math.max(keepSize - destination.getEnergyStored(), 0), canAccept) : canAccept;
		canAccept = packetLimit != -1 ? Math.min(canAccept, packetLimit) : canAccept;
		if (redstoneMode > 0) { canAccept = (int)(canAccept * ((redstoneMode == 1 ? 15 - getRSPower() : getRSPower()) / 15.0)); }
		if (canAccept == 0) { return 0; }
		busy = true;
		int accepted = destination.receiveEnergy(canAccept, simulate);
		busy = false;
		return accepted;
	}

	private void countOut(int amount) {
		if (amount <= 0) { return; }
		acceptedAmount += amount;
		packets++;
	}

	@Override public int extractEnergy(int maxExtract, boolean simulate) { return 0; }

	@Override public int getEnergyStored() {
		IEnergyStorage dest = getDestination();
		if (dest == null) { return 0; }
		return dest.getEnergyStored();
	}

	@Override public int getMaxEnergyStored() {
		IEnergyStorage dest = getDestination();
		if (dest == null) { return 0; }
		return dest.getMaxEnergyStored();
	}

	@Override public boolean canExtract() { return false; }

	@Override public boolean canReceive() { return true; }
}
