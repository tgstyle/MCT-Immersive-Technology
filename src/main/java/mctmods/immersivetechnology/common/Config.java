package mctmods.immersivetechnology.common;

import mctmods.immersivetechnology.ImmersiveTechnology;
import net.minecraftforge.common.config.Config.Comment;

@SuppressWarnings("unused")
public class Config {
    @net.minecraftforge.common.config.Config(modid=ImmersiveTechnology.MODID, name="mct_immersivetechnology")
    public static class ITConfig {
        public static Blocks blocks;
        public static Multiblocks multiBlocks;
        public static Settings settings;

        public static class Blocks {
            public static Barrels barrels = new Barrels();

            public static class Barrels {
                @Comment({"The capacity of the tank for the Open Barrel [Default=12000]"})
                public int barrel_open_tankSize = 12000;
                @Comment({"How fast can the Open Barrel push fluids out, in mB [Default=40]"})
                public int barrel_open_transferSpeed = 40;
                @Comment({"The capacity of the tank for the Steel Barrel [Default=24000]"})
                public int barrel_steel_tankSize = 24000;
                @Comment({"How fast can the Steel Barrel push fluids out, in mB [Default=500]"})
                public int barrel_steel_transferSpeed = 500;
            }
        }
        public static class Multiblocks {
            public static Alternator alternator = new Alternator();
            public static AdvancedCokeOven advancedCokeOven = new AdvancedCokeOven();
            public static AdvancedCokeOvenBaseheater advancedCokeOvenBaseheater = new AdvancedCokeOvenBaseheater();
            public static Boiler boiler = new Boiler();
            public static CoolingTower coolingTower = new CoolingTower();
            public static Distiller distiller = new Distiller();
            public static ElectrolyticCrucibleBattery electrolyticCrucibleBattery = new ElectrolyticCrucibleBattery();
            public static Enable enable = new Enable();
            public static GasTurbine gasTurbine = new GasTurbine();
            public static HeatExchanger heatExchanger = new HeatExchanger();
            public static HighPressureSteamTurbine highPressureSteamTurbine = new HighPressureSteamTurbine();
            public static MechanicalEnergy mechanicalEnergy = new MechanicalEnergy();
            public static MeltingCrucible meltingCrucible = new MeltingCrucible();
            public static Radiator radiator = new Radiator();
            public static Recipes recipes = new Recipes();
            public static SolarMelter solarMelter = new SolarMelter();
            public static SolarReflector solarReflector = new SolarReflector();
            public static SolarTower solarTower = new SolarTower();
            public static SteamTurbine steamTurbine = new SteamTurbine();
            public static SteelTank steelTank = new SteelTank();

