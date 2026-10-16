package mctmods.immersivetechnology.common.multiblocks.metal.recipe;

import mctmods.immersivetechnology.core.registration.RecipeTypes;

import com.google.common.collect.Lists;
import com.immersiveconvergence.api.capability.HeatCapabilities;
import com.immersiveconvergence.api.crafting.ICCachedRecipeList;
import com.immersiveconvergence.api.crafting.ICFluidTagInput;
import com.immersiveconvergence.api.crafting.ICMultiblockRecipe;
import com.immersiveconvergence.api.crafting.ICRecipeSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;

public class BoilerTankRecipe extends ICMultiblockRecipe {
    public static RegistryObject<ICRecipeSerializer<BoilerTankRecipe>> SERIALIZER;
    public static final ICCachedRecipeList<BoilerTankRecipe> RECIPES = new ICCachedRecipeList<>(RecipeTypes.BOILER_TANK);

    public final ICFluidTagInput input;
    public final FluidStack output;
    public final double requiredHeat;

    public BoilerTankRecipe(ResourceLocation id, ICFluidTagInput input, FluidStack output, int time, double requiredHeat) {
        super(RecipeTypes.BOILER_TANK, id, time, 0);
        this.input = input;
        this.output = output;
        this.requiredHeat = Math.min(requiredHeat, HeatCapabilities.maxHeat());
        this.fluidInputList = Lists.newArrayList(this.input);
        this.fluidOutputList = Lists.newArrayList(this.output);
    }

    @Override protected ICRecipeSerializer<?> getICSerializer() { return SERIALIZER.get(); }

    public boolean matches(FluidStack fluid) { return input.test(fluid); }

    public static BoilerTankRecipe findRecipe(Level level, FluidStack input) { return findRecipe(level, input, null); }

    public static BoilerTankRecipe findRecipe(Level level, FluidStack input, @Nullable BoilerTankRecipe hint) { return RECIPES.find(level, recipe -> recipe.matches(input), hint); }
}
