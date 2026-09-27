package mctmods.immersivetechnology.core.util;

import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import com.immersiveconvergence.api.util.ICItemUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

public class ItemOutputs {
    public static boolean overflows(ItemStack target, ItemStack added) { return !target.isEmpty() && (!ItemHandlerHelper.canItemStacksStack(target, added) || target.getCount() + added.getCount() > target.getMaxStackSize()); }

    public static void place(IItemHandlerModifiable inv, int slot, ItemStack target, ItemStack added) {
        if (target.isEmpty()) { inv.setStackInSlot(slot, added); }
        else {
            target.grow(added.getCount());
            inv.setStackInSlot(slot, target);
        }
    }

    public static void eject(IItemHandlerModifiable inv, CapabilityReference<IItemHandler> target, int... slots) {
        for (int slot : slots) {
            ItemStack stack = inv.getStackInSlot(slot);
            if (!stack.isEmpty()) { inv.setStackInSlot(slot, ICItemUtils.insertStackIntoInventory(target, stack, false)); }
        }
    }

    public static void merge(IItemHandlerModifiable inv, int slot, ItemStack added) {
        ItemStack target = inv.getStackInSlot(slot);
        if (!overflows(target, added)) { place(inv, slot, target, added); }
    }
}
