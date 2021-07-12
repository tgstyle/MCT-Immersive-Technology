package mctmods.immersivetechnology.common.util.compat.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import mctmods.immersivetechnology.api.crafting.ElectrolyticCrucibleBatteryRecipe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.immersivetechnology.ElectrolyticCrucibleBattery")
public class ElectrolyticCrucibleBattery {

    @ZenMethod
    public static void addRecipe(ILiquidStack outputFluid0, ILiquidStack outputFluid1, ILiquidStack outputFluid2, IItemStack outputItem, ILiquidStack inputFluid0, int energy, int time) {
        FluidStack fluidOut0 = CraftTweakerHelper.toFluidStack(outputFluid0);
        FluidStack fluidOut1 = CraftTweakerHelper.toFluidStack(outputFluid1);
        FluidStack fluidOut2 = CraftTweakerHelper.toFluidStack(outputFluid2);
        ItemStack itemOut = CraftTweakerHelper.toStack(outputItem);
        FluidStack fluidIn0 = CraftTweakerHelper.toFluidStack(inputFluid0);

        if(fluidIn0 == null || fluidOut0 == null) return;

        ElectrolyticCrucibleBatteryRecipe recipe = new ElectrolyticCrucibleBatteryRecipe(fluidOut0, fluidOut1, fluidOut2, itemOut, fluidIn0, energy, time);
        CraftTweakerAPI.apply(new ElectrolyticCrucibleBattery.Add(recipe));
    }

    private static class Add implements IAction {
        public ElectrolyticCrucibleBatteryRecipe recipe;
        public Add(ElectrolyticCrucibleBatteryRecipe recipe) {
            this.recipe = recipe;
        }

        @Override
        public void apply() {
            ElectrolyticCrucibleBatteryRecipe.recipeList.add(recipe);
        }

        @Override
        public String describe() { return "Adding Electrolytic Crucible Battery recipe for " + recipe.fluidInput0.getLocalizedName(); }
    }

    @ZenMethod
    public static void removeRecipe(ILiquidStack inputFluid0) {
        FluidStack fluidIn0 = CraftTweakerHelper.toFluidStack(inputFluid0);
        if(fluidIn0 != null)
            CraftTweakerAPI.apply(new ElectrolyticCrucibleBattery.Remove(fluidIn0));
    }

    private static class Remove implements IAction {
        private final FluidStack inputFluid0;

        public Remove(FluidStack inputFluid0) {
            this.inputFluid0 = inputFluid0;
        }

        @Override
        public void apply() {
            ElectrolyticCrucibleBatteryRecipe.recipeList.removeIf(recipe -> recipe != null && recipe.fluidInput0.isFluidEqual(inputFluid0));
        }

        @Override
        public String describe() {
            return "Removing Electrolytic Crucible Battery Recipe for " + inputFluid0.getLocalizedName();
        }
    }
}
