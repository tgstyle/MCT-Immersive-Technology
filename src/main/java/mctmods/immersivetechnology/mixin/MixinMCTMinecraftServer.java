package mctmods.immersivetechnology.mixin;

import net.minecraft.server.MinecraftServer;

import org.apache.logging.log4j.Logger;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import mctmods.immersivetechnology.core.MCTMixinConfig;

import java.io.PrintWriter;
import java.io.StringWriter;

@Mixin(MinecraftServer.class)
public abstract class MixinMCTMinecraftServer {
    @Redirect(method = "run", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;error(Ljava/lang/String;Ljava/lang/Throwable;)V", remap = false), remap = false)
    private void redirectErrorLog(Logger logger, String message, Throwable t) {
        if (MCTMixinConfig.mixinSettings.enableErrorLoggingRedirect) {
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            logger.error("{}: {}\n{}", message, t.getMessage(), sw.toString());
        } else {
            logger.error(message, t);
        }
    }
}
