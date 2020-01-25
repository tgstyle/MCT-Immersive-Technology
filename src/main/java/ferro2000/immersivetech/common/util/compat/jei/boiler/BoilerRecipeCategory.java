package ferro2000.immersivetech.common.util.compat.jei.boiler;

import java.util.List;

import ferro2000.immersivetech.api.crafting.BoilerRecipe;
import ferro2000.immersivetech.common.Config;
import ferro2000.immersivetech.common.ITContent;
import ferro2000.immersivetech.common.blocks.metal.types.BlockType_MetalMultiblock;
import ferro2000.immersivetech.common.util.compat.jei.ITRecipeCategory;
import ferro2000.immersivetech.common.util.compat.jei.JEIHelper;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.*;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fluids.FluidStack;

public class BoilerRecipeCategory extends ITRecipeCategory<BoilerRecipe, BoilerRecipeWrapper> {
	public static ResourceLocation background = new ResourceLocation("immersivetech:textures/gui/gui_boiler_jei.png");
	private final IDrawable tankOverlay;

	private static int inputTankSize = Config.ITConfig.Machines.boiler_input_tankSize;
	private static int outputTankSize = Config.ITConfig.Machines.boiler_output_tankSize;

	@SuppressWarnings("deprecation")
	public BoilerRecipeCategory(IGuiHelper helper) {
		super("boiler", "tile.immersivetech.metal_multiblock.boiler.name", helper.createDrawable(background, 0, 77, 176, 77), BoilerRecipe.class, new ItemStack(ITContent.blockMetalMultiblock, 1, BlockType_MetalMultiblock.BOILER.getMeta()));
		tankOverlay = helper.createDrawable(background, 177, 31, 16, 47, -2, 2, -2, 2);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void setRecipe(IRecipeLayout recipeLayout, BoilerRecipeWrapper recipeWrapper, IIngredients ingredients) {
		List<List<FluidStack>> inputs = ingredients.getInputs(FluidStack.class);
		List<List<FluidStack>> outputs = ingredients.getOutputs(FluidStack.class);
		IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();
		if(inputs.size () > 0) {
			guiFluidStacks.init(0, true, 100, 20, 16, 47, inputTankSize, false, tankOverlay);
			guiFluidStacks.set(0, inputs.get(0));
		}
		guiFluidStacks.init(1, false, 125, 20, 16, 47, outputTankSize, false, tankOverlay);
		guiFluidStacks.set(1, outputs.get(0));

		guiFluidStacks.addTooltipCallback(JEIHelper.fluidTooltipCallback);
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(BoilerRecipe recipe) {
		return new BoilerRecipeWrapper(recipe);
	}

}