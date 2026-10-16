package mctmods.immersivetechnology.common.multiblocks.metal.recipe;

import mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer.BoilerSolidRecipeSerializer;
import mctmods.immersivetechnology.core.registration.RecipeTypes;

import com.google.common.collect.Lists;
import com.immersiveconvergence.api.capability.HeatCapabilities;
import com.immersiveconvergence.api.crafting.ICCachedRecipeList;
import com.immersiveconvergence.api.crafting.ICIngredientWithSize;
import com.immersiveconvergence.api.crafting.ICMultiblockRecipe;
import com.immersiveconvergence.api.crafting.ICRecipeSerializer;
import com.immersiveconvergence.api.crafting.ICTagOutput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;

import javax.annotation.Nullable;

public class BoilerSolidRecipe extends ICMultiblockRecipe {
    public static DeferredHolder<RecipeSerializer<?>, BoilerSolidRecipeSerializer> SERIALIZER;
    public static final ICCachedRecipeList<BoilerSolidRecipe> RECIPES = new ICCachedRecipeList<>(RecipeTypes.BOILER_SOLID);

    public ICIngredientWithSize input;
    private final double heatPerTick;
    private final double targetHeat;

    public BoilerSolidRecipe(ICIngredientWithSize input, double heatPerTick, double targetHeat) {
        super(ICTagOutput.EMPTY, RecipeTypes.BOILER_SOLID, 0, 0);
        this.input = input;
        this.heatPerTick = heatPerTick;
        this.targetHeat = Math.min(targetHeat, HeatCapabilities.maxHeat());
        setInputListWithSizes(Lists.newArrayList(this.input));
    }

    public ICIngredientWithSize input() { return input; }
    public double heatPerTick() { return heatPerTick; }
    public double targetHeat() { return targetHeat; }

    public static BoilerSolidRecipe findRecipe(Level level, ItemStack input) { return findRecipe(level, input, null); }

    public static BoilerSolidRecipe findRecipe(Level level, ItemStack input, @Nullable BoilerSolidRecipe hint) {
        if (input.isEmpty()) return null;
        return RECIPES.find(level, recipe -> recipe.input.testIgnoringSize(input), hint);
    }

    @Override protected ICRecipeSerializer<?> getICSerializer() { return SERIALIZER.get(); }

    public double getHeatPerTick() {
        return heatPerTick;
    }

    public double getTargetHeat() {
        return targetHeat;
    }
}
