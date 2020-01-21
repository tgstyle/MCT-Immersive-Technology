package ferro2000.immersivetech.api.crafting;

import ferro2000.immersivetech.api.ITUtils;

import net.minecraftforge.fluids.FluidStack;

public class BoilerFuelRecipe extends GenericRecipe {
	public static RecipeList <BoilerFuelRecipe> recipeList = new RecipeList <> ();
	public static String recipeCategoryName = "Boiler";
	public final FluidStack input;
	public final double heat;
	public final int time;

	public BoilerFuelRecipe(FluidStack input, int time, double heat) {
		this.input = input;
		this.time = time;
		this.heat = heat;
		this.recipeName = input.getLocalizedName();
	}

	@Override
	public boolean equals(Object object) {
		if(object instanceof FluidStack) {
			return this.input.isFluidEqual((FluidStack)object);
		} else if(object instanceof BoilerFuelRecipe) {
			return this == object;
		} else return false;
	}

	public static BoilerFuelRecipe addFuel(FluidStack input, int time, double heat) {
		BoilerFuelRecipe recipe = new BoilerFuelRecipe(input, time, heat);
		recipeList.add(recipe);
		return recipe;
	}

	public static BoilerFuelRecipe findFuel(FluidStack input) {
		if(input == null) return null;
		return ITUtils.First(recipeList, input);
	}

}