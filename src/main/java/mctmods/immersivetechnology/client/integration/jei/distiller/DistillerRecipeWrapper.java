package mctmods.immersivetechnology.client.integration.jei.distiller;

import mctmods.immersivetechnology.client.integration.jei.ITMultiblockRecipeWrapper;
import mctmods.immersivetechnology.common.recipes.DistillerRecipe;

import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class DistillerRecipeWrapper extends ITMultiblockRecipeWrapper {
	public DistillerRecipe recipe;

	public DistillerRecipeWrapper(DistillerRecipe recipe) {
		super(recipe);
		this.recipe = recipe;
	}

	@Override @SideOnly(Side.CLIENT) public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) { drawTimeText(minecraft, recipe.getTotalProcessTime(), 125, 25); }
}
