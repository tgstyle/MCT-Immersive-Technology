package mctmods.immersivetechnology.client.render.multiblock;

import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityBoilerMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartBoiler;

import net.minecraft.block.Block;

public class TileRenderBoiler extends TileRenderITMultiblockStatic<TileEntityBoilerMaster> {
    @Override protected int getTotalBlocks() { return TileEntityITMultiblockPartBoiler.instance.width * TileEntityITMultiblockPartBoiler.instance.length * TileEntityITMultiblockPartBoiler.instance.height; }

    @Override protected Block getMultiblockBlock() { return ITContent.blockMetalMultiblock; }
}
