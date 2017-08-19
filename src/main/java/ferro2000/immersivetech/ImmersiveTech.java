package ferro2000.immersivetech;

import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.IEContent;
import ferro2000.immersivetech.common.CommonProxy;
import ferro2000.immersivetech.common.Config.ITConfig;
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
		Config.preInit(event);
		ITContent.preInit();
		proxy.preInit();
		proxy.preInitEnd();
		registerVariables();
		
	}
	
	@Mod.EventHandler
	public void init(FMLInitializationEvent event)
	{
		
		ITContent.init();
		
		NetworkRegistry.INSTANCE.registerGuiHandler(instance, proxy);
		
		proxy.init();
		
		proxy.initEnd();
		
	}
	
	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		proxy.postInit();
		proxy.postInitEnd();
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
	{
	}
	
	public static CreativeTabs creativeTab = new CreativeTabs(MODID)
	{
		@Override
		public ItemStack getTabIconItem()
		{
			return null;
		}
		@Override
		public ItemStack getIconItemStack()
		{
			return new ItemStack(IEContent.blockMetalDecoration0,1,6);
		}
	};
	
	public void registerVariables() {
		Config.manual_int.put("steamTurbine_output", ITConfig.Machines.steamTurbine_output);
		Config.manual_int.put("solarTower_steamWater", ITConfig.Machines.solarTower_steamWater);
		Config.manual_int.put("solarTower_steamDistWater", ITConfig.Machines.solarTower_steamDistWater);
		Config.manual_int.put("distiller_distWaterWater", ITConfig.Machines.distiller_distWaterWater);
	}

}
