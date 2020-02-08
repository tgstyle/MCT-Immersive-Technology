package mctmods.immersivetechnology.client.gui;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.GuiIEContainerBase;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntitySolarTower;
import mctmods.immersivetechnology.common.gui.ContainerSolarTower;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;

public class GuiSolarTower extends GuiIEContainerBase {
	TileEntitySolarTower tile;

	public GuiSolarTower(InventoryPlayer invPlayer, TileEntitySolarTower tile) {
		super(new ContainerSolarTower(invPlayer, tile));
		this.tile=tile;
	}

	@Override
	public void drawScreen(int mx, int my, float partial) {
		super.drawScreen(mx, my, partial);

		ArrayList<String> tooltip = new ArrayList<String>();
		ClientUtils.handleGuiTank(tile.tanks[0], guiLeft + 102, guiTop + 21, 16, 47, 177, 31, 20, 51, mx, my, "immersivetech:textures/gui/gui_solar_tower.png", tooltip);
		ClientUtils.handleGuiTank(tile.tanks[1], guiLeft + 126, guiTop + 21, 16, 47, 177, 31, 20, 51, mx, my, "immersivetech:textures/gui/gui_solar_tower.png", tooltip);
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