package mctmods.immersivetechnology.common.gui;

import blusunrize.immersiveengineering.common.gui.ContainerIEBase;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityTrashItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;

public class ContainerTrashItem extends ContainerIEBase<TileEntityTrashItem> {
	public ContainerTrashItem(InventoryPlayer inventoryPlayer, TileEntityTrashItem tile) {
		super(inventoryPlayer, tile);

		this.addSlotToContainer(new Slot(this.inv, 0, 80, 34));
		this.addSlotToContainer(new Slot(this.inv, 1, 80, 34));
		this.addSlotToContainer(new Slot(this.inv, 2, 80, 34));
		this.addSlotToContainer(new Slot(this.inv, 3, 80, 34));
		this.addSlotToContainer(new Slot(this.inv, 4, 80, 34));
		this.addSlotToContainer(new Slot(this.inv, 5, 80, 34));
		this.addSlotToContainer(new Slot(this.inv, 6, 80, 34));
		this.addSlotToContainer(new Slot(this.inv, 7, 80, 34));
		this.addSlotToContainer(new Slot(this.inv, 8, 80, 34));
		slotCount=9;

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

}