package ferro2000.immersivetech.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Map;

import ferro2000.immersivetech.ImmersiveTech;
import ferro2000.immersivetech.api.craftings.DistillerRecipes;
import ferro2000.immersivetech.api.craftings.SolarTowerRecipes;
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
			@Comment({"The Flux per tick that the Steam Turbine will output. The burn time of the steam determines the total output"})
			public static int steamTurbine_output = 4096;
			
			/*RECIPE*/
			@Comment({"A modifier to apply to the time of every Solar Tower recipe"})
			public static int solarTower_timeModifier = 1;
			
			@Comment({"A modifier to apply to the time of every Distiller recipe"})
			public static int distiller_timeModifier = 1;
			@Comment({"A modifier to apply to the energy costs of every Distiller recipe"})
			public static int distiller_energyModifier = 1;
			
		}
		
	}
	
	static Configuration config;
	
	public static void preInit(FMLPreInitializationEvent event)
	{

		SolarTowerRecipes.timeModifier = ITConfig.Machines.solarTower_timeModifier;
		
		DistillerRecipes.timeModifier = ITConfig.Machines.distiller_timeModifier;
		DistillerRecipes.energyModifier = ITConfig.Machines.distiller_energyModifier;

		
	}

}
