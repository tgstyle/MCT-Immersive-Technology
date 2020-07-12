package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedCollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedSelectionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.Lists;
import mctmods.immersivetechnology.api.ITLib;
import mctmods.immersivetechnology.api.ITUtils;
import mctmods.immersivetechnology.api.crafting.BoilerRecipe;
import mctmods.immersivetechnology.common.blocks.metal.TileEntityMultiblockNewSystem;
import mctmods.immersivetechnology.common.blocks.metal.multiblocks.MultiblockBoiler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;

import java.util.ArrayList;
import java.util.List;

public class TileEntityBoilerSlave extends TileEntityMultiblockNewSystem<TileEntityBoilerSlave, BoilerRecipe, TileEntityBoilerMaster> implements IGuiTile, IAdvancedSelectionBounds, IAdvancedCollisionBounds {

	public TileEntityBoilerSlave() {
		super(MultiblockBoiler.instance, new int[] { 3, 3, 5 }, 0, true);
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		super.readCustomNBT(nbt, descPacket);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		super.writeCustomNBT(nbt, descPacket);
	}

	@Override
	public void update() {
		if(isDummy()) ITUtils.RemoveDummyFromTicking(this);
		super.update();
	}

	@Override
	public boolean isDummy() {
		return true;
	}

	TileEntityBoilerMaster master;

	public TileEntityBoilerMaster master() {
		if(master != null && !master.tileEntityInvalid) return master;
		BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
		TileEntity te = Utils.getExistingTileEntity(world, masterPos);
		master = te instanceof TileEntityBoilerMaster?(TileEntityBoilerMaster)te: null;
		return master;
	}

	@Override
	public NonNullList<ItemStack> getInventory() {
		return master() == null? NonNullList.withSize(6, ItemStack.EMPTY) : master.inventory;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack) {
		return true;
	}

	@Override
	public int getSlotLimit(int slot) {
		return 64;
	}

	@Override
	public void doGraphicalUpdates(int slot) {
		this.markDirty();
		this.markContainingBlockForUpdate(null);
	}

	@Override
	public IFluidTank[] getInternalTanks() {
		return master() == null? new IFluidTank[0] : master.tanks;
	}

	@Override
	protected BoilerRecipe readRecipeFromNBT(NBTTagCompound tag) {
		return BoilerRecipe.loadFromNBT(tag);
	}

	@Override
	public BoilerRecipe findRecipeForInsertion(ItemStack inserting) {
		return null;
	}

	@Override
	public int[] getEnergyPos() {
		return new int[0];
	}

	@Override
	public int[] getRedstonePos() {
		return new int[] {19};
	}

	@Override
	public int[] getOutputSlots() {
		return new int[0];
	}

