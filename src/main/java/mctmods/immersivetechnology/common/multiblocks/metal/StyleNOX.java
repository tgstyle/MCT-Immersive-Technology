package mctmods.immersivetechnology.common.multiblocks.metal;

import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import com.immersiveconvergence.api.multiblock.ShapeData;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;

import net.minecraft.core.BlockPos;

public class StyleNOX extends MachineTemplateMultiblock {
    private static final ShapeData SHAPE = ITShapes.get("stylenox");
    public static final StyleNOX INSTANCE = new StyleNOX();

    public StyleNOX() { super(Reference.rl("multiblocks/stylenox"), SHAPE.masterPos, SHAPE.triggerPos,
            new BlockPos(SHAPE.width, SHAPE.height, SHAPE.length),
            SHAPE.manualScale, MultiblockRegistry.STYLENOX); }
}
