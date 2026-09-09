package mctmods.immersivetechnology.common.util.compat.jei.solartower;

import com.immersiveconvergence.api.crafting.MultiblockRecipeBase;

import mctmods.immersivetechnology.api.crafting.SolarTowerRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.util.TranslationKey;
import mctmods.immersivetechnology.common.util.compat.jei.ITMultiblockRecipeWrapper;

import java.text.DecimalFormat;
import javax.annotation.Nonnull;
import mezz.jei.api.gui.ITickTimer;
import net.minecraft.client.Minecraft;

public class SolarTowerRecipeWrapper extends ITMultiblockRecipeWrapper {
	public ITickTimer timer;
	private final MultiblockRecipeBase recipe;
	private static float speedMult() { return Multiblocks.solarTower.solarTower_speed_multiplier; }
	private static final DecimalFormat format = new DecimalFormat("#.####");

	public SolarTowerRecipeWrapper(MultiblockRecipeBase recipe) {
		super(recipe);
		this.recipe = recipe;
	}

	@Override public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
		float time = recipe.getTotalProcessTime() / (speedMult() * (timer.getValue() + 1));
		drawTimeText(minecraft, time, format, 21, 10);
		String text = TranslationKey.KEYWORD_HEAT_LEVEL.text() + ": " + (int)((SolarTowerRecipe)recipe).requiredTemp;
		minecraft.fontRenderer.drawString(text, 21, 20, 0x8B8B8B, true);
	}
}
