package ferro2000.immersivetech.common;

import blusunrize.immersiveengineering.common.Config.Mapped;
import ferro2000.immersivetech.ImmersiveTech;
import ferro2000.immersivetech.api.craftings.DistillerRecipes;
import ferro2000.immersivetech.api.craftings.SolarTowerRecipes;
import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntitySolarTower;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class Config {
		
	@net.minecraftforge.common.config.Config(modid=ImmersiveTech.MODID)
	public static class ITConfig{
		
		public static Machines machines = new Machines();
		
		public static class Machines{
			
			/*MULTIBLOCK*/
			/*ENERGY*/
			@Comment({"The Flux per tick that the Steam Turbine will output"})
			public static int steamTurbine_output = 12288;
			
			/*RECIPE*/
			@Comment({"A modifier to apply to the time of every Solar Tower recipe"})
			public static int solarTower_timeModifier = 1;
			@Comment({"The quantity of Steam from a bucket of Water"})
			public static int solarTower_steamWater = 500;
			@Comment({"The quantity of Steam from a bucket of Distilled Water"})
			public static int solarTower_steamDistWater = 750;
			
			@Comment({"A modifier to apply to the time of every Distiller recipe"})
			public static int distiller_timeModifier = 1;
			@Comment({"A modifier to apply to the energy costs of every Distiller recipe"})
			public static int distiller_energyModifier = 1;
			@Comment({"The quantity of Distilled Water from a bucket of Water"})
			public static int distiller_distWaterWater = 500;
			
			/*MISC*/
			@Comment({"The max distance between the Solar Tower and the Solar Reflectors"})
			public static int solarTower_range = 10;
			@Comment({"The burn time of Steam"})
			public static int steam_burnTime = 250;
			
		}
		
	}
	
	static Configuration config;
	
	public static void preInit(FMLPreInitializationEvent event)
	{

		SolarTowerRecipes.timeModifier = ITConfig.Machines.solarTower_timeModifier;
		ITContent.steamWater = ITConfig.Machines.solarTower_steamWater;
		ITContent.steamDistWater = ITConfig.Machines.solarTower_steamDistWater;
		
		DistillerRecipes.timeModifier = ITConfig.Machines.distiller_timeModifier;
		DistillerRecipes.energyModifier = ITConfig.Machines.distiller_energyModifier;
		ITContent.distWaterWater = ITConfig.Machines.distiller_distWaterWater;
		
		TileEntitySolarTower.range = ITConfig.Machines.solarTower_range;
		ITContent.steamBurnTime = ITConfig.Machines.steam_burnTime;
		
	}

}
