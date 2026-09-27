package mctmods.immersivetechnology.common.multiblocks.metal;

import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;

public class MeltingCrucible extends MachineTemplateMultiblock {
    public static final MeltingCrucible INSTANCE = new MeltingCrucible();

    public MeltingCrucible() { super(Reference.rl("multiblocks/melting_crucible"), () -> ITShapes.get("melting_crucible"), MultiblockRegistry.MELTING_CRUCIBLE); }
}