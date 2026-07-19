package mctmods.immersivetechnology.common.multiblocks.stone;

import mctmods.immersivetechnology.common.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.stone.shapes.CoolingTowerShape;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockRegistry;

import net.minecraft.core.BlockPos;

public class CoolingTower extends ITTemplateMultiblock {
    public static final CoolingTower INSTANCE = new CoolingTower();

    public CoolingTower() { super(ITLib.rl("multiblocks/cooling_tower"), CoolingTowerShape.MASTER_POS, CoolingTowerShape.TRIGGER_POS, new BlockPos(CoolingTowerShape.WIDTH,CoolingTowerShape.HEIGHT,CoolingTowerShape.LENGTH), CoolingTowerShape.CLIENT_OFFSET, CoolingTowerShape.MANUAL_SCALE, ITMultiblockRegistry.COOLING_TOWER); }
}
