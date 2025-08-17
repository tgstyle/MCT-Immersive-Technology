package mctmods.immersivetechnology.common.data.generators;

import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITSounds;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SoundDefinitionsProvider;

public class ITSoundProvider extends SoundDefinitionsProvider {
    public ITSoundProvider(PackOutput output, ExistingFileHelper helper) { super(output, ITLib.MODID, helper); }

    @Override
    public void registerSounds() {
        add(ITSounds.advancedCokeOven.get(), definition().subtitle("subtitles." + ITLib.MODID + ".advanced_coke_oven").with(sound(ITLib.MODID + ":advanced_coke_oven").volume(1.0f)));
        add(ITSounds.alternator.get(), definition().subtitle("subtitles." + ITLib.MODID + ".alternator").with(sound(ITLib.MODID + ":alternator").volume(1.0f)));
        add(ITSounds.boiler.get(), definition().subtitle("subtitles." + ITLib.MODID + ".boiler").with(sound(ITLib.MODID + ":boiler").volume(1.0f)));
        add(ITSounds.coolingTower.get(), definition().subtitle("subtitles." + ITLib.MODID + ".cooling_tower").with(sound(ITLib.MODID + ":cooling_tower").volume(1.0f)));
        add(ITSounds.dance.get(), definition().subtitle("subtitles." + ITLib.MODID + ".dance").with(sound(ITLib.MODID + ":dance").volume(1.0f)));
        add(ITSounds.distiller.get(), definition().subtitle("subtitles." + ITLib.MODID + ".distiller").with(sound(ITLib.MODID + ":distiller").volume(1.0f)));
        add(ITSounds.gasArc.get(), definition().subtitle("subtitles." + ITLib.MODID + ".gas_arc").with(sound(ITLib.MODID + ":gas_arc").volume(1.0f)));
        add(ITSounds.gasIgnite.get(), definition().subtitle("subtitles." + ITLib.MODID + ".gas_ignite").with(sound(ITLib.MODID + ":gas_ignite").volume(1.0f)));
        add(ITSounds.gasRunning.get(), definition().subtitle("subtitles." + ITLib.MODID + ".gas_running").with(sound(ITLib.MODID + ":gas_running").volume(1.0f)));
        add(ITSounds.gasSpark.get(), definition().subtitle("subtitles." + ITLib.MODID + ".gas_spark").with(sound(ITLib.MODID + ":gas_spark").volume(1.0f)));
        add(ITSounds.gasStarter.get(), definition().subtitle("subtitles." + ITLib.MODID + ".gas_starter").with(sound(ITLib.MODID + ":gas_starter").volume(1.0f)));
        add(ITSounds.heatExchanger.get(), definition().subtitle("subtitles." + ITLib.MODID + ".heat_exchanger").with(sound(ITLib.MODID + ":heat_exchanger").volume(1.0f)));
        add(ITSounds.steamTurbine.get(), definition().subtitle("subtitles." + ITLib.MODID + ".steam_turbine").with(sound(ITLib.MODID + ":steam_turbine").volume(1.0f)));
        add(ITSounds.solarTower.get(), definition().subtitle("subtitles." + ITLib.MODID + ".solar_tower").with(sound(ITLib.MODID + ":solar_tower").attenuationDistance(64).volume(1.0f)));
    }
}
