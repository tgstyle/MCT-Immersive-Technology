package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import mctmods.immersivetechnology.api.ITLib;
import mctmods.immersivetechnology.api.ITUtils;
import mctmods.immersivetechnology.common.Config;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class TileEntityTrashItem extends TileEntityGenericTrash implements IItemHandler, IInventory, IBlockBounds, IGuiTile {

	@Override
	public String[] getOverlayText(EntityPlayer player, RayTraceResult mop, boolean hammer) {
		return new String[]{
				ITUtils.Translate(".osd.general.trashed", false, true) +
						lastAcceptedAmount + ITUtils.Translate(".osd.trash_item.unit", true),
				ITUtils.Translate(".osd.general.inpackets", false, true) +
						lastPerSecond + ITUtils.Translate(".osd.general.packetslastsecond", true)
		};
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return true;
		return super.hasCapability(capability, facing);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return (T)this;
		return super.getCapability(capability, facing);
	}

	@Override
	public int getSlots() {
		return 9;
	}

	@Override
	public int getSizeInventory() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Nonnull
	@Override
	public ItemStack getStackInSlot(int i) {
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack decrStackSize(int i, int i1) {
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeStackFromSlot(int i) {
		return ItemStack.EMPTY;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemStack) {
		if(!itemStack.isEmpty()) insertItem(i, itemStack, false);
	}

	@Override
	public int getInventoryStackLimit() {
		return Integer.MAX_VALUE;
	}

	IItemHandler inputHandler = new IEInventoryHandler(slotCount, this);

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return (T)inputHandler;
		return super.getCapability(capability, facing);
	}

	@Override
	public void closeInventory(EntityPlayer entityPlayer) {

	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		return true;
	}

	@Override
	public int getField(int i) {
		return 0;
	}

	@Override
	public void setField(int i, int i1) {

	}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {

	}

	@Nonnull
	@Override
	public ItemStack insertItem(int i, @Nonnull ItemStack itemStack, boolean simulate) {
		ItemStack returnStack;
		int canFit = Config.ITConfig.Trash.item_max_void_rate - acceptedAmount;
		if(itemStack.getCount() > canFit) {
			returnStack = itemStack.copy();
			returnStack.setCount(itemStack.getCount() - canFit);
		} else returnStack = ItemStack.EMPTY;
		if(!simulate) {
			acceptedAmount += itemStack.getCount() - returnStack.getCount();
			perSecond++;
		}
		return returnStack;
	}

	@Nonnull
	@Override
	public ItemStack extractItem(int i, int i1, boolean b) {
		return ItemStack.EMPTY;
	}

	@Override
	public int getSlotLimit(int slot) {
		return Integer.MAX_VALUE;
	}

	@Override
	public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
		return true;
	}

	@Override
	public boolean canOpenGui() {
		return true;
	}

	@Override
	public int getGuiID() {
		return ITLib.GUIID_Trash_Item;
	}

	@Override
	public TileEntity getGuiMaster() {
		return this;
	}

	@Override
	public float[] getBlockBounds()	{
		return new float[]{facing.getAxis() == Axis.X ? 0 : .125f, 0, facing.getAxis() == Axis.Z ? .125f : .125f, facing.getAxis() == Axis.X ? 1 : .875f, 1, facing.getAxis() == Axis.Z ? .875f : .875f};
	}

	@Override
	public String getName() {
		return ITUtils.Translate(".metal_trash.trash_item.name");
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

}