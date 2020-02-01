package ferro2000.immersivetech.common.util.compat.jei.steamturbine;

import java.util.List;

import ferro2000.immersivetech.api.crafting.SteamTurbineRecipe;
import ferro2000.immersivetech.common.Config.ITConfig.Machines.SteamTurbine;
import ferro2000.immersivetech.common.ITContent;
import ferro2000.immersivetech.common.blocks.metal.types.BlockType_MetalMultiblock;
import ferro2000.immersivetech.common.util.compat.jei.ITRecipeCategory;
import ferro2000.immersivetech.common.util.compat.jei.JEIHelper;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.*;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fluids.FluidStack;

public class SteamTurbineRecipeCategory extends ITRecipeCategory<SteamTurbineRecipe, SteamTurbineRecipeWrapper> {
	public static ResourceLocation background = new ResourceLocation("immersivetech:textures/gui/gui_steam_turbine.png");
	private final IDrawable tankOverlay;
	private final IDrawableAnimated turbineAndArrow;

	private static int inputTankSize = SteamTurbine.steamTurbine_input_tankSize;
	private static int outputTankSize = SteamTurbine.steamTurbine_input_tankSize;

	@SuppressWarnings("deprecation")
	public SteamTurbineRecipeCategory(IGuiHelper helper) {
		super("steamTurbine", "tile.immersivetech.metal_multiblock.steam_turbine.name", helper.createDrawable(background, 0, 0, 96, 78), SteamTurbineRecipe.class, new ItemStack(ITContent.blockMetalMultiblock, 1, BlockType_MetalMultiblock.STEAM_TURBINE.getMeta()));
		tankOverlay = helper.createDrawable(background, 98, 2, 16, 47, -2, 2, -2, 2);
		IDrawableStatic staticImage = helper.createDrawable(background, 0, 78, 32, 42);
		this.turbineAndArrow = helper.createAnimatedDrawable(staticImage, 200, IDrawableAnimated.StartDirection.LEFT, false);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void setRecipe(IRecipeLayout recipeLayout, SteamTurbineRecipeWrapper recipeWrapper, IIngredients ingredients) {
		List<List<FluidStack>> inputs = ingredients.getInputs(FluidStack.class);
		List<List<FluidStack>> outputs = ingredients.getOutputs(FluidStack.class);
		IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();
		guiFluidStacks.init(0, true, 11, 11, 16, 47, inputTankSize, true, tankOverlay);
		guiFluidStacks.set(0, inputs.get(0));
		if(outputs.get(0) != null) {
			guiFluidStacks.init(1, false, 69, 11, 16, 47, outputTankSize, true, tankOverlay);
			guiFluidStacks.set(1, outputs.get(0));
		}
		guiFluidStacks.addTooltipCallback(JEIHelper.fluidTooltipCallback);
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(SteamTurbineRecipe recipe) {
		return new SteamTurbineRecipeWrapper(recipe);
	}
	
	@Override
	public void drawExtras(Minecraft minecraft) {
		turbineAndArrow.draw(minecraft, 32, 18);
	}

}