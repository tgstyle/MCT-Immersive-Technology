package mctmods.immersivetechnology.common.multiblocks.metal;

import mctmods.immersivetechnology.common.multiblocks.helper.ModTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.MeltingCrucibleShape;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import net.minecraft.core.BlockPos;

public class MeltingCrucible extends ModTemplateMultiblock {
    public static final MeltingCrucible INSTANCE = new MeltingCrucible();

    public MeltingCrucible() { super(Reference.rl("multiblocks/melting_crucible"), MeltingCrucibleShape.MASTER_POS, MeltingCrucibleShape.TRIGGER_POS, new BlockPos(MeltingCrucibleShape.WIDTH,MeltingCrucibleShape.HEIGHT,MeltingCrucibleShape.LENGTH), MeltingCrucibleShape.CLIENT_OFFSET, MeltingCrucibleShape.MANUAL_SCALE, MultiblockRegistry.MELTING_CRUCIBLE); }
}