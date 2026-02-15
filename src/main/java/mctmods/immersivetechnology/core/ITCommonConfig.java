package mctmods.immersivetechnology.core;

import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = ITLib.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ITCommonConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.DoubleValue BOILER_DEFAULT_WORKING_HEAT;
    public static final ForgeConfigSpec.DoubleValue SOLAR_TOWER_WORKING_HEAT_LEVEL;
    public static final ForgeConfigSpec.DoubleValue SOLAR_MELTER_WORKING_HEAT_LEVEL;
    public static final ForgeConfigSpec.IntValue MAX_RPM;

    public static double boilerDefaultWorkingHeat = 100.0D;
    public static double solarTowerWorkingHeatLevel = 400.0D;
    public static double solarMelterWorkingHeatLevel = 1000.0D;
    public static int maxRpm = 7200;

    static {
        BUILDER.comment("Options shared by all boiler types").push("boiler_shared");
        BOILER_DEFAULT_WORKING_HEAT = BUILDER
                .comment("Target heat level for full operation when a recipe does not specify its own requirement (applies to all boilers).")
                .defineInRange("default_working_heat", 100.0D, 0.0D, Double.MAX_VALUE);
        BUILDER.pop();

        BUILDER.comment("Solar Tower settings").push("solar_tower");
        SOLAR_TOWER_WORKING_HEAT_LEVEL = BUILDER.comment("Default target heat for full-speed operation (when recipe unspecified).").defineInRange("working_heat_level", 400.0D, 100.0D, Double.MAX_VALUE);
        BUILDER.pop();

        BUILDER.comment("Solar Melter settings").push("solar_melter");
        SOLAR_MELTER_WORKING_HEAT_LEVEL = BUILDER.comment("Default target heat for full-speed operation (when recipe unspecified).").defineInRange("working_heat_level", 1000.0D, 100.0D, Double.MAX_VALUE);
        BUILDER.pop();

        BUILDER.comment("Mechanical system global settings").push("mechanical");
        MAX_RPM = BUILDER
                .comment("Global maximum rotational speed in RPM for all mechanical devices (turbines, alternators, etc.). Default 7200 RPM. Changing this affects speed_factor calculations in turbines.")
                .defineInRange("max_rpm", 7200, 1000, 50000);
        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    public static void onConfig(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            boilerDefaultWorkingHeat = BOILER_DEFAULT_WORKING_HEAT.get();
            solarTowerWorkingHeatLevel = SOLAR_TOWER_WORKING_HEAT_LEVEL.get();
            solarMelterWorkingHeatLevel = SOLAR_MELTER_WORKING_HEAT_LEVEL.get();
            maxRpm = MAX_RPM.get();
        }
    }
}
