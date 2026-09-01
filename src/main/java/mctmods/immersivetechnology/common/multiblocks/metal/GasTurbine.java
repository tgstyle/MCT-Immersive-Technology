package mctmods.immersivetechnology.common.multiblocks.metal;

import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.GasTurbineShape;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import net.minecraft.core.BlockPos;

public class GasTurbine extends MachineTemplateMultiblock {
    public static final GasTurbine INSTANCE = new GasTurbine();

    public GasTurbine() { super(Reference.rl("multiblocks/gas_turbine"), GasTurbineShape.MASTER_POS, GasTurbineShape.TRIGGER_POS, new BlockPos(GasTurbineShape.WIDTH,GasTurbineShape.HEIGHT,GasTurbineShape.LENGTH), GasTurbineShape.CLIENT_OFFSET, GasTurbineShape.MANUAL_SCALE, MultiblockRegistry.GAS_TURBINE); }
}
