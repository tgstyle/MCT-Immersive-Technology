package mctmods.immersivetechnology.common.util.compat.jei;

import mctmods.immersivetechnology.common.util.TranslationKey;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.common.util.compat.jei.MultiblockRecipeWrapper;
import java.text.DecimalFormat;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class ITMultiblockRecipeWrapper extends MultiblockRecipeWrapper {
    public ITMultiblockRecipeWrapper(MultiblockRecipe recipe) { super(recipe); }

    protected static String getTimeText(int ticks) { return GuiScreen.isShiftKeyDown() ? TranslationKey.GUI_TICKS.format(ticks) : TranslationKey.GUI_SECONDS.format(((float)ticks) / 20); }

    protected static String getTimeText(float ticks, DecimalFormat format) { return GuiScreen.isShiftKeyDown() ? TranslationKey.GUI_TICKS.format(Math.round(ticks)) : TranslationKey.GUI_SECONDS.format(format.format(ticks / 20)); }

    @SideOnly(Side.CLIENT) protected void drawTimeText(@Nonnull Minecraft minecraft, int ticks, int x, int y) { minecraft.fontRenderer.drawString(getTimeText(ticks), x, y, 0x8B8B8B, true); }

    @SideOnly(Side.CLIENT) protected void drawTimeText(@Nonnull Minecraft minecraft, float ticks, DecimalFormat format, int x, int y) { minecraft.fontRenderer.drawString(getTimeText(ticks, format), x, y, 0x8B8B8B, true); }
}
