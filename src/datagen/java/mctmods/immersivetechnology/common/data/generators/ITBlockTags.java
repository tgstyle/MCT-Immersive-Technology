package mctmods.immersivetechnology.common.data.generators;

import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.common.register.IEBlocks;
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
    public ITBlockTags(PackOutput output, CompletableFuture<Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, ITLib.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(@NotNull Provider provider) {
        ITLib.IT_LOGGER.info("IT Block Tags");

        IntrinsicHolderTagsProvider.IntrinsicTagAppender<Block> tag1 = this.tag(BlockTags.MINEABLE_WITH_PICKAXE);
        IntrinsicHolderTagsProvider.IntrinsicTagAppender<Block> tag2 = this.tag(BlockTags.NEEDS_IRON_TOOL);

        registerMineable(tag1, ITMultiblockProvider.ADVANCED_COKE_OVEN, ITMultiblockProvider.ALTERNATOR, ITMultiblockProvider.BOILER, ITMultiblockProvider.DISTILLER, ITMultiblockProvider.GAS_TURBINE, ITMultiblockProvider.SOLAR_MELTER, ITMultiblockProvider.SOLAR_REFLECTOR, ITMultiblockProvider.SOLAR_TOWER, ITMultiblockProvider.STEAM_TURBINE);
        registerMineable(tag2, ITMultiblockProvider.ADVANCED_COKE_OVEN, ITMultiblockProvider.ALTERNATOR, ITMultiblockProvider.BOILER, ITMultiblockProvider.DISTILLER, ITMultiblockProvider.GAS_TURBINE, ITMultiblockProvider.SOLAR_MELTER, ITMultiblockProvider.SOLAR_REFLECTOR, ITMultiblockProvider.SOLAR_TOWER, ITMultiblockProvider.STEAM_TURBINE);

        registerMineable(tag1, ITBlocks.MetalDevices.CREATIVE_BARREL, ITBlocks.MetalDevices.STEEL_BARREL, ITBlocks.MetalDevices.OPEN_BARREL, ITBlocks.MetalDevices.TRASH_ENERGY, ITBlocks.MetalDevices.TRASH_FLUID, ITBlocks.MetalDevices.TRASH_ITEM, ITBlocks.Stone.REINFORCED_COKE_BRICK);
        registerMineable(tag2, ITBlocks.MetalDevices.CREATIVE_BARREL, ITBlocks.MetalDevices.STEEL_BARREL, ITBlocks.MetalDevices.OPEN_BARREL, ITBlocks.MetalDevices.TRASH_ENERGY, ITBlocks.MetalDevices.TRASH_FLUID, ITBlocks.MetalDevices.TRASH_ITEM, ITBlocks.Stone.REINFORCED_COKE_BRICK);
    }

    private void registerMineable(IntrinsicHolderTagsProvider.IntrinsicTagAppender<Block> tag, MultiblockRegistration<?>... entries) { for (MultiblockRegistration<?> entry : entries) { tag.add(entry.block().get()); } }

    private void registerMineable(IntrinsicTagAppender<Block> tag, ITBlocks.BlockEntry<?>... entries) { registerMineable(tag, Arrays.asList(entries)); }

    private void registerMineable(IntrinsicTagAppender<Block> tag, List<ITBlocks.BlockEntry<?>> entries) {
        entries.sort(Comparator.comparing(ITBlocks.BlockEntry::getId));
        for (ITBlocks.BlockEntry<?> entry : entries) {
            tag.add(entry.get());
            IEBlocks.BlockEntry<?> slab = IEBlocks.TO_SLAB.get(entry.getId());
            if (slab != null) tag.add(slab.get());
            IEBlocks.BlockEntry<?> stairs = IEBlocks.TO_STAIRS.get(entry.getId());
            if (stairs != null) tag.add(stairs.get());
            IEBlocks.BlockEntry<?> wall = IEBlocks.TO_WALL.get(entry.getId());
            if (wall != null) tag.add(wall.get());
        }
    }
}
