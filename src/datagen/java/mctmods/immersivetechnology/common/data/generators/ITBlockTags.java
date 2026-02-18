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

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ITBlockTags extends BlockTagsProvider {
    private static final MultiblockRegistration<?>[] MULTIBLOCKS = {
            ITMultiblockProvider.ALTERNATOR,
            ITMultiblockProvider.BOILER_LIQUID,
            ITMultiblockProvider.BOILER_SOLID,
            ITMultiblockProvider.BOILER_TANK,
            ITMultiblockProvider.COOLING_TOWER,
            ITMultiblockProvider.DISTILLER,
            ITMultiblockProvider.GAS_TURBINE,
            ITMultiblockProvider.HEAT_EXCHANGER,
            ITMultiblockProvider.SOLAR_MELTER,
            ITMultiblockProvider.SOLAR_REFLECTOR,
            ITMultiblockProvider.SOLAR_TOWER,
            ITMultiblockProvider.STEAM_TURBINE,
            ITMultiblockProvider.STEEL_SHEETMETAL_TANK
    };

    public ITBlockTags(PackOutput output, CompletableFuture<Provider> lookupProvider, ExistingFileHelper existingFileHelper) { super(output, lookupProvider, ITLib.MODID, existingFileHelper); }

    @Override protected void addTags(@NotNull Provider provider) {
        ITLib.IT_LOGGER.info("IT Block Tags");

        IntrinsicHolderTagsProvider.IntrinsicTagAppender<Block> tagAxe = this.tag(BlockTags.MINEABLE_WITH_AXE);
        IntrinsicHolderTagsProvider.IntrinsicTagAppender<Block> tagPickAxe = this.tag(BlockTags.MINEABLE_WITH_PICKAXE);
        IntrinsicHolderTagsProvider.IntrinsicTagAppender<Block> tagIronTool = this.tag(BlockTags.NEEDS_IRON_TOOL);

        registerMineable(tagPickAxe);
        registerMineable(tagIronTool);

        registerMineable(tagAxe, ITBlocks.Wooden.CRATE_CREATIVE);
        registerMineable(tagPickAxe,
                ITBlocks.Metal.BARREL_CREATIVE,
                ITBlocks.Metal.BARREL_OPEN,
                ITBlocks.Metal.BARREL_STEEL,
                ITBlocks.Metal.HEAT_CREATIVE,
                ITBlocks.Metal.ROTOR_CREATIVE,
                ITBlocks.Metal.TRASH_ENERGY,
                ITBlocks.Metal.TRASH_FLUID,
                ITBlocks.Metal.TRASH_ITEM,
                ITBlocks.Metal.VALVE_FLUID,
                ITBlocks.Metal.VALVE_LIMITER,
                ITBlocks.Metal.VALVE_LOAD,
                ITBlocks.Metal.TECHNOLOGY_ENGINEERING,
                ITBlocks.Stone.REINFORCED_COKE_BRICK
        );
        registerMineable(tagIronTool,
                ITBlocks.Metal.BARREL_CREATIVE,
                ITBlocks.Metal.BARREL_OPEN,
                ITBlocks.Metal.BARREL_STEEL,
                ITBlocks.Wooden.CRATE_CREATIVE,
                ITBlocks.Metal.HEAT_CREATIVE,
                ITBlocks.Metal.ROTOR_CREATIVE,
                ITBlocks.Metal.TRASH_ENERGY,
                ITBlocks.Metal.TRASH_FLUID,
                ITBlocks.Metal.TRASH_ITEM,
                ITBlocks.Metal.VALVE_FLUID,
                ITBlocks.Metal.VALVE_LIMITER,
                ITBlocks.Metal.VALVE_LOAD,
                ITBlocks.Metal.TECHNOLOGY_ENGINEERING,
                ITBlocks.Stone.REINFORCED_COKE_BRICK
        );
    }

    private void registerMineable(IntrinsicHolderTagsProvider.IntrinsicTagAppender<Block> tag) {
        for (MultiblockRegistration<?> entry : MULTIBLOCKS) { tag.add(entry.block().get()); }
    }

    private void registerMineable(IntrinsicHolderTagsProvider.IntrinsicTagAppender<Block> tag, ITBlocks.BlockEntry<?>... entries) { registerMineable(tag, Arrays.asList(entries)); }

    private void registerMineable(IntrinsicHolderTagsProvider.IntrinsicTagAppender<Block> tag, List<ITBlocks.BlockEntry<?>> entries) {
        entries.sort(Comparator.comparing(ITBlocks.BlockEntry::getId));
        for (ITBlocks.BlockEntry<?> entry : entries) {
            tag.add(entry.get());
            ITBlocks.BlockEntry<?> slab = ITBlocks.TO_SLAB.get(entry.getId());
            if (slab != null) { tag.add(slab.get()); }
            ITBlocks.BlockEntry<?> stairs = ITBlocks.TO_STAIRS.get(entry.getId());
            if (stairs != null) { tag.add(stairs.get()); }
            ITBlocks.BlockEntry<?> wall = ITBlocks.TO_WALL.get(entry.getId());
            if (wall != null) { tag.add(wall.get()); }
        }
    }
}
