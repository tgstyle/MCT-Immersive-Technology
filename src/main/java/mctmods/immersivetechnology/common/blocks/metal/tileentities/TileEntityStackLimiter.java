package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.common.util.Utils;

import mctmods.immersivetechnology.api.ITGUI;
import mctmods.immersivetechnology.client.gui.GuiStackLimiter;
import mctmods.immersivetechnology.common.shared.tileentities.TileEntityCommonValve;
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
import javax.annotation.Nullable;

public class TileEntityStackLimiter extends TileEntityCommonValve implements IItemHandler {

	public static final DummyInventory dummyInventory = new DummyInventory();

	public TileEntityStackLimiter() {
		super(TranslationKey.OVERLAY_OSD_STACK_LIMITER_NORMAL_FIRST_LINE,
				TranslationKey.OVERLAY_OSD_STACK_LIMITER_SNEAKING_FIRST_LINE,
				TranslationKey.OVERLAY_OSD_STACK_LIMITER_SNEAKING_SECOND_LINE,
				ITGUI.GUIID_Stack_Limiter);
	}

	@SideOnly(Side.CLIENT)
	@Override public void showGui() { Minecraft.getMinecraft().displayGuiScreen(new GuiStackLimiter(this)); }

	public static class DummyInventory implements IItemHandler {

		@Override public int getSlots() { return 0; }

		@Override @Nonnull public ItemStack getStackInSlot(int slot) { return ItemStack.EMPTY; }

		@Override @Nonnull public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) { return stack; }

		@Override @Nonnull public ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }

		@Override public int getSlotLimit(int slot) { return 0; }
	}

	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
				&& (facing == null || facing.getAxis() == this.facing.getAxis());
	}

	@SuppressWarnings("unchecked")
	@Override @Nonnull public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			if (facing == this.facing) { return (T)this; }
			else if (facing == this.facing.getOpposite() || facing == null) { return (T)dummyInventory; }
		}
		return super.getCapability(capability, facing);
	}

	boolean busy = false;

	public IItemHandler getDestination() {
		TileEntity dst = Utils.getExistingTileEntity(world, pos.offset(facing, -1));
		if (dst != null && dst.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing)) { return dst.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing); }
		return null;
	}

	@Override public int getSlots() {
		IItemHandler dest = getDestination();
		if (dest == null) { return 0; }
		return dest.getSlots();
	}

	@Override @Nonnull public ItemStack getStackInSlot(int slot) {
		IItemHandler dest = getDestination();
		if (dest == null) { return ItemStack.EMPTY; }
		return dest.getStackInSlot(slot);
	}

	@Override @Nonnull
	public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
		if (busy) { return stack; }
		IItemHandler destination = getDestination();
		if (destination == null) { return stack; }
		int canAccept = stack.getCount();
		canAccept = timeLimit != -1 ? Math.min(Math.max(timeLimit - longToInt(acceptedAmount), 0), canAccept) : canAccept;
		canAccept = keepSize != -1 ? Math.min(Math.max(keepSize - getInventoryFill(destination, stack), 0), canAccept) : canAccept;
		canAccept = packetLimit != -1 ? Math.min(canAccept, packetLimit) : canAccept;
		if (redstoneMode > 0) { canAccept *= (int)((double)(redstoneMode == 1 ? 15 - getRSPower() : getRSPower()) / 15); }
		if (canAccept == 0) { return stack; }
		ItemStack toInsert = stack.copy();
		toInsert.setCount(canAccept);
		busy = true;
		ItemStack remainder = destination.insertItem(slot, toInsert, simulate);
		busy = false;
		if (!simulate) {
			acceptedAmount += remainder.isEmpty() ? canAccept : canAccept - remainder.getCount();
			packets++;
		}
		if (remainder.isEmpty()) { stack.shrink(canAccept); }
		else { remainder.grow(stack.getCount() - canAccept); }
		return remainder.isEmpty() ? ItemStack.EMPTY : remainder;
	}

	public int getInventoryFill(IItemHandler dest, ItemStack stack) {
		int count = 0;
		for (int i = 0; i < dest.getSlots(); i++) {
			ItemStack inSlot = dest.getStackInSlot(i);
			if (inSlot.isItemEqual(stack)) { count += inSlot.getCount(); }
		}
		return count;
	}

	@Override @Nonnull public ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }

	@Override public int getSlotLimit(int slot) {
		IItemHandler dest = getDestination();
		if (dest == null) { return 0; }
		return dest.getSlotLimit(slot);
	}

	@Override public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
		IItemHandler dest = getDestination();
		if (dest == null) { return false; }
		return dest.isItemValid(slot, stack);
	}
}
