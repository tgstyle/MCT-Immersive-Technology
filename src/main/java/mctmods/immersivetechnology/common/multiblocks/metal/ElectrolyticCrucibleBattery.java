package mctmods.immersivetechnology.common.multiblocks.metal;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import mctmods.immersivetechnology.common.multiblocks.helper.ITClientMultiblockProperties;
import mctmods.immersivetechnology.common.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.ElectrolyticCrucibleBatteryShape;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.core.BlockPos;

import java.util.function.Consumer;

public class ElectrolyticCrucibleBattery extends ITTemplateMultiblock {
    public static final ElectrolyticCrucibleBattery INSTANCE = new ElectrolyticCrucibleBattery();

    public ElectrolyticCrucibleBattery() { super(ITLib.rl("multiblocks/electrolytic_crucible_battery"), ElectrolyticCrucibleBatteryShape.MASTER_POS, ElectrolyticCrucibleBatteryShape.TRIGGER_POS, new BlockPos(ElectrolyticCrucibleBatteryShape.WIDTH, ElectrolyticCrucibleBatteryShape.HEIGHT, ElectrolyticCrucibleBatteryShape.LENGTH), ITMultiblockProvider.ELECTROLYTIC_CRUCIBLE_BATTERY); }

    @Override public float getManualScale() {
        return ElectrolyticCrucibleBatteryShape.MANUAL_SCALE;
    }

    @Override public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer) { consumer.accept(new ITClientMultiblockProperties(this, ElectrolyticCrucibleBatteryShape.CLIENT_OFFSET.getX(), ElectrolyticCrucibleBatteryShape.CLIENT_OFFSET.getY(), ElectrolyticCrucibleBatteryShape.CLIENT_OFFSET.getZ())); }

    @Override public boolean canBeMirrored() {
        return true;
    }
}
