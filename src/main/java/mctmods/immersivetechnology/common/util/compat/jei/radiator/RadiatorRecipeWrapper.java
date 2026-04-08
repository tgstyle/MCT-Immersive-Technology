package mctmods.immersivetechnology.common.util.compat.jei.radiator;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.common.util.compat.jei.MultiblockRecipeWrapper;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.util.TranslationKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;

public class RadiatorRecipeWrapper extends MultiblockRecipeWrapper {
	private final MultiblockRecipe recipe;
	private static final float speedMult = Multiblocks.radiator.radiator_speed_multiplier;
	private static final DecimalFormat format = new DecimalFormat("#.####");

	public RadiatorRecipeWrapper(MultiblockRecipe recipe) {
		super(recipe);
		this.recipe = recipe;
	}

	@Override public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
		float time = recipe.getTotalProcessTime() / (speedMult);
		String text = (GuiScreen.isShiftKeyDown())?
				TranslationKey.GUI_TICKS.format(Math.round(time)) :
				TranslationKey.GUI_SECONDS.format(format.format(time/20));
		minecraft.fontRenderer.drawString(text, 50, 10, 0x8B8B8B, true);
	}
}
