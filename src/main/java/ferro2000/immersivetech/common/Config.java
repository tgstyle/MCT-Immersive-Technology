package ferro2000.immersivetech.common;

import ferro2000.immersivetech.ImmersiveTech;

import net.minecraftforge.common.config.Config.Comment;

public class Config {
	@net.minecraftforge.common.config.Config(modid=ImmersiveTech.MODID, name="immersivetechnology")
	public static class ITConfig {
		public static Machines machines;

		public static class Machines {

			/*MULTIBLOCK*/
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
			public static boolean enable_cokeOvenAdvanced = true;

			/*RECIPES*/
			@Comment({"Should default Steam Turbine recipes be registered ? [Default=true]"})
			public static boolean register_steamTurbine_recipes = true;
			@Comment({"Should default Boiler recipes be registered ? [Default=true]"})
			public static boolean register_boiler_recipes = true;
			@Comment({"Should default Solar Tower recipes be registered ? [Default=true]"})
			public static boolean register_solarTower_recipes = true;
			@Comment({"Should default Distiller recipes be registered ? [Default=true]"})
			public static boolean register_distiller_recipes = true;

			/*MACHINES*/
			@Comment({"The capacity of the input tank for the Steam Turbine [Default=10000]"})
			public static int steamTurbine_input_tankSize = 10000;
			@Comment({"The capacity of the output tank for the Steam Turbine [Default=10000]"})
			public static int steamTurbine_output_tankSize = 10000;
			@Comment({"The maximum speed that machines can tolerate, in RPM [Default=1800]"})
			public static int mechanicalEnergy_maxSpeed = 1800;
			@Comment({"How fast the Steam Turbine increases in speed per tick [Default=3]"})
			public static int steamTurbine_speedGainPerTick = 3;
			@Comment({"How fast the Steam Turbine loses speed per tick when inactive [Default=6]"})
			public static int steamTurbine_speedLossPerTick = 6;
			@Comment({"How fast should the Steam Turbine's axle rotate in degrees per tick (purely cosmetic) [Default=72]"})
			public static float steamTurbine_maxRotationSpeed = 72;
			@Comment({"The max of Flux that the Alternator can store [Default=1200000]"})
			public static int alternator_energyStorage = 1200000;
			@Comment({"Flux production when running at maximum speed and torque [Default=12288]"})
			public static int alternator_RfPerTick = 12288;

			/*ENERGY*/
			@Comment({"The Flux per tick the Coke Oven Preheater will consume to speed up the Coke Oven Advanced [Default=32]"})
			public static int cokeOvenPreheater_consumption = 32;
			@Comment({"The capacity of the fuel tank for the Boiler [Default=2000]"})
			public static int boiler_fuel_tankSize = 2000;
			@Comment({"The capacity of the input tank for the Boiler [Default=20000]"})
			public static int boiler_input_tankSize = 20000;
			@Comment({"The capacity of the output tank for the Boiler [Default=20000]"})
			public static int boiler_output_tankSize = 20000;
			@Comment({"A boiler can only start processing its recipes once it reaches this heat level [Default=12000.0]"})
			public static double boiler_workingHeatLevel = 12000.0;
			@Comment({"How fast the boiler cools down per tick when turned off or missing fuel [Default=5]"})
			public static int boiler_heatLossPerTick = 5;
			@Comment({"How fast the boiler loses progress in ticks when the heat drops below working point [Default=1]"})
			public static int boiler_progressLossInTicks = 1;

			/*MISC*/
			@Comment({"The minimun distance between the Solar Tower and the Solar Reflectors [Default=5]"})
			public static int solarTower_minRange = 5;
			@Comment({"The max distance between the Solar Tower and the Solar Reflectors [Default=10]"})
			public static int solarTower_maxRange = 10;
			@Comment({"The item for Salt the distiller should output [Default=immersivetech:material]"})
			public static String distiller_outputItem = "immersivetech:material";
			@Comment({"The item meta for Salt the distiller should output [Default=0]"})
			public static int distiller_outputItemMeta = 0;
			@Comment({"The chance for Salt the distiller should output [Default=0.009999999776482582]"})
			public static float distiller_outputItemChance = 0.009999999776482582F;
		}
	}

}