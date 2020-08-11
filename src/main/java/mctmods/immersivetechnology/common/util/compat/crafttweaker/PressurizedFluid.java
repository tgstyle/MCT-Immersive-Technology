package mctmods.immersivetechnology.common.util.compat.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.api.liquid.ILiquidStack;
import mctmods.immersivetechnology.common.ITContent;
import net.minecraftforge.fluids.Fluid;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.immersivetechnology.PressurizedFluid")
public class PressurizedFluid {

    @ZenMethod
    public static void add(ILiquidStack fluid) {
        Fluid actualFluid = CraftTweakerHelper.toFluidStack(fluid).getFluid();
        if(ITContent.normallyPressurized.contains(actualFluid)) return;
        CraftTweakerAPI.apply(new Add(actualFluid));
    }

    @ZenMethod
    public static void remove(ILiquidStack fluid) {
        Fluid actualFluid = CraftTweakerHelper.toFluidStack(fluid).getFluid();
        if(!ITContent.normallyPressurized.contains(actualFluid)) return;
        CraftTweakerAPI.apply(new Remove(actualFluid));
    }

    private static class Add implements IAction {
        public Fluid fluid;
        public Add(Fluid fluid) {
            this.fluid = fluid;
        }

        @Override
        public void apply() {
            ITContent.normallyPressurized.add(fluid);
        }

        @Override
        public String describe() {
            return "Adding Naturally Pressurized Fluid " + fluid.getName();
        }
    }

    private static class Remove implements IAction {
        public Fluid fluid;
        public Remove(Fluid fluid) {
            this.fluid = fluid;
        }

        @Override
        public void apply() {
            ITContent.normallyPressurized.remove(fluid);
        }

        @Override
        public String describe() {
            return "Removing Naturally Pressurized Fluid " + fluid.getName();
        }
    }

}