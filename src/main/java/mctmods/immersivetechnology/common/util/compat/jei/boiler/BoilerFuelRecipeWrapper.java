package mctmods.immersivetechnology.common.util.compat.jei.boiler;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.common.util.compat.jei.MultiblockRecipeWrapper;
import mctmods.immersivetechnology.api.crafting.BoilerRecipe.BoilerFuelRecipe;
import mctmods.immersivetechnology.common.util.TranslationKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BoilerFuelRecipeWrapper extends MultiblockRecipeWrapper {

	public MultiblockRecipe recipe;
	
	public BoilerFuelRecipeWrapper(MultiblockRecipe recipe) {
		super(recipe);
		this.recipe = recipe;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
		String text = (GuiScreen.isShiftKeyDown())?
				TranslationKey.GUI_TICKS.format(recipe.getTotalProcessTime()) :
				TranslationKey.GUI_SECONDS.format(((float)recipe.getTotalProcessTime()) / 20);
		minecraft.fontRenderer.drawString(text, 80, 10, 0x8B8B8B, true);
		text = TranslationKey.KEYWORD_GENERATE.text();
		minecraft.fontRenderer.drawString(text, 66, 21, 0x8B8B8B, true);
		text = TranslationKey.GUI_BOILER_HEAT_PER_TICK.format(((BoilerFuelRecipe) recipe).getHeat()/40);
		minecraft.fontRenderer.drawString(text, 66, 31, 0x8B8B8B, true);
		text = TranslationKey.GUI_BOILER_TOTAL_HEAT.format(((BoilerFuelRecipe) recipe).getHeat() * recipe.getTotalProcessTime()/40);
		minecraft.fontRenderer.drawString(text, 66, 41, 0x8B8B8B, true);
	}

}