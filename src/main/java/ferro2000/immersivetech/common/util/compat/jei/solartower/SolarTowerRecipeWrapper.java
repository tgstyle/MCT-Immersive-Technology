package ferro2000.immersivetech.common.util.compat.jei.solartower;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.common.util.compat.jei.MultiblockRecipeWrapper;
import ferro2000.immersivetech.api.crafting.SolarTowerRecipe;
import ferro2000.immersivetech.common.Config;
import mezz.jei.api.gui.ITickTimer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import java.text.DecimalFormat;

public class SolarTowerRecipeWrapper extends MultiblockRecipeWrapper {

	public ITickTimer timer;
	private MultiblockRecipe recipe;
	private static float speedMult = Config.ITConfig.Machines.solarTower_speedMultiplier;
	private static float reflectorSpeedMult = Config.ITConfig.Machines.solarTower_reflectorSpeedMultiplier;
	private static DecimalFormat format = new DecimalFormat("#.####");

	public SolarTowerRecipeWrapper(MultiblockRecipe recipe) {
		super(recipe);
		this.recipe = recipe;
	}

	@Override
	public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
		String text;
		float time = recipe.getTotalProcessTime() / (speedMult * (1 + timer.getValue() * (reflectorSpeedMult - 1)));
		text = (GuiScreen.isShiftKeyDown())? Math.round(time) + " ticks" : format.format(time/20) + "s";
		minecraft.fontRenderer.drawString(text, 21, 10, 0x8B8B8B, true);
	}
}