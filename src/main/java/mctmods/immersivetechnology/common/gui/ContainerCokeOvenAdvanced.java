package mctmods.immersivetechnology.common.gui;

import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.common.gui.ContainerIEBase;
import blusunrize.immersiveengineering.common.gui.IESlot;
import mctmods.immersivetechnology.common.blocks.stone.tileentities.TileEntityCokeOvenAdvancedMaster;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerCokeOvenAdvanced extends ContainerIEBase<TileEntityCokeOvenAdvancedMaster> {
	public ContainerCokeOvenAdvanced(InventoryPlayer inventoryPlayer, TileEntityCokeOvenAdvancedMaster tile) {
		super(inventoryPlayer, tile);

		slotCount = TileEntityCokeOvenAdvancedMaster.slotCount;
		this.addSlotToContainer(new IESlot(this, this.inv, 0, 30, 35) {
			@Override
			public boolean isItemValid(ItemStack itemStack) {
				return CokeOvenRecipe.findRecipe(itemStack) != null;
			}
		});
		this.addSlotToContainer(new IESlot.Output(this, this.inv, 1, 85, 35));
		this.addSlotToContainer(new IESlot.FluidContainer(this, this.inv, 2, 152, 17, 0));
		this.addSlotToContainer(new IESlot.Output(this, this.inv, 3, 152, 53));

		for(int i = 0 ; i < 3 ; i++) {
			for(int j = 0 ; j < 9 ; j++) {
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}
		for(int i = 0 ; i < 9 ; i++) addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 142));
	}

}