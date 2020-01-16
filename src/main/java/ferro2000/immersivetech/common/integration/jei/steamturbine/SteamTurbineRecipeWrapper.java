package ferro2000.immersivetech.common.integration.jei.steamturbine;

import ferro2000.immersivetech.api.crafting.SteamTurbineRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SteamTurbineRecipeWrapper implements IRecipeWrapper {

    public SteamTurbineRecipe recipe;

    public SteamTurbineRecipeWrapper(SteamTurbineRecipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInput(FluidStack.class, recipe.input);
        ingredients.setOutput(FluidStack.class, recipe.output);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        String text = I18n.format("keyword.immersivetech.duration") + " " + ((float)recipe.time)/20 + "s";
        minecraft.fontRenderer.drawString(text, 10, 62, 0x8B8B8B, true);
    }
}
