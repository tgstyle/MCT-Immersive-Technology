package mctmods.immersivetechnology.client.render.multiblock;

import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityRadiatorMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartRadiator;

import net.minecraft.block.Block;

public class TileRenderRadiator extends TileRenderITMultiblockStatic<TileEntityRadiatorMaster> {
    @Override protected int getTotalBlocks() { return TileEntityITMultiblockPartRadiator.instance.width * TileEntityITMultiblockPartRadiator.instance.length * TileEntityITMultiblockPartRadiator.instance.height; }

    @Override protected Block getMultiblockBlock() { return ITContent.blockMetalMultiblock; }
}
