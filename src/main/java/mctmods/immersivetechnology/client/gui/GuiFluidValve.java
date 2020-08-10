package mctmods.immersivetechnology.client.gui;

import blusunrize.immersiveengineering.client.ClientUtils;
import mctmods.immersivetechnology.common.tileentities.TileEntityFluidValve;
import mctmods.immersivetechnology.common.util.TranslationKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

public class GuiFluidValve extends GUICommonValve {

	public GuiFluidValve(TileEntityFluidValve tile) {
		this.tile = tile;
	}

	@Override
	public void initGui() {
		super.initGui();
		limitPacket = new GuiTextField(0, this.fontRenderer, width / 2 - 85, height / 2 - 13, 50, 8);
		limitPacket.setText(tile.packetLimit >= 0? String.valueOf(tile.packetLimit) : "");
		limitPacket.setFocused(true);
		limitTime = new GuiTextField(1, this.fontRenderer, width / 2 - 85, height / 2 + 5, 50, 8);
		limitTime.setText(tile.timeLimit >= 0? String.valueOf(tile.timeLimit) : "");
		destinationKeep = new GuiTextField(2, this.fontRenderer, width / 2 - 85, height / 2 + 23, 50, 8);
		destinationKeep.setText(tile.keepSize >= 0? String.valueOf(tile.keepSize) : "");
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());
		drawDefaultBackground();
		ClientUtils.bindTexture("immersivetech:textures/gui/gui_fluid_valve.png");
		drawModalRectWithCustomSizedTexture((resolution.getScaledWidth() - 216) / 2, (resolution.getScaledHeight() - 82) / 2, 0, 0, 216, 88, 216, 88);
		drawString(this.fontRenderer, TranslationKey.GUI_FLUID_VALVE_FIRSTLINE.text(), width / 2 - 85, height / 2 - 28, Color.WHITE.getRGB());
		limitPacket.drawTextBox();
		drawString(this.fontRenderer, TranslationKey.GUI_FLUID_VALVE_LIMIT_PACKET.text(), width / 2 - 30, height / 2 - 13, Color.WHITE.getRGB());
		limitTime.drawTextBox();
		drawString(this.fontRenderer, TranslationKey.GUI_FLUID_VALVE_LIMIT_TIME.text(), width / 2 - 30, height / 2 + 5, Color.WHITE.getRGB());
		destinationKeep.drawTextBox();
		drawString(this.fontRenderer, TranslationKey.GUI_FLUID_VALVE_LIMIT_DESTINATION.text(), width / 2 - 30, height / 2 + 23, Color.WHITE.getRGB());
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}