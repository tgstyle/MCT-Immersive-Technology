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

public class BoilerLiquidRecipe extends ICMultiblockRecipe {
    public static RegistryObject<ICRecipeSerializer<BoilerLiquidRecipe>> SERIALIZER;
    public static final ICCachedRecipeList<BoilerLiquidRecipe> RECIPES = new ICCachedRecipeList<>(RecipeTypes.BOILER_LIQUID);

    public final ICFluidTagInput input;
    private final double heatPerTick;
    private final double targetHeat;

    public BoilerLiquidRecipe(ResourceLocation id, ICFluidTagInput input, int time, double heatPerTick, double targetHeat) {
        super(RecipeTypes.BOILER_LIQUID, id, time, 0);
        this.input = input;
        this.heatPerTick = heatPerTick;
        this.targetHeat = Math.min(targetHeat, HeatCapabilities.maxHeat());
        this.fluidInputList = Lists.newArrayList(this.input);
    }

    public boolean matches(FluidStack fluid) { return input.test(fluid); }

    public static BoilerLiquidRecipe findRecipe(Level level, FluidStack input) { return findRecipe(level, input, null); }

    public static BoilerLiquidRecipe findRecipe(Level level, FluidStack input, @Nullable BoilerLiquidRecipe hint) { return RECIPES.find(level, recipe -> recipe.matches(input), hint); }

    @Override protected ICRecipeSerializer<?> getICSerializer() { return SERIALIZER.get(); }

    public double getHeatPerTick() { return heatPerTick; }

    public double getTargetHeat() { return targetHeat; }
}
