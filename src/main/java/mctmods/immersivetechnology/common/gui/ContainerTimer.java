package mctmods.immersivetechnology.common.gui;

import mctmods.immersivetechnology.common.blocks.connectors.tileentities.TileEntityTimer;

import javax.annotation.Nonnull;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

public class ContainerTimer extends Container {
	TileEntityTimer tile;

	public ContainerTimer(InventoryPlayer inventoryPlayer, TileEntityTimer tile) {
		this.tile=tile;
		for (Slot slot : ITContainerHelper.playerInventorySlots(inventoryPlayer, 85)) { addSlotToContainer(slot); }
	}

	@Override public boolean canInteractWith(@Nonnull EntityPlayer player) {
		return tile != null && tile.getWorld().getTileEntity(tile.getPos()) == tile && player.getDistanceSq(tile.getPos().getX() + .5, tile.getPos().getY() + .5, tile.getPos().getZ() + .5) <= 64;
	}
}
