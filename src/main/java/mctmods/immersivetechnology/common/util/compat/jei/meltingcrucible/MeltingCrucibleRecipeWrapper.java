package mctmods.immersivetechnology.common.util.compat.jei.meltingcrucible;

import mctmods.immersivetechnology.api.crafting.MeltingCrucibleRecipe;
import mctmods.immersivetechnology.common.util.TranslationKey;
import mctmods.immersivetechnology.common.util.compat.jei.ITMultiblockRecipeWrapper;

import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MeltingCrucibleRecipeWrapper extends ITMultiblockRecipeWrapper {
	public MeltingCrucibleRecipe recipe;

	public MeltingCrucibleRecipeWrapper(MeltingCrucibleRecipe recipe) {
		super(recipe);
		this.recipe = recipe;
	}

	@Override @SideOnly(Side.CLIENT) public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
		drawTimeText(minecraft, recipe.getTotalProcessTime(), 59, 8);
		String text = TranslationKey.KEYWORD_HEAT_LEVEL.text() + ": " + (int)recipe.requiredTemp;
		minecraft.fontRenderer.drawString(text, 59, 18, 0x8B8B8B, true);
	}
}
