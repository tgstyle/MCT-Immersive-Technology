package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.api.fluid.IFluidPipe;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.util.Utils;

import mctmods.immersivetechnology.client.ITGUI;
import mctmods.immersivetechnology.client.gui.GuiFluidValve;
import mctmods.immersivetechnology.common.shared.tileentities.TileEntityCommonValve;
import mctmods.immersivetechnology.common.util.TranslationKey;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class TileEntityFluidValve extends TileEntityCommonValve implements IFluidHandler, IFluidPipe, IEBlockInterfaces.IBlockBounds {

	public static final DummyTank dummyTank = new DummyTank();

	public TileEntityFluidValve() {
		super(TranslationKey.OVERLAY_OSD_FLUID_VALVE_NORMAL_FIRST_LINE,
				TranslationKey.OVERLAY_OSD_FLUID_VALVE_SNEAKING_FIRST_LINE,
				TranslationKey.OVERLAY_OSD_FLUID_VALVE_SNEAKING_SECOND_LINE,
				ITGUI.GUIID_Fluid_Valve);
	}

	@Override public boolean canOutputPressurized(boolean consumePower) { return false; }

	@Override public boolean hasOutputConnection(EnumFacing side) { return side == facing; }

	@SideOnly(Side.CLIENT)
	@Override public void showGui() { Minecraft.getMinecraft().displayGuiScreen(new GuiFluidValve(this)); }

	@SideOnly(Side.CLIENT)
	@Override public Optional<TRSRTransformation> applyTransformations(@Nonnull IBlockState object, @Nonnull String group, @Nonnull Optional<TRSRTransformation> transform) { return valveTransform(object, transform, 0, 90, 270, 2, 2); }

	public static class DummyTank implements IFluidHandler {

		@Override public IFluidTankProperties[] getTankProperties() { return new IFluidTankProperties[0]; }

		@Override public int fill(FluidStack resource, boolean doFill) { return 0; }

		@Nullable @Override public FluidStack drain(FluidStack resource, boolean doDrain) { return null; }

		@Nullable @Override public FluidStack drain(int maxDrain, boolean doDrain) { return null; }
	}

	@Override public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
		return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY
				&& (facing == null || facing.getAxis() == this.facing.getAxis())
				|| super.hasCapability(capability, facing);
	}

	@SuppressWarnings("unchecked")
	@Override @Nonnull public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			if (facing == this.facing) { return (T)this; }
			else if (facing == this.facing.getOpposite() || facing == null) { return (T)dummyTank; }
		}
		return super.getCapability(capability, facing);
	}

	IFluidTankProperties[] tankProperties = new IFluidTankProperties[] { new FluidTankProperties(null, Integer.MAX_VALUE, true, false) };

	@Override public IFluidTankProperties[] getTankProperties() { return tankProperties; }

	boolean busy = false;

	public IFluidHandler getDestination() {
		TileEntity dst = Utils.getExistingTileEntity(world, pos.offset(facing, -1));
		if (dst != null && dst.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing)) { return dst.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing); }
		return null;
	}

	@Override public int fill(FluidStack resource, boolean doFill) {
		if (busy) { return 0; }
		IFluidHandler destination = getDestination();
		if (destination == null || resource == null) { return 0; }
		int canAccept = resource.amount;
		canAccept = timeLimit != -1 ? Math.min(Math.max(timeLimit - longToInt(acceptedAmount), 0), canAccept) : canAccept;
		canAccept = keepSize != -1 ? Math.min(Math.max(keepSize - getTankFill(destination.getTankProperties(), resource), 0), canAccept) : canAccept;
		canAccept = packetLimit != -1 ? Math.min(canAccept, packetLimit) : canAccept;
		if (redstoneMode > 0) { canAccept = (int)(canAccept * ((redstoneMode == 1 ? 15 - getRSPower() : getRSPower()) / 15.0)); }
		if (canAccept == 0) { return 0; }
		FluidStack toInsert = resource.copy();
		toInsert.amount = canAccept;
		busy = true;
		int accepted = destination.fill(toInsert, doFill);
		busy = false;
		if (doFill) {
			acceptedAmount += accepted;
			packets++;
		}
		return accepted;
	}

	public static int getTankFill(IFluidTankProperties[] properties, FluidStack stack) {
		int total = 0;
		for (IFluidTankProperties prop : properties) {
			FluidStack contents = prop.getContents();
			if (contents != null && contents.isFluidEqual(stack)) { total += contents.amount; }
		}
		return total;
	}

	@Nullable @Override public FluidStack drain(FluidStack resource, boolean doDrain) { return null; }

	@Nullable @Override public FluidStack drain(int maxDrain, boolean doDrain) { return null; }

	public float isX(float ifTrue, float ifFalse) { return facing.getAxis() == EnumFacing.Axis.X ? ifTrue : ifFalse; }

	public float isY(float ifTrue, float ifFalse) { return facing.getAxis() == EnumFacing.Axis.Y ? ifTrue : ifFalse; }

	public float isZ(float ifTrue, float ifFalse) { return facing.getAxis() == EnumFacing.Axis.Z ? ifTrue : ifFalse; }

	@Override @Nonnull public float[] getBlockBounds() {
		return new float[]{
				isX(0, .125f), isY(0, .125f), isZ(0, .125f),
				isX(1, .875f), isY(1, .875f), isZ(1, .875f)
		};
	}
}
