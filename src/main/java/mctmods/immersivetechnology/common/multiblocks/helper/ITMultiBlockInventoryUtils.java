package mctmods.immersivetechnology.common.multiblocks.helper;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.function.Consumer;

public class ITMultiBlockInventoryUtils {
    public static void dropItems(IItemHandlerModifiable inv, Consumer<ItemStack> drop) {
        for (int slot = 0; slot < inv.getSlots(); slot++) {
            final ItemStack stack = inv.getStackInSlot(slot);
            if (!stack.isEmpty()) { drop.accept(stack.copy()); }
        }
    }
}
