package mctmods.immersivetechnology.client.gui;

import com.google.common.collect.ImmutableList;
import mctmods.immersivetechnology.client.gui.helper.ITContainerScreen;
import mctmods.immersivetechnology.client.gui.helper.ITFluidInfoArea;
import mctmods.immersivetechnology.client.gui.helper.ITInfoArea;
import mctmods.immersivetechnology.common.blocks.multiblocks.gui.SolarTowerMenu;
import mctmods.immersivetechnology.common.blocks.multiblocks.logic.SolarTowerLogic;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

public class SolarTowerScreen extends ITContainerScreen<SolarTowerMenu> {
    private static final ResourceLocation TEXTURE = ITLib.makeTextureLocation("solar_tower");
    private static final double WORKING_HEAT_LEVEL = SolarTowerLogic.WORKING_HEAT_LEVEL;

    public SolarTowerScreen(SolarTowerMenu container, Inventory inventoryPlayer, Component title) { super(container, inventoryPlayer, title, TEXTURE); }

    @Nonnull
    @Override
    protected List<ITInfoArea> makeInfoAreas() {
        return ImmutableList.of(
                new ITFluidInfoArea(menu.inputTank, new Rect2i(leftPos + 102, topPos + 21, 16, 47), 177, 31, 20, 51, TEXTURE),
                new ITFluidInfoArea(menu.outputTank, new Rect2i(leftPos + 126, topPos + 21, 16, 47), 177, 31, 20, 51, TEXTURE)
        );
    }

    @Override
    protected void drawContainerBackgroundPre(@Nonnull GuiGraphics graphics, float f, int mx, int my) {
        int heatLevel = menu.state.get(0);
        int heatBarSize = (int) Math.round(42 * (heatLevel / WORKING_HEAT_LEVEL));
        graphics.blit(TEXTURE, leftPos + 16, topPos + 9, 176, 0, heatBarSize, 9);
        int section = menu.state.get(1);
        if (section > 0) { graphics.blit(TEXTURE, leftPos + 32, topPos + 24, 198, 31, 10, 10); }
        if (section > 1) { graphics.blit(TEXTURE, leftPos + 16, topPos + 40, 198, 31, 10, 10); }
        if (section > 3) { graphics.blit(TEXTURE, leftPos + 32, topPos + 56, 198, 31, 10, 10); }
        if (section > 2) { graphics.blit(TEXTURE, leftPos + 48, topPos + 40, 198, 31, 10, 10); }
    }

    @Override
    protected void gatherAdditionalTooltips(int mouseX, int mouseY, Consumer<Component> addLine, Consumer<Component> addGray) {
        if (mouseX >= leftPos + 16 && mouseX < leftPos + 58 && mouseY >= topPos + 9 && mouseY < topPos + 17) {
            double heat = menu.state.get(0) / 20.0 + 30;
            double maxHeat = WORKING_HEAT_LEVEL / 20.0 + 30;
            addLine.accept(Component.literal("Temperature"));
            addLine.accept(Component.literal(String.format("%.2f/%.2fC", heat, maxHeat)).withStyle(ChatFormatting.RED));
        }
    }
}
