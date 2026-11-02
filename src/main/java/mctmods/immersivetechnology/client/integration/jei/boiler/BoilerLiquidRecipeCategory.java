package mctmods.immersivetechnology.client.integration.jei.boiler;

import mctmods.immersivetechnology.client.integration.jei.ITMultiblockIngredients;
import mctmods.immersivetechnology.client.integration.jei.ITRecipeCategory;
import mctmods.immersivetechnology.client.integration.jei.JEIHelper;
import mctmods.immersivetechnology.common.recipes.BoilerLiquidRecipe;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import java.util.List;
import javax.annotation.Nonnull;

public class BoilerLiquidRecipeCategory extends ITRecipeCategory<BoilerLiquidRecipe, BoilerLiquidRecipeWrapper> {
	public static ResourceLocation background = new ResourceLocation("immersivetech:textures/gui/boiler_liquid.png");
	private final IDrawable tankOverlay;

	public BoilerLiquidRecipeCategory(IGuiHelper helper) {
		super("boilerLiquid", "tile.immersivetech.metal_multiblock1.boiler_liquid.name", helper.createDrawable(background, 0, 0, 176, 77), BoilerLiquidRecipe.class, ITMultiblockIngredients.BOILER_LIQUID);
		tankOverlay = helper.drawableBuilder(background, 177, 31, 16, 47).addPadding(-2, 2, -2, 2).build();
	}

	@Override public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull BoilerLiquidRecipeWrapper recipeWrapper, @Nonnull IIngredients ingredients) {
		List<List<FluidStack>> inputs = ingredients.getInputs(VanillaTypes.FLUID);

		int tankSize = getMaxFluidAmount(inputs);

		IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();
		guiFluidStacks.init(0, true, 80, 20, 16, 47, tankSize, false, tankOverlay);
		guiFluidStacks.set(0, inputs.get(0));
		guiFluidStacks.addTooltipCallback(JEIHelper.fluidTooltipCallback);
	}

	@Override @Nonnull public IRecipeWrapper getRecipeWrapper(@Nonnull BoilerLiquidRecipe recipe) { return new BoilerLiquidRecipeWrapper(recipe); }
}
