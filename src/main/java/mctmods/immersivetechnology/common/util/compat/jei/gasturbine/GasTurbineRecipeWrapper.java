package mctmods.immersivetechnology.common.util.compat.jei.gasturbine;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.common.util.compat.jei.MultiblockRecipeWrapper;
import mctmods.immersivetechnology.common.util.TranslationKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GasTurbineRecipeWrapper extends MultiblockRecipeWrapper {

    public MultiblockRecipe recipe;

    public GasTurbineRecipeWrapper(MultiblockRecipe recipe) {
        super(recipe);
        this.recipe = recipe;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        String text = (GuiScreen.isShiftKeyDown())?
                TranslationKey.GUI_TICKS.format(recipe.getTotalProcessTime()) :
                TranslationKey.GUI_SECONDS.format(((float)recipe.getTotalProcessTime()) / 20);
        minecraft.fontRenderer.drawString(text, 44, 10, 0x8B8B8B, true);
    }
}
