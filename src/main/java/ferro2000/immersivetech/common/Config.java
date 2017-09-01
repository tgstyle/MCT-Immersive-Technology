package ferro2000.immersivetech.common;

import ferro2000.immersivetech.ImmersiveTech;
import ferro2000.immersivetech.api.craftings.BoilerRecipes;
import ferro2000.immersivetech.api.craftings.DistillerRecipes;
import ferro2000.immersivetech.api.craftings.SolarTowerRecipes;
import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntitySolarTower;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class Config {
		
	@net.minecraftforge.common.config.Config(modid=ImmersiveTech.MODID)
	public static class ITConfig{
		
		public static Machines machines;
		
		public static class Machines{
			
			/*MULTIBLOCK*/
			/*ENERGY*/
			@Comment({"The Flux per tick that the Steam Turbine will output"})
			public static int steamTurbine_output = 12288;
			@Comment({"A modifier to apply to the burn time of steam into the SteamTurbine: (1000 / steamBurnTime) * steamTurbine_burnTimeModifier mb/t"})
			public static int steamTurbine_burnTimeModifier = 5;
			
			/*RECIPE*/
			@Comment({"A modifier to apply to the time of every Solar Tower recipe"})
			public static int solarTower_timeModifier = 1;
			
			@Comment({"A modifier to apply to the time of every Distiller recipe"})
			public static int distiller_timeModifier = 1;
			@Comment({"A modifier to apply to the energy costs of every Distiller recipe"})
			public static int distiller_energyModifier = 1;
			
			@Comment({"A modifier to apply to the time of every Boiler recipe"})
			public static int boiler_timeModifier = 1;
			@Comment({"A modifier to apply to the burn time of fuel into the Boiler: (1000 / fuelBurnTime) * boiler_burnTimeModifier mb/t"})
			public static int boiler_burnTimeModifier = 4;
			
			/*MISC*/
			@Comment({"The max distance between the Solar Tower and the Solar Reflectors"})
			public static int solarTower_range = 10;
			
		}
		
	}
	
	static Configuration config;
	
	public static void preInit(FMLPreInitializationEvent event)
	{

		SolarTowerRecipes.timeModifier = ITConfig.Machines.solarTower_timeModifier;
		
		DistillerRecipes.timeModifier = ITConfig.Machines.distiller_timeModifier;
		DistillerRecipes.energyModifier = ITConfig.Machines.distiller_energyModifier;
		
		BoilerRecipes.timeModifier = ITConfig.machines.boiler_timeModifier;
		
		TileEntitySolarTower.range = ITConfig.Machines.solarTower_range;
		
	}

}
