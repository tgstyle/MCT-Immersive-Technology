package ferro2000.immersivetech.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.api.IEEnums;
import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IConfigurableSides;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;

import ferro2000.immersivetech.common.Config.ITConfig;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

public class TileEntityTrashEnergy extends TileEntityIEBase implements ITickable, IIEInternalFluxHandler, IConfigurableSides {

	private static int trashEnergySize = ITConfig.Machines.energyTrashCapacitorSize;

	FluxStorage energyStorage = new FluxStorage(trashEnergySize);

	public SideConfig[] sideConfig = {SideConfig.INPUT, SideConfig.INPUT, SideConfig.INPUT, SideConfig.INPUT, SideConfig.INPUT, SideConfig.INPUT};

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {
	}

	@Override
	public void update() {
		if(world.isRemote) return;
		if(energyStorage.getEnergyStored() > 0) energyStorage.setEnergy(0);
	}

	IEForgeEnergyWrapper[] wrappers = IEForgeEnergyWrapper.getDefaultWrapperArray(this);

	@Override
	public SideConfig getEnergySideConfig(EnumFacing facing) {
		if(facing == null) return SideConfig.NONE;
		return this.sideConfig[facing.ordinal()];
	}

	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(EnumFacing facing) {
		if (facing == null) return null;
		return wrappers[facing.ordinal()];
	}

	@Override
	public FluxStorage getFluxStorage() {
		return this.energyStorage;
	}

	@Override
	public IEEnums.SideConfig getSideConfig(int side) {
		return this.sideConfig[side];
	}

	@Override
	public boolean toggleSide(int side, EntityPlayer p) {
		return false;
	}

}