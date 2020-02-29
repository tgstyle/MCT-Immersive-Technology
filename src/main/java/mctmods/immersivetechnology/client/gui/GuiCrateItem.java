package mctmods.immersivetechnology.client.gui;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.GuiIEContainerBase;
import mctmods.immersivetechnology.common.blocks.wooden.tileentities.TileEntityCrate;
import mctmods.immersivetechnology.common.gui.ContainerCrateItem;
import net.minecraft.entity.player.InventoryPlayer;

public class GuiCrateItem extends GuiIEContainerBase {
	TileEntityCrate tile;

	public GuiCrateItem(InventoryPlayer invPlayer, TileEntityCrate tile) {
		super(new ContainerCrateItem(invPlayer, tile));
		this.tile=tile;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mx, int my) {
		ClientUtils.bindTexture("immersivetech:textures/gui/gui_single_item.png");
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
	}

}