package mctmods.immersivetechnology.client.gui;

import mctmods.immersivetechnology.common.blocks.connectors.gui.ConnectorTimerMenu;
import mctmods.immersivetechnology.common.blocks.connectors.logic.ConnectorTimerBlockEntity;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.network.ITMessageTileSync;
import mctmods.immersivetechnology.core.network.ITPacketHandler;
import mctmods.immersivetechnology.core.util.TranslationKey;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

public class ConnectorTimerScreen extends AbstractContainerScreen<ConnectorTimerMenu> {
    private static final ResourceLocation TEXTURE = ITLib.makeTextureLocation("timer");
    private final ConnectorTimerBlockEntity tile;

    public ConnectorTimerScreen(ConnectorTimerMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.tile = menu.tile;
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = -10000;
        this.titleLabelY = -10000;
    }

    @Override public void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.literal("+"), btn -> changeTarget(1)).bounds(leftPos + 39, topPos + 35, 16, 16).build());
        addRenderableWidget(Button.builder(Component.literal("-"), btn -> changeTarget(-1)).bounds(leftPos + 120, topPos + 35, 16, 16).build());
    }

    private void changeTarget(int increment) {
        CompoundTag message = new CompoundTag();
        message.putInt("increment", increment);
        ITPacketHandler.sendToServer(new ITMessageTileSync(tile.getBlockPos(), message));
    }

    @Override protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        this.renderBackground(graphics);
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        float time = (float) menu.getTarget() / 20f;
        graphics.drawString(font, String.format("%.1f Sec.", time), leftPos + 68, topPos + 40, 0xFFFFFF, false);
        String ioText = (tile.getIoMode() == 0) ? "INPUT" : "OUTPUT";
        graphics.drawString(font, TranslationKey.GUI_TIMER_MODE.text() + ioText, leftPos + 25, topPos + 75, 0xFFFFFF, false);
        graphics.drawString(font, TranslationKey.GUI_TIMER_CHANNEL.text() + tile.redstoneChannel.getName(), leftPos + 25, topPos + 85, 0xFFFFFF, false);
        graphics.drawString(font, TranslationKey.GUI_TIMER_TOGGLE.text(), leftPos + 25, topPos + 95, 0xFFFFFF, false);
    }

    @Override protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {}
}
