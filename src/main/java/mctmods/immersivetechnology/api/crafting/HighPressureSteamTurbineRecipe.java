package mctmods.immersivetechnology.api.crafting;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;

public class HighPressureSteamTurbineRecipe extends MultiblockRecipe {
	public static float timeModifier = 1;

	public final FluidStack fluidOutput;
	public final FluidStack fluidInput;

	int totalProcessTime;

	public HighPressureSteamTurbineRecipe(FluidStack fluidOutput, FluidStack fluidInput, int time) {
		this.fluidOutput = fluidOutput;
		this.fluidInput = fluidInput;
		this.totalProcessTime = (int)Math.floor(time * timeModifier);
		this.fluidInputList = Lists.newArrayList(this.fluidInput);
		this.fluidOutputList = Lists.newArrayList(this.fluidOutput);
	}

	public static ArrayList<HighPressureSteamTurbineRecipe> recipeList = new ArrayList<>();

	public static HighPressureSteamTurbineRecipe addFuel(FluidStack fluidOutput, FluidStack fluidInput, int time) {
		HighPressureSteamTurbineRecipe recipe = new HighPressureSteamTurbineRecipe(fluidOutput, fluidInput, time);
		recipeList.add(recipe);
		return recipe;
	}

	public static HighPressureSteamTurbineRecipe findFuel(FluidStack fluidInput) {
		if(fluidInput == null) return null;
		for(HighPressureSteamTurbineRecipe recipe : recipeList) {
			if(recipe.fluidInput != null && (fluidInput.containsFluid(recipe.fluidInput))) return recipe;
		}
		return null;
	}

	public static HighPressureSteamTurbineRecipe findFuelByFluid(Fluid fluidInput) {
		if(fluidInput == null) return null;
		for(HighPressureSteamTurbineRecipe recipe : recipeList) {
			if(recipe.fluidInput != null && fluidInput == recipe.fluidInput.getFluid()) return recipe;
		}
		return null;
	}

	@Override
	public int getMultipleProcessTicks() {
		return 0;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setTag("input", fluidInput.writeToNBT(new NBTTagCompound()));
		return nbt;
	}

	public static HighPressureSteamTurbineRecipe loadFromNBT(NBTTagCompound nbt) {
		FluidStack fluidInput = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("input"));
		return findFuel(fluidInput);
	}
	
	@Override
	public int getTotalProcessTime() {
		return this.totalProcessTime;
	}

}