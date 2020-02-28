package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import mctmods.immersivetechnology.common.Config;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class TileEntityTrashEnergy extends TileEntityGenericTrash implements IEnergyStorage {

	public String unit() {
		return ".osd.trash_energy.unit";
	}

	public String unitPerSecond() {
		return ".osd.trash_energy.unitlastsecond";
	}

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
		int canTransfer = (int)Math.min(maxReceive, Config.ITConfig.Trash.fluid_max_void_rate - acceptedAmount);
		if(!simulate) {
			acceptedAmount += canTransfer;
			packets++;
		}
		return canTransfer;
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
	
}