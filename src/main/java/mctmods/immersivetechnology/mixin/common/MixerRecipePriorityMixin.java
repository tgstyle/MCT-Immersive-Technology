package mctmods.immersivetechnology.mixin.common;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(MixerRecipe.class)
public abstract class MixerRecipePriorityMixin {

    private static final Logger LOGGER = LogManager.getLogger("immersivetechnology/MixerPriority");

    @Inject(
            method = "findRecipe(Lnet/minecraft/world/level/Level;Lnet/minecraftforge/fluids/FluidStack;Lnet/minecraft/core/NonNullList;)Lblusunrize/immersiveengineering/api/crafting/MixerRecipe;",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private static void it$prioritizeHigherIngredients(Level level, FluidStack fluid, NonNullList<ItemStack> components, CallbackInfoReturnable<MixerRecipe> cir) {
        List<MixerRecipe> allMatching = MixerRecipe.RECIPES.getRecipes(level).stream()
                .filter(r -> r.matches(fluid, components))
                .toList();

        if (allMatching.isEmpty()) {
            cir.setReturnValue(null);
            return;
        }

        LOGGER.info("[MixerPriority] Found {} matching recipes for fluid {}", allMatching.size(), fluid.getFluid());

        MixerRecipe best = allMatching.stream()
                .max((r1, r2) -> {
                    int sum1 = 0;
                    for (IngredientWithSize ingr : r1.itemInputs) {
                        if (ingr != null) sum1 += ingr.getCount();
                    }
                    int count1 = (int) java.util.Arrays.stream(r1.itemInputs).filter(ingr -> ingr != null).count();

                    int sum2 = 0;
                    for (IngredientWithSize ingr : r2.itemInputs) {
                        if (ingr != null) sum2 += ingr.getCount();
                    }
                    int count2 = (int) java.util.Arrays.stream(r2.itemInputs).filter(ingr -> ingr != null).count();

                    int cmp = Integer.compare(sum1, sum2);
                    if (cmp == 0) cmp = Integer.compare(count1, count2);
                    return cmp;
                })
                .orElse(allMatching.get(0));

        int bestSum = 0;
        for (IngredientWithSize ingr : best.itemInputs) {
            if (ingr != null) bestSum += ingr.getCount();
        }
        int bestCount = (int) java.util.Arrays.stream(best.itemInputs).filter(ingr -> ingr != null).count();

        LOGGER.info("[MixerPriority] Selected recipe {} (sum: {}, count: {})", best.getId(), bestSum, bestCount);

        for (MixerRecipe r : allMatching) {
            if (r != best) {
                int sum = 0;
                for (IngredientWithSize ingr : r.itemInputs) {
                    if (ingr != null) sum += ingr.getCount();
                }
                int count = (int) java.util.Arrays.stream(r.itemInputs).filter(ingr -> ingr != null).count();
                LOGGER.info("[MixerPriority] Other candidate {} (sum: {}, count: {})", r.getId(), sum, count);
            }
        }

        cir.setReturnValue(best);
    }
}
