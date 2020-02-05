package ferro2000.immersivetech.common.util.compat.jei.boiler;

import java.util.List;

import ferro2000.immersivetech.api.crafting.BoilerRecipe.BoilerFuelRecipe;
import ferro2000.immersivetech.common.Config.ITConfig.Machines.Boiler;
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

public class BoilerFuelRecipeCategory extends ITRecipeCategory<BoilerFuelRecipe, BoilerFuelRecipeWrapper> {
	public static ResourceLocation background = new ResourceLocation("immersivetech:textures/gui/gui_boiler_jei.png");
	private final IDrawable tankOverlay;

	private static int inputTankSize = Boiler.boiler_fuel_tankSize;

	@SuppressWarnings("deprecation")
	public BoilerFuelRecipeCategory(IGuiHelper helper) {
		super("boilerFuel", "tile.immersivetech.metal_multiblock.boiler.name", helper.createDrawable(background, 0, 0, 176, 77), BoilerFuelRecipe.class, new ItemStack(ITContent.blockMetalMultiblock, 1, BlockType_MetalMultiblock.BOILER.getMeta()));
		tankOverlay = helper.createDrawable(background, 177, 31, 16, 47, -2, 2, -2, 2);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void setRecipe(IRecipeLayout recipeLayout, BoilerFuelRecipeWrapper recipeWrapper, IIngredients ingredients) {
		int tankSize = 0;
		List<List<FluidStack>> inputs = ingredients.getInputs(FluidStack.class);
		for (List<FluidStack> lists : inputs) {
			for (FluidStack fluid : lists) if (fluid.amount > tankSize) tankSize = fluid.amount;
		}
		IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();
		guiFluidStacks.init(0, true, 13, 20, 16, 47, tankSize, false, tankOverlay);
		guiFluidStacks.set(0, inputs.get(0));
		guiFluidStacks.addTooltipCallback(JEIHelper.fluidTooltipCallback);
	}
	
	@Override
	public IRecipeWrapper getRecipeWrapper(BoilerFuelRecipe recipe) {
		return new BoilerFuelRecipeWrapper(recipe);
	}

}