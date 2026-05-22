package mctmods.immersivetechnology.core.integration.jade;

import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.*;
import mctmods.immersivetechnology.common.multiblocks.stone.logic.CoolingTowerLogic;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum ITStatusDataProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final long ACTIVE_COOLDOWN_TICKS = 40;
    private static final Map<BlockPos, Long> lastActiveTick = new ConcurrentHashMap<>();

    @Override public ResourceLocation getUid() { return ITLib.rl("status"); }

    @Override public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof IMultiblockBE<?> mbe) {
            IMultiblockState state = mbe.getHelper().getState();
            if (state == null) { return; }
            boolean active = false;
            boolean fuelEmpty = false;
            if (state instanceof DistillerLogic.State distiller) { active = distiller.isActive(); }
            else if (state instanceof BoilerLiquidLogic.State boiler) { active = boiler.active; fuelEmpty = boiler.tanks.input1().getFluid().isEmpty(); }
            else if (state instanceof AlternatorLogic.State alternator) { active = alternator.speed > 0; }
            else if (state instanceof BoilerSolidLogic.State boilerSolid) { active = boilerSolid.active; fuelEmpty = boilerSolid.inventory.getStackInSlot(BoilerSolidLogic.INPUT_FUEL_SLOT).isEmpty(); }
            else if (state instanceof BoilerTankLogic.State boilerTank) { active = boilerTank.active; }
            else if (state instanceof CoolingTowerLogic.State coolingTower) { active = coolingTower.active; }
            else if (state instanceof GasTurbineLogic.State gas) { active = gas.active; fuelEmpty = gas.tanks.input().getFluid().isEmpty(); }
            else if (state instanceof HeatExchangerLogic.State heatExchanger) { active = heatExchanger.active; }
            else if (state instanceof ElectrolyticCrucibleBatteryLogic.State electrolyticCrucibleBattery) { active = electrolyticCrucibleBattery.active; }
            else if (state instanceof SolarMelterLogic.State melter) { active = melter.active; fuelEmpty = melter.tanks.input().getFluid().isEmpty(); }
            else if (state instanceof SolarReflectorLogic.State reflector) { active = reflector.active; }
            else if (state instanceof SolarTowerLogic.State tower) { active = tower.active; fuelEmpty = tower.tanks.input().getFluid().isEmpty(); }
            else if (state instanceof SteamTurbineLogic.State steam) { active = steam.active; fuelEmpty = steam.tanks.input().getFluid().isEmpty(); }
            else if (state instanceof SteelSheetmetalTankLogic.State tank) { active = tank.active; }

            long now = accessor.getLevel().getGameTime();
            BlockPos pos = accessor.getPosition();
            Long last = lastActiveTick.get(pos);
            if (active) {
                lastActiveTick.put(pos, now);
            } else if (last != null && now - last < ACTIVE_COOLDOWN_TICKS) {
                active = true;
            }

            data.putBoolean("ITActive", active);
            if (fuelEmpty) { data.putBoolean("ITFuelEmpty", true); }
        }
    }
}
