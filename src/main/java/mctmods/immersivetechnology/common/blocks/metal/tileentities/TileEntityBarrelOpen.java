package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ITileDrop;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;
import java.util.Random;

public class TileEntityBarrelOpen extends TileEntityIEBase implements ITickable, IBlockOverlayText, IPlayerInteraction, ITileDrop, IComparatorOverride {

	public FluidTank tank = new FluidTank(12000);

	public static final int IGNITION_TEMPERATURE = 573;

	private int acceptedAmount = 0;
	private int lastRandom = 0;

	SidedFluidHandler[] sidedFluidHandler = {new SidedFluidHandler(this, EnumFacing.DOWN), new SidedFluidHandler(this, EnumFacing.UP)};
	SidedFluidHandler nullsideFluidHandler = new SidedFluidHandler(this, null);
	
	private static Random RANDOM = new Random();

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
		if(world.isRemote) return;
		boolean update = false;
		int random = 1 + RANDOM.nextInt(100);
		if(random == lastRandom) {
			if(tank.getFluid() == null || tank.getFluid().getFluid() == FluidRegistry.WATER) {
				float temp = world.getBiomeProvider().getTemperatureAtHeight(world.getBiome(pos).getTemperature(pos), pos.getY());
				if(world.isRaining() && world.canSeeSky(pos) && temp > 0.05F && temp < 2.0F) {
					int amount = 100;
					if(world.isThundering()) amount = 200;
					this.tank.fill(new FluidStack(FluidRegistry.WATER, amount), true);
					update = true;
				} else if(temp >= 2.0F) {
					this.tank.drain(100, true);
					update = true;
				}
			}
		}
		lastRandom = random;
		if(tank.getFluidAmount() > 0) {
			EnumFacing face = EnumFacing.DOWN;
			IFluidHandler output = FluidUtil.getFluidHandler(world, pos.offset(face), face.getOpposite());
			if(output != null) {
				FluidStack accepted = Utils.copyFluidStackWithAmount(tank.getFluid(), Math.min(40, tank.getFluidAmount()), false);
				accepted.amount = output.fill(Utils.copyFluidStackWithAmount(accepted, accepted.amount, true), false);
				if(accepted.amount > 0) {
					int drained = output.fill(Utils.copyFluidStackWithAmount(accepted, accepted.amount, false), true);
					this.tank.drain(drained, true);
					update=true;
				}
			}
		}
		if(update) {
			efficientMarkDirty();
			this.markContainingBlockForUpdate(null);
		}
	}

	@Override
	public String[] getOverlayText(EntityPlayer player, RayTraceResult mop, boolean hammer) {
		if(Utils.isFluidRelatedItemStack(player.getHeldItem(EnumHand.MAIN_HAND))) {
			String string = null;
			if(tank.getFluid()!=null) {
				string = tank.getFluid().getLocalizedName()+": " + tank.getFluidAmount() + "mB";
			} else {
				string = I18n.format(Lib.GUI+"empty");
			}
			return new String[]{string};
		}
		return null;
	}

	@Override
	public boolean useNixieFont(EntityPlayer player, RayTraceResult mop) {
		return false;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && (facing == null || facing.getAxis() == Axis.Y)) return true;
		return super.hasCapability(capability, facing);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && (facing == null || facing.getAxis() == Axis.Y)) return (T)(facing == null ? nullsideFluidHandler : sidedFluidHandler[facing.ordinal()]);
		return super.getCapability(capability, facing);
	}

	@Override
	public int getComparatorInputOverride()	{
		return (int)(15 * (tank.getFluidAmount() / (float)tank.getCapacity()));
	}

	public boolean isFluidValid(FluidStack fluid) {
		return fluid != null && fluid.getFluid() != null && !fluid.getFluid().isGaseous(fluid);
	}

	static class SidedFluidHandler implements IFluidHandler {
		TileEntityBarrelOpen barrel;
		EnumFacing facing;

		SidedFluidHandler(TileEntityBarrelOpen barrel, EnumFacing facing) {
			this.barrel = barrel;
			this.facing = facing;
		}

		@Override
		public int fill(FluidStack resource, boolean doFill) {
			if(resource == null || facing != EnumFacing.UP || !barrel.isFluidValid(resource)) return 0;
			int input = barrel.tank.fill(resource, doFill);
			if(input > 0) {
				barrel.markDirty();
				barrel.markContainingBlockForUpdate(null);
			}
			return input;
		}

		@Override
		public FluidStack drain(FluidStack resource, boolean doDrain) {
			if(resource == null) return null;
			return this.drain(resource.amount, doDrain);
		}

		@Override
		public FluidStack drain(int maxDrain, boolean doDrain) {
			if(facing != EnumFacing.DOWN) return null;
			FluidStack fluid = barrel.tank.drain(maxDrain, doDrain);
			if(fluid != null && fluid.amount > 0) {
				barrel.markDirty();
				barrel.markContainingBlockForUpdate(null);
			}
			return fluid;
		}

		@Override
		public IFluidTankProperties[] getTankProperties() {
			return barrel.tank.getTankProperties();
		}
	}

	@Override
	public boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ) {
		FluidStack fluid = FluidUtil.getFluidContained(heldItem);
		if(!isFluidValid(fluid)) {
			ChatUtils.sendServerNoSpamMessages(player, new TextComponentTranslation(Lib.CHAT_INFO + "noGasAllowed"));
			return true;
		}
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