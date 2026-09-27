package mctmods.immersivetechnology.common.multiblocks.metal;

import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;

public class RadiatorHorizontal extends MachineTemplateMultiblock {
    public static final RadiatorHorizontal INSTANCE = new RadiatorHorizontal();

    public RadiatorHorizontal() { super(Reference.rl("multiblocks/radiator_horizontal"), () -> ITShapes.get("radiator_horizontal"), MultiblockRegistry.RADIATOR_HORIZONTAL); }
}
