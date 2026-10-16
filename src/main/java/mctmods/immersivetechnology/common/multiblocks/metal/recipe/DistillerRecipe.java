package mctmods.immersivetechnology.common.multiblocks.metal.recipe;

import mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer.DistillerRecipeSerializer;
import mctmods.immersivetechnology.core.registration.RecipeTypes;

import com.immersiveconvergence.api.crafting.ICCachedRecipeList;
import com.immersiveconvergence.api.crafting.ICMultiblockRecipe;
import com.immersiveconvergence.api.crafting.ICRecipeSerializer;
import com.immersiveconvergence.api.crafting.ICTagOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredHolder;

import javax.annotation.Nullable;

public class DistillerRecipe extends ICMultiblockRecipe {
    public static DeferredHolder<RecipeSerializer<?>, DistillerRecipeSerializer> SERIALIZER;
    public static final ICCachedRecipeList<DistillerRecipe> RECIPES = new ICCachedRecipeList<>(RecipeTypes.DISTILLER);

    public final TagKey<Fluid> fluidTag;
    private final int amount;
    @Nullable public final FluidStack fluidOutput;
    public final ItemStack itemOutput;
    public final float chance;

    public DistillerRecipe(TagKey<Fluid> fluidTag, int amount, @Nullable FluidStack fluidOutput, ItemStack itemOutput, float chance, int time, int energy) {
        super(ICTagOutput.EMPTY, RecipeTypes.DISTILLER, time, energy);
        this.fluidTag = fluidTag;
        this.amount = amount;
        this.fluidOutput = fluidOutput;
        this.itemOutput = itemOutput;
        this.chance = chance;
    }

    public TagKey<Fluid> fluidTag() { return fluidTag; }
    public int amount() { return amount; }
    public FluidStack fluidOutput() { return fluidOutput; }
    public ItemStack itemOutput() { return itemOutput; }
    public float chance() { return chance; }

    public boolean matches(FluidStack stack) { return stack.is(this.fluidTag); }

    public int getInputAmount() { return amount; }

    public static RecipeHolder<DistillerRecipe> findRecipe(Level level, FluidStack inputFluid) { return findRecipe(level, inputFluid, null); }

    public static RecipeHolder<DistillerRecipe> findRecipe(Level level, FluidStack inputFluid, @Nullable RecipeHolder<DistillerRecipe> hint) {
        if (inputFluid.isEmpty() || inputFluid.getAmount() <= 0) { return null; }
        return RECIPES.findHolder(level, recipe -> recipe.matches(inputFluid), hint);
    }

    @Override protected ICRecipeSerializer<?> getICSerializer() { return SERIALIZER.get(); }
}
