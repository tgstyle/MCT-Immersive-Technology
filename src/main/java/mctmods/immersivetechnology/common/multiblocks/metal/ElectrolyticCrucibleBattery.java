package mctmods.immersivetechnology.common.multiblocks.metal;

import mctmods.immersivetechnology.common.multiblocks.helper.ModTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.ElectrolyticCrucibleBatteryShape;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;

import net.minecraft.core.BlockPos;

public class ElectrolyticCrucibleBattery extends ModTemplateMultiblock {
    public static final ElectrolyticCrucibleBattery INSTANCE = new ElectrolyticCrucibleBattery();

    public ElectrolyticCrucibleBattery() { super(Reference.rl("multiblocks/electrolytic_crucible_battery"), ElectrolyticCrucibleBatteryShape.MASTER_POS, ElectrolyticCrucibleBatteryShape.TRIGGER_POS, new BlockPos(ElectrolyticCrucibleBatteryShape.WIDTH, ElectrolyticCrucibleBatteryShape.HEIGHT, ElectrolyticCrucibleBatteryShape.LENGTH), ElectrolyticCrucibleBatteryShape.CLIENT_OFFSET, ElectrolyticCrucibleBatteryShape.MANUAL_SCALE, MultiblockRegistry.ELECTROLYTIC_CRUCIBLE_BATTERY); }
}
