package mctmods.immersivetechnology.common.util.compat.jei.radiator;

import mctmods.immersivetechnology.api.crafting.RadiatorRecipe;
import mctmods.immersivetechnology.common.util.compat.jei.ITMultiblockIngredients;
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

import javax.annotation.Nonnull;
import java.util.List;

public class RadiatorRecipeCategory extends ITRecipeCategory<RadiatorRecipe, RadiatorRecipeWrapper> {
	public static ResourceLocation background = new ResourceLocation("immersivetech:textures/gui/radiator_jei.png");
	private final IDrawable tankOverlay;
	private final IDrawableAnimated arrow;
	private final IDrawableAnimated drops;

	public RadiatorRecipeCategory(IGuiHelper helper) {
		super("radiator", "tile.immersivetech.metal_multiblock1.radiator.name", helper.createDrawable(background, 0, 0, 159, 69), RadiatorRecipe.class, ITMultiblockIngredients.RADIATOR);
		tankOverlay = helper.drawableBuilder(background, 161, 2, 16, 47).addPadding(-2, 2, -2, 2).build();
		IDrawableStatic staticImage = helper.createDrawable(background, 17, 69, 32, 9);
		this.arrow = helper.createAnimatedDrawable(staticImage, 200, IDrawableAnimated.StartDirection.LEFT, false);
		staticImage = helper.createDrawable(background, 0, 69, 17, 23);
		this.drops = helper.createAnimatedDrawable(staticImage, 200, IDrawableAnimated.StartDirection.TOP, false);
	}

	@Override public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull RadiatorRecipeWrapper recipeWrapper, @Nonnull IIngredients ingredients) {
		List<List<FluidStack>> inputs = ingredients.getInputs(VanillaTypes.FLUID);
		List<List<FluidStack>> outputs = ingredients.getOutputs(VanillaTypes.FLUID);

		int tankCapacity = getMaxFluidAmount(inputs, outputs);

		IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();
		guiFluidStacks.init(0, true, 11, 11, 16, 47, tankCapacity, false, tankOverlay);
		guiFluidStacks.set(0, inputs.get(0));
		guiFluidStacks.init(1, false, 109, 11, 16, 47, tankCapacity, false, tankOverlay);
		guiFluidStacks.set(1, outputs.get(0));
		guiFluidStacks.addTooltipCallback(JEIHelper.fluidTooltipCallback);
	}

	@Override @Nonnull public IRecipeWrapper getRecipeWrapper(@Nonnull RadiatorRecipe recipe) { return new RadiatorRecipeWrapper(recipe); }

	@Override public void drawExtras(@Nonnull Minecraft minecraft) {
		arrow.draw(minecraft, 52, 51);
		drops.draw(minecraft, 55, 32);
	}
}
