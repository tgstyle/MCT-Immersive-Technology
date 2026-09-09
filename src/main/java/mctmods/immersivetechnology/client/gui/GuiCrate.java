package mctmods.immersivetechnology.client.gui;

import mctmods.immersivetechnology.common.blocks.wooden.tileentities.TileEntityCrate;
import mctmods.immersivetechnology.common.gui.ContainerCrate;

import com.immersiveconvergence.api.client.ICClientUtils;
import com.immersiveconvergence.api.client.gui.GuiICContainerBase;

import net.minecraft.entity.player.InventoryPlayer;

public class GuiCrate extends GuiICContainerBase {
	TileEntityCrate tile;

	public GuiCrate(InventoryPlayer invPlayer, TileEntityCrate tile) {
		super(new ContainerCrate(invPlayer, tile));
		this.tile = tile;
	}

	@Override protected void drawGuiContainerBackgroundLayer(float partialTicks, int mx, int my) {
		ICClientUtils.bindTexture("immersivetech:textures/gui/single_item.png");
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
	}
}
