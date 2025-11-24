package mctmods.immersivetechnology.common.data.generators;

import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import mctmods.immersivetechnology.common.util.loot.ITDropInventoryLootEntry;
import mctmods.immersivetechnology.common.util.loot.ITMultiblockDropsLootContainer;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ITBlockLootProvider extends BlockLootSubProvider {
    public ITBlockLootProvider() { super(Set.of(), FeatureFlags.REGISTRY.allFlags()); }

    @Override
    protected void generate() {
        dropSelf(ITBlocks.MetalDevices.BARREL_CREATIVE.get());
        dropSelf(ITBlocks.MetalDevices.BARREL_OPEN.get());
        dropSelf(ITBlocks.MetalDevices.BARREL_STEEL.get());
        dropSelf(ITBlocks.MetalDevices.TRASH_ENERGY.get());
        dropSelf(ITBlocks.MetalDevices.TRASH_FLUID.get());
        dropSelf(ITBlocks.MetalDevices.TRASH_ITEM.get());
        dropSelf(ITBlocks.MetalDevices.VALVE_FLUID.get());
        dropSelf(ITBlocks.MetalDevices.VALVE_LIMITER.get());
        dropSelf(ITBlocks.MetalDevices.VALVE_LOAD.get());
        dropSelf(ITBlocks.MetalDevices.TECHNOLOGY_ENGINEERING.get());
        dropSelf(ITBlocks.Stone.REINFORCED_COKE_BRICK.get());
        dropSelf(ITBlocks.Stone.SLAB_REINFORCED_COKE_BRICK.get());
        registerMultiblocks();
        ITFluids.ALL_ENTRIES.forEach(entry -> add(entry.getBlock(), noDrop()));
    }

    private void registerMultiblocks() {
        registerMultiblock(ITMultiblockProvider.ALTERNATOR);
        registerMultiblock(ITMultiblockProvider.BOILER_LIQUID);
        registerMultiblock(ITMultiblockProvider.BOILER_SOLID);
        registerMultiblock(ITMultiblockProvider.BOILER_TANK);
        registerMultiblock(ITMultiblockProvider.COOLING_TOWER);
        registerMultiblock(ITMultiblockProvider.DISTILLER);
        registerMultiblock(ITMultiblockProvider.GAS_TURBINE);
        registerMultiblock(ITMultiblockProvider.SOLAR_MELTER);
        registerMultiblock(ITMultiblockProvider.SOLAR_REFLECTOR);
        registerMultiblock(ITMultiblockProvider.SOLAR_TOWER);
        registerMultiblock(ITMultiblockProvider.STEAM_TURBINE);
        registerMultiblock(ITMultiblockProvider.STEEL_SHEETMETAL_TANK);
    }

    private void registerMultiblock(MultiblockRegistration<?> registration) { registerMultiblock(registration.block()); }

    private void registerMultiblock(Supplier<? extends Block> b) { register(b, dropInv(), dropOriginalBlock()); }

    private void register(Supplier<? extends Block> b, LootPool.Builder... pools) {
        LootTable.Builder builder = LootTable.lootTable();
        for (LootPool.Builder pool : pools) { builder.withPool(pool); }
        add(b.get(), builder);
    }

    private LootPool.Builder dropInv() { return createPoolBuilder().add(ITDropInventoryLootEntry.builder()); }

    private LootPool.Builder dropOriginalBlock() { return createPoolBuilder().add(ITMultiblockDropsLootContainer.builder()); }

    private LootPool.Builder createPoolBuilder() { return LootPool.lootPool().when(ExplosionCondition.survivesExplosion()); }

    @Override
    protected @NotNull Set<Block> getKnownBlocks() { return ITBlocks.REGISTER.getEntries().stream().map(RegistryObject::get).collect(Collectors.toSet()); }
}
