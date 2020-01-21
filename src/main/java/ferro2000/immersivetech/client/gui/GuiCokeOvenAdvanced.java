package ferro2000.immersivetech.client.gui;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.GuiIEContainerBase;

import ferro2000.immersivetech.common.blocks.stone.tileentities.TileEntityCokeOvenAdvanced;
import ferro2000.immersivetech.common.gui.ContainerCokeOvenAdvanced;

import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;

public class GuiCokeOvenAdvanced extends GuiIEContainerBase {
	TileEntityCokeOvenAdvanced tile;

	public GuiCokeOvenAdvanced(InventoryPlayer inventoryPlayer, TileEntityCokeOvenAdvanced tile) {
		super(new ContainerCokeOvenAdvanced(inventoryPlayer, tile));
		this.tile=tile;
	}

	@Override
	public void drawScreen(int mx, int my, float partial) {
		super.drawScreen(mx, my, partial);

		ArrayList<String> tooltip = new ArrayList<String>();
		ClientUtils.handleGuiTank(tile.tank, guiLeft + 129, guiTop + 20, 16, 47, 176, 31, 20, 51, mx, my, "immersiveengineering:textures/gui/coke_oven.png", tooltip);
		if(!tooltip.isEmpty()) {
			ClientUtils.drawHoveringText(tooltip, mx, my, fontRenderer, guiLeft + xSize, - 1);
			RenderHelper.enableGUIStandardItemLighting();
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersiveengineering:textures/gui/coke_oven.png");
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		if(tile.processMax>0 && tile.process>0) {
			int h = (int)(12 * (tile.process / (float)tile.processMax));
			this.drawTexturedModalRect(guiLeft + 59, guiTop + 37 + 12 - h, 179, 1 + 12 - h, 9, h);
		}
		ClientUtils.handleGuiTank(tile.tank, guiLeft + 129, guiTop + 20, 16, 47, 176, 31, 20, 51, mx, my, "immersiveengineering:textures/gui/coke_oven.png", null);
	}

}