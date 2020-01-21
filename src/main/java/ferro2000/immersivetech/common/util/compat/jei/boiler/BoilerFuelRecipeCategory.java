package ferro2000.immersivetech.common.util.compat.jei.boiler;

import ferro2000.immersivetech.api.crafting.BoilerFuelRecipe;
import ferro2000.immersivetech.common.Config;
import ferro2000.immersivetech.common.ITContent;
import ferro2000.immersivetech.common.blocks.metal.types.BlockType_MetalMultiblock;
import ferro2000.immersivetech.common.util.compat.jei.GenericCategory;
import ferro2000.immersivetech.common.util.compat.jei.JEIHelper;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.IRecipeWrapper;

import net.minecraft.item.ItemStack;

import java.util.Collections;

public class BoilerFuelRecipeCategory extends GenericCategory<BoilerFuelRecipe, BoilerFuelRecipeWrapper> {
	private final IDrawable tankOverlay;
	public static BoilerFuelRecipeCategory instance;
	public static String categoryName = "it.boilerFuel";

	public static void registerCategory(IGuiHelper guiHelper, IRecipeCategoryRegistration registry) {
		if(instance != null) return;
		instance = new BoilerFuelRecipeCategory(guiHelper);
		registry.addRecipeCategories(instance);
	}

	public static void registerRest(IModRegistry modRegistry) {
		if(instance == null) return;
		modRegistry.addRecipes(Collections.unmodifiableList(BoilerFuelRecipe.recipeList), categoryName);
		modRegistry.addRecipeCatalyst(new ItemStack(ITContent.blockMetalMultiblock, 1, BlockType_MetalMultiblock.BOILER.getMeta()), categoryName);
		modRegistry.handleRecipes(BoilerFuelRecipe.class, instance, categoryName);
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(BoilerFuelRecipe recipe) {
		return new BoilerFuelRecipeWrapper(recipe);
	}

	@SuppressWarnings("deprecation")
	private BoilerFuelRecipeCategory(IGuiHelper guiHelper) {
		super(categoryName, "category.immersivetech.metal_multiblock.boilerFuel", "immersivetech:textures/gui/gui_boiler_jei.png");
		background = guiHelper.createDrawable(backgroundImage, 0, 0, 176, 77);
		tankOverlay = guiHelper.createDrawable(backgroundImage, 177, 31, 16, 47, -2, 2, -2, 2);
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, BoilerFuelRecipeWrapper recipeWrapper, IIngredients ingredients) {
		IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();
		guiFluidStacks.init(0, true, 13, 20, 16, 47, Config.ITConfig.Machines.boiler_fuel_tankSize, false, tankOverlay);
		guiFluidStacks.set(0, recipeWrapper.recipe.input);
		guiFluidStacks.addTooltipCallback(JEIHelper.fluidTooltipCallback);
	}

}