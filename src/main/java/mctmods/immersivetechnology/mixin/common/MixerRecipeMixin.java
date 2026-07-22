package mctmods.immersivetechnology.mixin.common;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Objects;

@Mixin(MixerRecipe.class)
public abstract class MixerRecipeMixin {

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

        MixerRecipe best = allMatching.stream()
                .max((r1, r2) -> {
                    int sum1 = 0;
                    for (IngredientWithSize ingr : r1.itemInputs) {
                        if (ingr != null) sum1 += ingr.getCount();
                    }
                    int count1 = (int) java.util.Arrays.stream(r1.itemInputs).filter(Objects::nonNull).count();

                    int sum2 = 0;
                    for (IngredientWithSize ingr : r2.itemInputs) {
                        if (ingr != null) sum2 += ingr.getCount();
                    }
                    int count2 = (int) java.util.Arrays.stream(r2.itemInputs).filter(Objects::nonNull).count();

                    int cmp = Integer.compare(sum1, sum2);
                    if (cmp == 0) cmp = Integer.compare(count1, count2);
                    return cmp;
                })
                .orElse(allMatching.get(0));

        cir.setReturnValue(best);
    }
}
