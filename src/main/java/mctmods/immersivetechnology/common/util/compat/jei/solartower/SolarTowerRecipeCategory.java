package mctmods.immersivetechnology.common.util.compat.jei.solartower;

import mctmods.immersivetechnology.api.crafting.SolarTowerRecipe;
import mctmods.immersivetechnology.common.util.compat.jei.GenericMultiblockIngredient;
import mctmods.immersivetechnology.common.util.compat.jei.ITRecipeCategory;
import mctmods.immersivetechnology.common.util.compat.jei.JEIHelper;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.List;

public class SolarTowerRecipeCategory extends ITRecipeCategory<SolarTowerRecipe, SolarTowerRecipeWrapper> {
	public static ResourceLocation background = new ResourceLocation("immersivetech:textures/gui/gui_solar_tower_jei.png");
	private final IDrawable tankOverlay;
	private final IDrawable reflectorOverlay;
	private final ITickTimer timer;

	public SolarTowerRecipeCategory(IGuiHelper helper) {
		super("solarTower", "tile.immersivetech.metal_multiblock.solar_tower.name", helper.createDrawable(background, 0, 0, 176, 77), SolarTowerRecipe.class, GenericMultiblockIngredient.SOLAR_TOWER);
		tankOverlay = helper.drawableBuilder(background, 177, 31, 16, 47).addPadding(-2, 2, -2, 2).build();
		reflectorOverlay = helper.drawableBuilder(background, 198, 31, 10, 10).addPadding(0,0,0,0).build();
		timer = helper.createTickTimer(200, 3, false);
	}

	@Override public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull SolarTowerRecipeWrapper recipeWrapper, @Nonnull IIngredients ingredients) {
		List<List<FluidStack>> inputs = ingredients.getInputs(VanillaTypes.FLUID);
		List<List<FluidStack>> outputs = ingredients.getOutputs(VanillaTypes.FLUID);

		int tankCapacity = 0;
		for (List<FluidStack> stacks : inputs) {
			for (FluidStack stack : stacks) {
				if (stack.amount > tankCapacity) tankCapacity = stack.amount;
			}
		}
		for (List<FluidStack> stacks : outputs) {
			for (FluidStack stack : stacks) {
				if (stack.amount > tankCapacity) tankCapacity = stack.amount;
			}
		}

		IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();
		guiFluidStacks.init(0, true, 102, 21, 16, 47, tankCapacity, false, tankOverlay);
		guiFluidStacks.set(0, inputs.get(0));
		guiFluidStacks.init(1, false, 126, 21, 16, 47, tankCapacity, false, tankOverlay);
		guiFluidStacks.set(1, outputs.get(0));
		guiFluidStacks.addTooltipCallback(JEIHelper.fluidTooltipCallback);
		recipeWrapper.timer = timer;
	}

	@SideOnly(Side.CLIENT)
	@Override public void drawExtras(@Nonnull Minecraft minecraft) {
		int reflectors = timer.getValue();
		reflectorOverlay.draw(minecraft, 32, 24);
		if (reflectors >= 1) reflectorOverlay.draw(minecraft, 16, 40);
		if (reflectors >= 2) reflectorOverlay.draw(minecraft, 48, 40);
		if (reflectors == 3) reflectorOverlay.draw(minecraft, 32, 56);
	}

	@Override @Nonnull public IRecipeWrapper getRecipeWrapper(@Nonnull SolarTowerRecipe recipe) { return new SolarTowerRecipeWrapper(recipe); }
}
