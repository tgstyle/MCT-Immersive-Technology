package mctmods.immersivetechnology.common.data.generators;

import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.Sounds;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

public class SoundDefinitions extends SoundDefinitionsProvider {
    public SoundDefinitions(PackOutput output, ExistingFileHelper helper) { super(output, Reference.MODID, helper); }

    @Override public void registerSounds() {
        add(Sounds.advancedCokeOvenFan.get(), definition().subtitle("subtitles." + Reference.MODID + ".advanced_coke_oven_fan").with(sound(Reference.MODID + ":advanced_coke_oven_fan").volume(1.0f)));
        add(Sounds.advancedCokeOven.get(), definition().subtitle("subtitles." + Reference.MODID + ".advanced_coke_oven").with(sound(Reference.MODID + ":advanced_coke_oven").volume(1.0f)));
        add(Sounds.alternator.get(), definition().subtitle("subtitles." + Reference.MODID + ".alternator").with(sound(Reference.MODID + ":alternator").volume(1.0f)));
        add(Sounds.boiler_liquid.get(), definition().subtitle("subtitles." + Reference.MODID + ".boiler_liquid").with(sound(Reference.MODID + ":boiler_liquid").volume(1.0f)));
        add(Sounds.boiler_solid.get(), definition().subtitle("subtitles." + Reference.MODID + ".boiler_solid").with(sound(Reference.MODID + ":boiler_solid").volume(1.0f)));
        add(Sounds.coolingTower.get(), definition().subtitle("subtitles." + Reference.MODID + ".cooling_tower").with(sound(Reference.MODID + ":cooling_tower").volume(1.0f)));
        add(Sounds.dance.get(), definition().subtitle("subtitles." + Reference.MODID + ".dance").with(sound(Reference.MODID + ":dance").volume(1.0f)));
        add(Sounds.electrolyticCrucibleBattery.get(), definition().subtitle("subtitles." + Reference.MODID + ".electrolytic_crucible_battery").with(sound(Reference.MODID + ":electrolytic_crucible_battery").volume(1.0f)));
        add(Sounds.distiller.get(), definition().subtitle("subtitles." + Reference.MODID + ".distiller").with(sound(Reference.MODID + ":distiller").volume(1.0f)));
        add(Sounds.gasArc.get(), definition().subtitle("subtitles." + Reference.MODID + ".gas_arc").with(sound(Reference.MODID + ":gas_arc").volume(1.0f)));
        add(Sounds.gasIgnite.get(), definition().subtitle("subtitles." + Reference.MODID + ".gas_ignite").with(sound(Reference.MODID + ":gas_ignite").volume(1.0f)));
        add(Sounds.gasRunning.get(), definition().subtitle("subtitles." + Reference.MODID + ".gas_running").with(sound(Reference.MODID + ":gas_running").volume(1.0f)));
        add(Sounds.gasSpark.get(), definition().subtitle("subtitles." + Reference.MODID + ".gas_spark").with(sound(Reference.MODID + ":gas_spark").volume(1.0f)));
        add(Sounds.gasStarter.get(), definition().subtitle("subtitles." + Reference.MODID + ".gas_starter").with(sound(Reference.MODID + ":gas_starter").volume(1.0f)));
        add(Sounds.heatExchanger.get(), definition().subtitle("subtitles." + Reference.MODID + ".heat_exchanger").with(sound(Reference.MODID + ":heat_exchanger").volume(1.0f)));
        add(Sounds.meltingCrucible.get(), definition().subtitle("subtitles." + Reference.MODID + ".melting_crucible").with(sound(Reference.MODID + ":melting_crucible").volume(1.0f)));
        add(Sounds.pilot.get(), definition().subtitle("subtitles." + Reference.MODID + ".pilot").with(sound(Reference.MODID + ":pilot").volume(1.0f)));
        add(Sounds.pressure_release.get(), definition().subtitle("subtitles." + Reference.MODID + ".pressure_release").with(sound(Reference.MODID + ":pressure_release").volume(1.0f)));
        add(Sounds.steamTurbine.get(), definition().subtitle("subtitles." + Reference.MODID + ".steam_turbine").with(sound(Reference.MODID + ":steam_turbine").volume(1.0f)));
        add(Sounds.solarMelter.get(), definition().subtitle("subtitles." + Reference.MODID + ".solar_melter").with(sound(Reference.MODID + ":solar_melter").attenuationDistance(64).volume(1.0f)));
        add(Sounds.solarTower.get(), definition().subtitle("subtitles." + Reference.MODID + ".solar_tower").with(sound(Reference.MODID + ":solar_tower").attenuationDistance(64).volume(1.0f)));
    }
}
