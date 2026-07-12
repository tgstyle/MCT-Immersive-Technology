package mctmods.immersivetechnology.client.render.multiblock;

import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntitySolarMelterMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartSolarMelter;

import net.minecraft.block.Block;

public class TileRenderSolarMelter extends TileRenderITMultiblockStatic<TileEntitySolarMelterMaster> {
    @Override protected int getTotalBlocks() { return TileEntityITMultiblockPartSolarMelter.instance.width * TileEntityITMultiblockPartSolarMelter.instance.length * TileEntityITMultiblockPartSolarMelter.instance.height; }

    @Override protected Block getMultiblockBlock() { return ITContent.blockMetalMultiblock; }
}
