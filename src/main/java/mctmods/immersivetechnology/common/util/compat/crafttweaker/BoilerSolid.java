package mctmods.immersivetechnology.common.util.compat.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import mctmods.immersivetechnology.api.crafting.BoilerSolidRecipe;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@SuppressWarnings("unused")
@ZenClass("mods.immersivetechnology.BoilerSolid")
public class BoilerSolid {

    @ZenMethod
    public static void addFuel(IIngredient inputItem, double heatPerTick, double targetHeat) {
        Object input = CraftTweakerHelper.toObject(inputItem);

        if (input == null) { return; }

        BoilerSolidRecipe recipe = new BoilerSolidRecipe(input, heatPerTick, targetHeat);
        CraftTweakerAPI.apply(new AddFuel(recipe));
    }

    private static class AddFuel implements IAction {
        public BoilerSolidRecipe recipe;
        public AddFuel(BoilerSolidRecipe recipe) { this.recipe = recipe; }

        @Override public void apply() { BoilerSolidRecipe.addFuel(recipe); }

        @Override public String describe() { return "Adding Solid Boiler Fuel"; }
    }

    @ZenMethod
    public static void removeFuel(IItemStack inputItem) {
        ItemStack stack = CraftTweakerHelper.toStack(inputItem);
        if (!stack.isEmpty()) { CraftTweakerAPI.apply(new RemoveFuel(stack)); }
    }

    private static class RemoveFuel implements IAction {
        private final ItemStack inputItem;

        public RemoveFuel(ItemStack inputItem) { this.inputItem = inputItem; }

        @Override public void apply() { BoilerSolidRecipe.removeFuel(inputItem); }

        @Override public String describe() { return "Removing Solid Boiler Fuel for " + inputItem.getDisplayName(); }
    }
}
