package mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart;

import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityGasTurbineSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.types.BlockType_MetalMultiblock1;
import com.immersiveconvergence.api.util.ICUtils;
import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;

public class TileEntityITMultiblockPartGasTurbine extends MachineTemplateMultiblock<TileEntityGasTurbineSlave> {

    public static TileEntityITMultiblockPartGasTurbine instance = new TileEntityITMultiblockPartGasTurbine();

    public TileEntityITMultiblockPartGasTurbine() { super("IT:GasTurbine", ITShapes.get("gas_turbine"), ICUtils.stateOf(ITContent.blockMetalMultiblock1, BlockType_MetalMultiblock1.GAS_TURBINE), ICUtils.stateOf(ITContent.blockMetalMultiblock1, BlockType_MetalMultiblock1.GAS_TURBINE_SLAVE), 2.3, 2.1, 2.5, 6.25); }
}
