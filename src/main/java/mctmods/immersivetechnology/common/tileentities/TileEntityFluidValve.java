package mctmods.immersivetechnology.common.tileentities;

import blusunrize.immersiveengineering.api.fluid.IFluidPipe;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.api.ITLib;
import mctmods.immersivetechnology.client.gui.GuiFluidValve;
import mctmods.immersivetechnology.common.util.TranslationKey;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class TileEntityFluidValve extends TileEntityCommonValve implements IFluidHandler, IFluidPipe, IEBlockInterfaces.IBlockBounds {

	public static DummyTank dummyTank = new DummyTank();

	public TileEntityFluidValve() {
		super( TranslationKey.OVERLAY_OSD_FLUID_VALVE_NORMAL_FIRST_LINE,
				TranslationKey.OVERLAY_OSD_FLUID_VALVE_SNEAKING_FIRST_LINE,
				TranslationKey.OVERLAY_OSD_FLUID_VALVE_SNEAKING_SECOND_LINE,
				ITLib.GUIID_Fluid_Valve);
	}

	@Override
	public boolean canOutputPressurized(boolean consumePower) {
		return false;
	}

	@Override
	public boolean hasOutputConnection(EnumFacing side) {
		return side == facing;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void showGui() {
		Minecraft.getMinecraft().displayGuiScreen(new GuiFluidValve(this));
	}

	public static class DummyTank implements IFluidHandler, IFluidTankProperties {

		IFluidTankProperties[] properties = new IFluidTankProperties[] { this };

		@Override
		public IFluidTankProperties[] getTankProperties() {
			return properties;
		}

		@Override
		public int fill(FluidStack fluidStack, boolean b) {
			return 0;
		}

		@Nullable
		@Override
		public FluidStack drain(FluidStack fluidStack, boolean b) {
			return null;
		}

		@Nullable
		@Override
		public FluidStack drain(int i, boolean b) {
			return null;
		}

		@Nullable
		@Override
		public FluidStack getContents() {
			return null;
		}

		@Override
		public int getCapacity() {
			return 0;
		}

		@Override
		public boolean canFill() {
			return false;
		}

		@Override
		public boolean canDrain() {
			return false;
		}

		@Override
		public boolean canFillFluidType(FluidStack fluidStack) {
			return false;
		}

		@Override
		public boolean canDrainFluidType(FluidStack fluidStack) {
			return false;
		}
	}

	@Override
	public boolean hasCapability(final Capability<?> capability, final EnumFacing facing) {
		if(facing == null) return false;
		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing.getAxis() == this.facing.getAxis()) return true;
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(final Capability<T> capability, final EnumFacing facing) {
		if(facing == null) return null;
		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			if(facing == this.facing) return (T)this;
			else if(facing == this.facing.getOpposite()) return (T)dummyTank;
		}
		return super.getCapability(capability, facing);
	}

	IFluidTankProperties[] tank = new IFluidTankProperties[] { new FluidTankProperties(null, Integer.MAX_VALUE, true, false) };

	@Override
	public IFluidTankProperties[] getTankProperties() {
		return tank;
	}

	boolean busy = false;

	@Override
	public int fill(FluidStack fluidStack, boolean doFill) {
		if(busy) return 0;
		IFluidHandler destination = getDestination();
		if(destination == null) return 0;
		int canAccept = fluidStack.amount;
		canAccept = timeLimit != -1? Math.min(Math.max(timeLimit - longToInt(acceptedAmount), 0), canAccept) : canAccept;
		canAccept = keepSize != -1? Math.min(Math.max(keepSize - getTankFill(destination.getTankProperties(), fluidStack), 0), canAccept) : canAccept;
		canAccept = packetLimit != -1? Math.min(canAccept, packetLimit) : canAccept;
		if(redstoneMode > 0) canAccept *= (double) (redstoneMode == 1? 15 - getRSPower() : getRSPower())/15;
		if(canAccept == 0) return 0;
		int toReturn = 0;
		busy = true;
		toReturn = destination.fill(new FluidStack(fluidStack, canAccept), doFill);
		busy = false;
		if(doFill) {
			acceptedAmount += toReturn;
			packets++;
		}
		return toReturn;
	}

	public static int getTankFill(IFluidTankProperties[] properties, FluidStack toFill) {
		int toReturn = 0;
		for(IFluidTankProperties property : properties) {
			FluidStack stored = property.getContents();
			if(stored != null && stored.isFluidEqual(toFill)) toReturn += stored.amount;
		}
		return toReturn;
	}

	@Nullable
	@Override
	public FluidStack drain(FluidStack fluidStack, boolean b) {
		return null;
	}

	@Nullable
	@Override
	public FluidStack drain(int i, boolean b) {
		return null;
	}

	public IFluidHandler getDestination() {
		TileEntity dst = Utils.getExistingTileEntity(world, pos.offset(facing, -1));
		if(dst != null && dst.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing)) {
			return dst.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing);
		}
		return null;
	}

	public float isX(float ifTrue, float ifFalse) {
		return (facing.getAxis() == EnumFacing.Axis.X)? ifTrue : ifFalse;
	}

	public float isY(float ifTrue, float ifFalse) {
		return (facing.getAxis() == EnumFacing.Axis.Y)? ifTrue : ifFalse;
	}

	public float isZ(float ifTrue, float ifFalse) {
		return (facing.getAxis() == EnumFacing.Axis.Z)? ifTrue : ifFalse;
	}

	@Override
	public float[] getBlockBounds()	{
		return new float[] {
				isX(0, .125f), isY(0, .125f), isZ(0, .125f),
				isX(1, .875f), isY(1, .875f), isZ(1, .875f),
		};
	}

}