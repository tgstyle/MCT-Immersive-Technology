package mctmods.immersivetechnology.common.multiblocks.metal.recipe;

import mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer.StyleNOXRecipeSerializer;
import mctmods.immersivetechnology.core.registration.RecipeTypes;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.TagOutput;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StyleNOXRecipe extends MultiblockRecipe {
    public static DeferredHolder<RecipeSerializer<?>, StyleNOXRecipeSerializer> SERIALIZER;
    public static final CachedRecipeList<StyleNOXRecipe> RECIPES = new CachedRecipeList<>(RecipeTypes.STYLENOX);

    public final IngredientWithSize input;
    public final TagOutput output;
    public final TagKey<Fluid> waterTag;
    private final int waterAmount;
    public final TagKey<Fluid> co2Tag;
    private final int co2Amount;
    public final FluidStack steamOutput;

    public StyleNOXRecipe(IngredientWithSize input, TagOutput output, TagKey<Fluid> waterTag, int waterAmount, TagKey<Fluid> co2Tag, int co2Amount, FluidStack steamOutput, int time, int energy) {
        super(output, RecipeTypes.STYLENOX, time, energy, () -> new MultiblockRecipe.RecipeMultiplier(() -> 1.0, () -> 1.0));
        this.input = input;
        this.output = output;
        this.waterTag = waterTag;
        this.waterAmount = waterAmount;
        this.co2Tag = co2Tag;
        this.co2Amount = co2Amount;
        this.steamOutput = steamOutput;
    }

    public IngredientWithSize input() { return input; }
    public TagOutput output() { return output; }
    public TagKey<Fluid> waterTag() { return waterTag; }
    public int waterAmount() { return waterAmount; }
    public TagKey<Fluid> co2Tag() { return co2Tag; }
    public int co2Amount() { return co2Amount; }
    public FluidStack steamOutput() { return steamOutput; }

    public boolean matches(ItemStack stack) { return this.input.testIgnoringSize(stack); }

    public static RecipeHolder<StyleNOXRecipe> findRecipe(Level level, ItemStack input) { return findRecipe(level, input, null); }

    public static RecipeHolder<StyleNOXRecipe> findRecipe(Level level, ItemStack input, @Nullable RecipeHolder<StyleNOXRecipe> hint) {
        if (input.isEmpty()) { return null; }
        if (hint != null && hint.value().matches(input)) { return hint; }
        for (RecipeHolder<StyleNOXRecipe> holder : RECIPES.getRecipes(level)) {
            if (holder.value().matches(input)) { return holder; }
        }
        return null;
    }

    @Override @NotNull public ItemStack getResultItem(HolderLookup.Provider access) { return this.output.get(); }

    @Override protected IERecipeSerializer<?> getIESerializer() { return SERIALIZER.get(); }

    @Override public int getMultipleProcessTicks() { return 0; }
}
