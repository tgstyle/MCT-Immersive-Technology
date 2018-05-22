package ferro2000.immersivetech.common.integration.jei.solartower;

import java.util.List;

import ferro2000.immersivetech.api.craftings.SolarTowerRecipes;
import ferro2000.immersivetech.common.ITContent;
import ferro2000.immersivetech.common.blocks.metal.types.BlockType_MetalMultiblock;
import ferro2000.immersivetech.common.integration.jei.ITRecipeCategory;
import ferro2000.immersivetech.common.integration.jei.JEIHelper;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public class SolarTowerRecipeCategory extends ITRecipeCategory<SolarTowerRecipes, SolarTowerRecipeWrapper> {

	public static ResourceLocation background = new ResourceLocation("immersivetech:textures/gui/gui_solar_tower.png");
	private final IDrawable tankOverlay;
	
	public SolarTowerRecipeCategory(IGuiHelper helper) {
		super("solarTower","tile.immersivetech.metal_multiblock.solar_tower.name", helper.createDrawable(background, 74,12, 96,59), SolarTowerRecipes.class, new ItemStack(ITContent.blockMetalMultiblock,1,BlockType_MetalMultiblock.SOLAR_TOWER.getMeta()));
		tankOverlay = helper.createDrawable(background, 177,31, 16,47, -2,2,-2,2);
	}


	@Override
	public IRecipeWrapper getRecipeWrapper(SolarTowerRecipes recipe) {
		return new SolarTowerRecipeWrapper(recipe);
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, SolarTowerRecipeWrapper recipeWrapper, IIngredients ingredients) {
		List<List<FluidStack>> inputs = ingredients.getInputs(FluidStack.class);
		IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();
		if(inputs.size()>0)
		{
			guiFluidStacks.init(0, true, 28,9, 16,47, 6000, false, tankOverlay);
			guiFluidStacks.set(0, inputs.get(0));
		}
		guiFluidStacks.init(1, false, 52,9, 16,47, 6000, false, tankOverlay);
		guiFluidStacks.set(1, ingredients.getOutputs(FluidStack.class).get(0));

		guiFluidStacks.addTooltipCallback(JEIHelper.fluidTooltipCallback);
	}

}
