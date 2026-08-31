package mctmods.immersivetechnology.common.util.compat.jei.boiler;

import mctmods.immersivetechnology.api.crafting.BoilerLiquidRecipe;
import mctmods.immersivetechnology.common.util.TranslationKey;
import mctmods.immersivetechnology.common.util.compat.jei.ITMultiblockRecipeWrapper;

import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BoilerLiquidRecipeWrapper extends ITMultiblockRecipeWrapper {
	public BoilerLiquidRecipe recipe;

	public BoilerLiquidRecipeWrapper(BoilerLiquidRecipe recipe) {
		super(recipe);
		this.recipe = recipe;
	}

	@Override @SideOnly(Side.CLIENT) public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
		String text = TranslationKey.KEYWORD_GENERATE.text();
		minecraft.fontRenderer.drawString(text, 10, 21, 0x8B8B8B, true);
		text = TranslationKey.GUI_BOILER_HEAT_PER_TICK.format(recipe.heatPerTick);
		minecraft.fontRenderer.drawString(text, 10, 31, 0x8B8B8B, true);
		text = TranslationKey.KEYWORD_HEAT_LEVEL.text() + ": " + (int)recipe.targetHeat;
		minecraft.fontRenderer.drawString(text, 10, 41, 0x8B8B8B, true);
	}
}
