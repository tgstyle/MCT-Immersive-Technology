package mctmods.immersivetechnology.core.util.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

import java.util.stream.Stream;
import com.immersiveconvergence.api.util.IItemDropProvider;

public interface IInventory extends IItemDropProvider {
    NonNullList<ItemStack> getInventory();

    boolean isStackValid(int var1, ItemStack var2);

    int getSlotLimit(int var1);

    void doGraphicalUpdates();

    default Stream<ItemStack> getDroppedItems() {
        return this.getInventory() != null ? this.getInventory().stream() : Stream.of();
    }
}
