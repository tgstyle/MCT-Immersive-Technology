package mctmods.immersivetechnology.common.multiblocks.metal;

import mctmods.immersivetechnology.common.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.MeltingCrucibleShape;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockRegistry;

import net.minecraft.core.BlockPos;

public class MeltingCrucible extends ITTemplateMultiblock {
    public static final MeltingCrucible INSTANCE = new MeltingCrucible();

    public MeltingCrucible() { super(ITLib.rl("multiblocks/melting_crucible"), MeltingCrucibleShape.MASTER_POS, MeltingCrucibleShape.TRIGGER_POS, new BlockPos(MeltingCrucibleShape.WIDTH, MeltingCrucibleShape.HEIGHT, MeltingCrucibleShape.LENGTH), MeltingCrucibleShape.CLIENT_OFFSET, MeltingCrucibleShape.MANUAL_SCALE, ITMultiblockRegistry.MELTING_CRUCIBLE); }
}
