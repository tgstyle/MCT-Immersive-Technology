package mctmods.immersivetechnology.api.crafting;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;

public class ElectrolyticCrucibleBatteryRecipe extends MultiblockRecipe {

    public static float timeModifier = 1;
    public static float energyModifier = 1;

    public final FluidStack fluidInput0;
    public final FluidStack fluidOutput0;
    public final FluidStack fluidOutput1;
    public final FluidStack fluidOutput2;
    public final ItemStack itemOutput;

    int totalProcessTime;
    int totalProcessEnergy;

    public ElectrolyticCrucibleBatteryRecipe(FluidStack fluidOutput0, FluidStack fluidOutput1, FluidStack fluidOutput2, ItemStack itemOutput, FluidStack fluidInput0, int energy, int time) {
        this.fluidInput0 = fluidInput0;
        this.fluidOutput0 = fluidOutput0;
        this.fluidOutput1 = fluidOutput1;
        this.fluidOutput2 = fluidOutput2;
        this.itemOutput = itemOutput;
        this.totalProcessTime = (int)Math.floor(time * timeModifier);
        this.totalProcessEnergy = (int)Math.floor(energy * energyModifier);
        this.fluidInputList = Lists.newArrayList(fluidInput0);
    }

    public static ArrayList<ElectrolyticCrucibleBatteryRecipe> recipeList = new ArrayList<>();

    public static ElectrolyticCrucibleBatteryRecipe addRecipe(FluidStack fluidOutput0, FluidStack fluidOutput1, FluidStack fluidOutput2, ItemStack itemOutput, FluidStack fluidInput0, int energy, int time) {
        ElectrolyticCrucibleBatteryRecipe recipe = new ElectrolyticCrucibleBatteryRecipe(fluidOutput0, fluidOutput1, fluidOutput2, itemOutput, fluidInput0, energy, time);
        recipeList.add(recipe);
        return recipe;
    }

    public static ElectrolyticCrucibleBatteryRecipe findRecipe(FluidStack fluidInput0) {
        if(fluidInput0 == null) return null;
        for(ElectrolyticCrucibleBatteryRecipe recipe : recipeList) {
            if(recipe.fluidInput0 != null && fluidInput0.containsFluid(recipe.fluidInput0)) return recipe;
        }
        return null;
    }

    public static ElectrolyticCrucibleBatteryRecipe findRecipeFluid(Fluid fluidInput0) {
        if(fluidInput0 == null) return null;
        for(ElectrolyticCrucibleBatteryRecipe recipe : recipeList) {
            if(recipe.fluidInput0 != null && fluidInput0 == recipe.fluidInput0.getFluid()) return recipe;
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
        return nbt;
    }

    public static ElectrolyticCrucibleBatteryRecipe loadFromNBT(NBTTagCompound nbt) {
        FluidStack fluidInput0 = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("input0"));
        return findRecipe(fluidInput0);
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
        if (fluidOutput2 != null) jeiFluidOutputList.add(fluidOutput2.copy());
    }
}
