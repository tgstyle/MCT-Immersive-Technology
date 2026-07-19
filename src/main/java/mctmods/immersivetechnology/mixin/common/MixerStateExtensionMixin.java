package mctmods.immersivetechnology.mixin.common;

import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.mixer.MixerLogic;
import mctmods.immersivetechnology.core.mixin.common.IMixerStateDebounceAccessor;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MixerLogic.State.class)
public abstract class MixerStateExtensionMixin implements IMixerStateDebounceAccessor {

    @Unique private final NonNullList<ItemStack> it$lastComponents = NonNullList.withSize(MixerLogic.NUM_SLOTS, ItemStack.EMPTY);

    @Unique private int it$stableTicks = 0;

    @Override public NonNullList<ItemStack> it$getLastComponents() { return it$lastComponents; }

    @Override public int it$getStableTicks() { return it$stableTicks; }

    @Override public void it$setStableTicks(int ticks) { this.it$stableTicks = ticks; }
}
