package mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart;

import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntitySolarReflectorSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.types.BlockType_MetalMultiblock;
import com.immersiveconvergence.api.util.ICUtils;
import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;

public class TileEntityITMultiblockPartSolarReflector extends MachineTemplateMultiblock<TileEntitySolarReflectorSlave> {
    public static TileEntityITMultiblockPartSolarReflector instance = new TileEntityITMultiblockPartSolarReflector();

    public TileEntityITMultiblockPartSolarReflector() { super("IT:SolarReflector", ITShapes.get("solar_reflector"), ICUtils.stateOf(ITContent.blockMetalMultiblock, BlockType_MetalMultiblock.SOLAR_REFLECTOR), ICUtils.stateOf(ITContent.blockMetalMultiblock, BlockType_MetalMultiblock.SOLAR_REFLECTOR_SLAVE), 1.5, 2.5, 0.5, 8); }
}
