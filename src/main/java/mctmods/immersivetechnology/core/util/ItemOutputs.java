package mctmods.immersivetechnology.core.util;

import com.immersiveconvergence.api.util.ICItemUtils;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import java.util.function.Supplier;

public class ItemOutputs {
    public static boolean overflows(ItemStack target, ItemStack added) { return !target.isEmpty() && (!ItemStack.isSameItemSameComponents(target, added) || target.getCount() + added.getCount() > target.getMaxStackSize()); }

    public static void place(IItemHandlerModifiable inv, int slot, ItemStack target, ItemStack added) {
        if (target.isEmpty()) { inv.setStackInSlot(slot, added); }
        else {
            target.grow(added.getCount());
            inv.setStackInSlot(slot, target);
        }
    }

    public static void eject(IItemHandlerModifiable inv, Supplier<IItemHandler> target, int... slots) {
        for (int slot : slots) {
            ItemStack stack = inv.getStackInSlot(slot);
            if (!stack.isEmpty()) { inv.setStackInSlot(slot, ICItemUtils.insertStackIntoInventory(target, stack, false)); }
        }
    }

    public static boolean merge(IItemHandlerModifiable inv, int slot, ItemStack added) {
        ItemStack target = inv.getStackInSlot(slot);
        if (overflows(target, added)) { return false; }
        place(inv, slot, target, added);
        return true;
    }
}
