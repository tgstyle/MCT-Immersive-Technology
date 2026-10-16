package mctmods.immersivetechnology.common.multiblocks.metal.recipe;

import mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer.StyleNOXRecipeSerializer;
import mctmods.immersivetechnology.core.registration.RecipeTypes;

import com.immersiveconvergence.api.crafting.ICCachedRecipeList;
import com.immersiveconvergence.api.crafting.ICIngredientWithSize;
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

public class StyleNOXRecipe extends ICMultiblockRecipe {
    public static DeferredHolder<RecipeSerializer<?>, StyleNOXRecipeSerializer> SERIALIZER;
    public static final ICCachedRecipeList<StyleNOXRecipe> RECIPES = new ICCachedRecipeList<>(RecipeTypes.STYLENOX);

    public final ICIngredientWithSize input;
    public final ICTagOutput output;
    public final TagKey<Fluid> waterTag;
    private final int waterAmount;
    public final TagKey<Fluid> co2Tag;
    private final int co2Amount;
    public final FluidStack steamOutput;

    public StyleNOXRecipe(ICIngredientWithSize input, ICTagOutput output, TagKey<Fluid> waterTag, int waterAmount, TagKey<Fluid> co2Tag, int co2Amount, FluidStack steamOutput, int time, int energy) {
        super(output, RecipeTypes.STYLENOX, time, energy);
        this.input = input;
        this.output = output;
        this.waterTag = waterTag;
        this.waterAmount = waterAmount;
        this.co2Tag = co2Tag;
        this.co2Amount = co2Amount;
        this.steamOutput = steamOutput;
    }

    public ICIngredientWithSize input() { return input; }
    public ICTagOutput output() { return output; }
    public TagKey<Fluid> waterTag() { return waterTag; }
    public int waterAmount() { return waterAmount; }
    public TagKey<Fluid> co2Tag() { return co2Tag; }
    public int co2Amount() { return co2Amount; }
    public FluidStack steamOutput() { return steamOutput; }

    public boolean matches(ItemStack stack) { return this.input.testIgnoringSize(stack); }

    public static RecipeHolder<StyleNOXRecipe> findRecipe(Level level, ItemStack input) { return findRecipe(level, input, null); }

    public static RecipeHolder<StyleNOXRecipe> findRecipe(Level level, ItemStack input, @Nullable RecipeHolder<StyleNOXRecipe> hint) {
        if (input.isEmpty()) { return null; }
        return RECIPES.findHolder(level, recipe -> recipe.matches(input), hint);
    }

    @Override @Nonnull public ItemStack getResultItem(@Nonnull HolderLookup.Provider access) { return this.output.get(); }

    @Override protected ICRecipeSerializer<?> getICSerializer() { return SERIALIZER.get(); }
}
