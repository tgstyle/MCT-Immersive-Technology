package mctmods.immersivetechnology.core.registration;

import blusunrize.immersiveengineering.api.utils.TagUtils;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

public class ITTags {
    public static final TagKey<Fluid> fluidChlorine = TagUtils.createFluidWrapper(modLoc("chlorine"));
    public static final TagKey<Fluid> fluidDistilledWater = TagUtils.createFluidWrapper(modLoc("distilled_water"));
    public static final TagKey<Fluid> fluidDistilledWaterForge = TagUtils.createFluidWrapper(forgeLoc("distilled_water"));
    public static final TagKey<Fluid> fluidExhaustSteam = TagUtils.createFluidWrapper(modLoc("exhaust_steam"));
    public static final TagKey<Fluid> fluidFlueGas = TagUtils.createFluidWrapper(modLoc("flue_gas"));
    public static final TagKey<Fluid> fluidHeatedSaltSlurry = TagUtils.createFluidWrapper(modLoc("heated_salt_slurry"));
    public static final TagKey<Fluid> fluidMoltenSalt = TagUtils.createFluidWrapper(modLoc("molten_salt"));
    public static final TagKey<Fluid> fluidSaltSlurry = TagUtils.createFluidWrapper(modLoc("salt_slurry"));
    public static final TagKey<Fluid> fluidSteam = TagUtils.createFluidWrapper(modLoc("steam"));
    public static final TagKey<Fluid> fluidSteamForge = TagUtils.createFluidWrapper(forgeLoc("steam"));

    public static final TagKey<Item> salt = TagUtils.createItemWrapper(modLoc("salt"));
    public static final TagKey<Item> saltForge = TagUtils.createItemWrapper(forgeLoc("dusts/salt"));

    private static ResourceLocation forgeLoc(String path) { return ResourceLocation.fromNamespaceAndPath("forge", path); }

    private static ResourceLocation modLoc(String path) { return ResourceLocation.fromNamespaceAndPath(ITLib.MODID, path); }
}
