package mctmods.immersivetechnology.common.multiblocks.metal.recipe;

import mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer.BoilerTankRecipeSerializer;
import mctmods.immersivetechnology.core.registration.RecipeTypes;

import com.immersiveconvergence.api.capability.HeatCapabilities;
import com.immersiveconvergence.api.crafting.ICCachedRecipeList;
import com.immersiveconvergence.api.crafting.ICMultiblockRecipe;
import com.immersiveconvergence.api.crafting.ICRecipeSerializer;
import com.immersiveconvergence.api.crafting.ICTagOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BoilerTankRecipe extends ICMultiblockRecipe {
    public static DeferredHolder<RecipeSerializer<?>, BoilerTankRecipeSerializer> SERIALIZER;
    public static final ICCachedRecipeList<BoilerTankRecipe> RECIPES = new ICCachedRecipeList<>(RecipeTypes.BOILER_TANK);

    public final TagKey<Fluid> fluidTag;
    private final int amount;
    public final FluidStack output;
    public final double requiredHeat;
    private final int time;

    public BoilerTankRecipe(TagKey<Fluid> fluidTag, int amount, FluidStack output, int time, double requiredHeat) {
        super(ICTagOutput.EMPTY, RecipeTypes.BOILER_TANK, time, 0);
        this.fluidTag = fluidTag;
        this.amount = amount;
        this.output = output;
        this.requiredHeat = Math.min(requiredHeat, HeatCapabilities.maxHeat());
        this.time = time;
    }

    public TagKey<Fluid> fluidTag() { return fluidTag; }
    public int amount() { return amount; }
    public FluidStack output() { return output; }
    public int time() { return time; }
    public double requiredHeat() { return requiredHeat; }

    public boolean matches(FluidStack stack) {
        return stack.is(this.fluidTag);
    }

    public int getInputAmount() {
        return amount;
    }

    public static BoilerTankRecipe findRecipe(Level level, FluidStack input) { return findRecipe(level, input, null); }

    public static BoilerTankRecipe findRecipe(Level level, FluidStack input, @Nullable BoilerTankRecipe hint) {
        if (input.isEmpty() || input.getAmount() <= 0) return null;
        if (hint != null && hint.matches(input)) return hint;
        for (RecipeHolder<BoilerTankRecipe> holder : RECIPES.getRecipes(level)) {
            BoilerTankRecipe recipe = holder.value();
            if (recipe.matches(input)) return recipe;
        }
        return null;
    }

    @Override public @Nonnull ItemStack getResultItem(HolderLookup.Provider registryAccess) { return ItemStack.EMPTY; }

    @Override protected ICRecipeSerializer<?> getICSerializer() { return SERIALIZER.get(); }

    @Override public int getTotalProcessTime() { return time; }

    @Override public int getTotalProcessEnergy() { return 0; }

    @Override public int getMultipleProcessTicks() { return 0; }
}
