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
    public static final ForgeConfigSpec.DoubleValue ALTERNATOR_POWER_FACTOR;

    public static final ForgeConfigSpec.IntValue ALTERNATOR_ENERGY_CAPACITY;
    public static final ForgeConfigSpec.DoubleValue ALTERNATOR_BASE_MASS;
    public static final ForgeConfigSpec.DoubleValue ALTERNATOR_FRICTION;
    public static final ForgeConfigSpec.IntValue ALTERNATOR_MAX_OUTPUT;

    public static final ForgeConfigSpec.IntValue CONFIG_BURN_TIME_DIVIDER;
    public static final ForgeConfigSpec.IntValue CONFIG_CREATIVE_BARREL_OUTPUT_AMOUNT;

    // Shared across all boilers
    public static final ForgeConfigSpec.DoubleValue BOILER_DEFAULT_WORKING_HEAT;

    // Liquid Boiler
    public static final ForgeConfigSpec.IntValue BOILER_LIQUID_TANK_CAPACITY;
    public static final ForgeConfigSpec.DoubleValue BOILER_LIQUID_HEAT_LOSS_PER_TICK;
    public static final ForgeConfigSpec.DoubleValue BOILER_LIQUID_PILOT_HEAT;

    // Solid Boiler
    public static final ForgeConfigSpec.DoubleValue BOILER_SOLID_HEAT_LOSS_PER_TICK;
    public static final ForgeConfigSpec.DoubleValue BOILER_SOLID_PILOT_HEAT;
    public static final ForgeConfigSpec.IntValue BOILER_SOLID_PILOT_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue BOILER_SOLID_DEFAULT_HEAT_PER_TICK;

    // Tank Boiler
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
    public static final ForgeConfigSpec.DoubleValue SOLAR_MELTER_WORKING_HEAT_LEVEL;
    public static final ForgeConfigSpec.DoubleValue SOLAR_MELTER_DAY_MIN_HEAT_LOSS;
    public static final ForgeConfigSpec.DoubleValue SOLAR_MELTER_LOSS_PER_SECTION_DROP;
    public static final ForgeConfigSpec.DoubleValue SOLAR_MELTER_TEMP_DEPENDENT_LOSS_FACTOR;
    public static final ForgeConfigSpec.DoubleValue SOLAR_MELTER_HEAT_INCREASE_FACTOR;
    public static final ForgeConfigSpec.DoubleValue SOLAR_MELTER_TEMP_TO_MIN_REFLECTORS_DIVISOR;
    public static final ForgeConfigSpec.DoubleValue SOLAR_MELTER_REFLECTOR_TIER_OFFSET;
    public static final ForgeConfigSpec.IntValue SOLAR_MELTER_PROGRESS_LOSS_OFF_TEMP;
    public static final ForgeConfigSpec.DoubleValue SOLAR_MELTER_SPEED_MULTIPLIER;

    // Solar Tower
    public static final ForgeConfigSpec.DoubleValue SOLAR_TOWER_WORKING_HEAT_LEVEL;
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

    // Solar Reflector
    public static final ForgeConfigSpec.DoubleValue SOLAR_REFLECTOR_BASE_FREQUENCY;
    public static final ForgeConfigSpec.DoubleValue SOLAR_REFLECTOR_DANCE_DURATION;

    // Steel Sheetmetal Tank
    public static final ForgeConfigSpec.IntValue STEEL_SHEETMETAL_TANK_CAPACITY;
    public static final ForgeConfigSpec.IntValue STEEL_SHEETMETAL_TANK_TRANSFER_SPEED;

    public static DisassemblyMode disassemblyMode = DisassemblyMode.PROCESS_QUEUE;
    public static double alternatorPowerFactor = 1.0D;
    public static int alternatorEnergyCapacity = 1200000;
    public static double alternatorBaseMass = 2.0D;
    public static double alternatorFriction = 0.0D;
    public static int alternatorMaxOutput = 12288;
    public static int burnTimeDivider = 10;
    public static int creativeBarrelOutputAmount = Integer.MAX_VALUE;

    public static double boilerDefaultWorkingHeat = 100.0D;

    public static int boilerLiquidTankCapacity = 24000;
    public static double boilerLiquidHeatLossPerTick = 0.2D;
    public static double boilerLiquidPilotHeat = 20.0D;

    public static double boilerSolidHeatLossPerTick = 0.2D;
    public static double boilerSolidPilotHeat = 20.0D;
    public static int boilerSolidPilotMultiplier = 15;
    public static double boilerSolidDefaultHeatPerTick = 0.1D;

    public static int boilerTankCapacity = 24000;
    public static int boilerTankProgressLossPerTick = 1;

    public static int distillerTankCapacity = 24000;
    public static int distillerEnergyCapacity = 32000;

    public static int gasTurbineTankCapacity = 12000;
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

    public static double solarMelterWorkingHeatLevel = 1000.0D;
    public static double solarMelterDayMinHeatLoss = 0.0D;
    public static double solarMelterLossPerSectionDrop = 0.035D;
    public static double solarMelterTempDependentLossFactor = 0.00036D;
    public static double solarMelterHeatIncreaseFactor = 0.00568D;
    public static double solarMelterTempToMinReflectorsDivisor = 25.0D;
    public static double solarMelterReflectorTierOffset = 4.0D;
    public static int solarMelterProgressLossOffTemp = 2;
    public static double solarMelterSpeedMultiplier = 1.0D;

    public static double solarTowerWorkingHeatLevel = 400.0D;
    public static double solarTowerDayMinHeatLoss = 0.0D;
    public static double solarTowerLossPerSectionDrop = 0.035D;
    public static double solarTowerTempDependentLossFactor = 0.0006D;
    public static double solarTowerHeatIncreaseFactor = 0.00300D;
    public static double solarTowerTempToMinReflectorsDivisor = 25.0D;
    public static double solarTowerReflectorTierOffset = 4.0D;
    public static int solarTowerProgressLossOffTemp = 2;
    public static double solarTowerSpeedMultiplier = 1.0D;

    public static int steamTurbineTankCapacity = 12000;
    public static double steamTurbineBaseMass = 10.0D;
    public static double steamTurbineDriveTorque = 360.0D;
    public static double steamTurbineFriction = 0.0D;
    public static double steamTurbineMaxSpeedFactor = 1.0D;

    public static double solarReflectorBaseFrequency = 2.09D;
    public static double solarReflectorDanceDuration = 63.0D;

    public static int steelSheetmetalTankCapacity = 2048000;
    public static int steelSheetmetalTankTransferSpeed = 1000;

    static {
        BUILDER.push("multiblocks");
        DISASSEMBLY_MODE = BUILDER.comment("Mode for multiblock disassembly. PROCESS_QUEUE: Use gradual queue with fake player. TEMPLATE_BLOCKS: Revert to template blocks like IE.").defineEnum("disassemblyMode", DisassemblyMode.PROCESS_QUEUE);

        ALTERNATOR_POWER_FACTOR = BUILDER
                .comment("Multiplier for Alternator power output. 1.0 = default output, 2.0 = double output, 0.5 = half output.")
                .defineInRange("alternator_power_factor", 1.0D, 0.0D, 1000.0D);

        ALTERNATOR_ENERGY_CAPACITY = BUILDER.comment("Energy storage capacity for the Alternator.").defineInRange("alternator_energy_capacity", 1200000, 1, Integer.MAX_VALUE);

        ALTERNATOR_BASE_MASS = BUILDER.comment("Base mass for the Alternator's mechanical consumer.").defineInRange("alternator_base_mass", 2.0D, 0.0D, Double.MAX_VALUE);

        ALTERNATOR_FRICTION = BUILDER.comment("Friction for the Alternator's mechanical consumer.").defineInRange("alternator_friction", 0.0D, 0.0D, Double.MAX_VALUE);

        ALTERNATOR_MAX_OUTPUT = BUILDER
                .comment("Maximum FE/t output for the Alternator before ratios and power factor.")
                .defineInRange("alternator_max_output", 12288, 0, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.comment("Shared Boiler options").push("boiler_shared");
        BOILER_DEFAULT_WORKING_HEAT = BUILDER
                .comment("Default target heat level required for full boiler operation when no recipe specifies otherwise (used by all boiler types).")
                .defineInRange("default_working_heat", 100.0D, 0.0D, Double.MAX_VALUE);
        BUILDER.pop();

        BUILDER.comment("Solid Boiler options").push("boiler_solid");
        CONFIG_BURN_TIME_DIVIDER = BUILDER.comment("Divider for burn time in solid boiler (Default: 10)").defineInRange("burnTimeDivider", 10, 1, Integer.MAX_VALUE);

        BOILER_SOLID_HEAT_LOSS_PER_TICK = BUILDER.comment("Heat loss per server tick (Default: 0.2).").defineInRange("heat_loss_per_tick", 0.2D, 0.0D, Double.MAX_VALUE);

        BOILER_SOLID_PILOT_HEAT = BUILDER.comment("Pilot light minimum heat (Default: 20.0).").defineInRange("pilot_heat", 20.0D, 0.0D, Double.MAX_VALUE);

        BOILER_SOLID_PILOT_MULTIPLIER = BUILDER
                .comment("Tick interval multiplier for pilot light fuel consumption (higher = slower) (Default: 15).")
                .defineInRange("pilot_multiplier", 15, 1, Integer.MAX_VALUE);

        BOILER_SOLID_DEFAULT_HEAT_PER_TICK = BUILDER.comment("Default heat gain per tick from fuel when no recipe (Default: 0.1).").defineInRange("default_heat_per_tick", 0.1D, 0.0D, Double.MAX_VALUE);
        BUILDER.pop();

        BUILDER.comment("Liquid Boiler options").push("boiler_liquid");
        BOILER_LIQUID_TANK_CAPACITY = BUILDER.comment("Input fuel tank capacity in mB (Default: 24000).").defineInRange("tank_capacity", 24000, 1000, Integer.MAX_VALUE);

        BOILER_LIQUID_HEAT_LOSS_PER_TICK = BUILDER.comment("Heat loss per server tick (Default: 0.2).").defineInRange("heat_loss_per_tick", 0.2D, 0.0D, Double.MAX_VALUE);

        BOILER_LIQUID_PILOT_HEAT = BUILDER.comment("Pilot light minimum heat (Default: 20.0).").defineInRange("pilot_heat", 20.0D, 0.0D, Double.MAX_VALUE);
        BUILDER.pop();

        BUILDER.comment("Tank Boiler options").push("boiler_tank");
        BOILER_TANK_CAPACITY = BUILDER
                .comment("Input and output tank capacity in mB for the Tank Boiler (Default: 24000).")
                .defineInRange("tank_capacity", 24000, 1000, Integer.MAX_VALUE);

        BOILER_TANK_PROGRESS_LOSS_PER_TICK = BUILDER
                .comment("Recipe progress loss per tick when insufficient heat (Default: 1).")
                .defineInRange("progress_loss_per_tick", 1, 0, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.comment("Distiller options").push("distiller");
        DISTILLER_TANK_CAPACITY = BUILDER
                .comment("Input and output tank capacity in mB for the Distiller (Default: 24000).")
                .defineInRange("tank_capacity", 24000, 1000, Integer.MAX_VALUE);

        DISTILLER_ENERGY_CAPACITY = BUILDER
                .comment("Internal energy buffer capacity in FE for the Distiller (Default: 32000).")
                .defineInRange("energy_capacity", 32000, 1000, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.comment("Gas Turbine options").push("gas_turbine");
        GAS_TURBINE_TANK_CAPACITY = BUILDER
                .comment("Input and output tank capacity in mB for the Gas Turbine (Default: 12000).")
                .defineInRange("tank_capacity", 12 * 1000, 1000, Integer.MAX_VALUE);

        GAS_TURBINE_ENERGY_CAPACITY_HV = BUILDER
                .comment("HV energy buffer capacity in FE (Default: 8192).")
                .defineInRange("energy_capacity_hv", 8192, 1000, Integer.MAX_VALUE);

        GAS_TURBINE_ENERGY_CAPACITY_MV = BUILDER
                .comment("MV energy buffer capacity in FE for sparkplug/starter (Default: 2048).")
                .defineInRange("energy_capacity_mv", 2048, 100, Integer.MAX_VALUE);

        GAS_TURBINE_STARTER_CONSUMPTION = BUILDER
                .comment("FE/t consumed by electric starter (Default: 4096).")
                .defineInRange("starter_consumption", 4096, 0, Integer.MAX_VALUE);

        GAS_TURBINE_SPARKPLUG_CONSUMPTION = BUILDER
                .comment("FE per ignition attempt by sparkplug (Default: 1024).")
                .defineInRange("sparkplug_consumption", 1024, 0, Integer.MAX_VALUE);

        GAS_TURBINE_BASE_MASS = BUILDER
                .comment("Base mass for inertia calculation (Default: 8.0).")
                .defineInRange("base_mass", 8.0D, 0.0D, Double.MAX_VALUE);

        GAS_TURBINE_DRIVE_TORQUE = BUILDER
                .comment("Base drive torque provided by the turbine (Default: 30.0).")
                .defineInRange("drive_torque", 30.0D, 0.0D, Double.MAX_VALUE);

        GAS_TURBINE_FRICTION = BUILDER
                .comment("Base friction for inertia calculation (Default: 60.0).")
                .defineInRange("friction", 60.0D, 0.0D, Double.MAX_VALUE);

        GAS_TURBINE_MAX_SPEED_FACTOR = BUILDER
                .comment("Maximum speed as fraction of MechanicalCapabilities.MAX_RPM (Default: 0.5 = half max RPM).")
                .defineInRange("max_speed_factor", 0.5D, 0.01D, 1.0D);
        BUILDER.pop();

        BUILDER.comment("Heat Exchanger options").push("heat_exchanger");
        HEAT_EXCHANGER_INPUT_TANK_CAPACITY = BUILDER
                .comment("Capacity in mB for each input tank (input0 and input1).")
                .defineInRange("input_tank_capacity", 10000, 1000, Integer.MAX_VALUE);

        HEAT_EXCHANGER_OUTPUT_TANK_CAPACITY = BUILDER
                .comment("Capacity in mB for each output tank (output0 and output1).")
                .defineInRange("output_tank_capacity", 10000, 1000, Integer.MAX_VALUE);

        HEAT_EXCHANGER_ENERGY_CAPACITY = BUILDER
                .comment("Internal energy buffer capacity in FE.")
                .defineInRange("energy_capacity", 2048, 1000, Integer.MAX_VALUE);

        HEAT_EXCHANGER_ENERGY_MAX_IO = BUILDER
                .comment("Maximum FE/t receive/extract for the energy buffer.")
                .defineInRange("energy_max_io", 1024, 0, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.comment("Solar Melter options").push("solar_melter");
        SOLAR_MELTER_WORKING_HEAT_LEVEL = BUILDER
                .comment("Default target heat level required for full operation when no recipe specifies otherwise.")
                .defineInRange("working_heat_level", 1000.0D, 100.0D, Double.MAX_VALUE);

        SOLAR_MELTER_DAY_MIN_HEAT_LOSS = BUILDER
                .comment("Minimum heat loss per tick during daytime (base loss before other factors).")
                .defineInRange("day_min_heat_loss", 0.0D, 0.0D, Double.MAX_VALUE);

        SOLAR_MELTER_LOSS_PER_SECTION_DROP = BUILDER
                .comment("Additional heat loss per sky darkness section drop (higher darkness = more loss).")
                .defineInRange("loss_per_section_drop", 0.035D, 0.0D, Double.MAX_VALUE);

        SOLAR_MELTER_TEMP_DEPENDENT_LOSS_FACTOR = BUILDER
                .comment("Heat loss factor scaled by current temperature (higher temp = more loss).")
                .defineInRange("temp_dependent_loss_factor", 0.00036D, 0.0D, Double.MAX_VALUE);

        SOLAR_MELTER_HEAT_INCREASE_FACTOR = BUILDER
                .comment("Base heat gain factor per reflector strength unit under perfect sun.")
                .defineInRange("heat_increase_factor", 0.00568D, 0.0D, Double.MAX_VALUE);

        SOLAR_MELTER_TEMP_TO_MIN_REFLECTORS_DIVISOR = BUILDER
                .comment("Divisor to calculate minimum reflectors needed for a recipe's required temperature.")
                .defineInRange("temp_to_min_reflectors_divisor", 25.0D, 1.0D, Double.MAX_VALUE);

        SOLAR_MELTER_REFLECTOR_TIER_OFFSET = BUILDER
                .comment("Offset added to min reflectors per tier difference in recipe temperature requirements.")
                .defineInRange("reflector_tier_offset", 4.0D, 0.0D, Double.MAX_VALUE);

        SOLAR_MELTER_PROGRESS_LOSS_OFF_TEMP = BUILDER
                .comment("Recipe progress loss per tick when below required temperature.")
                .defineInRange("progress_loss_off_temp", 2, 0, Integer.MAX_VALUE);

        SOLAR_MELTER_SPEED_MULTIPLIER = BUILDER
                .comment("Global speed multiplier for the melter process (1.0 = default).")
                .defineInRange("speed_multiplier", 1.0D, 0.01D, 10.0D);
        BUILDER.pop();

        BUILDER.comment("Solar Tower options").push("solar_tower");
        SOLAR_TOWER_WORKING_HEAT_LEVEL = BUILDER
                .comment("Default target heat level required for full operation when no recipe specifies otherwise.")
                .defineInRange("working_heat_level", 400.0D, 100.0D, Double.MAX_VALUE);

        SOLAR_TOWER_DAY_MIN_HEAT_LOSS = BUILDER
                .comment("Minimum heat loss per tick during daytime (base loss before other factors).")
                .defineInRange("day_min_heat_loss", 0.0D, 0.0D, Double.MAX_VALUE);

        SOLAR_TOWER_LOSS_PER_SECTION_DROP = BUILDER
                .comment("Additional heat loss per sky darkness section drop (higher darkness = more loss).")
                .defineInRange("loss_per_section_drop", 0.035D, 0.0D, Double.MAX_VALUE);

        SOLAR_TOWER_TEMP_DEPENDENT_LOSS_FACTOR = BUILDER
                .comment("Heat loss factor scaled by current temperature (higher temp = more loss).")
                .defineInRange("temp_dependent_loss_factor", 0.0006D, 0.0D, Double.MAX_VALUE);

        SOLAR_TOWER_HEAT_INCREASE_FACTOR = BUILDER
                .comment("Base heat gain factor per reflector strength unit under perfect sun.")
                .defineInRange("heat_increase_factor", 0.00300D, 0.0D, Double.MAX_VALUE);

        SOLAR_TOWER_TEMP_TO_MIN_REFLECTORS_DIVISOR = BUILDER
                .comment("Divisor to calculate minimum reflectors needed for a recipe's required temperature.")
                .defineInRange("temp_to_min_reflectors_divisor", 25.0D, 1.0D, Double.MAX_VALUE);

        SOLAR_TOWER_REFLECTOR_TIER_OFFSET = BUILDER
                .comment("Offset added to min reflectors per tier difference in recipe temperature requirements.")
                .defineInRange("reflector_tier_offset", 4.0D, 0.0D, Double.MAX_VALUE);

        SOLAR_TOWER_PROGRESS_LOSS_OFF_TEMP = BUILDER
                .comment("Recipe progress loss per tick when below required temperature.")
                .defineInRange("progress_loss_off_temp", 2, 0, Integer.MAX_VALUE);

        SOLAR_TOWER_SPEED_MULTIPLIER = BUILDER
                .comment("Global speed multiplier for the tower process (1.0 = default).")
                .defineInRange("speed_multiplier", 1.0D, 0.01D, 10.0D);
        BUILDER.pop();

        BUILDER.comment("Steam Turbine options").push("steam_turbine");
        STEAM_TURBINE_TANK_CAPACITY = BUILDER
                .comment("Input and output tank capacity in mB (Default: 12000).")
                .defineInRange("tank_capacity", 12 * 1000, 1000, Integer.MAX_VALUE);

        STEAM_TURBINE_BASE_MASS = BUILDER
                .comment("Base mass for inertia calculation (Default: 10.0).")
                .defineInRange("base_mass", 10.0D, 0.0D, Double.MAX_VALUE);

        STEAM_TURBINE_DRIVE_TORQUE = BUILDER
                .comment("Base drive torque provided by the turbine (Default: 360.0).")
                .defineInRange("drive_torque", 360.0D, 0.0D, Double.MAX_VALUE);

        STEAM_TURBINE_FRICTION = BUILDER
                .comment("Base friction for inertia calculation (Default: 0.0).")
                .defineInRange("friction", 0.0D, 0.0D, Double.MAX_VALUE);

        STEAM_TURBINE_MAX_SPEED_FACTOR = BUILDER
                .comment("Maximum speed as fraction of MechanicalCapabilities.MAX_RPM (Default: 1.0 = full max RPM).")
                .defineInRange("max_speed_factor", 1.0D, 0.01D, 1.0D);
        BUILDER.pop();

        BUILDER.comment("Solar Reflector options").push("solar_reflector");
        SOLAR_REFLECTOR_BASE_FREQUENCY = BUILDER
                .comment("Base frequency for the dance animation sine waves (higher = faster movement).")
                .defineInRange("base_frequency", 2.09D, 0.1D, 10.0D);

        SOLAR_REFLECTOR_DANCE_DURATION = BUILDER
                .comment("Duration in seconds for one full dance cycle (used for timing and looping).")
                .defineInRange("dance_duration", 63.0D, 10.0D, 300.0D);
        BUILDER.pop();

        BUILDER.comment("Creative Barrel options").push("barrel_creative");
        CONFIG_CREATIVE_BARREL_OUTPUT_AMOUNT = BUILDER.comment("Maximum fluid amount to output per tick from the creative barrel (Default: 2147483647)").defineInRange("creativeBarrelOutputAmount", Integer.MAX_VALUE, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.comment("Steel Sheetmetal Tank options").push("steel_sheetmetal_tank");
        STEEL_SHEETMETAL_TANK_CAPACITY = BUILDER
                .comment("Internal tank capacity in mB (Default: 2048 buckets).")
                .defineInRange("capacity", 2048 * 1000, 1000, Integer.MAX_VALUE);

        STEEL_SHEETMETAL_TANK_TRANSFER_SPEED = BUILDER
                .comment("Max fluid transfer speed per tick in mB (Default: 1 bucket).")
                .defineInRange("transfer_speed", 1000, 1, Integer.MAX_VALUE);
        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    public static void onConfig(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            disassemblyMode = DISASSEMBLY_MODE.get();
            alternatorPowerFactor = ALTERNATOR_POWER_FACTOR.get();
            alternatorEnergyCapacity = ALTERNATOR_ENERGY_CAPACITY.get();
            alternatorBaseMass = ALTERNATOR_BASE_MASS.get();
            alternatorFriction = ALTERNATOR_FRICTION.get();
            alternatorMaxOutput = ALTERNATOR_MAX_OUTPUT.get();
            burnTimeDivider = CONFIG_BURN_TIME_DIVIDER.get();
            creativeBarrelOutputAmount = CONFIG_CREATIVE_BARREL_OUTPUT_AMOUNT.get();

            boilerDefaultWorkingHeat = BOILER_DEFAULT_WORKING_HEAT.get();

            boilerLiquidTankCapacity = BOILER_LIQUID_TANK_CAPACITY.get();
            boilerLiquidHeatLossPerTick = BOILER_LIQUID_HEAT_LOSS_PER_TICK.get();
            boilerLiquidPilotHeat = BOILER_LIQUID_PILOT_HEAT.get();

            boilerSolidHeatLossPerTick = BOILER_SOLID_HEAT_LOSS_PER_TICK.get();
            boilerSolidPilotHeat = BOILER_SOLID_PILOT_HEAT.get();
            boilerSolidPilotMultiplier = BOILER_SOLID_PILOT_MULTIPLIER.get();
            boilerSolidDefaultHeatPerTick = BOILER_SOLID_DEFAULT_HEAT_PER_TICK.get();

            boilerTankCapacity = BOILER_TANK_CAPACITY.get();
            boilerTankProgressLossPerTick = BOILER_TANK_PROGRESS_LOSS_PER_TICK.get();

            distillerTankCapacity = DISTILLER_TANK_CAPACITY.get();
            distillerEnergyCapacity = DISTILLER_ENERGY_CAPACITY.get();

            gasTurbineTankCapacity = GAS_TURBINE_TANK_CAPACITY.get();
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

            solarMelterWorkingHeatLevel = SOLAR_MELTER_WORKING_HEAT_LEVEL.get();
            solarMelterDayMinHeatLoss = SOLAR_MELTER_DAY_MIN_HEAT_LOSS.get();
            solarMelterLossPerSectionDrop = SOLAR_MELTER_LOSS_PER_SECTION_DROP.get();
            solarMelterTempDependentLossFactor = SOLAR_MELTER_TEMP_DEPENDENT_LOSS_FACTOR.get();
            solarMelterHeatIncreaseFactor = SOLAR_MELTER_HEAT_INCREASE_FACTOR.get();
            solarMelterTempToMinReflectorsDivisor = SOLAR_MELTER_TEMP_TO_MIN_REFLECTORS_DIVISOR.get();
            solarMelterReflectorTierOffset = SOLAR_MELTER_REFLECTOR_TIER_OFFSET.get();
            solarMelterProgressLossOffTemp = SOLAR_MELTER_PROGRESS_LOSS_OFF_TEMP.get();
            solarMelterSpeedMultiplier = SOLAR_MELTER_SPEED_MULTIPLIER.get();

            solarTowerWorkingHeatLevel = SOLAR_TOWER_WORKING_HEAT_LEVEL.get();
            solarTowerDayMinHeatLoss = SOLAR_TOWER_DAY_MIN_HEAT_LOSS.get();
            solarTowerLossPerSectionDrop = SOLAR_TOWER_LOSS_PER_SECTION_DROP.get();
            solarTowerTempDependentLossFactor = SOLAR_TOWER_TEMP_DEPENDENT_LOSS_FACTOR.get();
            solarTowerHeatIncreaseFactor = SOLAR_TOWER_HEAT_INCREASE_FACTOR.get();
            solarTowerTempToMinReflectorsDivisor = SOLAR_TOWER_TEMP_TO_MIN_REFLECTORS_DIVISOR.get();
            solarTowerReflectorTierOffset = SOLAR_TOWER_REFLECTOR_TIER_OFFSET.get();
            solarTowerProgressLossOffTemp = SOLAR_TOWER_PROGRESS_LOSS_OFF_TEMP.get();
            solarTowerSpeedMultiplier = SOLAR_TOWER_SPEED_MULTIPLIER.get();

            steamTurbineTankCapacity = STEAM_TURBINE_TANK_CAPACITY.get();
            steamTurbineBaseMass = STEAM_TURBINE_BASE_MASS.get();
            steamTurbineDriveTorque = STEAM_TURBINE_DRIVE_TORQUE.get();
            steamTurbineFriction = STEAM_TURBINE_FRICTION.get();
            steamTurbineMaxSpeedFactor = STEAM_TURBINE_MAX_SPEED_FACTOR.get();

            solarReflectorBaseFrequency = SOLAR_REFLECTOR_BASE_FREQUENCY.get();
            solarReflectorDanceDuration = SOLAR_REFLECTOR_DANCE_DURATION.get();

            steelSheetmetalTankCapacity = STEEL_SHEETMETAL_TANK_CAPACITY.get();
            steelSheetmetalTankTransferSpeed = STEEL_SHEETMETAL_TANK_TRANSFER_SPEED.get();
        }
    }

    public enum DisassemblyMode {
        PROCESS_QUEUE,
        TEMPLATE_BLOCKS
    }
}
