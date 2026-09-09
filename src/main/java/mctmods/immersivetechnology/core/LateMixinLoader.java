package mctmods.immersivetechnology.core;

import zone.rong.mixinbooter.Context;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class LateMixinLoader implements ILateMixinLoader {
    private static final String II_CONFIG = "mixins.immersiveintelligence.json";

    @Override public List<String> getMixinConfigs() { return Arrays.asList("mixins.immersiveengineering.json", II_CONFIG); }

    @Override public boolean shouldMixinConfigQueue(Context context) {
        if (II_CONFIG.equals(context.mixinConfig())) { return context.isModPresent("immersiveintelligence"); }
        return context.isModPresent("immersiveengineering");
    }
}
