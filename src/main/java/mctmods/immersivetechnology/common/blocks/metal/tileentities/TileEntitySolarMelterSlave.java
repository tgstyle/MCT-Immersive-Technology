package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.Lists;
import mctmods.immersivetechnology.api.ITUtils;
import mctmods.immersivetechnology.api.crafting.MeltingCrucibleRecipe;
import mctmods.immersivetechnology.common.blocks.metal.TileEntityMultiblockNewSystem;
import mctmods.immersivetechnology.common.blocks.metal.multiblocks.MultiblockSolarMelter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.ArrayList;
import java.util.List;

public class TileEntitySolarMelterSlave extends TileEntityMultiblockNewSystem<TileEntitySolarMelterSlave, MeltingCrucibleRecipe, TileEntitySolarMelterMaster> implements IEBlockInterfaces.IAdvancedSelectionBounds, IEBlockInterfaces.IAdvancedCollisionBounds {
	public TileEntitySolarMelterSlave() {
		super(MultiblockSolarMelter.instance, new int[] { 21, 3, 3 }, 0, true);
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

	TileEntitySolarMelterMaster master;

	public TileEntitySolarMelterMaster master() {
		if(master != null && !master.tileEntityInvalid) return master;
		BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
		TileEntity te = Utils.getExistingTileEntity(world, masterPos);
		master = te instanceof TileEntitySolarMelterMaster?(TileEntitySolarMelterMaster)te: null;
		return master;
	}

	@Override
	public NonNullList<ItemStack> getInventory() {
		return master() == null? NonNullList.withSize(1, ItemStack.EMPTY) : master.inventory;
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
	protected MeltingCrucibleRecipe readRecipeFromNBT(NBTTagCompound tag) {
		return MeltingCrucibleRecipe.loadFromNBT(tag);
	}

	@Override
	public MeltingCrucibleRecipe findRecipeForInsertion(ItemStack inserting) {
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
		return new int[] { 0 };
	}

	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<MeltingCrucibleRecipe> process) {
		return true;
	}

	@Override
	public void doProcessOutput(ItemStack output) { }

	@Override
	public void doProcessFluidOutput(FluidStack output) { }

	@Override
	public void onProcessFinish(MultiblockProcess<MeltingCrucibleRecipe> process) { }

	@Override
	public int getMaxProcessPerTick() {
		return 1;
	}

	@Override
	public int getProcessQueueMaxLength() {
		return 1;
	}

	@Override
	public float getMinProcessDistance(MultiblockProcess<MeltingCrucibleRecipe> process) {
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
			if(pos == 10 && side == facing.getOpposite()) return new FluidTank[] { master.tanks[0] };
		}
		return new FluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resource) {return false;}

	@Override
	protected boolean canDrainTankFrom(int iTank, EnumFacing side) {
		return pos == 10 && side == facing.getOpposite();
	}

