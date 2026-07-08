package mctmods.immersivetechnology.mixin.common;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Objects;

@Mixin(MixerRecipe.class)
public abstract class MixerRecipePriorityMixin {

    @Inject(
            method = "findRecipe(Lnet/minecraft/world/level/Level;Lnet/neoforged/neoforge/fluids/FluidStack;Lnet/minecraft/core/NonNullList;)Lnet/minecraft/world/item/crafting/RecipeHolder;",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private static void it$prioritizeHigherIngredients(Level level, FluidStack fluid, NonNullList<ItemStack> components, CallbackInfoReturnable<RecipeHolder<MixerRecipe>> cir) {
        List<RecipeHolder<MixerRecipe>> allMatching = MixerRecipe.RECIPES.getRecipes(level).stream()
                .filter(r -> r.value().matches(fluid, components))
                .toList();

        if (allMatching.isEmpty()) {
            cir.setReturnValue(null);
            return;
        }

        RecipeHolder<MixerRecipe> best = allMatching.stream()
                .max((r1, r2) -> {
                    MixerRecipe m1 = r1.value();
                    MixerRecipe m2 = r2.value();

                    int sum1 = 0;
                    for (IngredientWithSize ingr : m1.itemInputs) {
                        if (ingr != null) sum1 += ingr.getCount();
                    }
                    long count1 = m1.itemInputs.stream().filter(Objects::nonNull).count();

                    int sum2 = 0;
                    for (IngredientWithSize ingr : m2.itemInputs) {
                        if (ingr != null) sum2 += ingr.getCount();
                    }
                    long count2 = m2.itemInputs.stream().filter(Objects::nonNull).count();

                    int cmp = Integer.compare(sum1, sum2);
                    if (cmp == 0) cmp = Long.compare(count1, count2);
                    return cmp;
                })
                .orElse(allMatching.getFirst());

        cir.setReturnValue(best);
    }
}
