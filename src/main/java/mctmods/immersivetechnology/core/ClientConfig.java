package mctmods.immersivetechnology.core;

import mctmods.immersivetechnology.core.lib.Reference;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = Reference.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.DoubleValue CONFIG_MULTIBLOCK_SPECIAL_RENDER_DISTANCE_MODIFIER;
    public static final ForgeConfigSpec.BooleanValue CONFIG_DO_SPECIAL_RENDER_GAS_TURBINE;
    public static final ForgeConfigSpec.BooleanValue CONFIG_DO_SPECIAL_RENDER_STEAM_TURBINE;
    public static final ForgeConfigSpec.BooleanValue CONFIG_DO_SPECIAL_RENDER_SOLAR_REFLECTOR;
    public static final ForgeConfigSpec.BooleanValue CONFIG_DISABLE_REFLECTOR_DANCE;
    public static final ForgeConfigSpec.BooleanValue CONFIG_LOOP_REFLECTOR_DANCE;
    public static final ForgeConfigSpec.DoubleValue CONFIG_SOLAR_REFLECTOR_BASE_FREQUENCY;
    public static final ForgeConfigSpec.DoubleValue CONFIG_SOLAR_REFLECTOR_DANCE_DURATION;
    public static final ForgeConfigSpec.BooleanValue CONFIG_PER_TICK_TRASH_CANS;
    public static final ForgeConfigSpec.BooleanValue CONFIG_DISABLE_FANCY_TESR;
    public static final ForgeConfigSpec.BooleanValue CONFIG_PARTICLE_COLLIDE;
    public static final ForgeConfigSpec.DoubleValue CONFIG_COLORED_SMOKE_HEIGHT;
    public static final ForgeConfigSpec.DoubleValue CONFIG_CUSTOM_SMOKE_HEIGHT;
    public static final ForgeConfigSpec.DoubleValue ADVANCED_COKE_OVEN_BASEHEATER_MAX_FAN_SPEED;
    public static final ForgeConfigSpec.DoubleValue ADVANCED_COKE_OVEN_BASEHEATER_FAN_ACCEL;
    public static final ForgeConfigSpec.DoubleValue ADVANCED_COKE_OVEN_BASEHEATER_FAN_DECEL;

    public static double multiblockSpecialRenderDistanceModifier;
    public static boolean doSpecialRenderGasTurbine;
    public static boolean doSpecialRenderSteamTurbine;
    public static boolean doSpecialRenderSolarReflector;
    public static boolean disableReflectorDance;
    public static boolean loopReflectorDance;
    public static double solarReflectorBaseFrequency;
    public static double solarReflectorDanceDuration;
    public static boolean perTickTrashCans;
    public static boolean disableFancyTESR;
    public static boolean particleCollide;
    public static double coloredSmokeHeight;
    public static double customSmokeHeight;
    public static double advancedCokeOvenBaseheaterMaxFanSpeed;
    public static double advancedCokeOvenBaseheaterFanAccel;
    public static double advancedCokeOvenBaseheaterFanDecel;

    static {
        BUILDER.comment("Render options").push("render");
        CONFIG_MULTIBLOCK_SPECIAL_RENDER_DISTANCE_MODIFIER = BUILDER.comment("This modifies the distance a special multiblock renderer is visible from (Default: 2.5).").defineInRange("multiblockSpecialRenderDistanceModifier", 2.5, 0, Double.MAX_VALUE);
        CONFIG_DO_SPECIAL_RENDER_GAS_TURBINE = BUILDER.comment("This controls if the animations and special client rendering applies to the Gas Turbine (Default: true).").define("gas_turbine_renderer", true);
        CONFIG_DO_SPECIAL_RENDER_STEAM_TURBINE = BUILDER.comment("This controls if the animations and special client rendering applies to the Steam Turbine (Default: true).").define("steam_turbine_renderer", true);
        CONFIG_DO_SPECIAL_RENDER_SOLAR_REFLECTOR = BUILDER.comment("This controls if the animations and special client rendering applies to the Solar Reflector (Default: true).").define("solar_reflector_renderer", true);
        CONFIG_DISABLE_FANCY_TESR = BUILDER.comment("Disables most lighting code for certain models that are rendered dynamically (TESR). May improve FPS. Affects various multiblocks.").define("disableFancyTESR", false);
        BUILDER.pop();
        BUILDER.comment("Solar Reflector options").push("solar_reflector");
        CONFIG_DISABLE_REFLECTOR_DANCE = BUILDER.comment("Disable the dance animation and sound for untaken solar reflectors (Default: false).").define("disable_dance", false);
        CONFIG_LOOP_REFLECTOR_DANCE = BUILDER.comment("Loop the dance animation and sound for untaken solar reflectors (Default: false).").define("loop_dance", false);
        CONFIG_SOLAR_REFLECTOR_BASE_FREQUENCY = BUILDER.comment("Base frequency for the sine-wave based 'dance' animation (higher = faster movement).").defineInRange("base_frequency", 2.09D, 0.1D, 10.0D);
        CONFIG_SOLAR_REFLECTOR_DANCE_DURATION = BUILDER.comment("Full cycle duration of the dance animation in seconds.").defineInRange("dance_duration", 63.0D, 10.0D, 300.0D);
        BUILDER.pop();
        BUILDER.comment("Experimental options").push("experimental");
        CONFIG_PER_TICK_TRASH_CANS = BUILDER.comment("Display trash can OSD as per tick average instead of per second (Default: false).").define("per_tick_trash_cans", false);
        BUILDER.pop();
        BUILDER.comment("Particle options").push("particles");
        CONFIG_PARTICLE_COLLIDE = BUILDER.comment("Enable particle collisions with blocks (Default: false).").define("collide", false);
        CONFIG_COLORED_SMOKE_HEIGHT = BUILDER.comment("Height scale for colored smoke particles (Default: 0.7).").defineInRange("colored_smoke_height", 0.7, 0.0, 3.0);
        CONFIG_CUSTOM_SMOKE_HEIGHT = BUILDER.comment("Height scale for custom smoke particles (Default: 1.0).").defineInRange("custom_smoke_height", 1.0, 0.0, 3.0);
        BUILDER.pop();
        BUILDER.comment("Advanced Coke Oven Base Heater options").push("advanced_coke_oven_baseheater");
        ADVANCED_COKE_OVEN_BASEHEATER_MAX_FAN_SPEED = BUILDER.comment("Maximum fan speed for the base heater animation.").defineInRange("max_fan_speed", 35.0D, 1.0D, 100.0D);
        ADVANCED_COKE_OVEN_BASEHEATER_FAN_ACCEL = BUILDER.comment("Fan acceleration rate for the base heater.").defineInRange("fan_accel", 5.0D, 0.1D, 20.0D);
        ADVANCED_COKE_OVEN_BASEHEATER_FAN_DECEL = BUILDER.comment("Fan deceleration rate for the base heater.").defineInRange("fan_decel", 1.0D, 0.1D, 10.0D);
        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent static void onConfig(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            multiblockSpecialRenderDistanceModifier = CONFIG_MULTIBLOCK_SPECIAL_RENDER_DISTANCE_MODIFIER.get();
            doSpecialRenderGasTurbine = CONFIG_DO_SPECIAL_RENDER_GAS_TURBINE.get();
            doSpecialRenderSteamTurbine = CONFIG_DO_SPECIAL_RENDER_STEAM_TURBINE.get();
            doSpecialRenderSolarReflector = CONFIG_DO_SPECIAL_RENDER_SOLAR_REFLECTOR.get();
            disableReflectorDance = CONFIG_DISABLE_REFLECTOR_DANCE.get();
            loopReflectorDance = CONFIG_LOOP_REFLECTOR_DANCE.get();
            solarReflectorBaseFrequency = CONFIG_SOLAR_REFLECTOR_BASE_FREQUENCY.get();
            solarReflectorDanceDuration = CONFIG_SOLAR_REFLECTOR_DANCE_DURATION.get();
            perTickTrashCans = CONFIG_PER_TICK_TRASH_CANS.get();
            disableFancyTESR = CONFIG_DISABLE_FANCY_TESR.get();
            particleCollide = CONFIG_PARTICLE_COLLIDE.get();
            coloredSmokeHeight = CONFIG_COLORED_SMOKE_HEIGHT.get();
            customSmokeHeight = CONFIG_CUSTOM_SMOKE_HEIGHT.get();
            advancedCokeOvenBaseheaterMaxFanSpeed = ADVANCED_COKE_OVEN_BASEHEATER_MAX_FAN_SPEED.get();
            advancedCokeOvenBaseheaterFanAccel = ADVANCED_COKE_OVEN_BASEHEATER_FAN_ACCEL.get();
            advancedCokeOvenBaseheaterFanDecel = ADVANCED_COKE_OVEN_BASEHEATER_FAN_DECEL.get();
        }
    }
}
