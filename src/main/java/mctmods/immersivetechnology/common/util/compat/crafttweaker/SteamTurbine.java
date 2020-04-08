package mctmods.immersivetechnology.common.util.compat.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.api.liquid.ILiquidStack;
import mctmods.immersivetechnology.api.crafting.SteamTurbineRecipe;
import net.minecraftforge.fluids.FluidStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.ArrayList;
import java.util.Iterator;

@ZenClass("mods.immersivetechnology.SteamTurbine")
public class SteamTurbine {

	@ZenMethod
	public static void addFuel(ILiquidStack outputFluid, ILiquidStack inputFluid, int time) {
		FluidStack fluidOut = CraftTweakerHelper.toFluidStack(outputFluid);
		FluidStack fluidIn = CraftTweakerHelper.toFluidStack(inputFluid);

		if(fluidIn == null) return;

		SteamTurbineRecipe recipe = new SteamTurbineRecipe(fluidOut, fluidIn, time);
		CraftTweakerAPI.apply(new Add(recipe));
	}

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
			if(recipe.fluidOutput == null) return "Adding Steam Turbine Fuel for " + recipe.fluidInput.getLocalizedName();
			return "Adding Steam Turbine Fuel for " + recipe.fluidInput.getLocalizedName() + " -> " + recipe.fluidOutput.getLocalizedName();
		}
	}

	@ZenMethod
	public static void removeFuel(ILiquidStack inputFluid) {
		if(CraftTweakerHelper.toFluidStack(inputFluid) != null) CraftTweakerAPI.apply(new Remove(CraftTweakerHelper.toFluidStack(inputFluid)));
	}

	private static class Remove implements IAction {
		private final FluidStack inputFluid;
		ArrayList<SteamTurbineRecipe> removedRecipes = new ArrayList<SteamTurbineRecipe>();

		public Remove(FluidStack inputFluid) {
			this.inputFluid = inputFluid;
		}

		@Override
		public void apply() {
			Iterator<SteamTurbineRecipe> iterator = SteamTurbineRecipe.recipeList.iterator();
			while(iterator.hasNext()) {
				SteamTurbineRecipe recipe = iterator.next();
				if(recipe != null && recipe.fluidInput.isFluidEqual(inputFluid)) {
					removedRecipes.add(recipe);
					iterator.remove();
				}
			}
		}

		@Override
		public String describe() {
			return "Removing Steam Turbine Fuel for " + inputFluid.getLocalizedName();
		}
	}

}