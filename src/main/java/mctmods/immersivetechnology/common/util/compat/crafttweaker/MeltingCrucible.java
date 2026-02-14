package mctmods.immersivetechnology.common.util.compat.crafttweaker;

import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import mctmods.immersivetechnology.api.crafting.MeltingCrucibleRecipe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@SuppressWarnings("unused")
@ZenClass("mods.immersivetechnology.MeltingCrucible")
public class MeltingCrucible {

    @ZenMethod
    public static void addRecipe(ILiquidStack outputFluid, IIngredient inputItem, int time) {
        FluidStack fluidOut = CraftTweakerHelper.toFluidStack(outputFluid);
        IngredientStack itemIn = CraftTweakerHelper.toIEIngredientStack(inputItem);

        if (fluidOut == null || itemIn == null || itemIn.inputSize <= 0) { return; }

        MeltingCrucibleRecipe recipe = new MeltingCrucibleRecipe(fluidOut, itemIn, time);
        CraftTweakerAPI.apply(new Add(recipe));
    }

    private static class Add implements IAction {
        public MeltingCrucibleRecipe recipe;
        public Add(MeltingCrucibleRecipe recipe) { this.recipe = recipe; }

        @Override public void apply() { MeltingCrucibleRecipe.recipeList.add(recipe); }

        @Override public String describe() {
            String inputName = recipe.itemInput.stack.getDisplayName();
            if (recipe.itemInput.inputSize > 1) inputName += " x" + recipe.itemInput.inputSize;
            return "Adding Melting Crucible recipe: " + inputName + " -> " + recipe.fluidOutput.getLocalizedName();
        }
    }

    @ZenMethod
    public static void removeRecipe(IItemStack inputItem) {
        ItemStack itemIn = CraftTweakerHelper.toStack(inputItem);
        if (!itemIn.isEmpty()) { CraftTweakerAPI.apply(new Remove(itemIn)); }
    }

    private static class Remove implements IAction {
        private final ItemStack inputStack;

        public Remove(ItemStack inputStack) { this.inputStack = inputStack; }

        @Override public void apply() {
            MeltingCrucibleRecipe.recipeList.removeIf(recipe -> recipe != null && recipe.itemInput.matches(inputStack));
        }

        @Override public String describe() { return "Removing Melting Crucible recipes matching " + inputStack.getDisplayName(); }
    }
}
