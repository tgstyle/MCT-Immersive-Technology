package mctmods.immersivetechnology.common.util.compat;

import mctmods.immersivetechnology.common.util.ITLogger;
import mctmods.immersivetechnology.common.util.compat.crafttweaker.CraftTweakerHelper;
import mctmods.immersivetechnology.common.util.compat.top.OneProbeHelper;
import mctmods.immersivetechnology.common.util.compat.opencomputers.OCHelper;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

	/*
	* Highly inspired to BluSunrize's class
	*/
public abstract class ITCompatModule {

	public static HashMap<String, Class<? extends ITCompatModule>> moduleClasses = new HashMap<String, Class<? extends ITCompatModule>>();
	public static Set<ITCompatModule> modules = new HashSet<ITCompatModule>();

	public static Consumer<Object> jeiAddFunc = o -> {};
	public static Consumer<Object> jeiRemoveFunc = o -> {};

	static {
		moduleClasses.put("crafttweaker", CraftTweakerHelper.class);
		moduleClasses.put("theoneprobe", OneProbeHelper.class);
		moduleClasses.put("opencomputers", OCHelper.class);
	}

	public static void doModulesPreInit() {
		for(Entry<String, Class<? extends ITCompatModule>> e : moduleClasses.entrySet()) {
			if(Loader.isModLoaded(e.getKey())) {
				try {
					ITCompatModule m = e.getValue().newInstance();
					modules.add(m);
					m.preInit();
				} catch(Exception exception) {
					ITLogger.logger.error("Compat module for" + e.getKey() + " could not be preInitialized. Report this and include the error message below!", exception);
				}
			}
		}
	}

	public static void doModulesInit() {
		for(ITCompatModule compat : ITCompatModule.modules) {
			try {
				compat.init();
			} catch(Exception exception) {
				ITLogger.logger.error("Compat module for" + compat + " could not be initialized. Report this and include the error message below!", exception);
			}
		}
	}

	public static void doModulesPostInit() {
		for(ITCompatModule compat : ITCompatModule.modules) {
			try {
				compat.postInit();
			} catch(Exception exception) {
				ITLogger.logger.error("Compat module for" + compat + " could not be postInitialized. Report this and include the error message below!", exception);
			}
		}
	}

	public static boolean serverStartingDone = false;
	public static void doModulesLoadComplete() {
		if(!serverStartingDone) {
			serverStartingDone = true;
			for(ITCompatModule compat : ITCompatModule.modules) {
				try {
					compat.loadComplete();
				} catch(Exception exception) {
					ITLogger.logger.error("Compat module for" + compat + " could not be initialized. Report this and include the error message below!", exception);
				}
			}
		}
	}

	public abstract void preInit();

	public abstract void init();

	public abstract void postInit();

	public void loadComplete() {
	}

	@SideOnly(Side.CLIENT)
	public void clientPreInit() {
	}

	@SideOnly(Side.CLIENT)
	public void clientInit() {
	}

	@SideOnly(Side.CLIENT)
	public void clientPostInit() {
	}

}
