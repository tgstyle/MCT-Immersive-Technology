package mctmods.immersivetechnology.mixin;

import net.minecraft.server.MinecraftServer;

import org.apache.logging.log4j.Logger;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import mctmods.immersivetechnology.core.MCTMixinConfig;

import java.util.Objects;

@Mixin(MinecraftServer.class)
public abstract class MixinMCTMinecraftServer {
    @Unique private static String formatStackTrace$helper(Throwable t) {
        StringBuilder sb = new StringBuilder();
        sb.append(Objects.toString(t.getMessage(), "No message")).append("\n");
        for (StackTraceElement ste : t.getStackTrace()) { sb.append("\tat ").append(ste).append("\n"); }
        Throwable cause = t.getCause();
        if (cause != null) {
            sb.append("Caused by: ").append(formatStackTrace$helper(cause));
        }
        return sb.toString();
    }

    @Redirect(method = "run",
            at = @At(
                    value = "CONSTANT",
                    args = "stringValue=Encountered an unexpected exception",
                    shift = At.Shift.BY,
                    by = 2
            )
    )
    @Unique private void redirectErrorLog$helper(Logger logger, String message, Throwable t) {
        if (MCTMixinConfig.mixinSettings.enableErrorLoggingRedirect) {
            String full = message + ": " + formatStackTrace$helper(t);
            System.err.print(full);
        } else {
            logger.error(message, t);
        }
    }
}
