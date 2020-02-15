package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.api.IEEnums;
import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IConfigurableSides;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.common.Config.ITConfig.Trash;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.RayTraceResult;

public class TileEntityTrashEnergy extends TileEntityIEBase implements ITickable, IBlockOverlayText, IBlockBounds, IIEInternalFluxHandler, IConfigurableSides {
	
	public EnumFacing facing = EnumFacing.NORTH;

	private static int trashEnergySize = Trash.trash_energy_capacitorSize;

	FluxStorage energyStorage = new FluxStorage(trashEnergySize);

	public SideConfig[] sideConfig = {SideConfig.INPUT, SideConfig.INPUT, SideConfig.INPUT, SideConfig.INPUT, SideConfig.INPUT, SideConfig.INPUT};

	private int acceptedAmount = 0;
	private int perSecond = 0;
	private int updateClient = 1;
	private int lastAmount;
	private int times;

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		acceptedAmount = nbt.getInteger("acceptedAmount");
		perSecond = nbt.getInteger("perSecond");
		this.readEnergy(nbt);
	}

	public void readEnergy(NBTTagCompound nbt) {
		energyStorage.readFromNBT(nbt.getCompoundTag("energy"));
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		nbt.setInteger("acceptedAmount", acceptedAmount);
		nbt.setInteger("perSecond", perSecond);
		this.writeEnergy(nbt, false);
	}

	public void writeEnergy(NBTTagCompound nbt, boolean toItem) {
		boolean write = energyStorage.getEnergyStored() > 0;
		NBTTagCompound energyTag = energyStorage.writeToNBT(new NBTTagCompound());
		if(!toItem || write) nbt.setTag("energy", energyTag);
	}

	public void efficientMarkDirty() { // !!!!!!! only use it within update() function !!!!!!!
		world.getChunkFromBlockCoords(this.getPos()).markDirty();
	}

	@Override
	public void update() {
		if(world.isRemote) return;
		boolean update = false;
		if(energyStorage.getEnergyStored() > 0) {
			lastAmount = energyStorage.getEnergyStored();
			times++;
			energyStorage.setEnergy(0);
		}
		if(updateClient >= 20) {
			acceptedAmount = lastAmount;
			perSecond = times;
			lastAmount = 0;
			times = 0;
			updateClient = 1;
			update = true;
		} else {
			updateClient++;
		}
		if(update) {
			efficientMarkDirty();
			this.markContainingBlockForUpdate(null);
		}
	}

	IEForgeEnergyWrapper[] wrappers = IEForgeEnergyWrapper.getDefaultWrapperArray(this);

	@Override
	public String[] getOverlayText(EntityPlayer player, RayTraceResult mop, boolean hammer) {
		String amount = I18n.format(ImmersiveTechnology.MODID + ".osd.trash_energy.trashed") + ": " + acceptedAmount + " IF " + perSecond + " " + I18n.format(ImmersiveTechnology.MODID + ".osd.trash_energy.lastsecond");
		return new String[]{amount};
	}

	@Override
	public boolean useNixieFont(EntityPlayer player, RayTraceResult mop) {
		return false;
	}

	@Override
	public SideConfig getEnergySideConfig(EnumFacing facing) {
		if(facing == null) return SideConfig.NONE;
		return this.sideConfig[facing.ordinal()];
	}

	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(EnumFacing facing) {
		if(facing == null) return null;
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

	@Override
	public float[] getBlockBounds()	{
		return new float[]{facing.getAxis()==Axis.X ? 0 : .125f, 0, facing.getAxis()==Axis.Z ? .125f : .125f, facing.getAxis()==Axis.X ? 1 : .875f, 1, facing.getAxis()==Axis.Z ? .875f : .875f};
	}

}