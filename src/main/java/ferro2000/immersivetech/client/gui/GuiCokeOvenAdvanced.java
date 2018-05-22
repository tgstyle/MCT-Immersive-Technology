package ferro2000.immersivetech.client.gui;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.GuiIEContainerBase;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityCokeOven;
import ferro2000.immersivetech.common.blocks.stone.tileentities.TileEntityCokeOvenAdvanced;
import ferro2000.immersivetech.common.gui.ContainerCokeOvenAdvanced;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;

public class GuiCokeOvenAdvanced extends GuiIEContainerBase
{
	TileEntityCokeOvenAdvanced tile;
	public GuiCokeOvenAdvanced(InventoryPlayer inventoryPlayer, TileEntityCokeOvenAdvanced tile)
	{
		super(new ContainerCokeOvenAdvanced(inventoryPlayer, tile));
		this.tile=tile;
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	@Override
	public void drawScreen(int mx, int my, float partial)
	{
		super.drawScreen(mx, my, partial);
		ArrayList<String> tooltip = new ArrayList<String>();
		ClientUtils.handleGuiTank(tile.tank, guiLeft+129,guiTop+20, 16,47, 176,31,20,51, mx,my, "immersiveengineering:textures/gui/coke_oven.png", tooltip);
		if(!tooltip.isEmpty())
		{
			ClientUtils.drawHoveringText(tooltip, mx, my, fontRenderer, guiLeft+xSize,-1);
			RenderHelper.enableGUIStandardItemLighting();
		}
	}


	/**
	 * Draws the background layer of this container (behind the items).
	 */
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersiveengineering:textures/gui/coke_oven.png");
		this.drawTexturedModalRect(guiLeft,guiTop, 0, 0, xSize, ySize);

		if(tile.processMax>0&&tile.process>0)
		{
			int h = (int)(12*(tile.process/(float)tile.processMax));
			this.drawTexturedModalRect(guiLeft+59,guiTop+37+12-h, 179, 1+12-h, 9, h);
		}

//		if(tile.tank.getFluid()!=null && tile.tank.getFluid().getFluid()!=null)
//		{
//			int h = (int)(47*(tile.tank.getFluid().amount/(float)tile.tank.getCapacity()));
//			ClientUtils.drawRepeatedFluidIcon(tile.tank.getFluid().getFluid(), guiLeft+129,guiTop+20+47-h, 16, h);
//			ClientUtils.bindTexture("immersiveengineering:textures/gui/cokeOven.png");
//		}
//		this.drawTexturedModalRect(guiLeft+127,guiTop+18, 176,31, 20,51);
		ClientUtils.handleGuiTank(tile.tank, guiLeft+129,guiTop+20, 16,47, 176,31,20,51, mx,my, "immersiveengineering:textures/gui/coke_oven.png", null);
		
	}
}
