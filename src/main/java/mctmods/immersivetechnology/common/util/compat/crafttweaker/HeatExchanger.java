package mctmods.immersivetechnology.common.util.compat.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.api.liquid.ILiquidStack;
import mctmods.immersivetechnology.api.crafting.HeatExchangerRecipe;
import net.minecraftforge.fluids.FluidStack;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@SuppressWarnings("unused")
@ZenClass("mods.immersivetechnology.HeatExchanger")
public class HeatExchanger {

    @ZenMethod
    public static void addRecipe(ILiquidStack outputFluid0, ILiquidStack outputFluid1, ILiquidStack inputFluid0, ILiquidStack inputFluid1, int energy, int time) {
        FluidStack fluidOut0 = CraftTweakerHelper.toFluidStack(outputFluid0);
        FluidStack fluidOut1 = CraftTweakerHelper.toFluidStack(outputFluid1);
        FluidStack fluidIn0 = CraftTweakerHelper.toFluidStack(inputFluid0);
        FluidStack fluidIn1 = CraftTweakerHelper.toFluidStack(inputFluid1);

        if (fluidIn0 == null || fluidIn1 == null || fluidOut0 == null) { return; }

        HeatExchangerRecipe recipe = new HeatExchangerRecipe(fluidOut0, fluidOut1, fluidIn0, fluidIn1, energy, time);
        CraftTweakerAPI.apply(new Add(recipe));
    }

    private static class Add implements IAction {
        public HeatExchangerRecipe recipe;
        public Add(HeatExchangerRecipe recipe) { this.recipe = recipe; }

        @Override public void apply() { HeatExchangerRecipe.addRecipe(recipe); }

        @Override public String describe() {
            String desc = "Adding Heat Exchanger recipe: " + recipe.fluidInput0.getLocalizedName() + " + " + recipe.fluidInput1.getLocalizedName();
            if (recipe.fluidOutput1 != null) {
                return desc + " -> " + recipe.fluidOutput0.getLocalizedName() + " + " + recipe.fluidOutput1.getLocalizedName();
            }
            return desc + " -> " + recipe.fluidOutput0.getLocalizedName();
        }
    }

    @ZenMethod
    public static void removeRecipe(ILiquidStack inputFluid0, @Optional ILiquidStack inputFluid1) {
        FluidStack fluidIn0 = CraftTweakerHelper.toFluidStack(inputFluid0);
        FluidStack fluidIn1 = CraftTweakerHelper.toFluidStack(inputFluid1);

        if (fluidIn0 != null) { CraftTweakerAPI.apply(new Remove(fluidIn0, fluidIn1)); }
    }

    private static class Remove implements IAction {
        private final FluidStack inputFluid0;
        private final FluidStack inputFluid1;

        public Remove(FluidStack inputFluid0, FluidStack inputFluid1) {
            this.inputFluid0 = inputFluid0;
            this.inputFluid1 = inputFluid1;
        }

        @Override public void apply() {
            HeatExchangerRecipe.removeRecipe(inputFluid0, inputFluid1);
        }

        @Override public String describe() {
            if (inputFluid1 == null) {
                return "Removing Heat Exchanger recipes matching input " + inputFluid0.getLocalizedName();
            }
            return "Removing Heat Exchanger recipe for inputs " + inputFluid0.getLocalizedName() + " and " + inputFluid1.getLocalizedName();
        }
    }
}
