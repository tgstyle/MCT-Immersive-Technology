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

public class BoilerLiquidRecipe extends MultiblockRecipe {
    public static float timeModifier = 1;
    public final FluidStack fluidInput;
    public final double heatPerTick;
    public final double targetHeat;
    int totalProcessTime;

    public BoilerLiquidRecipe(FluidStack fluidInput, int time, double heatPerTick, double targetHeat) {
        this.fluidInput = fluidInput;
        this.totalProcessTime = (int) Math.floor(time * timeModifier);
        this.heatPerTick = heatPerTick;
        this.targetHeat = Math.min(targetHeat, Multiblocks.boilerHeat.boiler_heat_max);
        this.fluidInputList = Lists.newArrayList(this.fluidInput);
    }

    public static ArrayList<BoilerLiquidRecipe> fuelList = new ArrayList<>();
    private static final Map<Fluid, BoilerLiquidRecipe> fuelMap = new HashMap<>();

    public static BoilerLiquidRecipe addFuel(FluidStack fluidInput, int time, double heatPerTick, double targetHeat) { return addFuel(new BoilerLiquidRecipe(fluidInput, time, heatPerTick, targetHeat)); }

    public static BoilerLiquidRecipe addFuel(BoilerLiquidRecipe recipe) {
        fuelList.add(recipe);
        fuelMap.put(recipe.fluidInput.getFluid(), recipe);
        return recipe;
    }

    public static void removeFuel(FluidStack fluidInput) {
        fuelList.removeIf(recipe -> recipe != null && recipe.fluidInput.isFluidEqual(fluidInput));
        fuelMap.values().removeIf(recipe -> recipe != null && recipe.fluidInput.isFluidEqual(fluidInput));
    }

    public static BoilerLiquidRecipe findFuel(FluidStack fluidInput) {
        if (fluidInput == null) { return null; }
        BoilerLiquidRecipe recipe = fuelMap.get(fluidInput.getFluid());
        if (recipe != null && recipe.fluidInput != null && fluidInput.containsFluid(recipe.fluidInput)) { return recipe; }
        for (BoilerLiquidRecipe r : fuelList) {
            if (r.fluidInput != null && fluidInput.containsFluid(r.fluidInput)) { return r; }
        }
        return null;
    }

    @Override public int getMultipleProcessTicks() { return 0; }

    @Override public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setTag("input", fluidInput.writeToNBT(new NBTTagCompound()));
        return nbt;
    }

    @Override public int getTotalProcessTime() { return this.totalProcessTime; }
}
