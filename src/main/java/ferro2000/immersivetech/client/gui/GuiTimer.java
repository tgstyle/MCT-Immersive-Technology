package ferro2000.immersivetech.client.gui;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.GuiIEContainerBase;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonIE;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonState;
import ferro2000.immersivetech.ImmersiveTech;
import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntityTimer;
import ferro2000.immersivetech.common.gui.ContainerTimer;
import ferro2000.immersivetech.common.network.TileMessage;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class GuiTimer extends GuiIEContainerBase {

	TileEntityTimer tile;
	
	public GuiTimer(InventoryPlayer inventoryPlayer, TileEntityTimer tile)
	{
		super(new ContainerTimer(inventoryPlayer, tile));
		this.tile=tile;
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		this.buttonList.clear();
		this.buttonList.add(new GuiButtonIE(0, guiLeft+39,guiTop+35, 16,16, "+", "immersivetech:textures/gui/gui_timer.png", 176, 0));
		this.buttonList.add(new GuiButtonIE(1, guiLeft+120,guiTop+35, 16,16, "-", "immersivetech:textures/gui/gui_timer.png", 176, 16));
		
	}
	
	@Override
	protected void actionPerformed(GuiButton button)
	{
		
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("buttonId", button.id);
		ImmersiveTech.packetHandler.sendToServer(new TileMessage(tile, tag));
		this.initGui();
		
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersivetech:textures/gui/gui_timer.png");
		this.drawTexturedModalRect(guiLeft,guiTop, 0, 	0, xSize, ySize);
		
		float time = (float)tile.getTarget()/20;
		this.drawString(this.fontRenderer, String.valueOf(time).toString()+" Sec.", guiLeft+68, guiTop+40, 0xFFFFFF);
		
	}

}
