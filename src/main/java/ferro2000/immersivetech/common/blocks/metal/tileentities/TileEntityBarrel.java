package ferro2000.immersivetech.common.blocks.metal.tileentities;

import javax.annotation.Nullable;

import org.lwjgl.input.Keyboard;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.util.Utils;
import ferro2000.immersivetech.ImmersiveTech;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;

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

	private int acceptedAmount;
	private int updateClient = 0;

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

	@Override
	public void update() {
		if(world.isRemote || tank.getFluidAmount() == 0) return;
		boolean update = false;
		if(tank.getFluidAmount() > 0) {
			if(tank.getFluidAmount() != tank.getCapacity()) {
				FluidStack filled = tank.getFluid();
				filled.amount = tank.getCapacity() - tank.getFluidAmount();
				tank.fill(filled, true);
				update = true;
			}
		}
		for(int index = 0; index < 6; index++) {
			EnumFacing face = EnumFacing.getFront(index);
			if(face == null) break;
			TileEntity tileEntity = world.getTileEntity(getPos().offset(face));
			if(tileEntity != null && tileEntity.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, face.getOpposite())) {
				IFluidHandler output = tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, face.getOpposite());
				FluidStack accepted = Utils.copyFluidStackWithAmount(tank.getFluid(), tank.getCapacity(), true);
				accepted.amount = output.fill(Utils.copyFluidStackWithAmount(accepted, accepted.amount, false), true);
				if(updateClient == 0) acceptedAmount = accepted.amount;
				output.fill(accepted, true);
				update=true;
			}
		}
		if(update) {
			this.markDirty();
			this.markContainingBlockForUpdate(null);
		}
		if(updateClient >= 19) {
			updateClient = 0;
		} else {
			updateClient++;
		}
	}

	@Override
	public String[] getOverlayText(EntityPlayer player, RayTraceResult mop, boolean hammer) {
		String amount = null;
		if(tank.getFluid() != null) {
			amount = tank.getFluid().getLocalizedName() + " " + I18n.format(ImmersiveTech.MODID +".osd.barrel.output") + ": " + acceptedAmount + "mB";
		} else {
			amount = I18n.format(ImmersiveTech.MODID + ".osd.barrel.empty");
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