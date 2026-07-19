package mctmods.immersivetechnology.common.multiblocks.metal;

import mctmods.immersivetechnology.common.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.DistillerShape;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockRegistry;

import net.minecraft.core.BlockPos;

public class Distiller extends ITTemplateMultiblock {
    public static final Distiller INSTANCE = new Distiller();

    public Distiller() { super(ITLib.rl("multiblocks/distiller"), DistillerShape.MASTER_POS, DistillerShape.TRIGGER_POS, new BlockPos(DistillerShape.WIDTH, DistillerShape.HEIGHT, DistillerShape.LENGTH), DistillerShape.CLIENT_OFFSET, DistillerShape.MANUAL_SCALE, ITMultiblockRegistry.DISTILLER); }
}
