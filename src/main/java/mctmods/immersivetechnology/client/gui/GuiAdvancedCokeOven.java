package mctmods.immersivetechnology.client.gui;

import com.immersiveconvergence.api.client.ICClientUtils;
import com.immersiveconvergence.api.client.gui.GuiICContainerBase;

import mctmods.immersivetechnology.common.gui.ContainerAdvancedCokeOven;
import mctmods.immersivetechnology.common.multiblocks.stone.tileentities.TileEntityAdvancedCokeOvenMaster;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public class GuiAdvancedCokeOven extends GuiICContainerBase {
    private static final String TEXTURE = "immersivetech:textures/gui/advanced_coke_oven.png";

    TileEntityAdvancedCokeOvenMaster tile;

    public GuiAdvancedCokeOven(InventoryPlayer inventoryPlayer, TileEntityAdvancedCokeOvenMaster tile) {
        super(new ContainerAdvancedCokeOven(inventoryPlayer, tile));
        this.tile = tile;
    }

    @Override public void drawScreen(int mx, int my, float partial) {
        super.drawScreen(mx, my, partial);

        ArrayList<String> tooltip = new ArrayList<>();
        ICClientUtils.handleGuiTank(tile.tank, guiLeft + 129, guiTop + 20, 16, 47, 176, 31, 20, 51, mx, my, TEXTURE, tooltip);
        if (!tooltip.isEmpty()) {
            ICClientUtils.drawHoveringText(tooltip, mx, my, fontRenderer, guiLeft + xSize, -1);
            RenderHelper.enableGUIStandardItemLighting();
        }
    }

    @Override protected void drawGuiContainerBackgroundLayer(float f, int mx, int my) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        ICClientUtils.bindTexture(TEXTURE);
        this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        if (tile.processTimeMax > 0 && tile.processTimeRemaining > 0) {
            int k = MathHelper.clamp(13 * tile.processTimeRemaining / tile.processTimeMax, 0, 13);
            this.drawTexturedModalRect(guiLeft + 59, guiTop + 36 + 13 - k, 176, 12 + (13 - k), 14, k + 1);
        }

        ICClientUtils.handleGuiTank(tile.tank, guiLeft + 129, guiTop + 20, 16, 47, 176, 31, 20, 51, mx, my, TEXTURE, null);
    }
}
