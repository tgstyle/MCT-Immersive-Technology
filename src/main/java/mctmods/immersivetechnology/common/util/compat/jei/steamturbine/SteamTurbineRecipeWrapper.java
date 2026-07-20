package mctmods.immersivetechnology.common.util.compat.jei.steamturbine;

import mctmods.immersivetechnology.common.util.compat.jei.ITMultiblockRecipeWrapper;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SteamTurbineRecipeWrapper extends ITMultiblockRecipeWrapper {
	public MultiblockRecipe recipe;

	public SteamTurbineRecipeWrapper(MultiblockRecipe recipe) {
		super(recipe);
		this.recipe = recipe;
	}

	@Override @SideOnly(Side.CLIENT) public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) { drawTimeText(minecraft, recipe.getTotalProcessTime(), 44, 10); }
}