            public static class Alternator {
                @Comment({"Alternator generation exponent [Default=2.0]"})
                public double alternator_exponent = 2.0;
                @Comment({"The maximum energy an Alternator can store [Default=1200000]"})
                public int alternator_energy_capacitorSize = 1200000;
                @Comment({"Energy production when running at maximum speed and torque [Default=12288]"})
                public int alternator_energy_perTick = 12288;
                @Comment({"Alternator sound based RPM or Capacity [Default=true]"})
                public boolean alternator_sound_RPM = true;
                @Comment({"Alternator generation threshold (fraction of speed below which it will not produce power, to emulate grid syncing) [Default=0.0]"})
                public double alternator_threshold = 0.0;
            }
            public static class AdvancedCokeOven {
                @Comment({"The capacity of the tank for the Advanced Coke Oven [Default=24000]"})
                public int advancedCokeOven_tankSize = 24000;
                @Comment({"How fast the Advanced Coke Oven (with no baseheaters) is when compared to the basic Coke Oven. A value of 1 means same speed. [Default=1]"})
                public float advancedCokeOven_speed_base = 1;
                @Comment({"This value gets added per baseheater, to the speed of the Advanced Coke Oven [Default=1]"})
                public float advancedCokeOven_baseheater_speed_increase = 1;
                @Comment({"The speed of the Advanced Coke Oven gets multiplied by this value per baseheater [Default=1]"})
                public float advancedCokeOven_baseheater_speed_multiplier = 1;
            }
            public static class AdvancedCokeOvenBaseheater {
                @Comment({"The energy per tick the Coke Oven Baseheater consumes while processing [Default=32]"})
                public int advancedCokeOvenBaseheater_energy_consumption = 32;
            }
            public static class Boiler {
                @Comment({"The capacity of the fuel tank for the Boiler [Default=2000]"})
                public int boiler_fuel_tankSize = 2000;
                @Comment({"How fast the Boiler cools down per tick when turned off or missing fuel [Default=5]"})
                public int boiler_heat_lossPerTick = 5;
                @Comment({"A Boiler can only start processing recipes once it reaches this heat level [Default=12000.0]"})
                public double boiler_heat_workingLevel = 12000.0;
                @Comment({"The capacity of the input tank for the Boiler [Default=20000]"})
                public int boiler_input_tankSize = 20000;
                @Comment({"The capacity of the output tank for the Boiler [Default=20000]"})
                public int boiler_output_tankSize = 20000;
                @Comment({"How fast the Boiler loses progress in ticks when the heat drops below processing heat level [Default=1]"})
                public int boiler_progress_lossInTicks = 1;
            }
            public static class CoolingTower {
                @Comment({"The capacity of the input tanks for the Cooling Tower [Default=20000]"})
                public int coolingTower_input_tankSize = 20000;
                @Comment({"The capacity of the output tanks for the Cooling Tower [Default=20000]"})
                public int coolingTower_output_tankSize = 20000;
            }
            public static class Distiller {
                @Comment({"The capacity of the input tank for the Distiller [Default=24000]"})
                public int distiller_input_tankSize = 24000;
                @Comment({"The item chance for Salt the Distiller should output [Default=0.009999999776482582]"})
                public float distiller_output_itemChance = 0.009999999776482582F;
                @Comment({"The item for Salt the Distiller should output [Default=immersivetech:material]"})
                public String distiller_output_item = "immersivetech:material";
                @Comment({"The item meta for Salt the Distiller should output [Default=0]"})
                public int distiller_output_itemMeta = 0;
                @Comment({"The capacity of the output tank for the Distiller [Default=24000]"})
                public int distiller_output_tankSize = 24000;
                @Comment({"The maximum energy a Distiller can store [Default=16000]"})
                public int distiller_energy_size = 16000;
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
                @Comment({"**WARNING** disable this before you load a new world or break the multiblocks before you do this!!! Can the Boiler Multiblock structure be built ? [Default=true]"})
                public boolean enable_boiler = true;
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
                @Comment({"The power consumption of the electric starter for the Gas Turbine [Default=3072]"})
                public int gasTurbine_electric_starter_consumption = 3072;
                @Comment({"The capacity of the electric starter for the Gas Turbine [Default=3072]"})
                public int gasTurbine_electric_starter_size = 6144;
                @Comment({"The capacity of the input tank for the Gas Turbine [Default=10000]"})
                public int gasTurbine_input_tankSize = 10000;
                @Comment({"The capacity of the output tank for the Gas Turbine [Default=10000]"})
                public int gasTurbine_output_tankSize = 10000;
                @Comment({"How fast should the Steam Turbine's axle rotate in degrees per tick (purely cosmetic) [Default=72]"})
                public float gasTurbine_speed_maxRotation = 72;
                @Comment({"How fast the Gas Turbine increases in speed per tick [Default=3]"})
                public int gasTurbine_speed_gainPerTick = 3;
                @Comment({"How fast the Gas Turbine loses speed per tick when inactive [Default=6]"})
                public int gasTurbine_speed_lossPerTick = 6;
                @Comment({"The power consumption  of the spark plug for the Gas Turbine [Default=1024]"})
                public int gasTurbine_sparkplug_consumption = 1024;
                @Comment({"The capacity of the spark plug for the Gas Turbine [Default=1024]"})
                public int gasTurbine_sparkplug_size = 1024;
                @Comment({"How much of the maximum alternator output power should the Gas Turbine generate [Default=0.5]"})
                public float gasTurbine_torque = 0.5f;
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
                @Comment({"How fast the Steam Turbine increases in speed per tick [Default=3]"})
                public int highPressureSteamTurbine_speed_gainPerTick = 1;
                @Comment({"How fast the Steam Turbine loses speed per tick when inactive [Default=6]"})
                public int highPressureSteamTurbine_speed_lossPerTick = 6;
                @Comment({"Should the steam turbine use tungsten, if it exists? Otherwise the turbine will use nickel [Default=true]"})
                public boolean highPressureSteamTurbine_turbine_material = true;
            }
            public static class MechanicalEnergy {
                @Comment({"The maximum speed that machines can tolerate in RPM [Default=1800]"})
                public int mechanicalEnergy_speed_max = 1800;
            }
            public static class MeltingCrucible {
                @Comment({"Heat loss multiplier for the Melting Crucible. Higher values = faster cooling when unpowered. [Default: 1.0]"})
                public double meltingCrucible_heat_loss_multiplier = 1.0;
                @Comment({"Base heat gain per tick when consuming full heating energy. Actual gain is proportional to energy consumed. [Default=4.5]"})
                public double meltingCrucible_heat_gain_base = 4.5;
                @Comment({"The maximum energy a Melting Crucible can store [Default=50000]"})
                public int meltingCrucible_energy_size = 50000;
                @Comment({"The maximum energy input per tick per port for the Melting Crucible [Default=1024]"})
                public int meltingCrucible_energy_maxInput = 1024;
                @Comment({"A Melting Crucible can only start processing recipes once it reaches this heat level [Default=19400.0]"})
                public double meltingCrucible_heat_workingLevel = 19400.0;
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
            }
            public static class Recipes {
                @Comment({"Should default Boiler recipes be registered ? [Default=true]"})
                public boolean register_boiler_recipes = true;
                @Comment({"Should default Cooling Tower recipes be registered ? [Default=true]"})
                public boolean register_cooling_tower_recipes = true;
                @Comment({"Should default Distiller recipes be registered ? [Default=true]"})
                public boolean register_distiller_recipes = true;
                @Comment({"Should default Electrolytic Crucible Battery recipes be registered ? [Default=false]"})
                public boolean register_electrolyticCrucibleBattery_recipes = true;
                @Comment({"Should default Gas Turbine recipes be registered ? [Default=true]"})
                public boolean register_gas_turbine_recipes = true;
                @Comment({"Should default Heat Exchanger recipes be registered ? [Default=true]"})
                public boolean register_heat_exchanger_recipes = true;
                @Comment({"Should default High Pressure Steam Turbine recipes be registered ? [Default=false]"})
                public boolean register_highPressureSteamTurbine_recipes = false;
                @Comment({"Should default Melting Crucible recipes be registered ? [Default=false]"})
                public boolean register_meltingCrucible_recipes = true;
                @Comment({"Should default Radiator recipes be registered ? [Default=true]"})
                public boolean register_radiator_recipes = true;
                @Comment({"Should default Solar Tower recipes be registered ? [Default=true]"})
                public boolean register_solarTower_recipes = true;
                @Comment({"Should default Steam Turbine recipes be registered ? [Default=true]"})
                public boolean register_steamTurbine_recipes = true;
            }
            public static class SolarMelter {
                @Comment({"How fast the Solar Tower cools down per tick when turned off or at night [Default=1.0]"})
                public double solarMelter_heat_loss_multiplier = 1.0;
                @Comment({"A Solar Melter can only start processing recipes once it reaches this heat level [Default=19400.0]"})
                public double solarMelter_heat_workingLevel = 19400.0;
                @Comment({"The maximum strength of the reflectors. Decreasing this reduces the amount of reflectors needed to achieve max processing speed. [Default=227.5]"})
                public double solarMelter_maximum_reflector_strength = 227.5;
                @Comment({"The capacity of the output tank for the Solar Melter [Default=10000]"})
                public int solarMelter_output_tankSize = 10000;
                @Comment({"Default amount of energy per tick the solar melter loses when not processing. Maximum energy input per tick by mirrors is ~30720  [Default=80]"})
                public int solarMelter_progress_lossEnergy = 80;
                @Comment({"The heat speed multiplier applied to all Solar Tower recipes (with a single reflector) [Default=1]"})
                public float solarMelter_speed_multiplier = 1;
            }
            public static class SolarReflector {
                @Comment({"The maximum distance between the Solar Reflectors and the Solar Tower **WARNING** The tower's ability to produce steam will be severely hampered if this number is small!!! [Default=48]"})
                public int solarReflector_maxRange = 48;
                @Comment({"The minimum distance between the Solar Reflectors and the Solar Tower [Default=12]"})
                public int solarReflector_minRange = 12;
            }
            public static class SolarTower {
                @Comment({"How fast the Solar Tower cools down per tick when turned off or at night [Default=1.0]"})
                public double solarTower_heat_loss_multiplier = 1.0;
                @Comment({"A Solar Tower can only start processing recipes once it reaches this heat level [Default=12000.0]"})
                public double solarTower_heat_workingLevel = 12000.0;
                @Comment({"The capacity of the input tank for the Solar Tower [Default=32000]"})
                public int solarTower_input_tankSize = 32000;
                @Comment({"The maximum strength of the reflectors. Decreasing this reduces the amount of reflectors needed to achieve max processing speed. [Default=227.5]"})
                public double solarTower_maximum_reflector_strength = 227.5;
                @Comment({"The capacity of the output tank for the Solar Tower [Default=32000]"})
                public int solarTower_output_tankSize = 32000;
                @Comment({"How fast the the Solar Tower loses progress in ticks when the heat drops below processing heat level [Default=1]"})
                public int solarTower_progress_lossInTicks = 1;
                @Comment({"The heat speed multiplier applied to all Solar Tower recipes (with a single reflector) [Default=1]"})
                public float solarTower_speed_multiplier = 1;
            }
            public static class SteamTurbine {
                @Comment({"The capacity of the input tank for the Steam Turbine [Default=10000]"})
                public int steamTurbine_input_tankSize = 10000;
                @Comment({"The capacity of the output tank for the Steam Turbine [Default=10000]"})
                public int steamTurbine_output_tankSize = 10000;
                @Comment({"How fast should the Steam Turbine's axle rotate in degrees per tick (purely cosmetic) [Default=72]"})
                public float steamTurbine_speed_maxRotation = 72;
                @Comment({"How fast the Steam Turbine increases in speed per tick [Default=3]"})
                public int steamTurbine_speed_gainPerTick = 3;
                @Comment({"How fast the Steam Turbine loses speed per tick when inactive [Default=6]"})
                public int steamTurbine_speed_lossPerTick = 6;
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
        public static class Settings {
            public static Experimental experimental = new Experimental();

            public static class Experimental {
                @Comment({"Should the text overlay for trash cans be per tick rather than per second? [Default=false]"})
                public boolean per_tick_trash_cans = false;
                @Comment({"Should pipes use round robin (false), which is more CPU intensive, or last served (true), which remembers the last valid path (closest first) [Default=false]"})
                public boolean pipe_last_served = false;
                @Comment({"How much should the pipes be capable of transferring when pressurized, in mb. [Default=1000]"})
                public int pipe_pressurized_transfer_rate = 1000;
                @Comment({"How much should the pipes be capable of transferring, in mb. [Default=50]"})
                public int pipe_transfer_rate = 50;
            }
        }
    }
}
