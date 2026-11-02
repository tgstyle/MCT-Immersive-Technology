package mctmods.immersivetechnology.core.util;

import mctmods.immersivetechnology.core.lib.Reference;

import com.immersiveconvergence.api.multiblock.MultiblockBuilder;
import net.neoforged.bus.api.IEventBus;

public class ModBusHooks {
    public static void install(IEventBus modBus) {
        Reference.MOD_BUS = modBus;
        MultiblockBuilder.modBus = modBus;
    }
}
