package mctmods.immersivetechnology.common.multiblocks.metal;

import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;

public class Alternator extends MachineTemplateMultiblock {
    public static final Alternator INSTANCE = new Alternator();

    public Alternator() { super(Reference.rl("multiblocks/alternator"), () -> ITShapes.get("alternator"), MultiblockRegistry.ALTERNATOR); }
}
