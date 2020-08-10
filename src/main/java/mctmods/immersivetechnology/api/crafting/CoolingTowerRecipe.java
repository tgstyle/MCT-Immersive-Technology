package mctmods.immersivetechnology.api.crafting;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;

public class CoolingTowerRecipe extends MultiblockRecipe {

    public static float timeModifier = 1;

    public final FluidStack fluidOutput0;
    public final FluidStack fluidOutput1;
    public final FluidStack fluidInput0;
    public final FluidStack fluidInput1;

    int totalProcessTime;

    public CoolingTowerRecipe(FluidStack fluidOutput0, FluidStack fluidOutput1, FluidStack fluidInput0, FluidStack fluidInput1, int time) {
        this.fluidOutput0 = fluidOutput0;
        this.fluidOutput1 = fluidOutput1;
        this.fluidInput0 = fluidInput0;
        this.fluidInput1 = fluidInput1;
        this.totalProcessTime = (int)Math.floor(time * timeModifier);
        this.fluidInputList = Lists.newArrayList(fluidInput0, fluidInput1);
        this.fluidOutputList = Lists.newArrayList(fluidOutput0, fluidOutput1);
    }

    public static ArrayList<CoolingTowerRecipe> recipeList = new ArrayList<CoolingTowerRecipe>();

    public static CoolingTowerRecipe addRecipe(FluidStack fluidOutput0, FluidStack fluidOutput1, FluidStack fluidInput0, FluidStack fluidInput1, int time) {
        CoolingTowerRecipe recipe = new CoolingTowerRecipe(fluidOutput0, fluidOutput1, fluidInput0, fluidInput1, time);
        recipeList.add(recipe);
        return recipe;
    }

    public static CoolingTowerRecipe findRecipe(FluidStack fluidInput0, FluidStack fluidInput1) {
        if(fluidInput0 == null || fluidInput1 == null) return null;
        for(CoolingTowerRecipe recipe : recipeList) {
            if(     recipe.fluidInput0 != null && fluidInput0.containsFluid(recipe.fluidInput0) &&
                    recipe.fluidInput1 != null && fluidInput1.containsFluid(recipe.fluidInput1)) return recipe;
        }
        return null;
    }

    public static CoolingTowerRecipe findRecipe0(FluidStack fluidInput0) {
        if(fluidInput0 == null) return null;
        for(CoolingTowerRecipe recipe : recipeList) {
            if(recipe.fluidInput0 != null && fluidInput0.containsFluid(recipe.fluidInput0)) return recipe;
        }
        return null;
    }

    public static CoolingTowerRecipe findRecipe1(FluidStack fluidInput1) {
        if(fluidInput1 == null) return null;
        for(CoolingTowerRecipe recipe : recipeList) {
            if(recipe.fluidInput1 != null && fluidInput1.containsFluid(recipe.fluidInput1)) return recipe;
        }
        return null;
    }

    public static CoolingTowerRecipe findRecipeByFluid(Fluid fluidInput0, Fluid fluidInput1) {
        if(fluidInput0 == null || fluidInput1 == null) return null;
        for(CoolingTowerRecipe recipe : recipeList) {
            if(     recipe.fluidInput0 != null && fluidInput0 == recipe.fluidInput0.getFluid() &&
                    recipe.fluidInput1 != null && fluidInput1 == recipe.fluidInput1.getFluid()) return recipe;
        }
        return null;
    }

    public static CoolingTowerRecipe findRecipeByFluid0(Fluid fluidInput0) {
        if(fluidInput0 == null) return null;
        for(CoolingTowerRecipe recipe : recipeList) {
            if(recipe.fluidInput0 != null && fluidInput0 == recipe.fluidInput0.getFluid()) return recipe;
        }
        return null;
    }

    public static CoolingTowerRecipe findRecipeByFluid1(Fluid fluidInput1) {
        if(fluidInput1 == null) return null;
        for(CoolingTowerRecipe recipe : recipeList) {
            if(recipe.fluidInput1 != null && fluidInput1 == recipe.fluidInput1.getFluid()) return recipe;
        }
        return null;
    }

    @Override
    public int getMultipleProcessTicks() {
        return 0;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setTag("input0", fluidInput0.writeToNBT(new NBTTagCompound()));
        nbt.setTag("input1", fluidInput0.writeToNBT(new NBTTagCompound()));
        return nbt;
    }

    public static CoolingTowerRecipe loadFromNBT(NBTTagCompound nbt) {
        FluidStack fluidInput0 = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("input0"));
        FluidStack fluidInput1 = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("input0"));
        return findRecipe(fluidInput0, fluidInput1);
    }

    @Override
    public int getTotalProcessTime() {
        return this.totalProcessTime;
    }
}
