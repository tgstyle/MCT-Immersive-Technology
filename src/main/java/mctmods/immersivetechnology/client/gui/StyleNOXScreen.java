package mctmods.immersivetechnology.client.gui;

import com.immersiveconvergence.api.client.gui.BaseContainerScreen;
import com.immersiveconvergence.api.client.gui.GuiEnergyArea;
import com.immersiveconvergence.api.client.gui.GuiFluidArea;
import com.immersiveconvergence.api.client.gui.GuiInfoArea;
import mctmods.immersivetechnology.common.multiblocks.gui.StyleNOXMenu;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.StyleNOXLogic;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.util.TranslationKey;

import com.immersiveconvergence.api.util.ICTextUtils;
import com.google.common.collect.ImmutableList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

public class StyleNOXScreen extends BaseContainerScreen<StyleNOXMenu> {
    private static final ResourceLocation TEXTURE = Reference.makeTextureLocation("stylenox");
    private static final int SCRAM_X = StyleNOXMenu.FUEL_GRID_X + 17;
    private static final int SCRAM_Y = StyleNOXMenu.FUEL_GRID_Y + 17;
    private final Button[] rodButtons = new Button[StyleNOXLogic.MAX_ROD_LEVEL];
    private ScramButton scramButton;

    public StyleNOXScreen(StyleNOXMenu container, Inventory inventoryPlayer, Component title) { super(container, inventoryPlayer, title, TEXTURE); }

    @Override public void init() {
        super.init();
        titleLabelY = -1000;
        for (int i = 0; i < StyleNOXLogic.MAX_ROD_LEVEL; i++) {
            final int level = i + 1;
            final int percent = level * 100 / StyleNOXLogic.MAX_ROD_LEVEL;
            rodButtons[i] = Button.builder(Component.literal(Integer.toString(percent)), btn -> sendButton(level))
                    .bounds(leftPos + 124, topPos + 17 + i * 13, 22, 12)
                    .tooltip(Tooltip.create(Component.translatable(TranslationKey.GUI_STYLENOX_ROD_LEVEL.getLocation(), percent)))
                    .build();
            addRenderableWidget(rodButtons[i]);
        }
        scramButton = new ScramButton(leftPos + SCRAM_X, topPos + SCRAM_Y);
        addRenderableWidget(scramButton);
        updateButtons();
    }

    private void sendButton(int id) { Objects.requireNonNull(Objects.requireNonNull(minecraft).gameMode).handleInventoryButtonClick(menu.containerId, id); }

    private void updateButtons() {
        for (int i = 0; i < rodButtons.length; i++) { rodButtons[i].active = menu.rodLevel != i + 1; }
        scramButton.active = menu.rodLevel > 0;
    }

    @Override public void containerTick() {
        super.containerTick();
        updateButtons();
    }

    @Override @Nonnull protected List<GuiInfoArea> makeInfoAreas() {
        return ImmutableList.of(
                new GuiFluidArea(menu.tanks.water(), new Rect2i(leftPos + 8, topPos + 21, 16, 47), 177, 31, 20, 51, TEXTURE),
                new GuiFluidArea(menu.tanks.co2(), new Rect2i(leftPos + 27, topPos + 21, 16, 47), 177, 31, 20, 51, TEXTURE),
                new GuiFluidArea(menu.tanks.steam(), new Rect2i(leftPos + 46, topPos + 21, 16, 47), 177, 31, 20, 51, TEXTURE),
                new EnergyBar(leftPos + 154, topPos + 39, 7, 30, menu.energy)
        );
    }

    @Override public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        TranslationKey stateKey = switch (StyleNOXLogic.ReactorState.values()[menu.reactorState]) {
            case IDLE -> TranslationKey.GUI_STYLENOX_IDLE;
            case STARTING -> TranslationKey.GUI_STYLENOX_STARTING;
            case CRITICAL -> TranslationKey.GUI_STYLENOX_CRITICAL;
        };
        graphics.drawString(font, Component.translatable(stateKey.getLocation()), leftPos + 8, topPos + 7, 0xFFFFFF, true);
        if (menu.burnTotal > 0) {
            int percent = (int) (100L * menu.burnProgress / menu.burnTotal);
            graphics.drawString(font, Component.translatable(TranslationKey.GUI_STYLENOX_BURNUP.getLocation(), percent), leftPos + StyleNOXMenu.FUEL_GRID_X - 1, topPos + 7, 0xFFFFFF, true);
        }
    }

    private class ScramButton extends AbstractButton {
        ScramButton(int x, int y) {
            super(x, y, 18, 18, Component.translatable(TranslationKey.GUI_STYLENOX_SCRAM.getLocation()));
            setTooltip(Tooltip.create(getMessage()));
        }

        @Override public void onPress() { sendButton(0); }

        @Override protected void renderWidget(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            int u = !active ? 212 : isHoveredOrFocused() ? 194 : 176;
            graphics.blit(TEXTURE, getX(), getY(), u, 84, 18, 18);
        }

        @Override protected void updateWidgetNarration(@Nonnull NarrationElementOutput output) { defaultButtonNarrationText(output); }
    }

    private static class EnergyBar extends GuiInfoArea {
        private final IEnergyStorage energy;

        EnergyBar(int x, int y, int width, int height, IEnergyStorage energy) {
            super(new Rect2i(x, y, width, height));
            this.energy = energy;
        }

        @Override protected void fillTooltipOverArea(int mouseX, int mouseY, List<Component> tooltip) { tooltip.add(ICTextUtils.applyFormat(Component.translatable(GuiEnergyArea.KEY_ENERGY_STORED, energy.getEnergyStored(), energy.getMaxEnergyStored()), ChatFormatting.GRAY)); }

        @Override public void draw(GuiGraphics graphics) {
            int height = area.getHeight();
            int stored = (int) (height * (energy.getEnergyStored() / (float) energy.getMaxEnergyStored()));
            graphics.fillGradient(area.getX(), area.getY() + (height - stored), area.getX() + area.getWidth(), area.getY() + height, 0xffb51500, 0xff600b00);
        }
    }
}
