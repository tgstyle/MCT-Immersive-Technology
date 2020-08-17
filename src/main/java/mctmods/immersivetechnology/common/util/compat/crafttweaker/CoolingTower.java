package mctmods.immersivetechnology.common.util.compat.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.api.liquid.ILiquidStack;
import mctmods.immersivetechnology.api.crafting.CoolingTowerRecipe;
import net.minecraftforge.fluids.FluidStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.ArrayList;
import java.util.Iterator;

@ZenClass("mods.immersivetechnology.CoolingTower")
public class CoolingTower {

    @ZenMethod
    public static void addRecipe(ILiquidStack outputFluid1, ILiquidStack outputFluid2, ILiquidStack inputFluid1, ILiquidStack inputFluid2, int time) {
        FluidStack fluidOut1 = CraftTweakerHelper.toFluidStack(outputFluid1);
        FluidStack fluidOut2 = CraftTweakerHelper.toFluidStack(outputFluid2);
        FluidStack fluidIn1 = CraftTweakerHelper.toFluidStack(inputFluid1);
        FluidStack fluidIn2 = CraftTweakerHelper.toFluidStack(inputFluid2);

        if(fluidIn1 == null || fluidOut1 == null) return;

        CoolingTowerRecipe recipe = new CoolingTowerRecipe(fluidOut1, fluidOut2, fluidIn1, fluidIn2, time);
        CraftTweakerAPI.apply(new CoolingTower.Add(recipe));
    }

    private static class Add implements IAction {
        public CoolingTowerRecipe recipe;
        public Add(CoolingTowerRecipe recipe) {
            this.recipe = recipe;
        }

        @Override
        public void apply() {
            CoolingTowerRecipe.recipeList.add(recipe);
        }

        @Override
        public String describe() {
            return "Adding Cooling Tower recipe for " + recipe.fluidInput0.getLocalizedName();
        }
    }

    @ZenMethod
    public static void removeRecipe(ILiquidStack inputFluid1, ILiquidStack inputFluid2) {
        FluidStack fluidIn1 = CraftTweakerHelper.toFluidStack(inputFluid1);
        FluidStack fluidIn2 = CraftTweakerHelper.toFluidStack(inputFluid2);
        if(fluidIn1 != null)
            CraftTweakerAPI.apply(new CoolingTower.Remove(fluidIn1, fluidIn2));
    }

    private static class Remove implements IAction {
        private final FluidStack inputFluid1;
        private final FluidStack inputFluid2;
        ArrayList<CoolingTowerRecipe> removedRecipes = new ArrayList<CoolingTowerRecipe>();

        public Remove(FluidStack inputFluid1, FluidStack inputFluid2) {
            this.inputFluid1 = inputFluid1;
            this.inputFluid2 = inputFluid2;
        }

        @Override
        public void apply() {
            Iterator<CoolingTowerRecipe> iterator = CoolingTowerRecipe.recipeList.iterator();
            while(iterator.hasNext()) {
                CoolingTowerRecipe recipe = iterator.next();
                if(recipe != null && recipe.fluidInput0.isFluidEqual(inputFluid1) &&
                        (inputFluid2 == null || recipe.fluidInput1.isFluidEqual(inputFluid2))) {
                    removedRecipes.add(recipe);
                    iterator.remove();
                }
            }
        }

        @Override
        public String describe() {
            return inputFluid2 == null? "Removing Cooling Tower Recipe for " + inputFluid1.getLocalizedName() :
                    "Removing Cooling Tower Recipe for " + inputFluid1.getLocalizedName() + " and " + inputFluid2.getLocalizedName();
        }
    }
}
