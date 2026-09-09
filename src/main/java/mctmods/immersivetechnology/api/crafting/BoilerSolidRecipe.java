package mctmods.immersivetechnology.api.crafting;

import com.google.common.collect.Lists;
import com.immersiveconvergence.api.crafting.ICIngredientStack;
import com.immersiveconvergence.api.crafting.MultiblockRecipeBase;
import com.immersiveconvergence.core.ICCommonConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import java.util.ArrayList;

public class BoilerSolidRecipe extends MultiblockRecipeBase {
    public final ICIngredientStack itemInput;
    public final double heatPerTick;
    public final double targetHeat;

    public BoilerSolidRecipe(Object itemInput, double heatPerTick, double targetHeat) {
        this.itemInput = ICIngredientStack.of(itemInput);
        this.heatPerTick = heatPerTick;
        this.targetHeat = Math.min(targetHeat, ICCommonConfig.heat.maxHeat);
        this.inputList = Lists.newArrayList(this.itemInput);
    }

    public static ArrayList<BoilerSolidRecipe> fuelList = new ArrayList<>();

    public static BoilerSolidRecipe addFuel(Object itemInput, double heatPerTick, double targetHeat) { return addFuel(new BoilerSolidRecipe(itemInput, heatPerTick, targetHeat)); }

    public static BoilerSolidRecipe addFuel(BoilerSolidRecipe recipe) {
        fuelList.add(recipe);
        return recipe;
    }

    public static void removeFuel(ItemStack itemInput) { fuelList.removeIf(recipe -> recipe != null && recipe.itemInput.matchesItemStackIgnoringSize(itemInput)); }

    public static BoilerSolidRecipe findFuel(ItemStack itemInput) {
        if (itemInput.isEmpty()) { return null; }
        for (BoilerSolidRecipe r : fuelList) {
            if (r.itemInput.matchesItemStackIgnoringSize(itemInput)) { return r; }
        }
        return null;
    }

    @Override public int getMultipleProcessTicks() { return 0; }

    @Override public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setTag("input", itemInput.writeToNBT(new NBTTagCompound()));
        return nbt;
    }

    @Override public int getTotalProcessTime() { return 0; }
}
