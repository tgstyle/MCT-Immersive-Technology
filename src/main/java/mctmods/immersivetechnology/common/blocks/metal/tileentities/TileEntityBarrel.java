package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ITileDrop;
import blusunrize.immersiveengineering.common.util.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import mctmods.immersivetechnology.common.tileentities.TileEntityCommonOSD;
import mctmods.immersivetechnology.common.util.IPipe;
import mctmods.immersivetechnology.common.util.TranslationKey;
import mctmods.immersivetechnology.common.util.network.BinaryMessageTileSync;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import javax.annotation.Nullable;

public class TileEntityBarrel extends TileEntityCommonOSD implements IFluidTank, IPlayerInteraction, ITileDrop, IFluidTankProperties, IFluidHandler {

	public FluidStack infiniteFluid;
	public FluidStack infiniteFluidPressurized;

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		super.readCustomNBT(nbt, descPacket);
		Fluid fluid = FluidRegistry.getFluid(nbt.getString("fluid"));
		if(fluid != null) setFluid(fluid);
		else if(nbt.hasKey("tank") && nbt.getCompoundTag("tank").hasKey("FluidName")) {
			fluid = FluidRegistry.getFluid(nbt.getCompoundTag("tank").getString("FluidName"));
			if(fluid != null) setFluid(fluid);
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
			if(output != null) {
				TileEntity tile = Utils.getExistingTileEntity(world, getPos().offset(face));
				acceptedAmount += output.fill((tile instanceof IPipe)?infiniteFluidPressurized : infiniteFluid, true);
			}
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
			setFluid(fluid.getFluid());
			efficientMarkDirty();
			return true;
		} else if(player.isSneaking()) {
			infiniteFluid = null;
			infiniteFluidPressurized = null;
			efficientMarkDirty();
			return true;
		}
		return FluidUtil.interactWithFluidHandler(player, hand, this);
	}

	public void setFluid(Fluid fluid) {
		infiniteFluid = new FluidStack(fluid, Integer.MAX_VALUE);
		infiniteFluidPressurized = new FluidStack(fluid, Integer.MAX_VALUE);
		infiniteFluidPressurized.tag = new NBTTagCompound();
		infiniteFluidPressurized.tag.setBoolean("pressurized", true);
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
		if (requestCooldown == 0) {
			ByteBuf message = Unpooled.copyBoolean(true);
			BinaryMessageTileSync.sendToServer(getPos(), message);
			requestCooldown = 20;
		}
		return new String[]{ infiniteFluid != null? text().format(infiniteFluid.getLocalizedName(), lastAcceptedAmount) : TranslationKey.GUI_EMPTY.text() };
	}

	@Override
	public void receiveMessageFromClient(ByteBuf buf, EntityPlayerMP player) {
		ByteBuf message = Unpooled.copyLong(lastAcceptedAmount);
		if(infiniteFluid != null) {
			ByteBufUtils.writeUTF8String(message, infiniteFluid.getFluid().getName());
		}
		BinaryMessageTileSync.sendToPlayer(player, getPos(), message);
	}

	@Override
	public void receiveMessageFromServer(ByteBuf buf) {
		lastAcceptedAmount = buf.readLong();
		setFluid(FluidRegistry.getFluid(ByteBufUtils.readUTF8String(buf)));
	}

	@Override
	public TranslationKey text() {
		return TranslationKey.OVERLAY_OSD_BARREL_NORMAL_FIRST_LINE;
	}

}