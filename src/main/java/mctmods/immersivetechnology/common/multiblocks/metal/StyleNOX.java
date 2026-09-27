package mctmods.immersivetechnology.common.multiblocks.metal;

import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;


public class StyleNOX extends MachineTemplateMultiblock {
    public static final StyleNOX INSTANCE = new StyleNOX();

    public StyleNOX() { super(Reference.rl("multiblocks/stylenox"), () -> ITShapes.get("stylenox"), MultiblockRegistry.STYLENOX); }
}
