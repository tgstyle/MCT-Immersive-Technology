package mctmods.immersivetechnology.api.crafting;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.common.util.ListUtils;
import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
        this.totalProcessTime = (int) Math.floor(time * timeModifier);
        this.totalProcessEnergy = (int) Math.floor(energy * energyModifier);
        this.fluidInputList = Lists.newArrayList(fluidInput0);
        this.fluidOutputList = Lists.newArrayList(fluidOutput0);
        if (fluidOutput1 != null) { this.fluidOutputList.add(fluidOutput1); }
        if (fluidOutput2 != null) { this.fluidOutputList.add(fluidOutput2); }
        this.outputList = ListUtils.fromItems(itemOutput);
    }

    public static ArrayList<ElectrolyticCrucibleBatteryRecipe> recipeList = new ArrayList<>();
    private static final Map<Fluid, ElectrolyticCrucibleBatteryRecipe> recipeMap = new HashMap<>();

    public static ElectrolyticCrucibleBatteryRecipe addRecipe(FluidStack fluidOutput0, FluidStack fluidOutput1, FluidStack fluidOutput2, ItemStack itemOutput, FluidStack fluidInput0, int energy, int time) { return addRecipe(new ElectrolyticCrucibleBatteryRecipe(fluidOutput0, fluidOutput1, fluidOutput2, itemOutput, fluidInput0, energy, time)); }

    public static ElectrolyticCrucibleBatteryRecipe addRecipe(ElectrolyticCrucibleBatteryRecipe recipe) {
        recipeList.add(recipe);
        recipeMap.put(recipe.fluidInput0.getFluid(), recipe);
        return recipe;
    }

    public static void removeRecipe(FluidStack fluidInput0) {
        recipeList.removeIf(recipe -> recipe != null && recipe.fluidInput0.isFluidEqual(fluidInput0));
        recipeMap.values().removeIf(recipe -> recipe != null && recipe.fluidInput0.isFluidEqual(fluidInput0));
    }

    public static ElectrolyticCrucibleBatteryRecipe findRecipe(FluidStack fluidInput0) {
        if (fluidInput0 == null || fluidInput0.getFluid() == null) { return null; }
        ElectrolyticCrucibleBatteryRecipe recipe = recipeMap.get(fluidInput0.getFluid());
        if (recipe != null && fluidInput0.containsFluid(recipe.fluidInput0)) { return recipe; }
        for (ElectrolyticCrucibleBatteryRecipe r : recipeList) {
            if (r.fluidInput0 != null && fluidInput0.containsFluid(r.fluidInput0)) { return r; }
        }
        return null;
    }

    @Override public int getMultipleProcessTicks() { return 0; }

    @Override public int getTotalProcessEnergy() { return this.totalProcessEnergy; }

    @Override public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setTag("input0", fluidInput0.writeToNBT(new NBTTagCompound()));
        return nbt;
    }

    public static ElectrolyticCrucibleBatteryRecipe loadFromNBT(NBTTagCompound nbt) {
        FluidStack fluidInput0 = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("input0"));
        return findRecipe(fluidInput0);
    }

    @Override public int getTotalProcessTime() { return this.totalProcessTime; }
}
