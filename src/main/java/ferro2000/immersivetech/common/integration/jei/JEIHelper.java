package ferro2000.immersivetech.common.integration.jei;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import ferro2000.immersivetech.api.craftings.BoilerRecipes;
import ferro2000.immersivetech.api.craftings.DistillerRecipes;
import ferro2000.immersivetech.api.craftings.SolarTowerRecipes;
import ferro2000.immersivetech.common.integration.ITIntegrationModule;
import ferro2000.immersivetech.common.integration.jei.boiler.BoilerRecipeCategory;
import ferro2000.immersivetech.common.integration.jei.distiller.DistillerRecipeCategory;
import ferro2000.immersivetech.common.integration.jei.solartower.SolarTowerRecipeCategory;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.ITooltipCallback;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.IRecipeWrapper;

@JEIPlugin
public class JEIHelper implements IModPlugin
{
	public static IJeiHelpers jeiHelpers;
	public static IModRegistry modRegistry;
	public static IDrawable slotDrawable;
	public static ITooltipCallback fluidTooltipCallback = new ITFluidTooltipCallback();

	@Override
	public void registerIngredients(IModIngredientRegistration registry)
	{
	}

	Map<Class, ITRecipeCategory> categories = new LinkedHashMap<>();

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry)
	{
		jeiHelpers = registry.getJeiHelpers();
		//Recipes
		IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
		slotDrawable = guiHelper.getSlotDrawable();
		
		categories.put(DistillerRecipes.class, new DistillerRecipeCategory(guiHelper));
		categories.put(BoilerRecipes.class, new BoilerRecipeCategory(guiHelper));
		categories.put(SolarTowerRecipes.class, new SolarTowerRecipeCategory(guiHelper));
			
		/*categories.put(CokeOvenRecipe.class, new CokeOvenRecipeCategory(guiHelper));
		categories.put(AlloyRecipe.class, new AlloySmelterRecipeCategory(guiHelper));
		categories.put(BlastFurnaceRecipe.class, new BlastFurnaceRecipeCategory(guiHelper));
		categories.put(BlastFurnaceFuel.class, new BlastFurnaceFuelCategory(guiHelper));
		categories.put(MetalPressRecipe.class, new MetalPressRecipeCategory(guiHelper));
		categories.put(CrusherRecipe.class, new CrusherRecipeCategory(guiHelper));
		categories.put(BlueprintCraftingRecipe.class, new WorkbenchRecipeCategory(guiHelper));
		categories.put(SqueezerRecipe.class, new SqueezerRecipeCategory(guiHelper));
		categories.put(FermenterRecipe.class, new FermenterRecipeCategory(guiHelper));
		categories.put(RefineryRecipe.class, new RefineryRecipeCategory(guiHelper));
		categories.put(ArcFurnaceRecipe.class, new ArcFurnaceRecipeCategory(guiHelper));
		categories.put(BottlingMachineRecipe.class, new BottlingMachineRecipeCategory(guiHelper));
		categories.put(MixerRecipe.class, new MixerRecipeCategory(guiHelper));*/
		registry.addRecipeCategories(categories.values().toArray(new IRecipeCategory[categories.size()]));
	}

	@Override
	public void register(IModRegistry registryIn)
	{
		modRegistry = registryIn;
		//Blacklist
		/*jeiHelpers.getIngredientBlacklist().addIngredientToBlacklist(new ItemStack(IEContent.blockCrop, 1, OreDictionary.WILDCARD_VALUE));
		jeiHelpers.getIngredientBlacklist().addIngredientToBlacklist(new ItemStack(IEContent.itemFakeIcons, 1, OreDictionary.WILDCARD_VALUE));
		jeiHelpers.getIngredientBlacklist().addIngredientToBlacklist(new ItemStack(IEContent.blockStoneDevice, 1, OreDictionary.WILDCARD_VALUE));
		jeiHelpers.getIngredientBlacklist().addIngredientToBlacklist(new ItemStack(IEContent.blockMetalMultiblock, 1, OreDictionary.WILDCARD_VALUE));*/

		/*modRegistry.getRecipeTransferRegistry().addRecipeTransferHandler(new AssemblerRecipeTransferHandler(), VanillaRecipeCategoryUid.CRAFTING);
		modRegistry.addRecipeCatalyst(new ItemStack(IEContent.blockMetalMultiblock, 1, BlockTypes_MetalMultiblock.ASSEMBLER.getMeta()), VanillaRecipeCategoryUid.CRAFTING);
		*/
		for(ITRecipeCategory<Object, IRecipeWrapper> cat : categories.values())
		{
			cat.addCatalysts(registryIn);
			modRegistry.handleRecipes(cat.getRecipeClass(), cat, cat.getRecipeCategoryUid());
		}
//		modRegistry.addRecipeHandlers(categories);

		modRegistry.addRecipes(new ArrayList((DistillerRecipes.recipeList)), "it.distiller");
		modRegistry.addRecipes(new ArrayList((BoilerRecipes.recipeList)), "it.boiler");
		modRegistry.addRecipes(new ArrayList((SolarTowerRecipes.recipeList)), "it.solarTower");
		
		/*modRegistry.addRecipes(new ArrayList(CokeOvenRecipe.recipeList), "ie.cokeoven");
		modRegistry.addRecipes(new ArrayList(AlloyRecipe.recipeList), "ie.alloysmelter");
		modRegistry.addRecipes(new ArrayList(BlastFurnaceRecipe.recipeList), "ie.blastfurnace");
		modRegistry.addRecipes(new ArrayList(BlastFurnaceRecipe.blastFuels), "ie.blastfurnace.fuel");
		modRegistry.addRecipes(new ArrayList(Collections2.filter(MetalPressRecipe.recipeList.values(), input -> input.listInJEI())), "ie.metalPress");
		modRegistry.addRecipes(new ArrayList(Collections2.filter(CrusherRecipe.recipeList, input -> input.listInJEI())), "ie.crusher");
		modRegistry.addRecipes(new ArrayList(Collections2.filter(BlueprintCraftingRecipe.recipeList.values(), input -> input.listInJEI())), "ie.workbench");
		modRegistry.addRecipes(new ArrayList(Collections2.filter(SqueezerRecipe.recipeList, input -> input.listInJEI())), "ie.squeezer");
		modRegistry.addRecipes(new ArrayList(Collections2.filter(FermenterRecipe.recipeList, input -> input.listInJEI())), "ie.fermenter");
		modRegistry.addRecipes(new ArrayList(Collections2.filter(RefineryRecipe.recipeList, input -> input.listInJEI())), "ie.refinery");
		modRegistry.addRecipes(new ArrayList(Collections2.filter(ArcFurnaceRecipe.recipeList, input -> input.listInJEI())), "ie.arcFurnace");
		modRegistry.addRecipes(new ArrayList(Collections2.filter(BottlingMachineRecipe.recipeList, input -> input.listInJEI())), "ie.bottlingMachine");
		modRegistry.addRecipes(new ArrayList(Collections2.filter(MixerRecipe.recipeList, input -> input.listInJEI())), "ie.mixer");*/
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime)
	{
		final IRecipeRegistry registry = jeiRuntime.getRecipeRegistry();
		ITIntegrationModule.jeiAddFunc = recipe ->
		{
			ITRecipeCategory factory = getFactory(recipe.getClass());
			if(factory!=null)
				registry.addRecipe(factory.getRecipeWrapper(recipe), factory.getUid());
		};
		ITIntegrationModule.jeiRemoveFunc = recipe ->
		{
			ITRecipeCategory factory = getFactory(recipe.getClass());
			if(factory!=null)
				registry.removeRecipe(factory.getRecipeWrapper(recipe), factory.getUid());
		};
	}

	private ITRecipeCategory getFactory(Class recipeClass)
	{
		ITRecipeCategory factory = this.categories.get(recipeClass);

		if(factory==null&&recipeClass!=Object.class)
			factory = getFactory(recipeClass.getSuperclass());

		return factory;
	}

}
