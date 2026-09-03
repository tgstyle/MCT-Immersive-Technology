package mctmods.immersivetechnology.core;

import com.immersiveconvergence.api.capability.HeatCapabilities;

import mctmods.immersivetechnology.core.lib.Reference;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = Reference.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.DoubleValue BOILER_DEFAULT_WORKING_HEAT;
    public static final ForgeConfigSpec.DoubleValue SOLAR_TOWER_WORKING_HEAT_LEVEL;
    public static final ForgeConfigSpec.DoubleValue SOLAR_MELTER_WORKING_HEAT_LEVEL;
    public static final ForgeConfigSpec.IntValue CREATIVE_BARREL_OUTPUT_AMOUNT;

    public static double boilerDefaultWorkingHeat = 600.0D;
    public static double solarTowerWorkingHeatLevel = 400.0D;
    public static double solarMelterWorkingHeatLevel = 1000.0D;
    public static int creativeBarrelOutputAmount = Integer.MAX_VALUE;

    static {
        BUILDER.comment("Options shared by all boiler types").push("boiler_shared");
        BOILER_DEFAULT_WORKING_HEAT = BUILDER
                .comment("Target heat level for full operation when a recipe does not specify its own requirement (applies to all boilers). 600 is the lowest accepted value; anything below is reset to 600.")
                .defineInRange("default_working_heat", 600.0D, 600.0D, Double.MAX_VALUE);
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

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    public static void onConfig(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            boilerDefaultWorkingHeat = BOILER_DEFAULT_WORKING_HEAT.get();
            solarTowerWorkingHeatLevel = SOLAR_TOWER_WORKING_HEAT_LEVEL.get();
            solarMelterWorkingHeatLevel = SOLAR_MELTER_WORKING_HEAT_LEVEL.get();
            creativeBarrelOutputAmount = CREATIVE_BARREL_OUTPUT_AMOUNT.get();
        }
    }

    public static double boilerDefaultWorkingHeat() { return Math.min(boilerDefaultWorkingHeat, HeatCapabilities.MAX_HEAT); }
}
