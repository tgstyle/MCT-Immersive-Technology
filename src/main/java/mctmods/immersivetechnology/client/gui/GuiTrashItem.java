package mctmods.immersivetechnology.client.gui;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.GuiIEContainerBase;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityTrashItem;
import mctmods.immersivetechnology.common.gui.ContainerTrashItem;
import net.minecraft.entity.player.InventoryPlayer;

public class GuiTrashItem extends GuiIEContainerBase {
	TileEntityTrashItem tile;

	public GuiTrashItem(InventoryPlayer invPlayer, TileEntityTrashItem tile) {
		super(new ContainerTrashItem(invPlayer, tile));
		this.tile=tile;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mx, int my) {
		ClientUtils.bindTexture("immersivetech:textures/gui/gui_single_item.png");
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
	}

}