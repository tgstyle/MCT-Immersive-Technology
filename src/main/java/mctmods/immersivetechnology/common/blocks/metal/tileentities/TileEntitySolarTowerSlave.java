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
		super(MultiblockSolarTower.instance, new int[] { 21, 3, 3 }, 0, true);
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
		return new int[] { 12 };
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
	public void doProcessOutput(ItemStack output) { }

	@Override
	public void doProcessFluidOutput(FluidStack output) { }

	@Override
	public void onProcessFinish(MultiblockProcess<SolarTowerRecipe> process) { }

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
			if (side == null) return master.tanks;
			else if((pos == 16 ) && side == facing) return new FluidTank[] { master.tanks[0] };
			else if(pos == 10 && side == facing.getOpposite()) return new FluidTank[] { master.tanks[1] };
		}
		return new FluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resource) {
		if(master() == null) return false;
		if((pos == 16) && side == facing) {
			if(master.tanks[iTank].getFluidAmount() >= master.tanks[iTank].getCapacity()) return false;
			if(master.tanks[iTank].getFluid() == null) return SolarTowerRecipe.findRecipeByFluid(resource.getFluid()) != null;
			else return resource.getFluid() == master.tanks[iTank].getFluid().getFluid();
		}
		return false;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, EnumFacing side) {
		return pos == 10 && side == facing.getOpposite();
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
	public float[]getBlockBounds() {
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
		if(mirrored) fl = fl.getOpposite();
		if(mirrored) fw = fw.getOpposite();
		boolean rotated = (fl == EnumFacing.NORTH || fl == EnumFacing.SOUTH);
		int h = (pos - (pos % 9)) / 9;

		if(pos == 162 || pos == 164 || pos == 166 || pos == 170 || pos == 180 || pos == 182 || pos == 186 || pos == 188) {
			float minY = 0;
			float maxY = .5f;
			if(h == 20) {
				minY = .5f;
				maxY = 1;
			}
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0, minY, 0, 1, maxY, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			if(pos == 168 || pos == 170 || pos == 182 || pos == 188) fl = fl.getOpposite();
			if(pos == 164 || pos == 170 || pos == 186 || pos == 188) fw = fw.getOpposite();
			float minX = fl == EnumFacing.WEST ? .6875f : fl == EnumFacing.EAST ? .0625f : fw == EnumFacing.EAST ? .0625f : .6875f;
			float maxX = fl == EnumFacing.EAST ? .3125f : fl == EnumFacing.WEST ? .9375f : fw == EnumFacing.EAST ? .3125f : .9375f;
			float minZ = fl == EnumFacing.NORTH ? .6875f : fl == EnumFacing.SOUTH ? .0625f : fw == EnumFacing.SOUTH ? .0625f : .6875f;
			float maxZ = fl == EnumFacing.SOUTH ? .3125f : fl == EnumFacing.NORTH ? .9375f : fw == EnumFacing.SOUTH ? .3125f : .9375f;
			minY = .5f;
			maxY = 1;
			if(pos > 170) {
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
		} else if (pos == 12) {
			float minX = fl == EnumFacing.NORTH ? 0.5f : 0f;
			float maxX = fl == EnumFacing.SOUTH ? 0.5f : 1f;
			float minZ = fw == EnumFacing.NORTH ? 0.5f : 1f;
			float maxZ = fw == EnumFacing.SOUTH ? 0.5f : 0f;

			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, 0.5, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			minX = fl == EnumFacing.NORTH ? 0.5f : 1f;
			maxX = fl == EnumFacing.SOUTH ? 0.5f : 0f;
			minZ = fw == EnumFacing.NORTH ? 0.5f : 0f;
			maxZ = fw == EnumFacing.SOUTH ? 0.5f : 1f;

			list.add(new AxisAlignedBB(minX, 0.0, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			minX = fl == EnumFacing.SOUTH ? 0.0f : fl == EnumFacing.NORTH ? 0.75f : 0.25f;
			maxX = fl == EnumFacing.SOUTH ? 0.25f : fl == EnumFacing.NORTH ? 1.0f : 0.75f;
			minZ = fw == EnumFacing.NORTH ? 0.25f : fw == EnumFacing.SOUTH ? 1.0f : 0.75f;
			maxZ = fw == EnumFacing.NORTH ? 0.0f : fw == EnumFacing.SOUTH ? 0.75f : 0.25f;

			list.add(new AxisAlignedBB(minX, 0.5, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			return list;

		} else if (pos == 13) {
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0, 0, 0, 1, 0.5, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			float minX = rotated ? 0f : 0.25f;
			float maxX = rotated ? 0.25f : 0.75f;
			float minZ = !rotated ? 0f : 0.25f;
			float maxZ = !rotated ? 0.25f : 0.75f;
			list.add(new AxisAlignedBB(minX, 0.5, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			minX = rotated ? 0.75f : 0.25f;
			maxX = rotated ? 1f : 0.75f;
			minZ = !rotated ? 0.75f : 0.25f;
			maxZ = !rotated ? 1f : 0.75f;
			list.add(new AxisAlignedBB(minX, 0.5, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		} else if (pos == 14) {
			float minX = fl == EnumFacing.SOUTH ? 0.5f : 0f;
			float maxX = fl == EnumFacing.NORTH ? 0.5f : 1f;
			float minZ = fw == EnumFacing.SOUTH ? 0.5f : 1f;
			float maxZ = fw == EnumFacing.NORTH ? 0.5f : 0f;

			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, 0.5, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			minX = fl == EnumFacing.NORTH ? 0.0f : fl == EnumFacing.SOUTH ? 0.75f : 0.25f;
			maxX = fl == EnumFacing.NORTH ? 0.25f : fl == EnumFacing.SOUTH ? 1.0f : 0.75f;
			minZ = fw == EnumFacing.SOUTH ? 0.25f : fw == EnumFacing.NORTH ? 1.0f : 0.75f;
			maxZ = fw == EnumFacing.SOUTH ? 0.0f : fw == EnumFacing.NORTH ? 0.75f : 0.25f;

			list.add(new AxisAlignedBB(minX, 0.5, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			return list;

		} else if (pos == 9 || pos == 11 || pos == 15 || pos == 17) {
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0.25, 0, 0.25, 0.75, 1, 0.75).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			int pos2 = pos;
			if (mirrored) {
				if (pos % 9 == 8 || pos % 9 == 6) {
					pos2 -= 6;
				} else if (pos % 9 == 2 || pos % 9 == 0) {
					pos2 += 6;
				}
			}

            if ((fl == EnumFacing.EAST && pos2 == 11) || (fl == EnumFacing.SOUTH && pos2 == 17) || (fl == EnumFacing.WEST && pos2 == 15) || (fl == EnumFacing.NORTH && pos2 == 9)) {
				list.add(new AxisAlignedBB(0.5, 0, 0, 1, 0.5, 0.5).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			} else if ((fl == EnumFacing.EAST && pos2 == 9) || (fl == EnumFacing.SOUTH && pos2 == 11) || (fl == EnumFacing.WEST && pos2 == 17) || (fl == EnumFacing.NORTH && pos2 == 15)) {
				list.add(new AxisAlignedBB(0.5, 0, 0.5, 1, 0.5, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			} else if ((fl == EnumFacing.EAST && pos2 == 17) || (fl == EnumFacing.SOUTH && pos2 == 15) || (fl == EnumFacing.WEST && pos2 == 9) || (fl == EnumFacing.NORTH && pos2 == 11)) {
				list.add(new AxisAlignedBB(0, 0, 0, 0.5, 0.5, 0.5).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			} else if ((fl == EnumFacing.EAST && pos2 == 15) || (fl == EnumFacing.SOUTH && pos2 == 9) || (fl == EnumFacing.WEST && pos2 == 11) || (fl == EnumFacing.NORTH && pos2 == 17)) {
				list.add(new AxisAlignedBB(0, 0, 0.5, 0.5, 0.5, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			}
			return list;
		} else if ((h == 2 || h == 5 || h == 6 || h == 13 || h == 14 || h == 17) && pos % 9 == 4) {
			float minX = rotated ? 0f : 0.25f;
			float maxX = rotated ? 0.25f : 0.75f;
			float minZ = !rotated ? 0f : 0.25f;
			float maxZ = !rotated ? 0.25f : 0.75f;
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(minX, 0.0, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			minX = rotated ? 0.75f : 0.25f;
			maxX = rotated ? 1f : 0.75f;
			minZ = !rotated ? 0.75f : 0.25f;
			maxZ = !rotated ? 1f : 0.75f;
			list.add(new AxisAlignedBB(minX, 0.0, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		} else if (h > 1 && h < 18 && (pos % 9 == 0 || pos % 9 == 2 || pos % 9 == 6 || pos % 9 == 8)) {
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0.3125, 0, 0.3125, 0.6875, 1, 0.6875).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			if (h == 3 || h == 8 || h == 11 || h == 16) {
				int pos2 = pos;
				if (mirrored) {
					if (pos % 9 == 8 || pos % 9 == 6) {
						pos2 -= 6;
					} else if (pos % 9 == 2 || pos % 9 == 0) {
						pos2 += 6;
					}
				}

				if ((fl == EnumFacing.EAST && pos2 % 9 == 2) || (fl == EnumFacing.SOUTH && pos2 % 9 == 8) || (fl == EnumFacing.WEST && pos2 % 9 == 6) || (fl == EnumFacing.NORTH && pos2 % 9 == 0)) {
					list.add(new AxisAlignedBB(0.6875, 0, 0.375, 1, 1, 0.625).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
					list.add(new AxisAlignedBB(0.375, 0, 0, 0.625, 1, 0.3125).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
				} else if ((fl == EnumFacing.EAST && pos2 % 9 == 0) || (fl == EnumFacing.SOUTH && pos2 % 9 == 2) || (fl == EnumFacing.WEST && pos2 % 9 == 8) || (fl == EnumFacing.NORTH && pos2 % 9 == 6)) {
					list.add(new AxisAlignedBB(0.6875, 0, 0.375, 1, 1, 0.625).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
					list.add(new AxisAlignedBB(0.375, 0, 0.6875, 0.625, 1, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
				} else if ((fl == EnumFacing.EAST && pos2 % 9 == 8) || (fl == EnumFacing.SOUTH && pos2 % 9 == 6) || (fl == EnumFacing.WEST && pos2 % 9 == 0) || (fl == EnumFacing.NORTH && pos2 % 9 == 2)) {
					list.add(new AxisAlignedBB(0, 0, 0.375, 0.3125, 1, 0.625).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
					list.add(new AxisAlignedBB(0.375, 0, 0, 0.625, 1, 0.3125).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
				} else if ((fl == EnumFacing.EAST && pos2 % 9 == 6) || (fl == EnumFacing.SOUTH && pos2 % 9 == 0) || (fl == EnumFacing.WEST && pos2 % 9 == 2) || (fl == EnumFacing.NORTH && pos2 % 9 == 8)) {
					list.add(new AxisAlignedBB(0, 0, 0.375, 0.3125, 1, 0.625).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
					list.add(new AxisAlignedBB(0.375, 0, 0.6875, 0.625, 1, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
				}
			}
			return list;
		} else if (h == 4 || h == 7 || h == 12 || h == 15 && (pos % 9 == 1 || pos % 9 == 3 || pos % 9 == 5 || pos % 9 == 7)) {
			List<AxisAlignedBB> list = Lists.newArrayList();
			if ((!rotated && (pos % 9 == 1 || pos % 9 == 7)) || (rotated  && (pos % 9 == 3 || pos % 9 == 5))) {
				list.add(new AxisAlignedBB(0.375, 0, 0, 0.625, 1, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			} else if ((!rotated && (pos % 9 == 3 || pos % 9 == 5)) || (rotated && (pos % 9 == 1 || pos % 9 == 7))) {
				list.add(new AxisAlignedBB(0, 0, 0.375, 1, 1, 0.625).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			}
			return list;
		} else if (h == 5 || h == 6 || h == 13 || h == 14 && (pos % 9 == 1 || pos % 9 == 3 || pos % 9 == 5 || pos % 9 == 7)) {
			List<AxisAlignedBB> list = Lists.newArrayList();
			if ( h == 6 || h == 14) {
				if ((!rotated && (pos % 9 == 1 || pos % 9 == 7)) || (rotated && (pos % 9 == 3 || pos % 9 == 5))) {
					list.add(new AxisAlignedBB(0.375, 0.5, 0.125, 0.625, 1, 0.875).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
					list.add(new AxisAlignedBB(0.3125, 0, 0.25, 0.6875, 0.5, 0.75).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
				} else if ((!rotated && (pos % 9 == 3 || pos % 9 == 5)) || (rotated && (pos % 9 == 1 || pos % 9 == 7))) {
					list.add(new AxisAlignedBB(0.125, 0.5, 0.375, 0.875, 1, 0.625).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
					list.add(new AxisAlignedBB(0.25, 0, 0.3125, 0.75, 0.5, 0.6875).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
				}
			} else {
				if ((!rotated && (pos % 9 == 1 || pos % 9 == 7)) || (rotated && (pos % 9 == 3 || pos % 9 == 5))) {
					list.add(new AxisAlignedBB(0.3125, 0.5, 0.25, 0.6875, 1, 0.75).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
					list.add(new AxisAlignedBB(0.375, 0, 0.125, 0.625, 0.5, 0.875).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
				} else if ((!rotated && (pos % 9 == 3 || pos % 9 == 5)) || (rotated && (pos % 9 == 1 || pos % 9 == 7))) {
					list.add(new AxisAlignedBB(0.25, 0.5, 0.3125, 0.75, 1, 0.6875).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
					list.add(new AxisAlignedBB(0.125, 0, 0.375, 0.875, 0.5, 0.625).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
				}
			}
			return list;
		} else if ((h == 2 || h == 3 || h == 8 || h == 9 || h == 10 || h == 11 || h == 16 || h == 17) && (pos % 9 == 3 || pos % 9 == 5)) {
			if (pos % 9 == 3) {
				float minX = fl == EnumFacing.SOUTH ? 0.0f : fl == EnumFacing.NORTH ? 0.75f : 0.25f;
				float maxX = fl == EnumFacing.SOUTH ? 0.25f : fl == EnumFacing.NORTH ? 1.0f : 0.75f;
				float minZ = fw == EnumFacing.NORTH ? 0.25f : fw == EnumFacing.SOUTH ? 1.0f : 0.75f;
				float maxZ = fw == EnumFacing.NORTH ? 0.0f : fw == EnumFacing.SOUTH ? 0.75f : 0.25f;

				return Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			} if (pos % 9 == 5) {
				float minX = fl == EnumFacing.NORTH ? 0.0f : fl == EnumFacing.SOUTH ? 0.75f : 0.25f;
				float maxX = fl == EnumFacing.NORTH ? 0.25f : fl == EnumFacing.SOUTH ? 1.0f : 0.75f;
				float minZ = fw == EnumFacing.SOUTH ? 0.25f : fw == EnumFacing.NORTH ? 1.0f : 0.75f;
				float maxZ = fw == EnumFacing.SOUTH ? 0.0f : fw == EnumFacing.NORTH ? 0.75f : 0.25f;

				return Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			}
		} else if (pos == 171) {
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
		} else if (pos == 173) {
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
		} else if (pos == 177) {
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
		} else if (pos == 179) {
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