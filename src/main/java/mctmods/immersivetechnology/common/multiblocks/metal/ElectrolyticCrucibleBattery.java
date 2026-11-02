package mctmods.immersivetechnology.common.multiblocks.metal;

import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;

import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;


public class ElectrolyticCrucibleBattery extends MachineTemplateMultiblock {
    public static final ElectrolyticCrucibleBattery INSTANCE = new ElectrolyticCrucibleBattery();

    public ElectrolyticCrucibleBattery() { super(Reference.rl("multiblocks/electrolytic_crucible_battery"), () -> ITShapes.get("electrolytic_crucible_battery"), MultiblockRegistry.ELECTROLYTIC_CRUCIBLE_BATTERY); }
}
