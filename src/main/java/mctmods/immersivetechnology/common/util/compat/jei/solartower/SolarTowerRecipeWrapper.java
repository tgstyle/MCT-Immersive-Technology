package mctmods.immersivetechnology.common.util.compat.jei.solartower;

import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.util.compat.jei.ITMultiblockRecipeWrapper;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import java.text.DecimalFormat;
import javax.annotation.Nonnull;
import mezz.jei.api.gui.ITickTimer;
import net.minecraft.client.Minecraft;

public class SolarTowerRecipeWrapper extends ITMultiblockRecipeWrapper {
	public ITickTimer timer;
	private final MultiblockRecipe recipe;
	private static float speedMult() { return Multiblocks.solarTower.solarTower_speed_multiplier; }
	private static final DecimalFormat format = new DecimalFormat("#.####");

	public SolarTowerRecipeWrapper(MultiblockRecipe recipe) {
		super(recipe);
		this.recipe = recipe;
	}

	@Override public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
		float time = recipe.getTotalProcessTime() / (speedMult() * (timer.getValue() + 1));
		drawTimeText(minecraft, time, format, 21, 10);
	}
}
