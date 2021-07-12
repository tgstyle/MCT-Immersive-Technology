package mctmods.immersivetechnology.common.util.compat.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.api.liquid.ILiquidStack;
import mctmods.immersivetechnology.api.crafting.SolarTowerRecipe;
import net.minecraftforge.fluids.FluidStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.ArrayList;
import java.util.Iterator;

@ZenClass("mods.immersivetechnology.SolarTower")
public class SolarTower {

	@ZenMethod
	public static void addRecipe(ILiquidStack outputFluid, ILiquidStack inputFluid, int time) {
		FluidStack fluidOut = CraftTweakerHelper.toFluidStack(outputFluid);
		FluidStack fluidIn = CraftTweakerHelper.toFluidStack(inputFluid);

		if(fluidOut == null || fluidIn == null) return;

		SolarTowerRecipe recipe = new SolarTowerRecipe(fluidOut, fluidIn, time);
		CraftTweakerAPI.apply(new Add(recipe));
	}

	private static class Add implements IAction {
		public SolarTowerRecipe recipe;
		public Add(SolarTowerRecipe recipe) {
			this.recipe = recipe;
		}

		@Override
		public void apply() {
			SolarTowerRecipe.recipeList.add(recipe);
		}

		@Override
		public String describe() {
			return "Adding Solar Tower Recipe for " + recipe.fluidInput.getLocalizedName() + " -> " + recipe.fluidOutput.getLocalizedName();
		}
	}

	@ZenMethod
	public static void removeRecipe(ILiquidStack inputFluid) {
		if(CraftTweakerHelper.toFluidStack(inputFluid) != null) CraftTweakerAPI.apply(new Remove(CraftTweakerHelper.toFluidStack(inputFluid)));
	}

	private static class Remove implements IAction {
		private final FluidStack inputFluid;
		ArrayList<SolarTowerRecipe> removedRecipes = new ArrayList<>();

		public Remove(FluidStack inputFluid) {
			this.inputFluid = inputFluid;
		}

		@Override
		public void apply() {
			Iterator<SolarTowerRecipe> iterator = SolarTowerRecipe.recipeList.iterator();
			while(iterator.hasNext()) {
				SolarTowerRecipe recipe = iterator.next();
				if(recipe != null && recipe.fluidInput.isFluidEqual(inputFluid)) {
					removedRecipes.add(recipe);
					iterator.remove();
				}
			}
		}

		@Override
		public String describe() {
			return "Removing Solar Tower Input Recipe for " + inputFluid.getLocalizedName();
		}
	}

}