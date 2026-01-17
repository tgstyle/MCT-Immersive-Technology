package mctmods.immersivetechnology.api.crafting;

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

    int totalProcessTime;

    public MeltingCrucibleRecipe(FluidStack fluidOutput, Object itemInput, int time) {
        this.fluidOutput = fluidOutput;
        this.itemInput = ApiUtils.createIngredientStack(itemInput);
        this.inputList = new ArrayList<>();
        this.inputList.add(this.itemInput);
        this.fluidOutputList = Lists.newArrayList(this.fluidOutput);
        this.totalProcessTime = (int) Math.floor(time * timeModifier);
    }

    public static ArrayList<MeltingCrucibleRecipe> recipeList = new ArrayList<>();

    public static MeltingCrucibleRecipe addRecipe(FluidStack fluidOutput, Object itemInput, int time) {
        MeltingCrucibleRecipe recipe = new MeltingCrucibleRecipe(fluidOutput, itemInput, time);
        recipeList.add(recipe);
        return recipe;
    }

    public static MeltingCrucibleRecipe findRecipe(ItemStack itemInput) {
        if (itemInput.isEmpty()) {
            return null;
        }
        for (MeltingCrucibleRecipe r : recipeList) {
            if (r.itemInput.matches(itemInput)) {
                return r;
            }
        }
        return null;
    }

    public static MeltingCrucibleRecipe findRecipe(IngredientStack itemInput) {
        if (itemInput == null) {
            return null;
        }
        for (MeltingCrucibleRecipe r : recipeList) {
            if (r.itemInput.equals(itemInput)) {
                return r;
            }
        }
        return null;
    }

    @Override public int getMultipleProcessTicks() {
        return 0;
    }

    @Override public int getTotalProcessTime() {
        return this.totalProcessTime;
    }

    @Override public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setTag("input", itemInput.writeToNBT(new NBTTagCompound()));
        return nbt;
    }

    public static MeltingCrucibleRecipe loadFromNBT(NBTTagCompound nbt) {
        IngredientStack itemInput = IngredientStack.readFromNBT(nbt.getCompoundTag("input"));
        return findRecipe(itemInput);
    }

    @Override public void setupJEI() {
        super.setupJEI();
        jeiFluidOutputList = new ArrayList<>();
        jeiFluidOutputList.add(fluidOutput.copy());
    }
}
