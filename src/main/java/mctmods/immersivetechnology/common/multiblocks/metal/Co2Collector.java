package mctmods.immersivetechnology.common.multiblocks.metal;

import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;

import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;


public class Co2Collector extends MachineTemplateMultiblock {
    public static final Co2Collector INSTANCE = new Co2Collector();

    public Co2Collector() { super(Reference.rl("multiblocks/co2_collector"), () -> ITShapes.get("co2_collector"), MultiblockRegistry.CO2_COLLECTOR); }
}
