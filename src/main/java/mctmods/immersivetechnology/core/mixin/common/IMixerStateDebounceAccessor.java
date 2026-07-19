package mctmods.immersivetechnology.core.mixin.common;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public interface IMixerStateDebounceAccessor {

    NonNullList<ItemStack> it$getLastComponents();

    int it$getStableTicks();

    void it$setStableTicks(int ticks);
}
