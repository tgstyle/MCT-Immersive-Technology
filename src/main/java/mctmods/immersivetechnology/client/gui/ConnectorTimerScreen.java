package mctmods.immersivetechnology.client.gui;

import mctmods.immersivetechnology.client.gui.elements.GuiButtonBoolean;
import mctmods.immersivetechnology.client.gui.elements.GuiButtonState;
import mctmods.immersivetechnology.client.util.ClientUtils;
import mctmods.immersivetechnology.common.blocks.connectors.gui.ConnectorTimerMenu;
import mctmods.immersivetechnology.common.blocks.connectors.logic.ConnectorTimerBlockEntity;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.network.MessageTileSync;
import mctmods.immersivetechnology.core.network.PacketHandler;

import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.client.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ConnectorTimerScreen extends AbstractContainerScreen<ConnectorTimerMenu> {
    private static final ResourceLocation TEXTURE = Reference.makeTextureLocation("timer");
    private static final ResourceLocation CONFIG_TEXTURE = Reference.makeTextureLocation("immersiveengineering", "redstone_configuration");
    private final ConnectorTimerBlockEntity tile;
    private GuiButtonState<IOSideConfig> buttonInOut;
    private GuiButtonBoolean[] colorButtons;

    public ConnectorTimerScreen(ConnectorTimerMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.tile = menu.tile;
        this.imageWidth = 176;
        this.imageHeight = 236;
        this.inventoryLabelY = -10000;
        this.titleLabelY = -10000;
    }

    @Override public void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.literal("+"), btn -> changeTarget(1)).bounds(leftPos + 39, topPos + 35, 16, 16).build());
        addRenderableWidget(Button.builder(Component.literal("-"), btn -> changeTarget(-1)).bounds(leftPos + 120, topPos + 35, 16, 16).build());

        this.buttonInOut = new GuiButtonState<>(leftPos + 38, topPos + 80, 18, 18, Component.empty(),
                new IOSideConfig[]{IOSideConfig.INPUT, IOSideConfig.OUTPUT},
                () -> tile.getIoMode() == 0 ? 0 : 1,
                CONFIG_TEXTURE, 176, 0, 1,
                (btn) -> sendConfig("ioMode", tile.getIoMode() == 0 ? 1 : 0));
        addRenderableWidget(this.buttonInOut);

        this.colorButtons = new GuiButtonBoolean[16];
        for (int i = 0; i < this.colorButtons.length; ++i) {
            DyeColor color = DyeColor.byId(i);
            this.colorButtons[i] = buildColorButton(this.colorButtons, leftPos + 82 + i % 4 * 14, topPos + 66 + i / 4 * 14,
                    () -> tile.redstoneChannel == color, color, (btn) -> sendConfig("redstoneChannel", color.getId()));
            addRenderableWidget(this.colorButtons[i]);
        }
    }

    private void changeTarget(int increment) {
        CompoundTag message = new CompoundTag();
        message.putInt("increment", increment);
        PacketHandler.sendToServer(new MessageTileSync(tile.getBlockPos(), message));
    }

    private void sendConfig(String key, int value) {
        CompoundTag message = new CompoundTag();
        message.putInt(key, value);
        PacketHandler.sendToServer(new MessageTileSync(tile.getBlockPos(), message));

        if ("ioMode".equals(key)) { tile.setIoMode(value); }
        else if ("redstoneChannel".equals(key)) { tile.redstoneChannel = DyeColor.byId(value); }
    }

    @Override protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        this.renderBackground(graphics);
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        float time = (float) menu.getTarget() / 20f;
        graphics.drawString(font, String.format("%.1f Sec.", time), leftPos + 68, topPos + 40, 0xFFFFFF, false);

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

    public static GuiButtonBoolean buildColorButton(GuiButtonBoolean[] buttons, int posX, int posY, Supplier<Boolean> active, final DyeColor color, Consumer<GuiButtonBoolean> onClick) {
        return new GuiButtonBoolean(posX, posY, 12, 12, "", active, CONFIG_TEXTURE, 194, 0, 1, (btn) -> {
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
                    if (!this.getState()) { col = ClientUtils.getDarkenedTextColour(col); }
                    col = 0xFF000000 | col;
                    graphics.fillGradient(this.getX() + 3, this.getY() + 3, this.getX() + 9, this.getY() + 9, col, col);
                }
            }
        };
    }

    @Override protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {}
}
