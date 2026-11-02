package mctmods.immersivetechnology.common.multiblocks.metal;

import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;

import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;


public class Distiller extends MachineTemplateMultiblock {
    public static final Distiller INSTANCE = new Distiller();

    public Distiller() { super(Reference.rl("multiblocks/distiller"), () -> ITShapes.get("distiller"), MultiblockRegistry.DISTILLER); }
}
