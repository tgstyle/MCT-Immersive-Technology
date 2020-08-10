package mctmods.immersivetechnology.common.util.compat.jei;

import mctmods.immersivetechnology.common.util.TranslationKey;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.util.ITooltipFlag;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class GenericMultiblockRenderer implements IIngredientRenderer<GenericMultiblockIngredient> {

    public static GenericMultiblockRenderer INSTANCE = new GenericMultiblockRenderer();

    @Override
    public void render(Minecraft minecraft, int xPosition, int yPosition, @Nullable GenericMultiblockIngredient genericMultiblockIngredient) {
        RenderHelper.enableGUIStandardItemLighting();
        minecraft.getRenderItem().renderItemAndEffectIntoGUI(null, genericMultiblockIngredient.renderStack, xPosition, yPosition);
        GlStateManager.disableBlend();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.color(1, 1, 1, 1);
    }

    @Override
    public List<String> getTooltip(Minecraft minecraft, GenericMultiblockIngredient ingredient, ITooltipFlag tooltipFlag) {
        return Arrays.asList(ingredient.renderStack.getDisplayName(), TranslationKey.GUI_GENERIC_MULTIBLOCK_TOOLTIP.text());
    }

}