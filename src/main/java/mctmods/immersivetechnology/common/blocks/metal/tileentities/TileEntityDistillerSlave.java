package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedCollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedSelectionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.Lists;
import mctmods.immersivetechnology.api.ITLib;
import mctmods.immersivetechnology.api.ITUtils;
import mctmods.immersivetechnology.api.crafting.DistillerRecipe;
import mctmods.immersivetechnology.common.blocks.metal.TileEntityMultiblockNewSystem;
import mctmods.immersivetechnology.common.blocks.metal.multiblocks.MultiblockDistiller;
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

public class TileEntityDistillerSlave extends TileEntityMultiblockNewSystem<TileEntityDistillerSlave, DistillerRecipe, TileEntityDistillerMaster> implements IGuiTile, IAdvancedSelectionBounds, IAdvancedCollisionBounds {

	public TileEntityDistillerSlave() {
		super(MultiblockDistiller.instance, new int[] { 3, 3, 3 }, 16000, true);
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

	TileEntityDistillerMaster master;

	public TileEntityDistillerMaster master() {
		if(master != null && !master.tileEntityInvalid) return master;
		BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
		TileEntity te = Utils.getExistingTileEntity(world, masterPos);
		master = te instanceof TileEntityDistillerMaster?(TileEntityDistillerMaster)te: null;
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
	protected DistillerRecipe readRecipeFromNBT(NBTTagCompound tag) {
		return DistillerRecipe.loadFromNBT(tag);
	}

	@Override
	public DistillerRecipe findRecipeForInsertion(ItemStack inserting) {
		return null;
	}

	@Override
	public int[] getEnergyPos() {
		return new int[] {9};
	}

	@Override
	public int[] getRedstonePos() {
		return new int[] {11};
	}

	@Override
	public int[] getOutputSlots() {
		return null ;
	}

	@Override
	public int[] getOutputTanks() {
		return new int[] {1};
	}

	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<DistillerRecipe> process) {
		return true;
	}

	@Override
	public void doProcessOutput(ItemStack output) {
		BlockPos pos = getPos().add(0, -1, 0).offset(facing.getOpposite(), -2);
		TileEntity inventoryTile = this.world.getTileEntity(pos);
		if(inventoryTile != null) output = Utils.insertStackIntoInventory(inventoryTile, output, facing.getOpposite());
		if(output != null) Utils.dropStackAtPos(world, pos, output, facing);
	}

	@Override
	public void doProcessFluidOutput(FluidStack output) {
	}

	@Override
	public void onProcessFinish(MultiblockProcess<DistillerRecipe> process) {
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
	public float getMinProcessDistance(MultiblockProcess<DistillerRecipe> process) {
		return 0;
	}

	@Override
	public boolean isInWorldProcessingMachine() {
		return false;
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side) {
		if(master() != null) {
			if(pos == 5 && (side == null || side == (mirrored ? facing.rotateYCCW():facing.rotateY()))) {
				return new FluidTank[] {master.tanks[0]};
			} else if(pos == 3 && (side == null || side == (mirrored ? facing.rotateY():facing.rotateYCCW()))) {
				return new FluidTank[] {master.tanks[1]};
			}
		}
		return new FluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resource) {
		if(master() == null) return false;
		if(pos == 5 && (side == null || side == (mirrored ? facing.rotateYCCW():facing.rotateY()))) {
			if(master.tanks[iTank].getFluidAmount() >= master.tanks[iTank].getCapacity()) return false;
			if(master.tanks[iTank].getFluid() == null) return DistillerRecipe.findRecipeByFluid(resource.getFluid()) != null;
			else return resource.getFluid() == master.tanks[iTank].getFluid().getFluid();
		}
		return false;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, EnumFacing side) {
		return (pos == 3 && (side == null || side == (mirrored ? facing.rotateY():facing.rotateYCCW())));
	}

	@Override
	public boolean canOpenGui() {
		return formed;
	}

	@Override
	public int getGuiID() {
		return ITLib.GUIID_Distiller;
	}

	@Override
	public TileEntity getGuiMaster() {
		return master();
	}

	@Override
	public TileEntityDistillerSlave getTileForPos(int targetPos) {
		BlockPos target = getBlockPosForPos(targetPos);
		TileEntity tile = world.getTileEntity(target);
		return tile instanceof TileEntityDistillerSlave ? (TileEntityDistillerSlave) tile : null;
	}

	@Override
	public float[] getBlockBounds() {
		if(pos > 0 && pos < 9 && pos != 5 && pos != 3 && pos != 7) return new float[] {0, 0, 0, 1, .5f, 1};
		if(pos == 11) return new float[] {facing == EnumFacing.WEST ? .5f:0, 0, facing == EnumFacing.NORTH ? .5f:0, facing == EnumFacing.EAST ? .5f:1, 1, facing == EnumFacing.SOUTH ? .5f:1};
		if(pos == 21 || pos == 24) return new float[] {0, 0, 0, 1, .5f, 1};
		return new float[] {0, 0, 0, 1, 1, 1};
	}

	@Override
	public List<AxisAlignedBB> getAdvancedColisionBounds() {
		return getAdvancedSelectionBounds();
	}

	@Override
	public List<AxisAlignedBB> getAdvancedSelectionBounds() {
		EnumFacing fl = facing;
		EnumFacing fw = facing.rotateY();
		if(mirrored) fw = fw.getOpposite();
		if(pos == 2) {
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0, 0, 0, 1, .5f, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			float minX = fl == EnumFacing.WEST ? .625f: fl == EnumFacing.EAST ? .125f: .125f;
			float maxX = fl == EnumFacing.EAST ? .375f: fl == EnumFacing.WEST ? .875f: .25f;
			float minZ = fl == EnumFacing.NORTH ? .625f: fl == EnumFacing.SOUTH ? .125f: .125f;
			float maxZ = fl == EnumFacing.SOUTH ? .375f: fl == EnumFacing.NORTH ? .875f: .25f;
			list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			minX = fl == EnumFacing.WEST ? .625f: fl == EnumFacing.EAST ? .125f: .75f;
			maxX = fl == EnumFacing.EAST ? .375f: fl == EnumFacing.WEST ? .875f: .875f;
			minZ = fl == EnumFacing.NORTH ? .625f: fl == EnumFacing.SOUTH ? .125f: .75f;
			maxZ = fl == EnumFacing.SOUTH ? .375f: fl == EnumFacing.NORTH ? .875f: .875f;
			list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		if(pos == 6 || pos == 8) {
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0, 0, 0, 1, .5f, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			fl = fl.getOpposite();
			if(pos == 8) fw = fw.getOpposite();
			float minX = fl == EnumFacing.WEST ? .6875f: fl == EnumFacing.EAST ? .0625f: fw == EnumFacing.EAST ? .0625f: .6875f;
			float maxX = fl == EnumFacing.EAST ? .3125f: fl == EnumFacing.WEST ? .9375f: fw == EnumFacing.EAST ? .3125f: .9375f;
			float minZ = fl == EnumFacing.NORTH ? .6875f: fl == EnumFacing.SOUTH ? .0625f: fw == EnumFacing.SOUTH ? .0625f: .6875f;
			float maxZ = fl == EnumFacing.SOUTH ? .3125f: fl == EnumFacing.NORTH ? .9375f: fw == EnumFacing.SOUTH ? .3125f: .9375f;
			list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, 1.1875f, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		return null;
	}

	@Override
	public boolean isOverrideBox(AxisAlignedBB box, EntityPlayer player, RayTraceResult mop, ArrayList<AxisAlignedBB> list) {
		return false;
	}

}