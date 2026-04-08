package mctmods.immersivetechnology.core;

import zone.rong.mixinbooter.IEarlyMixinLoader;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public class EarlyMixinLoader implements IEarlyMixinLoader {
    @Override public List<String> getMixinConfigs() { return Collections.singletonList("mixins.immersivetechnology.json"); }
}
