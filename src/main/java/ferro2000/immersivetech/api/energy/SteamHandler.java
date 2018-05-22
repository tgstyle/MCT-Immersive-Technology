package ferro2000.immersivetech.api.energy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import blusunrize.immersiveengineering.api.ApiUtils;

import java.util.Map;

import net.minecraftforge.fluids.Fluid;

public class SteamHandler {

	static final HashMap<String, Integer> steamGenBurnTime = new HashMap<String, Integer>();
	static final Set<Fluid> drillSteam = new HashSet<Fluid>();
	
	public static void registerSteam(Fluid steam, int burnTime) {
		if(steam!=null) {
			steamGenBurnTime.put(steam.getName(), burnTime);
		}
	}
	
	public static int getBurnTime(Fluid steam) {
		if(steam!=null) {
			String s = steam.getName();
			if(steamGenBurnTime.containsKey(s)) {
				return steamGenBurnTime.get(s);
			}
		}
		return 0;
	}
	
	public static boolean isValidSteam(Fluid steam) {
		if(steam!=null) {
			return steamGenBurnTime.containsKey(steam.getName());
		}
		return false;
	}
	
	public static HashMap<String, Integer> getSteamValues(){
		return steamGenBurnTime;
	}
	
	public static Map<String, Integer> getSteamValuesSorted(boolean inverse){
		return ApiUtils.sortMap(steamGenBurnTime, inverse);
	}
	
	public static void registerDrillSteam(Fluid steam) {
		if(steam!=null) {
			drillSteam.add(steam);
		}
	}
	
	public static boolean isValidDrillSteam(Fluid steam) {
		return steam!=null && drillSteam.contains(steam);
	}
	
}
