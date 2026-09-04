package mctmods.immersivetechnology.common.multiblocks.metal;

import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import com.immersiveconvergence.api.multiblock.ShapeData;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import net.minecraft.core.BlockPos;

public class MeltingCrucible extends MachineTemplateMultiblock {
    private static final ShapeData SHAPE = ITShapes.get("melting_crucible");
    public static final MeltingCrucible INSTANCE = new MeltingCrucible();

    public MeltingCrucible() { super(Reference.rl("multiblocks/melting_crucible"), SHAPE.masterPos, SHAPE.triggerPos, new BlockPos(SHAPE.width,SHAPE.height,SHAPE.length), SHAPE.manualScale, MultiblockRegistry.MELTING_CRUCIBLE); }
}