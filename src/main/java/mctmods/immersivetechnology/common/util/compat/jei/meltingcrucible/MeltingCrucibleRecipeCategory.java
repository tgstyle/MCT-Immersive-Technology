package mctmods.immersivetechnology.common.util.compat.jei.meltingcrucible;

import mctmods.immersivetechnology.api.crafting.MeltingCrucibleRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Machines.Multiblock;
import mctmods.immersivetechnology.common.util.compat.jei.GenericMultiblockIngredient;
import mctmods.immersivetechnology.common.util.compat.jei.ITRecipeCategory;
import mctmods.immersivetechnology.common.util.compat.jei.JEIHelper;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.*;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class MeltingCrucibleRecipeCategory extends ITRecipeCategory<MeltingCrucibleRecipe, MeltingCrucibleRecipeWrapper> {
	public static ResourceLocation background = new ResourceLocation("immersivetech:textures/gui/gui_melting_crucible_jei.png");
	private final IDrawable tankOverlay;
	private final IDrawableAnimated arrow;

	@SuppressWarnings("deprecation")
	public MeltingCrucibleRecipeCategory(IGuiHelper helper) {
		super("meltingCrucible", "tile.immersivetech.metal_multiblock1.melting_crucible.name", helper.createDrawable(background, 0, 0, 176, 64), MeltingCrucibleRecipe.class, Multiblock.enable_meltingCrucible ? GenericMultiblockIngredient.MELTING_CRUCIBLE :  null, Multiblock.enable_solarMelter ? GenericMultiblockIngredient.SOLAR_MELTER : null);
		tankOverlay = helper.createDrawable(background, 178, 2, 16, 47, -2, 2, -2, 2);
		IDrawableStatic staticImage = helper.createDrawable(background, 196, 0, 32, 18);
		this.arrow = helper.createAnimatedDrawable(staticImage, 200, IDrawableAnimated.StartDirection.LEFT, false);
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, MeltingCrucibleRecipeWrapper recipeWrapper, IIngredients ingredients) {
		List<List<FluidStack>> outputs = ingredients.getOutputs(VanillaTypes.FLUID);

		int tankSize = 0;

		for(List<FluidStack> lists : outputs) {
			for(FluidStack fluid : lists) if(fluid.amount > tankSize) tankSize = fluid.amount;
		}

		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		guiItemStacks.init(0, true, 11, 41);
		guiItemStacks.set(0, recipeWrapper.recipe.itemInput.getExampleStack());

		IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();
		if(!outputs.isEmpty()) {
			guiFluidStacks.init(0, false, 149, 12, 16, 47, tankSize, true, tankOverlay);
			guiFluidStacks.set(0, outputs.get(0));
		}
		guiFluidStacks.addTooltipCallback(JEIHelper.fluidTooltipCallback);
	}
	
	@Override
	public IRecipeWrapper getRecipeWrapper(MeltingCrucibleRecipe recipe) { return new MeltingCrucibleRecipeWrapper(recipe); }

	@Override
	public void drawExtras(Minecraft minecraft) {
		arrow.draw(minecraft, 57, 39);
	}

}