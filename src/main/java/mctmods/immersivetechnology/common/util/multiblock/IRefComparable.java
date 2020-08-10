package mctmods.immersivetechnology.common.util.multiblock;

import net.minecraft.item.ItemStack;

public interface IRefComparable {

    boolean isEquals(ItemStack toCompare);

    ItemStack toItemStack();
}
