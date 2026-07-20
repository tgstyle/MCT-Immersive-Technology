package mctmods.immersivetechnology.common.gui;

import mctmods.immersivetechnology.api.crafting.MeltingCrucibleRecipe;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityMeltingCrucibleMaster;

import blusunrize.immersiveengineering.common.gui.ContainerIEBase;
import blusunrize.immersiveengineering.common.gui.IESlot;
import java.util.Objects;
import javax.annotation.Nonnull;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

public class ContainerMeltingCrucible extends ContainerIEBase<TileEntityMeltingCrucibleMaster> {
    public ContainerMeltingCrucible(InventoryPlayer inventoryPlayer, TileEntityMeltingCrucibleMaster tile) {
        super(inventoryPlayer, tile);

        slotCount = TileEntityMeltingCrucibleMaster.slotCount;

        this.addSlotToContainer(new Slot(Objects.requireNonNull(this.inv), 0, 43, 36) {
            @Override public boolean isItemValid(@Nonnull ItemStack itemStack) {
                return MeltingCrucibleRecipe.findRecipe(itemStack) != null;
            }
        });

        this.addSlotToContainer(new IESlot.FluidContainer(this, this.inv, 1, 148, 17, 2) {
            @Override public boolean isItemValid(ItemStack itemStack) {
                return itemStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null) || FluidUtil.getFluidHandler(itemStack) != null;
            }
        });
        this.addSlotToContainer(new IESlot.Output(this, this.inv, 2, 148, 53));

        for (Slot slot : ITContainerHelper.playerInventorySlots(inventoryPlayer, 84)) { addSlotToContainer(slot); }
    }
}
