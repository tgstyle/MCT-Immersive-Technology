package mctmods.immersivetechnology.api.crafting;

import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collections;
import java.util.List;

public class DummyRecipe implements IMultiblockRecipe {
    @Override
    public List<IngredientStack> getItemInputs() { return Collections.emptyList(); }

    @Override
    public List<FluidStack> getFluidInputs() { return Collections.emptyList(); }

    @Override
    public NonNullList<ItemStack> getItemOutputs() { return NonNullList.create(); }

    @Override
    public List<FluidStack> getFluidOutputs() { return Collections.emptyList(); }

    @Override
    public NonNullList<ItemStack> getActualItemOutputs(TileEntity te) { return NonNullList.create(); }

    @Override
    public List<FluidStack> getActualFluidOutputs(TileEntity te) { return Collections.emptyList(); }

    @Override
    public int getTotalProcessTime() { return 0; }

    @Override
    public int getTotalProcessEnergy() { return 0; }

    @Override
    public int getMultipleProcessTicks() { return 1; }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) { return nbt; }

    @SuppressWarnings("unused")
    public static DummyRecipe loadFromNBT(NBTTagCompound tag) { return new DummyRecipe(); }
}
