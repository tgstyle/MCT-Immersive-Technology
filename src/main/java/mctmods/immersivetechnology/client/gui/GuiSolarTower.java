package mctmods.immersivetechnology.client.gui;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.GuiIEContainerBase;
import mctmods.immersivetechnology.common.Config.ITConfig.Machines.SolarTower;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntitySolarTowerMaster;
import mctmods.immersivetechnology.common.gui.ContainerSolarTower;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class GuiSolarTower extends GuiIEContainerBase {
	TileEntitySolarTowerMaster tile;
	
	private static double workingHeatLevel = SolarTower.solarTower_heat_workingLevel;

	public GuiSolarTower(InventoryPlayer invPlayer, TileEntitySolarTowerMaster tile) {
		super(new ContainerSolarTower(invPlayer, tile));
		this.tile=tile;
	}

	@Override
	public void drawScreen(int mx, int my, float partial) {
		super.drawScreen(mx, my, partial);

		ArrayList<String> tooltip = new ArrayList<String>();
		ClientUtils.handleGuiTank(tile.tanks[0], guiLeft + 102, guiTop + 21, 16, 47, 177, 31, 20, 51, mx, my, "immersivetech:textures/gui/gui_solar_tower.png", tooltip);
		ClientUtils.handleGuiTank(tile.tanks[1], guiLeft + 126, guiTop + 21, 16, 47, 177, 31, 20, 51, mx, my, "immersivetech:textures/gui/gui_solar_tower.png", tooltip);
		if(mx >= guiLeft + 16 && mx < guiLeft + 58 && my >= guiTop + 9 && my < guiTop + 17) {
			DecimalFormat df = new DecimalFormat("#.##");
			double heatLevel = Double.parseDouble(df.format(tile.heatLevel));
			tooltip.add("Heat Level");
			tooltip.add(TextFormatting.RED + "" + heatLevel + "/" + workingHeatLevel);
		}
		if(!tooltip.isEmpty()) {
			ClientUtils.drawHoveringText(tooltip, mx, my, fontRenderer, guiLeft + xSize, -1);
			RenderHelper.enableGUIStandardItemLighting();
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mx, int my) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersivetech:textures/gui/gui_solar_tower.png");
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		int heatBarSize = (int)Math.round(42 * (tile.heatLevel / workingHeatLevel));

		this.drawTexturedModalRect(guiLeft + 16, guiTop + 9, 176, 0, heatBarSize, 9);

		if(tile.reflectors[0] > 0) {
			this.drawTexturedModalRect(guiLeft + 32, guiTop + 24, 198, 31, 10, 10);
		}
		if(tile.reflectors[1] > 0) {
			this.drawTexturedModalRect(guiLeft + 16, guiTop + 40, 198, 31, 10, 10);
		}
		if(tile.reflectors[2] > 0) {
			this.drawTexturedModalRect(guiLeft + 32, guiTop + 56, 198, 31, 10, 10);
		}
		if(tile.reflectors[3] > 0) {
			this.drawTexturedModalRect(guiLeft + 48, guiTop + 40, 198, 31, 10, 10);
		}
		ClientUtils.handleGuiTank(tile.tanks[0], guiLeft + 102, guiTop + 21, 16, 47, 177, 31, 20, 51, mx, my, "immersivetech:textures/gui/gui_solar_tower.png", null);
		ClientUtils.handleGuiTank(tile.tanks[1], guiLeft + 126, guiTop + 21, 16, 47, 177, 31, 20, 51, mx, my, "immersivetech:textures/gui/gui_solar_tower.png", null);
	}

}