	@Override
	public int[] getOutputTanks() {
		return new int[] {2};
	}

	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<BoilerRecipe> process) {
		return true;
	}

	@Override
	public void doProcessOutput(ItemStack output) {

	}

	@Override
	public void doProcessFluidOutput(FluidStack output) {

	}

	@Override
	public void onProcessFinish(MultiblockProcess<BoilerRecipe> process) {

	}

	@Override
	public int getMaxProcessPerTick() {
		return 1;
	}

	@Override
	public int getProcessQueueMaxLength() {
		return 1;
	}

	@Override
	public float getMinProcessDistance(MultiblockProcess<BoilerRecipe> process) {
		return 0;
	}

	@Override
	public boolean isInWorldProcessingMachine() {
		return false;
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side) {
		if(master() != null) {
			if(pos == 35 && (side == null || side == EnumFacing.UP)) return new FluidTank[]{master.tanks[2]};
			if(pos == 5 && (side == null || side == (mirrored ? facing.rotateY() : facing.rotateYCCW()))) return new FluidTank[]{master.tanks[1]};
			if(pos == 9 && (side == null || side == (mirrored ? facing.rotateYCCW() : facing.rotateY()))) return new FluidTank[]{master.tanks[0]};
		}
		return ITUtils.emptyIFluidTankList;
	}

	@Override
	protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resource) {
		if(master() == null) return false;
		if(pos == 9 && (side == null || side == (mirrored? facing.rotateYCCW() : facing.rotateY()))) {
			if(master.tanks[iTank].getFluidAmount() >= master.tanks[iTank].getCapacity()) return false;
			if(master.tanks[iTank].getFluid() == null) return BoilerRecipe.findFuelByFluid(resource.getFluid()) != null;
			else return resource.getFluid() == master.tanks[iTank].getFluid().getFluid();
		}
		if(pos == 5 && (side == null || side == (mirrored?facing.rotateY() : facing.rotateYCCW()))) {
			if(master.tanks[1].getFluidAmount() >= master.tanks[1].getCapacity()) return false;
			if(master.tanks[1].getFluid() == null) return BoilerRecipe.findRecipeByFluid(resource.getFluid()) != null;
			else return resource.getFluid() == master.tanks[1].getFluid().getFluid();
		}
		return false;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, EnumFacing side) {
		return pos == 35 && (side == null || side == EnumFacing.UP);
	}

	@Override
	public boolean canOpenGui() {
		return formed;
	}

	@Override
	public int getGuiID() {
		return ITLib.GUIID_Boiler;
	}

	@Override
	public TileEntity getGuiMaster() {
		return master();
	}

	@Override
	public TileEntityBoilerSlave getTileForPos(int targetPos) {
		BlockPos target = getBlockPosForPos(targetPos);
		TileEntity tile = world.getTileEntity(target);
		return tile instanceof TileEntityBoilerSlave ? (TileEntityBoilerSlave) tile : null;
	}

	@Override
	public float[] getBlockBounds() {
		return null;
	}

	@Override
	public List<AxisAlignedBB> getAdvancedColisionBounds() {
		return getAdvancedSelectionBounds();
	}

	@Override
	public List<AxisAlignedBB> getAdvancedSelectionBounds() {
		double[] boundingArray = new double[6];
		EnumFacing fl = facing;
		EnumFacing fw = facing.rotateY();
		if(mirrored) fw = fw.getOpposite();
		if(pos < 15 && pos != 5 && pos != 9) {
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0, 0, 0, 1, .5f, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			if(pos == 0 || pos == 10) {
				if(pos != 0) fl = fl.getOpposite();
				boundingArray = ITUtils.smartBoundingBox(.375f, .375f, .75f, 0, .5f, 1, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
				boundingArray = ITUtils.smartBoundingBox(0, .5f, .5f, .25f, .5f, 1, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			}
			if(pos == 1 || pos == 2 || pos == 11 || pos == 12) {
				if(pos > 2) fl = fl.getOpposite();
				boundingArray = ITUtils.smartBoundingBox(.4375f, .4375f, 0, 0, .6875f, .9375f, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
				boundingArray = ITUtils.smartBoundingBox(0, .625f, 0, 0, .5f, 1, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			}
			if(pos == 3 || pos == 13) {
				if(pos != 3) fl = fl.getOpposite();
				boundingArray = ITUtils.smartBoundingBox(.4375f, .4375f, 0, .75f, .6875f, .9375f, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
				boundingArray = ITUtils.smartBoundingBox(.375f, .375f, .25f, .5f, .5f, 1, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
				boundingArray = ITUtils.smartBoundingBox(0, .625f, 0, .5f, .5f, 1, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
				boundingArray = ITUtils.smartBoundingBox(0, .5f, .5f, .25f, .5f, 1, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			}
			if(pos == 4) {
				boundingArray = ITUtils.smartBoundingBox(.625f, .125f, .125f, .75f, .5f, 1, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
				boundingArray = ITUtils.smartBoundingBox(.625f, .125f, .75f, .125f, .5f, 1, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			}
			if(pos == 14) {
				boundingArray = ITUtils.smartBoundingBox(.5f, .25f, .375f, .375f, .625f, 1, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
				boundingArray = ITUtils.smartBoundingBox(.4375f, .1875f, .3125f, .3125f, .5f, .625f, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			}
			return list;
		}
		if(pos == 15 || pos == 25) {
			if(pos != 15) fl = fl.getOpposite();
			boundingArray = ITUtils.smartBoundingBox(0, .25f, .5f, 0, 0, 1, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, .5f, .125f, .5f, .25f, 1, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, .75f, .125f, .5f, 0, .25f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		if(pos == 16 || pos == 17 || pos == 26 || pos == 27 || pos == 31 || pos == 32 || pos == 41 || pos == 42) {
			if((pos>17 && pos<31) || pos>32) fl = fl.getOpposite();
			if(pos > 27) {
				boundingArray = ITUtils.smartBoundingBox(0, .25f, 0, 0, 0, .5f, fl, fw);
				List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
				boundingArray = ITUtils.smartBoundingBox(0, .5f, 0, 0, .5f, 1, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
				return list;
			} else {
				boundingArray = ITUtils.smartBoundingBox(0, .25f, 0, 0, 0, 1, fl, fw);
				return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			}
		}
		if(pos == 18 || pos == 28 || pos == 33 || pos == 43) {
			if(pos != 18 && pos != 33) fl = fl.getOpposite();
			if(pos>28) {
				boundingArray = ITUtils.smartBoundingBox(0, .25f, 0, .25f, 0, .5f, fl, fw);
				List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
				boundingArray = ITUtils.smartBoundingBox(0, .5f, 0, .25f, .5f, 1, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
				return list;
			} else {
				boundingArray = ITUtils.smartBoundingBox(0, .25f, 0, .25f, 0, 1, fl, fw);
				return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			}
		}
		if(pos == 19) {
			boundingArray = ITUtils.smartBoundingBox(.5f, 0, 0, 0, 0, 1, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, .75f, .25f, .25f, .5f, 1, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		if(pos == 20) {
			boundingArray = ITUtils.smartBoundingBox(0, 0, 0, 0, 0, .375f, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, 0, .125f, 0, .375f, 1, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		if(pos == 23) {
			boundingArray = ITUtils.smartBoundingBox(0, 0, 0, .25f, 0, 1, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, 0, .75f, 0, .25f, 1, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		if(pos == 24) {
			boundingArray = ITUtils.smartBoundingBox(.125f, .125f, 0, .125f, .375f, 1, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.25f, .25f, .875f, 0, .5f, 1, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.875f, 0, .25f, .25f, .5f, 1, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, .875f, .3125f, .3125f, .5625f, .9375f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		if(pos == 29) {
			boundingArray = ITUtils.smartBoundingBox(.8125f, 0, .375f, .375f, .625f, .875f, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.4375f, .1875f, .3125f, .3125f, .5625f, .9375f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, .5625f, .0625f, .0625f, 0, 1, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.5f, .25f, .375f, .375f, 0, .5625f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		if(pos == 30 || pos == 40) {
			if(pos != 30) fl = fl.getOpposite();
			boundingArray = ITUtils.smartBoundingBox(0, .5f, .125f, .5f, 0, .25f, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, .25f, .5f, 0, 0, .5f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, .5f, .5f, 0, .5f, 1, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, .75f, .125f, .5f, .25f, .5f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		if(pos == 35) {
			boundingArray = ITUtils.smartBoundingBox(0, 0, 0, 0, .125f, 1, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, 0, .125f, 0, 0, .125f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		if(pos == 38) {
			boundingArray = ITUtils.smartBoundingBox(0, 0, 0, .25f, 0, 1, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, 0, .75f, 0, 0, .25f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		if(pos == 39) {
			boundingArray = ITUtils.smartBoundingBox(.125f, .125f, 0, .125f, 0, .125f, fl, fw);
			return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		if(pos == 44) {
			boundingArray = ITUtils.smartBoundingBox(0, .5625f, .0625f, .0625f, 0, .5f, fl, fw);
			return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		return null;
	}

	@Override
	public boolean isOverrideBox(AxisAlignedBB box, EntityPlayer player, RayTraceResult mop, ArrayList<AxisAlignedBB> list) {
		return false;
	}
}