package mctmods.immersivetechnology.common.shared.interfaces;

import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.common.util.Utils;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public interface IITMultiblockRecipe extends IMultiblockRecipe {
    List<IngredientStack> getItemInputs();

    default boolean shouldCheckItemAvailability() {
        return true;
    }

    List<FluidStack> getFluidInputs();

    NonNullList<ItemStack> getItemOutputs();

    default NonNullList<ItemStack> getActualItemOutputs(TileEntity tile) {
        return this.getItemOutputs();
    }

    List<FluidStack> getFluidOutputs();

    default ItemStack getDisplayStack(ItemStack input) {
        for(IngredientStack ingr : this.getItemInputs()) {
            if (ingr.matchesItemStack(input)) {
                return Utils.copyStackWithAmount(input, ingr.inputSize);
            }
        }

        return ItemStack.EMPTY;
    }

    default List<FluidStack> getActualFluidOutputs(TileEntity tile) {
        return this.getFluidOutputs();
    }

    int getTotalProcessTime();

    int getTotalProcessEnergy();

    int getMultipleProcessTicks();

    NBTTagCompound writeToNBT(NBTTagCompound var1);
}