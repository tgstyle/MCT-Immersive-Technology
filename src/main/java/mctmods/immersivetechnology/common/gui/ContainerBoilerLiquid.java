package mctmods.immersivetechnology.common.gui;

import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityBoilerLiquidMaster;

import blusunrize.immersiveengineering.common.gui.ContainerIEBase;
import blusunrize.immersiveengineering.common.gui.IESlot;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerBoilerLiquid extends ContainerIEBase<TileEntityBoilerLiquidMaster> {
	public ContainerBoilerLiquid(InventoryPlayer inventoryPlayer, TileEntityBoilerLiquidMaster tile) {
		super(inventoryPlayer, tile);

		slotCount = TileEntityBoilerLiquidMaster.slotCount;
		final TileEntityBoilerLiquidMaster tileF = tile;
		this.addSlotToContainer(new IESlot.FluidContainer(this, this.inv, 0, 26, 17, 2) {
			@Override public boolean isItemValid(ItemStack itemStack) { return ITContainerHelper.acceptsMatchingFluid(itemStack, tileF.tanks[0]); }
		});
		this.addSlotToContainer(new IESlot.Output(this, this.inv, 1, 26, 53));

		for (Slot slot : ITContainerHelper.playerInventorySlots(inventoryPlayer, 85)) { addSlotToContainer(slot); }
	}
}
