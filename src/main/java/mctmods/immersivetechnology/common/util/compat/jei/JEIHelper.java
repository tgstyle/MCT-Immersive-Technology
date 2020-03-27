package mctmods.immersivetechnology.common.util.compat.jei;

import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.util.compat.jei.cokeoven.CokeOvenRecipeCategory;
import mctmods.immersivetechnology.api.crafting.BoilerRecipe;
import mctmods.immersivetechnology.api.crafting.BoilerRecipe.BoilerFuelRecipe;
import mctmods.immersivetechnology.api.crafting.DistillerRecipe;
import mctmods.immersivetechnology.api.crafting.SolarTowerRecipe;
import mctmods.immersivetechnology.api.crafting.SteamTurbineRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Experimental;
import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.blocks.metal.multiblocks.MultiblockSteamTurbine;
import mctmods.immersivetechnology.common.blocks.metal.types.BlockType_MetalMultiblock;
import mctmods.immersivetechnology.common.util.compat.ITCompatModule;
import mctmods.immersivetechnology.common.util.compat.jei.boiler.BoilerFuelRecipeCategory;
import mctmods.immersivetechnology.common.util.compat.jei.boiler.BoilerRecipeCategory;
import mctmods.immersivetechnology.common.util.compat.jei.distiller.DistillerRecipeCategory;
import mctmods.immersivetechnology.common.util.compat.jei.solartower.SolarTowerRecipeCategory;
import mctmods.immersivetechnology.common.util.compat.jei.steamturbine.SteamTurbineRecipeCategory;
import mezz.jei.api.*;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.ITooltipCallback;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.*;

@JEIPlugin
public class JEIHelper implements IModPlugin {
	public static IJeiHelpers jeiHelpers;
	public static IModRegistry modRegistry;
	public static IDrawable slotDrawable;
	public static ITooltipCallback<FluidStack> fluidTooltipCallback = new ITFluidTooltipCallback();

	@Override
	public void registerIngredients(IModIngredientRegistration registry) {
		registry.register(GenericMultiblockIngredient.class, GenericMultiblockIngredient.list, new GenericMultiblockHelper(), new GenericMultiblockRenderer());
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

		if(Experimental.replace_IE_pipes) registryIn.getJeiHelpers().getIngredientBlacklist().addIngredientToBlacklist(new ItemStack(ITContent.blockMetalDevice1Dummy, 1, 0));

		for(ITRecipeCategory<Object, IRecipeWrapper> cat : categories.values()) {
			cat.addCatalysts(registryIn);
			modRegistry.handleRecipes(cat.getRecipeClass(), cat, cat.getRecipeCategoryUid());
		}

		modRegistry.addRecipeCatalyst(GenericMultiblockIngredient.COKE_OVEN_ADVANCED, "ie.cokeoven");

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