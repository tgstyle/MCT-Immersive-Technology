package mctmods.immersivetechnology.common.tileentities;

import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.api.ITLib;
import mctmods.immersivetechnology.client.gui.GuiStackLimiter;
import mctmods.immersivetechnology.common.util.TranslationKey;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class TileEntityStackLimiter extends TileEntityCommonValve implements IItemHandler {

	final public static DummyInventory dummyInventory = new DummyInventory();

	public TileEntityStackLimiter() {
		super( TranslationKey.OVERLAY_OSD_STACK_LIMITER_NORMAL_FIRST_LINE,
				TranslationKey.OVERLAY_OSD_STACK_LIMITER_SNEAKING_FIRST_LINE,
				TranslationKey.OVERLAY_OSD_STACK_LIMITER_SNEAKING_SECOND_LINE,
				ITLib.GUIID_Stack_Limiter);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void showGui() {
		Minecraft.getMinecraft().displayGuiScreen(new GuiStackLimiter(this));
	}

	public static class DummyInventory implements IItemHandler {

		@Override
		public int getSlots() {
			return 0;
		}

		@Nonnull
		@Override
		public ItemStack getStackInSlot(int i) {
			return ItemStack.EMPTY;
		}

		@Nonnull
		@Override
		public ItemStack insertItem(int i, @Nonnull ItemStack itemStack, boolean b) {
			return itemStack;
		}

		@Nonnull
		@Override
		public ItemStack extractItem(int i, int i1, boolean b) {
			return ItemStack.EMPTY;
		}

		@Override
		public int getSlotLimit(int i) {
			return 0;
		}
	}

	@Override
	public boolean hasCapability(final Capability<?> capability, final EnumFacing facing) {
		return (facing != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing.getAxis() == this.facing.getAxis());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(final Capability<T> capability, final EnumFacing facing) {
		if(facing == null) return null;
		if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			if(facing == this.facing) return (T)this;
			else if(facing == this.facing.getOpposite()) return (T)dummyInventory;
		}
		return super.getCapability(capability, facing);
	}

	boolean busy = false;

	public IItemHandler getDestination() {
		TileEntity dst = Utils.getExistingTileEntity(world, pos.offset(facing, -1));
		if(dst != null && dst.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing)) {
			return dst.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing);
		}
		return null;
	}

	@Override
	public int getSlots() {
		IItemHandler dest = getDestination();
		if(dest == null) return 0;
		return dest.getSlots();
	}

	@Nonnull
	@Override
	public ItemStack getStackInSlot(int i) {
		IItemHandler dest = getDestination();
		if(dest == null) return ItemStack.EMPTY;
		return dest.getStackInSlot(i);
	}

	@Nonnull
	@Override
	public ItemStack insertItem(int i, @Nonnull ItemStack itemStack, boolean simulate) {
		if(busy) return itemStack;
		IItemHandler destination = getDestination();
		if(destination == null) return itemStack;
		int canAccept = itemStack.getCount();
		canAccept = timeLimit != -1? Math.min(Math.max(timeLimit - longToInt(acceptedAmount), 0), canAccept) : canAccept;
		canAccept = keepSize != -1? Math.min(Math.max(keepSize - getInventoryFill(destination, itemStack), 0), canAccept) : canAccept;
		canAccept = packetLimit != -1? Math.min(canAccept, packetLimit) : canAccept;
		if(redstoneMode > 0) canAccept *= (double) (redstoneMode == 1? 15 - getRSPower() : getRSPower())/15;
		if(canAccept == 0) return itemStack;
		ItemStack toReturn;
		busy = true;
		toReturn = destination.insertItem(i, new ItemStack(itemStack.getItem(), canAccept), simulate);
		busy = false;
		if(!simulate) {
			acceptedAmount += (toReturn == ItemStack.EMPTY)? canAccept : canAccept - toReturn.getCount();
			packets++;
		}
		return new ItemStack(itemStack.getItem(), (toReturn == ItemStack.EMPTY)? itemStack.getCount() - canAccept :
				toReturn.getCount() + itemStack.getCount() - canAccept);
	}

	public int getInventoryFill(IItemHandler dest, ItemStack stack) {
		int count = 0;
		for(int index = 0; index < dest.getSlots(); index++) {
			ItemStack stackInSlot = dest.getStackInSlot(index);
			if(!stackInSlot.isItemEqual(stack)) continue;
			count += stackInSlot.getCount();
		}
		return count;
	}

	@Nonnull
	@Override
	public ItemStack extractItem(int i, int i1, boolean b) {
		return ItemStack.EMPTY;
	}

	@Override
	public int getSlotLimit(int i) {
		IItemHandler dest = getDestination();
		if(dest == null) return 0;
		return dest.getSlotLimit(i);
	}

	@Override
	public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
		IItemHandler dest = getDestination();
		if(dest == null) return false;
		return dest.isItemValid(slot, stack);
	}

}