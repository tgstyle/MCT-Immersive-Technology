package mctmods.immersivetechnology.mixin;

import mctmods.immersivetechnology.core.MCTMixinConfig;

import net.minecraft.server.MinecraftServer;

import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Objects;

@Mixin(value = MinecraftServer.class, priority = 1500)
public abstract class MixinMCTMinecraftServer {
    @Unique private static String formatStackTrace$helper(Throwable t) {
        StringBuilder sb = new StringBuilder();
        sb.append(Objects.toString(t.getMessage(), "No message")).append("\n");
        for (StackTraceElement ste : t.getStackTrace()) { sb.append("\tat ").append(ste).append("\n"); }
        Throwable cause = t.getCause();
        if (cause != null) { sb.append("Caused by: ").append(formatStackTrace$helper(cause)); }
        return sb.toString();
    }

    @Redirect(method = "run", remap = false,
            at = @At(value = "INVOKE",
                    target = "Lorg/apache/logging/log4j/Logger;error(Ljava/lang/String;Ljava/lang/Throwable;)V",
                    ordinal = 0))
    private void redirectErrorLog$helper(Logger logger, String message, Throwable t) {
        logger.error(message, t);
        if (MCTMixinConfig.mixinSettings.enableErrorLoggingRedirect) {
            String full = message + ": " + formatStackTrace$helper(t);
            System.err.print(full);
        }
    }
}
