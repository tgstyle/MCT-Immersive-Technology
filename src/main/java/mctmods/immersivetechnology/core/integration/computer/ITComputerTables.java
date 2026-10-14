package mctmods.immersivetechnology.core.integration.computer;

import mctmods.immersivetechnology.common.multiblocks.metal.logic.BoilerLiquidLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.BoilerSolidLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.BoilerTankLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.DistillerLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.GasTurbineLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.HeatExchangerLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.SolarCollectorLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.SolarTowerLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.SteamTurbineLogic;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;

import com.immersiveconvergence.api.compat.ICComputerControl;
import com.immersiveconvergence.api.compat.ICComputerTable;
import com.immersiveconvergence.api.util.ComputerValues;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ITComputerTables {
    public static final ICComputerTable<BoilerLiquidLogic.State> BOILER_LIQUID = ICComputerControl.multiblock("it_boiler_liquid", MultiblockRegistry.BOILER_LIQUID, ICComputerControl.at(() -> BoilerLiquidLogic.REDSTONE_POI))
            .add("getHeat", state -> state.heatLevel)
            .add("isPilotLit", state -> state.pilotLit)
            .add("getFuelTankInfo", state -> ComputerValues.tankInfo(state.tanks.input1()))
            .add("getFullCanisters", state -> canister(state.inventory, BoilerLiquidLogic.INPUT_FUEL_SLOT_FILLED))
            .add("getEmptyCanisters", state -> canister(state.inventory, BoilerLiquidLogic.INPUT_FUEL_SLOT_EMPTY));
    public static final ICComputerTable<BoilerSolidLogic.State> BOILER_SOLID = ICComputerControl.multiblock("it_boiler_solid", MultiblockRegistry.BOILER_SOLID, ICComputerControl.at(() -> BoilerSolidLogic.REDSTONE_POI))
            .add("getHeat", state -> state.heatLevel)
            .add("isPilotLit", state -> state.pilotLit)
            .add("getBurnRemaining", state -> state.burnRemaining)
            .add("getFuelStack", state -> state.inventory.getStackInSlot(BoilerSolidLogic.INPUT_FUEL_SLOT));
    public static final ICComputerTable<BoilerTankLogic.State> BOILER_TANK = ICComputerControl.multiblock("it_boiler_tank", MultiblockRegistry.BOILER_TANK, ICComputerControl.atAny(() -> BoilerTankLogic.COMPARATOR_POSITIONS))
            .add("getHeat", state -> state.heatLevel)
            .add("getInputTankInfo", state -> ComputerValues.tankInfo(state.tanks.input()))
            .add("getOutputTankInfo", state -> ComputerValues.tankInfo(state.tanks.output()))
            .add("getFullCanisters", state -> canisters(state.inventory, BoilerTankLogic.INPUT_SLOT_FILLED, BoilerTankLogic.OUTPUT_SLOT_FILLED))
            .add("getEmptyCanisters", state -> canisters(state.inventory, BoilerTankLogic.INPUT_SLOT_EMPTY, BoilerTankLogic.OUTPUT_SLOT_EMPTY));
    public static final ICComputerTable<DistillerLogic.State> DISTILLER = ICComputerControl.multiblock("it_distiller", MultiblockRegistry.DISTILLER, ICComputerControl.at(() -> DistillerLogic.REDSTONE_POI))
            .add("getInputTankInfo", state -> ComputerValues.tankInfo(state.tanks.input()))
            .add("getOutputTankInfo", state -> ComputerValues.tankInfo(state.tanks.output()))
            .add("getEnergyStored", state -> state.energy.getEnergyStored())
            .add("getMaxEnergyStored", state -> state.energy.getMaxEnergyStored())
            .add("getFullCanisters", state -> canisters(state.inventory, DistillerLogic.SLOT_INPUT_FILLED, DistillerLogic.SLOT_OUTPUT_FILLED))
            .add("getEmptyCanisters", state -> canisters(state.inventory, DistillerLogic.SLOT_INPUT_EMPTY, DistillerLogic.SLOT_OUTPUT_EMPTY));
    public static final ICComputerTable<GasTurbineLogic.State> GAS_TURBINE = ICComputerControl.multiblock("it_gas_turbine", MultiblockRegistry.GAS_TURBINE, ICComputerControl.at(() -> GasTurbineLogic.REDSTONE_POI))
            .add("getSpeed", state -> state.speed)
            .add("getInputTankInfo", state -> ComputerValues.tankInfo(state.tanks.input()))
            .add("getOutputTankInfo", state -> ComputerValues.tankInfo(state.tanks.output()));
    public static final ICComputerTable<HeatExchangerLogic.State> HEAT_EXCHANGER = ICComputerControl.multiblock("it_heat_exchanger", MultiblockRegistry.HEAT_EXCHANGER, ICComputerControl.at(() -> HeatExchangerLogic.REDSTONE_POI))
            .add("getFirstInputTankInfo", state -> ComputerValues.tankInfo(state.tanks.input0()))
            .add("getSecondInputTankInfo", state -> ComputerValues.tankInfo(state.tanks.input1()))
            .add("getFirstOutputTankInfo", state -> ComputerValues.tankInfo(state.tanks.output0()))
            .add("getSecondOutputTankInfo", state -> ComputerValues.tankInfo(state.tanks.output1()));
    public static final ICComputerTable<SolarTowerLogic.State> SOLAR_TOWER = ICComputerControl.multiblock("it_solar_tower", MultiblockRegistry.SOLAR_TOWER, ICComputerControl.at(() -> SolarTowerLogic.REDSTONE_POI))
            .add("getReflectors", state -> state.reflectorStrength)
            .add("getInputTankInfo", state -> ComputerValues.tankInfo(state.tanks.input()))
            .add("getOutputTankInfo", state -> ComputerValues.tankInfo(state.tanks.output()))
            .add("getFullCanisters", state -> canisters(state.inventory, SolarCollectorLogic.SLOT_INPUT_FILLED, SolarCollectorLogic.SLOT_OUTPUT_FILLED))
            .add("getEmptyCanisters", state -> canisters(state.inventory, SolarCollectorLogic.SLOT_INPUT_EMPTY, SolarCollectorLogic.SLOT_OUTPUT_EMPTY));
    public static final ICComputerTable<SteamTurbineLogic.State> STEAM_TURBINE = ICComputerControl.multiblock("it_steam_turbine", MultiblockRegistry.STEAM_TURBINE, ICComputerControl.at(() -> SteamTurbineLogic.REDSTONE_POI))
            .add("getSpeed", state -> state.speed)
            .add("getTankInfo", state -> ComputerValues.tankInfo(state.tanks.input()))
            .add("getOutputTankInfo", state -> ComputerValues.tankInfo(state.tanks.output()));

    private ITComputerTables() {}

    private static Map<String, ItemStack> canister(IItemHandler inventory, int slot) {
        Map<String, ItemStack> canisters = new LinkedHashMap<>();
        canisters.put("fuel", inventory.getStackInSlot(slot));
        return canisters;
    }

    private static Map<String, ItemStack> canisters(IItemHandler inventory, int inputSlot, int outputSlot) {
        Map<String, ItemStack> canisters = new LinkedHashMap<>();
        canisters.put("input", inventory.getStackInSlot(inputSlot));
        canisters.put("output", inventory.getStackInSlot(outputSlot));
        return canisters;
    }
}
