package mctmods.immersivetechnology.core;

import com.electronwill.nightconfig.core.Config;
import com.google.common.base.Preconditions;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = ITLib.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ITServerConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    private static Config rawConfig;

    public static Config getRawConfig() { return Preconditions.checkNotNull(rawConfig); }

    @SubscribeEvent
    public static void onConfig(ModConfigEvent ev) { if (SPEC == ev.getConfig().getSpec()) { rawConfig = ev.getConfig().getConfigData(); } }
}
