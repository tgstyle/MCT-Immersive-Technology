package ferro2000.immersivetech.common.util;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import ferro2000.immersivetech.ImmersiveTech;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.HashSet;
import java.util.Set;

public class ITSounds {

    static Set<SoundEvent> registeredEvents = new HashSet<>();
    public static SoundEvent turbine = registerSound("turbine");
    public static SoundEvent alternator = registerSound("alternator");
    public static SoundEvent boiler = registerSound("boiler");
    public static SoundEvent distiller = registerSound("distiller");

    private static SoundEvent registerSound(String name)
    {
        ResourceLocation location = new ResourceLocation(ImmersiveTech.MODID, name);
        SoundEvent event = new SoundEvent(location);
        registeredEvents.add(event.setRegistryName(location));
        return event;
    }

    public static void init()
    {
        for(SoundEvent event : registeredEvents)
            ForgeRegistries.SOUND_EVENTS.register(event);
    }
}
