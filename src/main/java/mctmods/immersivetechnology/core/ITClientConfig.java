package mctmods.immersivetechnology.core;

import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ITLib.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ITClientConfig {
    public static final ForgeConfigSpec SPEC;

    public final static ForgeConfigSpec.DoubleValue multiblockSpecialRenderDistanceModifier;
    public static final ForgeConfigSpec.BooleanValue doSpecialRenderGasTurbine;
    public static final ForgeConfigSpec.BooleanValue doSpecialRenderSteamTurbine;
    public static ForgeConfigSpec.BooleanValue perTickTrashCans;
    public static ForgeConfigSpec.BooleanValue disableReflectorDance;
    public static ForgeConfigSpec.BooleanValue loopReflectorDance;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        multiblockSpecialRenderDistanceModifier = builder.comment("This modifies the distance a special multiblock renderer is visible from Default is 2.5").defineInRange("multiblockSpecialRenderDistanceModifier", 2.5, 0, Double.MAX_VALUE);

        doSpecialRenderGasTurbine = builder.comment("This controls if the animations and special client rendering applies to the Gas Turbine").define("gas_turbine_renderer", true);
        doSpecialRenderSteamTurbine = builder.comment("This controls if the animations and special client rendering applies to the Steam Turbine").define("steam_turbine_renderer", true);

        builder.comment("Solar Reflector options").push("solar_reflector");
        disableReflectorDance = builder.comment("Disable the dance animation and sound for untaken solar reflectors").define("disable_dance", false);
        loopReflectorDance = builder.comment("Loop the dance animation and sound for untaken solar reflectors").define("loop_dance", false);
        builder.pop();

        builder.comment("Experimental options").push("experimental");
        perTickTrashCans = builder.comment("Display trash can OSD as per tick average instead of per second").define("per_tick_trash_cans", false);
        builder.pop();

        SPEC = builder.build();
    }
}
