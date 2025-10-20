package mctmods.immersivetechnology.core;

import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = ITLib.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ITCommonConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.IntValue CONFIG_BURN_TIME_DIVIDER;

    public static int burnTimeDivider;

    static {
        BUILDER.comment("Solid Boiler options").push("boiler_solid");
        CONFIG_BURN_TIME_DIVIDER = BUILDER.comment("Divider for burn time in solid boiler (Default: 10)").defineInRange("burnTimeDivider", 10, 1, Integer.MAX_VALUE);
        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    static void onConfig(final ModConfigEvent event) { if (event.getConfig().getSpec() == SPEC) { burnTimeDivider = CONFIG_BURN_TIME_DIVIDER.get(); } }
}
