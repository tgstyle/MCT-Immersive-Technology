package mctmods.immersivetechnology.common.util;

import mctmods.immersivetechnology.ImmersiveTechnology;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
public class ITSounds {
	static Set<ITSoundEvent> registeredEvents = new HashSet<>();
	public static ITSoundEvent advancedCokeOven = registerSound("advancedCokeOven");
	public static ITSoundEvent advancedCokeOvenFan = registerSound("advancedCokeOvenFan");
	public static ITSoundEvent alternator = registerSound("alternator");
	public static ITSoundEvent boilerLiquid = registerSound("boilerLiquid");
	public static ITSoundEvent boilerSolid = registerSound("boilerSolid");
	public static ITSoundEvent pilot = registerSound("pilot");
	public static ITSoundEvent coolingTower = registerSound("coolingTower");
	public static ITSoundEvent distiller = registerSound("distiller");
	public static ITSoundEvent electrolyticCrucibleBattery = registerSound("electrolyticCrucibleBattery");
	public static ITSoundEvent gasIgnite = registerSound("gasIgnite");
	public static ITSoundEvent gasTurbineArc = registerSound("gasTurbineArc");
	public static ITSoundEvent gasTurbineRunning = registerSound("gasTurbineRunning");
	public static ITSoundEvent gasTurbineSpark = registerSound("gasTurbineSpark");
	public static ITSoundEvent gasTurbineStarter = registerSound("gasTurbineStarter");
	public static ITSoundEvent heatExchanger = registerSound("heatExchanger");
	public static ITSoundEvent meltingCrucible = registerSound("meltingCrucible");
	public static ITSoundEvent pressureRelease = registerSound("pressureRelease");
	public static ITSoundEvent solarMelter = registerSound("solarMelter");
	public static ITSoundEvent solarTower = registerSound("solarTower");
	public static ITSoundEvent steamTurbine = registerSound("steamTurbine");

	private static ITSoundEvent registerSound(String name) {
		ResourceLocation location = new ResourceLocation(ImmersiveTechnology.MODID, name);
		ITSoundEvent event = new ITSoundEvent(location, SoundCategory.BLOCKS);
		event.setRegistryName(location);
		registeredEvents.add(event);
		return event;
	}

	public static void init() {
		for(SoundEvent event : registeredEvents) { ForgeRegistries.SOUND_EVENTS.register(event); }
	}
}
