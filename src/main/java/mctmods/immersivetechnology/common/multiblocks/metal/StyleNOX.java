package mctmods.immersivetechnology.common.multiblocks.metal;

import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;

import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;


public class StyleNOX extends MachineTemplateMultiblock {
    public static final StyleNOX INSTANCE = new StyleNOX();

    public StyleNOX() { super(Reference.rl("multiblocks/stylenox"), () -> ITShapes.get("stylenox"), MultiblockRegistry.STYLENOX); }
}
