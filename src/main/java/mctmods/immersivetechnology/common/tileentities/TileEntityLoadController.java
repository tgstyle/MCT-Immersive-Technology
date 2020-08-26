package mctmods.immersivetechnology.common.tileentities;

import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.api.ITLib;
import mctmods.immersivetechnology.client.gui.GuiLoadController;
import mctmods.immersivetechnology.common.util.TranslationKey;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityLoadController extends TileEntityCommonValve implements IEnergyStorage {

	public static DummyBattery dummyBattery = new DummyBattery();

	public TileEntityLoadController() {
		super( TranslationKey.OVERLAY_OSD_LOAD_CONTROLLER_NORMAL_FIRST_LINE,
				TranslationKey.OVERLAY_OSD_LOAD_CONTROLLER_SNEAKING_FIRST_LINE,
				TranslationKey.OVERLAY_OSD_LOAD_CONTROLLER_SNEAKING_SECOND_LINE,
				ITLib.GUIID_Load_Controller);
	}

	public static class DummyBattery implements IEnergyStorage {

		@Override
		public int receiveEnergy(int i, boolean b) {
			return 0;
		}

		@Override
		public int extractEnergy(int i, boolean b) {
			return 0;
		}

		@Override
		public int getEnergyStored() {
			return 0;
		}

		@Override
		public int getMaxEnergyStored() {
			return 0;
		}

		@Override
		public boolean canExtract() {
			return false;
		}

		@Override
		public boolean canReceive() {
			return false;
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void showGui() {
		Minecraft.getMinecraft().displayGuiScreen(new GuiLoadController(this));
	}

	@Override
	public boolean hasCapability(final Capability<?> capability, final EnumFacing facing) {
		if(facing == null) return false;
		if(capability == CapabilityEnergy.ENERGY && facing.getAxis() == this.facing.getAxis()) return true;
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(final Capability<T> capability, final EnumFacing facing) {
		if(facing == null) return null;
		if(capability == CapabilityEnergy.ENERGY) {
			if(facing == this.facing) return (T)this;
			else if(facing == this.facing.getOpposite()) return (T) dummyBattery;
		}
		return super.getCapability(capability, facing);
	}

	boolean busy = false;

	@Override
	public int receiveEnergy(int maxReceive, boolean simulate) {
		if(busy) return 0;
		IEnergyStorage destination = getDestination();
		if(destination == null) return 0;
		int canAccept = maxReceive;
		canAccept = timeLimit != -1? Math.min(Math.max(timeLimit - longToInt(acceptedAmount), 0), canAccept) : canAccept;
		canAccept = keepSize != -1? Math.min(Math.max(keepSize - destination.getEnergyStored(), 0), canAccept) : canAccept;
		canAccept = packetLimit != -1? Math.min(canAccept, packetLimit) : canAccept;
		if(redstoneMode > 0) canAccept *= (double) (redstoneMode == 1? 15 - getRSPower() : getRSPower())/15;
		if(canAccept == 0) return 0;
		int toReturn = 0;
		busy = true;
		toReturn = destination.receiveEnergy(canAccept, simulate);
		busy = false;
		if(!simulate) {
			acceptedAmount += toReturn;
			packets++;
		}
		return toReturn;
	}

	@Override
	public int extractEnergy(int i, boolean b) {
		return 0;
	}

	@Override
	public int getEnergyStored() {
		IEnergyStorage dest = getDestination();
		if(dest == null) return 0;
		return dest.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored() {
		IEnergyStorage dest = getDestination();
		if(dest == null) return 0;
		return dest.getMaxEnergyStored();
	}

	@Override
	public boolean canExtract() {
		return false;
	}

	@Override
	public boolean canReceive() {
		return true;
	}

	public IEnergyStorage getDestination() {
		TileEntity dst = Utils.getExistingTileEntity(world, pos.offset(facing, -1));
		if(dst != null && dst.hasCapability(CapabilityEnergy.ENERGY, facing)) {
			return dst.getCapability(CapabilityEnergy.ENERGY, facing);
		}
		return null;
	}

}