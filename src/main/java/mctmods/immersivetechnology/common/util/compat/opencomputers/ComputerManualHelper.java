package mctmods.immersivetechnology.common.util.compat.opencomputers;

import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.lib.manual.ManualPages;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

// Credit to Blusunrize, this thing is basically the same as the implementation in IE

public class ComputerManualHelper {
	private static boolean added = false;

	public static void addManualContent() {
		if(added) {
			return;
		}
		added = true;

		if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
			ManualHelper.getManual().addEntry("computer.steamTurbine", "computers",
					new ManualPages.Text(ManualHelper.getManual(), "computer.steamTurbine0")
			);
			ManualHelper.getManual().addEntry("computer.boiler", "computers",
					new ManualPages.Text(ManualHelper.getManual(), "computer.boiler0"),
					new ManualPages.Text(ManualHelper.getManual(), "computer.boiler1")
			);
			ManualHelper.getManual().addEntry("computer.distiller", "computers",
					new ManualPages.Text(ManualHelper.getManual(), "computer.distiller0"),
					new ManualPages.Text(ManualHelper.getManual(), "computer.distiller1")
			);
			ManualHelper.getManual().addEntry("computer.solarTower", "computers",
					new ManualPages.Text(ManualHelper.getManual(), "computer.solarTower0"),
					new ManualPages.Text(ManualHelper.getManual(), "computer.solarTower1")
			);
		}
	}
}
