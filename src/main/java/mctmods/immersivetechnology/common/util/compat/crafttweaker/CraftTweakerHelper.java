package mctmods.immersivetechnology.common.util.compat.crafttweaker;

import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.item.IngredientStack;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.oredict.IOreDictEntry;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.util.compat.ITCompatModule;
import mctmods.immersivetechnology.core.MCTMixinConfig;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import crafttweaker.CraftTweakerAPI;

@SuppressWarnings({"unused", "deprecation"})
public class CraftTweakerHelper extends ITCompatModule {

    @Override public void preInit() {
        if (Multiblocks.enable.enable_boiler) { CraftTweakerAPI.registerClass(Boiler.class); }
        if (Multiblocks.enable.enable_distiller) { CraftTweakerAPI.registerClass(Distiller.class); }
        if (Multiblocks.enable.enable_solarTower) { CraftTweakerAPI.registerClass(SolarTower.class); }
        if (Multiblocks.enable.enable_steamTurbine) { CraftTweakerAPI.registerClass(SteamTurbine.class); }
        if (Multiblocks.enable.enable_coolingTower) { CraftTweakerAPI.registerClass(CoolingTower.class); }
        if (Multiblocks.enable.enable_gasTurbine) { CraftTweakerAPI.registerClass(GasTurbine.class); }
        if (Multiblocks.enable.enable_heatExchanger) { CraftTweakerAPI.registerClass(HeatExchanger.class); }
        if (Multiblocks.enable.enable_highPressureSteamTurbine) { CraftTweakerAPI.registerClass(HighPressureSteamTurbine.class); }
        if (Multiblocks.enable.enable_electrolyticCrucibleBattery) { CraftTweakerAPI.registerClass(ElectrolyticCrucibleBattery.class); }
        if (Multiblocks.enable.enable_meltingCrucible || Multiblocks.enable.enable_solarMelter) { CraftTweakerAPI.registerClass(MeltingCrucible.class); }
        if (Multiblocks.enable.enable_radiator) { CraftTweakerAPI.registerClass(Radiator.class); }
        if (MCTMixinConfig.mixinSettings.replace_IE_pipes) { CraftTweakerAPI.registerClass(PressurizedFluid.class); }
    }

    @Override public void init() { }

    @Override public void postInit() { }

    public static ItemStack toStack(IItemStack iStack) {
        if (iStack == null) { return ItemStack.EMPTY; }
        return (ItemStack)iStack.getInternal();
    }

    public static Object toObject(IIngredient iStack) {
        if (iStack == null) { return null; }
        if (iStack instanceof IOreDictEntry) { return ((IOreDictEntry)iStack).getName(); }
        if (iStack instanceof IItemStack) { return toStack((IItemStack)iStack); }
        if (iStack instanceof IngredientStack) {
            IIngredient ingr = ReflectionHelper.getPrivateValue(IngredientStack.class, (IngredientStack)iStack, "ingredient");
            Object o = toObject(ingr);
            if (o instanceof String) { return new blusunrize.immersiveengineering.api.crafting.IngredientStack((String)o, iStack.getAmount()); }
            return o;
        }
        return null;
    }

    public static blusunrize.immersiveengineering.api.crafting.IngredientStack toIEIngredientStack(IIngredient iStack) {
        if (iStack == null) { return null; }
        if (iStack instanceof IOreDictEntry) { return new blusunrize.immersiveengineering.api.crafting.IngredientStack(((IOreDictEntry)iStack).getName()); }
        if (iStack instanceof IItemStack) { return new blusunrize.immersiveengineering.api.crafting.IngredientStack(toStack((IItemStack)iStack)); }
        if (iStack instanceof IngredientStack) {
            IIngredient ingr = ReflectionHelper.getPrivateValue(IngredientStack.class, (IngredientStack)iStack, "ingredient");
            blusunrize.immersiveengineering.api.crafting.IngredientStack ingrStack = toIEIngredientStack(ingr);
            ingrStack.inputSize = iStack.getAmount();
            return ingrStack;
        }
        return null;
    }

    public static Object[] toObjects(IIngredient[] iStacks) {
        Object[] oA = new Object[iStacks.length];
        for (int i = 0; i < iStacks.length; i++) { oA[i] = toObject(iStacks[i]); }
        return oA;
    }

    public static FluidStack toFluidStack(ILiquidStack iStack) {
        if (iStack == null) { return null; }
        return (FluidStack)iStack.getInternal();
    }
}
