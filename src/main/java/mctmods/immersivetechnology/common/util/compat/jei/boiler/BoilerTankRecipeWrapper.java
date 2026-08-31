package mctmods.immersivetechnology.common.util.compat.jei.boiler;

import mctmods.immersivetechnology.api.crafting.BoilerTankRecipe;
import mctmods.immersivetechnology.common.util.TranslationKey;
import mctmods.immersivetechnology.common.util.compat.jei.ITMultiblockRecipeWrapper;

import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BoilerTankRecipeWrapper extends ITMultiblockRecipeWrapper {
	public BoilerTankRecipe recipe;

	public BoilerTankRecipeWrapper(BoilerTankRecipe recipe) {
		super(recipe);
		this.recipe = recipe;
	}

	@Override @SideOnly(Side.CLIENT) public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
		drawTimeText(minecraft, recipe.getTotalProcessTime(), 10, 10);
		String text = TranslationKey.KEYWORD_HEAT_LEVEL.text() + ": " + (int)recipe.requiredHeat;
		minecraft.fontRenderer.drawString(text, 10, 60, 0x8B8B8B, true);
	}
}
