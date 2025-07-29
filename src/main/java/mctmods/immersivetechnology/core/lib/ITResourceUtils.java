package mctmods.immersivetechnology.core.lib;

import blusunrize.immersiveengineering.api.Lib;
import net.minecraft.resources.ResourceLocation;

public class ITResourceUtils {
    public static ResourceLocation it(String path) { return ResourceLocation.fromNamespaceAndPath(ITLib.MODID, path); }

    public static ResourceLocation ie(String path) { return ResourceLocation.fromNamespaceAndPath(Lib.MODID, path); }
}
