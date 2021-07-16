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
import net.minecraftforge.oredict.OreDictionary;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.immersivetechnology.MeltingCrucible")
public class MeltingCrucible {

    @ZenMethod
    public static void addRecipe(ILiquidStack outputFluid, IIngredient inputItem, int energy, int time) {
        FluidStack fluidOut = CraftTweakerHelper.toFluidStack(outputFluid);
        IngredientStack itemIn = CraftTweakerHelper.toIEIngredientStack(inputItem);

        if(inputItem.getItems().isEmpty() || fluidOut == null) return;

        MeltingCrucibleRecipe recipe = new MeltingCrucibleRecipe(fluidOut, itemIn, energy, time);
        CraftTweakerAPI.apply(new MeltingCrucible.Add(recipe));
    }

    private static class Add implements IAction {
        public MeltingCrucibleRecipe recipe;
        public Add(MeltingCrucibleRecipe recipe) {
            this.recipe = recipe;
        }

        @Override
        public void apply() {
            MeltingCrucibleRecipe.recipeList.add(recipe);
        }

        @Override
        public String describe() { return "Adding Melting Crucible recipe for " + recipe.itemInput.toString(); }
    }

    @ZenMethod
    public static void removeRecipe(IItemStack inputItem) {
        ItemStack itemIn = CraftTweakerHelper.toStack(inputItem);
        if(itemIn != null)
            CraftTweakerAPI.apply(new MeltingCrucible.Remove(itemIn));
    }

    private static class Remove implements IAction {
        private final ItemStack itemIn;

        public Remove(ItemStack inputItem) {
            this.itemIn = inputItem;
        }

        @Override
        public void apply() {
            MeltingCrucibleRecipe.recipeList.removeIf(recipe -> recipe != null &&
                    OreDictionary.itemMatches(recipe.itemInput.stack, itemIn, false));
        }

        @Override
        public String describe() {
            return  "Removing Melting Crucible Recipe for " + itemIn.toString();
        }
    }
}
