package mctmods.immersivetechnology.common.util.compat.jei.steamturbine;

import mctmods.immersivetechnology.api.crafting.SteamTurbineRecipe;
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

public class SteamTurbineRecipeCategory extends ITRecipeCategory<SteamTurbineRecipe, SteamTurbineRecipeWrapper> {
	public static ResourceLocation background = new ResourceLocation("immersivetech:textures/gui/turbine_jei.png");
	private final IDrawable tankOverlay;
	private final IDrawableAnimated turbineAndArrow;

	public SteamTurbineRecipeCategory(IGuiHelper helper) {
		super("steamTurbine", "tile.immersivetech.metal_multiblock.steam_turbine.name", helper.createDrawable(background, 0, 0, 116, 69), SteamTurbineRecipe.class, ITMultiblockIngredients.STEAM_TURBINE);
		tankOverlay = helper.drawableBuilder(background, 118, 2, 16, 47).addPadding(-2, 2, -2, 2).build();
		IDrawableStatic staticImage = helper.createDrawable(background, 0, 78, 32, 42);
		this.turbineAndArrow = helper.createAnimatedDrawable(staticImage, 200, IDrawableAnimated.StartDirection.LEFT, false);
	}

	@Override public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull SteamTurbineRecipeWrapper recipeWrapper, @Nonnull IIngredients ingredients) {
		List<List<FluidStack>> inputs = ingredients.getInputs(VanillaTypes.FLUID);
		List<List<FluidStack>> outputs = ingredients.getOutputs(VanillaTypes.FLUID);

		int tankSize = getMaxFluidAmount(inputs, outputs);

		IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();
		guiFluidStacks.init(0, true, 11, 11, 16, 47, tankSize, true, tankOverlay);
		guiFluidStacks.set(0, inputs.get(0));
		if(!outputs.isEmpty()) {
			guiFluidStacks.init(1, false, 89, 11, 16, 47, tankSize, true, tankOverlay);
			guiFluidStacks.set(1, outputs.get(0));
		}
		guiFluidStacks.addTooltipCallback(JEIHelper.fluidTooltipCallback);
	}

	@Override @Nonnull public IRecipeWrapper getRecipeWrapper(@Nonnull SteamTurbineRecipe recipe) { return new SteamTurbineRecipeWrapper(recipe); }

	@Override public void drawExtras(@Nonnull Minecraft minecraft) { turbineAndArrow.draw(minecraft, 42, 18); }
}
