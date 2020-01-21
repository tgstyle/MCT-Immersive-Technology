package ferro2000.immersivetech.common.util.compat.jei;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import ferro2000.immersivetech.api.crafting.DistillerRecipes;
import ferro2000.immersivetech.api.crafting.SolarTowerRecipes;
import ferro2000.immersivetech.common.util.compat.jei.boiler.BoilerFuelRecipeCategory;
import ferro2000.immersivetech.common.util.compat.jei.boiler.BoilerRecipeCategory;
import ferro2000.immersivetech.common.util.compat.jei.distiller.DistillerRecipeCategory;
import ferro2000.immersivetech.common.util.compat.jei.solartower.SolarTowerRecipeCategory;
import ferro2000.immersivetech.common.util.compat.jei.steamturbine.SteamTurbineRecipeCategory;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.gui.ITooltipCallback;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.IRecipeWrapper;

@JEIPlugin
public class JEIHelper implements IModPlugin {
	public static IJeiHelpers jeiHelpers;
	public static IModRegistry modRegistry;
	public static ITooltipCallback fluidTooltipCallback = new ITFluidTooltipCallback();

	@Override
	public void registerIngredients(IModIngredientRegistration registry) {
	}

	Map<Class, ITRecipeCategory> categories = new LinkedHashMap<>();

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry) {
		jeiHelpers = registry.getJeiHelpers();
		//Recipes
		IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
		categories.put(DistillerRecipes.class, new DistillerRecipeCategory(guiHelper));
		categories.put(SolarTowerRecipes.class, new SolarTowerRecipeCategory(guiHelper));
		registry.addRecipeCategories(categories.values().toArray(new IRecipeCategory[categories.size()]));
		SteamTurbineRecipeCategory.registerCategory(guiHelper, registry);
		BoilerRecipeCategory.registerCategory(guiHelper, registry);
		BoilerFuelRecipeCategory.registerCategory(guiHelper, registry);
	}

	@Override
	public void register(IModRegistry registryIn) {
		modRegistry = registryIn;
		SteamTurbineRecipeCategory.registerRest(modRegistry);
		BoilerRecipeCategory.registerRest(modRegistry);
		BoilerFuelRecipeCategory.registerRest(modRegistry);
		for(ITRecipeCategory<Object, IRecipeWrapper> cat : categories.values()) {
			cat.addCatalysts(registryIn);
			modRegistry.handleRecipes(cat.getRecipeClass(), cat, cat.getRecipeCategoryUid());
		}
		modRegistry.addRecipes(new ArrayList((DistillerRecipes.recipeList)), "it.distiller");
		modRegistry.addRecipes(new ArrayList((SolarTowerRecipes.recipeList)), "it.solarTower");
	}

}