package mctmods.immersivetechnology.core.util;

import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import com.immersiveconvergence.api.capability.IHeatConsumer;

public class HeatUtils {
    public static boolean hasWater(CapabilityReference<IHeatConsumer> boilerInput) {
        if (boilerInput == null) { return false; }
        IHeatConsumer consumer = boilerInput.getNullable();
        return consumer != null && consumer.getFluidAmount() > 0;
    }
}
