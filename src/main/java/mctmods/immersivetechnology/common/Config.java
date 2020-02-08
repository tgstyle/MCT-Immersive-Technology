package mctmods.immersivetechnology.common;

import mctmods.immersivetechnology.ImmersiveTech;
import net.minecraftforge.common.config.Config.Comment;

public class Config {
	@net.minecraftforge.common.config.Config(modid=ImmersiveTech.MODID, name="immersivetechnology")
	public static class ITConfig {
		public static Machines machines;
		public static MechanicalEnergy mechanicalenergy;
		public static Trash trash;

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

			public static class Multiblock {
				@Comment({"Can the Steam Turbine Multiblock structure be built ? [Default=true]"})
				public static boolean enable_steamTurbine = true;
				@Comment({"Can the Alternator Multiblock structure be built ? [Default=true]"})
				public static boolean enable_alternator = true;
				@Comment({"Can the Boiler Multiblock structure be built ? [Default=true]"})
				public static boolean enable_boiler = true;
				@Comment({"Can the Distiller Multiblock structure be built ? [Default=true]"})
				public static boolean enable_distiller = true;
				@Comment({"Can the Solar Tower Multiblock structure be built ? [Default=true]"})
				public static boolean enable_solarTower = true;
				@Comment({"Can the Solar Reflector Multiblock structure be built ? [Default=true]"})
				public static boolean enable_solarReflector = true;
				@Comment({"Can the Coke Oven Advanced Multiblock structure be built ? [Default=true]"})
			public static boolean enable_advancedCokeOven = true;
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
			}
			public static class AdvancedCokeOven {
				@Comment({"How fast the Advanced Coke Oven (with no preheaters) is when compared to the basic Coke Oven. A value of 1 means same speed. [Default=1]"})
				public static float advancedCokeOven_speed_base = 1;
				@Comment({"This value gets added per preheater, to the speed of the Advanced Coke Oven [Default=1]"})
				public static float advancedCokeOven_preheater_speed_increase = 1;
				@Comment({"The speed of the Advanced Coke Oven gets multiplied by this value per preheater [Default=1]"})
				public static float advancedCokeOven_preheater_speed_multiplier = 1;
			}
			public static class Alternator {
				@Comment({"The maximum energy an Alternator can store in IF [Default=1200000]"})
				public static int alternator_energy_capacitorSize = 1200000;
				@Comment({"Energy production when running at maximum speed and torque in IF [Default=12288]"})
				public static int alternator_energy_perTick = 12288;
			}
			public static class Boiler {
				@Comment({"The capacity of the fuel tank for the Boiler [Default=2000]"})
				public static int boiler_fuel_tankSize = 2000;
				@Comment({"The capacity of the input tank for the Boiler [Default=20000]"})
				public static int boiler_input_tankSize = 20000;
				@Comment({"The capacity of the output tank for the Boiler [Default=20000]"})
				public static int boiler_output_tankSize = 20000;
				@Comment({"A boiler can only start processing recipes once it reaches this heat level [Default=12000.0]"})
				public static double boiler_heat_workingLevel = 12000.0;
				@Comment({"How fast the boiler cools down per tick when turned off or missing fuel [Default=5]"})
				public static int boiler_heat_lossPerTick = 5;
				@Comment({"How fast the boiler loses progress in ticks when the heat drops below processing heat level [Default=1]"})
				public static int boiler_progress_lossInTicks = 1;
			}
			public static class CokeOvenPreheater {
				@Comment({"The energy per tick the Coke Oven Preheater consumes while processing in IF [Default=32]"})
				public static int cokeOvenPreheater_energy_consumption = 32;
			}
			public static class Distiller {
				@Comment({"The item for Salt the distiller should output [Default=immersivetech:material]"})
				public static String distiller_output_item = "immersivetech:material";
				@Comment({"The item meta for Salt the distiller should output [Default=0]"})
				public static int distiller_output_itemMeta = 0;
				@Comment({"The item chance for Salt the distiller should output [Default=0.009999999776482582]"})
				public static float distiller_output_itemChance = 0.009999999776482582F;
			}
			public static class SolarTower {
				@Comment({"The Speed multiplier applied to all Solar Tower recipes (with a single reflector) [Default=1]"})
				public static float solarTower_speed_multiplier = 1;
				@Comment({"The speed of the Solar Tower gets multiplied by this value, per Solar Reflector past the first one [Default=1.5]"})
				public static float solarTower_solarReflector_speed_multiplier = 1.5f;
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

		}

		public static class MechanicalEnergy {
			@Comment({"The maximum speed that machines can tolerate in RPM [Default=1800]"})
			public static int mechanicalEnergy_speed_max = 1800;
		}

		public static class Trash {
			@Comment({"Energy Trash Size in IF [Default=100000]"})
			public static int trash_energy_capacitorSize = 1000000;
			@Comment({"Fluid Trash Size in mB [Default=100000]"})
			public static int trash_fluid_tankSize = 100000;
		}
	}

}