package mctmods.immersivetechnology.common.multiblocks.metal;

import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import com.immersiveconvergence.api.multiblock.ShapeData;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import net.minecraft.core.BlockPos;

public class Distiller extends MachineTemplateMultiblock {
    private static final ShapeData SHAPE = ITShapes.get("distiller");
    public static final Distiller INSTANCE = new Distiller();

    public Distiller() { super(Reference.rl("multiblocks/distiller"), SHAPE.masterPos, SHAPE.triggerPos, new BlockPos(SHAPE.width,SHAPE.height,SHAPE.length), SHAPE.manualScale, MultiblockRegistry.DISTILLER); }
}
