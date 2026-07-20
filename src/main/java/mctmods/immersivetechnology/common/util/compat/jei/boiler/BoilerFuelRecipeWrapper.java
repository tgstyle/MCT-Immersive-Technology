package mctmods.immersivetechnology.common.util.compat.jei.boiler;

import mctmods.immersivetechnology.api.crafting.BoilerRecipe.BoilerFuelRecipe;
import mctmods.immersivetechnology.common.util.TranslationKey;
import mctmods.immersivetechnology.common.util.compat.jei.ITMultiblockRecipeWrapper;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BoilerFuelRecipeWrapper extends ITMultiblockRecipeWrapper {
	public MultiblockRecipe recipe;

	public BoilerFuelRecipeWrapper(MultiblockRecipe recipe) {
		super(recipe);
		this.recipe = recipe;
	}

	@Override @SideOnly(Side.CLIENT) public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
		drawTimeText(minecraft, recipe.getTotalProcessTime(), 80, 10);
		String text = TranslationKey.KEYWORD_GENERATE.text();
		minecraft.fontRenderer.drawString(text, 66, 21, 0x8B8B8B, true);
		text = TranslationKey.GUI_BOILER_HEAT_PER_TICK.format(((BoilerFuelRecipe) recipe).getHeat()/40);
		minecraft.fontRenderer.drawString(text, 66, 31, 0x8B8B8B, true);
		text = TranslationKey.GUI_BOILER_TOTAL_HEAT.format(((BoilerFuelRecipe) recipe).getHeat() * recipe.getTotalProcessTime()/40);
		minecraft.fontRenderer.drawString(text, 66, 41, 0x8B8B8B, true);
	}
}
