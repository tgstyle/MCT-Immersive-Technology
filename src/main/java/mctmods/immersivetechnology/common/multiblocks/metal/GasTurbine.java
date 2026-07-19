package mctmods.immersivetechnology.common.multiblocks.metal;

import mctmods.immersivetechnology.common.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.GasTurbineShape;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockRegistry;

import net.minecraft.core.BlockPos;

public class GasTurbine extends ITTemplateMultiblock {
    public static final GasTurbine INSTANCE = new GasTurbine();

    public GasTurbine() { super(ITLib.rl("multiblocks/gas_turbine"), GasTurbineShape.MASTER_POS, GasTurbineShape.TRIGGER_POS, new BlockPos(GasTurbineShape.WIDTH, GasTurbineShape.HEIGHT, GasTurbineShape.LENGTH), GasTurbineShape.CLIENT_OFFSET, GasTurbineShape.MANUAL_SCALE, ITMultiblockRegistry.GAS_TURBINE); }
}
