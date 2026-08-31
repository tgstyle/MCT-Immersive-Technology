package mctmods.immersivetechnology.common.gui;

import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityBoilerTankMaster;

import blusunrize.immersiveengineering.common.gui.ContainerIEBase;
import blusunrize.immersiveengineering.common.gui.IESlot;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerBoilerTank extends ContainerIEBase<TileEntityBoilerTankMaster> {
	public ContainerBoilerTank(InventoryPlayer inventoryPlayer, TileEntityBoilerTankMaster tile) {
		super(inventoryPlayer, tile);

		slotCount = TileEntityBoilerTankMaster.slotCount;
		final TileEntityBoilerTankMaster tileF = tile;
		this.addSlotToContainer(new IESlot.FluidContainer(this, this.inv, 0, 43, 15, 2) {
			@Override public boolean isItemValid(ItemStack itemStack) { return ITContainerHelper.acceptsMatchingFluid(itemStack, tileF.tanks[0]); }
		});
		this.addSlotToContainer(new IESlot.Output(this, this.inv, 1, 43, 54));
		this.addSlotToContainer(new IESlot.FluidContainer(this, this.inv, 2, 116, 15, 1));
		this.addSlotToContainer(new IESlot.Output(this, this.inv, 3, 116, 54));

		for (Slot slot : ITContainerHelper.playerInventorySlots(inventoryPlayer, 85)) { addSlotToContainer(slot); }
	}
}
