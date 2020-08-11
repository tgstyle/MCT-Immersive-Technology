package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import javax.annotation.Nullable;

import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.common.Config.ITConfig.Barrels;
import mctmods.immersivetechnology.common.util.ITFluidTank;
import mctmods.immersivetechnology.common.util.TranslationKey;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public class TileEntityBarrelSteel extends TileEntityIEBase implements ITickable, IEBlockInterfaces.IBlockOverlayText, IEBlockInterfaces.IConfigurableSides, IEBlockInterfaces.IPlayerInteraction, IEBlockInterfaces.ITileDrop, IEBlockInterfaces.IComparatorOverride, ITFluidTank.TankListener {

	private static int tankSize = Barrels.barrel_steel_tankSize;
	private static int transferSpeed = Barrels.barrel_steel_transferSpeed;

	public int[] sideConfig = {1, 0};

	public ITFluidTank tank;

	private int sleep = 0;

	SidedFluidHandler[] sidedFluidHandler = {new SidedFluidHandler(this, EnumFacing.DOWN), new SidedFluidHandler(this, EnumFacing.UP)};
	SidedFluidHandler nullsideFluidHandler = new SidedFluidHandler(this, null);

	public void createTank() {
		tank = new ITFluidTank(tankSize, this);
	}

	public TileEntityBarrelSteel() {
		createTank();
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		sideConfig = nbt.getIntArray("sideConfig");
		if(sideConfig == null || sideConfig.length < 2) sideConfig = new int[]{-1, 0};
		this.readTank(nbt);
	}

	public void readTank(NBTTagCompound nbt) {
		tank.readFromNBT(nbt.getCompoundTag("tank"));
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		nbt.setIntArray("sideConfig", sideConfig);
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
		for(int index = 0; index < 2; index++) {
			if(tank.getFluidAmount() > 0 && sideConfig[index] == 1) {
				EnumFacing face = EnumFacing.getFront(index);
				IFluidHandler output = FluidUtil.getFluidHandler(world, getPos().offset(face), face.getOpposite());
				if(output != null) {
					if(sleep == 0) {
						FluidStack accepted = Utils.copyFluidStackWithAmount(tank.getFluid(), Math.min(transferSpeed, tank.getFluidAmount()), false);
						if(accepted == null) {
							sleep = 20;
							return;
						}
						accepted.amount = output.fill(Utils.copyFluidStackWithAmount(accepted, accepted.amount, true), false);
						if(accepted.amount > 0) {
							int drained = output.fill(Utils.copyFluidStackWithAmount(accepted, accepted.amount, false), true);
							tank.drain(drained, true);
							sleep = 0;
						} else {
							sleep = 20;
						}
					} else {
						sleep--;
					}
				}
			}
		}
	}

	@Override
	public void TankContentsChanged() {
		this.markContainingBlockForUpdate(null);
	}

	@Override
	public String[] getOverlayText(EntityPlayer player, RayTraceResult mop, boolean hammer) {
		if(Utils.isFluidRelatedItemStack(player.getHeldItem(EnumHand.MAIN_HAND))) {
			FluidStack fluid = tank.getFluid();
			return (fluid != null)?
					new String[]{TranslationKey.OVERLAY_OSD_BARREL_NORMAL_FIRST_LINE.format(fluid.getLocalizedName(), fluid.amount)}:
					new String[]{TranslationKey.GUI_EMPTY.text()};
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

	@Override
	public SideConfig getSideConfig(int side) {
		if(side > 1) return SideConfig.NONE;
		return SideConfig.values()[this.sideConfig[side] + 1];
	}

	@Override
	public boolean toggleSide(int side, EntityPlayer p) {
		if(side != 0 && side != 1) return false;
		sideConfig[side]++;
		if(sideConfig[side] > 1) sideConfig[side] = -1;
		this.markDirty();
		this.markContainingBlockForUpdate(null);
		world.addBlockEvent(getPos(), this.getBlockType(), 0, 0);
		return true;
	}

	@Override
	public boolean receiveClientEvent(int id, int arg) {
		if(id == 0) {
			this.markContainingBlockForUpdate(null);
			return true;
		}
		return false;
	}

	public boolean isFluidValid(FluidStack fluid) {
		return fluid != null && fluid.getFluid() != null;
	}

	public static class SidedFluidHandler implements IFluidHandler {
		public TileEntityBarrelSteel barrel;
		EnumFacing facing;

		SidedFluidHandler(TileEntityBarrelSteel barrel, EnumFacing facing) {
			this.barrel = barrel;
			this.facing = facing;
		}

		@Override
		public int fill(FluidStack resource, boolean doFill) {
			if(resource == null || (facing != null && barrel.sideConfig[facing.ordinal()] != 0) || !barrel.isFluidValid(resource)) return 0;
			int input = barrel.tank.fill(resource, doFill);
			return input;
		}

		@Override
		public FluidStack drain(FluidStack resource, boolean doDrain) {
			if(resource == null) return null;
			return this.drain(resource.amount, doDrain);
		}

		@Override
		public FluidStack drain(int maxDrain, boolean doDrain) {
			if(facing != null && barrel.sideConfig[facing.ordinal()] != 1) return null;
			FluidStack output = barrel.tank.drain(maxDrain, doDrain);
			return output;
		}

		@Override
		public IFluidTankProperties[] getTankProperties() {
			return barrel.tank.getTankProperties();
		}
	}

	@Override
	public boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ) {
		if(FluidUtil.interactWithFluidHandler(player, hand, tank)) {
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