package mctmods.immersivetechnology.common.multiblocks.metal;

import mctmods.immersivetechnology.common.multiblocks.helper.ModTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.AlternatorShape;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import net.minecraft.core.BlockPos;

public class Alternator extends ModTemplateMultiblock {
    public static final Alternator INSTANCE = new Alternator();

    public Alternator() { super(Reference.rl("multiblocks/alternator"), AlternatorShape.MASTER_POS, AlternatorShape.TRIGGER_POS, new BlockPos(AlternatorShape.WIDTH,AlternatorShape.HEIGHT,AlternatorShape.LENGTH), AlternatorShape.CLIENT_OFFSET, AlternatorShape.MANUAL_SCALE, MultiblockRegistry.ALTERNATOR); }
}
