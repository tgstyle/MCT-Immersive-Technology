package mctmods.immersivetechnology.common.util.compat.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.api.liquid.ILiquidStack;
import mctmods.immersivetechnology.api.crafting.HeatExchangerRecipe;
import net.minecraftforge.fluids.FluidStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.immersivetechnology.HeatExchanger")
public class HeatExchanger {

    @ZenMethod
    public static void addRecipe(ILiquidStack outputFluid0, ILiquidStack outputFluid1, ILiquidStack inputFluid0, ILiquidStack inputFluid1, int energy, int time) {
        FluidStack fluidOut0 = CraftTweakerHelper.toFluidStack(outputFluid0);
        FluidStack fluidOut1 = CraftTweakerHelper.toFluidStack(outputFluid1);
        FluidStack fluidIn0 = CraftTweakerHelper.toFluidStack(inputFluid0);
        FluidStack fluidIn1 = CraftTweakerHelper.toFluidStack(inputFluid1);

        if(fluidIn0 == null || fluidIn1 == null || fluidOut0 == null) return;

        HeatExchangerRecipe recipe = new HeatExchangerRecipe(fluidOut0, fluidOut1, fluidIn0, fluidIn1, energy, time);
        CraftTweakerAPI.apply(new HeatExchanger.Add(recipe));
    }

    private static class Add implements IAction {
        public HeatExchangerRecipe recipe;
        public Add(HeatExchangerRecipe recipe) {
            this.recipe = recipe;
        }

        @Override
        public void apply() {
            HeatExchangerRecipe.recipeList.add(recipe);
        }

        @Override
        public String describe() {
            return "Adding Heat Exchanger recipe for " + recipe.fluidInput0.getLocalizedName();
        }
    }

    @ZenMethod
    public static void removeRecipe(ILiquidStack inputFluid0, ILiquidStack inputFluid1) {
        FluidStack fluidIn0 = CraftTweakerHelper.toFluidStack(inputFluid0);
        FluidStack fluidIn1 = CraftTweakerHelper.toFluidStack(inputFluid1);
        if(fluidIn0 != null && fluidIn1 != null)
            CraftTweakerAPI.apply(new HeatExchanger.Remove(fluidIn0, fluidIn1));
    }

    private static class Remove implements IAction {
        private final FluidStack inputFluid0;
        private final FluidStack inputFluid1;

        public Remove(FluidStack inputFluid0, FluidStack inputFluid1) {
            this.inputFluid0 = inputFluid0;
            this.inputFluid1 = inputFluid1;
        }

        @Override
        public void apply() {
            HeatExchangerRecipe.recipeList.removeIf(recipe -> recipe != null &&
                    recipe.fluidInput0.isFluidEqual(inputFluid0) &&
                    recipe.fluidInput1.isFluidEqual(inputFluid1));
        }

        @Override
        public String describe() {
            return inputFluid1 == null? "Removing Heat Exchanger Recipe for " + inputFluid0.getLocalizedName() :
                    "Removing Heat Exchanger Recipe for " + inputFluid0.getLocalizedName() + " and " + inputFluid1.getLocalizedName();
        }
    }
}
