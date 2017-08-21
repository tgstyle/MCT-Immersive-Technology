package ferro2000.immersivetech.api.craftings;

import java.util.ArrayList;

import com.google.common.collect.Lists;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.common.util.ListUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidStack;

public class DistillerRecipes extends MultiblockRecipe {

	public static float energyModifier = 1;
	public static float timeModifier = 1;
	public static float chance;

	public final FluidStack fluidOutput;
	public final FluidStack input;
	public final ItemStack itemOutput;
	
	public DistillerRecipes(FluidStack fluidOutput, FluidStack input, ItemStack itemOutput, int energy, int time, float chance)
	{
		this.fluidOutput = fluidOutput;
		this.input = input;
		this.itemOutput = itemOutput;
		this.totalProcessEnergy = (int)Math.floor(energy*energyModifier);
		this.totalProcessTime = (int)Math.floor(time*timeModifier);

		this.chance = chance;
		
		this.fluidInputList = Lists.newArrayList(this.input);
		this.fluidOutputList = Lists.newArrayList(this.fluidOutput);
		this.outputList = ListUtils.fromItem(this.itemOutput);
	}

	public static ArrayList<DistillerRecipes> recipeList = new ArrayList();
	public static DistillerRecipes addRecipe(FluidStack fluidOutput,  FluidStack input, ItemStack itemOutput, int energy, int time, float chance)
	{
		DistillerRecipes r = new DistillerRecipes(fluidOutput, input, itemOutput, energy, time, chance);
		recipeList.add(r);
		return r;
	}
	public static DistillerRecipes findRecipe(FluidStack input)
	{
		for (DistillerRecipes recipe : recipeList)
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
	public static DistillerRecipes loadFromNBT(NBTTagCompound nbt)
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
	
	int totalProcessEnergy;
	
	@Override
	public int getTotalProcessEnergy()
	{
		return this.totalProcessEnergy;
	}
	
	@Override
	public NonNullList<ItemStack> getActualItemOutputs(TileEntity tile)
	{
		if (tile.getWorld().rand.nextFloat() <= chance)
		{
			return getItemOutputs();
		}
		else
		{
			return ListUtils.fromItems();
		}
	}

}
