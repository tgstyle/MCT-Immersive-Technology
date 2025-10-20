package mctmods.immersivetechnology.core;

import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = ITLib.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ITClientConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.DoubleValue CONFIG_MULTIBLOCK_SPECIAL_RENDER_DISTANCE_MODIFIER;
    public static final ForgeConfigSpec.BooleanValue CONFIG_DO_SPECIAL_RENDER_GAS_TURBINE;
    public static final ForgeConfigSpec.BooleanValue CONFIG_DO_SPECIAL_RENDER_STEAM_TURBINE;
    public static final ForgeConfigSpec.BooleanValue CONFIG_DISABLE_REFLECTOR_DANCE;
    public static final ForgeConfigSpec.BooleanValue CONFIG_LOOP_REFLECTOR_DANCE;
    public static final ForgeConfigSpec.BooleanValue CONFIG_PER_TICK_TRASH_CANS;

    public static double multiblockSpecialRenderDistanceModifier;
    public static boolean doSpecialRenderGasTurbine;
    public static boolean doSpecialRenderSteamTurbine;
    public static boolean disableReflectorDance;
    public static boolean loopReflectorDance;
    public static boolean perTickTrashCans;

    static {
        BUILDER.comment("Render options").push("render");
        CONFIG_MULTIBLOCK_SPECIAL_RENDER_DISTANCE_MODIFIER = BUILDER.comment("This modifies the distance a special multiblock renderer is visible from (Default: 2.5)").defineInRange("multiblockSpecialRenderDistanceModifier", 2.5, 0, Double.MAX_VALUE);
        CONFIG_DO_SPECIAL_RENDER_GAS_TURBINE = BUILDER.comment("This controls if the animations and special client rendering applies to the Gas Turbine (Default: true)").define("gas_turbine_renderer", true);
        CONFIG_DO_SPECIAL_RENDER_STEAM_TURBINE = BUILDER.comment("This controls if the animations and special client rendering applies to the Steam Turbine (Default: true)").define("steam_turbine_renderer", true);
        BUILDER.pop();
        BUILDER.comment("Solar Reflector options").push("solar_reflector");
        CONFIG_DISABLE_REFLECTOR_DANCE = BUILDER.comment("Disable the dance animation and sound for untaken solar reflectors (Default: false").define("disable_dance", false);
        CONFIG_LOOP_REFLECTOR_DANCE = BUILDER.comment("Loop the dance animation and sound for untaken solar reflectors (Default: false)").define("loop_dance", false);
        BUILDER.pop();
        BUILDER.comment("Experimental options").push("experimental");
        CONFIG_PER_TICK_TRASH_CANS = BUILDER.comment("Display trash can OSD as per tick average instead of per second (Default: false)").define("per_tick_trash_cans", false);
        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    static void onConfig(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            multiblockSpecialRenderDistanceModifier = CONFIG_MULTIBLOCK_SPECIAL_RENDER_DISTANCE_MODIFIER.get();
            doSpecialRenderGasTurbine = CONFIG_DO_SPECIAL_RENDER_GAS_TURBINE.get();
            doSpecialRenderSteamTurbine = CONFIG_DO_SPECIAL_RENDER_STEAM_TURBINE.get();
            disableReflectorDance = CONFIG_DISABLE_REFLECTOR_DANCE.get();
            loopReflectorDance = CONFIG_LOOP_REFLECTOR_DANCE.get();
            perTickTrashCans = CONFIG_PER_TICK_TRASH_CANS.get();
        }
    }
}
