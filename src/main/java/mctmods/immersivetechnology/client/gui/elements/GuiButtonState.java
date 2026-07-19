package mctmods.immersivetechnology.client.gui.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.IntSupplier;

public class GuiButtonState<E> extends Button {
    public final E[] states;
    protected final IntSupplier state;
    protected final ResourceLocation texture;
    protected final int texU, texV, offsetDir;
    protected final int[] textOffset;

    @SuppressWarnings("unchecked")
    public GuiButtonState(int x, int y, int w, int h, Component name, E[] states, IntSupplier state, ResourceLocation texture, int texU, int texV, int offsetDir, Consumer<GuiButtonState<E>> handler) {
        super(x, y, w, h, name, btn -> handler.accept((GuiButtonState<E>) btn), DEFAULT_NARRATION);
        this.states = states;
        this.state = state;
        this.texture = texture;
        this.texU = texU;
        this.texV = texV;
        this.offsetDir = offsetDir;
        this.textOffset = new int[]{w + 1, h / 2 - 3};
    }

    protected int getStateAsInt() { return state.getAsInt(); }

    public E getState() { return states[getStateAsInt()]; }

    public E getNextState() { return states[(getStateAsInt() + 1) % states.length]; }

    protected int getTextColor(boolean highlighted) {
        if (!this.active) { return 0xA0A0A0; }
        if (highlighted) { return 0xF78034; }
        return 0xE0E0E0;
    }

    @Override public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) { return; }
        Font font = Minecraft.getInstance().font;
        this.isHovered = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + this.width && mouseY < getY() + this.height;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        int u = texU + (offsetDir == 0 ? getStateAsInt() * this.width : 0);
        int v = texV + (offsetDir == 1 ? getStateAsInt() * this.height : 0);
        graphics.blit(texture, getX(), getY(), u, v, this.width, this.height);
        if (!getMessage().getString().isEmpty()) { graphics.drawString(font, getMessage(), getX() + textOffset[0], getY() + textOffset[1], getTextColor(this.isHovered), false); }
    }
}
