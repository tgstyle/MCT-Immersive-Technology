package mctmods.immersivetechnology.common.multiblocks.metal;

import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.DistillerShape;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import net.minecraft.core.BlockPos;

public class Distiller extends MachineTemplateMultiblock {
    public static final Distiller INSTANCE = new Distiller();

    public Distiller() { super(Reference.rl("multiblocks/distiller"), DistillerShape.MASTER_POS, DistillerShape.TRIGGER_POS, new BlockPos(DistillerShape.WIDTH,DistillerShape.HEIGHT,DistillerShape.LENGTH), DistillerShape.CLIENT_OFFSET, DistillerShape.MANUAL_SCALE, MultiblockRegistry.DISTILLER); }
}
