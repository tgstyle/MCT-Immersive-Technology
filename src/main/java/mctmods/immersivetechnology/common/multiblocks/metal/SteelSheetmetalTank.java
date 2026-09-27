package mctmods.immersivetechnology.common.multiblocks.metal;

import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;

public class SteelSheetmetalTank extends MachineTemplateMultiblock {
    public static final SteelSheetmetalTank INSTANCE = new SteelSheetmetalTank();

    public SteelSheetmetalTank() { super(Reference.rl("multiblocks/steel_sheetmetal_tank"), () -> ITShapes.get("steel_sheetmetal_tank"), MultiblockRegistry.STEEL_SHEETMETAL_TANK); }
}
