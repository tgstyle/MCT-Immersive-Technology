package ferro2000.immersivetech;

import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.IEContent;

import ferro2000.immersivetech.common.CommonProxy;
import ferro2000.immersivetech.common.ITContent;
import ferro2000.immersivetech.common.util.ITLogger;
import ferro2000.immersivetech.common.util.ITSounds;
import ferro2000.immersivetech.common.util.compat.ITCompatModule;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

@Mod(
	modid = ImmersiveTech.MODID,
	name = ImmersiveTech.NAME,
	version = ImmersiveTech.VERSION,
	acceptedMinecraftVersions = "[1.12.2,1.13)",	
	dependencies = 
			"required-after:immersiveengineering;" +
			"required-after:forge@[14.23.3.2655,);")


public class ImmersiveTech {

	public static final String MODID = "immersivetech";
	public static final String NAME = "Immersive Technology";
	public static final String VERSION = "${version}";

	@SidedProxy(clientSide = "ferro2000.immersivetech.client.ClientProxy" , serverSide = "ferro2000.immersivetech.common.CommonProxy")
	public static CommonProxy proxy;
	public static final SimpleNetworkWrapper packetHandler = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);

	@Instance(MODID)
	public static ImmersiveTech instance;

	static {
		FluidRegistry.enableUniversalBucket();
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		ITLogger.logger = event.getModLog();
		Config.preInit(event);
		ITContent.preInit();
		proxy.preInit();
		ITCompatModule.doModulesPreInit();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		ITContent.init();
		NetworkRegistry.INSTANCE.registerGuiHandler(instance, proxy);
		proxy.preInitEnd();
		proxy.init();
		ITSounds.init();
		ITCompatModule.doModulesInit();
		proxy.initEnd();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		proxy.postInit();
		ITCompatModule.doModulesPostInit();
		proxy.postInitEnd();
	}

	@EventHandler
	public void loadComplete(FMLLoadCompleteEvent event) {
		ITCompatModule.doModulesLoadComplete();
	}

	@EventHandler
	public void serverStarted(FMLServerStartedEvent event) {
	}

	public static CreativeTabs creativeTab = new CreativeTabs(MODID) {
		@Override
		public ItemStack getTabIconItem() {
			return ItemStack.EMPTY;
		}
		@Override
		public ItemStack getIconItemStack() {
			return new ItemStack(IEContent.blockMetalDecoration0, 1, 6);
		}
	};

}