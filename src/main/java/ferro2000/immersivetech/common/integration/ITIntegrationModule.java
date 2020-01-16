package ferro2000.immersivetech.common.integration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

import blusunrize.immersiveengineering.common.util.IELogger;
import ferro2000.immersivetech.common.integration.Crafttweaker.CraftTweakerHelper;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Highly inspired to BluSunrize's class
 */
public abstract class ITIntegrationModule {

	public static HashMap<String, Class<? extends ITIntegrationModule>> moduleClasses = new HashMap<String, Class<? extends ITIntegrationModule>>();
	public static Set<ITIntegrationModule> modules = new HashSet<ITIntegrationModule>();

	public static Consumer<Object> jeiAddFunc = o -> {};
	public static Consumer<Object> jeiRemoveFunc = o -> {};

	static
	{
		moduleClasses.put("crafttweaker", CraftTweakerHelper.class);
		moduleClasses.put("theoneprobe", OneProbeHelper.class);
	}
	
	public static void doModulesPreInit()
	{
		for(Entry<String, Class<? extends ITIntegrationModule>> e : moduleClasses.entrySet())
			if(Loader.isModLoaded(e.getKey()))
				try
				{
					ITIntegrationModule m = e.getValue().newInstance();
					modules.add(m);
					m.preInit();
				} catch(Exception exception)
				{
					IELogger.logger.error("Compat module for "+e.getKey()+" could not be preInitialized. Report this and include the error message below!", exception);
				}
	}
	
	public static void doModulesInit()
	{
		for(ITIntegrationModule compat : ITIntegrationModule.modules)
			try{
				compat.init();
			}catch (Exception exception){
				IELogger.logger.error("Compat module for "+compat+" could not be initialized. Report this and include the error message below!", exception);
			}
	}
	
	public static void doModulesPostInit()
	{
		for(ITIntegrationModule compat : ITIntegrationModule.modules)
			try{
				compat.postInit();
			}catch (Exception exception){
				IELogger.logger.error("Compat module for "+compat+" could not be postInitialized. Report this and include the error message below!", exception);
			}
	}
	
	//We don't want this to happen multiple times after all >_>
	public static boolean serverStartingDone = false;
	public static void doModulesLoadComplete()
	{
		if(!serverStartingDone)
		{
			serverStartingDone = true;
			for(ITIntegrationModule compat : ITIntegrationModule.modules)
				try{
					compat.loadComplete();
				}catch (Exception exception){
					IELogger.logger.error("Compat module for "+compat+" could not be initialized. Report this and include the error message below!", exception);
				}
		}
	}

	public abstract void preInit();
	public abstract void init();
	public abstract void postInit();
	public void loadComplete(){}
	@SideOnly(Side.CLIENT)
	public void clientPreInit(){}
	@SideOnly(Side.CLIENT)
	public void clientInit(){}
	@SideOnly(Side.CLIENT)
	public void clientPostInit(){}
	
}
