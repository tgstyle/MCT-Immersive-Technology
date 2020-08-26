package mctmods.immersivetechnology.common.util;

import mctmods.immersivetechnology.ImmersiveTechnology;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.HashSet;
import java.util.Set;

public class ITSounds {

	static Set<ITSoundEvent> registeredEvents = new HashSet<>();
	public static ITSoundEvent turbine = registerSound("turbine", SoundCategory.BLOCKS);
	public static ITSoundEvent alternator = registerSound("alternator", SoundCategory.BLOCKS);
	public static ITSoundEvent boiler = registerSound("boiler", SoundCategory.BLOCKS);
	public static ITSoundEvent distiller = registerSound("distiller", SoundCategory.BLOCKS);
	public static ITSoundEvent advCokeOven = registerSound("advCokeOven", SoundCategory.BLOCKS);
	public static ITSoundEvent solarTower = registerSound("solarTower", SoundCategory.BLOCKS);
	public static ITSoundEvent coolingTower = registerSound("coolingTower", SoundCategory.BLOCKS);
	public static ITSoundEvent gasTurbineRunning = registerSound("gasTurbineRunning", SoundCategory.BLOCKS);
	public static ITSoundEvent gasTurbineStarter = registerSound("gasTurbineStarter", SoundCategory.BLOCKS);
	public static ITSoundEvent gasTurbineArc = registerSound("gasTurbineArc", SoundCategory.BLOCKS);
	public static ITSoundEvent gasTurbineSpark = registerSound("gasTurbineSpark", SoundCategory.BLOCKS);

	private static ITSoundEvent registerSound(String name, SoundCategory category) {
		ResourceLocation location = new ResourceLocation(ImmersiveTechnology.MODID, name);
		ITSoundEvent event = new ITSoundEvent(location, category);
		event.setRegistryName(location);
		registeredEvents.add(event);
		return event;
	}

	public static void init() {
		for(SoundEvent event : registeredEvents)
			ForgeRegistries.SOUND_EVENTS.register(event);
	}

}