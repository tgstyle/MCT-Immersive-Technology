package mctmods.immersivetechnology.client.gui;

import mctmods.immersivetechnology.common.blocks.metal.gui.RotorCreativeMenu;
import mctmods.immersivetechnology.common.blocks.metal.logic.RotorCreativeBlockEntity;
import mctmods.immersivetechnology.common.network.ITMessageTileSync;
import mctmods.immersivetechnology.common.network.ITPacketHandler;
import mctmods.immersivetechnology.common.util.TranslationKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class RotorCreativeScreen extends AbstractContainerScreen<RotorCreativeMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("immersivetechnology", "textures/gui/rotor.png");
    private final RotorCreativeBlockEntity tile;
    private EditBox rpmField;
    private int prevRpm = Integer.MIN_VALUE;

    public RotorCreativeScreen(RotorCreativeMenu menu, Inventory inv) {
        super(menu, inv, Component.empty());
        this.tile = menu.tile;
        this.imageWidth = 96;
        this.imageHeight = 112;
        this.inventoryLabelY = -1000;
        this.titleLabelY = -1000;
    }

    private boolean isValidInput(String s) {
        if (s.isEmpty() || s.equals("-")) { return true; }
        try { Integer.parseInt(s); return true; } catch (NumberFormatException e) { return false; }
    }

    @Override public void init() {
        super.init();
        rpmField = new EditBox(font, leftPos + 36, topPos + 30, 30, 9, Component.empty());
        rpmField.setFilter(this::isValidInput);
        rpmField.setBordered(false);
        addRenderableWidget(rpmField);
        addRenderableWidget(Button.builder(Component.translatable(TranslationKey.GUI_APPLY.location), btn -> apply()).bounds(leftPos + (imageWidth / 2 - 20), topPos + 90, 40, 20).build());
        updateFields();
    }

    private void updateFields() {
        int r = menu.getRpm();
        if (r != prevRpm) {
            rpmField.setValue(r == 0 ? "" : String.valueOf(r));
            prevRpm = r;
        }
    }

    private void apply() {
        String rStr = rpmField.getValue();
        int r = rStr.isEmpty() || rStr.equals("-") ? 0 : Integer.parseInt(rStr);
        CompoundTag message = new CompoundTag();
        message.putInt("rpm", r);
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
        graphics.blit(TEXTURE, leftPos, topPos, 0.0F, 0.0F, imageWidth, 48, 96, 48);
    }

    @Override public void render(@NotNull GuiGraphics graphics, int mx, int my, float pt) {
        super.render(graphics, mx, my, pt);
        graphics.drawString(font, Component.translatable(TranslationKey.GUI_ROTOR_CREATIVE_RPM.getLocation()), rpmField.getX() + rpmField.getWidth() + 4, rpmField.getY(), 0xFFFFFF, false);
    }

    @Override protected void renderLabels(@NotNull GuiGraphics graphics, int mx, int my) {}
}
