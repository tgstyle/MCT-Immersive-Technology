package mctmods.immersivetechnology.client.util;

import mctmods.immersivetechnology.core.util.TranslationKey;

import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;

public class ClientUtils {
    public static Component formatFluidStack(FluidStack fluid) {
        if (fluid.isEmpty()) { return Component.translatable(TranslationKey.GUI_EMPTY.text()); }
        return Component.literal(fluid.getDisplayName().getString() + ": " + fluid.getAmount() + "mB");
    }

    public static int getDarkenedTextColour(int colour) {
        int r = (colour >> 16 & 255) / 4;
        int g = (colour >> 8 & 255) / 4;
        int b = (colour & 255) / 4;
        return r << 16 | g << 8 | b;
    }

}
