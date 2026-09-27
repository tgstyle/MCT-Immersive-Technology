package mctmods.immersivetechnology.common.multiblocks.metal;

import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;

public class Radiator extends MachineTemplateMultiblock {
    public static final Radiator INSTANCE = new Radiator();

    public Radiator() { super(Reference.rl("multiblocks/radiator"), () -> ITShapes.get("radiator"), MultiblockRegistry.RADIATOR); }
}
