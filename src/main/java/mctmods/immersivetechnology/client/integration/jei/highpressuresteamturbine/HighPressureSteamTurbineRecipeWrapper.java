package mctmods.immersivetechnology.client.integration.jei.highpressuresteamturbine;

import mctmods.immersivetechnology.client.integration.jei.ITMultiblockRecipeWrapper;

import com.immersiveconvergence.api.crafting.ICRecipeBase;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class HighPressureSteamTurbineRecipeWrapper extends ITMultiblockRecipeWrapper {
	public ICRecipeBase recipe;

	public HighPressureSteamTurbineRecipeWrapper(ICRecipeBase recipe) {
		super(recipe);
		this.recipe = recipe;
	}

	@Override @SideOnly(Side.CLIENT) public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) { drawTimeText(minecraft, recipe.getTotalProcessTime(), 44, 10); }
}
