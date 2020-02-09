package mctmods.immersivetechnology.common.blocks.stone.tileentities;

import java.util.ArrayList;
import java.util.List;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import com.google.common.collect.Lists;
import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IActiveState;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedCollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedSelectionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IProcessTile;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import mctmods.immersivetechnology.api.ITLib;
import mctmods.immersivetechnology.api.ITUtils;
import mctmods.immersivetechnology.common.blocks.stone.multiblocks.MultiblockCokeOvenAdvanced;
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

public class TileEntityCokeOvenAdvancedSlave extends TileEntityMultiblockPart<TileEntityCokeOvenAdvancedSlave> implements IIEInventory, IGuiTile, IProcessTile, IActiveState, IAdvancedSelectionBounds, IAdvancedCollisionBounds {

	private static final int[] size = { 4, 3, 3 };

	public TileEntityCokeOvenAdvancedSlave() {
		super(size);
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
		ITUtils.RemoveDummyFromTicking(this);
	}

	@Override
	public int[] getCurrentProcessesStep() {
		TileEntityCokeOvenAdvancedMaster master = master();
		return (master != null)? master.getCurrentProcessesStep() : new int[] { 0 };
	}

	@Override
	public int[] getCurrentProcessesMax() {
		TileEntityCokeOvenAdvancedMaster master = master();
		return (master != null)? master.getCurrentProcessesMax() : new int[] { 0 };
	}

	@Override
	public boolean getIsActive() {
		TileEntityCokeOvenAdvancedMaster master = master();
		return (master != null) && master.active;
	}

	@Override
	public IEProperties.PropertyBoolInverted getBoolProperty(Class<? extends IEBlockInterfaces.IUsesBooleanProperty> inf) {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if((pos == 1 || pos == 31) && capability == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			TileEntityCokeOvenAdvancedMaster master = master();
			if(master == null) {
				return null;
			} else if(pos == 1 && facing == master.facing) {
				return (T)master.outputHandler;
			} else if(pos == 31 && facing == EnumFacing.UP) {
				return (T)master.inputHandler;
			}
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if((pos == 1 || pos == 31) && capability == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			TileEntityCokeOvenAdvancedMaster master = master();
			if(master == null) {
				return false;
			} else if(pos == 1 && facing == master.facing) {
				return true;
			} else if(pos == 31 && facing == EnumFacing.UP) {
				return true;
			}
		}
		return super.hasCapability(capability, facing);
	}

	@Override
	public NonNullList<ItemStack> getInventory() {
		TileEntityCokeOvenAdvancedMaster master = master();
		if(master != null && master.formed && formed) return master.inventory;
		return NonNullList.withSize(4, ItemStack.EMPTY);
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack) {
		if(stack.isEmpty()) return false;
		if(slot == 0) return CokeOvenRecipe.findRecipe(stack) != null;
		if(slot == 2) return Utils.isFluidRelatedItemStack(stack);
		return false;
	}

	@Override
	public int getSlotLimit(int slot) {
		return 64;
	}

	@Override
	public void doGraphicalUpdates(int slot) {
	}

	TileEntityCokeOvenAdvancedMaster master;

	@Override
	public TileEntityCokeOvenAdvancedMaster master() {
		if (master != null && !master.tileEntityInvalid) return master;
		BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
		TileEntity te = Utils.getExistingTileEntity(world, masterPos);
		master = te instanceof TileEntityCokeOvenAdvancedMaster?(TileEntityCokeOvenAdvancedMaster)te: null;
		return master;
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side) {
		TileEntityCokeOvenAdvancedMaster master = master();
		if(master != null) {
			if(pos == 7 && (side == null || side == facing)) {
				return new FluidTank[] {master.tank};
			}
		}
		return new FluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resource) {
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
		return ITLib.GUIID_Coke_oven_advanced;
	}

	@Override
	public TileEntity getGuiMaster() {
		return master();
	}

	@Override
	public ItemStack getOriginalBlock() {
		if(pos<0) return ItemStack.EMPTY;
		ItemStack s = ItemStack.EMPTY;
		try {
			s = MultiblockCokeOvenAdvanced.instance.getStructureManual()[pos/9][pos%9/3][pos%3];
		} catch(Exception e) {e.printStackTrace();}
		return s.copy();
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
		if(pos == 0 || pos == 2) {
			if(pos == 2) fw = fw.getOpposite();
			boundingArray = ITUtils.smartBoundingBox(0, .6875f, .6875f, 0, 0, 1, fl, fw);
			return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		if(pos == 3 || pos == 5 || pos == 12 || pos == 14) {
			if(pos == 5 || pos == 14) fw = fw.getOpposite();
			boundingArray = ITUtils.smartBoundingBox(0, 0, .5f, 0, 0, 1, fl, fw);
			return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		if(pos == 6 || pos == 8) {
			if(pos == 8) fw = fw.getOpposite();
			boundingArray = ITUtils.smartBoundingBox(.6875f, 0, .6875f, 0, 0, 1, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, .3125f, .375f, 0, 0, 1, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		if(pos == 9 || pos == 11) {
			if(pos == 11) fw = fw.getOpposite();
			boundingArray = ITUtils.smartBoundingBox(0, .5f, .5f, 0, 0, 1, fl, fw);
			return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		if(pos == 10) {
			boundingArray = ITUtils.smartBoundingBox(0, .5f, 0, 0, 0, 1, fl, fw);
			return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		if(pos == 15 || pos == 17) {
			if(pos == 17) fw = fw.getOpposite();
			boundingArray = ITUtils.smartBoundingBox(.6875f, 0, .5f, 0, 0, 1, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, .3125f, .375f, 0, 0, 1, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		if(pos == 18 || pos == 20) {
			if(pos == 20) fw = fw.getOpposite();
			boundingArray = ITUtils.smartBoundingBox(0, .625f, .625f, 0, 0, 1, fl, fw);
			return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		if(pos == 19) {
			boundingArray = ITUtils.smartBoundingBox(0, .625f, 0, 0, 0, 1, fl, fw);
			return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		if(pos == 21 || pos == 23) {
			if(pos == 23) fw = fw.getOpposite();
			boundingArray = ITUtils.smartBoundingBox(0, 0, .625f, 0, 0, 1, fl, fw);
			return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		if(pos == 24 || pos == 26) {
			if(pos == 26) fw = fw.getOpposite();
			boundingArray = ITUtils.smartBoundingBox(.6875f, 0, .625f, 0, 0, 1, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, .3125f, .375f, 0, 0, .25f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		if(pos == 25) {
			boundingArray = ITUtils.smartBoundingBox(.6875f, 0, 0, 0, 0, 1, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, .3125f, 0, 0, 0, .25f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		return null;
	}

	@Override
	public boolean isOverrideBox(AxisAlignedBB box, EntityPlayer player, RayTraceResult mop, ArrayList<AxisAlignedBB> list) {
		return false;
	}

}