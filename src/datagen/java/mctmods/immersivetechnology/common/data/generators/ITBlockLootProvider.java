package mctmods.immersivetechnology.common.data.generators;

import mctmods.immersivetechnology.core.registration.ITBlocks;
import mctmods.immersivetechnology.core.registration.ITFluids;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.Collectors;

public class ITBlockLootProvider extends BlockLootSubProvider {
    public ITBlockLootProvider() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        // Regular blocks: drop themselves
        dropSelf(ITBlocks.MetalDevices.CREATIVE_BARREL.get());
        dropSelf(ITBlocks.MetalDevices.COKE_OVEN_PREHEATER.get());
        dropSelf(ITBlocks.Stone.REINFORCED_COKE_BRICK.get());

        // Multiblocks: no drops
        add(ITMultiblockProvider.BOILER.block().get(), noDrop());
        add(ITMultiblockProvider.DISTILLER.block().get(), noDrop());
        add(ITMultiblockProvider.STEAM_TURBINE.block().get(), noDrop());
        add(ITMultiblockProvider.GAS_TURBINE.block().get(), noDrop());
        add(ITMultiblockProvider.ALTERNATOR.block().get(), noDrop());
        add(ITMultiblockProvider.ADV_COKE_OVEN.block().get(), noDrop());
        add(ITMultiblockProvider.SOLAR_TOWER.block().get(), noDrop()); // Added to handle solar_tower

        // Fluid blocks: no drops
        ITFluids.ALL_ENTRIES.forEach(entry -> add(entry.getBlock(), noDrop()));
    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return ITBlocks.REGISTER.getEntries().stream().map(RegistryObject::get).collect(Collectors.toList());
    }
}
