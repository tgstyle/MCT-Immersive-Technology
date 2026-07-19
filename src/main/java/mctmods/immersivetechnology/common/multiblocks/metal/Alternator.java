package mctmods.immersivetechnology.common.multiblocks.metal;

import mctmods.immersivetechnology.common.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.AlternatorShape;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockRegistry;

import net.minecraft.core.BlockPos;

public class Alternator extends ITTemplateMultiblock {
    public static final Alternator INSTANCE = new Alternator();

    public Alternator() { super(ITLib.rl("multiblocks/alternator"), AlternatorShape.MASTER_POS, AlternatorShape.TRIGGER_POS, new BlockPos(AlternatorShape.WIDTH, AlternatorShape.HEIGHT, AlternatorShape.LENGTH), AlternatorShape.CLIENT_OFFSET, AlternatorShape.MANUAL_SCALE, ITMultiblockRegistry.ALTERNATOR); }
}
