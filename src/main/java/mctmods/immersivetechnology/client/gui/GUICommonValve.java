package mctmods.immersivetechnology.client.gui;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.common.tileentities.TileEntityCommonValve;
import mctmods.immersivetechnology.common.util.network.MessageTileSync;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.Arrays;

public abstract class GUICommonValve extends GuiScreen {

	GuiTextField limitPacket;
	GuiTextField limitTime;
	GuiTextField destinationKeep;
	TileEntityCommonValve tile;

	static int[] acceptedKeys = new int[] {
			Keyboard.KEY_NUMPAD0,
			Keyboard.KEY_NUMPAD1,
			Keyboard.KEY_NUMPAD2,
			Keyboard.KEY_NUMPAD3,
			Keyboard.KEY_NUMPAD4,
			Keyboard.KEY_NUMPAD5,
			Keyboard.KEY_NUMPAD6,
			Keyboard.KEY_NUMPAD7,
			Keyboard.KEY_NUMPAD8,
			Keyboard.KEY_NUMPAD9,
			Keyboard.KEY_0,
			Keyboard.KEY_1,
			Keyboard.KEY_2,
			Keyboard.KEY_3,
			Keyboard.KEY_4,
			Keyboard.KEY_5,
			Keyboard.KEY_6,
			Keyboard.KEY_7,
			Keyboard.KEY_8,
			Keyboard.KEY_9,
			Keyboard.KEY_BACK,
			Keyboard.KEY_DELETE,
			Keyboard.KEY_LEFT,
			Keyboard.KEY_RIGHT
	};

	public static int safeStringToInt(String string) {
		try {
			return Integer.parseInt(string);
		} catch(NumberFormatException e) {
			return 0;
		}
	}

	@Override
	public void onGuiClosed() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("packetLimit", !limitPacket.getText().equals("") ? safeStringToInt(limitPacket.getText()) : -1);
		tag.setInteger("timeLimit", !limitTime.getText().equals("") ? safeStringToInt(limitTime.getText()) : -1);
		tag.setInteger("keepSize", !destinationKeep.getText().equals("") ? safeStringToInt(destinationKeep.getText()) : -1);
		ImmersiveTechnology.packetHandler.sendToServer(new MessageTileSync(tile, tag));
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		limitPacket.mouseClicked(mouseX, mouseY, mouseButton);
		limitTime.mouseClicked(mouseX, mouseY, mouseButton);
		destinationKeep.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if(keyCode == Keyboard.KEY_TAB) {
			if(limitPacket.isFocused()) {
				limitPacket.setFocused(false);
				limitTime.setFocused(true);
				destinationKeep.setFocused(false);
			} else if(limitTime.isFocused()) {
				limitPacket.setFocused(false);
				limitTime.setFocused(false);
				destinationKeep.setFocused(true);
			} else {
				limitPacket.setFocused(true);
				limitTime.setFocused(false);
				destinationKeep.setFocused(false);
			}
		} else if(keyCode == Keyboard.KEY_E || keyCode == Keyboard.KEY_ESCAPE) {
			super.keyTyped(typedChar, 1);
		} else if(Arrays.stream(acceptedKeys).anyMatch(x -> x == keyCode)) {
			limitPacket.textboxKeyTyped(typedChar, keyCode);
			limitTime.textboxKeyTyped(typedChar, keyCode);
			destinationKeep.textboxKeyTyped(typedChar, keyCode);
		}
	}

}