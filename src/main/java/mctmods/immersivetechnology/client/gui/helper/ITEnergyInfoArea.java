package mctmods.immersivetechnology.client.gui.helper;

import blusunrize.immersiveengineering.api.client.TextUtils;
import mctmods.immersivetechnology.common.util.TranslationKey;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.List;

public class ITEnergyInfoArea extends ITInfoArea {
    private final IEnergyStorage energy;

    public ITEnergyInfoArea(int xMin, int yMin, IEnergyStorage energy) {
        super(new Rect2i(xMin, yMin, 7, 46));
        this.energy = energy;
    }

    @Override protected void fillTooltipOverArea(int mouseX, int mouseY, List<Component> tooltip) {
        tooltip.add(TextUtils.applyFormat(Component.translatable(TranslationKey.GUI_ENERGY_STORED.getLocation(), energy.getEnergyStored(), energy.getMaxEnergyStored()), ChatFormatting.GRAY));
    }

    @Override public void draw(GuiGraphics graphics) {
        final int height = area.getHeight();
        int stored = (int) (height * (energy.getEnergyStored() / (float) energy.getMaxEnergyStored()));
        graphics.fillGradient(area.getX(), area.getY() + (height - stored), area.getX() + area.getWidth(), area.getY() + area.getHeight(), 0xffb51500, 0xff600b00);
    }
}
