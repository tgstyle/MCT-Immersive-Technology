package mctmods.immersivetechnology.common.util.compat.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.api.liquid.ILiquidStack;
import mctmods.immersivetechnology.api.crafting.BoilerTankRecipe;
import net.minecraftforge.fluids.FluidStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@SuppressWarnings("unused")
@ZenClass("mods.immersivetechnology.BoilerTank")
public class BoilerTank {

    @ZenMethod
    public static void addRecipe(ILiquidStack outputFluid, ILiquidStack inputFluid, int time, double requiredHeat) {
        FluidStack fluidOut = CraftTweakerHelper.toFluidStack(outputFluid);
        FluidStack fluidIn = CraftTweakerHelper.toFluidStack(inputFluid);

        if (fluidOut == null || fluidIn == null) { return; }

        BoilerTankRecipe recipe = new BoilerTankRecipe(fluidOut, fluidIn, time, requiredHeat);
        CraftTweakerAPI.apply(new Add(recipe));
    }

    private static class Add implements IAction {
        public BoilerTankRecipe recipe;
        public Add(BoilerTankRecipe recipe) { this.recipe = recipe; }

        @Override public void apply() { BoilerTankRecipe.addRecipe(recipe); }

        @Override public String describe() { return "Adding Boiler Tank Recipe for " + recipe.fluidInput.getLocalizedName() + " -> " + recipe.fluidOutput.getLocalizedName(); }
    }

    @ZenMethod
    public static void removeRecipe(ILiquidStack inputFluid) {
        FluidStack fluidIn = CraftTweakerHelper.toFluidStack(inputFluid);
        if (fluidIn != null) { CraftTweakerAPI.apply(new Remove(fluidIn)); }
    }

    private static class Remove implements IAction {
        private final FluidStack inputFluid;

        public Remove(FluidStack inputFluid) { this.inputFluid = inputFluid; }

        @Override public void apply() { BoilerTankRecipe.removeRecipe(inputFluid); }

        @Override public String describe() { return "Removing Boiler Tank Recipe for " + inputFluid.getLocalizedName(); }
    }
}
