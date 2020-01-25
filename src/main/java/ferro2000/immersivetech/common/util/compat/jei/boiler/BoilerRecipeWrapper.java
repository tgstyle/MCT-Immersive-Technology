package ferro2000.immersivetech.common.util.compat.jei.boiler;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.common.util.compat.jei.MultiblockRecipeWrapper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BoilerRecipeWrapper extends MultiblockRecipeWrapper {
	
	public MultiblockRecipe recipe;

	public BoilerRecipeWrapper(MultiblockRecipe recipe) {
		super(recipe);
		this.recipe = recipe;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
		String text = ((float)recipe.getTotalProcessTime()) / 20 + "s";
		minecraft.fontRenderer.drawString(I18n.format("keyword.immersivetech.duration"), 10, 10, 0x8B8B8B, true);
		minecraft.fontRenderer.drawString(text, 10, 20, 0x8B8B8B, true);
	}

}