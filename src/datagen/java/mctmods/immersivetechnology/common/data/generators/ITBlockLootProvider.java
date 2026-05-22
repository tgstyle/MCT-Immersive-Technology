package mctmods.immersivetechnology.common.data.generators;

import mctmods.immersivetechnology.core.util.loot.ITBEDropLootEntry;
import mctmods.immersivetechnology.core.registration.ITBlocks;
import mctmods.immersivetechnology.core.registration.ITFluids;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.Collectors;

public class ITBlockLootProvider extends BlockLootSubProvider {
    public ITBlockLootProvider() { super(Set.of(), FeatureFlags.REGISTRY.allFlags()); }

    @Override protected void generate() {
        registerEntity(ITBlocks.Metal.BARREL_CREATIVE.getRegObject());
        registerEntity(ITBlocks.Metal.BARREL_OPEN.getRegObject());
        registerEntity(ITBlocks.Metal.BARREL_STEEL.getRegObject());

        dropSelf(ITBlocks.Wooden.CRATE_CREATIVE.get());
        dropSelf(ITBlocks.Metal.ADVANCED_COKE_OVEN_BASEHEATER.get());
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

        ITFluids.ALL_ENTRIES.forEach(entry -> add(entry.getBlock(), noDrop()));
    }

    private void registerEntity(RegistryObject<? extends Block> block) {
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
        add(ITMultiblockProvider.SOLAR_MELTER.block().get(), noDrop());
        add(ITMultiblockProvider.SOLAR_REFLECTOR.block().get(), noDrop());
        add(ITMultiblockProvider.SOLAR_TOWER.block().get(), noDrop());
        add(ITMultiblockProvider.STEAM_TURBINE.block().get(), noDrop());
        add(ITMultiblockProvider.STEEL_SHEETMETAL_TANK.block().get(), noDrop());
    }

    private LootPool.Builder createPoolBuilder() { return LootPool.lootPool().when(ExplosionCondition.survivesExplosion()); }

    @Override @NotNull protected Set<Block> getKnownBlocks() { return ITBlocks.REGISTER.getEntries().stream().map(RegistryObject::get).collect(Collectors.toSet()); }
}
