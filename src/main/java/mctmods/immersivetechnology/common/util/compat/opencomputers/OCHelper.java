package mctmods.immersivetechnology.common.util.compat.opencomputers;

import li.cil.oc.api.API;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.util.compat.ITCompatModule;

public class OCHelper extends ITCompatModule {

	@Override public void preInit() { }

	@Override public void init() {
		if (Multiblocks.enable.enable_boiler) {
			API.driver.add(new BoilerTankDriver());
			API.driver.add(new BoilerLiquidDriver());
		}
		if (Multiblocks.enable.enable_boilerSolid) API.driver.add(new BoilerSolidDriver());
		if (Multiblocks.enable.enable_steamTurbine) API.driver.add(new SteamTurbineDriver());
		if (Multiblocks.enable.enable_distiller) API.driver.add(new DistillerDriver());
		if (Multiblocks.enable.enable_solarTower) API.driver.add(new SolarTowerDriver());
		if (Multiblocks.enable.enable_gasTurbine) API.driver.add(new GasTurbineDriver());
		if (Multiblocks.enable.enable_heatExchanger) API.driver.add(new HeatExchangerDriver());
		if (Multiblocks.enable.enable_highPressureSteamTurbine) API.driver.add(new HighPressureSteamTurbineDriver());
	}

	@Override public void postInit() {
		ComputerManualHelper.addManualContent();
	}
}
