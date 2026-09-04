package mctmods.immersivetechnology.common.multiblocks.metal;

import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import com.immersiveconvergence.api.multiblock.ShapeData;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import net.minecraft.core.BlockPos;

public class Radiator extends MachineTemplateMultiblock {
    private static final ShapeData SHAPE = ITShapes.get("radiator");
    public static final Radiator INSTANCE = new Radiator();

    public Radiator() { super(Reference.rl("multiblocks/radiator"), SHAPE.masterPos, SHAPE.triggerPos, new BlockPos(SHAPE.width, SHAPE.height, SHAPE.length), SHAPE.manualScale, MultiblockRegistry.RADIATOR); }
}
