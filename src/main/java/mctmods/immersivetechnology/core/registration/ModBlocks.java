package mctmods.immersivetechnology.core.registration;

import com.immersiveconvergence.api.ICMods;
import com.immersiveconvergence.api.block.StairsBlock;
import com.immersiveconvergence.api.block.ModWallBlock;
import mctmods.immersivetechnology.common.blocks.metal.*;
import mctmods.immersivetechnology.common.blocks.metal.logic.*;
import mctmods.immersivetechnology.common.blocks.stone.ReinforcedCokeBrick;
import mctmods.immersivetechnology.common.blocks.stone.slab.SlabReinforcedCokeBrick;
import mctmods.immersivetechnology.common.blocks.wooden.CrateCreativeBlock;
import mctmods.immersivetechnology.common.blocks.wooden.logic.CrateCreativeBlockEntity;
import com.immersiveconvergence.api.block.ModBlockItem;
import mctmods.immersivetechnology.core.lib.Reference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks REGISTER = DeferredRegister.createBlocks(Reference.MODID);
    public static final Map<ResourceLocation, ModBlocks.BlockEntry<? extends SlabBlock>> TO_SLAB = new HashMap<>();
    public static final Map<ResourceLocation, ModBlocks.BlockEntry<? extends StairsBlock>> TO_STAIRS = new HashMap<>();
    public static final Map<ResourceLocation, ModBlocks.BlockEntry<? extends ModWallBlock>> TO_WALL = new HashMap<>();

    public static final Collection<BlockEntry<?>> NEEDS_IE = new ArrayList<>();
    private static final HashMap<String, Supplier<? extends Block>> BLOCK_REGISTRY_MAP = new HashMap<>();
    public static Function<String, Block> getBlock = (key) -> BLOCK_REGISTRY_MAP.get(key).get();

    private static final Supplier<BlockBehaviour.Properties> DEFAULT_METAL_PROPERTIES = () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(3.0F, 15.0F).requiresCorrectToolForDrops();
    private static final Supplier<BlockBehaviour.Properties> METAL_PROPERTIES_NO_OVERLAY = () -> DEFAULT_METAL_PROPERTIES.get().isViewBlocking((state, blockReader, pos) -> false);
    public static final Supplier<BlockBehaviour.Properties> METAL_PROPERTIES_NO_OCCLUSION = () -> METAL_PROPERTIES_NO_OVERLAY.get().noOcclusion().forceSolidOn();

    private static final Supplier<BlockBehaviour.Properties> STONE_ORE_PROPERTIES = () -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE);
    private static final Supplier<BlockBehaviour.Properties> DEEPSLATE_ORE_PROPERTIES = () -> BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE);

    private static final Supplier<BlockBehaviour.Properties> DEFAULT_STONE_PROPERTIES = () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).strength(2, 20);
    public static final Supplier<BlockBehaviour.Properties> STONE_PROPERTIES_NO_OCCLUSION = () -> DEFAULT_STONE_PROPERTIES.get().noOcclusion();

    public static final class Metal {
        public static BlockEntry<AdvancedCokeOvenBaseHeaterBlock> ADVANCED_COKE_OVEN_BASEHEATER;
        public static BlockEntry<Block> GRAPHITE_MODERATOR;
        public static BlockEntry<Block> NUCLEAR_ENGINEERING;
        public static BlockEntry<Block> REACTOR_CLADDING;
        public static BlockEntry<Block> STORAGE_STAINLESS_STEEL;
        public static BlockEntry<Block> STORAGE_CHROMIUM;
        public static BlockEntry<Block> STORAGE_MAGNESIUM;
        public static BlockEntry<Block> SHEETMETAL_STAINLESS_STEEL;
        public static BlockEntry<Block> ORE_CHROMIUM;
        public static BlockEntry<Block> DEEPSLATE_ORE_CHROMIUM;
        public static BlockEntry<Block> ORE_MAGNESIUM;
        public static BlockEntry<Block> DEEPSLATE_ORE_MAGNESIUM;
        public static BlockEntry<BarrelCreativeBlock> BARREL_CREATIVE;
        public static BlockEntry<BarrelSteelBlock> BARREL_STEEL;
        public static BlockEntry<BarrelOpenBlock> BARREL_OPEN;
        public static BlockEntry<TechnologyEngineeringBlock> TECHNOLOGY_ENGINEERING;
        public static BlockEntry<TrashEnergyBlock> TRASH_ENERGY;
        public static BlockEntry<TrashFluidBlock> TRASH_FLUID;
        public static BlockEntry<TrashItemBlock> TRASH_ITEM;
        public static BlockEntry<ValveFluidBlock> VALVE_FLUID;
        public static BlockEntry<ValveLoadBlock> VALVE_LOAD;
        public static BlockEntry<ValveLimiterBlock> VALVE_LIMITER;

        private static void init() {
            ADVANCED_COKE_OVEN_BASEHEATER = new BlockEntry<>(
                    "advanced_coke_oven_baseheater",
                    METAL_PROPERTIES_NO_OCCLUSION,
                    p -> new AdvancedCokeOvenBaseHeaterBlock(AdvancedCokeOvenBaseHeaterBlockEntity::new, p)
            );

            BARREL_CREATIVE = new BlockEntry<>(
                    "barrel_creative",
                    METAL_PROPERTIES_NO_OCCLUSION,
                    p -> new BarrelCreativeBlock(BarrelCreativeBlockEntity::new, p)
            );

            BARREL_STEEL = new BlockEntry<>(
                    "barrel_steel",
                    METAL_PROPERTIES_NO_OCCLUSION,
                    p -> new BarrelSteelBlock(BarrelSteelBlockEntity::new, p)
            );

            BARREL_OPEN = new BlockEntry<>(
                    "barrel_open",
                    METAL_PROPERTIES_NO_OCCLUSION,
                    p -> new BarrelOpenBlock(BarrelOpenBlockEntity::new, p)
            );



            TECHNOLOGY_ENGINEERING = new BlockEntry<>(
                    "technology_engineering",
                    DEFAULT_METAL_PROPERTIES,
                    TechnologyEngineeringBlock::new
            );

            GRAPHITE_MODERATOR = new BlockEntry<>(
                    "graphite_moderator",
                    DEFAULT_METAL_PROPERTIES,
                    Block::new
            );

            NUCLEAR_ENGINEERING = new BlockEntry<>(
                    "nuclear_engineering",
                    DEFAULT_METAL_PROPERTIES,
                    Block::new
            );

            REACTOR_CLADDING = new BlockEntry<>(
                    "reactor_cladding",
                    DEFAULT_METAL_PROPERTIES,
                    Block::new
            );

            STORAGE_STAINLESS_STEEL = new BlockEntry<>(
                    "storage_stainless_steel",
                    DEFAULT_METAL_PROPERTIES,
                    Block::new
            );

            STORAGE_CHROMIUM = new BlockEntry<>(
                    "storage_chromium",
                    DEFAULT_METAL_PROPERTIES,
                    Block::new
            );

            STORAGE_MAGNESIUM = new BlockEntry<>(
                    "storage_magnesium",
                    DEFAULT_METAL_PROPERTIES,
                    Block::new
            );

            SHEETMETAL_STAINLESS_STEEL = new BlockEntry<>(
                    "sheetmetal_stainless_steel",
                    DEFAULT_METAL_PROPERTIES,
                    Block::new
            );

            ORE_CHROMIUM = new BlockEntry<>(
                    "ore_chromium",
                    STONE_ORE_PROPERTIES,
                    Block::new
            );

            DEEPSLATE_ORE_CHROMIUM = new BlockEntry<>(
                    "deepslate_ore_chromium",
                    DEEPSLATE_ORE_PROPERTIES,
                    Block::new
            );

            ORE_MAGNESIUM = new BlockEntry<>(
                    "ore_magnesium",
                    STONE_ORE_PROPERTIES,
                    Block::new
            );

            DEEPSLATE_ORE_MAGNESIUM = new BlockEntry<>(
                    "deepslate_ore_magnesium",
                    DEEPSLATE_ORE_PROPERTIES,
                    Block::new
            );

            TRASH_ENERGY = new BlockEntry<>(
                    "trash_energy",
                    METAL_PROPERTIES_NO_OCCLUSION,
                    p -> new TrashEnergyBlock(TrashEnergyBlockEntity::new, p)
            );

            TRASH_FLUID = new BlockEntry<>(
                    "trash_fluid",
                    METAL_PROPERTIES_NO_OCCLUSION,
                    p -> new TrashFluidBlock(TrashFluidBlockEntity::new, p)
            );

            TRASH_ITEM = new BlockEntry<>(
                    "trash_item",
                    METAL_PROPERTIES_NO_OCCLUSION,
                    p -> new TrashItemBlock(TrashItemBlockEntity::new, p)
            );
        }

        private static void initValves() {
            VALVE_FLUID = new BlockEntry<>(
                    "valve_fluid",
                    METAL_PROPERTIES_NO_OCCLUSION,
                    p -> new ValveFluidBlock(ValveFluidBlockEntity::new, p)
            );

            VALVE_LOAD = new BlockEntry<>(
                    "valve_load",
                    METAL_PROPERTIES_NO_OCCLUSION,
                    p -> new ValveLoadBlock(ValveLoadBlockEntity::new, p)
            );

            VALVE_LIMITER = new BlockEntry<>(
                    "valve_limiter",
                    METAL_PROPERTIES_NO_OCCLUSION,
                    p -> new ValveLimiterBlock(ValveLimiterBlockEntity::new, p)
            );
            NEEDS_IE.addAll(List.of(VALVE_FLUID, VALVE_LIMITER, VALVE_LOAD));
        }
    }

    public static final class Stone {
        public static BlockEntry<ReinforcedCokeBrick> REINFORCED_COKE_BRICK;
        public static BlockEntry<SlabReinforcedCokeBrick> SLAB_REINFORCED_COKE_BRICK;

        private static void init() {
            REINFORCED_COKE_BRICK = new BlockEntry<>(
                    "reinforced_coke_brick",
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.STONE),
                    ReinforcedCokeBrick::new
            );
            SLAB_REINFORCED_COKE_BRICK = new BlockEntry<>(
                    "slab_reinforced_coke_brick",
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.STONE),
                    SlabReinforcedCokeBrick::new
            );
        }
    }

    public static final class Wooden {
        public static BlockEntry<CrateCreativeBlock> CRATE_CREATIVE;

        private static void init() {
            CRATE_CREATIVE = new BlockEntry<>(
                    "crate_creative",
                    METAL_PROPERTIES_NO_OCCLUSION,
                    p -> new CrateCreativeBlock(CrateCreativeBlockEntity::new, p)
            );
        }
    }

    public static void initBlocks() {
        Metal.init();
        if (ICMods.immersiveEngineering()) { Metal.initValves(); }
        Stone.init();
        Wooden.init();
        TO_SLAB.put(Stone.REINFORCED_COKE_BRICK.getId(), Stone.SLAB_REINFORCED_COKE_BRICK);
    }

    public static void init(IEventBus event) {
        initBlocks();
        REGISTER.register(event);
        for(BlockEntry<?> entry : BlockEntry.ALL_ENTRIES) {
            Function<Block, ModBlockItem> finalToItem = ModBlockItem::new;
            ModItems.REGISTER.register(entry.getId().getPath(), () -> finalToItem.apply(entry.get()));
            BLOCK_REGISTRY_MAP.put(entry.getId().getPath(), entry.getRegObject());
        }
    }

    public static final class BlockEntry<T extends Block> extends com.immersiveconvergence.api.registration.BlockEntry<T> {
        public static final Collection<BlockEntry<?>> ALL_ENTRIES = new ArrayList<>();

        public BlockEntry(String name, Supplier<BlockBehaviour.Properties> properties, Function<BlockBehaviour.Properties, T> make) {
            super(REGISTER, name, properties, make);
            ALL_ENTRIES.add(this);
        }
    }
}
