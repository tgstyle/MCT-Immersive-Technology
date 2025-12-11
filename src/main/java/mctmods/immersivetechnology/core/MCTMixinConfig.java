package mctmods.immersivetechnology.core;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Config.Type;

@Config(modid = "mct_mixin")
public class MCTMixinConfig {
    public static MixinSettings mixinSettings = new MixinSettings();

    static {
        ConfigManager.sync("mct_mixin", Type.INSTANCE);
    }

    public static class MixinSettings {
        @Config.Comment({"Enable debug logging for tile entity additions in the World mixin (Only works if enableWorldMixin is true) [Default=false]"})
        public boolean enableAdditionsLogging = false;
        @Config.Comment({"Enable debug logging for tile entity potentials in the World mixin (Only works if enableWorldMixin is true) [Default=true]"})
        public boolean enablePotentialsLogging = true;
        @Config.Comment({"Enable dev environment mixin sided fix [Default=false]"})
        public boolean enableDevSided = false;
        @Config.Comment({"Enable the World mixin for tile entity additions (CME Fix) [Default=true]"})
        public boolean enableWorldMixin = true;
        @Config.Comment({"Replace IE pipes with IT's own version. [Default=true]"})
        public boolean replace_IE_pipes = true;
        @Config.Comment({"Enable the MinecraftServer mixin to redirect error logging for crash debugging [Default=true]"})
        public boolean enableErrorLoggingRedirect = true;
    }
}
