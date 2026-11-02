package mctmods.immersivetechnology.client.integration.jei.solartower;

import mctmods.immersivetechnology.client.integration.jei.ITMultiblockRecipeWrapper;
import mctmods.immersivetechnology.common.recipes.SolarTowerRecipe;
import mctmods.immersivetechnology.common.util.TranslationKey;
import mctmods.immersivetechnology.core.config.Config.ITConfig.Multiblocks;

import com.immersiveconvergence.api.crafting.ICRecipeBase;
import java.text.DecimalFormat;
import javax.annotation.Nonnull;
import mezz.jei.api.gui.ITickTimer;
import net.minecraft.client.Minecraft;

public class SolarTowerRecipeWrapper extends ITMultiblockRecipeWrapper {
	public ITickTimer timer;
	private final ICRecipeBase recipe;
	private static float speedMult() { return Multiblocks.solarTower.solarTower_speed_multiplier; }
	private static final DecimalFormat format = new DecimalFormat("#.####");

	public SolarTowerRecipeWrapper(ICRecipeBase recipe) {
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
