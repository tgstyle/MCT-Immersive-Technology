package mctmods.immersivetechnology.client.gui;

import blusunrize.immersiveengineering.api.IEApi;
import com.google.common.collect.ImmutableList;
import mctmods.immersivetechnology.client.gui.helper.ContainerScreen;
import mctmods.immersivetechnology.client.gui.helper.FluidInfoArea;
import mctmods.immersivetechnology.client.gui.helper.InfoArea;
import mctmods.immersivetechnology.common.multiblocks.gui.AdvancedCokeOvenMenu;
import mctmods.immersivetechnology.core.lib.Reference;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;
import java.util.List;

public class AdvancedCokeOvenScreen extends ContainerScreen<AdvancedCokeOvenMenu> {
    private static final ResourceLocation TEXTURE = Reference.makeTextureLocation("immersiveengineering", "coke_oven");
    private static final ResourceLocation TANK = IEApi.ieLoc("coke_oven/tank_overlay");
    private static final ResourceLocation FLAME = IEApi.ieLoc("coke_oven/flame");

    public AdvancedCokeOvenScreen(AdvancedCokeOvenMenu container, Inventory inventoryPlayer, Component title) { super(container, inventoryPlayer, title, TEXTURE); }

    @Override protected void drawContainerBackgroundPre(@Nonnull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int processMax = menu.getMaxProcessTime();
        int process = menu.getRemainingProcessTime();
        if (processMax > 0 && process > 0) {
            int h = (int) (12 * (process / (float) processMax));
            graphics.blitSprite(FLAME, 9, 12, 0, 12 - h, leftPos + 59, topPos + 37 + 12 - h, 9, h);
        }
    }

    @Override @Nonnull protected List<InfoArea> makeInfoAreas() { return ImmutableList.of( new FluidInfoArea(menu.tanks.output(), new Rect2i(leftPos + 129, topPos + 20, 16, 47), 20, 51, TANK) ); }
}
