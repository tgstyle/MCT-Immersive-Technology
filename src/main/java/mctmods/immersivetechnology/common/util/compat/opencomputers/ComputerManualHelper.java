package mctmods.immersivetechnology.common.util.compat.opencomputers;

import com.immersiveconvergence.api.manual.ICManual;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public class ComputerManualHelper {
	private static boolean added = false;

	public static void addManualContent() {
		if (added) { return; }
		added = true;

		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
			if (Multiblocks.enable.enable_steamTurbine) ICManual.addEntry("computer.steamTurbine", "computers",
					ICManual.text("computer.steamTurbine0")
			);
			if (Multiblocks.enable.enable_boiler) {
				ICManual.addEntry("computer.boilerTank", "computers",
						ICManual.text("computer.boilerTank0"),
						ICManual.text("computer.boilerTank1")
				);
				ICManual.addEntry("computer.boilerLiquid", "computers",
						ICManual.text("computer.boilerLiquid0"),
						ICManual.text("computer.boilerLiquid1")
				);
			}
			if (Multiblocks.enable.enable_boilerSolid) ICManual.addEntry("computer.boilerSolid", "computers",
					ICManual.text("computer.boilerSolid0")
			);
			if (Multiblocks.enable.enable_distiller) ICManual.addEntry("computer.distiller", "computers",
					ICManual.text("computer.distiller0"),
					ICManual.text("computer.distiller1")
			);
			if (Multiblocks.enable.enable_solarTower) ICManual.addEntry("computer.solarTower", "computers",
					ICManual.text("computer.solarTower0"),
					ICManual.text("computer.solarTower1")
			);
			if (Multiblocks.enable.enable_highPressureSteamTurbine) ICManual.addEntry("computer.highPressureSteamTurbine", "computers",
					ICManual.text("computer.highPressureSteamTurbine0")
			);
			if (Multiblocks.enable.enable_gasTurbine) ICManual.addEntry("computer.gasTurbine", "computers",
					ICManual.text("computer.gasTurbine0"),
					ICManual.text("computer.gasTurbine1")
			);
			if (Multiblocks.enable.enable_heatExchanger) ICManual.addEntry("computer.heatExchanger", "computers",
					ICManual.text("computer.heatExchanger0"),
					ICManual.text("computer.heatExchanger1")
			);
		}
	}
}
