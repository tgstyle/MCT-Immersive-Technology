package mctmods.immersivetechnology.core;

import mctmods.immersivetechnology.core.lib.ITLib;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.fml.event.config.ModConfigEvent;

@EventBusSubscriber(modid = ITLib.MODID)
public class ITCommonConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue BOILER_DEFAULT_WORKING_HEAT;
    public static final ModConfigSpec.DoubleValue SOLAR_TOWER_WORKING_HEAT_LEVEL;
    public static final ModConfigSpec.DoubleValue SOLAR_MELTER_WORKING_HEAT_LEVEL;
    public static final ModConfigSpec.IntValue CREATIVE_BARREL_OUTPUT_AMOUNT;

    public static double boilerDefaultWorkingHeat = 100.0D;
    public static double solarTowerWorkingHeatLevel = 400.0D;
    public static double solarMelterWorkingHeatLevel = 1000.0D;
    public static int creativeBarrelOutputAmount = Integer.MAX_VALUE;

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

        BUILDER.comment("Creative mode tools").push("creative");
        CREATIVE_BARREL_OUTPUT_AMOUNT = BUILDER
                .comment("Output amount per operation for the Creative Barrel in creative mode.")
                .defineInRange("barrel_output_amount", Integer.MAX_VALUE, 1, Integer.MAX_VALUE);
        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    public static void onConfig(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            boilerDefaultWorkingHeat = BOILER_DEFAULT_WORKING_HEAT.get();
            solarTowerWorkingHeatLevel = SOLAR_TOWER_WORKING_HEAT_LEVEL.get();
            solarMelterWorkingHeatLevel = SOLAR_MELTER_WORKING_HEAT_LEVEL.get();
            creativeBarrelOutputAmount = CREATIVE_BARREL_OUTPUT_AMOUNT.get();
        }
    }
}
