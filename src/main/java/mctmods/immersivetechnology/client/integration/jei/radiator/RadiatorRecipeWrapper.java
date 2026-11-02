package mctmods.immersivetechnology.client.integration.jei.radiator;

import mctmods.immersivetechnology.client.integration.jei.ITMultiblockRecipeWrapper;
import mctmods.immersivetechnology.core.config.Config.ITConfig.Multiblocks;

import com.immersiveconvergence.api.crafting.ICRecipeBase;
import java.text.DecimalFormat;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;

public class RadiatorRecipeWrapper extends ITMultiblockRecipeWrapper {
	private final ICRecipeBase recipe;
	private static float speedMult() { return Multiblocks.radiator.radiator_speed_multiplier; }
	private static final DecimalFormat format = new DecimalFormat("#.####");

	public RadiatorRecipeWrapper(ICRecipeBase recipe) {
		super(recipe);
		this.recipe = recipe;
	}

	@Override public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
		float time = recipe.getTotalProcessTime() / (speedMult());
		drawTimeText(minecraft, time, format, 50, 10);
	}
}
