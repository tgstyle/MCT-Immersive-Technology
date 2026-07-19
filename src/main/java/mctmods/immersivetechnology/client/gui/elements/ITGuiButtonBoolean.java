package mctmods.immersivetechnology.client.gui.elements;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ITGuiButtonBoolean extends ITGuiButtonState<Boolean> {
    public ITGuiButtonBoolean(int x, int y, int w, int h, String name, Supplier<Boolean> state, ResourceLocation texture, int texU, int texV, int offsetDir, Consumer<ITGuiButtonBoolean> handler) {
        super(x, y, w, h, Component.literal(name), new Boolean[]{false, true}, () -> state.get() ? 1 : 0, texture, texU, texV, offsetDir, btn -> handler.accept((ITGuiButtonBoolean) btn));
    }
}
