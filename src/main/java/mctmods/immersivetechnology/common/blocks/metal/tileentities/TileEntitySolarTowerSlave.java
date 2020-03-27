package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.Lists;
import mctmods.immersivetechnology.api.ITLib;
import mctmods.immersivetechnology.api.ITUtils;
import mctmods.immersivetechnology.api.crafting.SolarTowerRecipe;
import mctmods.immersivetechnology.common.blocks.metal.TileEntityMultiblockNewSystem;
import mctmods.immersivetechnology.common.blocks.metal.multiblocks.MultiblockSolarTower;
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

public class TileEntitySolarTowerSlave extends TileEntityMultiblockNewSystem<TileEntitySolarTowerSlave, SolarTowerRecipe, TileEntitySolarTowerMaster> implements IEBlockInterfaces.IGuiTile, IEBlockInterfaces.IAdvancedSelectionBounds, IEBlockInterfaces.IAdvancedCollisionBounds {

	public TileEntitySolarTowerSlave() {
		super(MultiblockSolarTower.instance, new int[] { 7, 3, 3 }, 0, true);
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

	TileEntitySolarTowerMaster master;

	public TileEntitySolarTowerMaster master() {
		if(master != null && !master.tileEntityInvalid) return master;
		BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
		TileEntity te = Utils.getExistingTileEntity(world, masterPos);
		master = te instanceof TileEntitySolarTowerMaster?(TileEntitySolarTowerMaster)te: null;
		return master;
	}

	@Override
	public NonNullList<ItemStack> getInventory() {
		return master() == null? NonNullList.withSize(4, ItemStack.EMPTY) : master.inventory;
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
	protected SolarTowerRecipe readRecipeFromNBT(NBTTagCompound tag) {
		return SolarTowerRecipe.loadFromNBT(tag);
	}

	@Override
	public SolarTowerRecipe findRecipeForInsertion(ItemStack inserting) {
		return null;
	}

	@Override
	public int[] getEnergyPos() {
		return new int[0];
	}

	@Override
	public int[] getRedstonePos() {
		return new int[] { 10 };
	}

	@Override
	public int[] getOutputSlots() {
		return new int[0];
	}

	@Override
	public int[] getOutputTanks() {
		return new int[] { 1 };
	}

	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<SolarTowerRecipe> process) {
		return true;
	}

	@Override
	public void doProcessOutput(ItemStack output) {
		BlockPos pos = getPos().offset(facing, 2);
		TileEntity inventoryTile = this.world.getTileEntity(pos);
		if(inventoryTile != null) output = Utils.insertStackIntoInventory(inventoryTile, output, facing.getOpposite());
		if(output != null) Utils.dropStackAtPos(world, pos, output, facing);
	}

	@Override
	public void doProcessFluidOutput(FluidStack output) {
	}

	@Override
	public void onProcessFinish(MultiblockProcess<SolarTowerRecipe> process) {
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
	public float getMinProcessDistance(MultiblockProcess<SolarTowerRecipe> process) {
		return 0;
	}

	@Override
	public boolean isInWorldProcessingMachine() {
		return false;
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side) {
		if(master() != null) {
			if((pos == 3 || pos == 5) && (side == null || side.getAxis() == facing.rotateYCCW().getAxis())) {
				return new FluidTank[] { master.tanks[0] };
			} else if(pos == 7 && (side == null || side == facing)) {
				return new FluidTank[] { master.tanks[1] };
			}
		}
		return new FluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resource) {
		if(master() == null) return false;
		if((pos == 3 || pos == 5) && (side == null || side.getAxis() == facing.rotateYCCW().getAxis())) {
			if(master.tanks[iTank].getFluidAmount() >= master.tanks[iTank].getCapacity()) return false;
			if(master.tanks[iTank].getFluid() == null) return SolarTowerRecipe.findRecipeByFluid(resource.getFluid()) != null;
			else return resource.getFluid() == master.tanks[iTank].getFluid().getFluid();
		}
		return false;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, EnumFacing side) {
		return (pos == 7 && (side == null || side == facing));
	}

	@Override
	public boolean canOpenGui() {
		return formed;
	}

	@Override
	public int getGuiID() {
		return ITLib.GUIID_Solar_Tower;
	}

	@Override
	public TileEntity getGuiMaster() {
		return master();
	}

	@Override
	public TileEntitySolarTowerSlave getTileForPos(int targetPos) {
		BlockPos target = getBlockPosForPos(targetPos);
		TileEntity tile = world.getTileEntity(target);
		return tile instanceof TileEntitySolarTowerSlave ? (TileEntitySolarTowerSlave) tile : null;
	}

	@Override
	public float[] getBlockBounds() {
		if(pos == 0 || pos == 2 || pos == 6 || pos == 8) return new float[] { 0, 0, 0, 1, .5f, 1 };
		if(pos == 62 || pos == 60 || pos == 54 || pos == 52) return new float[] { 0, .5f, 0, 1, 1, 1 };
		return new float[] { 0, 0, 0, 1, 1, 1 };
	}

	@Override
	public List<AxisAlignedBB> getAdvancedColisionBounds() {
		return getAdvancedSelectionBounds();
	}

	@Override
	public List<AxisAlignedBB> getAdvancedSelectionBounds() {
		EnumFacing fl = facing;
		EnumFacing fw = facing.rotateY();
		int h = (pos - (pos % 9)) / 9;
		if(pos == 0 || pos == 2 || pos == 6 || pos == 8 || pos == 62 || pos == 60 || pos == 56 || pos == 54) {
			float minY = 0;
			float maxY = .5f;
			if(h == 6) {
				minY = .5f;
				maxY = 1;
			}
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0, minY, 0, 1, maxY, 1)
					.offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			if(pos == 6 || pos == 8 || pos == 60 || pos == 62) fl = fl.getOpposite();
			if(pos == 2 || pos == 8 || pos == 56 || pos == 62) fw = fw.getOpposite();
			float minX = fl == EnumFacing.WEST ? .6875f : fl == EnumFacing.EAST ? .0625f : fw == EnumFacing.EAST ? .0625f : .6875f;
			float maxX = fl == EnumFacing.EAST ? .3125f : fl == EnumFacing.WEST ? .9375f : fw == EnumFacing.EAST ? .3125f : .9375f;
			float minZ = fl == EnumFacing.NORTH ? .6875f : fl == EnumFacing.SOUTH ? .0625f : fw == EnumFacing.SOUTH ? .0625f : .6875f;
			float maxZ = fl == EnumFacing.SOUTH ? .3125f : fl == EnumFacing.NORTH ? .9375f : fw == EnumFacing.SOUTH ? .3125f : .9375f;
			minY = .5f;
			maxY = 1;
			if(pos > 8) {
				minY = 0;
				maxY = .5f;
			}
			list.add(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			minX = fl == EnumFacing.EAST ? .5f : fl == EnumFacing.WEST ? 0 : fw == EnumFacing.EAST ? .5f : 0;
			maxX = fl == EnumFacing.EAST ? 1 : fl == EnumFacing.WEST ? .5f : fw == EnumFacing.EAST ? 1 : .5f;
			list.add(new AxisAlignedBB(minX, minY, 0, maxX, maxY, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			minZ = fl == EnumFacing.NORTH ? 0 : fl == EnumFacing.SOUTH ? .5f : fw == EnumFacing.NORTH ? 0 : .5f;
			maxZ = fl == EnumFacing.NORTH ? .5f : fl == EnumFacing.SOUTH ? 1 : fw == EnumFacing.NORTH ? .5f : 1;
			list.add(new AxisAlignedBB(0, minY, minZ, 1, maxY, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		if((pos != 0 && pos != 54 && (pos % 9 == 0))) {
			float minX = fl == EnumFacing.WEST ? .6875f : fl == EnumFacing.EAST ? .0625f : fw == EnumFacing.EAST ? .0625f : .6875f;
			float maxX = fl == EnumFacing.EAST ? .3125f : fl == EnumFacing.WEST ? .9375f : fw == EnumFacing.EAST ? .3125f : .9375f;
			float minZ = fl == EnumFacing.NORTH ? .6875f : fl == EnumFacing.SOUTH ? .0625f : fw == EnumFacing.SOUTH ? .0625f : .6875f;
			float maxZ = fl == EnumFacing.SOUTH ? .3125f : fl == EnumFacing.NORTH ? .9375f : fw == EnumFacing.SOUTH ? .3125f : .9375f;
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			minX = fl == EnumFacing.EAST ? .5f : fl == EnumFacing.WEST ? 0 : fw == EnumFacing.EAST ? .5f : 0;
			maxX = fl == EnumFacing.EAST ? 1 : fl == EnumFacing.WEST ? .5f : fw == EnumFacing.EAST ? 1 : .5f;
			list.add(new AxisAlignedBB(minX, 0, 0, maxX, 1, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			minZ = fl == EnumFacing.NORTH ? 0 : fl == EnumFacing.SOUTH ? .5f : fw == EnumFacing.NORTH ? 0 : .5f;
			maxZ = fl == EnumFacing.NORTH ? .5f : fl == EnumFacing.SOUTH ? 1 : fw == EnumFacing.NORTH ? .5f : 1;
			list.add(new AxisAlignedBB(0, 0, minZ, 1, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		if(pos == 2 + 9 * h) {
			fw = fw.getOpposite();
			float minX = fl == EnumFacing.WEST ? .6875f : fl == EnumFacing.EAST ? .0625f : fw == EnumFacing.EAST ? .0625f : .6875f;
			float maxX = fl == EnumFacing.EAST ? .3125f : fl == EnumFacing.WEST ? .9375f : fw == EnumFacing.EAST ? .3125f : .9375f;
			float minZ = fl == EnumFacing.NORTH ? .6875f : fl == EnumFacing.SOUTH ? .0625f : fw == EnumFacing.SOUTH ? .0625f : .6875f;
			float maxZ = fl == EnumFacing.SOUTH ? .3125f : fl == EnumFacing.NORTH ? .9375f : fw == EnumFacing.SOUTH ? .3125f : .9375f;
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			minX = fl == EnumFacing.EAST ? .5f : fl == EnumFacing.WEST ? 0 : fw == EnumFacing.EAST ? .5f : 0;
			maxX = fl == EnumFacing.EAST ? 1 : fl == EnumFacing.WEST ? .5f : fw == EnumFacing.EAST ? 1 : .5f;
			list.add(new AxisAlignedBB(minX, 0, 0, maxX, 1, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			minZ = fl == EnumFacing.NORTH ? 0 : fl == EnumFacing.SOUTH ? .5f : fw == EnumFacing.NORTH ? 0 : .5f;
			maxZ = fl == EnumFacing.NORTH ? .5f : fl == EnumFacing.SOUTH ? 1 : fw == EnumFacing.NORTH ? .5f : 1;
			list.add(new AxisAlignedBB(0, 0, minZ, 1, 1, maxZ).offset(getPos().getX(), getPos().getY(), 	getPos().getZ()));
			return list;
		}
		if(pos == 6 + 9 * h) {
			fl = fl.getOpposite();
			float minX = fl == EnumFacing.WEST ? .6875f	: fl == EnumFacing.EAST ? .0625f : fw == EnumFacing.EAST ? .0625f : .6875f;
			float maxX = fl == EnumFacing.EAST ? .3125f	: fl == EnumFacing.WEST ? .9375f : fw == EnumFacing.EAST ? .3125f : .9375f;
			float minZ = fl == EnumFacing.NORTH ? .6875f : fl == EnumFacing.SOUTH ? .0625f : fw == EnumFacing.SOUTH ? .0625f : .6875f;
			float maxZ = fl == EnumFacing.SOUTH ? .3125f : fl == EnumFacing.NORTH ? .9375f : fw == EnumFacing.SOUTH ? .3125f : .9375f;
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			minX = fl == EnumFacing.EAST ? .5f : fl == EnumFacing.WEST ? 0 : fw == EnumFacing.EAST ? .5f : 0;
			maxX = fl == EnumFacing.EAST ? 1 : fl == EnumFacing.WEST ? .5f : fw == EnumFacing.EAST ? 1 : .5f;
			list.add(new AxisAlignedBB(minX, 0, 0, maxX, 1, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			minZ = fl == EnumFacing.NORTH ? 0 : fl == EnumFacing.SOUTH ? .5f : fw == EnumFacing.NORTH ? 0 : .5f;
			maxZ = fl == EnumFacing.NORTH ? .5f : fl == EnumFacing.SOUTH ? 1 : fw == EnumFacing.NORTH ? .5f : 1;
			list.add(new AxisAlignedBB(0, 0, minZ, 1, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		if(pos == 8 + 9 * h) {
			fl = fl.getOpposite();
			fw = fw.getOpposite();
			float minX = fl == EnumFacing.WEST ? .6875f : fl == EnumFacing.EAST ? .0625f : fw == EnumFacing.EAST ? .0625f : .6875f;
			float maxX = fl == EnumFacing.EAST ? .3125f : fl == EnumFacing.WEST ? .9375f : fw == EnumFacing.EAST ? .3125f : .9375f;
			float minZ = fl == EnumFacing.NORTH ? .6875f : fl == EnumFacing.SOUTH ? .0625f : fw == EnumFacing.SOUTH ? .0625f : .6875f;
			float maxZ = fl == EnumFacing.SOUTH ? .3125f : fl == EnumFacing.NORTH ? .9375f : fw == EnumFacing.SOUTH ? .3125f : .9375f;
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			minX = fl == EnumFacing.EAST ? .5f : fl == EnumFacing.WEST ? 0 : fw == EnumFacing.EAST ? .5f : 0;
			maxX = fl == EnumFacing.EAST ? 1 : fl == EnumFacing.WEST ? .5f : fw == EnumFacing.EAST ? 1 : .5f;
			list.add(new AxisAlignedBB(minX, 0, 0, maxX, 1, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			minZ = fl == EnumFacing.NORTH ? 0 : fl == EnumFacing.SOUTH ? .5f : fw == EnumFacing.NORTH ? 0 : .5f;
			maxZ = fl == EnumFacing.NORTH ? .5f : fl == EnumFacing.SOUTH ? 1 : fw == EnumFacing.NORTH ? .5f : 1;
			list.add(new AxisAlignedBB(0, 0, minZ, 1, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		return null;
	}

	@Override
	public boolean isOverrideBox(AxisAlignedBB box, EntityPlayer player, RayTraceResult mop, ArrayList<AxisAlignedBB> list) {
		return false;
	}

}