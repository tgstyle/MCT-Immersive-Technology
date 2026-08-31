package mctmods.immersivetechnology.common.gui;

import mctmods.immersivetechnology.common.blocks.wooden.tileentities.TileEntityCrate;

import javax.annotation.Nonnull;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerCrate extends Container {
	TileEntityCrate tile;

	public ContainerCrate(InventoryPlayer inventoryPlayer, TileEntityCrate tile) {
		this.tile = tile;
		this.addSlotToContainer(new SlotItemHandler(tile, 0, 80, 34));
		for (Slot slot : ITContainerHelper.playerInventorySlots(inventoryPlayer, 85)) { addSlotToContainer(slot); }
	}

	@Override public boolean canInteractWith(@Nonnull EntityPlayer player) {
		return tile != null && tile.getWorld().getTileEntity(tile.getPos()) == tile && player.getDistanceSq(tile.getPos().getX() + .5, tile.getPos().getY() + .5, tile.getPos().getZ() + .5) <= 64;
	}

	@Override @Nonnull public ItemStack slotClick(int slotId, int dragType, @Nonnull ClickType clickType, @Nonnull EntityPlayer player) {
		if (slotId != 0) { return super.slotClick(slotId, dragType, clickType, player); }
		InventoryPlayer inventory = player.inventory;
		if (clickType == ClickType.PICKUP) {
			ItemStack carried = inventory.getItemStack();
			if (!carried.isEmpty()) {
				tile.setItemStack(carried.copy());
				inventory.setItemStack(ItemStack.EMPTY);
				return ItemStack.EMPTY;
			}
			ItemStack template = tile.getTemplate();
			if (template.isEmpty()) { return ItemStack.EMPTY; }
			ItemStack extracted = tile.extractItem(0, dragType == 0 ? template.getMaxStackSize() : 1, false);
			inventory.setItemStack(extracted);
			return extracted;
		}
		if (clickType == ClickType.QUICK_MOVE) {
			ItemStack template = tile.getTemplate();
			if (template.isEmpty()) { return ItemStack.EMPTY; }
			ItemStack extracted = tile.extractItem(0, template.getMaxStackSize(), false);
			if (!inventory.addItemStackToInventory(extracted)) { player.dropItem(extracted, false); }
		}
		return ItemStack.EMPTY;
	}

	@Override @Nonnull public ItemStack transferStackInSlot(@Nonnull EntityPlayer player, int index) {
		if (index == 0) { return ItemStack.EMPTY; }
		Slot slot = getSlot(index);
		if (slot.getHasStack()) {
			tile.setItemStack(slot.getStack().copy());
			slot.putStack(ItemStack.EMPTY);
		}
		return ItemStack.EMPTY;
	}
}
