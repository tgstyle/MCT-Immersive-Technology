package mctmods.immersivetechnology.client.gui;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.GuiIEContainerBase;

import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityMeltingCrucibleMaster;
import mctmods.immersivetechnology.common.gui.ContainerMeltingCrucible;

import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.text.TextFormatting;

import org.lwjgl.opengl.GL11;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class GuiMeltingCrucible extends GuiIEContainerBase {
    TileEntityMeltingCrucibleMaster tile;

    private static final double workingHeatLevel = Multiblocks.meltingCrucible.meltingCrucible_heat_workingLevel;

    public GuiMeltingCrucible(InventoryPlayer invPlayer, TileEntityMeltingCrucibleMaster tile) {
        super(new ContainerMeltingCrucible(invPlayer, tile));
        this.tile = tile;
    }

    @Override public void drawScreen(int mx, int my, float partial) {
        super.drawScreen(mx, my, partial);

        ArrayList<String> tooltip = new ArrayList<>();

        ClientUtils.handleGuiTank(tile.tanks[0], guiLeft + 126, guiTop + 21, 15, 47, 177, 31, 20, 51, mx, my, "immersivetech:textures/gui/gui_melting_crucible.png", tooltip);

        if (mx > guiLeft + 16 && mx < guiLeft + 23 && my > guiTop + 21 && my < guiTop + 68)
            tooltip.add(tile.getEnergyStored(null) + "/" + tile.getMaxEnergyStored(null) + " RF");
        if (mx >= guiLeft + 30 && mx < guiLeft + 79 && my >= guiTop + 9 && my < guiTop + 18) {
            DecimalFormat df = new DecimalFormat("0.00");
            tooltip.add("Temperature");
            tooltip.add(TextFormatting.RED + df.format(tile.heatLevel / 20 + 30) + "/" + df.format(workingHeatLevel / 20 + 30) + "C");
        }
        if (!tooltip.isEmpty()) {
            ClientUtils.drawHoveringText(tooltip, mx, my, fontRenderer, guiLeft + xSize, -1);
            RenderHelper.enableGUIStandardItemLighting();
        }
    }

    @Override protected void drawGuiContainerBackgroundLayer(float partialTicks, int mx, int my) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        ClientUtils.bindTexture("immersivetech:textures/gui/gui_melting_crucible.png");
        this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        int stored = (int)(46 * (tile.getEnergyStored(null) / (float)tile.getMaxEnergyStored(null)));
        ClientUtils.drawGradientRect(guiLeft + 16, guiTop + 22 + (46 - stored), guiLeft + 23, guiTop + 68, 0xffb51500, 0xff600b00);

        int heatBarSize = (int)(51 * (tile.heatLevel / workingHeatLevel));
        this.drawTexturedModalRect(guiLeft + 30, guiTop + 9, 176, 0, heatBarSize, 9);

        ClientUtils.handleGuiTank(tile.tanks[0], guiLeft + 126, guiTop + 21, 16, 47, 177, 31, 20, 51, mx, my, "immersivetech:textures/gui/gui_melting_crucible.png", null);
    }
}
