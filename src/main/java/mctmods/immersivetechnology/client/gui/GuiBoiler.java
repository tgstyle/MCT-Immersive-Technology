package mctmods.immersivetechnology.client.gui;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.GuiIEContainerBase;
import mctmods.immersivetechnology.common.Config.ITConfig.Machines.Boiler;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityBoilerMaster;
import mctmods.immersivetechnology.common.gui.ContainerBoiler;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public class GuiBoiler extends GuiIEContainerBase {
	TileEntityBoilerMaster tile;

	private static double workingHeatLevel = Boiler.boiler_heat_workingLevel;
	
	public GuiBoiler(InventoryPlayer inventoryPlayer, TileEntityBoilerMaster tile) {
		super(new ContainerBoiler(inventoryPlayer, tile));
		this.tile=tile;
	}

	@Override
	public void drawScreen(int mx, int my, float partial) {
		super.drawScreen(mx, my, partial);

		ArrayList<String> tooltip = new ArrayList<>();
		ClientUtils.handleGuiTank(tile.tanks[0], guiLeft + 13, guiTop + 20, 16, 47, 177, 31, 20, 51, mx, my, "immersivetech:textures/gui/gui_boiler.png", tooltip);
		ClientUtils.handleGuiTank(tile.tanks[1], guiLeft + 100, guiTop + 20, 16, 47, 177, 31, 20, 51, mx, my, "immersivetech:textures/gui/gui_boiler.png", tooltip);
		ClientUtils.handleGuiTank(tile.tanks[2], guiLeft + 123, guiTop + 20, 16, 47, 177, 31, 20, 51, mx, my, "immersivetech:textures/gui/gui_boiler.png", tooltip);
		if(mx >= guiLeft + 100 && mx < guiLeft + 141 && my >= guiTop + 5 && my < guiTop + 14) {
			tooltip.add("Temperature");
			tooltip.add(TextFormatting.RED + "" + ((tile.heatLevel/20) + 30) + "/" + ((workingHeatLevel/20) + 30) + "C");
		}
		if(!tooltip.isEmpty()) {
			ClientUtils.drawHoveringText(tooltip, mx, my, fontRenderer, guiLeft + xSize, - 1);
			RenderHelper.enableGUIStandardItemLighting();
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersivetech:textures/gui/gui_boiler.png");
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		int heatBarSize = (int)Math.round(41 * (tile.heatLevel / workingHeatLevel));

		this.drawTexturedModalRect(guiLeft + 100, guiTop + 5, 176, 0, heatBarSize, 9);

		ClientUtils.handleGuiTank(tile.tanks[0], guiLeft + 13, guiTop + 20, 16, 47, 177, 31, 20, 51, mx, my, "immersivetech:textures/gui/gui_boiler.png", null);
		ClientUtils.handleGuiTank(tile.tanks[1], guiLeft + 100, guiTop + 20, 16, 47, 177, 31, 20, 51, mx, my, "immersivetech:textures/gui/gui_boiler.png", null);
		ClientUtils.handleGuiTank(tile.tanks[2], guiLeft + 123, guiTop + 20, 16, 47, 177, 31, 20, 51, mx, my, "immersivetech:textures/gui/gui_boiler.png", null);
	}

}