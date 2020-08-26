package mctmods.immersivetechnology.api.crafting;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;

public class GasTurbineRecipe extends MultiblockRecipe {
    public static float timeModifier = 1;

    public final FluidStack fluidOutput;
    public final FluidStack fluidInput;

    int totalProcessTime;

    public GasTurbineRecipe(FluidStack fluidOutput, FluidStack fluidInput, int time) {
        this.fluidOutput = fluidOutput;
        this.fluidInput = fluidInput;
        this.totalProcessTime = (int)Math.floor(time * timeModifier);
        this.fluidInputList = Lists.newArrayList(this.fluidInput);
        this.fluidOutputList = Lists.newArrayList(this.fluidOutput);
    }

    public static ArrayList<GasTurbineRecipe> recipeList = new ArrayList<GasTurbineRecipe>();

    public static GasTurbineRecipe addFuel(FluidStack fluidOutput, FluidStack fluidInput, int time) {
        GasTurbineRecipe recipe = new GasTurbineRecipe(fluidOutput, fluidInput, time);
        recipeList.add(recipe);
        return recipe;
    }

    public static GasTurbineRecipe findFuel(FluidStack fluidInput) {
        if(fluidInput == null) return null;
        for(GasTurbineRecipe recipe : recipeList) {
            if(recipe.fluidInput != null && (fluidInput.containsFluid(recipe.fluidInput))) return recipe;
        }
        return null;
    }

    public static GasTurbineRecipe findFuelByFluid(Fluid fluidInput) {
        if(fluidInput == null) return null;
        for(GasTurbineRecipe recipe : recipeList) {
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

    public static GasTurbineRecipe loadFromNBT(NBTTagCompound nbt) {
        FluidStack fluidInput = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("input"));
        return findFuel(fluidInput);
    }

    @Override
    public int getTotalProcessTime() {
        return this.totalProcessTime;
    }

}