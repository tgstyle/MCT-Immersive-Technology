package mctmods.immersivetechnology.common.util.compat.jei.distiller;

import mctmods.immersivetechnology.api.crafting.DistillerRecipe;
import mctmods.immersivetechnology.common.util.TranslationKey;
import mctmods.immersivetechnology.common.util.compat.jei.GenericMultiblockIngredient;
import mctmods.immersivetechnology.common.util.compat.jei.ITRecipeCategory;
import mctmods.immersivetechnology.common.util.compat.jei.JEIHelper;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.List;

public class DistillerRecipeCategory extends ITRecipeCategory<DistillerRecipe, DistillerRecipeWrapper> {
	public static ResourceLocation background = new ResourceLocation("immersivetech:textures/gui/gui_distiller.png");
	private final IDrawable tankOverlay;

	public DistillerRecipeCategory(IGuiHelper helper) {
		super("distiller", "tile.immersivetech.metal_multiblock.distiller.name", helper.createDrawable(background, 8, 13, 168, 60), DistillerRecipe.class, GenericMultiblockIngredient.DISTILLER);
		tankOverlay = helper.drawableBuilder(background, 177, 31, 16, 47).addPadding(-2, 2, -2, 2).build();
	}

	@Override public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull DistillerRecipeWrapper recipeWrapper, @Nonnull IIngredients ingredients) {
		List<List<FluidStack>> fluidInputs = ingredients.getInputs(VanillaTypes.FLUID);
		List<List<FluidStack>> fluidOutputs = ingredients.getOutputs(VanillaTypes.FLUID);

		int tankCapacity = getMaxFluidAmount(fluidInputs, fluidOutputs);

		IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();
		guiFluidStacks.init(0, true, 50, 8, 16, 47, tankCapacity, false, tankOverlay);
		if (!fluidInputs.isEmpty()) guiFluidStacks.set(0, fluidInputs.get(0));

		guiFluidStacks.init(1, false, 104, 8, 16, 47, tankCapacity, false, tankOverlay);
		guiFluidStacks.set(1, fluidOutputs.get(0));
		guiFluidStacks.addTooltipCallback(JEIHelper.fluidTooltipCallback);

		ItemStack itemOutput = recipeWrapper.recipe.itemOutput;
		if (!itemOutput.isEmpty()) {
			IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
			guiItemStacks.init(0, false, 71, 21);
			guiItemStacks.set(0, itemOutput);
			guiItemStacks.addTooltipCallback((slotIndex, input, ingredient, tooltip) -> tooltip.add(TranslationKey.CATEGORY_DISTILLER_CHANCE.format(recipeWrapper.recipe.chance * 100) + "%"));
		}
	}

	@Override @Nonnull public IRecipeWrapper getRecipeWrapper(@Nonnull DistillerRecipe recipe) { return new DistillerRecipeWrapper(recipe); }
}
