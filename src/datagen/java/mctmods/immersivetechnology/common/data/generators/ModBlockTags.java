package mctmods.immersivetechnology.common.data.generators;

import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
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
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModBlockTags extends BlockTagsProvider {
    private static final MultiblockRegistration<?>[] MULTIBLOCKS = {
            MultiblockRegistry.ADVANCED_COKE_OVEN,
            MultiblockRegistry.ALTERNATOR,
            MultiblockRegistry.BOILER_LIQUID,
            MultiblockRegistry.BOILER_SOLID,
            MultiblockRegistry.BOILER_TANK,
            MultiblockRegistry.COOLING_TOWER,
            MultiblockRegistry.DISTILLER,
            MultiblockRegistry.GAS_TURBINE,
            MultiblockRegistry.HEAT_EXCHANGER,
            MultiblockRegistry.SOLAR_MELTER,
            MultiblockRegistry.SOLAR_REFLECTOR,
            MultiblockRegistry.SOLAR_TOWER,
            MultiblockRegistry.STEAM_TURBINE,
            MultiblockRegistry.STEEL_SHEETMETAL_TANK
    };

    public ModBlockTags(PackOutput output, CompletableFuture<Provider> lookupProvider, ExistingFileHelper existingFileHelper) { super(output, lookupProvider, Reference.MODID, existingFileHelper); }

    @Override protected void addTags(@NotNull Provider provider) {
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
                ModBlocks.Metal.HEAT_CREATIVE,
                ModBlocks.Metal.ROTOR_CREATIVE,
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
                ModBlocks.Metal.HEAT_CREATIVE,
                ModBlocks.Metal.ROTOR_CREATIVE,
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
        for (MultiblockRegistration<?> entry : MULTIBLOCKS) { tag.add(entry.block().get()); }
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
