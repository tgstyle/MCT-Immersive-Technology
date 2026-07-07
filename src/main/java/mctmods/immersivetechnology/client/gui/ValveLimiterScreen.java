package mctmods.immersivetechnology.client.gui;

import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import mctmods.immersivetechnology.common.blocks.metal.gui.ValveLimiterMenu;
import mctmods.immersivetechnology.common.blocks.metal.logic.ValveCommonBlockEntity;
import mctmods.immersivetechnology.core.network.ITMessageTileSync;
import mctmods.immersivetechnology.core.network.ITPacketHandler;
import mctmods.immersivetechnology.core.util.TranslationKey;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

public class ValveLimiterScreen extends AbstractContainerScreen<ValveLimiterMenu> {
    private static final ResourceLocation TEXTURE = ITLib.makeTextureLocation("valve");
    private final ValveCommonBlockEntity tile;
    private EditBox packetLimitField;
    private EditBox timeLimitField;
    private EditBox keepSizeField;

    private int prevPacketLimit = Integer.MIN_VALUE;
    private int prevTimeLimit = Integer.MIN_VALUE;
    private int prevKeepSize = Integer.MIN_VALUE;

    public ValveLimiterScreen(ValveLimiterMenu menu, Inventory inv) {
        super(menu, inv, Component.empty());
        this.tile = menu.tile;
        this.imageWidth = 216;
        this.imageHeight = 112;
        this.inventoryLabelY = -1000;
        this.titleLabelY = -1000;
    }

    private boolean isValidInput(String s) {
        if (s.isEmpty() || s.equals("-")) return true;
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) { return false; }
    }

    @Override public void init() {
        super.init();
        packetLimitField = new EditBox(font, leftPos + 23, topPos + 30, 50, 9, Component.empty());
        packetLimitField.setFilter(this::isValidInput);
        packetLimitField.setBordered(false);
        addRenderableWidget(packetLimitField);
        timeLimitField = new EditBox(font, leftPos + 23, topPos + 48, 50, 9, Component.empty());
        timeLimitField.setFilter(this::isValidInput);
        timeLimitField.setBordered(false);
        addRenderableWidget(timeLimitField);
        keepSizeField = new EditBox(font, leftPos + 23, topPos + 66, 50, 9, Component.empty());
        keepSizeField.setFilter(this::isValidInput);
        keepSizeField.setBordered(false);
        addRenderableWidget(keepSizeField);
        addRenderableWidget(Button.builder(Component.translatable(TranslationKey.GUI_APPLY.location), btn -> apply()).bounds(leftPos + (imageWidth / 2 - 20), topPos + 90, 40, 20).build());
        updateFields();
    }

    private void updateFields() {
        int pl = menu.getPacketLimit();
        if (pl != prevPacketLimit) {
            packetLimitField.setValue(pl == -1 ? "" : String.valueOf(pl));
            prevPacketLimit = pl;
        }
        int tl = menu.getTimeLimit();
        if (tl != prevTimeLimit) {
            timeLimitField.setValue(tl == -1 ? "" : String.valueOf(tl));
            prevTimeLimit = tl;
        }
        int ks = menu.getKeepSize();
        if (ks != prevKeepSize) {
            keepSizeField.setValue(ks == -1 ? "" : String.valueOf(ks));
            prevKeepSize = ks;
        }
    }

    private void apply() {
        String plStr = packetLimitField.getValue();
        int pl = plStr.isEmpty() || plStr.equals("-") ? -1 : Integer.parseInt(plStr);
        String tlStr = timeLimitField.getValue();
        int tl = tlStr.isEmpty() || tlStr.equals("-") ? -1 : Integer.parseInt(tlStr);
        String ksStr = keepSizeField.getValue();
        int ks = ksStr.isEmpty() || ksStr.equals("-") ? -1 : Integer.parseInt(ksStr);
        CompoundTag message = new CompoundTag();
        message.putInt("packetLimit", pl);
        message.putInt("timeLimit", tl);
        message.putInt("keepSize", ks);
        ITPacketHandler.sendToServer(new ITMessageTileSync(tile.getBlockPos(), message));
        Minecraft.getInstance().setScreen(null);
    }

    @Override public void containerTick() {
        super.containerTick();
        updateFields();
    }

    @Override public void onClose() { super.onClose(); }
    @Override protected void renderBg(@NotNull GuiGraphics graphics, float pt, int mx, int my) {
        this.renderBackground(graphics);
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0.0F, 0.0F, imageWidth, 88, imageWidth, 88);
    }

    @Override public void render(@NotNull GuiGraphics graphics, int mx, int my, float pt) {
        super.render(graphics, mx, my, pt);
        graphics.drawString(font, Component.translatable(TranslationKey.GUI_VALVE_FIRST_LINE.location), leftPos + 23, topPos + 14, 0xFFFFFF, false);
        graphics.drawString(font, Component.translatable(TranslationKey.GUI_VALVE_LIMITER_LIMIT_PACKET.location), packetLimitField.getX() + 54, packetLimitField.getY(), 0xFFFFFF, false);
        graphics.drawString(font, Component.translatable(TranslationKey.GUI_VALVE_LIMITER_LIMIT_TIME.location), timeLimitField.getX() + 54, timeLimitField.getY(), 0xFFFFFF, false);
        graphics.drawString(font, Component.translatable(TranslationKey.GUI_VALVE_LIMITER_LIMIT_DESTINATION.location), keepSizeField.getX() + 54, keepSizeField.getY(), 0xFFFFFF, false);
    }

    @Override protected void renderLabels(@NotNull GuiGraphics graphics, int mx, int my) {}
}
