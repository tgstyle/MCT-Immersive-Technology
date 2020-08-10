package mctmods.immersivetechnology.common.util.compat.opencomputers;

import mctmods.immersivetechnology.common.util.compat.ITCompatModule;

// Nearly the same thing as IE's OCHelper, so credit to BluSunrize for that

import li.cil.oc.api.API;

public class OCHelper extends ITCompatModule {

	@Override
	public void preInit() {
	}

	@Override
	public void init() {
		API.driver.add(new BoilerDriver());
		API.driver.add(new SteamTurbineDriver());
		API.driver.add(new DistillerDriver());
		API.driver.add(new SolarTowerDriver());
	}

	@Override
	public void postInit() {
		ComputerManualHelper.addManualContent();
	}
}
