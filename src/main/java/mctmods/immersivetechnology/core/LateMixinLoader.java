package mctmods.immersivetechnology.core;

import zone.rong.mixinbooter.Context;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public class LateMixinLoader implements ILateMixinLoader {
    private static final String II_CONFIG = "mixins.immersiveintelligence.json";

    @Override public List<String> getMixinConfigs() { return Collections.singletonList(II_CONFIG); }

    @Override public boolean shouldMixinConfigQueue(Context context) { return context.isModPresent("immersiveintelligence"); }
}
