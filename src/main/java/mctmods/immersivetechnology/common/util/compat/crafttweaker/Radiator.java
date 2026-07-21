package mctmods.immersivetechnology.common.util.compat.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.api.liquid.ILiquidStack;
import mctmods.immersivetechnology.api.crafting.RadiatorRecipe;
import net.minecraftforge.fluids.FluidStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@SuppressWarnings("unused")
@ZenClass("mods.immersivetechnology.Radiator")
public class Radiator {

    @ZenMethod
    public static void addRecipe(ILiquidStack outputFluid, ILiquidStack inputFluid, int time) {
        FluidStack fluidOut = CraftTweakerHelper.toFluidStack(outputFluid);
        FluidStack fluidIn = CraftTweakerHelper.toFluidStack(inputFluid);

        if (fluidOut == null || fluidIn == null) { return; }

        RadiatorRecipe recipe = new RadiatorRecipe(fluidOut, fluidIn, time);
        CraftTweakerAPI.apply(new Add(recipe));
    }

    private static class Add implements IAction {
        public RadiatorRecipe recipe;
        public Add(RadiatorRecipe recipe) { this.recipe = recipe; }

        @Override public void apply() { RadiatorRecipe.recipeList.add(recipe); }

        @Override public String describe() { return "Adding Radiator Recipe for " + recipe.fluidInput.getLocalizedName() + " -> " + recipe.fluidOutput.getLocalizedName(); }
    }

    @ZenMethod
    public static void removeRecipe(ILiquidStack inputFluid) {
        FluidStack fluidIn = CraftTweakerHelper.toFluidStack(inputFluid);
        if (fluidIn != null) { CraftTweakerAPI.apply(new Remove(fluidIn)); }
    }

    private static class Remove implements IAction {
        private final FluidStack inputFluid;

        public Remove(FluidStack inputFluid) { this.inputFluid = inputFluid; }

        @Override public void apply() { RadiatorRecipe.removeRecipe(inputFluid); }

        @Override public String describe() { return "Removing Radiator Input Recipe for " + inputFluid.getLocalizedName(); }
    }
}
