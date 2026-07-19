package mctmods.immersivetechnology.client.gui.helper;

import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;

public abstract class InfoArea {
    protected final Rect2i area;

    protected InfoArea(Rect2i area) {
        this.area = area;
    }

    public final void fillTooltip(int mouseX, int mouseY, List<Component> tooltip) {
        if (this.area.contains(mouseX, mouseY)) { this.fillTooltipOverArea(mouseX, mouseY, tooltip); }
    }

    protected abstract void fillTooltipOverArea(int var1, int var2, List<Component> var3);

    public abstract void draw(GuiGraphics var1);
}
