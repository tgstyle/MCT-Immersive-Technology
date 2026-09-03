package mctmods.immersivetechnology.common;

import com.immersiveconvergence.core.ICCommonConfig;
import mctmods.immersivetechnology.ImmersiveTechnology;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.RangeDouble;

@SuppressWarnings("unused")
public class Config {
    public static final double SMOKE_HEIGHT_DEFAULT = 0.7;

    @net.minecraftforge.common.config.Config(modid=ImmersiveTechnology.MODID, name="mct_immersivetechnology")
    public static class ITConfig {
        public static Blocks blocks;
        public static Client client;
        public static Multiblocks multiBlocks;
        public static Settings settings;

        public static class Blocks {
            public static Barrels barrels = new Barrels();

            public static class Barrels {
                @Comment({"How much fluid the Creative Barrel offers each neighbour per tick [Default=2147483647]"})
                public int barrel_creative_outputAmount = Integer.MAX_VALUE;
                @Comment({"The capacity of the tank for the Open Barrel [Default=12000]"})
                public int barrel_open_tankSize = 12000;
                @Comment({"How fast can the Open Barrel push fluids out, in mB [Default=1000]"})
                public int barrel_open_transferSpeed = 1000;
                @Comment({"The capacity of the tank for the Steel Barrel [Default=24000]"})
                public int barrel_steel_tankSize = 24000;
                @Comment({"How fast can the Steel Barrel push fluids out, in mB [Default=1000]"})
                public int barrel_steel_transferSpeed = 1000;
            }
        }
        public static class Multiblocks {
            public static Alternator alternator = new Alternator();
            public static AdvancedCokeOven advancedCokeOven = new AdvancedCokeOven();
            public static AdvancedCokeOvenBaseheater advancedCokeOvenBaseheater = new AdvancedCokeOvenBaseheater();
            public static BoilerHeat boilerHeat = new BoilerHeat();
            public static BoilerTank boilerTank = new BoilerTank();
            public static BoilerLiquid boilerLiquid = new BoilerLiquid();
            public static BoilerSolid boilerSolid = new BoilerSolid();
            public static CoolingTower coolingTower = new CoolingTower();
            public static Distiller distiller = new Distiller();
            public static ElectrolyticCrucibleBattery electrolyticCrucibleBattery = new ElectrolyticCrucibleBattery();
            public static Enable enable = new Enable();
            public static GasTurbine gasTurbine = new GasTurbine();
            public static HeatExchanger heatExchanger = new HeatExchanger();
            public static HighPressureSteamTurbine highPressureSteamTurbine = new HighPressureSteamTurbine();
            public static JEI JEI = new JEI();
            public static MeltingCrucible meltingCrucible = new MeltingCrucible();
            public static Radiator radiator = new Radiator();
            public static SolarMelter solarMelter = new SolarMelter();
            public static SolarReflector solarReflector = new SolarReflector();
            public static SolarTower solarTower = new SolarTower();
            public static SteamTurbine steamTurbine = new SteamTurbine();
            public static SteelTank steelTank = new SteelTank();

