package mctmods.immersivetechnology.core;

import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Config.Type;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = "mct_mixin", name = "MCT Mixin", version = "1.0", acceptedMinecraftVersions = "[1.12.2]")
public class MCTMixin {
    public static final Logger LOGGER = LogManager.getLogger("MCT_Mixin");

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ConfigManager.sync("mct_mixin", Type.INSTANCE);

        Thread.setDefaultUncaughtExceptionHandler((thread, t) -> {
            System.err.println("Uncaught exception in thread " + thread.getName() + ":");
            t.printStackTrace(System.err);
        });

        LOGGER.info("Loaded config: enableAdditionsLogging={}, enablePotentialsLogging={}, enableDevSided={}, enableWorldMixin={}, replace_IE_pipes={}, enableErrorLoggingRedirect={}",
                MCTMixinConfig.mixinSettings.enableAdditionsLogging,
                MCTMixinConfig.mixinSettings.enablePotentialsLogging,
                MCTMixinConfig.mixinSettings.enableDevSided,
                MCTMixinConfig.mixinSettings.enableWorldMixin,
                MCTMixinConfig.mixinSettings.replace_IE_pipes,
                MCTMixinConfig.mixinSettings.enableErrorLoggingRedirect);
    }
}
