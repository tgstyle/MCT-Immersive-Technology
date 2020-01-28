package ferro2000.immersivetech.client.gui;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.GuiIEContainerBase;

import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntityTrashItem;
import ferro2000.immersivetech.common.gui.ContainerTrashItem;

import net.minecraft.entity.player.InventoryPlayer;

public class GuiTrashItem extends GuiIEContainerBase {
	TileEntityTrashItem tile;

	public GuiTrashItem(InventoryPlayer invPlayer, TileEntityTrashItem tile) {
		super(new ContainerTrashItem(invPlayer, tile));
		this.tile=tile;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mx, int my) {
		ClientUtils.bindTexture("immersivetech:textures/gui/gui_trash_item.png");
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
	}

}