            public static class Alternator {
                @Comment({"Alternator generation exponent [Default=1.0]"})
                public double alternator_exponent = 1.0;
                @Comment({"The maximum energy an Alternator can store [Default=1200000]"})
                public int alternator_energy_capacitorSize = 1200000;
                @Comment({"Energy production when running at maximum speed and torque [Default=12288]"})
                public int alternator_energy_perTick = 12288;
                @Comment({"Rotating mass the Alternator adds to the turbine driving it [Default=2.0]"})
                public double alternator_baseMass = 2.0;
                @Comment({"Constant drag the Alternator adds to the turbine driving it [Default=0.0]"})
                public double alternator_friction = 0.0;
                @Comment({"Scales the Alternator's generation without changing its rated output [Default=1.0]"})
                public double alternator_powerFactor = 1.0;
                @Comment({"Alternator sound based RPM or Capacity [Default=true]"})
                public boolean alternator_sound_RPM = true;
                @Comment({"Alternator generation threshold (fraction of speed below which it will not produce power, to emulate grid syncing) [Default=0.0]"})
                public double alternator_threshold = 0.0;
            }
            public static class AdvancedCokeOven {
                @Comment({"The capacity of the tank for the Advanced Coke Oven [Default=12000]"})
                public int advancedCokeOven_tankSize = 12000;
                @Comment({"How fast the Advanced Coke Oven (with no baseheaters) is when compared to the basic Coke Oven. A value of 1 means same speed. [Default=1]"})
                public float advancedCokeOven_speed_base = 1;
                @Comment({"This value gets added per baseheater, to the speed of the Advanced Coke Oven [Default=0.25]"})
                public float advancedCokeOven_baseheater_speed_increase = 0.25f;
                @Comment({"The speed of the Advanced Coke Oven gets multiplied by this value per baseheater [Default=1.25]"})
                public float advancedCokeOven_baseheater_speed_multiplier = 1.25f;
            }
            public static class AdvancedCokeOvenBaseheater {
                @Comment({"The energy per tick the Coke Oven Baseheater consumes while processing [Default=32]"})
                public int advancedCokeOvenBaseheater_energy_consumption = 32;
            }
            public static class BoilerHeat {
                @Comment({"The heat level boilers work toward when no recipe specifies one. 600 is the lowest value used; anything below counts as 600 [Default=600.0]"})
                @RangeDouble(min = 600.0)
                public double boiler_heat_workingLevel = 600.0;

