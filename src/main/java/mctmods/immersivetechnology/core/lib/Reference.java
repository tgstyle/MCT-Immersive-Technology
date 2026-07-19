package mctmods.immersivetechnology.core.lib;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import org.slf4j.Logger;

@SuppressWarnings("unused")
public class Reference {
    public static final String MODID = "immersivetechnology";
    public static final String VERSION = "3.0.0";
    public static final String DESC = "desc." + MODID + ".";
    public static final String DESC_INFO = DESC + "info.";
    public static final String DESC_FLAVOUR = DESC + "flavour.";
    public static final Logger IT_LOGGER = LogUtils.getLogger();

    public static IEventBus MOD_BUS;
    public static ResourceLocation makeTextureLocation(String name) { return rl("textures/gui/" + name + ".png"); }
    public static ResourceLocation makeTextureLocation(String namespace, String name) { return rl(namespace,"textures/gui/" + name + ".png"); }
    public static ResourceLocation rl(String name) { return ResourceLocation.fromNamespaceAndPath(Reference.MODID, name); }
    public static ResourceLocation rl(String namespace, String name) { return ResourceLocation.fromNamespaceAndPath(namespace, name); }
    public static float remapRange(float inMin, float inMax, float outMin, float outMax, float value) { return outMin + ((value - inMin) / inMax) * (outMax - outMin); }
}
