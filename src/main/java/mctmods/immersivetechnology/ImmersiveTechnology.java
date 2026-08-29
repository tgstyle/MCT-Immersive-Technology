package mctmods.immersivetechnology;

import blusunrize.immersiveengineering.common.Config;
import mctmods.immersivetechnology.common.CommonProxy;
import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.util.ITLogger;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.compat.ITCompatModule;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityFluidPipeAlternative;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

import javax.annotation.Nonnull;

@Mod(modid = ImmersiveTechnology.MODID, name = ImmersiveTechnology.NAME, acceptedMinecraftVersions = "[1.12.2,1.13)", dependencies = "required-after:immersiveengineering@[0.12-92,);" + "required-after:immersiveconvergence@[1.0,);" + "required-after:forge@[14.23.3.2655,);")
public class ImmersiveTechnology {

    public static final String MODID = "immersivetech";
    public static final String NAME = "Immersive Technology";

    @SidedProxy(clientSide = "mctmods.immersivetechnology.client.ClientProxy" , serverSide = "mctmods.immersivetechnology.common.CommonProxy")
    public static CommonProxy proxy;
    public static final SimpleNetworkWrapper packetHandler = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);

    @Instance(MODID) public static ImmersiveTechnology instance;

    static { FluidRegistry.enableUniversalBucket(); }

    @EventHandler public void preInit(FMLPreInitializationEvent event) {
        ITLogger.logger = event.getModLog();
        Config.preInit(event);
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

    @EventHandler public void serverStopping(FMLServerStoppingEvent event) { TileEntityFluidPipeAlternative.indirectConnections.clear(); }

    public static CreativeTabs creativeTab = new CreativeTabs(MODID) {
        @Override @Nonnull public ItemStack createIcon() { return new ItemStack(ITContent.blockValve, 1, 0); }
    };
}
