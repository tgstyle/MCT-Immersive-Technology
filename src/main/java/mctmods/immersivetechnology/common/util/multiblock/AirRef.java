package mctmods.immersivetechnology.common.util.multiblock;

import net.minecraft.item.ItemStack;

public class AirRef implements IRefComparable {

    public static AirRef instance = new AirRef();

    @Override
    public boolean isEquals(ItemStack toCompare) {
        return toCompare.isEmpty();
    }

    @Override
    public ItemStack toItemStack() {
        return ItemStack.EMPTY;
    }
}
