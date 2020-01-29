package ferro2000.immersivetech.common.blocks.metal.tileentities;

import javax.annotation.Nullable;

import org.lwjgl.input.Keyboard;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.Utils;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.RayTraceResult;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class TileEntityBarrel extends TileEntityIEBase implements ITickable, IFluidTank, IBlockOverlayText, IPlayerInteraction, ITileDrop {

	public FluidTank tank = new FluidTank(100000);

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		this.readTank(nbt);
	}

	public void readTank(NBTTagCompound nbt) {
		tank.readFromNBT(nbt.getCompoundTag("tank"));
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		this.writeTank(nbt, false);
	}

	public void writeTank(NBTTagCompound nbt, boolean toItem) {
		boolean write = tank.getFluidAmount() > 0;
		NBTTagCompound tankTag = tank.writeToNBT(new NBTTagCompound());
		if(!toItem || write) nbt.setTag("tank", tankTag);
	}

	@Override
	public void update() {
		if(world.isRemote) return;
			boolean update = false;
			if(tank.getFluidAmount() > 0) {
				if(tank.getFluidAmount() != tank.getCapacity()) {
					FluidStack fluid = tank.getFluid();
					fluid.amount = tank.getCapacity() - tank.getFluidAmount();
					tank.fill(fluid, true);
					update = true;
				}
				for(int index = 0; index < 6; index++) {
				 	EnumFacing face = EnumFacing.getFront(index);
					TileEntity tileEntity = world.getTileEntity(getPos().offset(face));
					int out = Math.min(50, tank.getFluidAmount());
					if(tileEntity != null && tileEntity.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, face.getOpposite())) {
						IFluidHandler handler = tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, face.getOpposite());
						int accepted = handler.fill(Utils.copyFluidStackWithAmount(tank.getFluid(), out, false), false);
						FluidStack drained = this.tank.drain(accepted, false);
					if(drained != null) {
						handler.fill(drained, true);
						update = true;
					}
				}
			}
		}
		if(update) {
			this.markDirty();
			this.markContainingBlockForUpdate(null);
		}
	}

	@Override
	public String[] getOverlayText(EntityPlayer player, RayTraceResult mop, boolean hammer) {
		String amount;
		if(tank.getFluid() != null) {
			amount = tank.getFluid().getLocalizedName() + ": " + tank.getFluidAmount() + "mB";
		} else {
			amount = I18n.format(Lib.GUI + "empty");
		}
		return new String[]{amount};
	}

	@Override
	public boolean useNixieFont(EntityPlayer player, RayTraceResult mop) {
		return false;
	}

	@Override
	public boolean hasCapability(final Capability<?> capability, final EnumFacing facing) {
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) return true;
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) return (T) this.tank;
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
		return resource.amount;
	}

	@Override
	public FluidStack drain(int maxDrain, boolean doDrain) {
		return tank.drain(maxDrain, doDrain);
	}

	@Override
	public boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ) {
		if(heldItem.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
			tank.setFluid(null);
		} else if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) && heldItem.getUnlocalizedName() == "tile.air") {
			tank.setFluid(null);
		} else return false;
		if(FluidUtil.interactWithFluidHandler(player, hand, tank)) {
			this.markDirty();
			this.markContainingBlockForUpdate(null);
			return true;
		}
		return false;
	}

	@Override
	public ItemStack getTileDrop(EntityPlayer player, IBlockState state) {
		ItemStack stack = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
		NBTTagCompound tag = new NBTTagCompound();
		writeTank(tag, true);
		if(!tag.hasNoTags()) stack.setTagCompound(tag);
		return stack;
	}

	@Override
	public void readOnPlacement(EntityLivingBase placer, ItemStack stack) {
		if(stack.hasTagCompound()) readTank(stack.getTagCompound());
	}

}