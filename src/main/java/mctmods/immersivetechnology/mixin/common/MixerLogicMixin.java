package mctmods.immersivetechnology.mixin.common;

import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.mixer.MixerLogic;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.mixer.MixerLogic.State;
import mctmods.immersivetechnology.core.mixin.common.IMixerStateDebounceAccessor;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;

@Mixin(MixerLogic.class)
public abstract class MixerLogicMixin {

    @Unique private static final Object it$NOP;

    static {
        try {
            Class<?> resClass = Class.forName("blusunrize.immersiveengineering.common.blocks.multiblocks.logic.mixer.MixerLogic$RecipeEnqueueState");
            Field nopField = resClass.getDeclaredField("NOP");
            nopField.setAccessible(true);
            it$NOP = nopField.get(null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access MixerLogic.RecipeEnqueueState.NOP", e);
        }
    }

    @Inject(
            method = "enqueueNewRecipes(Lblusunrize/immersiveengineering/common/blocks/multiblocks/logic/mixer/MixerLogic$State;Lnet/minecraft/world/level/Level;)Lblusunrize/immersiveengineering/common/blocks/multiblocks/logic/mixer/MixerLogic$RecipeEnqueueState;",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void it$smartDebounce(State state, Level rawLevel, CallbackInfoReturnable<Object> cir) {
        IMixerStateDebounceAccessor ext = (IMixerStateDebounceAccessor) state;

        NonNullList<ItemStack> last = ext.it$getLastComponents();

        boolean playerChange = false;
        for (int i = 0; i < MixerLogic.NUM_SLOTS; ++i) {
            ItemStack current = state.inventory.getStackInSlot(i);
            ItemStack previous = last.get(i);

            if (!ItemStack.isSameItemSameComponents(current, previous)) {
                playerChange = true;
                break;
            }

            if (current.getCount() > previous.getCount()) {
                playerChange = true;
                break;
            }
        }

        if (playerChange) {
            ext.it$setStableTicks(0);
            for (int i = 0; i < MixerLogic.NUM_SLOTS; ++i) {
                ItemStack stack = state.inventory.getStackInSlot(i);
                last.set(i, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
            }
        } else {
            ext.it$setStableTicks(ext.it$getStableTicks() + 1);

            for (int i = 0; i < MixerLogic.NUM_SLOTS; ++i) {
                ItemStack stack = state.inventory.getStackInSlot(i);
                last.set(i, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
            }
        }

        if (state.processor.getQueue().isEmpty() && ext.it$getStableTicks() < 60) {
            cir.setReturnValue(it$NOP);
        }
    }
}
