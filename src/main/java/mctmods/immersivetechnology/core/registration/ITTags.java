package mctmods.immersivetechnology.core.registration;

import blusunrize.immersiveengineering.api.utils.TagUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public class ITTags
{
    public static final TagKey<Fluid> fluidSteamForge = TagUtils.createFluidWrapper(forgeLoc("steam"));
    public static final TagKey<Fluid> fluidSteam = TagUtils.createFluidWrapper(modLoc("steam"));
    public static final TagKey<Fluid> fluidFlueGas = TagUtils.createFluidWrapper(modLoc("flue_gas"));

    private static ResourceLocation forgeLoc(String path) {
        return new ResourceLocation("forge", path);
    }

    private static ResourceLocation modLoc(String path) {
        return new ResourceLocation("immersivetechnology", path);
    }
}
