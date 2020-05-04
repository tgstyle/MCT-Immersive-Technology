package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import mctmods.immersivetechnology.common.Config;
import mctmods.immersivetechnology.common.tileentities.TileEntityCommonOSD;
import mctmods.immersivetechnology.common.util.ITrashCanBounds;
import mctmods.immersivetechnology.common.util.TranslationKey;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class TileEntityTrashEnergy extends TileEntityCommonOSD implements IEnergyStorage, ITrashCanBounds {

	@Override
	public boolean hasCapability(final Capability<?> capability, final EnumFacing facing) {
		if(capability == CapabilityEnergy.ENERGY) return true;
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(final Capability<T> capability, final EnumFacing facing) {
		if(capability == CapabilityEnergy.ENERGY) return (T) this;
		return super.getCapability(capability, facing);
	}

	@Override
	public int receiveEnergy(int maxReceive, boolean simulate) {
		if(!simulate) acceptedAmount += maxReceive;
		return maxReceive;
	}

	@Override
	public int extractEnergy(int maxReceive, boolean simulate) {
		return 0;
	}

	@Override
	public int getEnergyStored() {
		return 0;
	}

	@Override
	public int getMaxEnergyStored() {
		return Integer.MAX_VALUE;
	}

	@Override
	public boolean canExtract() {
		return false;
	}

	@Override
	public boolean canReceive() {
		return true;
	}

	@Override
	public TranslationKey text() {
		return Config.ITConfig.Experimental.per_tick_trash_cans?
				TranslationKey.OVERLAY_OSD_TRASH_ENERGY_NORMAL_ALTERNATIVE :
				TranslationKey.OVERLAY_OSD_TRASH_ENERGY_NORMAL_FIRST_LINE;
	}
}