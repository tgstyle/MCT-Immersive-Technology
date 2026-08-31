package mctmods.immersivetechnology.common.util.compat.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.api.liquid.ILiquidStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@SuppressWarnings("unused")
@Deprecated
@ZenClass("mods.immersivetechnology.Boiler")
public class Boiler {
    private static final double DEFAULT_TARGET_HEAT = 600.0;

    @ZenMethod
    public static void addRecipe(ILiquidStack outputFluid, ILiquidStack inputFluid, int time) {
        CraftTweakerAPI.logWarning("mods.immersivetechnology.Boiler is deprecated, use mods.immersivetechnology.BoilerTank instead");
        BoilerTank.addRecipe(outputFluid, inputFluid, time, DEFAULT_TARGET_HEAT);
    }

    @ZenMethod
    public static void addFuel(ILiquidStack inputFluid, int time, double heat) {
        CraftTweakerAPI.logWarning("mods.immersivetechnology.Boiler is deprecated, use mods.immersivetechnology.BoilerLiquid instead");
        BoilerLiquid.addFuel(inputFluid, time, heat / 100, DEFAULT_TARGET_HEAT);
    }

    @ZenMethod
    public static void removeRecipe(ILiquidStack inputFluid) {
        CraftTweakerAPI.logWarning("mods.immersivetechnology.Boiler is deprecated, use mods.immersivetechnology.BoilerTank instead");
        BoilerTank.removeRecipe(inputFluid);
    }

    @ZenMethod
    public static void removeFuel(ILiquidStack inputFluid) {
        CraftTweakerAPI.logWarning("mods.immersivetechnology.Boiler is deprecated, use mods.immersivetechnology.BoilerLiquid instead");
        BoilerLiquid.removeFuel(inputFluid);
    }
}
