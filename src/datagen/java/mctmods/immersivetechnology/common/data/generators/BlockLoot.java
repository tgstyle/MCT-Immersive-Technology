package mctmods.immersivetechnology.common.data.generators;

import com.immersiveconvergence.api.loot.BlockEntityDropLootEntry;
import mctmods.immersivetechnology.core.registration.ModBlocks;
import mctmods.immersivetechnology.core.registration.ModFluids;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.stream.Collectors;

public class BlockLoot extends BlockLootSubProvider {
    public BlockLoot() { super(Set.of(), FeatureFlags.REGISTRY.allFlags()); }

    @Override protected void generate() {
        registerEntity(ModBlocks.Metal.BARREL_CREATIVE.getRegObject());
        registerEntity(ModBlocks.Wooden.CRATE_CREATIVE.getRegObject());
        registerEntity(ModBlocks.Metal.BARREL_OPEN.getRegObject());
        registerEntity(ModBlocks.Metal.BARREL_STEEL.getRegObject());

        dropSelf(ModBlocks.Connector.CONNECTOR_TIMER.get());
        dropSelf(ModBlocks.Metal.ADVANCED_COKE_OVEN_BASEHEATER.get());
        dropSelf(ModBlocks.Metal.TRASH_ENERGY.get());
        dropSelf(ModBlocks.Metal.TRASH_FLUID.get());
        dropSelf(ModBlocks.Metal.TRASH_ITEM.get());
        dropSelf(ModBlocks.Metal.VALVE_FLUID.get());
        dropSelf(ModBlocks.Metal.VALVE_LIMITER.get());
        dropSelf(ModBlocks.Metal.VALVE_LOAD.get());
        dropSelf(ModBlocks.Metal.TECHNOLOGY_ENGINEERING.get());
        dropSelf(ModBlocks.Stone.REINFORCED_COKE_BRICK.get());
        dropSelf(ModBlocks.Stone.SLAB_REINFORCED_COKE_BRICK.get());
        dropSelf(ModBlocks.Connector.CONNECTOR_TIMER.get());

        registerMultiblocksNoDrop();

        ModFluids.ALL_ENTRIES.forEach(entry -> add(entry.getBlock(), noDrop()));
    }

    private void registerEntity(RegistryObject<? extends Block> block) {
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
        add(MultiblockRegistry.SOLAR_MELTER.block().get(), noDrop());
        add(MultiblockRegistry.SOLAR_REFLECTOR.block().get(), noDrop());
        add(MultiblockRegistry.SOLAR_TOWER.block().get(), noDrop());
        add(MultiblockRegistry.STEAM_TURBINE.block().get(), noDrop());
        add(MultiblockRegistry.STEEL_SHEETMETAL_TANK.block().get(), noDrop());
    }

    private LootPool.Builder createPoolBuilder() { return LootPool.lootPool().when(ExplosionCondition.survivesExplosion()); }

    @Override @Nonnull protected Set<Block> getKnownBlocks() { return ModBlocks.REGISTER.getEntries().stream().map(RegistryObject::get).collect(Collectors.toSet()); }
}
