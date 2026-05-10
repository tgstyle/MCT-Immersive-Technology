package mctmods.immersivetechnology.core;

import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = ITLib.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ITServerConfig {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    // General / Global
    public static final ForgeConfigSpec.EnumValue<DisassemblyMode> DISASSEMBLY_MODE;
    public static final ForgeConfigSpec.IntValue CONFIG_BURN_TIME_DIVIDER;
    public static final ForgeConfigSpec.IntValue CONFIG_CREATIVE_BARREL_OUTPUT_AMOUNT;

    // Advanced Coke Oven
    public static final ForgeConfigSpec.DoubleValue ADVANCED_COKE_OVEN_SPEED_BASE;
    public static final ForgeConfigSpec.DoubleValue ADVANCED_COKE_OVEN_BASEHEATER_SPEED_INCREASE;
    public static final ForgeConfigSpec.DoubleValue ADVANCED_COKE_OVEN_BASEHEATER_SPEED_MULTIPLIER;
    public static final ForgeConfigSpec.IntValue ADVANCED_COKE_OVEN_BASEHEATER_MAX_ENERGY;
    public static final ForgeConfigSpec.IntValue ADVANCED_COKE_OVEN_BASEHEATER_ENERGY_CONSUMPTION;

    // Alternator
    public static final ForgeConfigSpec.DoubleValue ALTERNATOR_POWER_FACTOR;
    public static final ForgeConfigSpec.IntValue ALTERNATOR_ENERGY_CAPACITY;
    public static final ForgeConfigSpec.DoubleValue ALTERNATOR_BASE_MASS;
    public static final ForgeConfigSpec.DoubleValue ALTERNATOR_FRICTION;
    public static final ForgeConfigSpec.IntValue ALTERNATOR_MAX_OUTPUT;

    // Boiler - Liquid
    public static final ForgeConfigSpec.IntValue BOILER_LIQUID_TANK_CAPACITY;
    public static final ForgeConfigSpec.DoubleValue BOILER_LIQUID_HEAT_LOSS_PER_TICK;
    public static final ForgeConfigSpec.DoubleValue BOILER_LIQUID_PILOT_HEAT;

    // Boiler - Solid
    public static final ForgeConfigSpec.DoubleValue BOILER_SOLID_HEAT_LOSS_PER_TICK;
    public static final ForgeConfigSpec.DoubleValue BOILER_SOLID_PILOT_HEAT;
    public static final ForgeConfigSpec.IntValue BOILER_SOLID_PILOT_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue BOILER_SOLID_DEFAULT_HEAT_PER_TICK;

    // Boiler - Tank
    public static final ForgeConfigSpec.IntValue BOILER_TANK_CAPACITY;
    public static final ForgeConfigSpec.IntValue BOILER_TANK_PROGRESS_LOSS_PER_TICK;

    // Distiller
    public static final ForgeConfigSpec.IntValue DISTILLER_TANK_CAPACITY;
    public static final ForgeConfigSpec.IntValue DISTILLER_ENERGY_CAPACITY;

    // Gas Turbine
    public static final ForgeConfigSpec.IntValue GAS_TURBINE_TANK_CAPACITY;
    public static final ForgeConfigSpec.IntValue GAS_TURBINE_ENERGY_CAPACITY_HV;
    public static final ForgeConfigSpec.IntValue GAS_TURBINE_ENERGY_CAPACITY_MV;
    public static final ForgeConfigSpec.IntValue GAS_TURBINE_STARTER_CONSUMPTION;
    public static final ForgeConfigSpec.IntValue GAS_TURBINE_SPARKPLUG_CONSUMPTION;
    public static final ForgeConfigSpec.DoubleValue GAS_TURBINE_BASE_MASS;
    public static final ForgeConfigSpec.DoubleValue GAS_TURBINE_DRIVE_TORQUE;
    public static final ForgeConfigSpec.DoubleValue GAS_TURBINE_FRICTION;
    public static final ForgeConfigSpec.DoubleValue GAS_TURBINE_MAX_SPEED_FACTOR;

    // Heat Exchanger
    public static final ForgeConfigSpec.IntValue HEAT_EXCHANGER_INPUT_TANK_CAPACITY;
    public static final ForgeConfigSpec.IntValue HEAT_EXCHANGER_OUTPUT_TANK_CAPACITY;
    public static final ForgeConfigSpec.IntValue HEAT_EXCHANGER_ENERGY_CAPACITY;
    public static final ForgeConfigSpec.IntValue HEAT_EXCHANGER_ENERGY_MAX_IO;

    // Solar Melter
    public static final ForgeConfigSpec.DoubleValue SOLAR_MELTER_DAY_MIN_HEAT_LOSS;
    public static final ForgeConfigSpec.DoubleValue SOLAR_MELTER_LOSS_PER_SECTION_DROP;
    public static final ForgeConfigSpec.DoubleValue SOLAR_MELTER_TEMP_DEPENDENT_LOSS_FACTOR;
    public static final ForgeConfigSpec.DoubleValue SOLAR_MELTER_HEAT_INCREASE_FACTOR;
    public static final ForgeConfigSpec.DoubleValue SOLAR_MELTER_TEMP_TO_MIN_REFLECTORS_DIVISOR;
    public static final ForgeConfigSpec.DoubleValue SOLAR_MELTER_REFLECTOR_TIER_OFFSET;
    public static final ForgeConfigSpec.IntValue SOLAR_MELTER_PROGRESS_LOSS_OFF_TEMP;
    public static final ForgeConfigSpec.DoubleValue SOLAR_MELTER_SPEED_MULTIPLIER;

    // Solar Tower
    public static final ForgeConfigSpec.DoubleValue SOLAR_TOWER_DAY_MIN_HEAT_LOSS;
    public static final ForgeConfigSpec.DoubleValue SOLAR_TOWER_LOSS_PER_SECTION_DROP;
    public static final ForgeConfigSpec.DoubleValue SOLAR_TOWER_TEMP_DEPENDENT_LOSS_FACTOR;
    public static final ForgeConfigSpec.DoubleValue SOLAR_TOWER_HEAT_INCREASE_FACTOR;
    public static final ForgeConfigSpec.DoubleValue SOLAR_TOWER_TEMP_TO_MIN_REFLECTORS_DIVISOR;
    public static final ForgeConfigSpec.DoubleValue SOLAR_TOWER_REFLECTOR_TIER_OFFSET;
    public static final ForgeConfigSpec.IntValue SOLAR_TOWER_PROGRESS_LOSS_OFF_TEMP;
    public static final ForgeConfigSpec.DoubleValue SOLAR_TOWER_SPEED_MULTIPLIER;

    // Steam Turbine
    public static final ForgeConfigSpec.IntValue STEAM_TURBINE_TANK_CAPACITY;
    public static final ForgeConfigSpec.DoubleValue STEAM_TURBINE_BASE_MASS;
    public static final ForgeConfigSpec.DoubleValue STEAM_TURBINE_DRIVE_TORQUE;
    public static final ForgeConfigSpec.DoubleValue STEAM_TURBINE_FRICTION;
    public static final ForgeConfigSpec.DoubleValue STEAM_TURBINE_MAX_SPEED_FACTOR;

    // Steel Sheetmetal Tank
    public static final ForgeConfigSpec.IntValue STEEL_SHEETMETAL_TANK_CAPACITY;
    public static final ForgeConfigSpec.IntValue STEEL_SHEETMETAL_TANK_TRANSFER_SPEED;

    // Runtime values (synced from config)
    public static DisassemblyMode disassemblyMode = DisassemblyMode.PROCESS_QUEUE;

    // General
    public static int burnTimeDivider = 10;
    public static int creativeBarrelOutputAmount = Integer.MAX_VALUE;

    // Advanced Coke Oven
    public static double advancedCokeOvenSpeedBase = 1.0D;
    public static double advancedCokeOvenBaseheaterSpeedIncrease = 0.25D;
    public static double advancedCokeOvenBaseheaterSpeedMultiplier = 1.25D;
    public static int advancedCokeOvenBaseheaterMaxEnergy = 8000;
    public static int advancedCokeOvenBaseheaterEnergyConsumption = 32;

    // Alternator
    public static double alternatorPowerFactor = 1.0D;
    public static int alternatorEnergyCapacity = 1200000;
    public static double alternatorBaseMass = 2.0D;
    public static double alternatorFriction = 0.0D;
    public static int alternatorMaxOutput = 12288;

    // Boiler - Liquid
    public static int boilerLiquidTankCapacity = 24000;
    public static double boilerLiquidHeatLossPerTick = 0.2D;
    public static double boilerLiquidPilotHeat = 20.0D;

    // Boiler - Solid
    public static double boilerSolidHeatLossPerTick = 0.2D;
    public static double boilerSolidPilotHeat = 20.0D;
    public static int boilerSolidPilotMultiplier = 15;
    public static double boilerSolidDefaultHeatPerTick = 0.1D;

    // Boiler - Tank
    public static int boilerTankCapacity = 24000;
    public static int boilerTankProgressLossPerTick = 1;

    // Distiller
    public static int distillerTankCapacity = 24000;
    public static int distillerEnergyCapacity = 32000;

    // Gas Turbine
    public static int gasTurbineTankCapacity = 12000;
    public static int gasTurbineEnergyCapacityHV = 8192;
    public static int gasTurbineEnergyCapacityMV = 2048;
    public static int gasTurbineStarterConsumption = 4096;
    public static int gasTurbineSparkplugConsumption = 1024;
    public static double gasTurbineBaseMass = 8.0D;
    public static double gasTurbineDriveTorque = 30.0D;
    public static double gasTurbineFriction = 60.0D;
    public static double gasTurbineMaxSpeedFactor = 0.5D;

    // Heat Exchanger
    public static int heatExchangerInputTankCapacity = 10000;
    public static int heatExchangerOutputTankCapacity = 10000;
    public static int heatExchangerEnergyCapacity = 2048;
    public static int heatExchangerEnergyMaxIO = 1024;

    // Solar Melter
    public static double solarMelterDayMinHeatLoss = 0.0D;
    public static double solarMelterLossPerSectionDrop = 0.035D;
    public static double solarMelterTempDependentLossFactor = 0.00036D;
    public static double solarMelterHeatIncreaseFactor = 0.00568D;
    public static double solarMelterTempToMinReflectorsDivisor = 25.0D;
    public static double solarMelterReflectorTierOffset = 4.0D;
    public static int solarMelterProgressLossOffTemp = 2;
    public static double solarMelterSpeedMultiplier = 1.0D;

    // Solar Tower
    public static double solarTowerDayMinHeatLoss = 0.0D;
    public static double solarTowerLossPerSectionDrop = 0.035D;
    public static double solarTowerTempDependentLossFactor = 0.0006D;
    public static double solarTowerHeatIncreaseFactor = 0.00300D;
    public static double solarTowerTempToMinReflectorsDivisor = 25.0D;
    public static double solarTowerReflectorTierOffset = 4.0D;
    public static int solarTowerProgressLossOffTemp = 2;
    public static double solarTowerSpeedMultiplier = 1.0D;

    // Steam Turbine
    public static int steamTurbineTankCapacity = 12000;
    public static double steamTurbineBaseMass = 10.0D;
    public static double steamTurbineDriveTorque = 360.0D;
    public static double steamTurbineFriction = 0.0D;
    public static double steamTurbineMaxSpeedFactor = 1.0D;

    // Steel Sheetmetal Tank
    public static int steelSheetmetalTankCapacity = 2048000;
    public static int steelSheetmetalTankTransferSpeed = 1000;

    static {
        BUILDER.push("multiblocks");

        // General
        DISASSEMBLY_MODE = BUILDER
                .comment("Controls multiblock disassembly behavior. PROCESS_QUEUE = gradual block removal using fake player actions. TEMPLATE_BLOCKS = reverts structure to placeholder blocks (like vanilla IE behavior).")
                .defineEnum("disassemblyMode", DisassemblyMode.PROCESS_QUEUE);

        CONFIG_BURN_TIME_DIVIDER = BUILDER
                .comment("Divisor applied to fuel burn times (higher value = slower fuel consumption).")
                .defineInRange("burnTimeDivider", 10, 1, Integer.MAX_VALUE);

        // Advanced Coke Oven
        BUILDER.comment("Advanced Coke Oven settings").push("advanced_coke_oven");
        ADVANCED_COKE_OVEN_SPEED_BASE = BUILDER
                .comment("Base processing speed multiplier (1.0 = normal speed).")
                .defineInRange("speed_base", 1.0D, 0.1D, 10.0D);
        ADVANCED_COKE_OVEN_BASEHEATER_SPEED_INCREASE = BUILDER
                .comment("Additive speed bonus per active base heater.")
                .defineInRange("baseheater_speed_increase", 0.25D, 0.0D, 5.0D);
        ADVANCED_COKE_OVEN_BASEHEATER_SPEED_MULTIPLIER = BUILDER
                .comment("Multiplicative speed bonus per active base heater.")
                .defineInRange("baseheater_speed_multiplier", 1.25D, 1.0D, 5.0D);
        ADVANCED_COKE_OVEN_BASEHEATER_MAX_ENERGY = BUILDER
                .comment("Maximum energy storage capacity of the Advanced Coke Oven Base Heater (in FE).")
                .defineInRange("baseheater_max_energy", 8000, 1000, Integer.MAX_VALUE);
        ADVANCED_COKE_OVEN_BASEHEATER_ENERGY_CONSUMPTION = BUILDER
                .comment("Energy consumption per tick when the base heater is active (in FE/t).")
                .defineInRange("baseheater_energy_consumption", 32, 1, 1024);
        BUILDER.pop();

        // Alternator
        ALTERNATOR_POWER_FACTOR = BUILDER
                .comment("Global multiplier applied to Alternator power generation (1.0 = default, 2.0 = double output, 0.5 = half).")
                .defineInRange("alternator_power_factor", 1.0D, 0.0D, 1000.0D);
        ALTERNATOR_ENERGY_CAPACITY = BUILDER
                .comment("Internal energy buffer capacity of the Alternator (in FE).")
                .defineInRange("alternator_energy_capacity", 1200000, 1, Integer.MAX_VALUE);
        ALTERNATOR_BASE_MASS = BUILDER
                .comment("Base inertia mass for the Alternator's mechanical consumer.")
                .defineInRange("alternator_base_mass", 2.0D, 0.0D, Double.MAX_VALUE);
        ALTERNATOR_FRICTION = BUILDER
                .comment("Friction coefficient affecting rotational deceleration of the Alternator.")
                .defineInRange("alternator_friction", 0.0D, 0.0D, Double.MAX_VALUE);
        ALTERNATOR_MAX_OUTPUT = BUILDER
                .comment("Hard cap on FE/t output from the Alternator before power factor and other modifiers are applied.")
                .defineInRange("alternator_max_output", 12288, 0, Integer.MAX_VALUE);

        // Boiler - Solid
        BUILDER.comment("Solid Fuel Boiler settings").push("boiler_solid");
        BOILER_SOLID_HEAT_LOSS_PER_TICK = BUILDER
                .comment("Passive heat loss per server tick.")
                .defineInRange("heat_loss_per_tick", 0.2D, 0.0D, Double.MAX_VALUE);
        BOILER_SOLID_PILOT_HEAT = BUILDER
                .comment("Minimum heat maintained by the pilot light.")
                .defineInRange("pilot_heat", 20.0D, 0.0D, Double.MAX_VALUE);
        BOILER_SOLID_PILOT_MULTIPLIER = BUILDER
                .comment("Interval multiplier for pilot light fuel consumption (higher = less frequent usage).")
                .defineInRange("pilot_multiplier", 15, 1, Integer.MAX_VALUE);
        BOILER_SOLID_DEFAULT_HEAT_PER_TICK = BUILDER
                .comment("Heat gain per tick from fuel when no recipe is active.")
                .defineInRange("default_heat_per_tick", 0.1D, 0.0D, Double.MAX_VALUE);
        BUILDER.pop();

        // Boiler - Liquid
        BUILDER.comment("Liquid Fuel Boiler settings").push("boiler_liquid");
        BOILER_LIQUID_TANK_CAPACITY = BUILDER
                .comment("Input fuel tank size (mB).")
                .defineInRange("tank_capacity", 24000, 1000, Integer.MAX_VALUE);
        BOILER_LIQUID_HEAT_LOSS_PER_TICK = BUILDER
                .comment("Passive heat loss per server tick.")
                .defineInRange("heat_loss_per_tick", 0.2D, 0.0D, Double.MAX_VALUE);
        BOILER_LIQUID_PILOT_HEAT = BUILDER
                .comment("Minimum heat maintained by the pilot light.")
                .defineInRange("pilot_heat", 20.0D, 0.0D, Double.MAX_VALUE);
        BUILDER.pop();

        // Boiler - Tank
        BUILDER.comment("Tank Boiler settings").push("boiler_tank");
        BOILER_TANK_CAPACITY = BUILDER
                .comment("Capacity of input and output fluid tanks (mB).")
                .defineInRange("tank_capacity", 24000, 1000, Integer.MAX_VALUE);
        BOILER_TANK_PROGRESS_LOSS_PER_TICK = BUILDER
                .comment("Recipe progress lost per tick when below required heat.")
                .defineInRange("progress_loss_per_tick", 1, 0, Integer.MAX_VALUE);
        BUILDER.pop();

        // Distiller
        BUILDER.comment("Distiller settings").push("distiller");
        DISTILLER_TANK_CAPACITY = BUILDER
                .comment("Capacity of input and output fluid tanks (mB).")
                .defineInRange("tank_capacity", 24000, 1000, Integer.MAX_VALUE);
        DISTILLER_ENERGY_CAPACITY = BUILDER
                .comment("Internal energy buffer size (FE).")
                .defineInRange("energy_capacity", 32000, 1000, Integer.MAX_VALUE);
        BUILDER.pop();

        // Gas Turbine
        BUILDER.comment("Gas Turbine settings").push("gas_turbine");
        GAS_TURBINE_TANK_CAPACITY = BUILDER
                .comment("Capacity of input and output fluid tanks (mB).")
                .defineInRange("tank_capacity", 12000, 1000, Integer.MAX_VALUE);
        GAS_TURBINE_ENERGY_CAPACITY_HV = BUILDER
                .comment("High-voltage energy buffer capacity (FE).")
                .defineInRange("energy_capacity_hv", 8192, 1000, Integer.MAX_VALUE);
        GAS_TURBINE_ENERGY_CAPACITY_MV = BUILDER
                .comment("Medium-voltage buffer for starter/sparkplug (FE).")
                .defineInRange("energy_capacity_mv", 2048, 100, Integer.MAX_VALUE);
        GAS_TURBINE_STARTER_CONSUMPTION = BUILDER
                .comment("FE/t consumed by the electric starter motor.")
                .defineInRange("starter_consumption", 4096, 0, Integer.MAX_VALUE);
        GAS_TURBINE_SPARKPLUG_CONSUMPTION = BUILDER
                .comment("FE per ignition attempt by the sparkplug.")
                .defineInRange("sparkplug_consumption", 1024, 0, Integer.MAX_VALUE);
        GAS_TURBINE_BASE_MASS = BUILDER
                .comment("Base inertia mass for rotational dynamics.")
                .defineInRange("base_mass", 8.0D, 0.0D, Double.MAX_VALUE);
        GAS_TURBINE_DRIVE_TORQUE = BUILDER
                .comment("Base torque output from combustion (affects acceleration).")
                .defineInRange("drive_torque", 30.0D, 0.0D, Double.MAX_VALUE);
        GAS_TURBINE_FRICTION = BUILDER
                .comment("Friction coefficient affecting rotational deceleration.")
                .defineInRange("friction", 60.0D, 0.0D, Double.MAX_VALUE);
        GAS_TURBINE_MAX_SPEED_FACTOR = BUILDER
                .comment("Maximum achievable speed as fraction of MechanicalCapabilities.MAX_RPM.")
                .defineInRange("max_speed_factor", 0.5D, 0.01D, 1.0D);
        BUILDER.pop();

        // Heat Exchanger
        BUILDER.comment("Heat Exchanger settings").push("heat_exchanger");
        HEAT_EXCHANGER_INPUT_TANK_CAPACITY = BUILDER
                .comment("Capacity per input tank (mB).")
                .defineInRange("input_tank_capacity", 10000, 1000, Integer.MAX_VALUE);
        HEAT_EXCHANGER_OUTPUT_TANK_CAPACITY = BUILDER
                .comment("Capacity per output tank (mB).")
                .defineInRange("output_tank_capacity", 10000, 1000, Integer.MAX_VALUE);
        HEAT_EXCHANGER_ENERGY_CAPACITY = BUILDER
                .comment("Internal energy buffer capacity (FE).")
                .defineInRange("energy_capacity", 2048, 1000, Integer.MAX_VALUE);
        HEAT_EXCHANGER_ENERGY_MAX_IO = BUILDER
                .comment("Maximum FE/t that can be received or extracted.")
                .defineInRange("energy_max_io", 1024, 0, Integer.MAX_VALUE);
        BUILDER.pop();

        // Solar Melter
        BUILDER.comment("Solar Melter settings").push("solar_melter");
        SOLAR_MELTER_DAY_MIN_HEAT_LOSS = BUILDER
                .comment("Base heat loss per tick during daylight (before sky darkness penalties).")
                .defineInRange("day_min_heat_loss", 0.0D, 0.0D, Double.MAX_VALUE);
        SOLAR_MELTER_LOSS_PER_SECTION_DROP = BUILDER
                .comment("Additional heat loss per unit of sky darkness.")
                .defineInRange("loss_per_section_drop", 0.035D, 0.0D, Double.MAX_VALUE);
        SOLAR_MELTER_TEMP_DEPENDENT_LOSS_FACTOR = BUILDER
                .comment("Temperature-scaled heat loss multiplier.")
                .defineInRange("temp_dependent_loss_factor", 0.00036D, 0.0D, Double.MAX_VALUE);
        SOLAR_MELTER_HEAT_INCREASE_FACTOR = BUILDER
                .comment("Heat gain per reflector strength unit under ideal sunlight.")
                .defineInRange("heat_increase_factor", 0.00568D, 0.0D, Double.MAX_VALUE);
        SOLAR_MELTER_TEMP_TO_MIN_REFLECTORS_DIVISOR = BUILDER
                .comment("Divisor used to compute minimum reflectors needed for recipe temperature.")
                .defineInRange("temp_to_min_reflectors_divisor", 25.0D, 1.0D, Double.MAX_VALUE);
        SOLAR_MELTER_REFLECTOR_TIER_OFFSET = BUILDER
                .comment("Additional reflectors required per tier difference in temperature demand.")
                .defineInRange("reflector_tier_offset", 4.0D, 0.0D, Double.MAX_VALUE);
        SOLAR_MELTER_PROGRESS_LOSS_OFF_TEMP = BUILDER
                .comment("Recipe progress lost per tick below required temperature.")
                .defineInRange("progress_loss_off_temp", 2, 0, Integer.MAX_VALUE);
        SOLAR_MELTER_SPEED_MULTIPLIER = BUILDER
                .comment("Global process speed multiplier (1.0 = default).")
                .defineInRange("speed_multiplier", 1.0D, 0.01D, 10.0D);
        BUILDER.pop();

        // Solar Tower
        BUILDER.comment("Solar Tower settings").push("solar_tower");
        SOLAR_TOWER_DAY_MIN_HEAT_LOSS = BUILDER
                .comment("Base heat loss per tick during daylight (before sky darkness penalties).")
                .defineInRange("day_min_heat_loss", 0.0D, 0.0D, Double.MAX_VALUE);
        SOLAR_TOWER_LOSS_PER_SECTION_DROP = BUILDER
                .comment("Additional heat loss per unit of sky darkness.")
                .defineInRange("loss_per_section_drop", 0.035D, 0.0D, Double.MAX_VALUE);
        SOLAR_TOWER_TEMP_DEPENDENT_LOSS_FACTOR = BUILDER
                .comment("Temperature-scaled heat loss multiplier.")
                .defineInRange("temp_dependent_loss_factor", 0.0006D, 0.0D, Double.MAX_VALUE);
        SOLAR_TOWER_HEAT_INCREASE_FACTOR = BUILDER
                .comment("Heat gain per reflector strength unit under ideal sunlight.")
                .defineInRange("heat_increase_factor", 0.00300D, 0.0D, Double.MAX_VALUE);
        SOLAR_TOWER_TEMP_TO_MIN_REFLECTORS_DIVISOR = BUILDER
                .comment("Divisor used to compute minimum reflectors needed for recipe temperature.")
                .defineInRange("temp_to_min_reflectors_divisor", 25.0D, 1.0D, Double.MAX_VALUE);
        SOLAR_TOWER_REFLECTOR_TIER_OFFSET = BUILDER
                .comment("Additional reflectors required per tier difference in temperature demand.")
                .defineInRange("reflector_tier_offset", 4.0D, 0.0D, Double.MAX_VALUE);
        SOLAR_TOWER_PROGRESS_LOSS_OFF_TEMP = BUILDER
                .comment("Recipe progress lost per tick below required temperature.")
                .defineInRange("progress_loss_off_temp", 2, 0, Integer.MAX_VALUE);
        SOLAR_TOWER_SPEED_MULTIPLIER = BUILDER
                .comment("Global process speed multiplier (1.0 = default).")
                .defineInRange("speed_multiplier", 1.0D, 0.01D, 10.0D);
        BUILDER.pop();

        // Steam Turbine
        BUILDER.comment("Steam Turbine settings").push("steam_turbine");
        STEAM_TURBINE_TANK_CAPACITY = BUILDER
                .comment("Capacity of input and output fluid tanks (mB).")
                .defineInRange("tank_capacity", 12000, 1000, Integer.MAX_VALUE);
        STEAM_TURBINE_BASE_MASS = BUILDER
                .comment("Base inertia mass for rotational dynamics.")
                .defineInRange("base_mass", 10.0D, 0.0D, Double.MAX_VALUE);
        STEAM_TURBINE_DRIVE_TORQUE = BUILDER
                .comment("Base torque output from steam expansion.")
                .defineInRange("drive_torque", 360.0D, 0.0D, Double.MAX_VALUE);
        STEAM_TURBINE_FRICTION = BUILDER
                .comment("Friction coefficient affecting rotational deceleration.")
                .defineInRange("friction", 0.0D, 0.0D, Double.MAX_VALUE);
        STEAM_TURBINE_MAX_SPEED_FACTOR = BUILDER
                .comment("Maximum achievable speed as fraction of global max RPM.")
                .defineInRange("max_speed_factor", 1.0D, 0.01D, 1.0D);
        BUILDER.pop();

        // Creative Barrel
        BUILDER.comment("Creative Barrel settings").push("barrel_creative");
        CONFIG_CREATIVE_BARREL_OUTPUT_AMOUNT = BUILDER
                .comment("Maximum fluid output per tick (for creative/testing use).")
                .defineInRange("creativeBarrelOutputAmount", Integer.MAX_VALUE, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        // Steel Sheetmetal Tank
        BUILDER.comment("Steel Sheetmetal Tank settings").push("steel_sheetmetal_tank");
        STEEL_SHEETMETAL_TANK_CAPACITY = BUILDER
                .comment("Total fluid storage capacity (mB).")
                .defineInRange("capacity", 2048000, 1000, Integer.MAX_VALUE);
        STEEL_SHEETMETAL_TANK_TRANSFER_SPEED = BUILDER
                .comment("Maximum fluid transfer rate per tick (mB/t).")
                .defineInRange("transfer_speed", 1000, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    public static void onConfig(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            // General
            disassemblyMode = DISASSEMBLY_MODE.get();
            burnTimeDivider = CONFIG_BURN_TIME_DIVIDER.get();
            creativeBarrelOutputAmount = CONFIG_CREATIVE_BARREL_OUTPUT_AMOUNT.get();

            // Advanced Coke Oven
            advancedCokeOvenSpeedBase = ADVANCED_COKE_OVEN_SPEED_BASE.get();
            advancedCokeOvenBaseheaterSpeedIncrease = ADVANCED_COKE_OVEN_BASEHEATER_SPEED_INCREASE.get();
            advancedCokeOvenBaseheaterSpeedMultiplier = ADVANCED_COKE_OVEN_BASEHEATER_SPEED_MULTIPLIER.get();
            advancedCokeOvenBaseheaterMaxEnergy = ADVANCED_COKE_OVEN_BASEHEATER_MAX_ENERGY.get();
            advancedCokeOvenBaseheaterEnergyConsumption = ADVANCED_COKE_OVEN_BASEHEATER_ENERGY_CONSUMPTION.get();

            // Alternator
            alternatorPowerFactor = ALTERNATOR_POWER_FACTOR.get();
            alternatorEnergyCapacity = ALTERNATOR_ENERGY_CAPACITY.get();
            alternatorBaseMass = ALTERNATOR_BASE_MASS.get();
            alternatorFriction = ALTERNATOR_FRICTION.get();
            alternatorMaxOutput = ALTERNATOR_MAX_OUTPUT.get();

            // Boiler - Liquid
            boilerLiquidTankCapacity = BOILER_LIQUID_TANK_CAPACITY.get();
            boilerLiquidHeatLossPerTick = BOILER_LIQUID_HEAT_LOSS_PER_TICK.get();
            boilerLiquidPilotHeat = BOILER_LIQUID_PILOT_HEAT.get();

            // Boiler - Solid
            boilerSolidHeatLossPerTick = BOILER_SOLID_HEAT_LOSS_PER_TICK.get();
            boilerSolidPilotHeat = BOILER_SOLID_PILOT_HEAT.get();
            boilerSolidPilotMultiplier = BOILER_SOLID_PILOT_MULTIPLIER.get();
            boilerSolidDefaultHeatPerTick = BOILER_SOLID_DEFAULT_HEAT_PER_TICK.get();

            // Boiler - Tank
            boilerTankCapacity = BOILER_TANK_CAPACITY.get();
            boilerTankProgressLossPerTick = BOILER_TANK_PROGRESS_LOSS_PER_TICK.get();

            // Distiller
            distillerTankCapacity = DISTILLER_TANK_CAPACITY.get();
            distillerEnergyCapacity = DISTILLER_ENERGY_CAPACITY.get();

            // Gas Turbine
            gasTurbineTankCapacity = GAS_TURBINE_TANK_CAPACITY.get();
            gasTurbineEnergyCapacityHV = GAS_TURBINE_ENERGY_CAPACITY_HV.get();
            gasTurbineEnergyCapacityMV = GAS_TURBINE_ENERGY_CAPACITY_MV.get();
            gasTurbineStarterConsumption = GAS_TURBINE_STARTER_CONSUMPTION.get();
            gasTurbineSparkplugConsumption = GAS_TURBINE_SPARKPLUG_CONSUMPTION.get();
            gasTurbineBaseMass = GAS_TURBINE_BASE_MASS.get();
            gasTurbineDriveTorque = GAS_TURBINE_DRIVE_TORQUE.get();
            gasTurbineFriction = GAS_TURBINE_FRICTION.get();
            gasTurbineMaxSpeedFactor = GAS_TURBINE_MAX_SPEED_FACTOR.get();

            // Heat Exchanger
            heatExchangerInputTankCapacity = HEAT_EXCHANGER_INPUT_TANK_CAPACITY.get();
            heatExchangerOutputTankCapacity = HEAT_EXCHANGER_OUTPUT_TANK_CAPACITY.get();
            heatExchangerEnergyCapacity = HEAT_EXCHANGER_ENERGY_CAPACITY.get();
            heatExchangerEnergyMaxIO = HEAT_EXCHANGER_ENERGY_MAX_IO.get();

            // Solar Melter
            solarMelterDayMinHeatLoss = SOLAR_MELTER_DAY_MIN_HEAT_LOSS.get();
            solarMelterLossPerSectionDrop = SOLAR_MELTER_LOSS_PER_SECTION_DROP.get();
            solarMelterTempDependentLossFactor = SOLAR_MELTER_TEMP_DEPENDENT_LOSS_FACTOR.get();
            solarMelterHeatIncreaseFactor = SOLAR_MELTER_HEAT_INCREASE_FACTOR.get();
            solarMelterTempToMinReflectorsDivisor = SOLAR_MELTER_TEMP_TO_MIN_REFLECTORS_DIVISOR.get();
            solarMelterReflectorTierOffset = SOLAR_MELTER_REFLECTOR_TIER_OFFSET.get();
            solarMelterProgressLossOffTemp = SOLAR_MELTER_PROGRESS_LOSS_OFF_TEMP.get();
            solarMelterSpeedMultiplier = SOLAR_MELTER_SPEED_MULTIPLIER.get();

            // Solar Tower
            solarTowerDayMinHeatLoss = SOLAR_TOWER_DAY_MIN_HEAT_LOSS.get();
            solarTowerLossPerSectionDrop = SOLAR_TOWER_LOSS_PER_SECTION_DROP.get();
            solarTowerTempDependentLossFactor = SOLAR_TOWER_TEMP_DEPENDENT_LOSS_FACTOR.get();
            solarTowerHeatIncreaseFactor = SOLAR_TOWER_HEAT_INCREASE_FACTOR.get();
            solarTowerTempToMinReflectorsDivisor = SOLAR_TOWER_TEMP_TO_MIN_REFLECTORS_DIVISOR.get();
            solarTowerReflectorTierOffset = SOLAR_TOWER_REFLECTOR_TIER_OFFSET.get();
            solarTowerProgressLossOffTemp = SOLAR_TOWER_PROGRESS_LOSS_OFF_TEMP.get();
            solarTowerSpeedMultiplier = SOLAR_TOWER_SPEED_MULTIPLIER.get();

            // Steam Turbine
            steamTurbineTankCapacity = STEAM_TURBINE_TANK_CAPACITY.get();
            steamTurbineBaseMass = STEAM_TURBINE_BASE_MASS.get();
            steamTurbineDriveTorque = STEAM_TURBINE_DRIVE_TORQUE.get();
            steamTurbineFriction = STEAM_TURBINE_FRICTION.get();
            steamTurbineMaxSpeedFactor = STEAM_TURBINE_MAX_SPEED_FACTOR.get();

            // Steel Sheetmetal Tank
            steelSheetmetalTankCapacity = STEEL_SHEETMETAL_TANK_CAPACITY.get();
            steelSheetmetalTankTransferSpeed = STEEL_SHEETMETAL_TANK_TRANSFER_SPEED.get();
        }
    }

    public enum DisassemblyMode {
        PROCESS_QUEUE,
        TEMPLATE_BLOCKS
    }
}
