package mctmods.immersivetechnology.client.gui;

import com.immersiveconvergence.ImmersiveConvergence;
import com.immersiveconvergence.api.client.gui.ICGuiButton;
import com.immersiveconvergence.api.client.ICClientUtils;
import com.immersiveconvergence.api.client.gui.GuiICContainerBase;
import com.immersiveconvergence.api.network.TileSyncMessage;

import mctmods.immersivetechnology.common.blocks.connectors.tileentities.TileEntityTimer;
import mctmods.immersivetechnology.common.gui.ContainerTimer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

public class GuiTimer extends GuiICContainerBase {
	TileEntityTimer tile;

	public GuiTimer(InventoryPlayer inventoryPlayer, TileEntityTimer tile) {
		super(new ContainerTimer(inventoryPlayer, tile));
		this.tile=tile;
	}

	@Override public void initGui() {
		super.initGui();
		this.buttonList.clear();
		this.buttonList.add(new ICGuiButton(0, guiLeft + 39, guiTop + 35, 16, 16, "+", "immersivetech:textures/gui/timer.png", 176, 0));
		this.buttonList.add(new ICGuiButton(1, guiLeft + 120, guiTop + 35, 16, 16, "-", "immersivetech:textures/gui/timer.png", 176, 16));
	}

	@Override protected void actionPerformed(@Nonnull GuiButton button) {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("buttonId", button.id);
		ImmersiveConvergence.packetHandler.sendToServer(new TileSyncMessage(tile, tag));
		this.initGui();
	}

	@Override protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		ICClientUtils.bindTexture("immersivetech:textures/gui/timer.png");
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		float time = (float)tile.getTarget() / 20;
		this.drawString(this.fontRenderer, time + " Sec.", guiLeft + 68, guiTop + 40, 0xFFFFFF);
	}
}
