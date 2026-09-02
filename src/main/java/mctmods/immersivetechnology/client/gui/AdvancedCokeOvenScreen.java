package mctmods.immersivetechnology.client.gui;

import com.google.common.collect.ImmutableList;
import com.immersiveconvergence.api.client.gui.BaseContainerScreen;
import com.immersiveconvergence.api.client.gui.GuiFluidArea;
import com.immersiveconvergence.api.client.gui.GuiInfoArea;
import mctmods.immersivetechnology.common.multiblocks.gui.AdvancedCokeOvenMenu;
import mctmods.immersivetechnology.core.lib.Reference;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;
import java.util.List;

public class AdvancedCokeOvenScreen extends BaseContainerScreen<AdvancedCokeOvenMenu> {
    private static final ResourceLocation TEXTURE = Reference.makeTextureLocation("immersiveengineering", "coke_oven");

    public AdvancedCokeOvenScreen(AdvancedCokeOvenMenu container, Inventory inventoryPlayer, Component title) { super(container, inventoryPlayer, title, TEXTURE); }

    @Override protected void drawContainerBackgroundPre(@Nonnull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int processMax = menu.getMaxProcessTime();
        int process = menu.getRemainingProcessTime();
        if (processMax > 0 && process > 0) {
            int h = (int) (12 * (process / (float) processMax));
            graphics.blit(TEXTURE, leftPos + 59, topPos + 37 + 12 - h, 179, 1 + 12 - h, 9, h);
        }
    }

    @Override @Nonnull protected List<GuiInfoArea> makeInfoAreas() {
        return ImmutableList.of(new GuiFluidArea(menu.tanks.output(), new Rect2i(leftPos + 129, topPos + 20, 16, 47), 176, 31, 20, 51, TEXTURE));
    }
}
