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

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ITBlockTags extends BlockTagsProvider
{
    public ITBlockTags(PackOutput output, CompletableFuture<Provider> lookupProvider, ExistingFileHelper existingFileHelper)
    {
        super(output, lookupProvider, ITLib.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(Provider provider)
    {
        ITLib.IT_LOGGER.info("IT Block Tags");

        IntrinsicHolderTagsProvider.IntrinsicTagAppender<Block> tag = this.tag(BlockTags.MINEABLE_WITH_PICKAXE);
        IntrinsicHolderTagsProvider.IntrinsicTagAppender<Block> tag2 = this.tag(BlockTags.NEEDS_IRON_TOOL);

        registerMineable(tag, ITMultiblockProvider.ADV_COKE_OVEN);
        registerMineable(tag, ITMultiblockProvider.BOILER);
        registerMineable(tag, ITMultiblockProvider.ALTERNATOR);
        registerMineable(tag, ITMultiblockProvider.STEAM_TURBINE);
        registerMineable(tag, ITMultiblockProvider.GAS_TURBINE);
        registerMineable(tag2, ITMultiblockProvider.ADV_COKE_OVEN);
        registerMineable(tag2, ITMultiblockProvider.BOILER);
        registerMineable(tag2, ITMultiblockProvider.ALTERNATOR);
        registerMineable(tag2, ITMultiblockProvider.STEAM_TURBINE);
        registerMineable(tag2, ITMultiblockProvider.GAS_TURBINE);
        registerMineable(tag, ITBlocks.MetalDevices.COKE_OVEN_PREHEATER);
    }

    private void registerMineable(IntrinsicHolderTagsProvider.IntrinsicTagAppender<Block> tag, MultiblockRegistration<?>... entries) {
        MultiblockRegistration[] var3 = entries;
        int var4 = entries.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            MultiblockRegistration<?> entry = var3[var5];
            tag.add((Block)entry.block().get());
        }
    }

    private <T extends Block> void registerMineable(IntrinsicTagAppender<Block> tag, Map<?, ITBlocks.BlockEntry<T>> entries)
    {
        registerMineable(tag, new ArrayList<>(entries.values()));
    }

    private void registerMineable(IntrinsicTagAppender<Block> tag, ITBlocks.BlockEntry<?>... entries)
    {
        registerMineable(tag, Arrays.asList(entries));
    }

    private void registerMineable(IntrinsicTagAppender<Block> tag, List<ITBlocks.BlockEntry<?>> entries)
    {
        entries.sort(Comparator.comparing(ITBlocks.BlockEntry::getId));
        for(ITBlocks.BlockEntry<?> entry : entries)
        {
            tag.add(entry.get());
            IEBlocks.BlockEntry<?> slab = IEBlocks.TO_SLAB.get(entry.getId());
            if(slab!=null)
                tag.add(slab.get());
            IEBlocks.BlockEntry<?> stairs = IEBlocks.TO_STAIRS.get(entry.getId());
            if(stairs!=null)
                tag.add(stairs.get());
            IEBlocks.BlockEntry<?> wall = IEBlocks.TO_WALL.get(entry.getId());
            if(wall!=null)
                tag.add(wall.get());
        }
    }
}
