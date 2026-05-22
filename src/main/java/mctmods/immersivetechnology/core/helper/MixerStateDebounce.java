package mctmods.immersivetechnology.core.helper;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public interface MixerStateDebounce {

    NonNullList<ItemStack> it$getLastComponents();

    int it$getStableTicks();

    void it$setStableTicks(int ticks);
}
