package mctmods.immersivetechnology.common.util.compat.jei.boiler;

import mctmods.immersivetechnology.api.crafting.BoilerLiquidRecipe;
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

public class BoilerLiquidRecipeCategory extends ITRecipeCategory<BoilerLiquidRecipe, BoilerLiquidRecipeWrapper> {
	public static ResourceLocation background = new ResourceLocation("immersivetech:textures/gui/gui_boiler_liquid.png");
	private final IDrawable tankOverlay;

	public BoilerLiquidRecipeCategory(IGuiHelper helper) {
		super("boilerLiquid", "tile.immersivetech.metal_multiblock1.boiler_liquid.name", helper.createDrawable(background, 0, 0, 176, 77), BoilerLiquidRecipe.class, GenericMultiblockIngredient.BOILER_LIQUID);
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

	@Override @Nonnull public IRecipeWrapper getRecipeWrapper(@Nonnull BoilerLiquidRecipe recipe) {
		return new BoilerLiquidRecipeWrapper(recipe);
	}
}
