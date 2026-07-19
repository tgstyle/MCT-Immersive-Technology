package mctmods.immersivetechnology.client.gui.elements;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GuiButtonBoolean extends GuiButtonState<Boolean> {
    public GuiButtonBoolean(int x, int y, int w, int h, String name, Supplier<Boolean> state, ResourceLocation texture, int texU, int texV, int offsetDir, Consumer<GuiButtonBoolean> handler) {
        super(x, y, w, h, Component.literal(name), new Boolean[]{false, true}, () -> state.get() ? 1 : 0, texture, texU, texV, offsetDir, btn -> handler.accept((GuiButtonBoolean) btn));
    }
}
