package mctmods.immersivetechnology.client.gui;

import com.google.common.collect.ImmutableList;
import mctmods.immersivetechnology.client.gui.helper.ITContainerScreen;
import mctmods.immersivetechnology.client.gui.helper.ITFluidInfoArea;
import mctmods.immersivetechnology.client.gui.helper.ITInfoArea;
import mctmods.immersivetechnology.common.multiblocks.gui.AdvancedCokeOvenMenu;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;
import java.util.List;

public class AdvancedCokeOvenScreen extends ITContainerScreen<AdvancedCokeOvenMenu> {
    private static final ResourceLocation TEXTURE = ITLib.makeTextureLocation("immersiveengineering", "coke_oven");

    public AdvancedCokeOvenScreen(AdvancedCokeOvenMenu container, Inventory inventoryPlayer, Component title) { super(container, inventoryPlayer, title, TEXTURE); }

    @Override protected void drawContainerBackgroundPre(@Nonnull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int processMax = menu.getMaxProcessTime();
        int process = menu.getRemainingProcessTime();
        if (processMax > 0 && process > 0) {
            int h = (int) (12 * (process / (float) processMax));
            graphics.blit(TEXTURE, leftPos + 59, topPos + 37 + 12 - h, 179, 1 + 12 - h, 9, h);
        }
    }

    @Override @Nonnull protected List<ITInfoArea> makeInfoAreas() {
        return ImmutableList.of(new ITFluidInfoArea(menu.tanks.output(), new Rect2i(leftPos + 129, topPos + 20, 16, 47), 176, 31, 20, 51, TEXTURE));
    }
}
