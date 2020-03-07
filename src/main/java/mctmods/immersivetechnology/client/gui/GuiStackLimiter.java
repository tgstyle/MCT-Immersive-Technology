package mctmods.immersivetechnology.client.gui;

import blusunrize.immersiveengineering.client.ClientUtils;
import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.common.blocks.metal.TileEntityStackLimiter;
import mctmods.immersivetechnology.common.util.TranslationKey;
import mctmods.immersivetechnology.common.util.network.MessageTileSync;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;

public class GuiStackLimiter extends GuiScreen {

    TileEntityStackLimiter tile;
    GuiTextField limitPacket;
    GuiTextField limitTime;
    GuiTextField destinationKeep;

    public GuiStackLimiter(TileEntityStackLimiter tile) {
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
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());
        drawDefaultBackground();
        ClientUtils.bindTexture("immersivetech:textures/gui/gui_fluid_valve.png");
        drawModalRectWithCustomSizedTexture((resolution.getScaledWidth() - 216) / 2, (resolution.getScaledHeight() - 82) / 2, 0, 0, 216, 88, 216, 88);
        drawString(this.fontRenderer, TranslationKey.GUI_FLUID_VALVE_FIRSTLINE.text(), width / 2 - 85, height / 2 - 28, Color.WHITE.getRGB());
        limitPacket.drawTextBox();
        drawString(this.fontRenderer, TranslationKey.GUI_STACK_LIMITER_LIMIT_PACKET.text(), width / 2 - 30, height / 2 - 13, Color.WHITE.getRGB());
        limitTime.drawTextBox();
        drawString(this.fontRenderer, TranslationKey.GUI_STACK_LIMITER_LIMIT_TIME.text(), width / 2 - 30, height / 2 + 5, Color.WHITE.getRGB());
        destinationKeep.drawTextBox();
        drawString(this.fontRenderer, TranslationKey.GUI_STACK_LIMITER_LIMIT_DESTINATION.text(), width / 2 - 30, height / 2 + 23, Color.WHITE.getRGB());
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        limitPacket.mouseClicked(mouseX, mouseY, mouseButton);
        limitTime.mouseClicked(mouseX, mouseY, mouseButton);
        destinationKeep.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_TAB) {
            if (limitPacket.isFocused()) {
                limitPacket.setFocused(false);
                limitTime.setFocused(true);
                destinationKeep.setFocused(false);
            } else if (limitTime.isFocused()) {
                limitPacket.setFocused(false);
                limitTime.setFocused(false);
                destinationKeep.setFocused(true);
            } else {
                limitPacket.setFocused(true);
                limitTime.setFocused(false);
                destinationKeep.setFocused(false);
            }
        } else if (keyCode == Keyboard.KEY_E || keyCode == Keyboard.KEY_ESCAPE) {
            super.keyTyped(typedChar, 1);
        } else if (keyCode >= 2 && keyCode <= 11 || keyCode == Keyboard.KEY_BACK || keyCode == Keyboard.KEY_DELETE || keyCode == Keyboard.KEY_LEFT || keyCode == Keyboard.KEY_RIGHT) {
            limitPacket.textboxKeyTyped(typedChar, keyCode);
            limitTime.textboxKeyTyped(typedChar, keyCode);
            destinationKeep.textboxKeyTyped(typedChar, keyCode);
        }
    }
}
