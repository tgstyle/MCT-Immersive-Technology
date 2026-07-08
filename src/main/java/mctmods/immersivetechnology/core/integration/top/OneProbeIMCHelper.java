package mctmods.immersivetechnology.core.integration.top;

import java.util.function.Function;

import net.neoforged.fml.InterModComms;

public class OneProbeIMCHelper {
    public static void register() {
        InterModComms.sendTo("theoneprobe", "getTheOneProbe", () -> (Function<mcjty.theoneprobe.api.ITheOneProbe, Void>) top -> {
            OneProbeHelper.register(top);
            return null;
        });
    }
}
