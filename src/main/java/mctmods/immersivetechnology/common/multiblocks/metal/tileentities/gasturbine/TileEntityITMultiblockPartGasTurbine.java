package mctmods.immersivetechnology.common.multiblocks.metal.tileentities.gasturbine;

import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.common.multiblocks.metal.types.BlockType_MetalMultiblock1;

import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import com.immersiveconvergence.api.util.ICUtils;

public class TileEntityITMultiblockPartGasTurbine extends MachineTemplateMultiblock<TileEntityGasTurbineSlave> {

    public static TileEntityITMultiblockPartGasTurbine instance = new TileEntityITMultiblockPartGasTurbine();

    public TileEntityITMultiblockPartGasTurbine() { super("IT:GasTurbine", ITShapes.get("gas_turbine"), ICUtils.stateOf(ITContent.blockMetalMultiblock1, BlockType_MetalMultiblock1.GAS_TURBINE), ICUtils.stateOf(ITContent.blockMetalMultiblock1, BlockType_MetalMultiblock1.GAS_TURBINE_SLAVE), 2.3, 2.1, 2.5, 6.25); }
}
