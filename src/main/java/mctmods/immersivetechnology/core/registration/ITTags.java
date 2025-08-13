package mctmods.immersivetechnology.core.registration;

import blusunrize.immersiveengineering.api.utils.TagUtils;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public class ITTags {
    public static final TagKey<Fluid> fluidChlorine = TagUtils.createFluidWrapper(modLoc("chlorine"));
    public static final TagKey<Fluid> fluidDistilledWater = TagUtils.createFluidWrapper(modLoc("distilled_water"));
    public static final TagKey<Fluid> fluidDistilledWaterForge = TagUtils.createFluidWrapper(forgeLoc("distilled_water"));
    public static final TagKey<Fluid> fluidExhaustSteam = TagUtils.createFluidWrapper(modLoc("exhaust_steam"));
    public static final TagKey<Fluid> fluidFlueGas = TagUtils.createFluidWrapper(modLoc("flue_gas"));
    public static final TagKey<Fluid> fluidHighPressureSteam = TagUtils.createFluidWrapper(modLoc("high_pressure_steam"));
    public static final TagKey<Fluid> fluidHotSpringWater = TagUtils.createFluidWrapper(modLoc("hot_spring_water"));
    public static final TagKey<Fluid> fluidMoltenSalt = TagUtils.createFluidWrapper(modLoc("molten_salt"));
    public static final TagKey<Fluid> fluidMoltenSodium = TagUtils.createFluidWrapper(modLoc("molten_sodium"));
    public static final TagKey<Fluid> fluidSteam = TagUtils.createFluidWrapper(modLoc("steam"));
    public static final TagKey<Fluid> fluidSteamForge = TagUtils.createFluidWrapper(forgeLoc("steam"));
    public static final TagKey<Fluid> fluidSuperheatedMoltenSodium = TagUtils.createFluidWrapper(modLoc("superheated_molten_sodium"));

    private static ResourceLocation forgeLoc(String path) { return ResourceLocation.fromNamespaceAndPath("forge", path); }

    private static ResourceLocation modLoc(String path) { return ResourceLocation.fromNamespaceAndPath(ITLib.MODID, path); }
}
