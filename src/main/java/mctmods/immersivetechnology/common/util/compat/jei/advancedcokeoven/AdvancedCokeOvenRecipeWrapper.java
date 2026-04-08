package mctmods.immersivetechnology.common.util.compat.jei.advancedcokeoven;

import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.common.IEContent;

import com.google.common.collect.ImmutableList;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class AdvancedCokeOvenRecipeWrapper implements IRecipeWrapper {

    private final List<ItemStack> inputAlternatives;
    private final ItemStack outputItem;
    private final FluidStack outputFluid;

    public AdvancedCokeOvenRecipeWrapper(CokeOvenRecipe recipe) {
        List<ItemStack> alternatives = new ArrayList<>();
        Object input = recipe.input;

        if (input instanceof Ingredient) {
            for (ItemStack stack : ((Ingredient) input).getMatchingStacks()) {
                if (!stack.isEmpty()) {
                    ItemStack copy = stack.copy();
                    copy.setCount(1);
                    alternatives.add(copy);
                }
            }
        }
        else if (input instanceof ItemStack) {
            ItemStack copy = ((ItemStack) input).copy();
            copy.setCount(1);
            alternatives.add(copy);
        }
        else if (input instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<ItemStack> list = (List<ItemStack>) input;
            for (ItemStack stack : list) {
                if (!stack.isEmpty()) {
                    ItemStack copy = stack.copy();
                    copy.setCount(1);
                    alternatives.add(copy);
                }
            }
        }

        this.inputAlternatives = alternatives;
        this.outputItem = recipe.output.copy();
        this.outputFluid = recipe.creosoteOutput > 0 ? new FluidStack(IEContent.fluidCreosote, recipe.creosoteOutput) : null;
    }

    @Override
    public void getIngredients(@Nonnull IIngredients ingredients) {
        if (!inputAlternatives.isEmpty()) { ingredients.setInputLists(VanillaTypes.ITEM, ImmutableList.of(inputAlternatives)); }
        ingredients.setOutput(VanillaTypes.ITEM, outputItem);
        if (outputFluid != null) { ingredients.setOutput(VanillaTypes.FLUID, outputFluid); }
    }
}
