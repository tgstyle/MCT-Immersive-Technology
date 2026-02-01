package mctmods.immersivetechnology.core;

import net.minecraftforge.common.config.Config;

@Config(modid = "mct_mixin")
public class MCTMixinConfig {
    public static MixinSettings mixinSettings = new MixinSettings();

    public static class MixinSettings {
        @Config.Comment("Enable debug logging for tile entity additions in the World mixin (Only works if enableWorldMixin is true) [Default=false]")
        public boolean enableAdditionsLogging = false;
        @Config.Comment("Enable debug logging for tile entity potentials in the World mixin (Only works if enableWorldMixin is true) [Default=true]")
        public boolean enablePotentialsLogging = true;
        @Config.Comment("Enable the World mixin for tile entity additions (CME Fix) [Default=true]")
        public boolean enableWorldMixin = true;
        @Config.Comment("Replace IE pipes with IT's own version. [Default=true]")
        public boolean replace_IE_pipes = true;
        @Config.Comment("Enable the MinecraftServer mixin to redirect error logging for crash debugging [Default=true]")
        public boolean enableErrorLoggingRedirect = true;
        @Config.Comment("Enable the Chunk mixin to prevent CME during Chunk.onLoad by copying collections before iteration [Default=true]")
        public boolean enableChunkCMEFix = true;
    }
}
