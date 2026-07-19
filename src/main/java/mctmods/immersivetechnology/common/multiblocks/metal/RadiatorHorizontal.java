package mctmods.immersivetechnology.common.multiblocks.metal;

import mctmods.immersivetechnology.common.multiblocks.helper.ModTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.RadiatorHorizontalShape;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;

import net.minecraft.core.BlockPos;

public class RadiatorHorizontal extends ModTemplateMultiblock {
    public static final RadiatorHorizontal INSTANCE = new RadiatorHorizontal();

    public RadiatorHorizontal() { super(Reference.rl("multiblocks/radiator_horizontal"), RadiatorHorizontalShape.MASTER_POS, RadiatorHorizontalShape.TRIGGER_POS, new BlockPos(RadiatorHorizontalShape.WIDTH, RadiatorHorizontalShape.HEIGHT, RadiatorHorizontalShape.LENGTH), RadiatorHorizontalShape.CLIENT_OFFSET, RadiatorHorizontalShape.MANUAL_SCALE, MultiblockRegistry.RADIATOR_HORIZONTAL); }
}
