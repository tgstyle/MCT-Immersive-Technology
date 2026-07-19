package mctmods.immersivetechnology.common.data.generators;

import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.ModFluids;
import mctmods.immersivetechnology.core.registration.ModTags;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModFluidTags extends FluidTagsProvider {
    public ModFluidTags(PackOutput output, CompletableFuture<Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Reference.MODID, existingFileHelper);
    }

    @Override protected void addTags(@NotNull Provider provider) {
        tag(ModTags.fluidChlorine).add(ModFluids.CHLORINE.getStill());
        tag(ModTags.fluidDistilledWater).add(ModFluids.DISTILLED_WATER.getStill());
        tag(ModTags.fluidDistilledWaterForge).add(ModFluids.DISTILLED_WATER.getStill());
        tag(ModTags.fluidExhaustSteam).add(ModFluids.EXHAUST_STEAM.getStill());
        tag(ModTags.fluidFlueGas).add(ModFluids.FLUE_GAS.getStill());
        tag(ModTags.fluidGravelSlurry).add(ModFluids.GRAVEL_SLURRY.getStill());
        tag(ModTags.fluidHeatedGravelSlurry).add(ModFluids.HEATED_GRAVEL.getStill());
        tag(ModTags.fluidHeatedSaltSlurry).add(ModFluids.HEATED_SALT.getStill());
        tag(ModTags.fluidHotWater).add(ModFluids.HOT_WATER.getStill());
        tag(ModTags.fluidHydrogen).add(ModFluids.HYDROGEN.getStill());
        tag(ModTags.fluidMoltenSalt).add(ModFluids.MOLTEN_SALT.getStill());
        tag(ModTags.fluidSaltSlurry).add(ModFluids.SALT_SLURRY.getStill());
        tag(ModTags.fluidSteam).add(ModFluids.STEAM.getStill());
        tag(ModTags.fluidSteamForge).add(ModFluids.STEAM.getStill());
    }
}
