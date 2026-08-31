package mctmods.immersivetechnology.common.util.compat.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.api.liquid.ILiquidStack;
import mctmods.immersivetechnology.api.crafting.BoilerLiquidRecipe;
import net.minecraftforge.fluids.FluidStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@SuppressWarnings("unused")
@ZenClass("mods.immersivetechnology.BoilerLiquid")
public class BoilerLiquid {

    @ZenMethod
    public static void addFuel(ILiquidStack inputFluid, int time, double heatPerTick, double targetHeat) {
        FluidStack fluidIn = CraftTweakerHelper.toFluidStack(inputFluid);

        if (fluidIn == null) { return; }

        BoilerLiquidRecipe recipe = new BoilerLiquidRecipe(fluidIn, time, heatPerTick, targetHeat);
        CraftTweakerAPI.apply(new AddFuel(recipe));
    }

    private static class AddFuel implements IAction {
        public BoilerLiquidRecipe recipe;
        public AddFuel(BoilerLiquidRecipe recipe) { this.recipe = recipe; }

        @Override public void apply() { BoilerLiquidRecipe.addFuel(recipe); }

        @Override public String describe() { return "Adding Liquid Boiler Fuel for " + recipe.fluidInput.getLocalizedName(); }
    }

    @ZenMethod
    public static void removeFuel(ILiquidStack inputFluid) {
        FluidStack fluidIn = CraftTweakerHelper.toFluidStack(inputFluid);
        if (fluidIn != null) { CraftTweakerAPI.apply(new RemoveFuel(fluidIn)); }
    }

    private static class RemoveFuel implements IAction {
        private final FluidStack inputFluid;

        public RemoveFuel(FluidStack inputFluid) { this.inputFluid = inputFluid; }

        @Override public void apply() { BoilerLiquidRecipe.removeFuel(inputFluid); }

        @Override public String describe() { return "Removing Liquid Boiler Fuel for " + inputFluid.getLocalizedName(); }
    }
}