                public double workingLevel() { return Math.min(Math.max(600.0, boiler_heat_workingLevel), ICCommonConfig.heat.maxHeat); }
            }
            public static class BoilerTank {
                @Comment({"The capacity of the input and output tanks for the Boiler Tank [Default=24000]"})
                public int boilerTank_tankSize = 24000;
                @Comment({"How fast the Boiler Tank loses progress in ticks when the heat drops below the required level [Default=1]"})
                public int boilerTank_progress_lossInTicks = 1;
            }
            public static class BoilerLiquid {
                @Comment({"The capacity of the fuel tank for the Liquid Boiler [Default=24000]"})
                public int boilerLiquid_fuel_tankSize = 24000;
                @Comment({"How fast the Liquid Boiler cools down per tick when unlit [Default=0.2]"})
                public double boilerLiquid_heat_lossPerTick = 0.2;
                @Comment({"The heat level the Liquid Boiler holds while idling on its pilot light [Default=20.0]"})
                public double boilerLiquid_heat_pilot = 20.0;
            }
            public static class BoilerSolid {
                @Comment({"How fast the Solid Boiler cools down per tick when unlit [Default=0.2]"})
                public double boilerSolid_heat_lossPerTick = 0.2;
                @Comment({"The heat level the Solid Boiler holds while idling on its pilot light [Default=20.0]"})
                public double boilerSolid_heat_pilot = 20.0;
                @Comment({"Fuel is only consumed every this many ticks while the Solid Boiler idles on its pilot light [Default=15]"})
                public int boilerSolid_pilot_fuelMultiplier = 15;
                @Comment({"Heat added per tick by furnace fuels that have no Solid Boiler recipe [Default=0.1]"})
                public double boilerSolid_heat_defaultPerTick = 0.1;
                @Comment({"Furnace burn times are divided by this value to get the Solid Boiler burn time [Default=10]"})
                public int boilerSolid_burnTime_divider = 10;
            }
            public static class CoolingTower {
                @Comment({"The capacity of the input tanks for the Cooling Tower [Default=24000]"})
                public int coolingTower_input_tankSize = 24000;
                @Comment({"The capacity of the output tanks for the Cooling Tower [Default=24000]"})
                public int coolingTower_output_tankSize = 24000;
                @Comment({"Biome temperature effect strength on cooling tower speed (0 = disabled). Cold biomes faster, hot slower. Neutral ~0.8 [Default=0.5]"})
                public double coolingTower_biome_temp_factor = 0.5;
                @Comment({"How much the local biome's humidity affects Cooling Tower speed. 0 disables the effect entirely. Drier biomes speed it up, wetter biomes slow it down [Default=3.0]"})
                public double coolingTower_biome_humidity_factor = 3.0;
            }
            public static class Distiller {
                @Comment({"The capacity of the input tank for the Distiller [Default=24000]"})
                public int distiller_input_tankSize = 24000;
                @Comment({"The capacity of the output tank for the Distiller [Default=24000]"})
                public int distiller_output_tankSize = 24000;
                @Comment({"The maximum energy a Distiller can store [Default=32000]"})
                public int distiller_energy_size = 32000;
                @Comment({"The maximum energy input per tick per port for the Distiller [Default=512]"})
                public int distiller_energy_maxInput = 512;
            }
            public static class ElectrolyticCrucibleBattery {
                @Comment({"The maximum energy an Electrolytic Crucible Battery can store [Default=16000]"})
                public int electrolyticCrucibleBattery_energy_size = 16000;
                @Comment({"The maximum energy input per tick per port for the Electrolytic Crucible Battery [Default=4096]"})
                public int electrolyticCrucibleBattery_energy_maxInput = 4096;
                @Comment({"The capacity of the input tanks for the Electrolytic Crucible Battery [Default=10000]"})
                public int electrolyticCrucibleBattery_input_tankSize = 10000;
                @Comment({"The capacity of the output tanks for the Electrolytic Crucible Battery [Default=10000]"})
                public int electrolyticCrucibleBattery_output_tankSize = 10000;
            }
            public static class Enable {
                @Comment({"**WARNING** disable this before you load a new world or break the multiblocks before you do this!!! Can the Advanced Coke Oven Multiblock structure and Baseheater be built ? [Default=true]"})
                public boolean enable_advancedCokeOven = true;
                @Comment({"**WARNING** disable this before you load a new world or break the multiblocks before you do this!!! Can the Boiler Tank and Liquid Boiler Multiblock structures be built ? [Default=true]"})
                public boolean enable_boiler = true;
                @Comment({"**WARNING** disable this before you load a new world or break the multiblocks before you do this!!! Can the Solid Boiler Multiblock structure be built ? [Default=true]"})
                public boolean enable_boilerSolid = true;
                @Comment({"**WARNING** disable this before you load a new world or break the multiblocks before you do this!!! Can the Cooling Tower Multiblock structures be built ? [Default=true]"})
                public boolean enable_coolingTower = true;
                @Comment({"**WARNING** disable this before you load a new world or break the multiblocks before you do this!!! Can the Distiller Multiblock structure be built ? [Default=true]"})
                public boolean enable_distiller = true;
                @Comment({"**WARNING** disable this before you load a new world or break the multiblocks before you do this!!! Can the Electrolytic Crucible Battery Multiblock structures be built ? [Default=false]"})
                public boolean enable_electrolyticCrucibleBattery = true;
                @Comment({"**WARNING** disable this before you load a new world or break the multiblocks before you do this!!! Can the Gas Turbine Multiblock structures be built ? [Default=true]"})
                public boolean enable_gasTurbine = true;
                @Comment({"**WARNING** disable this before you load a new world or break the multiblocks before you do this!!! Can the Heat Exchanger Multiblock structures be built ? [Default=true]"})
                public boolean enable_heatExchanger = true;
                @Comment({"**WARNING** disable this before you load a new world or break the multiblocks before you do this!!! Can the High Pressure Steam Turbine Multiblock structures be built ? [Default=false]"})
                public boolean enable_highPressureSteamTurbine = false;
                @Comment({"**WARNING** disable this before you load a new world or break the multiblocks before you do this!!! Can the Melting Crucible Multiblock structures be built ? [Default=false]"})
                public boolean enable_meltingCrucible = true;
                @Comment({"**WARNING** disable this before you load a new world or break the multiblocks before you do this!!! Can the Radiator Multiblock structures be built ? [Default=true]"})
                public boolean enable_radiator = true;
                @Comment({"**WARNING** disable this before you load a new world or break the multiblocks before you do this!!! Can the Solar Melter Multiblock structures be built ? [Default=false]"})
                public boolean enable_solarMelter = true;
                @Comment({"**WARNING** disable this before you load a new world or break the multiblocks before you do this!!! Can the Solar Tower / Solar Reflector Multiblock structures be built ? [Default=true]"})
                public boolean enable_solarTower = true;
                @Comment({"**WARNING** disable this before you load a new world or break the multiblocks before you do this!!! Can the Steam Turbine / Alternator Multiblock structures be built ? [Default=true]"})
                public boolean enable_steamTurbine = true;
            }
            public static class GasTurbine {
                @Comment({"The power consumption of the electric starter for the Gas Turbine [Default=4096]"})
                public int gasTurbine_electric_starter_consumption = 4096;
                @Comment({"The capacity of the electric starter for the Gas Turbine [Default=8192]"})
                public int gasTurbine_electric_starter_size = 8192;
                @Comment({"The capacity of the input tank for the Gas Turbine [Default=12000]"})
                public int gasTurbine_input_tankSize = 12000;
                @Comment({"The capacity of the output tank for the Gas Turbine [Default=12000]"})
                public int gasTurbine_output_tankSize = 12000;
                @Comment({"How fast should the Steam Turbine's axle rotate in degrees per tick (purely cosmetic) [Default=72]"})
                public float gasTurbine_speed_maxRotation = 72;
                @Comment({"Rotating mass of the Gas Turbine. Higher values slow both spin-up and coast-down [Default=8.0]"})
                public double gasTurbine_baseMass = 8.0;
                @Comment({"Torque the Gas Turbine's drive applies [Default=30.0]"})
                public double gasTurbine_driveTorque = 30.0;
                @Comment({"Constant drag on the Gas Turbine [Default=60.0]"})
                public double gasTurbine_friction = 60.0;
                @Comment({"Fraction of the maximum tolerated RPM the Gas Turbine can reach [Default=0.5]"})
                public float gasTurbine_speed_maxFactor = 0.5f;
                @Comment({"The power consumption  of the spark plug for the Gas Turbine [Default=1024]"})
                public int gasTurbine_sparkplug_consumption = 1024;
                @Comment({"The capacity of the spark plug for the Gas Turbine [Default=2048]"})
                public int gasTurbine_sparkplug_size = 2048;
                @Comment({"How much of the maximum alternator output power should the Gas Turbine generate [Default=1.0]"})
                public float gasTurbine_torque = 1.0f;
            }
            public static class HeatExchanger {
                @Comment({"The maximum energy a Heat Exchanger can store [Default=2048]"})
                public int heatExchanger_energy_size = 2048;
                @Comment({"The maximum energy input per tick per port for the Heat Exchanger [Default=1024]"})
                public int heatExchanger_energy_maxInput = 1024;
                @Comment({"The capacity of the input tanks for the Heat Exchanger [Default=10000]"})
                public int heatExchanger_input_tankSize = 10000;
                @Comment({"The capacity of the output tanks for the Heat Exchanger [Default=10000]"})
                public int heatExchanger_output_tankSize = 10000;
            }
            public static class HighPressureSteamTurbine {
                @Comment({"The capacity of the input tank for the Steam Turbine [Default=10000]"})
                public int highPressureSteamTurbine_input_tankSize = 10000;
                @Comment({"The capacity of the output tank for the Steam Turbine [Default=10000]"})
                public int highPressureSteamTurbine_output_tankSize = 10000;
                @Comment({"How fast should the Steam Turbine's axle rotate in degrees per tick (purely cosmetic) [Default=72]"})
                public float highPressureSteamTurbine_speed_maxRotation = 72;
                @Comment({"Rotating mass of the High Pressure Steam Turbine. Higher values slow both spin-up and coast-down [Default=30.0]"})
                public double highPressureSteamTurbine_baseMass = 30.0;
                @Comment({"Torque the High Pressure Steam Turbine's drive applies. High pressure steam drives far harder than the regular turbine's, so it spins up twice as fast despite the heavier rotor [Default=2160.0]"})
                public double highPressureSteamTurbine_driveTorque = 2160.0;
                @Comment({"Constant drag on the High Pressure Steam Turbine [Default=0.0]"})
                public double highPressureSteamTurbine_friction = 0.0;
                @Comment({"Fraction of the maximum tolerated RPM the High Pressure Steam Turbine can reach. Above 1.0 it overdrives the alternator past its rated output [Default=1.0]"})
                public float highPressureSteamTurbine_speed_maxFactor = 1.0f;
                @Comment({"How much of the maximum alternator output power should the Steam Turbine generate [Default=1.0]"})
                public float highPressureSteamTurbine_torque = 1.0f;
                @Comment({"Should the steam turbine use tungsten, if it exists? Otherwise the turbine will use nickel [Default=true]"})
                public boolean highPressureSteamTurbine_turbine_material = true;
            }
            public static class JEI {
                @Comment({"Display IT Multiblocks in JEI [Default=true]"})
                public boolean enableJEIMultiblocks = true;
            }
            public static class MeltingCrucible {
                @Comment({"Heat loss multiplier for the Melting Crucible. Higher values = faster cooling when unpowered. [Default: 0.2]"})
                public double meltingCrucible_heat_loss_multiplier = 0.2;
                @Comment({"Temperature gain per tick while heating energy is being consumed. [Default=0.55]"})
                public double meltingCrucible_heat_gainPerTick = 0.55;
                @Comment({"The maximum energy a Melting Crucible can store [Default=50000]"})
                public int meltingCrucible_energy_size = 50000;
                @Comment({"The maximum energy input per tick per port for the Melting Crucible [Default=1024]"})
                public int meltingCrucible_energy_maxInput = 1024;
                @Comment({"A Melting Crucible can only start processing recipes once it reaches this temperature [Default=1000.0]"})
                public double meltingCrucible_heat_workingTemperature = 1000.0;
                @Comment({"The capacity of the output tank for the Melting Crucible [Default=10000]"})
                public int meltingCrucible_output_tankSize = 10000;
                @Comment({"RF per tick consumed while heating the crucible to working temperature [Default: 1000]"})
                public int meltingCrucible_energy_per_tick_heating = 1000;
                @Comment({"RF per tick consumed to maintain temperature when at max heat and not processing [Default: 512]"})
                public int meltingCrucible_energy_per_tick_maintain = 512;
            }
            public static class Radiator {
                @Comment({"The capacity of the input tank for the Radiator [Default=8000]"})
                public int radiator_input_tankSize = 8000;
                @Comment({"The capacity of the output tank for the Radiator [Default=8000]"})
                public int radiator_output_tankSize = 8000;
                @Comment({"The heat speed multiplier applied to all Radiator recipes [Default=1]"})
                public float radiator_speed_multiplier = 1;
                @Comment({"Biome temperature effect strength on radiator speed (0 = disabled). Cold biomes faster, hot slower. Neutral ~0.8 [Default=0.5]"})
                public double radiator_biome_temp_factor = 0.5;
                @Comment({"How much the local biome's humidity affects Radiator efficiency. 0 disables the effect entirely. Drier biomes give a bonus, wetter biomes a penalty [Default=3.0]"})
                public double radiator_biome_humidity_factor = 3.0;
            }
            public static class SolarMelter {
                @Comment({"How fast the Solar Tower cools down per tick when turned off or at night [Default=1.0]"})
                public double solarMelter_heat_loss_multiplier = 1.0;
                @Comment({"A Solar Melter can only start processing recipes once it reaches this temperature [Default=1000.0]"})
                public double solarMelter_heat_workingTemperature = 1000.0;
                @Comment({"The maximum strength of the reflectors. Decreasing this reduces the amount of reflectors needed to achieve max processing speed. [Default=227.5]"})
                public double solarMelter_maximum_reflector_strength = 227.5;
                @Comment({"The capacity of the output tank for the Solar Melter [Default=12000]"})
                public int solarMelter_output_tankSize = 12000;
                @Comment({"The heat speed multiplier applied to all Solar Tower recipes (with a single reflector) [Default=1]"})
                public float solarMelter_speed_multiplier = 1;
            }
            public static class SolarReflector {
                @Comment({"The maximum distance between the Solar Reflectors and the Solar Tower **WARNING** The tower's ability to produce steam will be severely hampered if this number is small!!! [Default=22]"})
                public int solarReflector_maxRange = 22;
                @Comment({"The minimum distance between the Solar Reflectors and the Solar Tower [Default=12]"})
                public int solarReflector_minRange = 12;
            }
            public static class SolarTower {
                @Comment({"How fast the Solar Tower cools down per tick when turned off or at night [Default=1.0]"})
                public double solarTower_heat_loss_multiplier = 1.0;
                @Comment({"A Solar Tower can only start processing recipes once it reaches this temperature [Default=400.0]"})
                public double solarTower_heat_workingTemperature = 400.0;
                @Comment({"The capacity of the input tank for the Solar Tower [Default=12000]"})
                public int solarTower_input_tankSize = 12000;
                @Comment({"The maximum strength of the reflectors. Decreasing this reduces the amount of reflectors needed to achieve max processing speed. [Default=227.5]"})
                public double solarTower_maximum_reflector_strength = 227.5;
                @Comment({"The capacity of the output tank for the Solar Tower [Default=12000]"})
                public int solarTower_output_tankSize = 12000;
                @Comment({"How fast the the Solar Tower loses progress in ticks when the heat drops below processing heat level [Default=1]"})
                public int solarTower_progress_lossInTicks = 1;
                @Comment({"The heat speed multiplier applied to all Solar Tower recipes (with a single reflector) [Default=1]"})
                public float solarTower_speed_multiplier = 1;
            }
            public static class SteamTurbine {
                @Comment({"The capacity of the input tank for the Steam Turbine [Default=12000]"})
                public int steamTurbine_input_tankSize = 12000;
                @Comment({"The capacity of the output tank for the Steam Turbine [Default=12000]"})
                public int steamTurbine_output_tankSize = 12000;
                @Comment({"How fast should the Steam Turbine's axle rotate in degrees per tick (purely cosmetic) [Default=72]"})
                public float steamTurbine_speed_maxRotation = 72;
                @Comment({"Rotating mass of the Steam Turbine. Higher values slow both spin-up and coast-down [Default=10.0]"})
                public double steamTurbine_baseMass = 10.0;
                @Comment({"Torque the Steam Turbine's drive applies [Default=360.0]"})
                public double steamTurbine_driveTorque = 360.0;
                @Comment({"Constant drag on the Steam Turbine [Default=0.0]"})
                public double steamTurbine_friction = 0.0;
                @Comment({"Fraction of the maximum tolerated RPM the Steam Turbine can reach [Default=1.0]"})
                public float steamTurbine_speed_maxFactor = 1.0f;
                @Comment({"How much of the maximum alternator output power should the Steam Turbine generate [Default=1.0]"})
                public float steamTurbine_torque = 1.0f;
            }
            public static class SteelTank {
                @Comment({"Steel Tank Size in mB [Default=2048000]"})
                public int steelTank_tankSize = 2048000;
                @Comment({"How fast can the Steel Tank push fluids out, in mB, when powered by Redstone [Default=1000]"})
                public int steelTank_transferSpeed = 1000;
            }
        }
        public static class Client {
            public static Particles particles = new Particles();
            public static Render render = new Render();

