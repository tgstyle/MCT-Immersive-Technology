package mctmods.immersivetechnology.core.lib;

import blusunrize.immersiveengineering.api.Lib;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("all")
public class ResourceUtils
{
    public static ResourceLocation it(String path)
    {
        return new ResourceLocation(ITLib.MODID, path);
    }

    public static ResourceLocation ie(String path){
        return new ResourceLocation(Lib.MODID, path);
    }
}
