package mctmods.immersivetechnology.common.multiblocks.metal.recipe;

import mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer.BoilerSolidRecipeSerializer;
import mctmods.immersivetechnology.core.registration.RecipeTypes;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.TagOutput;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import com.google.common.collect.Lists;
import com.immersiveconvergence.api.HeatCapabilities;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;
import javax.annotation.Nullable;

public class BoilerSolidRecipe extends MultiblockRecipe {
    public static DeferredHolder<RecipeSerializer<?>, BoilerSolidRecipeSerializer> SERIALIZER;
    public static final CachedRecipeList<BoilerSolidRecipe> RECIPES = new CachedRecipeList<>(RecipeTypes.BOILER_SOLID);

    public IngredientWithSize input;
    private final double heatPerTick;
    private final double targetHeat;

    public BoilerSolidRecipe(IngredientWithSize input, double heatPerTick, double targetHeat) {
        super(TagOutput.EMPTY, RecipeTypes.BOILER_SOLID, 0, 0, () -> new MultiblockRecipe.RecipeMultiplier(() -> 1.0, () -> 1.0));
        this.input = input;
        this.heatPerTick = heatPerTick;
        this.targetHeat = Math.min(targetHeat, HeatCapabilities.MAX_HEAT);
        setInputListWithSizes(Lists.newArrayList(this.input));
    }

    public IngredientWithSize input() { return input; }
    public double heatPerTick() { return heatPerTick; }
    public double targetHeat() { return targetHeat; }

    public static BoilerSolidRecipe findRecipe(Level level, ItemStack input) { return findRecipe(level, input, null); }

    public static BoilerSolidRecipe findRecipe(Level level, ItemStack input, @Nullable BoilerSolidRecipe hint) {
        if (input.isEmpty()) return null;
        if (hint != null && hint.input.testIgnoringSize(input)) return hint;
        for (RecipeHolder<BoilerSolidRecipe> holder : RECIPES.getRecipes(level)) {
            BoilerSolidRecipe recipe = holder.value();
            if (recipe.input.testIgnoringSize(input)) return recipe;
        }
        return null;
    }

    @Override
    @NotNull
    public ItemStack getResultItem(HolderLookup.Provider registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    protected IERecipeSerializer<?> getIESerializer() {
        return SERIALIZER.get();
    }

    @Override
    public int getTotalProcessTime() {
        return 0;
    }

    @Override
    public int getTotalProcessEnergy() {
        return 0;
    }

    @Override
    public int getMultipleProcessTicks() {
        return 0;
    }

    public double getHeatPerTick() {
        return heatPerTick;
    }

    public double getTargetHeat() {
        return targetHeat;
    }
}
