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
	public static ITSoundEvent boiler = registerSound("boiler");
	public static ITSoundEvent coolingTower = registerSound("coolingTower");
	public static ITSoundEvent distiller = registerSound("distiller");
	public static ITSoundEvent gasTurbineArc = registerSound("gasTurbineArc");
	public static ITSoundEvent gasTurbineRunning = registerSound("gasTurbineRunning");
	public static ITSoundEvent gasTurbineSpark = registerSound("gasTurbineSpark");
	public static ITSoundEvent gasTurbineStarter = registerSound("gasTurbineStarter");
	public static ITSoundEvent heatExchanger = registerSound("heatExchanger");
	public static ITSoundEvent solarTower = registerSound("solarTower");
	public static ITSoundEvent turbine = registerSound("turbine");

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
