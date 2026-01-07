package mctmods.immersivetechnology.core.registration;

import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

public class ITTags {
    public static final TagKey<Fluid> fluidChlorine = FluidTags.create(modLoc("chlorine"));
    public static final TagKey<Fluid> fluidDistilledWater = FluidTags.create(modLoc("distilled_water"));
    public static final TagKey<Fluid> fluidDistilledWaterForge = FluidTags.create(forgeLoc("distilled_water"));
    public static final TagKey<Fluid> fluidExhaustSteam = FluidTags.create(modLoc("exhaust_steam"));
    public static final TagKey<Fluid> fluidFlueGas = FluidTags.create(modLoc("flue_gas"));
    public static final TagKey<Fluid> fluidGravelSlurry = FluidTags.create(modLoc("gravel_slurry"));
    public static final TagKey<Fluid> fluidHeatedGravelSlurry = FluidTags.create(modLoc("heated_gravel_slurry"));
    public static final TagKey<Fluid> fluidHeatedSaltSlurry = FluidTags.create(modLoc("heated_salt_slurry"));
    public static final TagKey<Fluid> fluidMoltenSalt = FluidTags.create(modLoc("molten_salt"));
    public static final TagKey<Fluid> fluidSaltSlurry = FluidTags.create(modLoc("salt_slurry"));
    public static final TagKey<Fluid> fluidSteam = FluidTags.create(modLoc("steam"));
    public static final TagKey<Fluid> fluidSteamForge = FluidTags.create(forgeLoc("steam"));

    public static final TagKey<Item> salt = ItemTags.create(modLoc("salt"));
    public static final TagKey<Item> saltForge = ItemTags.create(forgeLoc("dusts/salt"));
    public static final TagKey<Item> igniters = ItemTags.create(modLoc("igniters"));
    public static final TagKey<Item> igniters_consume = ItemTags.create(modLoc("igniters_consume"));
    public static final TagKey<Item> formationTools = ItemTags.create(modLoc("formation_tools"));
    public static final TagKey<Item> screwdrivers = ItemTags.create(modLoc("screwdrivers"));

    private static ResourceLocation forgeLoc(String path) { return ResourceLocation.fromNamespaceAndPath("forge", path); }

    private static ResourceLocation modLoc(String path) { return ITLib.rl(path); }
}
