package mctmods.immersivetechnology.common.util.compat.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.api.liquid.ILiquidStack;
import mctmods.immersivetechnology.api.crafting.GasTurbineRecipe;
import net.minecraftforge.fluids.FluidStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@SuppressWarnings("unused")
@ZenClass("mods.immersivetechnology.GasTurbine")
public class GasTurbine {

    @ZenMethod
    public static void addFuel(ILiquidStack outputFluid, ILiquidStack inputFluid, int time) {
        FluidStack fluidOut = CraftTweakerHelper.toFluidStack(outputFluid);
        FluidStack fluidIn = CraftTweakerHelper.toFluidStack(inputFluid);

        if (fluidIn == null) { return; }

        GasTurbineRecipe recipe = new GasTurbineRecipe(fluidOut, fluidIn, time);
        CraftTweakerAPI.apply(new Add(recipe));
    }

    private static class Add implements IAction {
        public GasTurbineRecipe recipe;
        public Add(GasTurbineRecipe recipe) { this.recipe = recipe; }

        @Override public void apply() { GasTurbineRecipe.addFuel(recipe); }

        @Override public String describe() {
            if (recipe.fluidOutput == null) { return "Adding Gas Turbine Fuel for " + recipe.fluidInput.getLocalizedName(); }
            return "Adding Gas Turbine Fuel for " + recipe.fluidInput.getLocalizedName() + " -> " + recipe.fluidOutput.getLocalizedName();
        }
    }

    @ZenMethod
    public static void removeFuel(ILiquidStack inputFluid) {
        FluidStack fluidIn = CraftTweakerHelper.toFluidStack(inputFluid);
        if (fluidIn != null) { CraftTweakerAPI.apply(new Remove(fluidIn)); }
    }

    private static class Remove implements IAction {
        private final FluidStack inputFluid;

        public Remove(FluidStack inputFluid) { this.inputFluid = inputFluid; }

        @Override public void apply() { GasTurbineRecipe.removeFuel(inputFluid); }

        @Override public String describe() { return "Removing Gas Turbine Fuel for " + inputFluid.getLocalizedName(); }
    }
}
