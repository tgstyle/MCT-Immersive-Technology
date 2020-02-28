package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ITileDrop;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.common.util.TranslationKey;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;

public class TileEntityBarrel extends TileEntityIEBase implements ITickable, IFluidTank, IBlockOverlayText, IPlayerInteraction, ITileDrop {

	public FluidTank tank = new FluidTank(100000);

	private int acceptedAmount = 0;
	private int updateClient = 0;
	private int lastAmount;

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		acceptedAmount = nbt.getInteger("acceptedAmount");
		this.readTank(nbt);
	}

	public void readTank(NBTTagCompound nbt) {
		tank.readFromNBT(nbt.getCompoundTag("tank"));
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		nbt.setInteger("acceptedAmount", acceptedAmount);
		this.writeTank(nbt, false);
	}

	public void writeTank(NBTTagCompound nbt, boolean toItem) {
		boolean write = tank.getFluidAmount() > 0;
		NBTTagCompound tankTag = tank.writeToNBT(new NBTTagCompound());
		if(!toItem || write) nbt.setTag("tank", tankTag);
	}

	public void efficientMarkDirty() { // !!!!!!! only use it within update() function !!!!!!!
		world.getChunkFromBlockCoords(this.getPos()).markDirty();
	}

	@Override
	public void update() {
		if(world.isRemote || tank.getFluidAmount() == 0) return;
		boolean update = false;
		if(tank.getFluidAmount() != tank.getCapacity()) {
			FluidStack filled = tank.getFluid();
			filled.amount = tank.getCapacity() - tank.getFluidAmount();
			tank.fill(filled, true);
			update = true;
		}
		for(int index = 0; index < 6; index++) {
			EnumFacing face = EnumFacing.getFront(index);
			IFluidHandler output = FluidUtil.getFluidHandler(world, getPos().offset(face), face.getOpposite());
			if(output != null) {
				FluidStack accepted = Utils.copyFluidStackWithAmount(tank.getFluid(), tank.getCapacity(), false);
				accepted.amount = output.fill(Utils.copyFluidStackWithAmount(accepted, accepted.amount, true), false);
				if(accepted.amount > 0) {
					lastAmount = accepted.amount;
					output.fill(accepted, true);
					update=true;
				}
			}
		}
		if(updateClient >= 20) {
			acceptedAmount = lastAmount;
			lastAmount = 0;
			updateClient = 1;
		} else {
			updateClient++;
		}
		if(update) {
			efficientMarkDirty();
			this.markContainingBlockForUpdate(null);
		}
	}

	@Override
	public String[] getOverlayText(EntityPlayer player, RayTraceResult mop, boolean hammer) {
		FluidStack fluid = tank.getFluid();
		return (fluid != null)?
				new String[]{TranslationKey.OVERLAY_OSD_BARREL_NORMAL_FIRST_LINE.format(fluid.getLocalizedName(), acceptedAmount)}:
				new String[]{TranslationKey.GUI_EMPTY.text()};
	}

	@Override
	public boolean useNixieFont(EntityPlayer player, RayTraceResult mop) {
		return false;
	}

	@Override
	public boolean hasCapability(final Capability<?> capability, final EnumFacing facing) {
		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) return true;
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
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
		return tank.getCapacity();
	}

	@Override
	public FluidStack drain(int maxDrain, boolean doDrain) {
		return tank.drain(0, false);
	}

	@Override
	public boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ) {
		FluidStack fluid = FluidUtil.getFluidContained(heldItem);
		if(fluid != null) {
			tank.setFluid(null);
		} else if(player.isSneaking()) {
			tank.setFluid(null);
		} else return false;
		if(FluidUtil.interactWithFluidHandler(player, hand, tank)) {
			efficientMarkDirty();
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