            public static class Particles {
                @Comment({"Should smoke particles collide with blocks instead of drifting through them [Default=false]"})
                public boolean collide = false;
                @Comment({"How strongly the tinted smoke from the boilers, turbines, coke oven and solar melter rises [Default=0.7]"})
                public double colored_smoke_height = 0.7;
                @Comment({"Height scale for the cooling tower's smoke. 1.0 is the shipped height [Default=1.0]"})
                public double custom_smoke_height = 1.0;
            }
            public static class Render {
                @Comment({"Disables the lighting code for models rendered dynamically (TESR). May improve FPS [Default=false]"})
                public boolean disableFancyTESR = false;
                @Comment({"Should the animations and special client rendering apply to the Gas Turbine [Default=true]"})
                public boolean gas_turbine_renderer = true;
                @Comment({"This modifies the distance a special multiblock renderer is visible from [Default=2.5]"})
                public double multiblockSpecialRenderDistanceModifier = 2.5;
                @Comment({"Should the animations and special client rendering apply to the Solar Reflector [Default=true]"})
                public boolean solar_reflector_renderer = true;
                @Comment({"Should the animations and special client rendering apply to the Steam Turbines [Default=true]"})
                public boolean steam_turbine_renderer = true;
            }
        }
        public static class Settings {
            public static Experimental experimental = new Experimental();

            public static class Experimental {
                @Comment({"Should the text overlay for trash cans be per tick rather than per second? [Default=false]"})
                public boolean per_tick_trash_cans = false;
                @Comment({"Should pipes use round robin (false), which is more CPU intensive, or last served (true), which remembers the last valid path (closest first) [Default=false]"})
                public boolean pipe_last_served = false;
                @Comment({"How much should the pipes be capable of transferring when pressurized, in mb. [Default=2500]"})
                public int pipe_pressurized_transfer_rate = 2500;
                @Comment({"How much should the pipes be capable of transferring, in mb. [Default=100]"})
                public int pipe_transfer_rate = 100;
            }
        }
    }
}
