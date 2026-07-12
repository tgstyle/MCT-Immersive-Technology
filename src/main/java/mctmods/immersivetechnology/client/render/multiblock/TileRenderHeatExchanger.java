package mctmods.immersivetechnology.client.render.multiblock;

import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityHeatExchangerMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartHeatExchanger;

import net.minecraft.block.Block;

public class TileRenderHeatExchanger extends TileRenderITMultiblockStatic<TileEntityHeatExchangerMaster> {
    @Override protected int getTotalBlocks() { return TileEntityITMultiblockPartHeatExchanger.instance.width * TileEntityITMultiblockPartHeatExchanger.instance.length * TileEntityITMultiblockPartHeatExchanger.instance.height; }

    @Override protected Block getMultiblockBlock() { return ITContent.blockMetalMultiblock; }
}