	@Override
	public TileEntitySolarMelterSlave getTileForPos(int targetPos) {
		BlockPos target = getBlockPosForPos(targetPos);
		TileEntity tile = world.getTileEntity(target);
		return tile instanceof TileEntitySolarMelterSlave ? (TileEntitySolarMelterSlave) tile : null;
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

			return list;

		} else if (pos == 13) {
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0, 0, 0, 1, 0.5, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			if (fl.getAxis() == EnumFacing.Axis.X)
                list.add(new AxisAlignedBB(0, 0.5, 0.25, 1, 1, 0.75).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			else
				list.add(new AxisAlignedBB(0.25, 0.5, 0, 0.75, 1, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			return list;
		} else if (pos == 14) {
			float minX = fl == EnumFacing.SOUTH ? 0.5f : 0f;
			float maxX = fl == EnumFacing.NORTH ? 0.5f : 1f;
			float minZ = fw == EnumFacing.SOUTH ? 0.5f : 1f;
			float maxZ = fw == EnumFacing.NORTH ? 0.5f : 0f;

			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, 0.5, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

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
		}  else if (pos == 31) {
			return Lists.newArrayList(new AxisAlignedBB(0, 0, 0, 1, 0.25, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		} else if (h > 1 && h < 18 && (pos % 9 == 0 || pos % 9 == 2 || pos % 9 == 6 || pos % 9 == 8)) {
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0.3125, 0, 0.3125, 0.6875, 1, 0.6875).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			int pos2 = pos;
			if (mirrored) {
				if (pos % 9 == 8 || pos % 9 == 6) {
					pos2 -= 6;
				} else if (pos % 9 == 2 || pos % 9 == 0) {
					pos2 += 6;
				}
			}

			if (h == 3 || h == 8 || h == 11 || h == 16) {
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
			if (h == 2 || h == 3) {
				float yTop = h == 2 ? 1 : 0.25f;
				int pos3 = (pos2 % 9) + 9;

				if ((fl == EnumFacing.EAST && pos3 == 11) || (fl == EnumFacing.SOUTH && pos3 == 17) || (fl == EnumFacing.WEST && pos3 == 15) || (fl == EnumFacing.NORTH && pos3 == 9)) {
					list.add(new AxisAlignedBB(0.5, 0, 0, 1, yTop, 0.5).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
				} else if ((fl == EnumFacing.EAST && pos3 == 9) || (fl == EnumFacing.SOUTH && pos3 == 11) || (fl == EnumFacing.WEST && pos3 == 17) || (fl == EnumFacing.NORTH && pos3 == 15)) {
					list.add(new AxisAlignedBB(0.5, 0, 0.5, 1, yTop, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
				} else if ((fl == EnumFacing.EAST && pos3 == 17) || (fl == EnumFacing.SOUTH && pos3 == 15) || (fl == EnumFacing.WEST && pos3 == 9) || (fl == EnumFacing.NORTH && pos3 == 11)) {
					list.add(new AxisAlignedBB(0, 0, 0, 0.5, yTop, 0.5).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
				} else if ((fl == EnumFacing.EAST && pos3 == 15) || (fl == EnumFacing.SOUTH && pos3 == 9) || (fl == EnumFacing.WEST && pos3 == 11) || (fl == EnumFacing.NORTH && pos3 == 17)) {
					list.add(new AxisAlignedBB(0, 0, 0.5, 0.5, yTop, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
				}
			}
			return list;
		} else if (h == 2) {
			List<AxisAlignedBB> list = Lists.newArrayList();

			boolean toFix = !(offset[0] == 0 && offset[2] == 1) && !(offset[0] == 1 && offset[2] == 0);

			if ((!rotated && (pos % 9 == 1 || pos % 9 == 7)) || (rotated && (pos % 9 == 3 || pos % 9 == 5))) {
				list.add(new AxisAlignedBB(toFix ? 1 : 0, 0, 0, 0.5, 1, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			} else if ((!rotated && (pos % 9 == 3 || pos % 9 == 5)) || (rotated && (pos % 9 == 1 || pos % 9 == 7))) {
				list.add(new AxisAlignedBB(0, 0, toFix ? 1 : 0, 1, 1, 0.5).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			}
			return list;
		} else if (h == 3) {
			List<AxisAlignedBB> list = Lists.newArrayList();

			boolean toFix = !(offset[0] == 0 && offset[2] == 1) && !(offset[0] == 1 && offset[2] == 0);

			if ((!rotated && (pos % 9 == 1 || pos % 9 == 7)) || (rotated && (pos % 9 == 3 || pos % 9 == 5))) {
				list.add(new AxisAlignedBB(toFix ? 1 : 0, 0, 0, 0.5, 0.25, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			} else if ((!rotated && (pos % 9 == 3 || pos % 9 == 5)) || (rotated && (pos % 9 == 1 || pos % 9 == 7))) {
				list.add(new AxisAlignedBB(0, 0, toFix ? 1 : 0, 1, 0.25, 0.5).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
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
		}  else if (pos == 171) {
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

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if((pos == 16) && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return master() != null;
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if((pos == 16) && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			TileEntitySolarMelterMaster master = master();
			if(master == null)
				return null;
			return (T)master.insertionHandler;
		}
		return super.getCapability(capability, facing);
	}

}