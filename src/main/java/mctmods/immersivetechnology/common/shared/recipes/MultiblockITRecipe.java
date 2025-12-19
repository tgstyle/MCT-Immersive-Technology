package mctmods.immersivetechnology.common.shared.recipes;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.IJEIRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mctmods.immersivetechnology.common.shared.interfaces.IITMultiblockRecipe;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

public abstract class MultiblockITRecipe implements IITMultiblockRecipe, IJEIRecipe {
    protected List<IngredientStack> inputList;
    protected NonNullList<ItemStack> outputList;
    protected List<FluidStack> fluidInputList;
    protected List<FluidStack> fluidOutputList;
    int totalProcessTime;
    protected int totalProcessEnergy;
    public ArrayList<ItemStack>[] jeiItemInputList;
    protected List<ItemStack> jeiTotalItemInputList;
    public ArrayList<ItemStack>[] jeiItemOutputList;
    protected List<ItemStack> jeiTotalItemOutputList;
    protected List<FluidStack> jeiFluidInputList;
    protected List<FluidStack> jeiFluidOutputList;

    public List<IngredientStack> getItemInputs() { return this.inputList; }

    public NonNullList<ItemStack> getItemOutputs() { return this.outputList; }

    public List<FluidStack> getFluidInputs() { return this.fluidInputList; }

    public List<FluidStack> getFluidOutputs() { return this.fluidOutputList; }

    public int getTotalProcessTime() { return this.totalProcessTime; }

    public int getTotalProcessEnergy() { return this.totalProcessEnergy; }

    public void setupJEI() {
        if (this.inputList != null) {
            @SuppressWarnings("unchecked")
            ArrayList<ItemStack>[] tempInput = (ArrayList<ItemStack>[]) new ArrayList[this.inputList.size()];
            this.jeiItemInputList = tempInput;
            this.jeiTotalItemInputList = new ArrayList<>();

            for(int i = 0; i < this.inputList.size(); ++i) {
                IngredientStack ingr = this.inputList.get(i);
                ArrayList<ItemStack> list = new ArrayList<>();
                if (ingr.oreName != null) {
                    for(ItemStack stack : OreDictionary.getOres(ingr.oreName)) { list.add(ApiUtils.copyStackWithAmount(stack, ingr.inputSize)); }
                } else if (ingr.stackList != null) {
                    for(ItemStack stack : ingr.stackList) { list.add(ApiUtils.copyStackWithAmount(stack, ingr.inputSize)); }
                } else { list.add(ApiUtils.copyStackWithAmount(ingr.stack, ingr.inputSize)); }

                this.jeiItemInputList[i] = list;
                this.jeiTotalItemInputList.addAll(list);
            }
        } else { this.jeiTotalItemInputList = Collections.emptyList(); }

        if (this.outputList != null) {
            @SuppressWarnings("unchecked")
            ArrayList<ItemStack>[] tempOutput = (ArrayList<ItemStack>[]) new ArrayList[this.outputList.size()];
            this.jeiItemOutputList = tempOutput;
            this.jeiTotalItemOutputList = new ArrayList<>();

            for(int i = 0; i < this.outputList.size(); ++i) {
                ItemStack s = this.outputList.get(i);
                ArrayList<ItemStack> list = Lists.newArrayList(!s.isEmpty() ? s.copy() : ItemStack.EMPTY);
                this.jeiItemOutputList[i] = list;
                this.jeiTotalItemOutputList.addAll(list);
            }
        } else { this.jeiTotalItemOutputList = Collections.emptyList(); }

        if (this.fluidInputList != null) {
            this.jeiFluidInputList = new ArrayList<>();

            for (FluidStack fs : this.fluidInputList) {
                if (fs != null) {this.jeiFluidInputList.add(fs.copy());}
            }
        } else { this.jeiFluidInputList = Collections.emptyList(); }

        if (this.fluidOutputList != null) {
            this.jeiFluidOutputList = new ArrayList<>();

            for (FluidStack fluidStack : this.fluidOutputList) {
                if (fluidStack != null) { this.jeiFluidOutputList.add(fluidStack.copy()); }
            }
        } else { this.jeiFluidOutputList = Collections.emptyList(); }

    }

    public List<ItemStack> getJEITotalItemInputs() { return this.jeiTotalItemInputList; }

    public List<ItemStack> getJEITotalItemOutputs() { return this.jeiTotalItemOutputList; }

    public List<FluidStack> getJEITotalFluidInputs() { return this.jeiFluidInputList; }

    public List<FluidStack> getJEITotalFluidOutputs() { return this.jeiFluidOutputList; }
}