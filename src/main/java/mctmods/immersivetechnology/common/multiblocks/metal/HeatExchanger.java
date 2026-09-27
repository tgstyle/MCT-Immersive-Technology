package mctmods.immersivetechnology.common.multiblocks.metal;

import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;


public class HeatExchanger extends MachineTemplateMultiblock {
    public static final HeatExchanger INSTANCE = new HeatExchanger();

    public HeatExchanger() { super(Reference.rl("multiblocks/heat_exchanger"), () -> ITShapes.get("heat_exchanger"), MultiblockRegistry.HEAT_EXCHANGER); }
}
