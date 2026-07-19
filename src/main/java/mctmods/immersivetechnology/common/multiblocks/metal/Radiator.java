package mctmods.immersivetechnology.common.multiblocks.metal;

import mctmods.immersivetechnology.common.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.RadiatorShape;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockRegistry;

import net.minecraft.core.BlockPos;

public class Radiator extends ITTemplateMultiblock {
    public static final Radiator INSTANCE = new Radiator();

    public Radiator() { super(ITLib.rl("multiblocks/radiator"), RadiatorShape.MASTER_POS, RadiatorShape.TRIGGER_POS, new BlockPos(RadiatorShape.WIDTH, RadiatorShape.HEIGHT, RadiatorShape.LENGTH), RadiatorShape.CLIENT_OFFSET, RadiatorShape.MANUAL_SCALE, ITMultiblockRegistry.RADIATOR); }
}
