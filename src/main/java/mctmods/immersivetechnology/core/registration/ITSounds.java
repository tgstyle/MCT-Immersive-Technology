package mctmods.immersivetechnology.core.registration;

import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ITSounds {
    private static final DeferredRegister<SoundEvent> REGISTER = DeferredRegister.create(Registries.SOUND_EVENT, ITLib.MODID);

    public static final RegistryObject<SoundEvent> advancedCokeOven = registerSound("advanced_coke_oven");
    public static final RegistryObject<SoundEvent> alternator = registerSound("alternator");
    public static final RegistryObject<SoundEvent> boiler = registerSound("boiler");
    public static final RegistryObject<SoundEvent> coolingTower = registerSound("cooling_tower");
    public static final RegistryObject<SoundEvent> dance = registerSound("dance");
    public static final RegistryObject<SoundEvent> distiller = registerSound("distiller");
    public static final RegistryObject<SoundEvent> electrolyticCrucibleBattery = registerSound("electrolytic_crucible_battery");
    public static final RegistryObject<SoundEvent> gasArc = registerSound("gas_arc");
    public static final RegistryObject<SoundEvent> gasIgnite = registerSound("gas_ignite");
    public static final RegistryObject<SoundEvent> gasRunning = registerSound("gas_running");
    public static final RegistryObject<SoundEvent> gasSpark = registerSound("gas_spark");
    public static final RegistryObject<SoundEvent> gasStarter = registerSound("gas_starter");
    public static final RegistryObject<SoundEvent> heatExchanger = registerSound("heat_exchanger");
    public static final RegistryObject<SoundEvent> meltingCrucible = registerSound("melting_crucible");
    public static final RegistryObject<SoundEvent> steamTurbine = registerSound("steam_turbine");
    public static final RegistryObject<SoundEvent> solarMelter = registerSound("solar_melter");
    public static final RegistryObject<SoundEvent> solarTower = registerSound("solar_tower");

    public static void init(IEventBus event) {
        REGISTER.register(event);
    }

    private static RegistryObject<SoundEvent> registerSound(String name) {
        return REGISTER.register(name, () -> SoundEvent.createVariableRangeEvent(ITLib.rl(name)));
    }
}
