package mctmods.immersivetechnology.core.lib;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

@SuppressWarnings("unused")
public class ITLib {
    public static final String MODID = "immersivetechnology";
    public static final String VERSION = "2.0.0";
    public static final String DESC = "desc." + MODID + ".";
    public static final String DESC_INFO = DESC + "info.";
    public static final String DESC_FLAVOUR = DESC + "flavour.";
    public static final String GUIID_AdvCokeOven = "coke_oven_advanced";
    public static final String GUIID_Boiler = "gui_boiler";
    public static final String GUIID_Distiller = "gui_distiller";
    public static final String GUIID_SolarTower = "gui_solar_tower";
    public static final Logger IT_LOGGER = LogUtils.getLogger();

    public static ResourceLocation makeTextureLocation(String name) { return rl("textures/gui/" + name + ".png"); }

    public static ResourceLocation rl(String name) { return ResourceLocation.fromNamespaceAndPath(ITLib.MODID, name); }

    public static float remapRange(float inMin, float inMax, float outMin, float outMax, float value) { return outMin + ((value - inMin) / inMax) * (outMax - outMin); }
}
