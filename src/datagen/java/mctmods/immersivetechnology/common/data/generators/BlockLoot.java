package mctmods.immersivetechnology.common.data.generators;

import com.immersiveconvergence.api.loot.BlockEntityDropLootEntry;
import mctmods.immersivetechnology.core.registration.ModBlocks;
import mctmods.immersivetechnology.core.registration.ModItems;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.TagEntry;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.neoforged.neoforge.registries.DeferredHolder;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BlockLoot extends BlockLootSubProvider {
    public BlockLoot(HolderLookup.Provider registries) { super(Set.of(), FeatureFlags.DEFAULT_FLAGS, registries); }

    @Override protected void generate() {
        registerEntity(ModBlocks.Metal.BARREL_CREATIVE.getRegObject());
        registerEntity(ModBlocks.Wooden.CRATE_CREATIVE.getRegObject());
        registerEntity(ModBlocks.Metal.BARREL_OPEN.getRegObject());
        registerEntity(ModBlocks.Metal.BARREL_STEEL.getRegObject());
        dropSelf(ModBlocks.Metal.ADVANCED_COKE_OVEN_BASEHEATER.get());
        dropSelf(ModBlocks.Metal.TRASH_ENERGY.get());
        dropSelf(ModBlocks.Metal.TRASH_FLUID.get());
        dropSelf(ModBlocks.Metal.TRASH_ITEM.get());
        dropSelf(ModBlocks.Metal.TECHNOLOGY_ENGINEERING.get());
        dropSelf(ModBlocks.Metal.GRAPHITE_MODERATOR.get());
        dropSelf(ModBlocks.Metal.NUCLEAR_ENGINEERING.get());
        dropSelf(ModBlocks.Metal.REACTOR_CLADDING.get());
        dropSelf(ModBlocks.Metal.SOLAR_PANEL.get());
        dropSelf(ModBlocks.Metal.STORAGE_STAINLESS_STEEL.get());
        dropSelf(ModBlocks.Metal.STORAGE_CHROMIUM.get());
        dropSelf(ModBlocks.Metal.STORAGE_MAGNESIUM.get());
        dropSelf(ModBlocks.Metal.SHEETMETAL_STAINLESS_STEEL.get());
        add(ModBlocks.Metal.ORE_CHROMIUM.get(), createOreDrop(ModBlocks.Metal.ORE_CHROMIUM.get(), ModItems.RAW_CHROMIUM.get()));
        add(ModBlocks.Metal.DEEPSLATE_ORE_CHROMIUM.get(), createOreDrop(ModBlocks.Metal.DEEPSLATE_ORE_CHROMIUM.get(), ModItems.RAW_CHROMIUM.get()));
        add(ModBlocks.Metal.ORE_MAGNESIUM.get(), createOreDrop(ModBlocks.Metal.ORE_MAGNESIUM.get(), ModItems.RAW_MAGNESIUM.get()));
        add(ModBlocks.Metal.DEEPSLATE_ORE_MAGNESIUM.get(), createOreDrop(ModBlocks.Metal.DEEPSLATE_ORE_MAGNESIUM.get(), ModItems.RAW_MAGNESIUM.get()));
        dropSelf(ModBlocks.Stone.REINFORCED_COKE_BRICK.get());
        dropSelf(ModBlocks.Stone.SLAB_REINFORCED_COKE_BRICK.get());
        ModBlocks.NEEDS_IE.forEach(entry -> add(entry.get(), LootTable.lootTable().withPool(createPoolBuilder().add(TagEntry.tagContents(ModItemTags.drops(entry))))));
        registerMultiblocksNoDrop();
    }

    private void registerEntity(Supplier<? extends Block> block) {
        LootPool.Builder pool = createPoolBuilder().add(BlockEntityDropLootEntry.builder());
        add(block.get(), LootTable.lootTable().withPool(pool));
    }

    private void registerMultiblocksNoDrop() {
        add(MultiblockRegistry.ADVANCED_COKE_OVEN.block().get(), noDrop());
        add(MultiblockRegistry.ALTERNATOR.block().get(), noDrop());
        add(MultiblockRegistry.BOILER_LIQUID.block().get(), noDrop());
        add(MultiblockRegistry.BOILER_SOLID.block().get(), noDrop());
        add(MultiblockRegistry.BOILER_TANK.block().get(), noDrop());
        add(MultiblockRegistry.COOLING_TOWER.block().get(), noDrop());
        add(MultiblockRegistry.DISTILLER.block().get(), noDrop());
        add(MultiblockRegistry.ELECTROLYTIC_CRUCIBLE_BATTERY.block().get(), noDrop());
        add(MultiblockRegistry.GAS_TURBINE.block().get(), noDrop());
        add(MultiblockRegistry.HEAT_EXCHANGER.block().get(), noDrop());
        add(MultiblockRegistry.MELTING_CRUCIBLE.block().get(), noDrop());
        add(MultiblockRegistry.RADIATOR.block().get(), noDrop());
        add(MultiblockRegistry.RADIATOR_HORIZONTAL.block().get(), noDrop());
        add(MultiblockRegistry.SOLAR_COLLECTOR.block().get(), noDrop());
        add(MultiblockRegistry.SOLAR_MELTER.block().get(), noDrop());
        add(MultiblockRegistry.SOLAR_REFLECTOR.block().get(), noDrop());
        add(MultiblockRegistry.SOLAR_TOWER.block().get(), noDrop());
        add(MultiblockRegistry.STEAM_TURBINE.block().get(), noDrop());
        add(MultiblockRegistry.STEEL_SHEETMETAL_TANK.block().get(), noDrop());
        add(MultiblockRegistry.STYLENOX.block().get(), noDrop());
    }

    private LootPool.Builder createPoolBuilder() { return LootPool.lootPool().when(ExplosionCondition.survivesExplosion()); }

    @Override @Nonnull protected Set<Block> getKnownBlocks() { return ModBlocks.REGISTER.getEntries().stream().map(DeferredHolder::get).collect(Collectors.toSet()); }
}
