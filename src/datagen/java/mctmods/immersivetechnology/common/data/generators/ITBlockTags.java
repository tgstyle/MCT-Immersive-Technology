package mctmods.immersivetechnology.common.data.generators;

import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITBlocks;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ITBlockTags extends BlockTagsProvider {
    public ITBlockTags(PackOutput output, CompletableFuture<Provider> lookupProvider, ExistingFileHelper existingFileHelper) { super(output, lookupProvider, ITLib.MODID, existingFileHelper); }

    @Override
    protected void addTags(@NotNull Provider provider) {
        ITLib.IT_LOGGER.info("IT Block Tags");

        IntrinsicHolderTagsProvider.IntrinsicTagAppender<Block> tag1 = this.tag(BlockTags.MINEABLE_WITH_PICKAXE);
        IntrinsicHolderTagsProvider.IntrinsicTagAppender<Block> tag2 = this.tag(BlockTags.NEEDS_IRON_TOOL);

        registerMineable(tag1, ITMultiblockProvider.ALTERNATOR, ITMultiblockProvider.BOILER_LIQUID, ITMultiblockProvider.BOILER_SOLID, ITMultiblockProvider.BOILER_TANK, ITMultiblockProvider.COOLING_TOWER, ITMultiblockProvider.DISTILLER, ITMultiblockProvider.GAS_TURBINE, ITMultiblockProvider.SOLAR_MELTER, ITMultiblockProvider.SOLAR_REFLECTOR, ITMultiblockProvider.SOLAR_TOWER, ITMultiblockProvider.STEAM_TURBINE, ITMultiblockProvider.STEEL_SHEETMETAL_TANK);
        registerMineable(tag2, ITMultiblockProvider.ALTERNATOR, ITMultiblockProvider.BOILER_LIQUID, ITMultiblockProvider.BOILER_SOLID, ITMultiblockProvider.BOILER_TANK, ITMultiblockProvider.COOLING_TOWER, ITMultiblockProvider.DISTILLER, ITMultiblockProvider.GAS_TURBINE, ITMultiblockProvider.SOLAR_MELTER, ITMultiblockProvider.SOLAR_REFLECTOR, ITMultiblockProvider.SOLAR_TOWER, ITMultiblockProvider.STEAM_TURBINE, ITMultiblockProvider.STEEL_SHEETMETAL_TANK);

        registerMineable(tag1, ITBlocks.MetalDevices.BARREL_CREATIVE, ITBlocks.MetalDevices.BARREL_OPEN, ITBlocks.MetalDevices.BARREL_STEEL, ITBlocks.MetalDevices.TRASH_ENERGY, ITBlocks.MetalDevices.TRASH_FLUID, ITBlocks.MetalDevices.TRASH_ITEM, ITBlocks.MetalDevices.VALVE_FLUID, ITBlocks.MetalDevices.VALVE_LIMITER, ITBlocks.MetalDevices.VALVE_LOAD, ITBlocks.MetalDevices.TECHNOLOGY_ENGINEERING, ITBlocks.Stone.REINFORCED_COKE_BRICK);
        registerMineable(tag2, ITBlocks.MetalDevices.BARREL_CREATIVE, ITBlocks.MetalDevices.BARREL_OPEN, ITBlocks.MetalDevices.BARREL_STEEL, ITBlocks.MetalDevices.TRASH_ENERGY, ITBlocks.MetalDevices.TRASH_FLUID, ITBlocks.MetalDevices.TRASH_ITEM, ITBlocks.MetalDevices.VALVE_FLUID, ITBlocks.MetalDevices.VALVE_LIMITER, ITBlocks.MetalDevices.VALVE_LOAD, ITBlocks.MetalDevices.TECHNOLOGY_ENGINEERING, ITBlocks.Stone.REINFORCED_COKE_BRICK);
    }

    private void registerMineable(IntrinsicHolderTagsProvider.IntrinsicTagAppender<Block> tag, MultiblockRegistration<?>... entries) { for (MultiblockRegistration<?> entry : entries) { tag.add(entry.block().get()); } }

    private void registerMineable(IntrinsicTagAppender<Block> tag, ITBlocks.BlockEntry<?>... entries) { registerMineable(tag, Arrays.asList(entries)); }

    private void registerMineable(IntrinsicTagAppender<Block> tag, List<ITBlocks.BlockEntry<?>> entries) {
        entries.sort(Comparator.comparing(ITBlocks.BlockEntry::getId));
        for (ITBlocks.BlockEntry<?> entry : entries) {
            tag.add(entry.get());
            ITBlocks.BlockEntry<?> slab = ITBlocks.TO_SLAB.get(entry.getId());
            if (slab != null) tag.add(slab.get());
            ITBlocks.BlockEntry<?> stairs = ITBlocks.TO_STAIRS.get(entry.getId());
            if (stairs != null) tag.add(stairs.get());
            ITBlocks.BlockEntry<?> wall = ITBlocks.TO_WALL.get(entry.getId());
            if (wall != null) tag.add(wall.get());
        }
    }
}
