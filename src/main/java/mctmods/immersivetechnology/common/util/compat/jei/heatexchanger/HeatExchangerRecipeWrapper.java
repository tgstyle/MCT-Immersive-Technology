package mctmods.immersivetechnology.common.util.compat.jei.heatexchanger;

import mctmods.immersivetechnology.common.util.TranslationKey;
import mctmods.immersivetechnology.common.util.compat.jei.ITMultiblockRecipeWrapper;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class HeatExchangerRecipeWrapper extends ITMultiblockRecipeWrapper {
    public MultiblockRecipe recipe;

    public HeatExchangerRecipeWrapper(MultiblockRecipe recipe) {
        super(recipe);
        this.recipe = recipe;
    }

    @Override @SideOnly(Side.CLIENT) public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        drawTimeText(minecraft, recipe.getTotalProcessTime(), 74, 8);
        minecraft.fontRenderer.drawString(TranslationKey.GUI_IF_PER_TICK.format(recipe.getTotalProcessEnergy()/recipe.getTotalProcessTime()),61, 20, 0x8B8B8B, true);
    }
}
