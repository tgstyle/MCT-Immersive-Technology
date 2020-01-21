package ferro2000.immersivetech.common.util.compat.jei.boiler;

import ferro2000.immersivetech.api.crafting.BoilerRecipe;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BoilerRecipeWrapper implements IRecipeWrapper {
	public BoilerRecipe recipe;

	public BoilerRecipeWrapper(BoilerRecipe recipe) {
		this.recipe = recipe;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void getIngredients(IIngredients ingredients) {
		ingredients.setInput(FluidStack.class, recipe.input);
		ingredients.setOutput(FluidStack.class, recipe.output);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
		String text = ((float)recipe.time)/20 + "s";
		minecraft.fontRenderer.drawString(I18n.format("keyword.immersivetech.duration"), 10, 10, 0x8B8B8B, true);
		minecraft.fontRenderer.drawString(text, 10, 20, 0x8B8B8B, true);
	}

}