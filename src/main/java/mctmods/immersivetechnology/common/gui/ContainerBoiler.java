package mctmods.immersivetechnology.common.gui;

import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityBoilerMaster;

import blusunrize.immersiveengineering.common.gui.ContainerIEBase;
import blusunrize.immersiveengineering.common.gui.IESlot;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

public class ContainerBoiler extends ContainerIEBase<TileEntityBoilerMaster> {
	public ContainerBoiler(InventoryPlayer inventoryPlayer, TileEntityBoilerMaster tile) {
		super(inventoryPlayer, tile);

		slotCount = TileEntityBoilerMaster.slotCount;
		final TileEntityBoilerMaster tileF = tile;
		this.addSlotToContainer(new IESlot.FluidContainer(this, this.inv, 0, 37, 15, 2) {
			@Override public boolean isItemValid(ItemStack itemStack) { return ITContainerHelper.acceptsMatchingFluid(itemStack, tileF.tanks[0]); }
		});
		this.addSlotToContainer(new IESlot.Output(this, this.inv, 1, 37, 54));
		this.addSlotToContainer(new IESlot.FluidContainer(this, this.inv, 2, 76, 15, 2) {
			@Override public boolean isItemValid(ItemStack itemStack) { return ITContainerHelper.acceptsMatchingFluid(itemStack, tileF.tanks[1]); }
		});
		this.addSlotToContainer(new IESlot.Output(this, this.inv, 3, 76, 54));
		this.addSlotToContainer(new IESlot.FluidContainer(this, this.inv, 4, 149, 15, 0) {
			@Override public boolean isItemValid(ItemStack itemStack) {
				return super.isItemValid(itemStack) || (!itemStack.isEmpty() && itemStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null));
			}
		});
		this.addSlotToContainer(new IESlot.Output(this, this.inv, 5, 149, 54));

		for (Slot slot : ITContainerHelper.playerInventorySlots(inventoryPlayer, 85)) { addSlotToContainer(slot); }
	}
}
