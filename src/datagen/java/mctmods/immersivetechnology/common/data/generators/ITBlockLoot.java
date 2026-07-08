package mctmods.immersivetechnology.common.data.generators;

import mctmods.immersivetechnology.core.util.loot.ITBEDropLootEntry;
import mctmods.immersivetechnology.core.registration.ITBlocks;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ITBlockLoot extends BlockLootSubProvider {
    public ITBlockLoot(HolderLookup.Provider registries) { super(Set.of(), FeatureFlags.DEFAULT_FLAGS, registries); }

    @Override protected void generate() {
        registerEntity(ITBlocks.Metal.BARREL_CREATIVE.getRegObject());
        registerEntity(ITBlocks.Metal.BARREL_OPEN.getRegObject());
        registerEntity(ITBlocks.Metal.BARREL_STEEL.getRegObject());
        dropSelf(ITBlocks.Metal.ADVANCED_COKE_OVEN_BASEHEATER.get());
        dropSelf(ITBlocks.Wooden.CRATE_CREATIVE.get());
        dropSelf(ITBlocks.Connector.CONNECTOR_TIMER.get());
        dropSelf(ITBlocks.Metal.HEAT_CREATIVE.get());
        dropSelf(ITBlocks.Metal.ROTOR_CREATIVE.get());
        dropSelf(ITBlocks.Metal.TRASH_ENERGY.get());
        dropSelf(ITBlocks.Metal.TRASH_FLUID.get());
        dropSelf(ITBlocks.Metal.TRASH_ITEM.get());
        dropSelf(ITBlocks.Metal.VALVE_FLUID.get());
        dropSelf(ITBlocks.Metal.VALVE_LIMITER.get());
        dropSelf(ITBlocks.Metal.VALVE_LOAD.get());
        dropSelf(ITBlocks.Metal.TECHNOLOGY_ENGINEERING.get());
        dropSelf(ITBlocks.Stone.REINFORCED_COKE_BRICK.get());
        dropSelf(ITBlocks.Stone.SLAB_REINFORCED_COKE_BRICK.get());
        registerMultiblocksNoDrop();
    }

    private void registerEntity(Supplier<? extends Block> block) {
        LootPool.Builder pool = createPoolBuilder().add(ITBEDropLootEntry.builder());
        add(block.get(), LootTable.lootTable().withPool(pool));
    }

    private void registerMultiblocksNoDrop() {
        add(ITMultiblockProvider.ADVANCED_COKE_OVEN.block().get(), noDrop());
        add(ITMultiblockProvider.ALTERNATOR.block().get(), noDrop());
        add(ITMultiblockProvider.BOILER_LIQUID.block().get(), noDrop());
        add(ITMultiblockProvider.BOILER_SOLID.block().get(), noDrop());
        add(ITMultiblockProvider.BOILER_TANK.block().get(), noDrop());
        add(ITMultiblockProvider.COOLING_TOWER.block().get(), noDrop());
        add(ITMultiblockProvider.DISTILLER.block().get(), noDrop());
        add(ITMultiblockProvider.ELECTROLYTIC_CRUCIBLE_BATTERY.block().get(), noDrop());
        add(ITMultiblockProvider.GAS_TURBINE.block().get(), noDrop());
        add(ITMultiblockProvider.HEAT_EXCHANGER.block().get(), noDrop());
        add(ITMultiblockProvider.MELTING_CRUCIBLE.block().get(), noDrop());
        add(ITMultiblockProvider.RADIATOR.block().get(), noDrop());
        add(ITMultiblockProvider.RADIATOR_HORIZONTAL.block().get(), noDrop());
        add(ITMultiblockProvider.SOLAR_MELTER.block().get(), noDrop());
        add(ITMultiblockProvider.SOLAR_REFLECTOR.block().get(), noDrop());
        add(ITMultiblockProvider.SOLAR_TOWER.block().get(), noDrop());
        add(ITMultiblockProvider.STEAM_TURBINE.block().get(), noDrop());
        add(ITMultiblockProvider.STEEL_SHEETMETAL_TANK.block().get(), noDrop());
    }

    private LootPool.Builder createPoolBuilder() { return LootPool.lootPool().when(ExplosionCondition.survivesExplosion()); }

    @Override @NotNull protected Set<Block> getKnownBlocks() { return ITBlocks.REGISTER.getEntries().stream().map(DeferredHolder::get).collect(Collectors.toSet()); }
}
