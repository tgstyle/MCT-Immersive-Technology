package ferro2000.immersivetech.common.util.compat.jei.steamturbine;

import ferro2000.immersivetech.api.crafting.SteamTurbineRecipe;
import ferro2000.immersivetech.common.Config;
import ferro2000.immersivetech.common.ITContent;
import ferro2000.immersivetech.common.blocks.metal.types.BlockType_MetalMultiblock;
import ferro2000.immersivetech.common.util.compat.jei.GenericCategory;
import ferro2000.immersivetech.common.util.compat.jei.JEIHelper;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.gui.*;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import java.util.Collections;

public class SteamTurbineRecipeCategory extends GenericCategory<SteamTurbineRecipe, SteamTurbineRecipeWrapper> {
	private final IDrawable tankOverlay;
	private final IDrawableAnimated turbineAndArrow;
	public static SteamTurbineRecipeCategory instance;
	public static String categoryName = "it.steamturbine";

	public static void registerCategory(IGuiHelper guiHelper, IRecipeCategoryRegistration registry) {
		if(instance != null) return;
		instance = new SteamTurbineRecipeCategory(guiHelper);
		registry.addRecipeCategories(instance);
	}

	public static void registerRest(IModRegistry modRegistry) {
		if(instance == null) return;
		modRegistry.addRecipes(Collections.unmodifiableList(SteamTurbineRecipe.recipeList), categoryName);
		modRegistry.addRecipeCatalyst(new ItemStack(ITContent.blockMetalMultiblock, 1, BlockType_MetalMultiblock.STEAM_TURBINE.getMeta()), categoryName);
		modRegistry.handleRecipes(SteamTurbineRecipe.class, instance, categoryName);
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(SteamTurbineRecipe recipe) {
		return new SteamTurbineRecipeWrapper(recipe);
	}

	@SuppressWarnings("deprecation")
	private SteamTurbineRecipeCategory(IGuiHelper guiHelper) {
		super(categoryName, "tile.immersivetech.metal_multiblock.steam_turbine.name", "immersivetech:textures/gui/gui_steam_turbine.png");
		background = guiHelper.createDrawable(backgroundImage, 0, 0, 96, 78);
		tankOverlay = guiHelper.createDrawable(backgroundImage, 98, 2, 16, 47, -2, 2, -2, 2);
		IDrawableStatic staticImage = guiHelper.createDrawable(backgroundImage, 0, 78, 32, 42);
		this.turbineAndArrow = guiHelper.createAnimatedDrawable(staticImage, 200, IDrawableAnimated.StartDirection.LEFT, false);
	}

	@Override
	public void drawExtras(Minecraft minecraft) {
		turbineAndArrow.draw(minecraft, 32, 18);
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, SteamTurbineRecipeWrapper recipeWrapper, IIngredients ingredients) {
		IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();
		guiFluidStacks.init(0, true, 11, 11, 16, 47, Config.ITConfig.Machines.steamTurbine_input_tankSize, true, tankOverlay);
		guiFluidStacks.set(0, recipeWrapper.recipe.input);
		if(recipeWrapper.recipe.output != null) {
			guiFluidStacks.init(1, false, 69, 11, 16, 47, Config.ITConfig.Machines.steamTurbine_output_tankSize, true, tankOverlay);
			guiFluidStacks.set(1, recipeWrapper.recipe.output);
		}
		guiFluidStacks.addTooltipCallback(JEIHelper.fluidTooltipCallback);
	}
}