package mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart;

import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityMeltingCrucibleSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.types.BlockType_MetalMultiblock1;
import com.immersiveconvergence.api.util.ICUtils;
import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;

public class TileEntityITMultiblockPartMeltingCrucible extends MachineTemplateMultiblock<TileEntityMeltingCrucibleSlave> {
    public static TileEntityITMultiblockPartMeltingCrucible instance = new TileEntityITMultiblockPartMeltingCrucible();

    public TileEntityITMultiblockPartMeltingCrucible() { super("IT:meltingCrucible", ITShapes.get("melting_crucible"), ICUtils.stateOf(ITContent.blockMetalMultiblock1, BlockType_MetalMultiblock1.MELTING_CRUCIBLE), ICUtils.stateOf(ITContent.blockMetalMultiblock1, BlockType_MetalMultiblock1.MELTING_CRUCIBLE_SLAVE), 1.5, 1.5, 1.5, 3.5); }
}
