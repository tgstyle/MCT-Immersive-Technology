package mctmods.immersivetechnology.common.multiblocks.metal;

import mctmods.immersivetechnology.common.multiblocks.helper.ModTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.GasTurbineShape;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;

import net.minecraft.core.BlockPos;

public class GasTurbine extends ModTemplateMultiblock {
    public static final GasTurbine INSTANCE = new GasTurbine();

    public GasTurbine() { super(Reference.rl("multiblocks/gas_turbine"), GasTurbineShape.MASTER_POS, GasTurbineShape.TRIGGER_POS, new BlockPos(GasTurbineShape.WIDTH, GasTurbineShape.HEIGHT, GasTurbineShape.LENGTH), GasTurbineShape.CLIENT_OFFSET, GasTurbineShape.MANUAL_SCALE, MultiblockRegistry.GAS_TURBINE); }
}
