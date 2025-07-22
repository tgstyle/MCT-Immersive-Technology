package mctmods.immersivetechnology.core.registration;

import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("all")
public class ITSounds
{
    private static final DeferredRegister<SoundEvent> REGISTER = DeferredRegister.create(
            Registries.SOUND_EVENT, ITLib.MODID
    );

    public static final RegistryObject<SoundEvent> alternator = registerSound("alternator");
    public static final RegistryObject<SoundEvent> advCokeOven = registerSound("adv_coke_oven");
    public static final RegistryObject<SoundEvent> steamTurbine = registerSound("turbine");
    public static final RegistryObject<SoundEvent> boiler = registerSound("boiler");
    public static final RegistryObject<SoundEvent> gasRunning = registerSound("gas_running");
    public static final RegistryObject<SoundEvent> gasStarter = registerSound("gas_starter");
    public static final RegistryObject<SoundEvent> gasArc = registerSound("gas_arc");
    public static final RegistryObject<SoundEvent> gasSpark = registerSound("gas_spark");

    public static void init(IEventBus event)
    {
        REGISTER.register(event);
    }

    private static RegistryObject<SoundEvent> registerSound(String name)
    {
        return REGISTER.register(name, () -> SoundEvent.createVariableRangeEvent(ITLib.rl(name)));
    }
}
