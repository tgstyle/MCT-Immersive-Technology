package mctmods.immersivetechnology;

import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.util.ITLogger;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.core.integration.ITCompatModule;
import mctmods.immersivetechnology.core.proxy.CommonProxy;
import mctmods.immersivetechnology.core.registration.ITCreativeTab;
import mctmods.immersivetechnology.core.util.ITServerHooks;

import com.immersiveconvergence.api.ICIntegration;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;

@Mod(modid = ImmersiveTechnology.MODID, name = ImmersiveTechnology.NAME, acceptedMinecraftVersions = "[1.12.2,1.13)", dependencies = "after:immersiveengineering@[0.12-92,);" + "after:immersivefabrication;" + "required-after:immersiveconvergence@[1.0.183,);" + "required-after:mixinbooter@[10.7,);" + "required-after:forge@[14.23.3.2655,);") public class ImmersiveTechnology {

    public static final String MODID = "immersivetech";
    public static final String NAME = "Immersive Technology";
    @SidedProxy(clientSide = "mctmods.immersivetechnology.core.proxy.ClientProxy" , serverSide = "mctmods.immersivetechnology.core.proxy.CommonProxy") public static CommonProxy proxy;
    @Instance(MODID) public static ImmersiveTechnology instance;

    static {
        FluidRegistry.enableUniversalBucket();
        ITCreativeTab.register();
    }

    @EventHandler public void preInit(FMLPreInitializationEvent event) {
        ITLogger.logger = event.getModLog();
        ICIntegration.refreshConfig(event);
        ITContent.preInit();
        proxy.preInit();
        ITCompatModule.doModulesPreInit();
    }

    @EventHandler public void init(FMLInitializationEvent event) {
        ITContent.init();
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, proxy);
        proxy.preInitEnd();
        proxy.init();
        ITSounds.init();
        ITCompatModule.doModulesInit();
        proxy.initEnd();
    }

    @EventHandler public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit();
        ITCompatModule.doModulesPostInit();
        proxy.postInitEnd();
    }

    @EventHandler public void loadComplete(FMLLoadCompleteEvent event) { ITCompatModule.doModulesLoadComplete(); }

    @EventHandler public void serverStarted(FMLServerStartedEvent event) { }

    @EventHandler public void serverStopping(FMLServerStoppingEvent event) { ITServerHooks.serverStopping(); }
}
