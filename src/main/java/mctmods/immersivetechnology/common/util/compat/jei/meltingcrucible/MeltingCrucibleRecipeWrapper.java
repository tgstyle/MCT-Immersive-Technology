package mctmods.immersivetechnology.common.util.compat.jei.meltingcrucible;

import blusunrize.immersiveengineering.common.util.compat.jei.MultiblockRecipeWrapper;
import mctmods.immersivetechnology.api.crafting.MeltingCrucibleRecipe;
import mctmods.immersivetechnology.common.util.TranslationKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MeltingCrucibleRecipeWrapper extends MultiblockRecipeWrapper {

	public MeltingCrucibleRecipe recipe;

	public MeltingCrucibleRecipeWrapper(MeltingCrucibleRecipe recipe) {
		super(recipe);
		this.recipe = recipe;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
		String text = (GuiScreen.isShiftKeyDown())?
				TranslationKey.GUI_TICKS.format(recipe.getTotalProcessTime()) :
				TranslationKey.GUI_SECONDS.format(((float)recipe.getTotalProcessTime()) / 20);
		minecraft.fontRenderer.drawString(text, 59, 8, 0x8B8B8B, true);
		minecraft.fontRenderer.drawString(TranslationKey.GUI_IF_PER_TICK.format(recipe.getTotalProcessEnergy()/recipe.getTotalProcessTime()),59, 20, 0x8B8B8B, true);
	}

}