package mctmods.immersivetechnology.api.crafting;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;

public class BoilerRecipe extends MultiblockRecipe {
	public static float timeModifier = 1;

	public final FluidStack fluidOutput;
	public final FluidStack fluidInput;

	int totalProcessTime;
	double heat;

	public BoilerRecipe(FluidStack fluidOutput, FluidStack fluidInput, int time) {
		this.fluidOutput = fluidOutput;
		this.fluidInput = fluidInput;
		this.totalProcessTime = (int)Math.floor(time * timeModifier);
		this.fluidInputList = Lists.newArrayList(this.fluidInput);
		this.fluidOutputList = Lists.newArrayList(this.fluidOutput);
	}

	public static ArrayList<BoilerRecipe> recipeList = new ArrayList<>();

	public static BoilerRecipe addRecipe(FluidStack fluidOutput, FluidStack fluidInput, int time) {
		BoilerRecipe recipe = new BoilerRecipe(fluidOutput, fluidInput, time);
		recipeList.add(recipe);
		return recipe;
	}

	public static BoilerRecipe findRecipe(FluidStack fluidInput) {
		if(fluidInput == null) return null;
		for(BoilerRecipe recipe : recipeList) {
			if(recipe.fluidInput != null && (fluidInput.containsFluid(recipe.fluidInput))) return recipe;
		}
		return null;
	}

	public static BoilerRecipe findRecipeByFluid(Fluid fluidInput) {
		if(fluidInput == null) return null;
		for(BoilerRecipe recipe : recipeList) {
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

	public static BoilerRecipe loadFromNBT(NBTTagCompound nbt) {
		FluidStack fluidInput = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("input"));
		return findRecipe(fluidInput);
	}
	
	@Override
	public int getTotalProcessTime() {
		return this.totalProcessTime;
	}

	public static ArrayList<BoilerFuelRecipe> fuelList = new ArrayList<>();
	
	public static BoilerFuelRecipe addFuel(FluidStack fluidInput, int time, double heat) {
		BoilerFuelRecipe recipe = new BoilerFuelRecipe(fluidInput, time, heat);
		fuelList.add(recipe);
		return recipe;
	}

	public static BoilerFuelRecipe findFuel(FluidStack fluidInput) {
		for(BoilerFuelRecipe recipe : fuelList) {
			if(fluidInput != null) {
				if(recipe.fluidInput != null && (fluidInput.containsFluid(recipe.fluidInput))) {
					return recipe;
				}
			}
		}
		return null;
	}

	public static BoilerFuelRecipe findFuelByFluid(Fluid fluidInput) {
		if(fluidInput == null) return null;
		for(BoilerFuelRecipe recipe : fuelList) {
			if(recipe.fluidInput != null && fluidInput == recipe.fluidInput.getFluid()) return recipe;
		}
		return null;
	}

	public static class BoilerFuelRecipe extends MultiblockRecipe {
		public static float timeModifier = 1;

		public final FluidStack fluidInput;

		int totalProcessTime;
		double heat;

		public BoilerFuelRecipe(FluidStack fluidInput, int time, double heat) {
			this.fluidInput = fluidInput;
			this.totalProcessTime = (int)Math.floor(time * timeModifier);
			this.heat = heat;
			this.fluidInputList = Lists.newArrayList(this.fluidInput);
		}

		@Override
		public int getMultipleProcessTicks() {
			return 0;
		}

		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			nbt.setTag("inputFuel", fluidInput.writeToNBT(new NBTTagCompound()));
			return nbt;
		}

		public static BoilerFuelRecipe loadFromNBT(NBTTagCompound nbt) {
			FluidStack fluidInput = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("inputFuel"));
			return findFuel(fluidInput);
		}

		@Override
		public int getTotalProcessTime() {
			return this.totalProcessTime;
		}

		public double getHeat() {
			return this.heat;
		}
	}

}