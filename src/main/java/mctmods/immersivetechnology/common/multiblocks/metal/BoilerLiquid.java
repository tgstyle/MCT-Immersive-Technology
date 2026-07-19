package mctmods.immersivetechnology.common.multiblocks.metal;

import mctmods.immersivetechnology.common.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.BoilerLiquidShape;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockRegistry;

import net.minecraft.core.BlockPos;

public class BoilerLiquid extends ITTemplateMultiblock {
    public static final BoilerLiquid INSTANCE = new BoilerLiquid();

    public BoilerLiquid() { super(ITLib.rl("multiblocks/boiler_liquid"), BoilerLiquidShape.MASTER_POS, BoilerLiquidShape.TRIGGER_POS, new BlockPos(BoilerLiquidShape.WIDTH, BoilerLiquidShape.HEIGHT, BoilerLiquidShape.LENGTH), BoilerLiquidShape.CLIENT_OFFSET, BoilerLiquidShape.MANUAL_SCALE, ITMultiblockRegistry.BOILER_LIQUID); }
}
