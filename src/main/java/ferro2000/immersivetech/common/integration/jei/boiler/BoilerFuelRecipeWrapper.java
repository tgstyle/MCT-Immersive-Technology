package ferro2000.immersivetech.common.integration.jei.boiler;

import ferro2000.immersivetech.api.crafting.BoilerFuelRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BoilerFuelRecipeWrapper implements IRecipeWrapper {

    public BoilerFuelRecipe recipe;

    public BoilerFuelRecipeWrapper(BoilerFuelRecipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInput(FluidStack.class, recipe.input);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
		String text = I18n.format("keyword.immersivetech.duration") + " " + ((float)recipe.time)/20 + "s";
		minecraft.fontRenderer.drawString(text, 70, 5, 0x8B8B8B, true);
        text = I18n.format("keyword.immersivetech.generate");
        minecraft.fontRenderer.drawString(text, 70, 15, 0x8B8B8B, true);
        text =  recipe.heat + " " + I18n.format("keyword.immersivetech.heatPerTick");
        minecraft.fontRenderer.drawString(text, 70, 25, 0x8B8B8B, true);
        text = (recipe.heat * recipe.time) + " " + I18n.format("keyword.immersivetech.totalHeat");
        minecraft.fontRenderer.drawString(text, 70, 35, 0x8B8B8B, true);
    }
}
