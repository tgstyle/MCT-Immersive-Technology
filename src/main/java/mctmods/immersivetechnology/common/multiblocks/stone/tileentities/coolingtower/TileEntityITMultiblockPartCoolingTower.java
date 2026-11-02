package mctmods.immersivetechnology.common.multiblocks.stone.tileentities.coolingtower;

import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.common.multiblocks.stone.types.BlockType_StoneMultiblock;

import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import com.immersiveconvergence.api.util.ICUtils;

public class TileEntityITMultiblockPartCoolingTower extends MachineTemplateMultiblock<TileEntityCoolingTowerSlave> {
    public static TileEntityITMultiblockPartCoolingTower instance = new TileEntityITMultiblockPartCoolingTower();

    public TileEntityITMultiblockPartCoolingTower() { super("IT:CoolingTower", ITShapes.get("cooling_tower"), ICUtils.stateOf(ITContent.blockStoneMultiblock, BlockType_StoneMultiblock.COOLING_TOWER), ICUtils.stateOf(ITContent.blockStoneMultiblock, BlockType_StoneMultiblock.COOLING_TOWER_SLAVE), 1.75, 6.4, 7.25, 12.5); }
}
