package mctmods.immersivetechnology.common.util.compat.jei;

import mctmods.immersivetechnology.api.crafting.BoilerRecipe;
import mctmods.immersivetechnology.api.crafting.BoilerRecipe.BoilerFuelRecipe;
import mctmods.immersivetechnology.api.crafting.DistillerRecipe;
import mctmods.immersivetechnology.api.crafting.SolarTowerRecipe;
import mctmods.immersivetechnology.api.crafting.SteamTurbineRecipe;
import mctmods.immersivetechnology.common.util.compat.ITCompatModule;
import mctmods.immersivetechnology.common.util.compat.jei.boiler.BoilerFuelRecipeCategory;
import mctmods.immersivetechnology.common.util.compat.jei.boiler.BoilerRecipeCategory;
import mctmods.immersivetechnology.common.util.compat.jei.distiller.DistillerRecipeCategory;
import mctmods.immersivetechnology.common.util.compat.jei.solartower.SolarTowerRecipeCategory;
import mctmods.immersivetechnology.common.util.compat.jei.steamturbine.SteamTurbineRecipeCategory;
import mezz.jei.api.*;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.ITooltipCallback;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@JEIPlugin
public class JEIHelper implements IModPlugin {
	public static IJeiHelpers jeiHelpers;
	public static IModRegistry modRegistry;
	public static IDrawable slotDrawable;
	public static ITooltipCallback<FluidStack> fluidTooltipCallback = new ITFluidTooltipCallback();

	@Override
	public void registerIngredients(IModIngredientRegistration registry) {
		//registry.register();
	}

	@SuppressWarnings("rawtypes")
	Map<Class, ITRecipeCategory> categories = new LinkedHashMap<>();

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry) {
		jeiHelpers = registry.getJeiHelpers();

		//Recipes
		IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
		slotDrawable = guiHelper.getSlotDrawable();
		
		categories.put(DistillerRecipe.class, new DistillerRecipeCategory(guiHelper));
		categories.put(BoilerRecipe.class, new BoilerRecipeCategory(guiHelper));
		categories.put(BoilerFuelRecipe.class, new BoilerFuelRecipeCategory(guiHelper));
		categories.put(SolarTowerRecipe.class, new SolarTowerRecipeCategory(guiHelper));
		categories.put(SteamTurbineRecipe.class, new SteamTurbineRecipeCategory(guiHelper));
			
		registry.addRecipeCategories(categories.values().toArray(new IRecipeCategory[categories.size()]));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void register(IModRegistry registryIn) {
		modRegistry = registryIn;

		for(ITRecipeCategory<Object, IRecipeWrapper> cat : categories.values()) {
			cat.addCatalysts(registryIn);
			modRegistry.handleRecipes(cat.getRecipeClass(), cat, cat.getRecipeCategoryUid());
		}

		modRegistry.addRecipes(new ArrayList<Object>((DistillerRecipe.recipeList)), "it.distiller");
		modRegistry.addRecipes(new ArrayList<Object>((BoilerRecipe.recipeList)), "it.boiler");
		modRegistry.addRecipes(new ArrayList<Object>((BoilerRecipe.fuelList)), "it.boilerFuel");
		modRegistry.addRecipes(new ArrayList<Object>((SolarTowerRecipe.recipeList)), "it.solarTower");
		modRegistry.addRecipes(new ArrayList<Object>((SteamTurbineRecipe.recipeList)), "it.steamTurbine");
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime)
	{
		final IRecipeRegistry registry = jeiRuntime.getRecipeRegistry();
		ITCompatModule.jeiAddFunc = recipe -> {
			ITRecipeCategory<Object, ?> factory = getFactory(recipe.getClass());
			if(factory != null)	registry.addRecipe(factory.getRecipeWrapper(recipe), factory.getUid());
		};
		ITCompatModule.jeiRemoveFunc = recipe -> {
			ITRecipeCategory<Object, ?> factory = getFactory(recipe.getClass());
			if(factory != null)	registry.removeRecipe(factory.getRecipeWrapper(recipe), factory.getUid());
		};
	}

	@SuppressWarnings("unchecked")
	private ITRecipeCategory<Object, ?> getFactory(Class<?> recipeClass) {
		ITRecipeCategory<Object, ?> factory = this.categories.get(recipeClass);

		if(factory == null && recipeClass != Object.class)
			factory = getFactory(recipeClass.getSuperclass());

		return factory;
	}

}