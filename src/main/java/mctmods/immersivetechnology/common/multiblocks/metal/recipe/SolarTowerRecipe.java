package mctmods.immersivetechnology.common.multiblocks.metal.recipe;

import mctmods.immersivetechnology.core.registration.RecipeTypes;

import com.immersiveconvergence.api.crafting.ICCachedRecipeList;
import com.immersiveconvergence.api.crafting.ICFluidTagInput;
import com.immersiveconvergence.api.crafting.ICMultiblockRecipe;
import com.immersiveconvergence.api.crafting.ICRecipeSerializer;
import com.google.common.collect.Lists;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;

public class SolarTowerRecipe extends ICMultiblockRecipe implements ISolarRecipe {
    public static RegistryObject<ICRecipeSerializer<SolarTowerRecipe>> SERIALIZER;
    public static final ICCachedRecipeList<SolarTowerRecipe> RECIPES = new ICCachedRecipeList<>(RecipeTypes.SOLAR_TOWER);

    public final ICFluidTagInput input;
    public final FluidStack fluidOutput;
    public final double requiredTemp;

    public SolarTowerRecipe(ResourceLocation id, ICFluidTagInput input, @Nullable FluidStack fluidOutput, int time, double requiredTemp) {
        super(RecipeTypes.SOLAR_TOWER, id, time, 0);
        this.input = input;
        this.fluidOutput = fluidOutput;
        this.requiredTemp = requiredTemp;
        this.fluidInputList = Lists.newArrayList(this.input);
        this.fluidOutputList = fluidOutput == null ? Lists.newArrayList() : Lists.newArrayList(fluidOutput);
    }

    @Override public ICFluidTagInput input() { return input; }

    @Override public FluidStack fluidOutput() { return fluidOutput; }

    @Override public double requiredTemp() { return requiredTemp; }

    public boolean matches(FluidStack fluid) { return input.testIgnoringAmount(fluid) && fluid.getAmount() >= input.getAmount(); }

    @Nullable public static SolarTowerRecipe findRecipe(Level level, FluidStack fluid) { return findRecipe(level, fluid, null); }

    @Nullable public static SolarTowerRecipe findRecipe(Level level, FluidStack fluid, @Nullable SolarTowerRecipe hint) {
        if (fluid == null || fluid.isEmpty()) return null;
        return RECIPES.find(level, recipe -> recipe.matches(fluid), hint);
    }

    @Override protected ICRecipeSerializer<?> getICSerializer() { return SERIALIZER.get(); }
}
