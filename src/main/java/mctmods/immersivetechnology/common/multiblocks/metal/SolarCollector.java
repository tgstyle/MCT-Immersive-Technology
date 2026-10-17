package mctmods.immersivetechnology.common.multiblocks.metal;

import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;

import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;

public class SolarCollector extends MachineTemplateMultiblock {
    public static final SolarCollector INSTANCE = new SolarCollector();

    public SolarCollector() { super(Reference.rl("multiblocks/solar_collector"), () -> ITShapes.get("solar_collector"), MultiblockRegistry.SOLAR_COLLECTOR); }
}
