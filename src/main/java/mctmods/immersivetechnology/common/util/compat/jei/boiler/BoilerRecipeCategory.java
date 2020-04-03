package mctmods.immersivetechnology.common.util.compat.jei.boiler;

import mctmods.immersivetechnology.api.crafting.BoilerRecipe;
import mctmods.immersivetechnology.common.util.compat.jei.GenericMultiblockIngredient;
import mctmods.immersivetechnology.common.util.compat.jei.ITRecipeCategory;
import mctmods.immersivetechnology.common.util.compat.jei.JEIHelper;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class BoilerRecipeCategory extends ITRecipeCategory<BoilerRecipe, BoilerRecipeWrapper> {
	public static ResourceLocation background = new ResourceLocation("immersivetech:textures/gui/gui_boiler_jei.png");
	private final IDrawable tankOverlay;

	@SuppressWarnings("deprecation")
	public BoilerRecipeCategory(IGuiHelper helper) {
		super("boiler", "tile.immersivetech.metal_multiblock.boiler.name", helper.createDrawable(background, 0, 77, 176, 77), BoilerRecipe.class, GenericMultiblockIngredient.BOILER);
		tankOverlay = helper.createDrawable(background, 177, 31, 16, 47, -2, 2, -2, 2);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void setRecipe(IRecipeLayout recipeLayout, BoilerRecipeWrapper recipeWrapper, IIngredients ingredients) {
		List<List<FluidStack>> inputs = ingredients.getInputs(FluidStack.class);
		List<List<FluidStack>> outputs = ingredients.getOutputs(FluidStack.class);

		int tankSize = 0;
		for(List<FluidStack> lists : inputs) {
			for(FluidStack fluid : lists) if(fluid.amount > tankSize) tankSize = fluid.amount;
		}
		for(List<FluidStack> lists : outputs) {
			for(FluidStack fluid : lists) if(fluid.amount > tankSize) tankSize = fluid.amount;
		}

		IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();
		if(inputs.size () > 0) {
			guiFluidStacks.init(0, true, 100, 20, 16, 47, tankSize, false, tankOverlay);
			guiFluidStacks.set(0, inputs.get(0));
		}
		guiFluidStacks.init(1, false, 125, 20, 16, 47, tankSize, false, tankOverlay);
		guiFluidStacks.set(1, outputs.get(0));

		guiFluidStacks.addTooltipCallback(JEIHelper.fluidTooltipCallback);
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(BoilerRecipe recipe) {
		return new BoilerRecipeWrapper(recipe);
	}

}