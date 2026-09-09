package mctmods.immersivetechnology.common.gui;

import com.immersiveconvergence.api.gui.ICContainerBase;
import com.immersiveconvergence.api.gui.ICSlot;

import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityBoilerTankMaster;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerBoilerTank extends ICContainerBase<TileEntityBoilerTankMaster> {
	public ContainerBoilerTank(InventoryPlayer inventoryPlayer, TileEntityBoilerTankMaster tile) {
		super(inventoryPlayer, tile);

		slotCount = TileEntityBoilerTankMaster.slotCount;
		final TileEntityBoilerTankMaster tileF = tile;
		this.addSlotToContainer(new ICSlot.FluidContainer(this, this.inv, 0, 43, 15, 2) {
			@Override public boolean isItemValid(ItemStack itemStack) { return ITContainerHelper.acceptsMatchingFluid(itemStack, tileF.tanks[0]); }
		});
		this.addSlotToContainer(new ICSlot.Output(this, this.inv, 1, 43, 54));
		this.addSlotToContainer(new ICSlot.FluidContainer(this, this.inv, 2, 116, 15, 1));
		this.addSlotToContainer(new ICSlot.Output(this, this.inv, 3, 116, 54));

		for (Slot slot : ITContainerHelper.playerInventorySlots(inventoryPlayer, 85)) { addSlotToContainer(slot); }
	}
}
