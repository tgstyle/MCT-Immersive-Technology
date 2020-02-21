package mctmods.immersivetechnology.common.util.compat.jei.boiler;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.common.util.compat.jei.MultiblockRecipeWrapper;
import mctmods.immersivetechnology.api.crafting.BoilerRecipe.BoilerFuelRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
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
		String text = (GuiScreen.isShiftKeyDown())? recipe.getTotalProcessTime() + "t" : ((float)recipe.getTotalProcessTime()) / 20 + "s";
		minecraft.fontRenderer.drawString(text, 80, 10, 0x8B8B8B, true);
		text = I18n.format("keyword.immersivetech.generate");
		minecraft.fontRenderer.drawString(text, 66, 21, 0x8B8B8B, true);
		text = ((BoilerFuelRecipe) recipe).getHeat() + " " + I18n.format("keyword.immersivetech.heatPerTick");
		minecraft.fontRenderer.drawString(text, 66, 31, 0x8B8B8B, true);
		text = (((BoilerFuelRecipe) recipe).getHeat() * recipe.getTotalProcessTime()) + " " + I18n.format("keyword.immersivetech.totalHeat");
		minecraft.fontRenderer.drawString(text, 66, 41, 0x8B8B8B, true);
	}

}