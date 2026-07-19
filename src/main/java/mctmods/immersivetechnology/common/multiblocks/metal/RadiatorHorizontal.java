package mctmods.immersivetechnology.common.multiblocks.metal;

import mctmods.immersivetechnology.common.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.RadiatorHorizontalShape;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockRegistry;

import net.minecraft.core.BlockPos;

public class RadiatorHorizontal extends ITTemplateMultiblock {
    public static final RadiatorHorizontal INSTANCE = new RadiatorHorizontal();

    public RadiatorHorizontal() { super(ITLib.rl("multiblocks/radiator_horizontal"), RadiatorHorizontalShape.MASTER_POS, RadiatorHorizontalShape.TRIGGER_POS, new BlockPos(RadiatorHorizontalShape.WIDTH, RadiatorHorizontalShape.HEIGHT, RadiatorHorizontalShape.LENGTH), RadiatorHorizontalShape.CLIENT_OFFSET, RadiatorHorizontalShape.MANUAL_SCALE, ITMultiblockRegistry.RADIATOR_HORIZONTAL); }
}
