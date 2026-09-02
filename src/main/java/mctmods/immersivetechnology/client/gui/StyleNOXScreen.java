package mctmods.immersivetechnology.client.gui;

import com.immersiveconvergence.api.client.gui.BaseContainerScreen;
import com.immersiveconvergence.api.client.gui.GuiEnergyArea;
import com.immersiveconvergence.api.client.gui.GuiFluidArea;
import com.immersiveconvergence.api.client.gui.GuiInfoArea;
import mctmods.immersivetechnology.common.multiblocks.gui.StyleNOXMenu;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.StyleNOXLogic;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.util.TranslationKey;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

public class StyleNOXScreen extends BaseContainerScreen<StyleNOXMenu> {
    private static final ResourceLocation TEXTURE = Reference.makeTextureLocation("stylenox");
    private final Button[] rodButtons = new Button[StyleNOXLogic.MAX_ROD_LEVEL];
    private Button scramButton;

    public StyleNOXScreen(StyleNOXMenu container, Inventory inventoryPlayer, Component title) { super(container, inventoryPlayer, title, TEXTURE); }

    @Override public void init() {
        super.init();
        titleLabelY = -1000;
        for (int i = 0; i < StyleNOXLogic.MAX_ROD_LEVEL; i++) {
            final int level = i + 1;
            rodButtons[i] = Button.builder(Component.literal((level * 25) + "%"), btn -> sendButton(level))
                    .bounds(leftPos + 43 + i * 24, topPos + 40, 23, 14).build();
            addRenderableWidget(rodButtons[i]);
        }
        scramButton = Button.builder(Component.translatable(TranslationKey.GUI_STYLENOX_SCRAM.getLocation()), btn -> sendButton(0))
                .bounds(leftPos + 43, topPos + 57, 95, 14).build();
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
                new GuiFluidArea(menu.tanks.water(), new Rect2i(leftPos + 8, topPos + 17, 16, 47), 177, 31, 20, 51, TEXTURE),
                new GuiFluidArea(menu.tanks.co2(), new Rect2i(leftPos + 27, topPos + 17, 16, 47), 177, 31, 20, 51, TEXTURE),
                new GuiFluidArea(menu.tanks.steam(), new Rect2i(leftPos + 134, topPos + 17, 16, 47), 177, 31, 20, 51, TEXTURE),
                new GuiEnergyArea(leftPos + 158, topPos + 17, menu.energy)
        );
    }

    @Override public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        TranslationKey stateKey = switch (StyleNOXLogic.ReactorState.values()[menu.reactorState]) {
            case IDLE -> TranslationKey.GUI_STYLENOX_IDLE;
            case STARTING -> TranslationKey.GUI_STYLENOX_STARTING;
            case CRITICAL -> TranslationKey.GUI_STYLENOX_CRITICAL;
        };
        graphics.drawString(font, Component.translatable(stateKey.getLocation()), leftPos + 8, topPos + 6, 0xFFFFFF, true);
        if (menu.burnTotal > 0) {
            int percent = (int) (100L * menu.burnProgress / menu.burnTotal);
            graphics.drawString(font, Component.translatable(TranslationKey.GUI_STYLENOX_BURNUP.getLocation(), percent), leftPos + 90, topPos + 6, 0xFFFFFF, true);
        }
    }
}
