package mctmods.immersivetechnology.common.gui;

import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityBoilerSolidMaster;

import blusunrize.immersiveengineering.common.gui.ContainerIEBase;
import blusunrize.immersiveengineering.common.gui.IESlot;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerBoilerSolid extends ContainerIEBase<TileEntityBoilerSolidMaster> {
	public ContainerBoilerSolid(InventoryPlayer inventoryPlayer, TileEntityBoilerSolidMaster tile) {
		super(inventoryPlayer, tile);

		slotCount = TileEntityBoilerSolidMaster.slotCount;
		final TileEntityBoilerSolidMaster tileF = tile;
		this.addSlotToContainer(new IESlot(this, this.inv, 0, 44, 34) {
			@Override public boolean isItemValid(ItemStack itemStack) { return tileF.isStackValid(0, itemStack); }
		});

		for (Slot slot : ITContainerHelper.playerInventorySlots(inventoryPlayer, 85)) { addSlotToContainer(slot); }
	}
}
