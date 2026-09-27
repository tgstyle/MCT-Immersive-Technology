package mctmods.immersivetechnology.common.multiblocks.stone;

import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;

public class AdvancedCokeOven extends MachineTemplateMultiblock {
    public static final AdvancedCokeOven INSTANCE = new AdvancedCokeOven();

    public AdvancedCokeOven() { super(Reference.rl("multiblocks/advanced_coke_oven"), () -> ITShapes.get("advanced_coke_oven"), MultiblockRegistry.ADVANCED_COKE_OVEN); }
}
