package mctmods.immersivetechnology.client.render.multiblock;

import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityElectrolyticCrucibleBatteryMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartElectrolyticCrucibleBattery;

import net.minecraft.block.Block;

public class TileRenderElectrolyticCrucibleBattery extends TileRenderITMultiblockStatic<TileEntityElectrolyticCrucibleBatteryMaster> {
    @Override protected int getTotalBlocks() { return TileEntityITMultiblockPartElectrolyticCrucibleBattery.instance.width * TileEntityITMultiblockPartElectrolyticCrucibleBattery.instance.length * TileEntityITMultiblockPartElectrolyticCrucibleBattery.instance.height; }

    @Override protected Block getMultiblockBlock() { return ITContent.blockMetalMultiblock; }
}
