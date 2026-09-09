package mctmods.immersivetechnology.common.gui;

import mctmods.immersivetechnology.common.multiblocks.stone.tileentities.TileEntityAdvancedCokeOvenMaster;

import com.immersiveconvergence.api.crafting.ICCokeOvenRecipe;
import com.immersiveconvergence.api.gui.ICContainerBase;
import com.immersiveconvergence.api.gui.ICSlot;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerAdvancedCokeOven extends ICContainerBase<TileEntityAdvancedCokeOvenMaster> {
    public ContainerAdvancedCokeOven(InventoryPlayer inventoryPlayer, TileEntityAdvancedCokeOvenMaster tile) {
        super(inventoryPlayer, tile);

        slotCount = TileEntityAdvancedCokeOvenMaster.slotCount;
        this.addSlotToContainer(new ICSlot(this, this.inv, 0, 30, 35) {
            @Override public boolean isItemValid(ItemStack itemStack) { return ICCokeOvenRecipe.findRecipe(itemStack) != null; }
        });
        this.addSlotToContainer(new ICSlot.Output(this, this.inv, 1, 85, 35));
        this.addSlotToContainer(new ICSlot.FluidContainer(this, this.inv, 2, 152, 17, 0));
        this.addSlotToContainer(new ICSlot.Output(this, this.inv, 3, 152, 53));

        for (Slot slot : ITContainerHelper.playerInventorySlots(inventoryPlayer, 84)) { addSlotToContainer(slot); }
    }
}
