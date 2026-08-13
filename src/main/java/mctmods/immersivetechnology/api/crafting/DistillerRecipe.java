package mctmods.immersivetechnology.api.crafting;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.common.util.ListUtils;
import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DistillerRecipe extends MultiblockRecipe {
    public static float energyModifier = 1;
    public static float timeModifier = 1;
    public float chance;
    public final FluidStack fluidOutput;
    public final FluidStack fluidInput;
    public final ItemStack itemOutput;
    int totalProcessTime;
    int totalProcessEnergy;

    public DistillerRecipe(FluidStack fluidOutput, FluidStack fluidInput, ItemStack itemOutput, int energy, int time, float chance) {
        this.fluidOutput = fluidOutput;
        this.fluidInput = fluidInput;
        this.itemOutput = itemOutput;
        this.totalProcessEnergy = (int) Math.floor(energy * energyModifier);
        this.totalProcessTime = (int) Math.floor(time * timeModifier);
        this.chance = chance;
        this.fluidInputList = Lists.newArrayList(this.fluidInput);
        this.fluidOutputList = Lists.newArrayList(this.fluidOutput);
        this.outputList = ListUtils.fromItems(this.itemOutput);
    }

    public static ArrayList<DistillerRecipe> recipeList = new ArrayList<>();
    private static final Map<Fluid, DistillerRecipe> recipeMap = new HashMap<>();

    public static DistillerRecipe addRecipe(FluidStack fluidOutput, FluidStack fluidInput, ItemStack itemOutput, int energy, int time, float chance) { return addRecipe(new DistillerRecipe(fluidOutput, fluidInput, itemOutput, energy, time, chance)); }

    public static DistillerRecipe addRecipe(DistillerRecipe recipe) {
        recipeList.add(recipe);
        recipeMap.put(recipe.fluidInput.getFluid(), recipe);
        return recipe;
    }

    public static void removeRecipe(FluidStack fluidInput) {
        recipeList.removeIf(recipe -> recipe != null && recipe.fluidInput.isFluidEqual(fluidInput));
        recipeMap.values().removeIf(recipe -> recipe != null && recipe.fluidInput.isFluidEqual(fluidInput));
    }

    public static DistillerRecipe findRecipe(FluidStack fluidInput) {
        if (fluidInput == null) { return null; }
        DistillerRecipe recipe = recipeMap.get(fluidInput.getFluid());
        if (recipe != null && fluidInput.containsFluid(recipe.fluidInput)) { return recipe; }
        for (DistillerRecipe r : recipeList) {
            if (r.fluidInput != null && fluidInput.containsFluid(r.fluidInput)) { return r; }
        }
        return null;
    }

    @Override public int getMultipleProcessTicks() { return 0; }

    @Override public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setTag("input", fluidInput.writeToNBT(new NBTTagCompound()));
        return nbt;
    }

    public static DistillerRecipe loadFromNBT(NBTTagCompound nbt) {
        FluidStack fluidInput = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("input"));
        return findRecipe(fluidInput);
    }

    @Override public int getTotalProcessTime() { return this.totalProcessTime; }

    @Override public int getTotalProcessEnergy() { return this.totalProcessEnergy; }

    @Override public NonNullList<ItemStack> getActualItemOutputs(TileEntity tile) {
        if (tile.getWorld().rand.nextFloat() <= chance) { return outputList; }
        else { return ListUtils.fromItems(); }
    }
}
