package mctmods.immersivetechnology.common.util.compat.computercraft;

import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.util.compat.ITCompatModule;
import mctmods.immersivetechnology.common.util.compat.computers.ITComputerTables;

import com.immersiveconvergence.api.compat.computercraft.ICPeripheralProvider;
import dan200.computercraft.api.ComputerCraftAPI;

public class CCHelper extends ITCompatModule {
	@Override public void preInit() {}

	@Override public void init() {
		ICPeripheralProvider provider = new ICPeripheralProvider();
		if (Multiblocks.enable.enable_boiler) {
			provider.add(ITComputerTables.BOILER_TANK);
			provider.add(ITComputerTables.BOILER_LIQUID);
		}
		if (Multiblocks.enable.enable_boilerSolid) { provider.add(ITComputerTables.BOILER_SOLID); }
		if (Multiblocks.enable.enable_steamTurbine) { provider.add(ITComputerTables.STEAM_TURBINE); }
		if (Multiblocks.enable.enable_distiller) { provider.add(ITComputerTables.DISTILLER); }
		if (Multiblocks.enable.enable_solarTower) { provider.add(ITComputerTables.SOLAR_TOWER); }
		if (Multiblocks.enable.enable_gasTurbine) { provider.add(ITComputerTables.GAS_TURBINE); }
		if (Multiblocks.enable.enable_heatExchanger) { provider.add(ITComputerTables.HEAT_EXCHANGER); }
		if (Multiblocks.enable.enable_highPressureSteamTurbine) { provider.add(ITComputerTables.HIGH_PRESSURE_STEAM_TURBINE); }
		ComputerCraftAPI.registerPeripheralProvider(provider);
	}

	@Override public void postInit() {}
}
