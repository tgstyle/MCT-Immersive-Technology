package mctmods.immersivetechnology.api.crafting;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
        this.totalProcessTime = (int) Math.floor(time * timeModifier);
        this.totalProcessEnergy = (int) Math.floor(energy * energyModifier);
        this.fluidInputList = Lists.newArrayList(fluidInput0, fluidInput1);
        this.fluidOutputList = Lists.newArrayList(fluidOutput0);
        if (fluidOutput1 != null) this.fluidOutputList.add(fluidOutput1);
    }

    public static ArrayList<HeatExchangerRecipe> recipeList = new ArrayList<>();

    private static final Map<FluidPair, HeatExchangerRecipe> recipeMap = new HashMap<>();
    private static final Map<Fluid, HeatExchangerRecipe> input0Map = new HashMap<>();
    private static final Map<Fluid, HeatExchangerRecipe> input1Map = new HashMap<>();

    public static HeatExchangerRecipe addRecipe(FluidStack fluidOutput0, FluidStack fluidOutput1, FluidStack fluidInput0, FluidStack fluidInput1, int energy, int time) {
        HeatExchangerRecipe recipe = new HeatExchangerRecipe(fluidOutput0, fluidOutput1, fluidInput0, fluidInput1, energy, time);
        recipeList.add(recipe);
        if (fluidInput0 != null) input0Map.put(fluidInput0.getFluid(), recipe);
        if (fluidInput1 != null) input1Map.put(fluidInput1.getFluid(), recipe);
        recipeMap.put(new FluidPair(fluidInput0 != null ? fluidInput0.getFluid() : null, fluidInput1 != null ? fluidInput1.getFluid() : null), recipe);
        return recipe;
    }

    public static HeatExchangerRecipe findRecipe(FluidStack fluidInput0, FluidStack fluidInput1) {
        if (fluidInput0 == null || fluidInput1 == null) {
            return null;
        }
        HeatExchangerRecipe recipe = recipeMap.get(new FluidPair(fluidInput0.getFluid(), fluidInput1.getFluid()));
        if (recipe != null && fluidInput0.containsFluid(recipe.fluidInput0) && fluidInput1.containsFluid(recipe.fluidInput1)) {
            return recipe;
        }
        for (HeatExchangerRecipe r : recipeList) {
            if (r.fluidInput0 != null && fluidInput0.containsFluid(r.fluidInput0) && r.fluidInput1 != null && fluidInput1.containsFluid(r.fluidInput1)) {
                return r;
            }
        }
        return null;
    }

    public static HeatExchangerRecipe findRecipeByFluid0(Fluid fluidInput0) {
        if (fluidInput0 == null) {
            return null;
        }
        return input0Map.get(fluidInput0);
    }

    public static HeatExchangerRecipe findRecipeByFluid1(Fluid fluidInput1) {
        if (fluidInput1 == null) {
            return null;
        }
        return input1Map.get(fluidInput1);
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
        nbt.setTag("input1", fluidInput1.writeToNBT(new NBTTagCompound()));
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
        if (fluidOutput1 != null) {
            jeiFluidOutputList.add(fluidOutput1.copy());
        }
    }

    static class FluidPair {
        private final Fluid fluid0;
        private final Fluid fluid1;

        FluidPair(Fluid f0, Fluid f1) {
            fluid0 = f0;
            fluid1 = f1;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            FluidPair that = (FluidPair) o;
            return Objects.equals(fluid0, that.fluid0) && Objects.equals(fluid1, that.fluid1);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fluid0, fluid1);
        }
    }
}
