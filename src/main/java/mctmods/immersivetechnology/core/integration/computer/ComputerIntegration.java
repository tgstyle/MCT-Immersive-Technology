package mctmods.immersivetechnology.core.integration.computer;

import mctmods.immersivetechnology.common.multiblocks.metal.logic.BoilerLiquidLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.BoilerSolidLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.BoilerTankLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.DistillerLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.GasTurbineLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.HeatExchangerLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.SolarTowerLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.SteamTurbineLogic;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;

import com.immersiveconvergence.api.util.ComputerCallbacks;

public final class ComputerIntegration {
    private ComputerIntegration() {}

    public static void register() {
        ComputerCallbacks.registerMultiblock(MultiblockRegistry.BOILER_LIQUID, new BoilerLiquidCallbacks(), "it_boiler_liquid", ComputerCallbacks.at(() -> BoilerLiquidLogic.REDSTONE_POI));
        ComputerCallbacks.registerMultiblock(MultiblockRegistry.BOILER_SOLID, new BoilerSolidCallbacks(), "it_boiler_solid", ComputerCallbacks.at(() -> BoilerSolidLogic.REDSTONE_POI));
        ComputerCallbacks.registerMultiblock(MultiblockRegistry.BOILER_TANK, new BoilerTankCallbacks(), "it_boiler_tank", ComputerCallbacks.atAny(() -> BoilerTankLogic.COMPARATOR_POSITIONS));
        ComputerCallbacks.registerMultiblock(MultiblockRegistry.DISTILLER, new DistillerCallbacks(), "it_distiller", ComputerCallbacks.at(() -> DistillerLogic.REDSTONE_POI));
        ComputerCallbacks.registerMultiblock(MultiblockRegistry.GAS_TURBINE, new GasTurbineCallbacks(), "it_gas_turbine", ComputerCallbacks.at(() -> GasTurbineLogic.REDSTONE_POI));
        ComputerCallbacks.registerMultiblock(MultiblockRegistry.HEAT_EXCHANGER, new HeatExchangerCallbacks(), "it_heat_exchanger", ComputerCallbacks.at(() -> HeatExchangerLogic.REDSTONE_POI));
        ComputerCallbacks.registerMultiblock(MultiblockRegistry.SOLAR_TOWER, new SolarTowerCallbacks(), "it_solar_tower", ComputerCallbacks.at(() -> SolarTowerLogic.REDSTONE_POI));
        ComputerCallbacks.registerMultiblock(MultiblockRegistry.STEAM_TURBINE, new SteamTurbineCallbacks(), "it_steam_turbine", ComputerCallbacks.at(() -> SteamTurbineLogic.REDSTONE_POI));
    }
}
