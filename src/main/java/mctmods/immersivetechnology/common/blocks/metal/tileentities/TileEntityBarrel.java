package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ITileDrop;
import mctmods.immersivetechnology.common.tileentities.TileEntityCommonOSD;
import mctmods.immersivetechnology.common.util.TranslationKey;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;

public class TileEntityBarrel extends TileEntityCommonOSD implements IFluidTank, IPlayerInteraction, ITileDrop, IFluidTankProperties, IFluidHandler {

	public FluidStack infiniteFluid;

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		super.readCustomNBT(nbt, descPacket);
		Fluid fluid = FluidRegistry.getFluid(nbt.getString("fluid"));
		if(fluid != null) infiniteFluid = new FluidStack(fluid, Integer.MAX_VALUE);
		else if(nbt.hasKey("tank") && nbt.getCompoundTag("tank").hasKey("FluidName")) {
			fluid = FluidRegistry.getFluid(nbt.getCompoundTag("tank").getString("FluidName"));
			if(fluid != null) infiniteFluid = new FluidStack(fluid, Integer.MAX_VALUE);
		}
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		super.writeCustomNBT(nbt, descPacket);
		if(infiniteFluid != null) nbt.setString("fluid", infiniteFluid.getFluid().getName());
	}

	@Override
	public void update() {
		super.update();
		if(world.isRemote || infiniteFluid == null) return;
		for(int index = 0; index < 6; index++) {
			EnumFacing face = EnumFacing.getFront(index);
			IFluidHandler output = FluidUtil.getFluidHandler(world, getPos().offset(face), face.getOpposite());
			if(output != null) acceptedAmount += output.fill(infiniteFluid, true);
		}
	}

	@Override
	public boolean hasCapability(final Capability<?> capability, final EnumFacing facing) {
		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) return true;
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) return (T) this;
		return super.getCapability(capability, facing);
	}

	@Override
	public FluidStack getFluid() {
		return infiniteFluid;
	}

	@Override
	public int getFluidAmount() {
		return Integer.MAX_VALUE;
	}

	@Nullable
	@Override
	public FluidStack getContents() {
		return infiniteFluid;
	}

	@Override
	public int getCapacity() {
		return Integer.MAX_VALUE;
	}

	@Override
	public boolean canFill() {
		return false;
	}

	@Override
	public boolean canDrain() {
		return true;
	}

	@Override
	public boolean canFillFluidType(FluidStack fluidStack) {
		return false;
	}

	@Override
	public boolean canDrainFluidType(FluidStack fluidStack) {
		return fluidStack.getFluid() == this.infiniteFluid.getFluid();
	}

	IFluidTankProperties[] tank = new IFluidTankProperties[] { this };

	@Override
	public FluidTankInfo getInfo() {
		return new FluidTankInfo(infiniteFluid, Integer.MAX_VALUE);
	}

	@Override
	public IFluidTankProperties[] getTankProperties() {
		return tank;
	}

	@Override
	public int fill(FluidStack resource, boolean doFill) {
		return 0;
	}

	@Nullable
	@Override
	public FluidStack drain(FluidStack fluidStack, boolean b) {
		return this.drain(fluidStack.amount, b);
	}

	@Override
	public FluidStack drain(int maxDrain, boolean doDrain) {
		if(infiniteFluid == null) return null;
		if(doDrain) acceptedAmount += maxDrain;
		return new FluidStack(infiniteFluid, maxDrain);
	}

	@Override
	public boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ) {
		FluidStack fluid = FluidUtil.getFluidContained(heldItem);
		if(fluid != null) {
			infiniteFluid = new FluidStack(fluid, Integer.MAX_VALUE);
			efficientMarkDirty();
			return true;
		} else if(player.isSneaking()) {
			infiniteFluid = null;
			efficientMarkDirty();
			return true;
		}
		return FluidUtil.interactWithFluidHandler(player, hand, this);
	}

	@Override
	public void receiveMessageFromServer(NBTTagCompound message) {
		if(serializeNBT().hasKey("fluid")) infiniteFluid = new FluidStack(FluidRegistry.getFluid(message.getString("fluid")), Integer.MAX_VALUE);
		super.receiveMessageFromServer(message);
	}

	@Override
	public void notifyNearbyClients(NBTTagCompound nbt) {
		if(infiniteFluid != null) nbt.setString("fluid", infiniteFluid.getFluid().getName());
		super.notifyNearbyClients(nbt);
	}

	@Override
	public ItemStack getTileDrop(EntityPlayer player, IBlockState state) {
		ItemStack stack = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
		if(infiniteFluid != null) {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setString("fluid", infiniteFluid.getFluid().getName());
			stack.setTagCompound(tag);
		}
		return stack;
	}

	@Override
	public void readOnPlacement(EntityLivingBase placer, ItemStack stack) {
		if(stack.hasTagCompound()) readCustomNBT(stack.getTagCompound(), false);
	}

	@Override
	public String[] getOverlayText(EntityPlayer player, RayTraceResult mop, boolean hammer) {
		return new String[]{ infiniteFluid != null? text().format(infiniteFluid.getLocalizedName(), lastAcceptedAmount) : TranslationKey.GUI_EMPTY.text() };
	}

	@Override
	public TranslationKey text() {
		return TranslationKey.OVERLAY_OSD_BARREL_NORMAL_FIRST_LINE;
	}

}