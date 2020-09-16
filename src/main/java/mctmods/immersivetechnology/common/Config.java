package mctmods.immersivetechnology.common;

import mctmods.immersivetechnology.ImmersiveTechnology;
import net.minecraftforge.common.config.Config.Comment;

public class Config {
	@net.minecraftforge.common.config.Config(modid=ImmersiveTechnology.MODID, name="mctimmersivetechnology")
	public static class ITConfig {
		public static Machines machines;
		public static MechanicalEnergy mechanicalenergy;
		public static Barrels barrels;
		public static Experimental experimental;

		public static class Machines {
			public static Multiblock multiblock;
			public static Recipes recipes;
			public static Alternator alternator;
			public static Boiler boiler;
			public static AdvancedCokeOven advancedcokeoven;
			public static CokeOvenPreheater cokeovenpreheater;
			public static Distiller distiller;
			public static SolarReflector solarreflector;
			public static SolarTower solartower;
			public static SteamTurbine steamturbine;
			public static SteelTank steeltank;
			public static CoolingTower coolingTower;
			public static GasTurbine gasTurbine;

			public static class Multiblock {
				@Comment({"**WARNING** disable this before you load a new world or break the multiblocks before you do this!!! Can the Advanced Coke Oven Multiblock structure and Preheater be built ? [Default=true]"})
				public static boolean enable_advancedCokeOven = true;
				@Comment({"**WARNING** disable this before you load a new world or break the multiblocks before you do this!!! Can the Boiler Multiblock structure be built ? [Default=true]"})
				public static boolean enable_boiler = true;
				@Comment({"**WARNING** disable this before you load a new world or break the multiblocks before you do this!!! Can the Distiller Multiblock structure be built ? [Default=true]"})
				public static boolean enable_distiller = true;
				@Comment({"**WARNING** disable this before you load a new world or break the multiblocks before you do this!!! Can the Solar Tower / Solar Reflector Multiblock structures be built ? [Default=true]"})
				public static boolean enable_solarTower = true;
				@Comment({"**WARNING** disable this before you load a new world or break the multiblocks before you do this!!! Can the Steam Turbine / Alternator Multiblock structures be built ? [Default=true]"})
				public static boolean enable_steamTurbine = true;
				@Comment({"**WARING** disable this before you load a new world or break the multiblocks before you do this!!! Can the Cooling Tower Multiblock structures be built ? [Default=true]"})
				public static boolean enable_coolingTower = true;
				@Comment({"**WARING** disable this before you load a new world or break the multiblocks before you do this!!! Can the Gas Turbine Multiblock structures be built ? [Default=true]"})
				public static boolean enable_gasTurbine = true;
			}
			public static class Recipes {
				@Comment({"Should default Steam Turbine recipes be registered ? [Default=true]"})
				public static boolean register_steamTurbine_recipes = true;
				@Comment({"Should default Boiler recipes be registered ? [Default=true]"})
				public static boolean register_boiler_recipes = true;
				@Comment({"Should default Solar Tower recipes be registered ? [Default=true]"})
				public static boolean register_solarTower_recipes = true;
				@Comment({"Should default Distiller recipes be registered ? [Default=true]"})
				public static boolean register_distiller_recipes = true;
				@Comment({"Should default Cooling Tower recipes be registered ? [Default=true]"})
				public static boolean register_cooling_tower_recipes = true;
				@Comment({"Should default Gas Turbine recipes be registered ? [Default=true]"})
				public static boolean register_gas_turbine_recipes = true;
			}
			public static class AdvancedCokeOven {
				@Comment({"The capacity of the tank for the Advanced Coke Oven [Default=24000]"})
				public static int advancedCokeOven_tankSize = 24000;
				@Comment({"How fast the Advanced Coke Oven (with no preheaters) is when compared to the basic Coke Oven. A value of 1 means same speed. [Default=1]"})
				public static float advancedCokeOven_speed_base = 1;
				@Comment({"This value gets added per preheater, to the speed of the Advanced Coke Oven [Default=1]"})
				public static float advancedCokeOven_preheater_speed_increase = 1;
				@Comment({"The speed of the Advanced Coke Oven gets multiplied by this value per preheater [Default=1]"})
				public static float advancedCokeOven_preheater_speed_multiplier = 1;
			}
			public static class CoolingTower {
				@Comment({"The capacity of the input tanks for the Cooling Tower [Default=10000]"})
				public static int coolingTower_input_tankSize = 10000;
				@Comment({"The capacity of the output tanks for the Cooling Tower [Default=10000]"})
				public static int coolingTower_output_tankSize = 10000;
			}
			public static class Alternator {
				@Comment({"The maximum energy an Alternator can store in if[Default=1200000]"})
				public static int alternator_energy_capacitorSize = 1200000;
				@Comment({"Energy production when running at maximum speed and torque in if[Default=12288]"})
				public static int alternator_energy_perTick = 12288;
				@Comment({"Alternator sound based RPM or Capacity [Default=true]"})
				public static boolean alternator_sound_RPM = true;
			}
			public static class Boiler {
				@Comment({"The capacity of the input tank for the Boiler [Default=20000]"})
				public static int boiler_input_tankSize = 20000;
				@Comment({"The capacity of the output tank for the Boiler [Default=20000]"})
				public static int boiler_output_tankSize = 20000;
				@Comment({"The capacity of the fuel tank for the Boiler [Default=2000]"})
				public static int boiler_fuel_tankSize = 2000;
				@Comment({"A Boiler can only start processing recipes once it reaches this heat level [Default=12000.0]"})
				public static double boiler_heat_workingLevel = 12000.0;
				@Comment({"How fast the Boiler cools down per tick when turned off or missing fuel [Default=5]"})
				public static int boiler_heat_lossPerTick = 5;
				@Comment({"How fast the Boiler loses progress in ticks when the heat drops below processing heat level [Default=1]"})
				public static int boiler_progress_lossInTicks = 1;
			}
			public static class CokeOvenPreheater {
				@Comment({"The energy per tick the Coke Oven Preheater consumes while processing in if[Default=32]"})
				public static int cokeOvenPreheater_energy_consumption = 32;
			}
			public static class Distiller {
				@Comment({"The capacity of the input tank for the Distiller [Default=24000]"})
				public static int distiller_input_tankSize = 24000;
				@Comment({"The capacity of the output tank for the Distiller [Default=24000]"})
				public static int distiller_output_tankSize = 24000;
				@Comment({"The item for Salt the Distiller should output [Default=immersivetech:material]"})
				public static String distiller_output_item = "immersivetech:material";
				@Comment({"The item meta for Salt the Distiller should output [Default=0]"})
				public static int distiller_output_itemMeta = 0;
				@Comment({"The item chance for Salt the Distiller should output [Default=0.009999999776482582]"})
				public static float distiller_output_itemChance = 0.009999999776482582F;
			}
			public static class SolarTower {
				@Comment({"The capacity of the input tank for the Solar Tower [Default=32000]"})
				public static int solarTower_input_tankSize = 32000;
				@Comment({"The capacity of the output tank for the Solar Tower [Default=32000]"})
				public static int solarTower_output_tankSize = 32000;
				@Comment({"The heat speed multiplier applied to all Solar Tower recipes (with a single reflector) [Default=1]"})
				public static float solarTower_speed_multiplier = 1;
				@Comment({"The heat speed of the Solar Tower gets multiplied by this value, per Solar Reflector past the first one [Default=1.5]"})
				public static float solarTower_solarReflector_speed_multiplier = 1.5f;
				@Comment({"A Solar Tower can only start processing recipes once it reaches this heat level [Default=6000.0]"})
				public static double solarTower_heat_workingLevel = 6000.0;
				@Comment({"How fast the Solar Tower cools down per tick when turned off or at night [Default=1]"})
				public static int solarTower_heat_lossPerTick = 1;
				@Comment({"How fast the the Solar Tower loses progress in ticks when the heat drops below processing heat level [Default=1]"})
				public static int solarTower_progress_lossInTicks = 1;
			}
			public static class SolarReflector {
				@Comment({"The minimum distance between the Solar Reflectors and the Solar Tower [Default=5]"})
				public static int solarReflector_minRange = 5;
				@Comment({"The maximum distance between the Solar Reflectors and the Solar Tower [Default=10]"})
				public static int solarReflector_maxRange = 10;
			}
			public static class SteamTurbine {
				@Comment({"The capacity of the input tank for the Steam Turbine [Default=10000]"})
				public static int steamTurbine_input_tankSize = 10000;
				@Comment({"The capacity of the output tank for the Steam Turbine [Default=10000]"})
				public static int steamTurbine_output_tankSize = 10000;
				@Comment({"How fast the Steam Turbine increases in speed per tick [Default=3]"})
				public static int steamTurbine_speed_gainPerTick = 3;
				@Comment({"How fast the Steam Turbine loses speed per tick when inactive [Default=6]"})
				public static int steamTurbine_speed_lossPerTick = 6;
				@Comment({"How fast should the Steam Turbine's axle rotate in degrees per tick (purely cosmetic) [Default=72]"})
				public static float steamTurbine_speed_maxRotation = 72;
			}
			public static class GasTurbine {
				@Comment({"The power consumption of the electric starter for the Gas Turbine [Default=3072]"})
				public static int gasTurbine_electric_starter_consumption = 3072;
				@Comment({"The power consumption  of the sparkplug for the Gas Turbine [Default=1024]"})
				public static int gasTurbine_sparkplug_consumption = 1024;
				@Comment({"The capacity of the electric starter for the Gas Turbine [Default=3072]"})
				public static int gasTurbine_electric_starter_size = 3072;
				@Comment({"The capacity of the sparkplug for the Gas Turbine [Default=1024]"})
				public static int gasTurbine_sparkplug_size = 1024;
				@Comment({"The capacity of the input tank for the Gas Turbine [Default=10000]"})
				public static int gasTurbine_input_tankSize = 10000;
				@Comment({"The capacity of the output tank for the Gas Turbine [Default=10000]"})
				public static int gasTurbine_output_tankSize = 10000;
				@Comment({"How fast the Gas Turbine increases in speed per tick [Default=3]"})
				public static int gasTurbine_speed_gainPerTick = 3;
				@Comment({"How fast the Gas Turbine loses speed per tick when inactive [Default=6]"})
				public static int gasTurbine_speed_lossPerTick = 6;
				@Comment({"How fast should the Steam Turbine's axle rotate in degrees per tick (purely cosmetic) [Default=72]"})
				public static float gasTurbine_speed_maxRotation = 72;
			}
			public static class SteelTank {
				@Comment({"Steel Tank Size in mB [Default=2048000]"})
				public static int steelTank_tankSize = 2048000;
				@Comment({"How fast can the Steel Tank push fluids out, in mB, when powered by Redstone [Default=1000]"})
				public static int steelTank_transferSpeed = 1000;
			}
		}
		public static class MechanicalEnergy {
			@Comment({"The maximum speed that machines can tolerate in RPM [Default=1800]"})
			public static int mechanicalEnergy_speed_max = 1800;
		}
		public static class Barrels {
			@Comment({"The capacity of the tank for the Open Barrel [Default=12000]"})
			public static int barrel_open_tankSize = 12000;
			@Comment({"How fast can the Open Barrel push fluids out, in mB [Default=40]"})
			public static int barrel_open_transferSpeed = 40;
			@Comment({"The capacity of the tank for the Steel Barrel [Default=24000]"})
			public static int barrel_steel_tankSize = 24000;
			@Comment({"How fast can the Steel Barrel push fluids out, in mB [Default=500]"})
			public static int barrel_steel_transferSpeed = 500;
		}
		public static class Experimental {
			@Comment({"Should the text overlay for trash cans be per tick rather than per second? [Default=false]"})
			public static boolean per_tick_trash_cans = false;
			@Comment({"Replace IE pipes with IT's own version. [Default=true]"})
			public static boolean replace_IE_pipes = true;
			@Comment({"Replace pipe pathing algorithm with IT's own version. Only works if also replacing IE pipes! [Default=false]"})
			public static boolean replace_pipe_algorithm = false;
			@Comment({"How much should the pipes be capable of transfering, in mb. [Default=50]"})
			public static int pipe_transfer_rate = 50;
			@Comment({"How much should the pipes be capable of transfering when pressurized, in mb. [Default=1000]"})
			public static int pipe_pressurized_transfer_rate = 1000;
		}
	}

}