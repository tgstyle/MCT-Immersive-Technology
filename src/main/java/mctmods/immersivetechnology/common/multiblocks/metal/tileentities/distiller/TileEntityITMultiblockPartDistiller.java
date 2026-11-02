package mctmods.immersivetechnology.common.multiblocks.metal.tileentities.distiller;

import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.common.multiblocks.metal.types.BlockType_MetalMultiblock;

import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import com.immersiveconvergence.api.util.ICUtils;

public class TileEntityITMultiblockPartDistiller extends MachineTemplateMultiblock<TileEntityDistillerSlave> {
    public static TileEntityITMultiblockPartDistiller instance = new TileEntityITMultiblockPartDistiller();

    public TileEntityITMultiblockPartDistiller() { super("IT:Distiller", ITShapes.get("distiller"), ICUtils.stateOf(ITContent.blockMetalMultiblock, BlockType_MetalMultiblock.DISTILLER), ICUtils.stateOf(ITContent.blockMetalMultiblock, BlockType_MetalMultiblock.DISTILLER_SLAVE), 1.5, 1.5, 1.5, 4); }
}
