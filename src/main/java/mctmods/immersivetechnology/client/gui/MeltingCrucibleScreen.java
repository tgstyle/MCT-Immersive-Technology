package mctmods.immersivetechnology.client.gui;

import mctmods.immersivetechnology.client.gui.helper.ITContainerScreen;
import mctmods.immersivetechnology.client.gui.helper.ITEnergyInfoArea;
import mctmods.immersivetechnology.client.gui.helper.ITFluidInfoArea;
import mctmods.immersivetechnology.client.gui.helper.ITInfoArea;
import mctmods.immersivetechnology.common.multiblocks.gui.MeltingCrucibleMenu;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.MeltingCrucibleLogic;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

public class MeltingCrucibleScreen extends ITContainerScreen<MeltingCrucibleMenu> {
    private static final ResourceLocation TEXTURE = ITLib.makeTextureLocation("melting_crucible");

    public MeltingCrucibleScreen(MeltingCrucibleMenu container, Inventory inventoryPlayer, Component title) {
        super(container, inventoryPlayer, title, TEXTURE);
    }

    @Override @Nonnull protected List<ITInfoArea> makeInfoAreas() {
        return List.of(
                new ITEnergyInfoArea(this.leftPos + 16, this.topPos + 22, menu.energy),
                new ITFluidInfoArea(menu.inputTank, new Rect2i(this.leftPos + 102, this.topPos + 21, 16, 47), 177, 31, 20, 51, TEXTURE),
                new ITFluidInfoArea(menu.outputTank, new Rect2i(this.leftPos + 126, this.topPos + 21, 16, 47), 177, 31, 20, 51, TEXTURE)
        );
    }

    @Override protected void drawContainerBackgroundPre(@Nonnull GuiGraphics graphics, float f, int mx, int my) {
        int energyStored = menu.state.get(1);
        int energyMax = menu.state.get(2);
        int stored = energyMax > 0 ? (int) (46 * (energyStored / (float) energyMax)) : 0;
        graphics.blit(TEXTURE, leftPos + 16, topPos + 22 + (46 - stored), 176, 0, 7, stored);

        int heat = menu.state.get(3);
        int heatBarSize = (int)(51 * (heat / (float) MeltingCrucibleLogic.WORKING_HEAT_LEVEL));
        graphics.blit(TEXTURE, leftPos + 30, topPos + 9, 176, 0, heatBarSize, 9);
    }

    @Override protected void gatherAdditionalTooltips(int mouseX, int mouseY, Consumer<Component> addLine, Consumer<Component> addGray) {
        if (mouseX >= leftPos + 16 && mouseX < leftPos + 23 && mouseY >= topPos + 22 && mouseY < topPos + 68) {
            addLine.accept(Component.translatable("gui.immersivetechnology.energy_stored", menu.state.get(1), menu.state.get(2)));
        }
        if (mouseX >= leftPos + 30 && mouseX < leftPos + 81 && mouseY >= topPos + 9 && mouseY < topPos + 18) {
            int internalHeat = menu.state.get(3);
            addLine.accept(Component.literal("Temperature"));
            addLine.accept(Component.literal(String.format("%.0f/%.0f C", (double)internalHeat, MeltingCrucibleLogic.WORKING_HEAT_LEVEL)));
        }
    }
}
