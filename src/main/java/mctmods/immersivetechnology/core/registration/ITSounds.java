package mctmods.immersivetechnology.core.registration;

import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.bus.api.IEventBus;

public class ITSounds {
    private static final DeferredRegister<SoundEvent> REGISTER = DeferredRegister.create(Registries.SOUND_EVENT, ITLib.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> advancedCokeOvenFan = registerSound("advanced_coke_oven_fan");
    public static final DeferredHolder<SoundEvent, SoundEvent> advancedCokeOven = registerSound("advanced_coke_oven");
    public static final DeferredHolder<SoundEvent, SoundEvent> alternator = registerSound("alternator");
    public static final DeferredHolder<SoundEvent, SoundEvent> boiler_liquid = registerSound("boiler_liquid");
    public static final DeferredHolder<SoundEvent, SoundEvent> boiler_solid = registerSound("boiler_solid");
    public static final DeferredHolder<SoundEvent, SoundEvent> coolingTower = registerSound("cooling_tower");
    public static final DeferredHolder<SoundEvent, SoundEvent> dance = registerSound("dance");
    public static final DeferredHolder<SoundEvent, SoundEvent> distiller = registerSound("distiller");
    public static final DeferredHolder<SoundEvent, SoundEvent> electrolyticCrucibleBattery = registerSound("electrolytic_crucible_battery");
    public static final DeferredHolder<SoundEvent, SoundEvent> gasArc = registerSound("gas_arc");
    public static final DeferredHolder<SoundEvent, SoundEvent> gasIgnite = registerSound("gas_ignite");
    public static final DeferredHolder<SoundEvent, SoundEvent> gasRunning = registerSound("gas_running");
    public static final DeferredHolder<SoundEvent, SoundEvent> gasSpark = registerSound("gas_spark");
    public static final DeferredHolder<SoundEvent, SoundEvent> gasStarter = registerSound("gas_starter");
    public static final DeferredHolder<SoundEvent, SoundEvent> heatExchanger = registerSound("heat_exchanger");
    public static final DeferredHolder<SoundEvent, SoundEvent> meltingCrucible = registerSound("melting_crucible");
    public static final DeferredHolder<SoundEvent, SoundEvent> pilot = registerSound("pilot");
    public static final DeferredHolder<SoundEvent, SoundEvent> pressure_release = registerSound("pressure_release");
    public static final DeferredHolder<SoundEvent, SoundEvent> steamTurbine = registerSound("steam_turbine");
    public static final DeferredHolder<SoundEvent, SoundEvent> solarMelter = registerSound("solar_melter");
    public static final DeferredHolder<SoundEvent, SoundEvent> solarTower = registerSound("solar_tower");

    public static void init(IEventBus event) { REGISTER.register(event); }

    private static DeferredHolder<SoundEvent, SoundEvent> registerSound(String name) {
        return REGISTER.register(name, () -> SoundEvent.createVariableRangeEvent(ITLib.rl(name)));
    }
}
