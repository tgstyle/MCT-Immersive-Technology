package mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart;

import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityAlternatorSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.types.BlockType_MetalMultiblock;
import com.immersiveconvergence.api.util.ICUtils;
import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;

public class TileEntityITMultiblockPartAlternator extends MachineTemplateMultiblock<TileEntityAlternatorSlave> {
    public static TileEntityITMultiblockPartAlternator instance = new TileEntityITMultiblockPartAlternator();

    public TileEntityITMultiblockPartAlternator() { super("IT:Alternator", ITShapes.get("alternator"), ICUtils.stateOf(ITContent.blockMetalMultiblock, BlockType_MetalMultiblock.ALTERNATOR), ICUtils.stateOf(ITContent.blockMetalMultiblock, BlockType_MetalMultiblock.ALTERNATOR_SLAVE), 0.5, 1.5, 1.5, 4); }
}
