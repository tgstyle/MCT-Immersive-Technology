package mctmods.immersivetechnology.common.data.generators;

import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITFluids;
import mctmods.immersivetechnology.core.registration.ITTags;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ITFluidTags extends FluidTagsProvider {
    public ITFluidTags(PackOutput output, CompletableFuture<Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, ITLib.MODID, existingFileHelper);
    }

    @Override protected void addTags(@NotNull Provider provider) {
        tag(ITTags.fluidChlorine).add(ITFluids.CHLORINE.getStill());
        tag(ITTags.fluidDistilledWater).add(ITFluids.DISTILLED_WATER.getStill());
        tag(ITTags.fluidDistilledWaterForge).add(ITFluids.DISTILLED_WATER.getStill());
        tag(ITTags.fluidExhaustSteam).add(ITFluids.EXHAUST_STEAM.getStill());
        tag(ITTags.fluidFlueGas).add(ITFluids.FLUE_GAS.getStill());
        tag(ITTags.fluidHeatedSaltSlurry).add(ITFluids.HEATED_SALT.getStill());
        tag(ITTags.fluidMoltenSalt).add(ITFluids.MOLTEN_SALT.getStill());
        tag(ITTags.fluidSaltSlurry).add(ITFluids.SALT_SLURRY.getStill());
        tag(ITTags.fluidSteam).add(ITFluids.STEAM.getStill());
        tag(ITTags.fluidSteamForge).add(ITFluids.STEAM.getStill());
    }
}
