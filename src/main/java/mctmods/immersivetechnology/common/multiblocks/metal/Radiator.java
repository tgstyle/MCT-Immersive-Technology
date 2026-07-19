package mctmods.immersivetechnology.common.multiblocks.metal;

import mctmods.immersivetechnology.common.multiblocks.helper.ModTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.RadiatorShape;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;

import net.minecraft.core.BlockPos;

public class Radiator extends ModTemplateMultiblock {
    public static final Radiator INSTANCE = new Radiator();

    public Radiator() { super(Reference.rl("multiblocks/radiator"), RadiatorShape.MASTER_POS, RadiatorShape.TRIGGER_POS, new BlockPos(RadiatorShape.WIDTH, RadiatorShape.HEIGHT, RadiatorShape.LENGTH), RadiatorShape.CLIENT_OFFSET, RadiatorShape.MANUAL_SCALE, MultiblockRegistry.RADIATOR); }
}
