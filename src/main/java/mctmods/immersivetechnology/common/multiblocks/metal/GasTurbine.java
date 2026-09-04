package mctmods.immersivetechnology.common.multiblocks.metal;

import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import com.immersiveconvergence.api.multiblock.ShapeData;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import net.minecraft.core.BlockPos;

public class GasTurbine extends MachineTemplateMultiblock {
    private static final ShapeData SHAPE = ITShapes.get("gas_turbine");
    public static final GasTurbine INSTANCE = new GasTurbine();

    public GasTurbine() { super(Reference.rl("multiblocks/gas_turbine"), SHAPE.masterPos, SHAPE.triggerPos, new BlockPos(SHAPE.width,SHAPE.height,SHAPE.length), SHAPE.manualScale, MultiblockRegistry.GAS_TURBINE); }
}
