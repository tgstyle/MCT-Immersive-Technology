package mctmods.immersivetechnology.common.util.compat.jei.boiler;

import mctmods.immersivetechnology.api.crafting.BoilerTankRecipe;
import mctmods.immersivetechnology.common.util.compat.jei.GenericMultiblockIngredient;
import mctmods.immersivetechnology.common.util.compat.jei.ITRecipeCategory;
import mctmods.immersivetechnology.common.util.compat.jei.JEIHelper;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.List;

public class BoilerTankRecipeCategory extends ITRecipeCategory<BoilerTankRecipe, BoilerTankRecipeWrapper> {
	public static ResourceLocation background = new ResourceLocation("immersivetech:textures/gui/boiler_tank.png");
	private final IDrawable tankOverlay;

	public BoilerTankRecipeCategory(IGuiHelper helper) {
		super("boilerTank", "tile.immersivetech.metal_multiblock.boiler_tank.name", helper.createDrawable(background, 0, 0, 176, 77), BoilerTankRecipe.class, GenericMultiblockIngredient.BOILER_TANK);
		tankOverlay = helper.drawableBuilder(background, 177, 31, 16, 47).addPadding(-2, 2, -2, 2).build();
	}

	@Override public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull BoilerTankRecipeWrapper recipeWrapper, @Nonnull IIngredients ingredients) {
		List<List<FluidStack>> inputs = ingredients.getInputs(VanillaTypes.FLUID);
		List<List<FluidStack>> outputs = ingredients.getOutputs(VanillaTypes.FLUID);

		int tankSize = getMaxFluidAmount(inputs, outputs);

		IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();
		guiFluidStacks.init(0, true, 67, 20, 16, 47, tankSize, false, tankOverlay);
		guiFluidStacks.set(0, inputs.get(0));
		guiFluidStacks.init(1, false, 92, 20, 16, 47, tankSize, false, tankOverlay);
		guiFluidStacks.set(1, outputs.get(0));

		guiFluidStacks.addTooltipCallback(JEIHelper.fluidTooltipCallback);
	}

	@Override @Nonnull public IRecipeWrapper getRecipeWrapper(@Nonnull BoilerTankRecipe recipe) { return new BoilerTankRecipeWrapper(recipe); }
}
