package mctmods.immersivetechnology.api.crafting;

import mctmods.immersivetechnology.common.Config;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import java.util.ArrayList;

public class MeltingCrucibleRecipe extends MultiblockRecipe {
    public static float timeModifier = 1;
    public final FluidStack fluidOutput;
    public final IngredientStack itemInput;
    public final double requiredTemp;
    int totalProcessTime;

    public MeltingCrucibleRecipe(FluidStack fluidOutput, Object itemInput, int time) { this(fluidOutput, itemInput, time, defaultTemperature()); }

    public MeltingCrucibleRecipe(FluidStack fluidOutput, Object itemInput, int time, double requiredTemp) {
        this.fluidOutput = fluidOutput;
        this.itemInput = ApiUtils.createIngredientStack(itemInput);
        this.requiredTemp = requiredTemp;
        this.inputList = new ArrayList<>();
        this.inputList.add(this.itemInput);
        this.fluidOutputList = Lists.newArrayList(this.fluidOutput);
        this.totalProcessTime = (int) Math.floor(time * timeModifier);
    }

    public static double defaultTemperature() { return Config.ITConfig.Multiblocks.meltingCrucible.meltingCrucible_heat_workingTemperature; }

    public static ArrayList<MeltingCrucibleRecipe> recipeList = new ArrayList<>();

    public static MeltingCrucibleRecipe addRecipe(FluidStack fluidOutput, Object itemInput, int time) { return addRecipe(new MeltingCrucibleRecipe(fluidOutput, itemInput, time)); }

    public static MeltingCrucibleRecipe addRecipe(FluidStack fluidOutput, Object itemInput, int time, double requiredTemp) { return addRecipe(new MeltingCrucibleRecipe(fluidOutput, itemInput, time, requiredTemp)); }

    public static MeltingCrucibleRecipe addRecipe(MeltingCrucibleRecipe recipe) {
        recipeList.add(recipe);
        return recipe;
    }

    public static void removeRecipe(ItemStack itemInput) { recipeList.removeIf(recipe -> recipe != null && recipe.itemInput.matches(itemInput)); }

    public static MeltingCrucibleRecipe findRecipe(ItemStack itemInput) {
        if (itemInput.isEmpty()) { return null; }
        for (MeltingCrucibleRecipe r : recipeList) {
            if (r.itemInput.matches(itemInput)) { return r; }
        }
        return null;
    }

    public static MeltingCrucibleRecipe findRecipe(IngredientStack itemInput) {
        if (itemInput == null) { return null; }
        for (MeltingCrucibleRecipe r : recipeList) {
            if (r.itemInput.equals(itemInput)) { return r; }
        }
        return null;
    }

    @Override public int getMultipleProcessTicks() { return 0; }

    @Override public int getTotalProcessTime() { return this.totalProcessTime; }

    @Override public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setTag("input", itemInput.writeToNBT(new NBTTagCompound()));
        return nbt;
    }

    public static MeltingCrucibleRecipe loadFromNBT(NBTTagCompound nbt) {
        IngredientStack itemInput = IngredientStack.readFromNBT(nbt.getCompoundTag("input"));
        return findRecipe(itemInput);
    }
}
