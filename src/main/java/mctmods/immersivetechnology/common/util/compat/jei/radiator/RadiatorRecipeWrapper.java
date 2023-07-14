package mctmods.immersivetechnology.common.util.compat.jei.radiator;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.common.util.compat.jei.MultiblockRecipeWrapper;
import mctmods.immersivetechnology.common.Config;
import mctmods.immersivetechnology.common.util.TranslationKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.text.DecimalFormat;

public class RadiatorRecipeWrapper extends MultiblockRecipeWrapper {

	private MultiblockRecipe recipe;
	private static float speedMult = Config.ITConfig.Machines.Radiator.radiator_speed_multiplier;
	private static DecimalFormat format = new DecimalFormat("#.####");

	public RadiatorRecipeWrapper(MultiblockRecipe recipe) {
		super(recipe);
		this.recipe = recipe;
	}

	@Override
	public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
		float time = recipe.getTotalProcessTime() / (speedMult);
		String text = (GuiScreen.isShiftKeyDown())?
				TranslationKey.GUI_TICKS.format(Math.round(time)) :
				TranslationKey.GUI_SECONDS.format(format.format(time/20));
		minecraft.fontRenderer.drawString(text, 50, 10, 0x8B8B8B, true);
	}

}