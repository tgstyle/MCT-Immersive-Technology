package mctmods.immersivetechnology.core;

import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = ITLib.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ITServerConfig {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.EnumValue<DisassemblyMode> DISASSEMBLY_MODE;

    public static final ForgeConfigSpec.DoubleValue ADVANCED_COKE_OVEN_SPEED_BASE;
    public static final ForgeConfigSpec.DoubleValue ADVANCED_COKE_OVEN_BASEHEATER_SPEED_INCREASE;
    public static final ForgeConfigSpec.DoubleValue ADVANCED_COKE_OVEN_BASEHEATER_SPEED_MULTIPLIER;
    public static final ForgeConfigSpec.IntValue ADVANCED_COKE_OVEN_BASEHEATER_MAX_ENERGY;
    public static final ForgeConfigSpec.IntValue ADVANCED_COKE_OVEN_BASEHEATER_ENERGY_CONSUMPTION;

    public static final ForgeConfigSpec.DoubleValue ALTERNATOR_POWER_FACTOR;
    public static final ForgeConfigSpec.IntValue ALTERNATOR_ENERGY_CAPACITY;
    public static final ForgeConfigSpec.DoubleValue ALTERNATOR_BASE_MASS;
    public static final ForgeConfigSpec.DoubleValue ALTERNATOR_FRICTION;
    public static final ForgeConfigSpec.IntValue ALTERNATOR_MAX_OUTPUT;

    public static final ForgeConfigSpec.IntValue BOILER_LIQUID_TANK_CAPACITY;
    public static final ForgeConfigSpec.DoubleValue BOILER_LIQUID_HEAT_LOSS_PER_TICK;
    public static final ForgeConfigSpec.DoubleValue BOILER_LIQUID_PILOT_HEAT;

    public static final ForgeConfigSpec.DoubleValue BOILER_SOLID_HEAT_LOSS_PER_TICK;
    public static final ForgeConfigSpec.DoubleValue BOILER_SOLID_PILOT_HEAT;
    public static final ForgeConfigSpec.IntValue BOILER_SOLID_PILOT_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue BOILER_SOLID_DEFAULT_HEAT_PER_TICK;
    public static final ForgeConfigSpec.IntValue BOILER_SOLID_BURN_TIME_DIVIDER;

    public static final ForgeConfigSpec.IntValue BOILER_TANK_CAPACITY;
    public static final ForgeConfigSpec.IntValue BOILER_TANK_PROGRESS_LOSS_PER_TICK;

    public static final ForgeConfigSpec.DoubleValue COOLING_TOWER_BIOME_TEMP_FACTOR;
    public static final ForgeConfigSpec.IntValue COOLING_TOWER_INPUT_TANK_CAPACITY;
    public static final ForgeConfigSpec.IntValue COOLING_TOWER_OUTPUT_TANK_CAPACITY;

    public static final ForgeConfigSpec.IntValue DISTILLER_INPUT_TANK_CAPACITY;
    public static final ForgeConfigSpec.IntValue DISTILLER_OUTPUT_TANK_CAPACITY;
    public static final ForgeConfigSpec.IntValue DISTILLER_ENERGY_CAPACITY;

    public static final ForgeConfigSpec.IntValue ELECTROLYTIC_CRUCIBLE_BATTERY_INPUT_TANK_CAPACITY;
    public static final ForgeConfigSpec.IntValue ELECTROLYTIC_CRUCIBLE_BATTERY_OUTPUT_TANK_CAPACITY;
    public static final ForgeConfigSpec.IntValue ELECTROLYTIC_CRUCIBLE_BATTERY_ENERGY_CAPACITY;
    public static final ForgeConfigSpec.IntValue ELECTROLYTIC_CRUCIBLE_BATTERY_ENERGY_MAX_INPUT;

    public static final ForgeConfigSpec.IntValue GAS_TURBINE_INPUT_TANK_CAPACITY;
    public static final ForgeConfigSpec.IntValue GAS_TURBINE_OUTPUT_TANK_CAPACITY;
    public static final ForgeConfigSpec.IntValue GAS_TURBINE_ENERGY_CAPACITY_HV;
    public static final ForgeConfigSpec.IntValue GAS_TURBINE_ENERGY_CAPACITY_MV;
    public static final ForgeConfigSpec.IntValue GAS_TURBINE_STARTER_CONSUMPTION;
    public static final ForgeConfigSpec.IntValue GAS_TURBINE_SPARKPLUG_CONSUMPTION;
    public static final ForgeConfigSpec.DoubleValue GAS_TURBINE_BASE_MASS;
    public static final ForgeConfigSpec.DoubleValue GAS_TURBINE_DRIVE_TORQUE;
    public static final ForgeConfigSpec.DoubleValue GAS_TURBINE_FRICTION;
    public static final ForgeConfigSpec.DoubleValue GAS_TURBINE_MAX_SPEED_FACTOR;

    public static final ForgeConfigSpec.IntValue HEAT_EXCHANGER_INPUT_TANK_CAPACITY;
    public static final ForgeConfigSpec.IntValue HEAT_EXCHANGER_OUTPUT_TANK_CAPACITY;
    public static final ForgeConfigSpec.IntValue HEAT_EXCHANGER_ENERGY_CAPACITY;
    public static final ForgeConfigSpec.IntValue HEAT_EXCHANGER_ENERGY_MAX_IO;

    public static final ForgeConfigSpec.IntValue MELTING_CRUCIBLE_INPUT_TANK_CAPACITY;
    public static final ForgeConfigSpec.IntValue MELTING_CRUCIBLE_OUTPUT_TANK_CAPACITY;
    public static final ForgeConfigSpec.IntValue MELTING_CRUCIBLE_ENERGY_CAPACITY;
    public static final ForgeConfigSpec.DoubleValue MELTING_CRUCIBLE_HEAT_WORKING_LEVEL;
    public static final ForgeConfigSpec.DoubleValue MELTING_CRUCIBLE_HEAT_LOSS_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue MELTING_CRUCIBLE_HEAT_GAIN_BASE;
    public static final ForgeConfigSpec.IntValue MELTING_CRUCIBLE_ENERGY_PER_TICK_TO_HEAT;
    public static final ForgeConfigSpec.IntValue MELTING_CRUCIBLE_ENERGY_PER_TICK_TO_MAINTAIN;

    public static final ForgeConfigSpec.DoubleValue RADIATOR_BIOME_TEMP_FACTOR;
    public static final ForgeConfigSpec.IntValue RADIATOR_INPUT_TANK_CAPACITY;
    public static final ForgeConfigSpec.IntValue RADIATOR_OUTPUT_TANK_CAPACITY;
    public static final ForgeConfigSpec.DoubleValue RADIATOR_SPEED_MULTIPLIER;

    public static final ForgeConfigSpec.DoubleValue SOLAR_MELTER_DAY_MIN_HEAT_LOSS;
    public static final ForgeConfigSpec.DoubleValue SOLAR_MELTER_LOSS_PER_SECTION_DROP;
    public static final ForgeConfigSpec.DoubleValue SOLAR_MELTER_TEMP_DEPENDENT_LOSS_FACTOR;
    public static final ForgeConfigSpec.DoubleValue SOLAR_MELTER_HEAT_INCREASE_FACTOR;
    public static final ForgeConfigSpec.DoubleValue SOLAR_MELTER_TEMP_TO_MIN_REFLECTORS_DIVISOR;
    public static final ForgeConfigSpec.DoubleValue SOLAR_MELTER_REFLECTOR_TIER_OFFSET;
    public static final ForgeConfigSpec.IntValue SOLAR_MELTER_PROGRESS_LOSS_OFF_TEMP;
    public static final ForgeConfigSpec.DoubleValue SOLAR_MELTER_SPEED_MULTIPLIER;
    public static final ForgeConfigSpec.IntValue SOLAR_MELTER_INPUT_TANK_CAPACITY;
    public static final ForgeConfigSpec.IntValue SOLAR_MELTER_OUTPUT_TANK_CAPACITY;

    public static final ForgeConfigSpec.DoubleValue SOLAR_TOWER_DAY_MIN_HEAT_LOSS;
    public static final ForgeConfigSpec.DoubleValue SOLAR_TOWER_LOSS_PER_SECTION_DROP;
    public static final ForgeConfigSpec.DoubleValue SOLAR_TOWER_TEMP_DEPENDENT_LOSS_FACTOR;
    public static final ForgeConfigSpec.DoubleValue SOLAR_TOWER_HEAT_INCREASE_FACTOR;
    public static final ForgeConfigSpec.DoubleValue SOLAR_TOWER_TEMP_TO_MIN_REFLECTORS_DIVISOR;
    public static final ForgeConfigSpec.DoubleValue SOLAR_TOWER_REFLECTOR_TIER_OFFSET;
    public static final ForgeConfigSpec.IntValue SOLAR_TOWER_PROGRESS_LOSS_OFF_TEMP;
    public static final ForgeConfigSpec.DoubleValue SOLAR_TOWER_SPEED_MULTIPLIER;
    public static final ForgeConfigSpec.IntValue SOLAR_TOWER_INPUT_TANK_CAPACITY;
    public static final ForgeConfigSpec.IntValue SOLAR_TOWER_OUTPUT_TANK_CAPACITY;

    public static final ForgeConfigSpec.IntValue STEAM_TURBINE_INPUT_TANK_CAPACITY;
    public static final ForgeConfigSpec.IntValue STEAM_TURBINE_OUTPUT_TANK_CAPACITY;
    public static final ForgeConfigSpec.DoubleValue STEAM_TURBINE_BASE_MASS;
    public static final ForgeConfigSpec.DoubleValue STEAM_TURBINE_DRIVE_TORQUE;
    public static final ForgeConfigSpec.DoubleValue STEAM_TURBINE_FRICTION;
    public static final ForgeConfigSpec.DoubleValue STEAM_TURBINE_MAX_SPEED_FACTOR;

    public static final ForgeConfigSpec.IntValue STEEL_SHEETMETAL_TANK_CAPACITY;
    public static final ForgeConfigSpec.IntValue STEEL_SHEETMETAL_TANK_TRANSFER_SPEED;

    public static DisassemblyMode disassemblyMode = DisassemblyMode.PROCESS_QUEUE;

    public static double advancedCokeOvenSpeedBase = 1.0D;
    public static double advancedCokeOvenBaseheaterSpeedIncrease = 0.25D;
    public static double advancedCokeOvenBaseheaterSpeedMultiplier = 1.25D;
    public static int advancedCokeOvenBaseheaterMaxEnergy = 8000;
    public static int advancedCokeOvenBaseheaterEnergyConsumption = 32;

    public static double alternatorPowerFactor = 1.0D;
    public static int alternatorEnergyCapacity = 1200000;
    public static double alternatorBaseMass = 2.0D;
    public static double alternatorFriction = 0.0D;
    public static int alternatorMaxOutput = 12288;

    public static int boilerLiquidTankCapacity = 24000;
    public static double boilerLiquidHeatLossPerTick = 0.2D;
    public static double boilerLiquidPilotHeat = 20.0D;

    public static double boilerSolidHeatLossPerTick = 0.2D;
    public static double boilerSolidPilotHeat = 20.0D;
    public static int boilerSolidPilotMultiplier = 15;
    public static double boilerSolidDefaultHeatPerTick = 0.1D;
    public static int boilerSolidBurnTimeDivider = 10;

    public static int boilerTankCapacity = 24000;
    public static int boilerTankProgressLossPerTick = 1;

    public static double coolingTowerBiomeTempFactor = 0.5D;
    public static int coolingTowerInputTankCapacity = 24000;
    public static int coolingTowerOutputTankCapacity = 24000;

    public static int distillerInputTankCapacity = 24000;
    public static int distillerOutputTankCapacity = 24000;
    public static int distillerEnergyCapacity = 32000;

    public static int electrolyticCrucibleBatteryInputTankCapacity = 10000;
    public static int electrolyticCrucibleBatteryOutputTankCapacity = 10000;
    public static int electrolyticCrucibleBatteryEnergyCapacity = 16000;
    public static int electrolyticCrucibleBatteryEnergyMaxInput = 4096;

    public static int gasTurbineInputTankCapacity = 12000;
    public static int gasTurbineOutputTankCapacity = 12000;
    public static int gasTurbineEnergyCapacityHV = 8192;
    public static int gasTurbineEnergyCapacityMV = 2048;
    public static int gasTurbineStarterConsumption = 4096;
    public static int gasTurbineSparkplugConsumption = 1024;
    public static double gasTurbineBaseMass = 8.0D;
    public static double gasTurbineDriveTorque = 30.0D;
    public static double gasTurbineFriction = 60.0D;
    public static double gasTurbineMaxSpeedFactor = 0.5D;

    public static int heatExchangerInputTankCapacity = 10000;
    public static int heatExchangerOutputTankCapacity = 10000;
    public static int heatExchangerEnergyCapacity = 2048;
    public static int heatExchangerEnergyMaxIO = 1024;

    public static int meltingCrucibleInputTankCapacity = 10000;
    public static int meltingCrucibleOutputTankCapacity = 10000;
    public static int meltingCrucibleEnergyCapacity = 50000;
    public static double meltingCrucibleHeatWorkingLevel = 1000.0D;
    public static double meltingCrucibleHeatLossMultiplier = 0.2D;
    public static double meltingCrucibleHeatGainBase = 0.55D;
    public static int meltingCrucibleEnergyPerTickToHeat = 1000;
    public static int meltingCrucibleEnergyPerTickToMaintain = 512;

    public static int radiatorInputTankCapacity = 8000;
    public static int radiatorOutputTankCapacity = 8000;
    public static double radiatorSpeedMultiplier = 1.0D;
    public static double radiatorBiomeTempFactor = 0.5D;

    public static double solarMelterDayMinHeatLoss = 0.0D;
    public static double solarMelterLossPerSectionDrop = 0.035D;
    public static double solarMelterTempDependentLossFactor = 0.00036D;
    public static double solarMelterHeatIncreaseFactor = 0.00568D;
    public static double solarMelterTempToMinReflectorsDivisor = 25.0D;
    public static double solarMelterReflectorTierOffset = 4.0D;
    public static int solarMelterProgressLossOffTemp = 2;
    public static double solarMelterSpeedMultiplier = 1.0D;
    public static int solarMelterInputTankCapacity = 12000;
    public static int solarMelterOutputTankCapacity = 12000;

    public static double solarTowerDayMinHeatLoss = 0.0D;
    public static double solarTowerLossPerSectionDrop = 0.035D;
    public static double solarTowerTempDependentLossFactor = 0.00036D;
    public static double solarTowerHeatIncreaseFactor = 0.00568D;
    public static double solarTowerTempToMinReflectorsDivisor = 25.0D;
    public static double solarTowerReflectorTierOffset = 4.0D;
    public static int solarTowerProgressLossOffTemp = 2;
    public static double solarTowerSpeedMultiplier = 1.0D;
    public static int solarTowerInputTankCapacity = 12000;
    public static int solarTowerOutputTankCapacity = 12000;

    public static int steamTurbineInputTankCapacity = 12000;
    public static int steamTurbineOutputTankCapacity = 12000;
    public static double steamTurbineBaseMass = 10.0D;
    public static double steamTurbineDriveTorque = 360.0D;
    public static double steamTurbineFriction = 0.0D;
    public static double steamTurbineMaxSpeedFactor = 1.0D;

    public static int steelSheetmetalTankCapacity = 2048000;
    public static int steelSheetmetalTankTransferSpeed = 1000;

    static {
        BUILDER.push("multiblocks");

        DISASSEMBLY_MODE = BUILDER.defineEnum("disassemblyMode", DisassemblyMode.PROCESS_QUEUE);

        BUILDER.push("advanced_coke_oven");
        ADVANCED_COKE_OVEN_SPEED_BASE = BUILDER.defineInRange("speed_base", 1.0D, 0.1D, 10.0D);
        ADVANCED_COKE_OVEN_BASEHEATER_SPEED_INCREASE = BUILDER.defineInRange("baseheater_speed_increase", 0.25D, 0.0D, 5.0D);
        ADVANCED_COKE_OVEN_BASEHEATER_SPEED_MULTIPLIER = BUILDER.defineInRange("baseheater_speed_multiplier", 1.25D, 1.0D, 5.0D);
        ADVANCED_COKE_OVEN_BASEHEATER_MAX_ENERGY = BUILDER.defineInRange("baseheater_max_energy", 8000, 1000, Integer.MAX_VALUE);
        ADVANCED_COKE_OVEN_BASEHEATER_ENERGY_CONSUMPTION = BUILDER.defineInRange("baseheater_energy_consumption", 32, 1, 1024);
        BUILDER.pop();

        BUILDER.push("alternator");
        ALTERNATOR_POWER_FACTOR = BUILDER.defineInRange("alternator_power_factor", 1.0D, 0.0D, 1000.0D);
        ALTERNATOR_ENERGY_CAPACITY = BUILDER.defineInRange("alternator_energy_capacity", 1200000, 1, Integer.MAX_VALUE);
        ALTERNATOR_BASE_MASS = BUILDER.defineInRange("alternator_base_mass", 2.0D, 0.0D, Double.MAX_VALUE);
        ALTERNATOR_FRICTION = BUILDER.defineInRange("alternator_friction", 0.0D, 0.0D, Double.MAX_VALUE);
        ALTERNATOR_MAX_OUTPUT = BUILDER.defineInRange("alternator_max_output", 12288, 0, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("boiler_liquid");
        BOILER_LIQUID_TANK_CAPACITY = BUILDER.defineInRange("tank_capacity", 24000, 1000, Integer.MAX_VALUE);
        BOILER_LIQUID_HEAT_LOSS_PER_TICK = BUILDER.defineInRange("heat_loss_per_tick", 0.2D, 0.0D, Double.MAX_VALUE);
        BOILER_LIQUID_PILOT_HEAT = BUILDER.defineInRange("pilot_heat", 20.0D, 0.0D, Double.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("boiler_solid");
        BOILER_SOLID_HEAT_LOSS_PER_TICK = BUILDER.defineInRange("heat_loss_per_tick", 0.2D, 0.0D, Double.MAX_VALUE);
        BOILER_SOLID_PILOT_HEAT = BUILDER.defineInRange("pilot_heat", 20.0D, 0.0D, Double.MAX_VALUE);
        BOILER_SOLID_PILOT_MULTIPLIER = BUILDER.defineInRange("pilot_multiplier", 15, 1, Integer.MAX_VALUE);
        BOILER_SOLID_DEFAULT_HEAT_PER_TICK = BUILDER.defineInRange("default_heat_per_tick", 0.1D, 0.0D, Double.MAX_VALUE);
        BOILER_SOLID_BURN_TIME_DIVIDER = BUILDER.defineInRange("burn_time_divider", 10, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("boiler_tank");
        BOILER_TANK_CAPACITY = BUILDER.defineInRange("tank_capacity", 24000, 1000, Integer.MAX_VALUE);
        BOILER_TANK_PROGRESS_LOSS_PER_TICK = BUILDER.defineInRange("progress_loss_per_tick", 1, 0, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("cooling_tower");
        COOLING_TOWER_INPUT_TANK_CAPACITY = BUILDER.defineInRange("input_tank_capacity", 24000, 1000, Integer.MAX_VALUE);
        COOLING_TOWER_OUTPUT_TANK_CAPACITY = BUILDER.defineInRange("output_tank_capacity", 24000, 1000, Integer.MAX_VALUE);
        COOLING_TOWER_BIOME_TEMP_FACTOR = BUILDER
                .comment("Biome temperature effect strength on cooling tower speed (0 = disabled). Cold biomes faster, hot slower. Neutral ~0.8.")
                .defineInRange("biome_temp_factor", 0.5D, 0.0D, 2.0D);
        BUILDER.pop();

        BUILDER.push("distiller");
        DISTILLER_INPUT_TANK_CAPACITY = BUILDER.defineInRange("input_tank_capacity", 24000, 1000, Integer.MAX_VALUE);
        DISTILLER_OUTPUT_TANK_CAPACITY = BUILDER.defineInRange("output_tank_capacity", 24000, 1000, Integer.MAX_VALUE);
        DISTILLER_ENERGY_CAPACITY = BUILDER.defineInRange("energy_capacity", 32000, 1000, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("electrolytic_crucible_battery");
        ELECTROLYTIC_CRUCIBLE_BATTERY_INPUT_TANK_CAPACITY = BUILDER.defineInRange("input_tank_capacity", 10000, 1000, Integer.MAX_VALUE);
        ELECTROLYTIC_CRUCIBLE_BATTERY_OUTPUT_TANK_CAPACITY = BUILDER.defineInRange("output_tank_capacity", 10000, 1000, Integer.MAX_VALUE);
        ELECTROLYTIC_CRUCIBLE_BATTERY_ENERGY_CAPACITY = BUILDER.defineInRange("energy_capacity", 16000, 1000, Integer.MAX_VALUE);
        ELECTROLYTIC_CRUCIBLE_BATTERY_ENERGY_MAX_INPUT = BUILDER.defineInRange("energy_max_input", 4096, 256, 65536);
        BUILDER.pop();

        BUILDER.push("gas_turbine");
        GAS_TURBINE_INPUT_TANK_CAPACITY = BUILDER.defineInRange("input_tank_capacity", 12000, 1000, Integer.MAX_VALUE);
        GAS_TURBINE_OUTPUT_TANK_CAPACITY = BUILDER.defineInRange("output_tank_capacity", 12000, 1000, Integer.MAX_VALUE);
        GAS_TURBINE_ENERGY_CAPACITY_HV = BUILDER.defineInRange("energy_capacity_hv", 8192, 1000, Integer.MAX_VALUE);
        GAS_TURBINE_ENERGY_CAPACITY_MV = BUILDER.defineInRange("energy_capacity_mv", 2048, 100, Integer.MAX_VALUE);
        GAS_TURBINE_STARTER_CONSUMPTION = BUILDER.defineInRange("starter_consumption", 4096, 0, Integer.MAX_VALUE);
        GAS_TURBINE_SPARKPLUG_CONSUMPTION = BUILDER.defineInRange("sparkplug_consumption", 1024, 0, Integer.MAX_VALUE);
        GAS_TURBINE_BASE_MASS = BUILDER.defineInRange("base_mass", 8.0D, 0.0D, Double.MAX_VALUE);
        GAS_TURBINE_DRIVE_TORQUE = BUILDER.defineInRange("drive_torque", 30.0D, 0.0D, Double.MAX_VALUE);
        GAS_TURBINE_FRICTION = BUILDER.defineInRange("friction", 60.0D, 0.0D, Double.MAX_VALUE);
        GAS_TURBINE_MAX_SPEED_FACTOR = BUILDER.defineInRange("max_speed_factor", 0.5D, 0.01D, 1.0D);
        BUILDER.pop();

        BUILDER.push("heat_exchanger");
        HEAT_EXCHANGER_INPUT_TANK_CAPACITY = BUILDER.defineInRange("input_tank_capacity", 10000, 1000, Integer.MAX_VALUE);
        HEAT_EXCHANGER_OUTPUT_TANK_CAPACITY = BUILDER.defineInRange("output_tank_capacity", 10000, 1000, Integer.MAX_VALUE);
        HEAT_EXCHANGER_ENERGY_CAPACITY = BUILDER.defineInRange("energy_capacity", 2048, 1000, Integer.MAX_VALUE);
        HEAT_EXCHANGER_ENERGY_MAX_IO = BUILDER.defineInRange("energy_max_io", 1024, 0, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("melting_crucible");
        MELTING_CRUCIBLE_INPUT_TANK_CAPACITY = BUILDER.defineInRange("input_tank_capacity", 10000, 1000, Integer.MAX_VALUE);
        MELTING_CRUCIBLE_OUTPUT_TANK_CAPACITY = BUILDER.defineInRange("output_tank_capacity", 10000, 1000, Integer.MAX_VALUE);
        MELTING_CRUCIBLE_ENERGY_CAPACITY = BUILDER.defineInRange("energy_capacity", 50000, 1000, Integer.MAX_VALUE);
        MELTING_CRUCIBLE_HEAT_WORKING_LEVEL = BUILDER.defineInRange("heat_workingLevel", 1000.0D, 100.0D, Double.MAX_VALUE);
        MELTING_CRUCIBLE_HEAT_LOSS_MULTIPLIER = BUILDER.defineInRange("heat_loss_multiplier", 0.2D, 0.0D, Double.MAX_VALUE);
        MELTING_CRUCIBLE_HEAT_GAIN_BASE = BUILDER.defineInRange("heat_gain_base", 0.55D, 0.1D, Double.MAX_VALUE);
        MELTING_CRUCIBLE_ENERGY_PER_TICK_TO_HEAT = BUILDER.defineInRange("energy_per_tick_heating", 1000, 32, Integer.MAX_VALUE);
        MELTING_CRUCIBLE_ENERGY_PER_TICK_TO_MAINTAIN = BUILDER.defineInRange("energy_per_tick_maintain", 512, 16, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("radiator");
        RADIATOR_INPUT_TANK_CAPACITY = BUILDER.defineInRange("input_tank_capacity", 8000, 1000, Integer.MAX_VALUE);
        RADIATOR_OUTPUT_TANK_CAPACITY = BUILDER.defineInRange("output_tank_capacity", 8000, 1000, Integer.MAX_VALUE);
        RADIATOR_SPEED_MULTIPLIER = BUILDER.defineInRange("speed_multiplier", 1.0D, 0.1D, 10.0D);
        RADIATOR_BIOME_TEMP_FACTOR = BUILDER
                .comment("Biome temperature effect strength on radiator speed (0 = disabled). Cold biomes faster, hot slower. Neutral ~0.8.")
                .defineInRange("biome_temp_factor", 0.5D, 0.0D, 2.0D);
        BUILDER.pop();

        BUILDER.push("solar_melter");
        SOLAR_MELTER_DAY_MIN_HEAT_LOSS = BUILDER.defineInRange("day_min_heat_loss", 0.0D, 0.0D, Double.MAX_VALUE);
        SOLAR_MELTER_LOSS_PER_SECTION_DROP = BUILDER.defineInRange("loss_per_section_drop", 0.035D, 0.0D, Double.MAX_VALUE);
        SOLAR_MELTER_TEMP_DEPENDENT_LOSS_FACTOR = BUILDER.defineInRange("temp_dependent_loss_factor", 0.00036D, 0.0D, Double.MAX_VALUE);
        SOLAR_MELTER_HEAT_INCREASE_FACTOR = BUILDER.defineInRange("heat_increase_factor", 0.00568D, 0.0D, Double.MAX_VALUE);
        SOLAR_MELTER_TEMP_TO_MIN_REFLECTORS_DIVISOR = BUILDER.defineInRange("temp_to_min_reflectors_divisor", 25.0D, 1.0D, Double.MAX_VALUE);
        SOLAR_MELTER_REFLECTOR_TIER_OFFSET = BUILDER.defineInRange("reflector_tier_offset", 4.0D, 0.0D, Double.MAX_VALUE);
        SOLAR_MELTER_PROGRESS_LOSS_OFF_TEMP = BUILDER.defineInRange("progress_loss_off_temp", 2, 0, Integer.MAX_VALUE);
        SOLAR_MELTER_SPEED_MULTIPLIER = BUILDER.defineInRange("speed_multiplier", 1.0D, 0.01D, 10.0D);
        SOLAR_MELTER_INPUT_TANK_CAPACITY = BUILDER.defineInRange("input_tank_capacity", 12000, 1000, Integer.MAX_VALUE);
        SOLAR_MELTER_OUTPUT_TANK_CAPACITY = BUILDER.defineInRange("output_tank_capacity", 12000, 1000, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("solar_tower");
        SOLAR_TOWER_DAY_MIN_HEAT_LOSS = BUILDER.defineInRange("day_min_heat_loss", 0.0D, 0.0D, Double.MAX_VALUE);
        SOLAR_TOWER_LOSS_PER_SECTION_DROP = BUILDER.defineInRange("loss_per_section_drop", 0.035D, 0.0D, Double.MAX_VALUE);
        SOLAR_TOWER_TEMP_DEPENDENT_LOSS_FACTOR = BUILDER.defineInRange("temp_dependent_loss_factor", 0.00036D, 0.0D, Double.MAX_VALUE);
        SOLAR_TOWER_HEAT_INCREASE_FACTOR = BUILDER.defineInRange("heat_increase_factor", 0.00568D, 0.0D, Double.MAX_VALUE);
        SOLAR_TOWER_TEMP_TO_MIN_REFLECTORS_DIVISOR = BUILDER.defineInRange("temp_to_min_reflectors_divisor", 25.0D, 1.0D, Double.MAX_VALUE);
        SOLAR_TOWER_REFLECTOR_TIER_OFFSET = BUILDER.defineInRange("reflector_tier_offset", 4.0D, 0.0D, Double.MAX_VALUE);
        SOLAR_TOWER_PROGRESS_LOSS_OFF_TEMP = BUILDER.defineInRange("progress_loss_off_temp", 2, 0, Integer.MAX_VALUE);
        SOLAR_TOWER_SPEED_MULTIPLIER = BUILDER.defineInRange("speed_multiplier", 1.0D, 0.01D, 10.0D);
        SOLAR_TOWER_INPUT_TANK_CAPACITY = BUILDER.defineInRange("input_tank_capacity", 12000, 1000, Integer.MAX_VALUE);
        SOLAR_TOWER_OUTPUT_TANK_CAPACITY = BUILDER.defineInRange("output_tank_capacity", 12000, 1000, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("steam_turbine");
        STEAM_TURBINE_INPUT_TANK_CAPACITY = BUILDER.defineInRange("input_tank_capacity", 12000, 1000, Integer.MAX_VALUE);
        STEAM_TURBINE_OUTPUT_TANK_CAPACITY = BUILDER.defineInRange("output_tank_capacity", 12000, 1000, Integer.MAX_VALUE);
        STEAM_TURBINE_BASE_MASS = BUILDER.defineInRange("base_mass", 10.0D, 0.0D, Double.MAX_VALUE);
        STEAM_TURBINE_DRIVE_TORQUE = BUILDER.defineInRange("drive_torque", 360.0D, 0.0D, Double.MAX_VALUE);
        STEAM_TURBINE_FRICTION = BUILDER.defineInRange("friction", 0.0D, 0.0D, Double.MAX_VALUE);
        STEAM_TURBINE_MAX_SPEED_FACTOR = BUILDER.defineInRange("max_speed_factor", 1.0D, 0.01D, 1.0D);
        BUILDER.pop();

        BUILDER.push("steel_sheetmetal_tank");
        STEEL_SHEETMETAL_TANK_CAPACITY = BUILDER.defineInRange("capacity", 2048000, 1000, Integer.MAX_VALUE);
        STEEL_SHEETMETAL_TANK_TRANSFER_SPEED = BUILDER.defineInRange("transfer_speed", 1000, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    public static void onConfig(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            disassemblyMode = DISASSEMBLY_MODE.get();

            advancedCokeOvenSpeedBase = ADVANCED_COKE_OVEN_SPEED_BASE.get();
            advancedCokeOvenBaseheaterSpeedIncrease = ADVANCED_COKE_OVEN_BASEHEATER_SPEED_INCREASE.get();
            advancedCokeOvenBaseheaterSpeedMultiplier = ADVANCED_COKE_OVEN_BASEHEATER_SPEED_MULTIPLIER.get();
            advancedCokeOvenBaseheaterMaxEnergy = ADVANCED_COKE_OVEN_BASEHEATER_MAX_ENERGY.get();
            advancedCokeOvenBaseheaterEnergyConsumption = ADVANCED_COKE_OVEN_BASEHEATER_ENERGY_CONSUMPTION.get();

            alternatorPowerFactor = ALTERNATOR_POWER_FACTOR.get();
            alternatorEnergyCapacity = ALTERNATOR_ENERGY_CAPACITY.get();
            alternatorBaseMass = ALTERNATOR_BASE_MASS.get();
            alternatorFriction = ALTERNATOR_FRICTION.get();
            alternatorMaxOutput = ALTERNATOR_MAX_OUTPUT.get();

            boilerLiquidTankCapacity = BOILER_LIQUID_TANK_CAPACITY.get();
            boilerLiquidHeatLossPerTick = BOILER_LIQUID_HEAT_LOSS_PER_TICK.get();
            boilerLiquidPilotHeat = BOILER_LIQUID_PILOT_HEAT.get();

            boilerSolidHeatLossPerTick = BOILER_SOLID_HEAT_LOSS_PER_TICK.get();
            boilerSolidPilotHeat = BOILER_SOLID_PILOT_HEAT.get();
            boilerSolidPilotMultiplier = BOILER_SOLID_PILOT_MULTIPLIER.get();
            boilerSolidDefaultHeatPerTick = BOILER_SOLID_DEFAULT_HEAT_PER_TICK.get();
            boilerSolidBurnTimeDivider = BOILER_SOLID_BURN_TIME_DIVIDER.get();

            boilerTankCapacity = BOILER_TANK_CAPACITY.get();
            boilerTankProgressLossPerTick = BOILER_TANK_PROGRESS_LOSS_PER_TICK.get();

            coolingTowerInputTankCapacity = COOLING_TOWER_INPUT_TANK_CAPACITY.get();
            coolingTowerOutputTankCapacity = COOLING_TOWER_OUTPUT_TANK_CAPACITY.get();
            coolingTowerBiomeTempFactor = COOLING_TOWER_BIOME_TEMP_FACTOR.get();

            distillerInputTankCapacity = DISTILLER_INPUT_TANK_CAPACITY.get();
            distillerOutputTankCapacity = DISTILLER_OUTPUT_TANK_CAPACITY.get();
            distillerEnergyCapacity = DISTILLER_ENERGY_CAPACITY.get();

            electrolyticCrucibleBatteryInputTankCapacity = ELECTROLYTIC_CRUCIBLE_BATTERY_INPUT_TANK_CAPACITY.get();
            electrolyticCrucibleBatteryOutputTankCapacity = ELECTROLYTIC_CRUCIBLE_BATTERY_OUTPUT_TANK_CAPACITY.get();
            electrolyticCrucibleBatteryEnergyCapacity = ELECTROLYTIC_CRUCIBLE_BATTERY_ENERGY_CAPACITY.get();
            electrolyticCrucibleBatteryEnergyMaxInput = ELECTROLYTIC_CRUCIBLE_BATTERY_ENERGY_MAX_INPUT.get();

            gasTurbineInputTankCapacity = GAS_TURBINE_INPUT_TANK_CAPACITY.get();
            gasTurbineOutputTankCapacity = GAS_TURBINE_OUTPUT_TANK_CAPACITY.get();
            gasTurbineEnergyCapacityHV = GAS_TURBINE_ENERGY_CAPACITY_HV.get();
            gasTurbineEnergyCapacityMV = GAS_TURBINE_ENERGY_CAPACITY_MV.get();
            gasTurbineStarterConsumption = GAS_TURBINE_STARTER_CONSUMPTION.get();
            gasTurbineSparkplugConsumption = GAS_TURBINE_SPARKPLUG_CONSUMPTION.get();
            gasTurbineBaseMass = GAS_TURBINE_BASE_MASS.get();
            gasTurbineDriveTorque = GAS_TURBINE_DRIVE_TORQUE.get();
            gasTurbineFriction = GAS_TURBINE_FRICTION.get();
            gasTurbineMaxSpeedFactor = GAS_TURBINE_MAX_SPEED_FACTOR.get();

            heatExchangerInputTankCapacity = HEAT_EXCHANGER_INPUT_TANK_CAPACITY.get();
            heatExchangerOutputTankCapacity = HEAT_EXCHANGER_OUTPUT_TANK_CAPACITY.get();
            heatExchangerEnergyCapacity = HEAT_EXCHANGER_ENERGY_CAPACITY.get();
            heatExchangerEnergyMaxIO = HEAT_EXCHANGER_ENERGY_MAX_IO.get();

            meltingCrucibleInputTankCapacity = MELTING_CRUCIBLE_INPUT_TANK_CAPACITY.get();
            meltingCrucibleOutputTankCapacity = MELTING_CRUCIBLE_OUTPUT_TANK_CAPACITY.get();
            meltingCrucibleEnergyCapacity = MELTING_CRUCIBLE_ENERGY_CAPACITY.get();
            meltingCrucibleHeatWorkingLevel = MELTING_CRUCIBLE_HEAT_WORKING_LEVEL.get();
            meltingCrucibleHeatLossMultiplier = MELTING_CRUCIBLE_HEAT_LOSS_MULTIPLIER.get();
            meltingCrucibleHeatGainBase = MELTING_CRUCIBLE_HEAT_GAIN_BASE.get();
            meltingCrucibleEnergyPerTickToHeat = MELTING_CRUCIBLE_ENERGY_PER_TICK_TO_HEAT.get();
            meltingCrucibleEnergyPerTickToMaintain = MELTING_CRUCIBLE_ENERGY_PER_TICK_TO_MAINTAIN.get();

            radiatorInputTankCapacity = RADIATOR_INPUT_TANK_CAPACITY.get();
            radiatorOutputTankCapacity = RADIATOR_OUTPUT_TANK_CAPACITY.get();
            radiatorSpeedMultiplier = RADIATOR_SPEED_MULTIPLIER.get();
            radiatorBiomeTempFactor = RADIATOR_BIOME_TEMP_FACTOR.get();

            solarMelterDayMinHeatLoss = SOLAR_MELTER_DAY_MIN_HEAT_LOSS.get();
            solarMelterLossPerSectionDrop = SOLAR_MELTER_LOSS_PER_SECTION_DROP.get();
            solarMelterTempDependentLossFactor = SOLAR_MELTER_TEMP_DEPENDENT_LOSS_FACTOR.get();
            solarMelterHeatIncreaseFactor = SOLAR_MELTER_HEAT_INCREASE_FACTOR.get();
            solarMelterTempToMinReflectorsDivisor = SOLAR_MELTER_TEMP_TO_MIN_REFLECTORS_DIVISOR.get();
            solarMelterReflectorTierOffset = SOLAR_MELTER_REFLECTOR_TIER_OFFSET.get();
            solarMelterProgressLossOffTemp = SOLAR_MELTER_PROGRESS_LOSS_OFF_TEMP.get();
            solarMelterSpeedMultiplier = SOLAR_MELTER_SPEED_MULTIPLIER.get();
            solarMelterInputTankCapacity = SOLAR_MELTER_INPUT_TANK_CAPACITY.get();
            solarMelterOutputTankCapacity = SOLAR_MELTER_OUTPUT_TANK_CAPACITY.get();

            solarTowerDayMinHeatLoss = SOLAR_TOWER_DAY_MIN_HEAT_LOSS.get();
            solarTowerLossPerSectionDrop = SOLAR_TOWER_LOSS_PER_SECTION_DROP.get();
            solarTowerTempDependentLossFactor = SOLAR_TOWER_TEMP_DEPENDENT_LOSS_FACTOR.get();
            solarTowerHeatIncreaseFactor = SOLAR_TOWER_HEAT_INCREASE_FACTOR.get();
            solarTowerTempToMinReflectorsDivisor = SOLAR_TOWER_TEMP_TO_MIN_REFLECTORS_DIVISOR.get();
            solarTowerReflectorTierOffset = SOLAR_TOWER_REFLECTOR_TIER_OFFSET.get();
            solarTowerProgressLossOffTemp = SOLAR_TOWER_PROGRESS_LOSS_OFF_TEMP.get();
            solarTowerSpeedMultiplier = SOLAR_TOWER_SPEED_MULTIPLIER.get();
            solarTowerInputTankCapacity = SOLAR_TOWER_INPUT_TANK_CAPACITY.get();
            solarTowerOutputTankCapacity = SOLAR_TOWER_OUTPUT_TANK_CAPACITY.get();

            steamTurbineInputTankCapacity = STEAM_TURBINE_INPUT_TANK_CAPACITY.get();
            steamTurbineOutputTankCapacity = STEAM_TURBINE_OUTPUT_TANK_CAPACITY.get();
            steamTurbineBaseMass = STEAM_TURBINE_BASE_MASS.get();
            steamTurbineDriveTorque = STEAM_TURBINE_DRIVE_TORQUE.get();
            steamTurbineFriction = STEAM_TURBINE_FRICTION.get();
            steamTurbineMaxSpeedFactor = STEAM_TURBINE_MAX_SPEED_FACTOR.get();

            steelSheetmetalTankCapacity = STEEL_SHEETMETAL_TANK_CAPACITY.get();
            steelSheetmetalTankTransferSpeed = STEEL_SHEETMETAL_TANK_TRANSFER_SPEED.get();
        }
    }

    public enum DisassemblyMode {
        PROCESS_QUEUE,
        TEMPLATE_BLOCKS
    }
}
