package ferro2000.immersivetech.client.gui;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntitySolarTower;
import ferro2000.immersivetech.common.gui.ContainerSolarTower;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;

public class GuiSolarTower extends GuiContainer {
	
	TileEntitySolarTower tile;

	public GuiSolarTower(InventoryPlayer invPlayer, TileEntitySolarTower tile) {
		super(new ContainerSolarTower(invPlayer, tile));
		this.tile=tile;
	}
	
	@Override
	public void drawScreen(int mx, int my, float partial)
	{
		super.drawScreen(mx, my, partial);
		
		ArrayList<String> tooltip = new ArrayList();
		ClientUtils.handleGuiTank(tile.tanks[0], guiLeft+102,guiTop+21, 16,47, 177,31,20,51, mx,my, "immersivetech:textures/gui/gui_solar_tower.png", tooltip);
		ClientUtils.handleGuiTank(tile.tanks[1], guiLeft+126,guiTop+21, 16,47, 177,31,20,51, mx,my, "immersivetech:textures/gui/gui_solar_tower.png", tooltip);
		
		if(!tooltip.isEmpty())
		{
			ClientUtils.drawHoveringText(tooltip, mx, my, fontRenderer, guiLeft+xSize,-1);
			RenderHelper.enableGUIStandardItemLighting();
		}
		
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mx, int my) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersivetech:textures/gui/gui_solar_tower.png");
		this.drawTexturedModalRect(guiLeft,guiTop, 0, 0, xSize, ySize);
		
		if(tile.ref0==1) {
			this.drawTexturedModalRect(guiLeft+32, guiTop+24, 198, 31, 10, 10);
		}
		if(tile.ref1==1) {
			this.drawTexturedModalRect(guiLeft+16, guiTop+40, 198, 31, 10, 10);
		}
		if(tile.ref2==1) {
			this.drawTexturedModalRect(guiLeft+32, guiTop+56, 198, 31, 10, 10);
		}
		if(tile.ref3==1) {
			this.drawTexturedModalRect(guiLeft+48, guiTop+40, 198, 31, 10, 10);
		}
		
		ClientUtils.handleGuiTank(tile.tanks[0], guiLeft+102,guiTop+21, 16,47, 177,31,20,51, mx,my, "immersivetech:textures/gui/gui_solar_tower.png", null);
		ClientUtils.handleGuiTank(tile.tanks[1], guiLeft+126,guiTop+21, 16,47, 177,31,20,51, mx,my, "immersivetech:textures/gui/gui_solar_tower.png", null);
	}

}