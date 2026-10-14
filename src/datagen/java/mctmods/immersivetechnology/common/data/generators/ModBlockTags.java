package mctmods.immersivetechnology.common.data.generators;

import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.ModBlocks;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class ModBlockTags extends BlockTagsProvider {
    private static final List<Supplier<? extends Block>> MULTIBLOCKS = List.of(
            MultiblockRegistry.ADVANCED_COKE_OVEN.block(),
            MultiblockRegistry.ALTERNATOR.block(),
            MultiblockRegistry.BOILER_LIQUID.block(),
            MultiblockRegistry.BOILER_SOLID.block(),
            MultiblockRegistry.BOILER_TANK.block(),
            MultiblockRegistry.COOLING_TOWER.block(),
            MultiblockRegistry.DISTILLER.block(),
            MultiblockRegistry.GAS_TURBINE.block(),
            MultiblockRegistry.HEAT_EXCHANGER.block(),
            MultiblockRegistry.SOLAR_MELTER.block(),
            MultiblockRegistry.SOLAR_REFLECTOR.block(),
            MultiblockRegistry.SOLAR_TOWER.block(),
            MultiblockRegistry.STEAM_TURBINE.block(),
            MultiblockRegistry.STEEL_SHEETMETAL_TANK.block()
    );

    public ModBlockTags(PackOutput output, CompletableFuture<Provider> lookupProvider, ExistingFileHelper existingFileHelper) { super(output, lookupProvider, Reference.MODID, existingFileHelper); }

    @Override protected void addTags(@Nonnull Provider provider) {
        Reference.IT_LOGGER.info("IT Block Tags");

        IntrinsicHolderTagsProvider.IntrinsicTagAppender<Block> tagAxe = this.tag(BlockTags.MINEABLE_WITH_AXE);
        IntrinsicHolderTagsProvider.IntrinsicTagAppender<Block> tagPickAxe = this.tag(BlockTags.MINEABLE_WITH_PICKAXE);
        IntrinsicHolderTagsProvider.IntrinsicTagAppender<Block> tagIronTool = this.tag(BlockTags.NEEDS_IRON_TOOL);

        registerMineable(tagPickAxe);
        registerMineable(tagIronTool);

        registerMineable(tagAxe, ModBlocks.Wooden.CRATE_CREATIVE);
        registerMineable(tagPickAxe,
                ModBlocks.Metal.ADVANCED_COKE_OVEN_BASEHEATER,
                ModBlocks.Metal.BARREL_CREATIVE,
                ModBlocks.Metal.BARREL_OPEN,
                ModBlocks.Metal.BARREL_STEEL,
                ModBlocks.Connector.CONNECTOR_TIMER,
                ModBlocks.Metal.TRASH_ENERGY,
                ModBlocks.Metal.TRASH_FLUID,
                ModBlocks.Metal.TRASH_ITEM,
                ModBlocks.Metal.VALVE_FLUID,
                ModBlocks.Metal.VALVE_LIMITER,
                ModBlocks.Metal.VALVE_LOAD,
                ModBlocks.Metal.TECHNOLOGY_ENGINEERING,
                ModBlocks.Stone.REINFORCED_COKE_BRICK
        );
        registerMineable(tagIronTool,
                ModBlocks.Metal.ADVANCED_COKE_OVEN_BASEHEATER,
                ModBlocks.Metal.BARREL_CREATIVE,
                ModBlocks.Metal.BARREL_OPEN,
                ModBlocks.Metal.BARREL_STEEL,
                ModBlocks.Wooden.CRATE_CREATIVE,
                ModBlocks.Connector.CONNECTOR_TIMER,
                ModBlocks.Metal.TRASH_ENERGY,
                ModBlocks.Metal.TRASH_FLUID,
                ModBlocks.Metal.TRASH_ITEM,
                ModBlocks.Metal.VALVE_FLUID,
                ModBlocks.Metal.VALVE_LIMITER,
                ModBlocks.Metal.VALVE_LOAD,
                ModBlocks.Metal.TECHNOLOGY_ENGINEERING,
                ModBlocks.Stone.REINFORCED_COKE_BRICK
        );
    }

    private void registerMineable(IntrinsicHolderTagsProvider.IntrinsicTagAppender<Block> tag) {
        for (Supplier<? extends Block> block : MULTIBLOCKS) { tag.add(block.get()); }
    }

    private void registerMineable(IntrinsicHolderTagsProvider.IntrinsicTagAppender<Block> tag, ModBlocks.BlockEntry<?>... entries) { registerMineable(tag, Arrays.asList(entries)); }

    private void registerMineable(IntrinsicHolderTagsProvider.IntrinsicTagAppender<Block> tag, List<ModBlocks.BlockEntry<?>> entries) {
        entries.sort(Comparator.comparing(ModBlocks.BlockEntry::getId));
        for (ModBlocks.BlockEntry<?> entry : entries) {
            tag.add(entry.get());
            ModBlocks.BlockEntry<?> slab = ModBlocks.TO_SLAB.get(entry.getId());
            if (slab != null) { tag.add(slab.get()); }
            ModBlocks.BlockEntry<?> stairs = ModBlocks.TO_STAIRS.get(entry.getId());
            if (stairs != null) { tag.add(stairs.get()); }
            ModBlocks.BlockEntry<?> wall = ModBlocks.TO_WALL.get(entry.getId());
            if (wall != null) { tag.add(wall.get()); }
        }
    }
}
