package mctmods.immersivetechnology.common.gui;

import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityTrashItem;

import javax.annotation.Nonnull;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerTrashItem extends Container {
	TileEntityTrashItem tile;

	public ContainerTrashItem(InventoryPlayer inventoryPlayer, TileEntityTrashItem tile) {
		this.tile = tile;
		this.addSlotToContainer(new Slot(tile.inv, 0, 80, 34));
		for (Slot slot : ITContainerHelper.playerInventorySlots(inventoryPlayer, 85)) { addSlotToContainer(slot); }
	}

	@Override public boolean canInteractWith(@Nonnull EntityPlayer player) {
		return tile != null && tile.getWorld().getTileEntity(tile.getPos()) == tile && player.getDistanceSq(tile.getPos().getX() + .5, tile.getPos().getY() + .5, tile.getPos().getZ() + .5) <= 64;
	}

	@Override @Nonnull public ItemStack transferStackInSlot(@Nonnull EntityPlayer playerIn, int index) {
		Slot slot = getSlot(index);
		if (slot.getHasStack()) {
			tile.insertItem(0, slot.getStack(), false);
			slot.putStack(ItemStack.EMPTY);
		}
		return ItemStack.EMPTY;
	}
}
