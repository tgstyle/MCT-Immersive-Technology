package mctmods.immersivetechnology.api.crafting;

import com.immersiveconvergence.api.crafting.MultiblockRecipeBase;

import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;

import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GasTurbineRecipe extends MultiblockRecipeBase {
    public static float timeModifier = 1;
    public final FluidStack fluidOutput;
    public final FluidStack fluidInput;
    int totalProcessTime;
    public final float torque;

    public GasTurbineRecipe(FluidStack fluidOutput, FluidStack fluidInput, int time) { this(fluidOutput, fluidInput, time, defaultTorque()); }

    public GasTurbineRecipe(FluidStack fluidOutput, FluidStack fluidInput, int time, float torque) {
        this.fluidOutput = fluidOutput;
        this.fluidInput = fluidInput;
        this.totalProcessTime = (int) Math.floor(time * timeModifier);
        this.fluidInputList = Lists.newArrayList(this.fluidInput);
        this.fluidOutputList = Lists.newArrayList();
        if (this.fluidOutput != null) { this.fluidOutputList.add(this.fluidOutput); }
        this.torque = torque;
    }

    public static float defaultTorque() { return Multiblocks.gasTurbine.gasTurbine_torque; }

    public static ArrayList<GasTurbineRecipe> recipeList = new ArrayList<>();
    private static final Map<Fluid, GasTurbineRecipe> fuelMap = new HashMap<>();

    public static void addFuel(FluidStack fluidOutput, FluidStack fluidInput, int time) { addFuel(new GasTurbineRecipe(fluidOutput, fluidInput, time)); }

    public static void addFuel(FluidStack fluidOutput, FluidStack fluidInput, int time, float torque) { addFuel(new GasTurbineRecipe(fluidOutput, fluidInput, time, torque)); }

    public static GasTurbineRecipe addFuel(GasTurbineRecipe recipe) {
        recipeList.add(recipe);
        fuelMap.put(recipe.fluidInput.getFluid(), recipe);
        return recipe;
    }

    public static void removeFuel(FluidStack fluidInput) {
        recipeList.removeIf(recipe -> recipe != null && recipe.fluidInput.isFluidEqual(fluidInput));
        fuelMap.values().removeIf(recipe -> recipe != null && recipe.fluidInput.isFluidEqual(fluidInput));
    }

    public static GasTurbineRecipe findFuel(FluidStack fluidInput) {
        if (fluidInput == null) { return null; }
        GasTurbineRecipe recipe = fuelMap.get(fluidInput.getFluid());
        if (recipe != null && fluidInput.containsFluid(recipe.fluidInput)) { return recipe; }
        for (GasTurbineRecipe r : recipeList) {
            if (r.fluidInput != null && fluidInput.containsFluid(r.fluidInput)) { return r; }
        }
        return null;
    }

    @Override public int getMultipleProcessTicks() { return 0; }

    @Override public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setTag("input", fluidInput.writeToNBT(new NBTTagCompound()));
        return nbt;
    }

    public static GasTurbineRecipe loadFromNBT(NBTTagCompound nbt) {
        FluidStack fluidInput = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("input"));
        return findFuel(fluidInput);
    }

    @Override public int getTotalProcessTime() { return this.totalProcessTime; }
}
