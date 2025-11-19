package mctmods.immersivetechnology.api.crafting;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MeltingCrucibleRecipe extends MultiblockRecipe {
    public static float timeModifier = 1;
    public static float energyModifier = 1;

    public final FluidStack fluidOutput;
    public final IngredientStack itemInput;

    int totalProcessTime;
    int totalProcessEnergy;

    public MeltingCrucibleRecipe(FluidStack fluidOutput, Object itemInput, int energy, int time) {
        this.fluidOutput = fluidOutput;
        this.itemInput = ApiUtils.createIngredientStack(itemInput);
        this.inputList = new ArrayList<>();
        this.inputList.add(this.itemInput);
        this.fluidOutputList = Lists.newArrayList(this.fluidOutput);
        this.totalProcessTime = (int) Math.floor(time * timeModifier);
        this.totalProcessEnergy = (int) Math.floor(energy * energyModifier);
    }

    public static ArrayList<MeltingCrucibleRecipe> recipeList = new ArrayList<>();

    private static final Map<ItemStack, MeltingCrucibleRecipe> recipeMap = new HashMap<>();

    public static MeltingCrucibleRecipe addRecipe(FluidStack fluidOutput, Object itemInput, int energy, int time) {
        MeltingCrucibleRecipe recipe = new MeltingCrucibleRecipe(fluidOutput, itemInput, energy, time);
        recipeList.add(recipe);
        for (ItemStack stack : recipe.itemInput.stackList) {
            ItemStack key = stack.copy();
            key.setCount(1);
            if (!recipe.itemInput.useNBT) {
                key.setTagCompound(null);
            }
            recipeMap.put(key, recipe);
        }
        return recipe;
    }

    public static MeltingCrucibleRecipe findRecipe(ItemStack itemInput) {
        if (itemInput.isEmpty()) {
            return null;
        }
        ItemStack key = itemInput.copy();
        key.setCount(1);
        MeltingCrucibleRecipe recipe = recipeMap.get(key);
        if (recipe != null && recipe.itemInput.matches(itemInput)) {
            return recipe;
        }
        key = key.copy();
        key.setTagCompound(null);
        recipe = recipeMap.get(key);
        if (recipe != null && recipe.itemInput.matches(itemInput)) {
            return recipe;
        }
        for (MeltingCrucibleRecipe r : recipeList) {
            if (r.itemInput.matches(itemInput)) {
                return r;
            }
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
        nbt.setTag("input", itemInput.writeToNBT(new NBTTagCompound()));
        return nbt;
    }

    public static MeltingCrucibleRecipe loadFromNBT(NBTTagCompound nbt) {
        IngredientStack itemInput = IngredientStack.readFromNBT(nbt.getCompoundTag("input"));
        for (MeltingCrucibleRecipe recipe : recipeList) {
            if (recipe.itemInput.equals(itemInput)) {
                return recipe;
            }
        }
        return null;
    }

    @Override
    public int getTotalProcessTime() {
        return this.totalProcessTime;
    }

    @Override
    public void setupJEI() {
        super.setupJEI();
        jeiFluidOutputList = new ArrayList<>();
        jeiFluidOutputList.add(fluidOutput.copy());
    }
}
