package ferro2000.immersivetech.common.util.compat.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.api.liquid.ILiquidStack;

import ferro2000.immersivetech.api.ITUtils;
import ferro2000.immersivetech.api.crafting.BoilerFuelRecipe;
import ferro2000.immersivetech.api.crafting.BoilerRecipe;
import net.minecraftforge.fluids.FluidStack;

import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.immersivetech.Boiler")
public class Boiler {
	private static class AddRecipe implements IAction {
		public BoilerRecipe recipe;
		public AddRecipe(BoilerRecipe recipe) {
			this.recipe = recipe;
		}

		@Override
		public void apply() {
			BoilerRecipe.recipeList.add(recipe);
		}

		@Override
		public String describe() {
			return "Adding " + BoilerRecipe.recipeCategoryName + " Recipe for'" + recipe.recipeName + "'";
		}
	}

	private static class RemoveRecipe implements IAction {
		public BoilerRecipe recipe;
		public RemoveRecipe(BoilerRecipe recipe) {
			this.recipe = recipe;
		}

		@Override
		public void apply() {
			BoilerRecipe.recipeList.remove(recipe);
		}

		@Override
		public String describe() {
			return "Removing " + BoilerRecipe.recipeCategoryName + " Recipe for'" + recipe.recipeName + "'";
		}
	}

	private static class AddFuel implements IAction {
		public BoilerFuelRecipe recipe;
		public AddFuel(BoilerFuelRecipe recipe) {
			this.recipe = recipe;
		}

		@Override
		public void apply() {
			BoilerFuelRecipe.recipeList.add(recipe);
		}

		@Override
		public String describe() {
			return "Adding " + BoilerFuelRecipe.recipeCategoryName + " Fuel for'" + recipe.recipeName + "'";
		}
	}

	private static class RemoveFuel implements IAction {
		public BoilerFuelRecipe recipe;
		public RemoveFuel(BoilerFuelRecipe recipe) {
			this.recipe = recipe;
		}

		@Override
		public void apply() {
			BoilerFuelRecipe.recipeList.remove(recipe);
		}

		@Override
		public String describe() {
			return "Removing " + BoilerFuelRecipe.recipeCategoryName + " Fuel for'" + recipe.recipeName + "'";
		}
	}

	@ZenMethod
	public static void removeFuel(ILiquidStack input) {
		FluidStack fluid = CraftTweakerHelper.toFluidStack(input);
		if(fluid == null) {
			CraftTweakerAPI.logError("Can't remove " + BoilerFuelRecipe.recipeCategoryName + " Fuel with null input!");
			return;
		}
		BoilerFuelRecipe recipe = ITUtils.First(BoilerFuelRecipe.recipeList, fluid);
		if(recipe == null) {
			CraftTweakerAPI.logWarning(BoilerFuelRecipe.recipeCategoryName +" recipe with input " + fluid.getLocalizedName() + " can't be found! Skipping...");
			return;
		}
		CraftTweakerAPI.apply(new RemoveFuel(recipe));
	}

	@ZenMethod
	public static void addFuel(ILiquidStack input, int time, double heat) {
		if(input == null) {
			CraftTweakerAPI.logError("Can't add recipe for" + BoilerFuelRecipe.recipeCategoryName + " with null input!");
			return;
		}
		FluidStack fIn = CraftTweakerHelper.toFluidStack(input);

		if(fIn == null) {
			CraftTweakerAPI.logError("Invalid recipe for" + BoilerFuelRecipe.recipeCategoryName + " with input " + input.getDisplayName());
			return;
		}
		if(ITUtils.First(BoilerFuelRecipe.recipeList, fIn) != null) {
			CraftTweakerAPI.logWarning("Recipe for" + BoilerFuelRecipe.recipeCategoryName + " with input " + input.getDisplayName() + " already exists! Skipping...");
			return;
		}
		BoilerFuelRecipe recipe = new BoilerFuelRecipe(fIn, time, heat);
		CraftTweakerAPI.apply(new AddFuel(recipe));
	}

	@ZenMethod
	public static void removeRecipe(ILiquidStack input) {
		FluidStack fluid = CraftTweakerHelper.toFluidStack(input);
		if(fluid == null) {
			CraftTweakerAPI.logError("Can't remove " + BoilerRecipe.recipeCategoryName + " recipe with null input!");
			return;
		}
		BoilerRecipe recipe = ITUtils.First(BoilerRecipe.recipeList, fluid);
		if(recipe == null) {
			CraftTweakerAPI.logWarning(BoilerRecipe.recipeCategoryName +" recipe with input " + fluid.getLocalizedName() + " can't be found! Skipping...");
			return;
		}
		CraftTweakerAPI.apply(new RemoveRecipe(recipe));
	}

	@ZenMethod
	public static void addRecipe(ILiquidStack output, ILiquidStack input, int time) {
		if(input == null) {
			CraftTweakerAPI.logError("Can't add recipe for" + BoilerRecipe.recipeCategoryName + " with null input!");
			return;
		}
		if(output == null) {
			CraftTweakerAPI.logError("Can't add recipe for" + BoilerRecipe.recipeCategoryName + " with null output!");
			return;
		}
		FluidStack fOut = CraftTweakerHelper.toFluidStack(output);
		FluidStack fIn = CraftTweakerHelper.toFluidStack(input);

		if(fIn == null) {
			CraftTweakerAPI.logError("Invalid recipe for" + BoilerRecipe.recipeCategoryName + " with input " + input.getDisplayName());
			return;
		}
		if(fOut == null) {
			CraftTweakerAPI.logError("Invalid recipe for" + BoilerRecipe.recipeCategoryName + " with output " + output.getDisplayName());
			return;
		}
		if(ITUtils.First(BoilerRecipe.recipeList, fIn) != null) {
			CraftTweakerAPI.logWarning("Recipe for" + BoilerRecipe.recipeCategoryName + " with input " + input.getDisplayName() + " already exists! Skipping...");
			return;
		}
		BoilerRecipe recipe = new BoilerRecipe(fOut, fIn, time);
		CraftTweakerAPI.apply(new AddRecipe(recipe));
	}

}