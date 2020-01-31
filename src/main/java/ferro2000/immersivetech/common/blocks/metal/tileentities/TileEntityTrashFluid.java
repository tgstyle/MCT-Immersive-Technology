package ferro2000.immersivetech.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;

import ferro2000.immersivetech.common.Config.ITConfig;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

public class TileEntityTrashFluid extends TileEntityIEBase implements ITickable, IFluidTank {

	private static int trashFluidSize = ITConfig.Machines.fluidTrashTankSize;

	public FluidTank tank = new FluidTank(trashFluidSize);

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {
	}

	@Override
	public void update() {
		if(world.isRemote) return;
		if(tank.getFluidAmount() > 0) tank.setFluid(null);
	}

	@Override
	public boolean hasCapability(final Capability<?> capability, final EnumFacing facing) {
		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) return true;
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(final Capability<T> capability, final EnumFacing facing) {
		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) return (T) this.tank;
		return super.getCapability(capability, facing);
	}

	@Override
	public FluidStack getFluid() {
		return tank.getFluid();
	}

	@Override
	public int getFluidAmount() {
		return tank.getFluidAmount();
	}

	@Override
	public int getCapacity() {
		return tank.getCapacity();
	}

	@Override
	public FluidTankInfo getInfo() {
		return tank.getInfo();
	}

	@Override
	public int fill(FluidStack resource, boolean doFill) {
		return Math.min(resource.amount, tank.getCapacity());
	}

	@Override
	public FluidStack drain(int maxDrain, boolean doDrain) {
		return tank.drain(maxDrain, doDrain);
	}

}