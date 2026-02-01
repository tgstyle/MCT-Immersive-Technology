package mctmods.immersivetechnology.core;

import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public class LateMixinLoader implements ILateMixinLoader {
    @Override public List<String> getMixinConfigs() { return Collections.singletonList("mixins.immersiveengineering.json"); }
}
