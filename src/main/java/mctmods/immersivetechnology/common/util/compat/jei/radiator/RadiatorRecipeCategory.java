package mctmods.immersivetechnology.common.util.compat.jei.radiator;

import mctmods.immersivetechnology.api.crafting.RadiatorRecipe;
import mctmods.immersivetechnology.common.util.compat.jei.GenericMultiblockIngredient;
import mctmods.immersivetechnology.common.util.compat.jei.ITRecipeCategory;
import mctmods.immersivetechnology.common.util.compat.jei.JEIHelper;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.*;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class RadiatorRecipeCategory extends ITRecipeCategory<RadiatorRecipe, RadiatorRecipeWrapper> {
	public static ResourceLocation background = new ResourceLocation("immersivetech:textures/gui/gui_radiator_jei.png");
	private final IDrawable tankOverlay;
	private final IDrawableAnimated arrow;
	private final IDrawableAnimated drops;

	@SuppressWarnings("deprecation")
	public RadiatorRecipeCategory(IGuiHelper helper) {
		super("radiator", "tile.immersivetech.metal_multiblock1.radiator.name", helper.createDrawable(background, 0, 0, 159, 69), RadiatorRecipe.class, GenericMultiblockIngredient.RADIATOR);
		tankOverlay = helper.createDrawable(background, 161, 2, 16, 47, -2, 2, -2, 2);
		IDrawableStatic staticImage = helper.createDrawable(background, 17, 69, 32, 9);
		this.arrow = helper.createAnimatedDrawable(staticImage, 200, IDrawableAnimated.StartDirection.LEFT, false);
		staticImage = helper.createDrawable(background, 0, 69, 17, 23);
		this.drops = helper.createAnimatedDrawable(staticImage, 200, IDrawableAnimated.StartDirection.TOP, false);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void setRecipe(IRecipeLayout recipeLayout, RadiatorRecipeWrapper recipeWrapper, IIngredients ingredients) {
		List<List<FluidStack>> inputs = ingredients.getInputs(FluidStack.class);
		int tankCapacity = 0;
		for(List<FluidStack> stacks : inputs) {
			for(FluidStack stack : stacks) {
				if(stack.amount > tankCapacity) tankCapacity = stack.amount;
			}
		}
		List<List<FluidStack>> outputs = ingredients.getOutputs(FluidStack.class);
		for(List<FluidStack> stacks : outputs) {
			for(FluidStack stack : stacks) {
				if(stack.amount > tankCapacity) tankCapacity = stack.amount;
			}
		}

		IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();
		if(inputs.size() > 0) {
			guiFluidStacks.init(0, true, 11, 11, 16, 47, tankCapacity, false, tankOverlay);
			guiFluidStacks.set(0, inputs.get(0));
		}
		guiFluidStacks.init(1, false, 109, 11, 16, 47, tankCapacity, false, tankOverlay);
		guiFluidStacks.set(1, outputs.get(0));
		guiFluidStacks.addTooltipCallback(JEIHelper.fluidTooltipCallback);
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(RadiatorRecipe recipe) {
		return new RadiatorRecipeWrapper(recipe);
	}

	@Override
	public void drawExtras(Minecraft minecraft) {
		arrow.draw(minecraft, 52, 51);
		drops.draw(minecraft, 55, 32);
	}
}