package mctmods.immersivetechnology.api.crafting;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HighPressureSteamTurbineRecipe extends MultiblockRecipe {
    public static float timeModifier = 1;

    public final FluidStack fluidOutput;
    public final FluidStack fluidInput;

    int totalProcessTime;

    public HighPressureSteamTurbineRecipe(FluidStack fluidOutput, FluidStack fluidInput, int time) {
        this.fluidOutput = fluidOutput;
        this.fluidInput = fluidInput;
        this.totalProcessTime = (int) Math.floor(time * timeModifier);
        this.fluidInputList = Lists.newArrayList(this.fluidInput);
        this.fluidOutputList = Lists.newArrayList();
        if (this.fluidOutput != null) this.fluidOutputList.add(this.fluidOutput);
    }

    public static ArrayList<HighPressureSteamTurbineRecipe> recipeList = new ArrayList<>();

    private static final Map<Fluid, HighPressureSteamTurbineRecipe> recipeMap = new HashMap<>();

    public static void addFuel(FluidStack fluidOutput, FluidStack fluidInput, int time) {
        HighPressureSteamTurbineRecipe recipe = new HighPressureSteamTurbineRecipe(fluidOutput, fluidInput, time);
        recipeList.add(recipe);
        recipeMap.put(fluidInput.getFluid(), recipe);
    }

    public static HighPressureSteamTurbineRecipe findFuel(FluidStack fluidInput) {
        if (fluidInput == null) {
            return null;
        }
        HighPressureSteamTurbineRecipe recipe = recipeMap.get(fluidInput.getFluid());
        if (recipe != null && fluidInput.containsFluid(recipe.fluidInput)) {
            return recipe;
        }
        for (HighPressureSteamTurbineRecipe r : recipeList) {
            if (r.fluidInput != null && fluidInput.containsFluid(r.fluidInput)) {
                return r;
            }
        }
        return null;
    }

    public static HighPressureSteamTurbineRecipe findFuelByFluid(Fluid fluidInput) {
        if (fluidInput == null) {
            return null;
        }
        return recipeMap.get(fluidInput);
    }

    @Override public int getMultipleProcessTicks() {
        return 0;
    }

    @Override public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setTag("input", fluidInput.writeToNBT(new NBTTagCompound()));
        return nbt;
    }

    public static HighPressureSteamTurbineRecipe loadFromNBT(NBTTagCompound nbt) {
        FluidStack fluidInput = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("input"));
        return findFuel(fluidInput);
    }

    @Override public int getTotalProcessTime() {
        return this.totalProcessTime;
    }
}
