package ferro2000.immersivetech;

import blusunrize.immersiveengineering.common.IEContent;
import ferro2000.immersivetech.common.CommonProxy;
import ferro2000.immersivetech.common.ITContent;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;

@Mod(modid = ImmersiveTech.MODID, name = ImmersiveTech.NAME, version = ImmersiveTech.VERSION, dependencies = "required-after:immersiveengineering;")

public class ImmersiveTech {
	
	public static final String MODID = "immersivetech";
	public static final String NAME = "Immersive Tech";
	public static final String VERSION = "@VERSION@";
	
	@SidedProxy(clientSide="ferro2000.immersivetech.client.ClientProxy", serverSide="ferro2000.immersivetech.common.CommonProxy")
	
	public static CommonProxy proxy;
	
	@Instance(MODID)
	public static ImmersiveTech instance;
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		ITContent.preInit();
		proxy.preInit();
		proxy.preInitEnd();
		
	}
	
	@Mod.EventHandler
	public void init(FMLInitializationEvent event)
	{
		/*DistillationRecipe.energyModifier = IPConfig.Machines.distillationTower_energyModifier;
		DistillationRecipe.timeModifier = IPConfig.Machines.distillationTower_timeModifier;
		
		PumpjackHandler.oilChance = IPConfig.Reservoirs.reservoir_chance;
		
		Config.manual_int.put("distillationTower_operationCost", (int) (2048 * IPConfig.Machines.distillationTower_energyModifier));
		Config.manual_int.put("pumpjack_consumption", IPConfig.Machines.pumpjack_consumption);
		Config.manual_int.put("pumpjack_speed", IPConfig.Machines.pumpjack_speed);
		
		int oil_min = 1000000;
		int oil_max = 5000000;
		for (ReservoirType type : PumpjackHandler.reservoirList.keySet())
		{
			if (type.name.equals("oil"))
			{
				oil_min = type.minSize;
				oil_max = type.maxSize;
				break;
			}
		}
		Config.manual_int.put("pumpjack_days", (((oil_max + oil_min) / 2) + oil_min) / (IPConfig.Machines.pumpjack_speed * 24000));*/

		
		
		ITContent.init();
		
		NetworkRegistry.INSTANCE.registerGuiHandler(instance, proxy);
		
		proxy.init();
		
		proxy.initEnd();
		
		//MinecraftForge.EVENT_BUS.register(new EventHandler());
	}
	
	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		proxy.postInit();
		proxy.postInitEnd();
		//PumpjackHandler.recalculateChances(true);
	}
	
	public static <T extends IForgeRegistryEntry<?>> T register(T object, String name)
	{
		return registerByFullName(object, MODID+":"+name);
	}
	public static <T extends IForgeRegistryEntry<?>> T registerByFullName(T object, String name)
	{
		object.setRegistryName(new ResourceLocation(name));
		return GameRegistry.register(object);
	}
	public static Block registerBlockByFullName(Block block, ItemBlock itemBlock, String name)
	{
		block = registerByFullName(block, name);
		registerByFullName(itemBlock, name);
		return block;
	}
	public static Block registerBlockByFullName(Block block, Class<? extends ItemBlock> itemBlock, String name)
	{
		try{
			return registerBlockByFullName(block, itemBlock.getConstructor(Block.class).newInstance(block), name);
		}catch(Exception e){e.printStackTrace();}
		return null;
	}
	public static Block registerBlock(Block block, Class<? extends ItemBlock> itemBlock, String name)
	{
		try{
			return registerBlockByFullName(block, itemBlock.getConstructor(Block.class).newInstance(block), MODID+":"+name);
		}catch(Exception e){e.printStackTrace();}
		return null;
	}
	
	@Mod.EventHandler
	public void serverStarted(FMLServerStartedEvent event)
	{/*
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
		{
			World world = FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld();
			if(!world.isRemote)
			{
				IPSaveData worldData = (IPSaveData) world.loadItemData(IPSaveData.class, IPSaveData.dataName);
				if(worldData == null)
				{
					worldData = new IPSaveData(IPSaveData.dataName);
					world.setItemData(IPSaveData.dataName, worldData);
				}
				IPSaveData.setInstance(world.provider.getDimension(), worldData);
			}
		}*/
	}
	
	public static CreativeTabs creativeTab = new CreativeTabs(MODID)
	{
		@Override
		public Item getTabIconItem()
		{
			return null;
		}
		@Override
		public ItemStack getIconItemStack()
		{
			return new ItemStack(IEContent.blockMetalDecoration0,1,6);
		}
	};

}
