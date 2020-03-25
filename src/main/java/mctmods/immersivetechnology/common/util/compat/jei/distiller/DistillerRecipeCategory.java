package mctmods.immersivetechnology.common.util.compat.jei.distiller;

import mctmods.immersivetechnology.api.crafting.DistillerRecipe;
import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.blocks.metal.types.BlockType_MetalMultiblock;
import mctmods.immersivetechnology.common.util.compat.jei.ITRecipeCategory;
import mctmods.immersivetechnology.common.util.compat.jei.JEIHelper;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class DistillerRecipeCategory extends ITRecipeCategory<DistillerRecipe, DistillerRecipeWrapper> {
	public static ResourceLocation background = new ResourceLocation("immersivetech:textures/gui/gui_distiller.png");
	private final IDrawable tankOverlay;

	@SuppressWarnings("deprecation")
	public DistillerRecipeCategory(IGuiHelper helper) {
		super("distiller", "tile.immersivetech.metal_multiblock.distiller.name", helper.createDrawable(background, 0, 166, 176, 77), DistillerRecipe.class, new ItemStack(ITContent.blockMetalMultiblock, 1, BlockType_MetalMultiblock.DISTILLER.getMeta()));
		tankOverlay = helper.createDrawable(background, 177, 31, 16, 47, -2, 2, -2, 2);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void setRecipe(IRecipeLayout recipeLayout, DistillerRecipeWrapper recipeWrapper, IIngredients ingredients) {
		List<List<FluidStack>> inputs = ingredients.getInputs(FluidStack.class);
		List<List<FluidStack>> outputs = ingredients.getOutputs(FluidStack.class);

		int tankSize = 0;
		for(List<FluidStack> lists : inputs) {
			for(FluidStack fluid : lists) if(fluid.amount > tankSize) tankSize = fluid.amount;
		}
		for(List<FluidStack> lists : outputs) {
			for(FluidStack fluid : lists) if(fluid.amount > tankSize) tankSize = fluid.amount;
		}
		IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();
		if(inputs.size() > 0) {
			guiFluidStacks.init(0, true, 58, 21, 16, 47, tankSize, false, tankOverlay);
			guiFluidStacks.set(0, inputs.get(0));
		}
		guiFluidStacks.init(1, false, 112, 21, 16, 47, tankSize, false, tankOverlay);
		guiFluidStacks.set(1, outputs.get(0));
		guiFluidStacks.addTooltipCallback(JEIHelper.fluidTooltipCallback);
	}
	
	@Override
	public IRecipeWrapper getRecipeWrapper(DistillerRecipe recipe) {
		return new DistillerRecipeWrapper(recipe);
	}

}