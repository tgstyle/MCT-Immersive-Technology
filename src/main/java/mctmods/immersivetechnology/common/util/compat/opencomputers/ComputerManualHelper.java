package mctmods.immersivetechnology.common.util.compat.opencomputers;

import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.lib.manual.ManualPages;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public class ComputerManualHelper {
	private static boolean added = false;

	public static void addManualContent() {
		if (added) { return; }
		added = true;

		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
			if (Multiblocks.enable.enable_steamTurbine) ManualHelper.getManual().addEntry("computer.steamTurbine", "computers",
					new ManualPages.Text(ManualHelper.getManual(), "computer.steamTurbine0")
			);
			if (Multiblocks.enable.enable_boiler) {
				ManualHelper.getManual().addEntry("computer.boilerTank", "computers",
						new ManualPages.Text(ManualHelper.getManual(), "computer.boilerTank0"),
						new ManualPages.Text(ManualHelper.getManual(), "computer.boilerTank1")
				);
				ManualHelper.getManual().addEntry("computer.boilerLiquid", "computers",
						new ManualPages.Text(ManualHelper.getManual(), "computer.boilerLiquid0"),
						new ManualPages.Text(ManualHelper.getManual(), "computer.boilerLiquid1")
				);
			}
			if (Multiblocks.enable.enable_boilerSolid) ManualHelper.getManual().addEntry("computer.boilerSolid", "computers",
					new ManualPages.Text(ManualHelper.getManual(), "computer.boilerSolid0")
			);
			if (Multiblocks.enable.enable_distiller) ManualHelper.getManual().addEntry("computer.distiller", "computers",
					new ManualPages.Text(ManualHelper.getManual(), "computer.distiller0"),
					new ManualPages.Text(ManualHelper.getManual(), "computer.distiller1")
			);
			if (Multiblocks.enable.enable_solarTower) ManualHelper.getManual().addEntry("computer.solarTower", "computers",
					new ManualPages.Text(ManualHelper.getManual(), "computer.solarTower0"),
					new ManualPages.Text(ManualHelper.getManual(), "computer.solarTower1")
			);
			if (Multiblocks.enable.enable_highPressureSteamTurbine) ManualHelper.getManual().addEntry("computer.highPressureSteamTurbine", "computers",
					new ManualPages.Text(ManualHelper.getManual(), "computer.highPressureSteamTurbine0")
			);
			if (Multiblocks.enable.enable_gasTurbine) ManualHelper.getManual().addEntry("computer.gasTurbine", "computers",
					new ManualPages.Text(ManualHelper.getManual(), "computer.gasTurbine0"),
					new ManualPages.Text(ManualHelper.getManual(), "computer.gasTurbine1")
			);
			if (Multiblocks.enable.enable_heatExchanger) ManualHelper.getManual().addEntry("computer.heatExchanger", "computers",
					new ManualPages.Text(ManualHelper.getManual(), "computer.heatExchanger0"),
					new ManualPages.Text(ManualHelper.getManual(), "computer.heatExchanger1")
			);
		}
	}
}
