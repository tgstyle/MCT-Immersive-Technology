package mctmods.immersivetechnology.client.gui;

import com.google.common.collect.ImmutableList;
import mctmods.immersivetechnology.client.gui.helper.ITContainerScreen;
import mctmods.immersivetechnology.client.gui.helper.ITInfoArea;
import mctmods.immersivetechnology.common.blocks.multiblocks.gui.BoilerSolidMenu;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;
import java.util.List;

public class BoilerSolidScreen extends ITContainerScreen<BoilerSolidMenu> {
    private static final ResourceLocation TEXTURE = ITLib.makeTextureLocation("boiler_solid");

    public BoilerSolidScreen(BoilerSolidMenu container, Inventory inventoryPlayer, Component title) { super(container, inventoryPlayer, title, TEXTURE); }

    @Override
    protected void drawContainerBackgroundPre(@Nonnull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        float heatLevel = menu.getHeatLevel();
        int barWidth = Mth.clamp(Math.round(41 * heatLevel / 100.0f), 0, 41);
        graphics.blit(TEXTURE, leftPos + 119, topPos + 38, 176, 0, barWidth, 9);

        int total = menu.getTotalBurnTime();
        if (total > 0) {
            int k = (total - menu.getBurnRemaining()) * 13 / total;
            graphics.blit(TEXTURE, leftPos + 81, topPos + 35 + 13 - k, 176, 12 + (13 - k), 14, k + 1);
        }
    }

    @Nonnull
    @Override
    protected List<ITInfoArea> makeInfoAreas() {
        return ImmutableList.of(
                new ITInfoArea(new Rect2i(leftPos + 119, topPos + 38, 41, 9)) {
                    @Override
                    protected void fillTooltipOverArea(int mouseX, int mouseY, List<Component> tooltip) {
                        tooltip.add(Component.literal("Temperature"));
                        float heatLevel = menu.getHeatLevel();
                        tooltip.add(Component.literal(ChatFormatting.RED + "" + (int)heatLevel + "/100 C"));
                    }

                    @Override
                    public void draw(GuiGraphics graphics) {}
                }
        );
    }
}
