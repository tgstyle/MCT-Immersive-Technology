package ferro2000.immersivetech.common.integration.Crafttweaker.steamturbine;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.api.liquid.ILiquidStack;
import ferro2000.immersivetech.api.ITUtils;
import ferro2000.immersivetech.api.crafting.SteamTurbineRecipe;
import ferro2000.immersivetech.common.integration.Crafttweaker.CraftTweakerHelper;
import net.minecraftforge.fluids.FluidStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.immersivetech.SteamTurbine")
public class SteamTurbineActions {

    private static class Add implements IAction {

        public SteamTurbineRecipe recipe;

        public Add(SteamTurbineRecipe recipe) {
            this.recipe = recipe;
        }

        @Override
        public void apply() {
            SteamTurbineRecipe.recipeList.add(recipe);
        }

        @Override
        public String describe() {
            return "Adding " + recipe.recipeCategoryName + " Recipe for '" + recipe.recipeName + "'";
        }
    }

    private static class Remove implements IAction {

        public SteamTurbineRecipe recipe;

        public Remove(SteamTurbineRecipe recipe) {
            this.recipe = recipe;
        }

        @Override
        public void apply() {
            SteamTurbineRecipe.recipeList.remove(recipe);
        }

        @Override
        public String describe() {
            return "Removing " + recipe.recipeCategoryName + " Recipes for '" + recipe.recipeName + "'";
        }
    }

    @ZenMethod
    public static void removeFuel(ILiquidStack input) {
        FluidStack fluid = CraftTweakerHelper.toFluidStack(input);
        if (fluid == null) {
            CraftTweakerAPI.logError("Can't remove " + SteamTurbineRecipe.recipeCategoryName + " recipe with null input!");
            return;
        }
        SteamTurbineRecipe recipe = ITUtils.First(SteamTurbineRecipe.recipeList, fluid);
        if (recipe == null) {
            CraftTweakerAPI.logWarning(SteamTurbineRecipe.recipeCategoryName +" recipe with input " + fluid.getLocalizedName() + " can't be found! Skipping...");
            return;
        }
        CraftTweakerAPI.apply(new Remove(recipe));
    }

    @ZenMethod
    public static void addFuel(ILiquidStack output, ILiquidStack input0, int time)
    {
        if(input0 == null) {
            CraftTweakerAPI.logError("Can't add recipe for " + SteamTurbineRecipe.recipeCategoryName + " with null input!");
            return;
        }

        FluidStack fOut = CraftTweakerHelper.toFluidStack(output);
        FluidStack fIn0 = CraftTweakerHelper.toFluidStack(input0);

        if(fIn0 == null) {
            CraftTweakerAPI.logError("Invalid recipe for " + SteamTurbineRecipe.recipeCategoryName + " with input " + input0.getDisplayName());
            return;
        }

        if (ITUtils.First(SteamTurbineRecipe.recipeList, fIn0) != null) {
            CraftTweakerAPI.logWarning("Recipe for " + SteamTurbineRecipe.recipeCategoryName + " with input " + input0.getDisplayName() + " already exists! Skipping...");
            return;
        }
        SteamTurbineRecipe recipe = new SteamTurbineRecipe(fOut, fIn0, time);
        CraftTweakerAPI.apply(new Add(recipe));
    }

}