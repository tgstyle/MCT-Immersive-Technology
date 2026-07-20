package mctmods.immersivetechnology.common.gui;

import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityDistillerMaster;

import blusunrize.immersiveengineering.common.gui.ContainerIEBase;
import blusunrize.immersiveengineering.common.gui.IESlot;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

public class ContainerDistiller extends ContainerIEBase<TileEntityDistillerMaster> {
	public ContainerDistiller(InventoryPlayer inventoryPlayer, TileEntityDistillerMaster tile) {
		super(inventoryPlayer, tile);

		slotCount = TileEntityDistillerMaster.slotCount;
		final TileEntityDistillerMaster tileF = tile;
		this.addSlotToContainer(new IESlot.FluidContainer(this, this.inv, 0, 26, 17, 2) {
			@Override public boolean isItemValid(ItemStack itemStack) { return ITContainerHelper.acceptsMatchingFluid(itemStack, tileF.tanks[0]); }
		});
		this.addSlotToContainer(new IESlot.Output(this, this.inv, 1, 26, 53));
		this.addSlotToContainer(new IESlot.FluidContainer(this, this.inv, 2, 134, 17, 0) {
			@Override public boolean isItemValid(ItemStack itemStack) {
				return super.isItemValid(itemStack) || itemStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
			}
		});
		this.addSlotToContainer(new IESlot.Output(this, this.inv, 3, 134, 53));
		this.addSlotToContainer(new IESlot.Output(this, this.inv, 4, 80, 35));

		for (Slot slot : ITContainerHelper.playerInventorySlots(inventoryPlayer, 85)) { addSlotToContainer(slot); }
	}
}
