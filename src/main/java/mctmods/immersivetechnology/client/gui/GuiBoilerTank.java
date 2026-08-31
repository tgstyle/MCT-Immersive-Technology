package mctmods.immersivetechnology.client.gui;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.GuiIEContainerBase;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityBoilerTankMaster;
import mctmods.immersivetechnology.common.gui.ContainerBoilerTank;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public class GuiBoilerTank extends GuiIEContainerBase {
	private static final String TEXTURE = "immersivetech:textures/gui/gui_boiler_tank.png";
	TileEntityBoilerTankMaster tile;

	public GuiBoilerTank(InventoryPlayer inventoryPlayer, TileEntityBoilerTankMaster tile) {
		super(new ContainerBoilerTank(inventoryPlayer, tile));
		this.tile = tile;
	}

	@Override public void drawScreen(int mx, int my, float partial) {
		super.drawScreen(mx, my, partial);

		ArrayList<String> tooltip = new ArrayList<>();
		ClientUtils.handleGuiTank(tile.tanks[0], guiLeft + 67, guiTop + 20, 16, 47, 177, 31, 20, 51, mx, my, TEXTURE, tooltip);
		ClientUtils.handleGuiTank(tile.tanks[1], guiLeft + 92, guiTop + 20, 16, 47, 177, 31, 20, 51, mx, my, TEXTURE, tooltip);
		if (mx >= guiLeft + 67 && mx < guiLeft + 108 && my >= guiTop + 5 && my < guiTop + 14) {
			tooltip.add("Temperature");
			tooltip.add(TextFormatting.RED + "" + (int)tile.heatLevel + "/" + (int)tile.workingHeatLevel);
		}
		if (!tooltip.isEmpty()) {
			ClientUtils.drawHoveringText(tooltip, mx, my, fontRenderer, guiLeft + xSize, - 1);
			RenderHelper.enableGUIStandardItemLighting();
		}
	}

	@Override protected void drawGuiContainerBackgroundLayer(float f, int mx, int my) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture(TEXTURE);
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		int heatBarSize = MathHelper.clamp((int)Math.round(41 * (tile.heatLevel / tile.workingHeatLevel)), 0, 41);
		this.drawTexturedModalRect(guiLeft + 67, guiTop + 5, 176, 0, heatBarSize, 9);

		ClientUtils.handleGuiTank(tile.tanks[0], guiLeft + 67, guiTop + 20, 16, 47, 177, 31, 20, 51, mx, my, TEXTURE, null);
		ClientUtils.handleGuiTank(tile.tanks[1], guiLeft + 92, guiTop + 20, 16, 47, 177, 31, 20, 51, mx, my, TEXTURE, null);
	}
}
