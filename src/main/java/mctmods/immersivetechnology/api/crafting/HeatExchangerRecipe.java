package mctmods.immersivetechnology.api.crafting;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;

public class HeatExchangerRecipe extends MultiblockRecipe {

    public static float timeModifier = 1;
    public static float energyModifier = 1;

    public final FluidStack fluidOutput0;
    public final FluidStack fluidOutput1;
    public final FluidStack fluidInput0;
    public final FluidStack fluidInput1;

    int totalProcessTime;
    int totalProcessEnergy;

    public HeatExchangerRecipe(FluidStack fluidOutput0, FluidStack fluidOutput1, FluidStack fluidInput0, FluidStack fluidInput1, int energy, int time) {
        this.fluidOutput0 = fluidOutput0;
        this.fluidOutput1 = fluidOutput1;
        this.fluidInput0 = fluidInput0;
        this.fluidInput1 = fluidInput1;
        this.totalProcessTime = (int)Math.floor(time * timeModifier);
        this.totalProcessEnergy = (int)Math.floor(energy * energyModifier);
        this.fluidInputList = Lists.newArrayList(fluidInput0, fluidInput1);
    }

    public static ArrayList<HeatExchangerRecipe> recipeList = new ArrayList<>();

    public static HeatExchangerRecipe addRecipe(FluidStack fluidOutput0, FluidStack fluidOutput1, FluidStack fluidInput0, FluidStack fluidInput1, int energy, int time) {
        HeatExchangerRecipe recipe = new HeatExchangerRecipe(fluidOutput0, fluidOutput1, fluidInput0, fluidInput1, energy, time);
        recipeList.add(recipe);
        return recipe;
    }

    public static HeatExchangerRecipe findRecipe(FluidStack fluidInput0, FluidStack fluidInput1) {
        if(fluidInput0 == null || fluidInput1 == null) return null;
        for(HeatExchangerRecipe recipe : recipeList) {
            if(     recipe.fluidInput0 != null && fluidInput0.containsFluid(recipe.fluidInput0) &&
                    recipe.fluidInput1 != null && fluidInput1.containsFluid(recipe.fluidInput1)) return recipe;
        }
        return null;
    }

    public static HeatExchangerRecipe findRecipe0(FluidStack fluidInput0) {
        if(fluidInput0 == null) return null;
        for(HeatExchangerRecipe recipe : recipeList) {
            if(recipe.fluidInput0 != null && fluidInput0.containsFluid(recipe.fluidInput0)) return recipe;
        }
        return null;
    }

    public static HeatExchangerRecipe findRecipe1(FluidStack fluidInput1) {
        if(fluidInput1 == null) return null;
        for(HeatExchangerRecipe recipe : recipeList) {
            if(recipe.fluidInput1 != null && fluidInput1.containsFluid(recipe.fluidInput1)) return recipe;
        }
        return null;
    }

    public static HeatExchangerRecipe findRecipeByFluid(Fluid fluidInput0, Fluid fluidInput1) {
        if(fluidInput0 == null || fluidInput1 == null) return null;
        for(HeatExchangerRecipe recipe : recipeList) {
            if(     recipe.fluidInput0 != null && fluidInput0 == recipe.fluidInput0.getFluid() &&
                    recipe.fluidInput1 != null && fluidInput1 == recipe.fluidInput1.getFluid()) return recipe;
        }
        return null;
    }

    public static HeatExchangerRecipe findRecipeByFluid0(Fluid fluidInput0) {
        if(fluidInput0 == null) return null;
        for(HeatExchangerRecipe recipe : recipeList) {
            if(recipe.fluidInput0 != null && fluidInput0 == recipe.fluidInput0.getFluid()) return recipe;
        }
        return null;
    }

    public static HeatExchangerRecipe findRecipeByFluid1(Fluid fluidInput1) {
        if(fluidInput1 == null) return null;
        for(HeatExchangerRecipe recipe : recipeList) {
            if(recipe.fluidInput1 != null && fluidInput1 == recipe.fluidInput1.getFluid()) return recipe;
        }
        return null;
    }

    @Override
    public int getMultipleProcessTicks() {
        return 0;
    }

    @Override
    public int getTotalProcessEnergy() {
        return this.totalProcessEnergy;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setTag("input0", fluidInput0.writeToNBT(new NBTTagCompound()));
        nbt.setTag("input1", fluidInput0.writeToNBT(new NBTTagCompound()));
        return nbt;
    }

    public static HeatExchangerRecipe loadFromNBT(NBTTagCompound nbt) {
        FluidStack fluidInput0 = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("input0"));
        FluidStack fluidInput1 = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("input1"));
        return findRecipe(fluidInput0, fluidInput1);
    }

    @Override
    public int getTotalProcessTime() {
        return this.totalProcessTime;
    }

    @Override
    public void setupJEI() {
        super.setupJEI();
        jeiFluidOutputList = new ArrayList<>();
        jeiFluidOutputList.add(fluidOutput0.copy());
        if (fluidOutput1 != null) jeiFluidOutputList.add(fluidOutput1.copy());
    }
}
