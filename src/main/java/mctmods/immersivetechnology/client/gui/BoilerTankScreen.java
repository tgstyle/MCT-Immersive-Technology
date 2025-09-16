package mctmods.immersivetechnology.client.gui;

import mctmods.immersivetechnology.client.gui.helper.ITContainerScreen;
import mctmods.immersivetechnology.client.gui.helper.ITFluidInfoArea;
import mctmods.immersivetechnology.client.gui.helper.ITInfoArea;
import mctmods.immersivetechnology.common.blocks.multiblocks.gui.BoilerTankMenu;
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

public class BoilerTankScreen extends ITContainerScreen<BoilerTankMenu> {
    private static final ResourceLocation TEXTURE = ITLib.makeTextureLocation("boiler_tank");

    public BoilerTankScreen(BoilerTankMenu container, Inventory inventoryPlayer, Component title) { super(container, inventoryPlayer, title, TEXTURE); }

    @Override
    protected void drawContainerBackgroundPre(@Nonnull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        float heatLevel = menu.getHeatLevel();
        int barWidth = Mth.clamp(Math.round(41 * heatLevel / 100.0f), 0, 41);
        graphics.blit(TEXTURE, leftPos + 67, topPos + 5, 176, 0, barWidth, 9);
    }

    @Nonnull
    @Override
    protected List<ITInfoArea> makeInfoAreas() {
        return ImmutableList.of(
                new ITFluidInfoArea(menu.tanks.input(), new Rect2i(leftPos + 67, topPos + 20, 16, 47), 177, 31, 20, 51, TEXTURE),
                new ITFluidInfoArea(menu.tanks.output(), new Rect2i(leftPos + 92, topPos + 20, 16, 47), 177, 31, 20, 51, TEXTURE),
                new ITInfoArea(new Rect2i(leftPos + 67, topPos + 5, 41, 9)) {
                    @Override
                    protected void fillTooltipOverArea(int mouseX, int mouseY, List<Component> tooltip) {
                        tooltip.add(Component.literal("Temperature"));
                        float heatLevel = menu.getHeatLevel();
                        tooltip.add(Component.literal(ChatFormatting.RED + "" + (int)heatLevel + "/100 C"));
                    }

                    @Override
                    public void draw(GuiGraphics graphics) {
                        // No drawing needed, as the bar is drawn in drawContainerBackgroundPre
                    }
                }
        );
    }
}
