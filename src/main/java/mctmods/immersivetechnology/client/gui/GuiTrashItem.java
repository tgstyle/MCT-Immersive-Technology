package mctmods.immersivetechnology.client.gui;

import com.immersiveconvergence.api.client.ICClientUtils;
import com.immersiveconvergence.api.client.gui.GuiICContainerBase;

import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityTrashItem;
import mctmods.immersivetechnology.common.gui.ContainerTrashItem;
import net.minecraft.entity.player.InventoryPlayer;

public class GuiTrashItem extends GuiICContainerBase {
	TileEntityTrashItem tile;

	public GuiTrashItem(InventoryPlayer invPlayer, TileEntityTrashItem tile) {
		super(new ContainerTrashItem(invPlayer, tile));
		this.tile=tile;
	}

	@Override protected void drawGuiContainerBackgroundLayer(float partialTicks, int mx, int my) {
		ICClientUtils.bindTexture("immersivetech:textures/gui/single_item.png");
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
	}
}
