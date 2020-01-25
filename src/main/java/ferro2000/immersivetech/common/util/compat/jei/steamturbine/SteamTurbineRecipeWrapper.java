package ferro2000.immersivetech.common.util.compat.jei.steamturbine;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.common.util.compat.jei.MultiblockRecipeWrapper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SteamTurbineRecipeWrapper extends MultiblockRecipeWrapper {

	public MultiblockRecipe recipe;

	public SteamTurbineRecipeWrapper(MultiblockRecipe recipe) {
		super(recipe);
		this.recipe = recipe;
	}

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        String text = I18n.format("keyword.immersivetech.duration") + " " + ((float)recipe.getTotalProcessTime()) / 20 + "s";
        minecraft.fontRenderer.drawString(text, 10, 62, 0x8B8B8B, true);
    }

}