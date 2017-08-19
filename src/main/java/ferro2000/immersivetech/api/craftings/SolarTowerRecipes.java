package ferro2000.immersivetech.api.craftings;

import java.util.ArrayList;

import com.google.common.collect.Lists;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

public class SolarTowerRecipes extends MultiblockRecipe{

	public static float timeModifier = 1;

	public final FluidStack fluidOutput;
	public final FluidStack input;
	
	public SolarTowerRecipes(FluidStack fluidOutput, FluidStack input, int time)
	{
		this.fluidOutput = fluidOutput;
		this.input = input;
		this.totalProcessTime = (int)Math.floor(time*timeModifier);
		
		this.fluidInputList = Lists.newArrayList(this.input);
		this.fluidOutputList = Lists.newArrayList(this.fluidOutput);
	}

	public static ArrayList<SolarTowerRecipes> recipeList = new ArrayList();
	public static SolarTowerRecipes addRecipe(FluidStack fluidOutput,  FluidStack input, int time)
	{
		SolarTowerRecipes r = new SolarTowerRecipes(fluidOutput, input, time);
		recipeList.add(r);
		return r;
	}
	public static SolarTowerRecipes findRecipe(FluidStack input)
	{
		for (SolarTowerRecipes recipe : recipeList)
		{
			if (input != null)
			{
				if (recipe.input != null && (input.containsFluid(recipe.input)))
				{
					return recipe;
				}

			}
		}
		return null;
	}

	@Override
	public int getMultipleProcessTicks()
	{
		return 0;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		nbt.setTag("input", input.writeToNBT(new NBTTagCompound()));
		return nbt;
	}
	public static SolarTowerRecipes loadFromNBT(NBTTagCompound nbt)
	{
		FluidStack input = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("input"));
		return findRecipe(input);
	}
	
	int totalProcessTime;


	@Override
	public int getTotalProcessTime()
	{
		return this.totalProcessTime;
	}
	

}
