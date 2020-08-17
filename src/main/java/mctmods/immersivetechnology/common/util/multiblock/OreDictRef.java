package mctmods.immersivetechnology.common.util.multiblock;

import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class OreDictRef implements IRefComparable {

    public final String name;

    public OreDictRef(String name) {
        this.name = name;
    }

    @Override
    public boolean isEquals(ItemStack toCompare) {
        return Utils.compareToOreName(toCompare, name);
    }

    @Override
    public ItemStack toItemStack() {
        return OreDictionary.getOres(name).get(0);
    }
}
