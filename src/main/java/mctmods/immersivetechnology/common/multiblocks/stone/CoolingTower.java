package mctmods.immersivetechnology.common.multiblocks.stone;

import mctmods.immersivetechnology.common.multiblocks.helper.ModTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.stone.shapes.CoolingTowerShape;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;

import net.minecraft.core.BlockPos;

public class CoolingTower extends ModTemplateMultiblock {
    public static final CoolingTower INSTANCE = new CoolingTower();

    public CoolingTower() { super(Reference.rl("multiblocks/cooling_tower"), CoolingTowerShape.MASTER_POS, CoolingTowerShape.TRIGGER_POS, new BlockPos(CoolingTowerShape.WIDTH,CoolingTowerShape.HEIGHT,CoolingTowerShape.LENGTH), CoolingTowerShape.CLIENT_OFFSET, CoolingTowerShape.MANUAL_SCALE, MultiblockRegistry.COOLING_TOWER); }
}
