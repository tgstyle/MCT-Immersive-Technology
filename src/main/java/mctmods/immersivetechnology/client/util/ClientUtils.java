package mctmods.immersivetechnology.client.util;

import mctmods.immersivetechnology.core.util.TranslationKey;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.FluidStack;

public class ClientUtils {
    public static Component formatFluidStack(FluidStack fluid) {
        if (fluid.isEmpty()) { return Component.translatable(TranslationKey.GUI_EMPTY.text()); }
        return Component.literal(fluid.getHoverName().getString() + ": " + fluid.getAmount() + "mB");
    }

}
