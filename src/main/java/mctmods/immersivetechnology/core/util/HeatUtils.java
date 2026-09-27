package mctmods.immersivetechnology.core.util;

import com.immersiveconvergence.api.capability.IHeatConsumer;
import java.util.function.Supplier;

public class HeatUtils {
    public static boolean hasWater(Supplier<IHeatConsumer> boilerInput) {
        if (boilerInput == null) { return false; }
        IHeatConsumer consumer = boilerInput.get();
        return consumer != null && consumer.getFluidAmount() > 0;
    }
}
