package mctmods.immersivetechnology.core;

import net.minecraftforge.common.config.Config;

@Config(modid = "mct_mixin")
public class MCTMixinConfig {
    public static MixinSettings mixinSettings = new MixinSettings();

    public static class MixinSettings {

        @Config.Comment("Replace IE pipes with IT's own version. [Default=true]")
        public boolean replace_IE_pipes = true;

        @Config.Comment("Replace IE conveyors with IT's optimized version. [Default=true]")
        public boolean replace_IE_conveyors = true;

        @Config.Comment("Fix IE wire rendering for vertical/near-vertical wires and chunk-border cases [Default=true]")
        public boolean fix_IE_wires = true;
    }
}
