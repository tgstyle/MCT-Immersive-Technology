package mctmods.immersivetechnology.mixin.common.helper;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public interface IMixerStateDebounceAccessor {

    NonNullList<ItemStack> it$getLastComponents();

    int it$getStableTicks();

    void it$setStableTicks(int ticks);
}
