package ferro2000.immersivetech.common;

import ferro2000.immersivetech.ImmersiveTech;
import net.minecraftforge.common.config.Config.Comment;

public class Config {
		
	@net.minecraftforge.common.config.Config(modid=ImmersiveTech.MODID)
	public static class ITConfig{
		
		public static Machines machines;
		
		public static class Machines{

			/*MULTIBLOCK*/
			@Comment({"Can the Steam Turbine Multiblock structure be built?"})
			public static boolean enable_steamTurbine = true;
			@Comment({"Can the Alternator Multiblock structure be built?"})
			public static boolean enable_alternator = true;
			@Comment({"Can the Boiler Multiblock structure be built?"})
			public static boolean enable_boiler = true;
			@Comment({"Can the Distiller Multiblock structure be built?"})
			public static boolean enable_distiller = true;
			@Comment({"Can the Solar Tower Multiblock structure be built?"})
			public static boolean enable_solarTower = true;
			@Comment({"Can the Solar Reflector Multiblock structure be built?"})
			public static boolean enable_solarReflector = true;
			@Comment({"Can the Coke Oven Advanced Multiblock structure be built?"})
			public static boolean enable_cokeOvenAdvanced = true;

			/*RECIPES*/
			@Comment({"Should default Steam Turbine recipes be registered?"})
			public static boolean register_steamTurbine_recipes = true;
			@Comment({"Should default Boiler recipes be registered?"})
			public static boolean register_boiler_recipes = true;
			@Comment({"Should default Solar Tower recipes be registered?"})
			public static boolean register_solarTower_recipes = true;
			@Comment({"Should default Distiller recipes be registered?"})
			public static boolean register_distiller_recipes = true;

			/*ENERGY*/
			@Comment({"The capacity of the input tank for the Steam Turbine"})
			public static int steamTurbine_input_tankSize = 2000;
			@Comment({"The capacity of the output tank for the Steam Turbine"})
			public static int steamTurbine_output_tankSize = 10000;
			@Comment({"The maximum torque that machines can consume or produce"})
			public static int mechanicalEnergy_maxTorque = 8192;
			@Comment({"The maximum speed that machines can tolerate, in RPM"})
			public static int mechanicalEnergy_maxSpeed = 1800;
			@Comment({"How fast the Steam Turbine increases in speed per tick"})
			public static int steamTurbine_speedGainPerTick = 3;
			@Comment({"How fast the Steam Turbine increases in torque per tick"})
			public static int steamTurbine_torqueGainPerTick = 14;
			@Comment({"How fast the Steam Turbine loses speed per tick when inactive"})
			public static int steamTurbine_speedLossPerTick = 6;
			@Comment({"How fast the Steam Turbine loses torque per tick when inactive"})
			public static int steamTurbine_torqueLossPerTick = 28;
			@Comment({"How fast should the Steam Turbine's axle rotate in degrees per tick (purely cosmetic)"})
			public static float steamTurbine_maxRotationSpeed = 72;
			
			@Comment({"The max of Flux that the Alternator can store"})
			public static int alternator_energyStorage = 1200000;
			@Comment({"Flux production when running at maximum speed and torque"})
			public static int alternator_RfPerTick = 24576;
			@Comment({"The max of Flux that the Alternator can output per each energy device connected"})
			public static int alternator_RfPerTickPerPort = 4096;
			
			/*BLOCK*/
			/*ENERGY*/
			@Comment({"The Flux per tick the Coke Oven Preheater will consume to speed up the Coke Oven Advanced"})
			public static int cokeOvenPreheater_consumption = 32;

			@Comment({"A modifier to apply to the burn time of fuel into the Boiler: (1000 / fuelBurnTime) * (4 * boiler_burnTimeModifier) mb/t {fuelBurnTime [biodiesel = 125], [fuel = 375], [diesel = 175]}"})
			public static int boiler_burnTimeModifier = 1;

			@Comment({"The capacity of the fuel tank for the Boiler"})
			public static int boiler_fuel_tankSize = 2000;
			@Comment({"The capacity of the input tank for the Boiler"})
			public static int boiler_input_tankSize = 20000;
			@Comment({"The capacity of the output tank for the Boiler"})
			public static int boiler_output_tankSize = 20000;
			@Comment({"A boiler can only start processing its recipes once it reaches this heat level"})
			public static double boiler_workingHeatLevel = 12000;
			@Comment({"How fast the boiler cools down per tick when turned off or missing fuel"})
			public static int boiler_heatLossPerTick = 5;
			@Comment({"How fast the boiler loses progress in ticks when the heat drops below working point"})
			public static int boiler_progressLossInTicks = 1;
			
			/*MISC*/
			@Comment({"The minimun distance between the Solar Tower and the Solar Reflectors"})
			public static int solarTower_minRange = 5;
			@Comment({"The max distance between the Solar Tower and the Solar Reflectors"})
			public static int solarTower_maxRange = 10;
			
		}
		
	}
}
