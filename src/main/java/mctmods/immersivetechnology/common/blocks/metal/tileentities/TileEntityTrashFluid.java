package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import mctmods.immersivetechnology.common.Config;
import mctmods.immersivetechnology.common.tileentities.TileEntityCommonOSD;
import mctmods.immersivetechnology.common.util.ITrashCanBounds;
import mctmods.immersivetechnology.common.util.TranslationKey;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;

public class TileEntityTrashFluid extends TileEntityCommonOSD implements IFluidTank, IFluidHandler, ITrashCanBounds {

	@Override
	public boolean hasCapability(final Capability<?> capability, final EnumFacing facing) {
		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) return true;
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(final Capability<T> capability, final EnumFacing facing) {
		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) return (T) this;
		return super.getCapability(capability, facing);
	}

	@Override
	public FluidStack getFluid() {
		return null;
	}

	@Override
	public int getFluidAmount() {
		return 0;
	}

	@Override
	public int getCapacity() {
		return Integer.MAX_VALUE;
	}

	FluidTankInfo info = new FluidTankInfo(null, Integer.MAX_VALUE);
	IFluidTankProperties[] tank = new IFluidTankProperties[] { new FluidTankProperties(null, Integer.MAX_VALUE, true, false)};

	@Override
	public FluidTankInfo getInfo() {
		return info;
	}

	@Override
	public IFluidTankProperties[] getTankProperties() {
		return tank;
	}

	@Override
	public int fill(FluidStack resource, boolean doFill) {
		if(doFill) acceptedAmount += resource.amount;
		return resource.amount;
	}

	@Nullable
	@Override
	public FluidStack drain(FluidStack fluidStack, boolean b) {
		return null;
	}

	@Override
	public FluidStack drain(int maxDrain, boolean doDrain) {
		return null;
	}

	@Override
	public TranslationKey text() {
		return Config.ITConfig.Experimental.per_tick_trash_cans?
				TranslationKey.OVERLAY_OSD_TRASH_FLUID_NORMAL_ALTERNATIVE :
				TranslationKey.OVERLAY_OSD_TRASH_FLUID_NORMAL_FIRST_LINE;
	}
}