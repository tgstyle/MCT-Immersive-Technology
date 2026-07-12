package mctmods.immersivetechnology.client.render.multiblock;

import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityCoolingTowerMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartCoolingTower;

import net.minecraft.block.Block;

public class TileRenderCoolingTower extends TileRenderITMultiblockStatic<TileEntityCoolingTowerMaster> {
    @Override protected int getTotalBlocks() { return TileEntityITMultiblockPartCoolingTower.instance.width * TileEntityITMultiblockPartCoolingTower.instance.length * TileEntityITMultiblockPartCoolingTower.instance.height; }

    @Override protected Block getMultiblockBlock() { return ITContent.blockMetalMultiblock; }
}
