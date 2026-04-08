package mctmods.immersivetechnology.client.gui;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.GuiIEContainerBase;
import mctmods.immersivetechnology.common.gui.ContainerAdvancedCokeOven;
import mctmods.immersivetechnology.common.multiblocks.stone.tileentities.TileEntityAdvancedCokeOvenMaster;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public class GuiAdvancedCokeOven extends GuiIEContainerBase {
    TileEntityAdvancedCokeOvenMaster tile;

    public GuiAdvancedCokeOven(InventoryPlayer inventoryPlayer, TileEntityAdvancedCokeOvenMaster tile) {
        super(new ContainerAdvancedCokeOven(inventoryPlayer, tile));
        this.tile = tile;
    }

    @Override public void drawScreen(int mx, int my, float partial) {
        super.drawScreen(mx, my, partial);

        ArrayList<String> tooltip = new ArrayList<>();
        ClientUtils.handleGuiTank(tile.tank, guiLeft + 129, guiTop + 20, 16, 47, 176, 31, 20, 51, mx, my, "immersiveengineering:textures/gui/coke_oven.png", tooltip);
        if (!tooltip.isEmpty()) {
            ClientUtils.drawHoveringText(tooltip, mx, my, fontRenderer, guiLeft + xSize, -1);
            RenderHelper.enableGUIStandardItemLighting();
        }
    }

    @Override protected void drawGuiContainerBackgroundLayer(float f, int mx, int my) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        ClientUtils.bindTexture("immersiveengineering:textures/gui/coke_oven.png");
        this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        if (tile.processTimeMax > 0 && (tile.processTimeMax - tile.processTimeRemaining) > 0) {
            int h = (int)(12 * ((tile.processTimeMax - tile.processTimeRemaining) / (float)tile.processTimeMax));
            this.drawTexturedModalRect(guiLeft + 59, guiTop + 37 + 12 - h, 179, 1 + 12 - h, 9, h);
        }

        ClientUtils.handleGuiTank(tile.tank, guiLeft + 129, guiTop + 20, 16, 47, 176, 31, 20, 51, mx, my, "immersiveengineering:textures/gui/coke_oven.png", null);
    }
}
