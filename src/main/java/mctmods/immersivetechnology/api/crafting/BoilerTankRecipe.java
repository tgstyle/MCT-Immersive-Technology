package mctmods.immersivetechnology.api.crafting;

import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BoilerTankRecipe extends MultiblockRecipe {
    public static float timeModifier = 1;
    public final FluidStack fluidOutput;
    public final FluidStack fluidInput;
    public final double requiredHeat;
    int totalProcessTime;

    public BoilerTankRecipe(FluidStack fluidOutput, FluidStack fluidInput, int time, double requiredHeat) {
        this.fluidOutput = fluidOutput;
        this.fluidInput = fluidInput;
        this.totalProcessTime = (int) Math.floor(time * timeModifier);
        this.requiredHeat = Math.min(requiredHeat, Multiblocks.boilerHeat.boiler_heat_max);
        this.fluidInputList = Lists.newArrayList(this.fluidInput);
        this.fluidOutputList = Lists.newArrayList(this.fluidOutput);
    }

    public static ArrayList<BoilerTankRecipe> recipeList = new ArrayList<>();
    private static final Map<Fluid, BoilerTankRecipe> recipeMap = new HashMap<>();

    public static BoilerTankRecipe addRecipe(FluidStack fluidOutput, FluidStack fluidInput, int time, double requiredHeat) { return addRecipe(new BoilerTankRecipe(fluidOutput, fluidInput, time, requiredHeat)); }

    public static BoilerTankRecipe addRecipe(BoilerTankRecipe recipe) {
        recipeList.add(recipe);
        recipeMap.put(recipe.fluidInput.getFluid(), recipe);
        return recipe;
    }

    public static void removeRecipe(FluidStack fluidInput) {
        recipeList.removeIf(recipe -> recipe != null && recipe.fluidInput.isFluidEqual(fluidInput));
        recipeMap.values().removeIf(recipe -> recipe != null && recipe.fluidInput.isFluidEqual(fluidInput));
    }

    public static BoilerTankRecipe findRecipe(FluidStack fluidInput) {
        if (fluidInput == null) { return null; }
        BoilerTankRecipe recipe = recipeMap.get(fluidInput.getFluid());
        if (recipe != null && recipe.fluidInput != null && fluidInput.containsFluid(recipe.fluidInput)) { return recipe; }
        for (BoilerTankRecipe r : recipeList) {
            if (r.fluidInput != null && fluidInput.containsFluid(r.fluidInput)) { return r; }
        }
        return null;
    }

    @Override public int getMultipleProcessTicks() { return 0; }

    @Override public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setTag("input", fluidInput.writeToNBT(new NBTTagCompound()));
        return nbt;
    }

    public static BoilerTankRecipe loadFromNBT(NBTTagCompound nbt) {
        FluidStack fluidInput = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("input"));
        return findRecipe(fluidInput);
    }

    @Override public int getTotalProcessTime() { return this.totalProcessTime; }
}
