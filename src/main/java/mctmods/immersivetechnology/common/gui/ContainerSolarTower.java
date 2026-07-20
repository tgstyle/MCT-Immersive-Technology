package mctmods.immersivetechnology.common.gui;

import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntitySolarTowerMaster;

import blusunrize.immersiveengineering.common.gui.ContainerIEBase;
import blusunrize.immersiveengineering.common.gui.IESlot;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

public class ContainerSolarTower extends ContainerIEBase<TileEntitySolarTowerMaster> {
	public ContainerSolarTower(InventoryPlayer inventoryPlayer, TileEntitySolarTowerMaster tile) {
		super(inventoryPlayer, tile);

		slotCount = TileEntitySolarTowerMaster.slotCount;
		final TileEntitySolarTowerMaster tileF = tile;
		this.addSlotToContainer(new IESlot.FluidContainer(this, this.inv, 0, 80, 17, 2) {
			@Override public boolean isItemValid(ItemStack itemStack) { return ITContainerHelper.acceptsMatchingFluid(itemStack, tileF.tanks[0]); }
		});
		this.addSlotToContainer(new IESlot.Output(this, this.inv, 1, 80, 53));
		this.addSlotToContainer(new IESlot.FluidContainer(this, this.inv, 2, 148, 17, 0) {
			@Override public boolean isItemValid(ItemStack itemStack) {
				return super.isItemValid(itemStack) || itemStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
			}
		});
		this.addSlotToContainer(new IESlot.Output(this, this.inv, 3, 148, 53));

		for (Slot slot : ITContainerHelper.playerInventorySlots(inventoryPlayer, 85)) { addSlotToContainer(slot); }
	}
}
