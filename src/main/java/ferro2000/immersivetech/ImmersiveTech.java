package ferro2000.immersivetech;

import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.IEContent;
import ferro2000.immersivetech.common.CommonProxy;
import ferro2000.immersivetech.common.Config.ITConfig;
import ferro2000.immersivetech.common.ITContent;
import ferro2000.immersivetech.common.integration.ITIntegrationModule;
import ferro2000.immersivetech.common.network.TileMessage;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = ImmersiveTech.MODID, name = ImmersiveTech.NAME, version = ImmersiveTech.VERSION, dependencies = "required-after:immersiveengineering;required-after:forge@[14.23.3.2655,);after:jei@[4.7,)")

public class ImmersiveTech {
	
	public static final String MODID = "immersivetech";
	public static final String NAME = "Immersive Tech";
	public static final String VERSION = "@VERSION@";
	
	@SidedProxy(clientSide="ferro2000.immersivetech.client.ClientProxy", serverSide="ferro2000.immersivetech.common.CommonProxy")
	
	public static CommonProxy proxy;
	
	public static final SimpleNetworkWrapper packetHandler = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
	
	@Instance(MODID)
	public static ImmersiveTech instance;
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		Config.preInit(event);
		ITContent.preInit();
		proxy.preInit();
		registerVariables();
		ITIntegrationModule.doModulesPreInit();
		//ITWireType.init();
		
	}
	
	@Mod.EventHandler
	public void init(FMLInitializationEvent event)
	{

		ITContent.init();
		NetworkRegistry.INSTANCE.registerGuiHandler(instance, proxy);
		int messageId = 0;
		packetHandler.registerMessage(TileMessage.HandlerServer.class, TileMessage.class, messageId++, Side.SERVER);
		packetHandler.registerMessage(TileMessage.HandlerClient.class, TileMessage.class, messageId++, Side.CLIENT);
		proxy.preInitEnd();
		proxy.init();
		ITIntegrationModule.doModulesInit();
		proxy.initEnd();
		
	}
	
	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		proxy.postInit();
		ITIntegrationModule.doModulesPostInit();
		proxy.postInitEnd();
	}
	
	@Mod.EventHandler
	public void loadComplete(FMLLoadCompleteEvent event)
	{
		ITIntegrationModule.doModulesLoadComplete();
	}
	
	@Mod.EventHandler
	public void serverStarted(FMLServerStartedEvent event)
	{
	}
	
	public static CreativeTabs creativeTab = new CreativeTabs(MODID)
	{
		@Override
		public ItemStack getTabIconItem()
		{
			return ItemStack.EMPTY;
		}
		@Override
		public ItemStack getIconItemStack()
		{
			return new ItemStack(IEContent.blockMetalDecoration0,1,6);
		}
	};
	
	public void registerVariables() {
		Config.manual_int.put("steamTurbine_timeToMax", (ITConfig.Machines.mechanicalEnergy_maxSpeed / ITConfig.Machines.steamTurbine_speedGainPerTick)/20);
		
		Config.manual_int.put("solarTower_minRange", ITConfig.Machines.solarTower_minRange);
		Config.manual_int.put("solarTower_maxRange", ITConfig.Machines.solarTower_maxRange);

		Config.manual_double.put("boiler_cooldownTime", ((ITConfig.Machines.boiler_workingHeatLevel/ITConfig.Machines.boiler_progressLossInTicks)/20));
		
		Config.manual_int.put("alternator_RfPerTickPerPort", ITConfig.Machines.alternator_RfPerTickPerPort);
		Config.manual_int.put("alternator_energyStorage", ITConfig.Machines.alternator_energyStorage);
		Config.manual_int.put("alternator_energyPerTick", ITConfig.Machines.alternator_RfPerTick);
		
		Config.manual_int.put("cokeOvenPreheater_consumption", ITConfig.Machines.cokeOvenPreheater_consumption);
		
	}

}
