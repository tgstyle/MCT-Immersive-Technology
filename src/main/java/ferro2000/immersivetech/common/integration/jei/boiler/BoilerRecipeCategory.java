package ferro2000.immersivetech.common.integration.jei.boiler;

import java.util.Collections;

import ferro2000.immersivetech.api.crafting.BoilerRecipe;
import ferro2000.immersivetech.common.Config;
import ferro2000.immersivetech.common.ITContent;
import ferro2000.immersivetech.common.blocks.metal.types.BlockType_MetalMultiblock;
import ferro2000.immersivetech.common.integration.jei.GenericCategory;
import ferro2000.immersivetech.common.integration.jei.JEIHelper;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.gui.*;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

public class BoilerRecipeCategory extends GenericCategory<BoilerRecipe, BoilerRecipeWrapper> {

	private final IDrawable tankOverlay;
	public static BoilerRecipeCategory instance;
	public static String categoryName = "it.boiler";

	public static void registerCategory(IGuiHelper guiHelper, IRecipeCategoryRegistration registry) {
		if (instance != null) return;
		instance = new BoilerRecipeCategory(guiHelper);
		registry.addRecipeCategories(instance);
	}

	public static void registerRest(IModRegistry modRegistry) {
		if (instance == null) return;
		modRegistry.addRecipes(Collections.unmodifiableList(BoilerRecipe.recipeList), categoryName);
		modRegistry.addRecipeCatalyst(new ItemStack(ITContent.blockMetalMultiblock,1, BlockType_MetalMultiblock.BOILER.getMeta()), categoryName);
		modRegistry.handleRecipes(BoilerRecipe.class, instance, categoryName);
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(BoilerRecipe recipe) {
		return new BoilerRecipeWrapper(recipe);
	}

	private BoilerRecipeCategory(IGuiHelper guiHelper) {
		super(categoryName, "category.immersivetech.metal_multiblock.boilerRecipe", "immersivetech:textures/gui/gui_boiler_jei.png");
		background = guiHelper.createDrawable(backgroundImage, 0, 77, 176, 77);
		tankOverlay = guiHelper.createDrawable(backgroundImage, 177, 31, 16, 47, -2, 2, -2, 2);
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, BoilerRecipeWrapper recipeWrapper, IIngredients ingredients) {
		IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();

		guiFluidStacks.init(0, true, 100, 20, 16, 47, Config.ITConfig.Machines.boiler_input_tankSize, false, tankOverlay);
		guiFluidStacks.set(0, recipeWrapper.recipe.input);

		guiFluidStacks.init(1, false, 125, 20, 16, 47, Config.ITConfig.Machines.boiler_output_tankSize, false, tankOverlay);
		guiFluidStacks.set(1, recipeWrapper.recipe.output);

		guiFluidStacks.addTooltipCallback(JEIHelper.fluidTooltipCallback);
	}
}