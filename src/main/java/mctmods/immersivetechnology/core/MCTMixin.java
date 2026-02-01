package mctmods.immersivetechnology.core;

import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Config.Type;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import zone.rong.mixinbooter.IEarlyMixinLoader;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Mod(modid = "mct_mixin", name = "MCT Mixin", version = "1.0", acceptedMinecraftVersions = "[1.12.2]")
@IFMLLoadingPlugin.Name("MCTMixin")
@IFMLLoadingPlugin.SortingIndex(1001)
public class MCTMixin implements IFMLLoadingPlugin, IEarlyMixinLoader {
    public static final Logger LOGGER = LogManager.getLogger("MCT_Mixin");

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("Entering preInit - syncing config now");
        ConfigManager.sync("mct_mixin", Type.INSTANCE);

        Thread.setDefaultUncaughtExceptionHandler((thread, t) -> {
            System.err.println("Uncaught exception in thread " + thread.getName() + ":");
            t.printStackTrace(System.err);
        });

        LOGGER.info("Loaded config: enableAdditionsLogging={}, enablePotentialsLogging={}, enableWorldMixin={}, replace_IE_pipes={}, enableErrorLoggingRedirect={}",
                MCTMixinConfig.mixinSettings.enableAdditionsLogging,
                MCTMixinConfig.mixinSettings.enablePotentialsLogging,
                MCTMixinConfig.mixinSettings.enableWorldMixin,
                MCTMixinConfig.mixinSettings.replace_IE_pipes,
                MCTMixinConfig.mixinSettings.enableErrorLoggingRedirect);
    }

    @Override public String[] getASMTransformerClass() { return new String[0]; }

    @Override public String getModContainerClass() { return null; }

    @Override public String getSetupClass() { return null; }

    @Override public void injectData(Map<String, Object> data) {}

    @Override public String getAccessTransformerClass() { return null; }

    @Override public List<String> getMixinConfigs() { return Collections.singletonList("mixins.immersivetechnology.json"); }
}
