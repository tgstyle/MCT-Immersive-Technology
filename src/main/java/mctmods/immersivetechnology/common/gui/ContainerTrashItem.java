package mctmods.immersivetechnology.common.gui;

import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityTrashItem;
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
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 9; j++) {
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 85 + i * 18));
			}
		}
		for(int i = 0; i < 9; i++) addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 143));
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return tile != null && tile.getWorld().getTileEntity(tile.getPos()) == tile && player.getDistanceSq(tile.getPos().getX() + .5, tile.getPos().getY() + .5, tile.getPos().getZ() + .5) <= 64;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
		Slot slot = getSlot(index);
		if(slot != null && slot.getHasStack()) {
			tile.insertItem(0, slot.getStack(), false);
			slot.putStack(ItemStack.EMPTY);
		}
		return ItemStack.EMPTY;
	}

}