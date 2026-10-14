package mctmods.immersivetechnology.core.integration.computer;

import com.immersiveconvergence.api.compat.ICComputerTables;

public final class ComputerIntegration {
    private ComputerIntegration() {}

    public static void register() {
        ICComputerTables.register(ITComputerTables.BOILER_LIQUID);
        ICComputerTables.register(ITComputerTables.BOILER_SOLID);
        ICComputerTables.register(ITComputerTables.BOILER_TANK);
        ICComputerTables.register(ITComputerTables.DISTILLER);
        ICComputerTables.register(ITComputerTables.GAS_TURBINE);
        ICComputerTables.register(ITComputerTables.HEAT_EXCHANGER);
        ICComputerTables.register(ITComputerTables.SOLAR_TOWER);
        ICComputerTables.register(ITComputerTables.STEAM_TURBINE);
    }
}
