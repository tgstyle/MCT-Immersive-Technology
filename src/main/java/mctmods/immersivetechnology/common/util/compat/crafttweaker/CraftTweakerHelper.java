package mctmods.immersivetechnology.common.util.compat.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.item.IngredientStack;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.oredict.IOreDictEntry;
import mctmods.immersivetechnology.common.Config.ITConfig.Machines.Multiblock;
import mctmods.immersivetechnology.common.Config.ITConfig.Experimental;
import mctmods.immersivetechnology.common.util.compat.ITCompatModule;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

/*
* Created by Kurtchekov on 2018-12-27, based on BluSunrize's code for Immersive Engineering.
*/

@SuppressWarnings("deprecation")
public class CraftTweakerHelper extends ITCompatModule {
	@Override
	public void preInit() {
		if(Multiblock.enable_boiler) CraftTweakerAPI.registerClass(Boiler.class);
		if(Multiblock.enable_distiller) CraftTweakerAPI.registerClass(Distiller.class);
		if(Multiblock.enable_solarTower) CraftTweakerAPI.registerClass(SolarTower.class);
		if(Multiblock.enable_steamTurbine) CraftTweakerAPI.registerClass(SteamTurbine.class);
		if(Multiblock.enable_coolingTower) CraftTweakerAPI.registerClass(CoolingTower.class);
		if(Multiblock.enable_gasTurbine) CraftTweakerAPI.registerClass(GasTurbine.class);
		if(Experimental.replace_IE_pipes) CraftTweakerAPI.registerClass(PressurizedFluid.class);
	}

	@Override
	public void init() {
	}

	@Override
	public void postInit() {
	}

	/*
	* Helper Methods
	*/
	public static ItemStack toStack(IItemStack iStack) {
		if(iStack == null) return ItemStack.EMPTY;
		return (ItemStack)iStack.getInternal();
	}

	public static Object toObject(IIngredient iStack) {
		if(iStack == null) return null;
		else {
			if(iStack instanceof IOreDictEntry) return ((IOreDictEntry)iStack).getName();
			else if(iStack instanceof IItemStack) return toStack((IItemStack)iStack);
			else if(iStack instanceof IngredientStack) {
				IIngredient ingr = ReflectionHelper.getPrivateValue(IngredientStack.class, (IngredientStack)iStack, "ingredient");
				Object o = toObject(ingr);
				if(o instanceof String) return new blusunrize.immersiveengineering.api.crafting.IngredientStack((String)o, iStack.getAmount());
				else return o;
			} else return null;
		}
	}

	public static blusunrize.immersiveengineering.api.crafting.IngredientStack toIEIngredientStack(IIngredient iStack) {
		if(iStack == null) return null;
		else {
			if(iStack instanceof IOreDictEntry) return new blusunrize.immersiveengineering.api.crafting.IngredientStack(((IOreDictEntry)iStack).getName());
			else if(iStack instanceof IItemStack) return new blusunrize.immersiveengineering.api.crafting.IngredientStack(toStack((IItemStack)iStack));
			else if(iStack instanceof IngredientStack) {
				IIngredient ingr = ReflectionHelper.getPrivateValue(IngredientStack.class, (IngredientStack)iStack, "ingredient");
				blusunrize.immersiveengineering.api.crafting.IngredientStack ingrStack = toIEIngredientStack(ingr);
				ingrStack.inputSize = iStack.getAmount();
				return ingrStack;
			}
			else return null;
		}
	}

	public static Object[] toObjects(IIngredient[] iStacks) {
		Object[] oA = new Object[iStacks.length];
		for(int i = 0; i < iStacks.length; i++) oA[i] = toObject(iStacks[i]);
		return oA;
	}

	public static FluidStack toFluidStack(ILiquidStack iStack) {
		if(iStack == null) {
			return null;
		}
		return (FluidStack)iStack.getInternal();
	}

}