package mctmods.immersivetechnology.common.multiblocks.metal;

import mctmods.immersivetechnology.common.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.ElectrolyticCrucibleBatteryShape;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockRegistry;

import net.minecraft.core.BlockPos;

public class ElectrolyticCrucibleBattery extends ITTemplateMultiblock {
    public static final ElectrolyticCrucibleBattery INSTANCE = new ElectrolyticCrucibleBattery();

    public ElectrolyticCrucibleBattery() { super(ITLib.rl("multiblocks/electrolytic_crucible_battery"), ElectrolyticCrucibleBatteryShape.MASTER_POS, ElectrolyticCrucibleBatteryShape.TRIGGER_POS, new BlockPos(ElectrolyticCrucibleBatteryShape.WIDTH, ElectrolyticCrucibleBatteryShape.HEIGHT, ElectrolyticCrucibleBatteryShape.LENGTH), ElectrolyticCrucibleBatteryShape.CLIENT_OFFSET, ElectrolyticCrucibleBatteryShape.MANUAL_SCALE, ITMultiblockRegistry.ELECTROLYTIC_CRUCIBLE_BATTERY); }
}
