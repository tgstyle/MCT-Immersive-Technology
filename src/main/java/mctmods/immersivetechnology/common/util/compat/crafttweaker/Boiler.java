package mctmods.immersivetechnology.common.util.compat.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.api.liquid.ILiquidStack;
import mctmods.immersivetechnology.api.crafting.BoilerRecipe;
import mctmods.immersivetechnology.api.crafting.BoilerRecipe.BoilerFuelRecipe;
import net.minecraftforge.fluids.FluidStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@SuppressWarnings("unused")
@ZenClass("mods.immersivetechnology.Boiler")
public class Boiler {

    @ZenMethod
    public static void addRecipe(ILiquidStack outputFluid, ILiquidStack inputFluid, int time) {
        FluidStack fluidOut = CraftTweakerHelper.toFluidStack(outputFluid);
        FluidStack fluidIn = CraftTweakerHelper.toFluidStack(inputFluid);

        if (fluidOut == null || fluidIn == null) { return; }

        BoilerRecipe recipe = new BoilerRecipe(fluidOut, fluidIn, time);
        CraftTweakerAPI.apply(new Add(recipe));
    }

    @ZenMethod
    public static void addFuel(ILiquidStack inputFluid, int time, double heat) {
        FluidStack fluidIn = CraftTweakerHelper.toFluidStack(inputFluid);

        if (fluidIn == null) { return; }

        BoilerFuelRecipe recipe = new BoilerFuelRecipe(fluidIn, time, heat);
        CraftTweakerAPI.apply(new AddFuel(recipe));
    }

    private static class Add implements IAction {
        public BoilerRecipe recipe;
        public Add(BoilerRecipe recipe) { this.recipe = recipe; }

        @Override public void apply() { BoilerRecipe.recipeList.add(recipe); }

        @Override public String describe() { return "Adding Boiler Recipe for " + recipe.fluidInput.getLocalizedName() + " -> " + recipe.fluidOutput.getLocalizedName(); }
    }

    private static class AddFuel implements IAction {
        public BoilerFuelRecipe recipe;
        public AddFuel(BoilerFuelRecipe recipe) { this.recipe = recipe; }

        @Override public void apply() { BoilerRecipe.fuelList.add(recipe); }

        @Override public String describe() { return "Adding Boiler Fuel Recipe for " + recipe.fluidInput.getLocalizedName(); }
    }

    @ZenMethod
    public static void removeRecipe(ILiquidStack inputFluid) {
        FluidStack fluidIn = CraftTweakerHelper.toFluidStack(inputFluid);
        if (fluidIn != null) { CraftTweakerAPI.apply(new Remove(fluidIn)); }
    }

    @ZenMethod
    public static void removeFuel(ILiquidStack inputFluid) {
        FluidStack fluidIn = CraftTweakerHelper.toFluidStack(inputFluid);
        if (fluidIn != null) { CraftTweakerAPI.apply(new RemoveFuel(fluidIn)); }
    }

    private static class Remove implements IAction {
        private final FluidStack inputFluid;

        public Remove(FluidStack inputFluid) { this.inputFluid = inputFluid; }

        @Override public void apply() { BoilerRecipe.recipeList.removeIf(recipe -> recipe != null && recipe.fluidInput.isFluidEqual(inputFluid)); }

        @Override public String describe() { return "Removing Boiler Input Recipe for " + inputFluid.getLocalizedName(); }
    }

    private static class RemoveFuel implements IAction {
        private final FluidStack inputFluid;

        public RemoveFuel(FluidStack inputFluid) { this.inputFluid = inputFluid; }

        @Override public void apply() { BoilerRecipe.fuelList.removeIf(recipe -> recipe != null && recipe.fluidInput.isFluidEqual(inputFluid)); }

        @Override public String describe() { return "Removing Boiler Fuel Recipe for " + inputFluid.getLocalizedName(); }
    }
}
