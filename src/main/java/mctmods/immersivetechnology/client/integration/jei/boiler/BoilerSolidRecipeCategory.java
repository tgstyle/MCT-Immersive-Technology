package mctmods.immersivetechnology.client.integration.jei.boiler;

import mctmods.immersivetechnology.client.integration.jei.ITMultiblockIngredients;
import mctmods.immersivetechnology.client.integration.jei.ITRecipeCategory;
import mctmods.immersivetechnology.common.recipes.BoilerSolidRecipe;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import java.util.List;
import javax.annotation.Nonnull;

public class BoilerSolidRecipeCategory extends ITRecipeCategory<BoilerSolidRecipe, BoilerSolidRecipeWrapper> {
	public static ResourceLocation background = new ResourceLocation("immersivetech:textures/gui/boiler_solid.png");

	public BoilerSolidRecipeCategory(IGuiHelper helper) { super("boilerSolid", "tile.immersivetech.metal_multiblock2.boiler_solid.name", helper.createDrawable(background, 0, 0, 176, 77), BoilerSolidRecipe.class, ITMultiblockIngredients.BOILER_SOLID); }

	@Override public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull BoilerSolidRecipeWrapper recipeWrapper, @Nonnull IIngredients ingredients) {
		List<List<ItemStack>> inputs = ingredients.getInputs(VanillaTypes.ITEM);

		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		guiItemStacks.init(0, true, 43, 33);
		guiItemStacks.set(0, inputs.get(0));
	}

	@Override @Nonnull public IRecipeWrapper getRecipeWrapper(@Nonnull BoilerSolidRecipe recipe) { return new BoilerSolidRecipeWrapper(recipe); }
}
