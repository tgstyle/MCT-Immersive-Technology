package mctmods.immersivetechnology.common.util.compat.jei.radiator;

import com.immersiveconvergence.api.crafting.ICRecipeBase;

import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.util.compat.jei.ITMultiblockRecipeWrapper;

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
