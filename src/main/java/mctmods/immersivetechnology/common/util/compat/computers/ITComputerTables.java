package mctmods.immersivetechnology.common.util.compat.computers;

import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityBoilerLiquidMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityBoilerSolidMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityBoilerTankMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityDistillerMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityGasTurbineMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityHeatExchangerMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityHighPressureSteamTurbineMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntitySolarTowerMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntitySteamTurbineMaster;

import com.immersiveconvergence.api.compat.ICComputerControl;
import com.immersiveconvergence.api.compat.ICComputerTable;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.HashMap;

public final class ITComputerTables {
	public static final ICComputerTable<TileEntityBoilerLiquidMaster> BOILER_LIQUID = ICComputerControl.multiblock("it_boiler_liquid", TileEntityBoilerLiquidMaster.class, true)
			.add("getHeat", te -> te.heatLevel)
			.add("isPilotLit", te -> te.pilotLit)
			.add("getFuelTankInfo", te -> te.tanks[0].getInfo())
			.add("getFullCanisters", te -> fuelCanister(te.inventory,0))
			.add("getEmptyCanisters", te -> fuelCanister(te.inventory,1));
	public static final ICComputerTable<TileEntityBoilerSolidMaster> BOILER_SOLID = ICComputerControl.multiblock("it_boiler_solid", TileEntityBoilerSolidMaster.class, true)
			.add("getHeat", te -> te.heatLevel)
			.add("isPilotLit", te -> te.pilotLit)
			.add("getBurnRemaining", te -> te.burnRemaining)
			.add("getFuelStack", te -> te.inventory.get(0));
	public static final ICComputerTable<TileEntityBoilerTankMaster> BOILER_TANK = ICComputerControl.multiblock("it_boiler_tank", TileEntityBoilerTankMaster.class, false)
			.add("getHeat", te -> te.heatLevel)
			.add("getInputTankInfo", te -> te.tanks[0].getInfo())
			.add("getOutputTankInfo", te -> te.tanks[1].getInfo())
			.add("getFullCanisters", te -> canisters(te.inventory, 0, 3))
			.add("getEmptyCanisters", te -> canisters(te.inventory, 1, 2));
	public static final ICComputerTable<TileEntityDistillerMaster> DISTILLER = ICComputerControl.multiblock("it_distiller", TileEntityDistillerMaster.class, true)
			.add("getInputTankInfo", te -> te.tanks[0].getInfo())
			.add("getOutputTankInfo", te -> te.tanks[1].getInfo())
			.add("getEnergyStored", te -> te.energyStorage.getEnergyStored())
			.add("getMaxEnergyStored", te -> te.energyStorage.getMaxEnergyStored())
			.add("getFullCanisters", te -> canisters(te.inventory, 1, 3))
			.add("getEmptyCanisters", te -> canisters(te.inventory, 0, 2));
	public static final ICComputerTable<TileEntityGasTurbineMaster> GAS_TURBINE = ICComputerControl.multiblock("it_gas_turbine", TileEntityGasTurbineMaster.class, true)
			.add("getSpeed", te -> te.speed)
			.add("getInputTankInfo", te -> te.tanks[0].getInfo())
			.add("getOutputTankInfo", te -> te.tanks[1].getInfo());
	public static final ICComputerTable<TileEntityHeatExchangerMaster> HEAT_EXCHANGER = ICComputerControl.multiblock("it_heat_exchanger", TileEntityHeatExchangerMaster.class, true)
			.add("getFirstInputTankInfo", te -> te.tanks[0].getInfo())
			.add("getSecondInputTankInfo", te -> te.tanks[1].getInfo())
			.add("getFirstOutputTankInfo", te -> te.tanks[2].getInfo())
			.add("getSecondOutputTankInfo", te -> te.tanks[3].getInfo());
	public static final ICComputerTable<TileEntityHighPressureSteamTurbineMaster> HIGH_PRESSURE_STEAM_TURBINE = ICComputerControl.multiblock("it_high_pressure_steam_turbine", TileEntityHighPressureSteamTurbineMaster.class, true)
			.add("getSpeed", te -> te.speed)
			.add("getTankInfo", te -> te.tanks[0].getInfo())
			.add("getOutputTankInfo", te -> te.tanks[1].getInfo());
	public static final ICComputerTable<TileEntitySolarTowerMaster> SOLAR_TOWER = ICComputerControl.multiblock("it_solar_tower", TileEntitySolarTowerMaster.class, true)
			.add("getReflectors", te -> te.reflectorStrength)
			.add("getInputTankInfo", te -> te.tanks[0].getInfo())
			.add("getOutputTankInfo", te -> te.tanks[1].getInfo())
			.add("getFullCanisters", te -> canisters(te.inventory, 1, 3))
			.add("getEmptyCanisters", te -> canisters(te.inventory, 0, 2));
	public static final ICComputerTable<TileEntitySteamTurbineMaster> STEAM_TURBINE = ICComputerControl.multiblock("it_steam_turbine", TileEntitySteamTurbineMaster.class, true)
			.add("getSpeed", te -> te.speed)
			.add("getTankInfo", te -> te.tanks[0].getInfo())
			.add("getOutputTankInfo", te -> te.tanks[1].getInfo());

	private ITComputerTables() {}

	private static HashMap<String, ItemStack> fuelCanister(NonNullList<ItemStack> inventory, int slot) {
		HashMap<String, ItemStack> canisters = new HashMap<>(1);
		canisters.put("fuel", inventory.get(slot));
		return canisters;
	}

	private static HashMap<String, ItemStack> canisters(NonNullList<ItemStack> inventory, int inputSlot, int outputSlot) {
		HashMap<String, ItemStack> canisters = new HashMap<>(2);
		canisters.put("input", inventory.get(inputSlot));
		canisters.put("output", inventory.get(outputSlot));
		return canisters;
	}
}
