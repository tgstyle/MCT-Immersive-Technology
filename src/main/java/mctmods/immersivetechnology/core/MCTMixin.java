package mctmods.immersivetechnology.core;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.spongepowered.asm.mixin.Mixins;

@Mod(modid = "mct_mixin", name = "MCT Mixin", version = "1.0", acceptedMinecraftVersions = "[1.12.2]")
public class MCTMixin {
    public static final Logger LOGGER = LogManager.getLogger("MCT_Mixin");

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Mixins.addConfiguration("mixins.immersiveengineering.json");
        Mixins.addConfiguration("mixins.immersivetechnology.json");
        Thread.setDefaultUncaughtExceptionHandler((thread, t) -> {
            System.err.println("Uncaught exception in thread " + thread.getName() + ":");
            t.printStackTrace(System.err);
        });
        LOGGER.info("Loaded config: enableAdditionsLogging={}, enablePotentialsLogging={}, enableDevSided={}", MCTMixinConfig.mixinSettings.enableAdditionsLogging, MCTMixinConfig.mixinSettings.enablePotentialsLogging, MCTMixinConfig.mixinSettings.enableDevSided);
    }
}
