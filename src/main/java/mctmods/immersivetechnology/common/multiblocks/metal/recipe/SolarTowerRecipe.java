package mctmods.immersivetechnology.common.multiblocks.metal.recipe;

import mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer.SolarTowerRecipeSerializer;
import mctmods.immersivetechnology.core.registration.RecipeTypes;

import com.immersiveconvergence.api.crafting.ICCachedRecipeList;
import com.immersiveconvergence.api.crafting.ICMultiblockRecipe;
import com.immersiveconvergence.api.crafting.ICRecipeSerializer;
import com.immersiveconvergence.api.crafting.ICTagOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredHolder;

import javax.annotation.Nullable;

public class SolarTowerRecipe extends ICMultiblockRecipe implements ISolarRecipe {
    public static DeferredHolder<RecipeSerializer<?>, SolarTowerRecipeSerializer> SERIALIZER;
    public static final ICCachedRecipeList<SolarTowerRecipe> RECIPES = new ICCachedRecipeList<>(RecipeTypes.SOLAR_TOWER);

    private final ResourceLocation id;
    public final TagKey<Fluid> inputTag;
    private final int inputAmount;
    @Nullable public final FluidStack fluidOutput;
    public final double requiredTemp;

    public SolarTowerRecipe(ResourceLocation id, TagKey<Fluid> inputTag, int inputAmount, @Nullable FluidStack fluidOutput, int time, double requiredTemp) {
        super(ICTagOutput.EMPTY, RecipeTypes.SOLAR_TOWER, time, 0);
        this.id = id;
        this.inputTag = inputTag;
        this.inputAmount = inputAmount;
        this.fluidOutput = fluidOutput;
        this.requiredTemp = requiredTemp;
    }

    public SolarTowerRecipe(TagKey<Fluid> inputTag, int inputAmount, @Nullable FluidStack fluidOutput, int time, double requiredTemp) {
        this(ResourceLocation.fromNamespaceAndPath("immersivetechnology", "codec_generated"), inputTag, inputAmount, fluidOutput, time, requiredTemp);
    }

    public ResourceLocation getId() { return id; }
    public TagKey<Fluid> inputTag() { return inputTag; }
    public int inputAmount() { return inputAmount; }
    public int getInputAmount() { return inputAmount; }
    public FluidStack fluidOutput() { return fluidOutput; }
    @Override public double requiredTemp() { return requiredTemp; }

    public boolean matches(FluidStack stack) {
        return stack != null && stack.is(this.inputTag);
    }

    @Nullable public static SolarTowerRecipe findRecipe(Level level, FluidStack fluid) { return findRecipe(level, fluid, null); }

    @Nullable public static SolarTowerRecipe findRecipe(Level level, FluidStack fluid, @Nullable SolarTowerRecipe hint) {
        if (fluid == null || fluid.isEmpty()) return null;
        return RECIPES.find(level, recipe -> recipe.matches(fluid) && fluid.getAmount() >= recipe.getInputAmount(), hint);
    }

    @Override protected ICRecipeSerializer<?> getICSerializer() { return SERIALIZER.get(); }
}
