package ferro2000.immersivetech.api.crafting;

import ferro2000.immersivetech.api.ITUtils;

import net.minecraftforge.fluids.FluidStack;

public class BoilerRecipe extends GenericRecipe {
	public static RecipeList <BoilerRecipe> recipeList = new RecipeList <> ();
	public static String recipeCategoryName = "Boiler";
	public final FluidStack output;
	public final FluidStack input;
	public final int time;

	public BoilerRecipe(FluidStack output, FluidStack input, int time) {
		this.output = output;
		this.input = input;
		this.time = time;
		this.recipeName = input.getLocalizedName();
	}

	@Override
	public boolean equals(Object object) {
		if(object instanceof FluidStack) {
			return this.input.isFluidEqual((FluidStack)object);
		} else if(object instanceof BoilerRecipe) {
			return this == object;
		} else return false;
	}

	public static BoilerRecipe addRecipe(FluidStack output, FluidStack input, int time) {
		BoilerRecipe recipe = new BoilerRecipe(output, input, time);
		recipeList.add(recipe);
		return recipe;
	}

	public static BoilerRecipe findRecipe(FluidStack input) {
		if(input == null) return null;
		return ITUtils.First(recipeList, input);
	}

}