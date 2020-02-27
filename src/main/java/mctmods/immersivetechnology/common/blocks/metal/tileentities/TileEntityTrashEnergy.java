package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import mctmods.immersivetechnology.api.ITUtils;
import mctmods.immersivetechnology.common.Config;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class TileEntityTrashEnergy extends TileEntityGenericTrash implements IBlockBounds, IEnergyStorage {
	
	public EnumFacing facing = EnumFacing.NORTH;

	@Override
	public String[] getOverlayText(EntityPlayer player, RayTraceResult mop, boolean hammer) {
		return new String[]{
				ITUtils.Translate(".osd.general.trashed", false, true) +
						lastAcceptedAmount + ITUtils.Translate(".osd.trash_energy.unit", true),
				ITUtils.Translate(".osd.general.inpackets", false, true) +
						lastPerSecond + ITUtils.Translate(".osd.general.packetslastsecond", true)
		};
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
	public float[] getBlockBounds()	{
		return new float[]{facing.getAxis()==Axis.X ? 0 : .125f, 0, facing.getAxis()==Axis.Z ? .125f : .125f, facing.getAxis()==Axis.X ? 1 : .875f, 1, facing.getAxis()==Axis.Z ? .875f : .875f};
	}

	@Override
	public int receiveEnergy(int maxReceive, boolean simulate) {
		int canTransfer = Math.min(maxReceive, Config.ITConfig.Trash.fluid_max_void_rate - acceptedAmount);
		if (!simulate) {
			acceptedAmount += canTransfer;
			perSecond++;
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