package mctmods.immersivetechnology.common.util.multiblock;

import net.minecraft.item.ItemStack;

public class ItemStackRef implements IRefComparable {

    public final ItemStack itemStack;

    public ItemStackRef(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    public boolean isEquals(ItemStack toCompare) {
        return toCompare.isItemEqual(itemStack);
    }

    @Override
    public ItemStack toItemStack() {
        return itemStack;
    }
}
