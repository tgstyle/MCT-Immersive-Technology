package mctmods.immersivetechnology.common.gui;

import mctmods.immersivetechnology.common.multiblocks.stone.tileentities.TileEntityAdvancedCokeOvenMaster;

import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.common.gui.ContainerIEBase;
import blusunrize.immersiveengineering.common.gui.IESlot;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerAdvancedCokeOven extends ContainerIEBase<TileEntityAdvancedCokeOvenMaster> {
    public ContainerAdvancedCokeOven(InventoryPlayer inventoryPlayer, TileEntityAdvancedCokeOvenMaster tile) {
        super(inventoryPlayer, tile);

        slotCount = TileEntityAdvancedCokeOvenMaster.slotCount;
        this.addSlotToContainer(new IESlot(this, this.inv, 0, 30, 35) {
            @Override public boolean isItemValid(ItemStack itemStack) { return CokeOvenRecipe.findRecipe(itemStack) != null; }
        });
        this.addSlotToContainer(new IESlot.Output(this, this.inv, 1, 85, 35));
        this.addSlotToContainer(new IESlot.FluidContainer(this, this.inv, 2, 152, 17, 0));
        this.addSlotToContainer(new IESlot.Output(this, this.inv, 3, 152, 53));

        for (Slot slot : ITContainerHelper.playerInventorySlots(inventoryPlayer, 84)) { addSlotToContainer(slot); }
    }
}
