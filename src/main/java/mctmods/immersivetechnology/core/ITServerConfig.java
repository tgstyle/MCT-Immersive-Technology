package mctmods.immersivetechnology.core;

import com.electronwill.nightconfig.core.Config;
import com.google.common.base.Preconditions;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = ITLib.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ITServerConfig
{
    public static final ForgeConfigSpec SPEC;

    static
    {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        SPEC = builder.build();
    }

    private static Config rawConfig;

    public static Config getRawConfig()
    {
        return Preconditions.checkNotNull(rawConfig);
    }

    @SubscribeEvent
    public static void onConfigReload(ModConfigEvent ev)
    {
        if(SPEC==ev.getConfig().getSpec())
        {
            rawConfig = ev.getConfig().getConfigData();
        }
    }
}
