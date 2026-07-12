package mctmods.immersivetechnology.client.render.multiblock;

import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntitySolarTowerMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartSolarTower;

import net.minecraft.block.Block;

public class TileRenderSolarTower extends TileRenderITMultiblockStatic<TileEntitySolarTowerMaster> {
    @Override protected int getTotalBlocks() { return TileEntityITMultiblockPartSolarTower.instance.width * TileEntityITMultiblockPartSolarTower.instance.length * TileEntityITMultiblockPartSolarTower.instance.height; }

    @Override protected Block getMultiblockBlock() { return ITContent.blockMetalMultiblock; }
}
