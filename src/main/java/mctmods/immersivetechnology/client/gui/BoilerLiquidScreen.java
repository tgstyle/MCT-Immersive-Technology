package mctmods.immersivetechnology.client.gui;

import blusunrize.immersiveengineering.api.client.TextUtils;
import mctmods.immersivetechnology.client.gui.helper.ITContainerScreen;
import mctmods.immersivetechnology.client.gui.helper.ITFluidInfoArea;
import mctmods.immersivetechnology.client.gui.helper.ITInfoArea;
import mctmods.immersivetechnology.common.multiblocks.gui.BoilerLiquidMenu;
import mctmods.immersivetechnology.core.util.TranslationKey;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import com.google.common.collect.ImmutableList;
import javax.annotation.Nonnull;
import java.util.List;

public class BoilerLiquidScreen extends ITContainerScreen<BoilerLiquidMenu> {
    private static final ResourceLocation TEXTURE = ITLib.makeTextureLocation("boiler_liquid");

    public BoilerLiquidScreen(BoilerLiquidMenu container, Inventory inventoryPlayer, Component title) { super(container, inventoryPlayer, title, TEXTURE); }

    @Override protected void drawContainerBackgroundPre(@Nonnull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        float heatLevel = menu.getHeatLevel();
        double workingHeatLevel = menu.getWorkingHeatLevel();
        int barWidth = Mth.clamp(Math.round(41 * heatLevel / (float)workingHeatLevel), 0, 41);
        graphics.blit(TEXTURE, leftPos + 119, topPos + 38, 176, 0, barWidth, 9);
    }

    @Override @Nonnull protected List<ITInfoArea> makeInfoAreas() {
        return ImmutableList.of(
                new ITFluidInfoArea(menu.tanks.input1(), new Rect2i(leftPos + 80, topPos + 20, 16, 47), 177, 31, 20, 51, TEXTURE),
                new ITInfoArea(new Rect2i(leftPos + 119, topPos + 38, 41, 9)) {
                    @Override protected void fillTooltipOverArea(int mouseX, int mouseY, List<Component> tooltip) {
                        tooltip.add(Component.translatable(TranslationKey.GUI_TEMPERATURE.getLocation()));
                        float heatLevel = menu.getHeatLevel();
                        double maxHeat = menu.getWorkingHeatLevel();
                        tooltip.add(TextUtils.applyFormat(Component.translatable(TranslationKey.GUI_HEAT_LEVEL_DETAILED.getLocation(), (int)heatLevel, (int)maxHeat), ChatFormatting.RED));
                    }

                    @Override public void draw(GuiGraphics graphics) {}
                }
        );
    }
}
