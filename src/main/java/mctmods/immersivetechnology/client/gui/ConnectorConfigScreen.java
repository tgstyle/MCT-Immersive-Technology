package mctmods.immersivetechnology.client.gui;

import mctmods.immersivetechnology.client.gui.elements.ITGuiButtonBoolean;
import mctmods.immersivetechnology.client.gui.elements.ITGuiButtonState;
import mctmods.immersivetechnology.client.util.ITClientUtils;
import mctmods.immersivetechnology.common.blocks.connectors.logic.ConnectorTimerBlockEntity;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.network.ITIMessageTileSync;
import mctmods.immersivetechnology.core.network.ITPacketHandler;

import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.client.TextUtils;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ConnectorConfigScreen extends ClientBlockEntityScreen<ConnectorTimerBlockEntity> {
    private static final ResourceLocation TEXTURE = ITLib.makeTextureLocation("immersiveengineering", "redstone_configuration");
    private final ConnectorTimerBlockEntity tile;
    private ITGuiButtonState<IOSideConfig> buttonInOut;
    private ITGuiButtonBoolean[] colorButtons;

    public ConnectorConfigScreen(ConnectorTimerBlockEntity tile, Component title) {
        super(tile, title);
        this.tile = tile;
        this.xSize = 100;
        this.ySize = 120;
    }

    public ConnectorConfigScreen(ConnectorTimerBlockEntity tile) { this(tile, Component.empty()); }

    @Override public void renderBackground(@NotNull GuiGraphics graphics) {}

    @Override public void init() {
        super.init();
        this.clearWidgets();
        this.buttonInOut = new ITGuiButtonState<>(this.guiLeft + 41, this.guiTop + 20, 18, 18, Component.empty(),
                new IOSideConfig[]{IOSideConfig.INPUT, IOSideConfig.OUTPUT},
                () -> tile.getIoMode() == 0 ? 0 : 1,
                TEXTURE, 176, 0, 1,
                (btn) -> {
                    int newMode = tile.getIoMode() == 0 ? 1 : 0;
                    sendConfig("ioMode", newMode);
                });
        this.addRenderableWidget(this.buttonInOut);

        this.colorButtons = new ITGuiButtonBoolean[16];
        for (int i = 0; i < this.colorButtons.length; ++i) {
            DyeColor color = DyeColor.byId(i);
            this.colorButtons[i] = buildColorButton(this.colorButtons, this.guiLeft + 22 + i % 4 * 14, this.guiTop + 44 + i / 4 * 14,
                    () -> tile.redstoneChannel == color, color, (btn) -> sendConfig("redstoneChannel", color.getId()));
            this.addRenderableWidget(this.colorButtons[i]);
        }
    }

    private void sendConfig(String key, int value) {
        CompoundTag message = new CompoundTag();
        message.putInt(key, value);
        ITPacketHandler.sendToServer(new ITIMessageTileSync(tile.getBlockPos(), message));

        if ("ioMode".equals(key)) { tile.setIoMode(value); }
        else if ("redstoneChannel".equals(key)) { tile.redstoneChannel = DyeColor.byId(value); }
    }

    @Override protected void drawGuiContainerBackgroundLayer(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {}

    @Override protected void drawGuiContainerForegroundLayer(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ArrayList<Component> tooltip = new ArrayList<>();
        if (this.buttonInOut != null && this.buttonInOut.isHoveredOrFocused()) {
            tooltip.add(Component.translatable("gui.immersiveengineering.config.redstone_iomode"));
            tooltip.add(TextUtils.applyFormat(this.buttonInOut.getState().getTextComponent(), ChatFormatting.GRAY));
        }
        if (this.colorButtons != null) {
            for (int i = 0; i < this.colorButtons.length; ++i) {
                if (this.colorButtons[i].isHoveredOrFocused()) {
                    tooltip.add(Component.translatable("gui.immersiveengineering.config.redstone_color"));
                    tooltip.add(TextUtils.applyFormat(Component.translatable("color.minecraft." + DyeColor.byId(i).getName()), ChatFormatting.GRAY));
                }
            }
        }
        if (!tooltip.isEmpty()) { graphics.renderTooltip(this.font, tooltip, Optional.empty(), mouseX, mouseY); }
    }

    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        InputConstants.Key key = InputConstants.getKey(keyCode, scanCode);
        if (Minecraft.getInstance().options.keyInventory.isActiveAndMatches(key)) {
            this.onClose();
            return true;
        }
        else { return super.keyPressed(keyCode, scanCode, modifiers); }
    }

    public static ITGuiButtonBoolean buildColorButton(ITGuiButtonBoolean[] buttons, int posX, int posY, Supplier<Boolean> active, final DyeColor color, Consumer<ITGuiButtonBoolean> onClick) {
        return new ITGuiButtonBoolean(posX, posY, 12, 12, "", active, TEXTURE, 194, 0, 1, (btn) -> {
            if (btn.getNextState()) { onClick.accept(btn); }
            for (int j = 0; j < buttons.length; ++j) {
                if (j != color.ordinal() && buttons[j].getState()) {
                    buttons[j].onClick(buttons[j].getX(), buttons[j].getY());
                }
            }
        }) {
            @Override protected boolean isValidClickButton(int button) { return button == 0 && !this.getState(); }

            @Override public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                super.render(graphics, mouseX, mouseY, partialTicks);
                if (this.visible) {
                    int col = color.getTextColor();
                    if (!this.getState()) { col = ITClientUtils.getDarkenedTextColour(col); }
                    col = 0xFF000000 | col;
                    graphics.fillGradient(this.getX() + 3, this.getY() + 3, this.getX() + 9, this.getY() + 9, col, col);
                }
            }
        };
    }

    public static void open(ConnectorTimerBlockEntity timer) { if (timer != null) { Minecraft.getInstance().setScreen(new ConnectorConfigScreen(timer)); } }
}
