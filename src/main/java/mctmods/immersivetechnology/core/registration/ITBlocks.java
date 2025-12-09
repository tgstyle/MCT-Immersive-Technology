package mctmods.immersivetechnology.core.registration;

import mctmods.immersivetechnology.common.blocks.helper.ITStairsBlock;
import mctmods.immersivetechnology.common.blocks.helper.ITWallBlock;
import mctmods.immersivetechnology.common.blocks.metal.*;
import mctmods.immersivetechnology.common.blocks.metal.logic.*;
import mctmods.immersivetechnology.common.blocks.stone.ReinforcedCokeBrick;
import mctmods.immersivetechnology.common.blocks.stone.slab.SlabReinforcedCokeBrick;
import mctmods.immersivetechnology.common.blocks.wooden.CrateCreativeBlock;
import mctmods.immersivetechnology.common.blocks.wooden.logic.CrateCreativeBlockEntity;
import mctmods.immersivetechnology.common.items.helper.ITBlockItem;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ITBlocks {
    public static final DeferredRegister<Block> REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, ITLib.MODID);
    public static final Map<ResourceLocation, ITBlocks.BlockEntry<? extends SlabBlock>> TO_SLAB = new HashMap<>();
    public static final Map<ResourceLocation, ITBlocks.BlockEntry<? extends ITStairsBlock>> TO_STAIRS = new HashMap<>();
    public static final Map<ResourceLocation, ITBlocks.BlockEntry<? extends ITWallBlock>> TO_WALL = new HashMap<>();

    private static final HashMap<String, RegistryObject<? extends Block>> BLOCK_REGISTRY_MAP = new HashMap<>();
    public static Function<String, Block> getBlock = (key) -> BLOCK_REGISTRY_MAP.get(key).get();

    private static final Supplier<BlockBehaviour.Properties> DEFAULT_METAL_PROPERTIES = () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(3.0F, 15.0F).requiresCorrectToolForDrops();
    private static final Supplier<BlockBehaviour.Properties> METAL_PROPERTIES_NO_OVERLAY = () -> DEFAULT_METAL_PROPERTIES.get().isViewBlocking((state, blockReader, pos) -> false);
    public static final Supplier<BlockBehaviour.Properties> METAL_PROPERTIES_NO_OCCLUSION = () -> METAL_PROPERTIES_NO_OVERLAY.get().noOcclusion().forceSolidOn();

    public static final class Metal {
        public static BlockEntry<BarrelCreativeBlock> BARREL_CREATIVE;
        public static BlockEntry<BarrelSteelBlock> BARREL_STEEL;
        public static BlockEntry<BarrelOpenBlock> BARREL_OPEN;
        public static BlockEntry<HeatCreativeBlock> HEAT_CREATIVE;
        public static BlockEntry<RotorCreativeBlock> ROTOR_CREATIVE;
        public static BlockEntry<TechnologyEngineeringBlock> TECHNOLOGY_ENGINEERING;
        public static BlockEntry<TrashEnergyBlock> TRASH_ENERGY;
        public static BlockEntry<TrashFluidBlock> TRASH_FLUID;
        public static BlockEntry<TrashItemBlock> TRASH_ITEM;
        public static BlockEntry<ValveFluidBlock> VALVE_FLUID;
        public static BlockEntry<ValveLoadBlock> VALVE_LOAD;
        public static BlockEntry<ValveLimiterBlock> VALVE_LIMITER;

        private static void init() {
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

            ROTOR_CREATIVE = new BlockEntry<>(
                    "rotor_creative",
                    METAL_PROPERTIES_NO_OCCLUSION,
                    p -> new RotorCreativeBlock(RotorCreativeBlockEntity::new, p)
            );

            HEAT_CREATIVE = new BlockEntry<>(
                    "heat_creative",
                    METAL_PROPERTIES_NO_OCCLUSION,
                    p -> new HeatCreativeBlock(HeatCreativeBlockEntity::new, p)
            );

            TECHNOLOGY_ENGINEERING = new BlockEntry<>(
                    "technology_engineering",
                    DEFAULT_METAL_PROPERTIES,
                    TechnologyEngineeringBlock::new
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
        }
    }

    public static final class Stone {
        public static BlockEntry<ReinforcedCokeBrick> REINFORCED_COKE_BRICK;
        public static BlockEntry<SlabReinforcedCokeBrick> SLAB_REINFORCED_COKE_BRICK;

        private static void init() {
            REINFORCED_COKE_BRICK = new BlockEntry<>(
                    "reinforced_coke_brick",
                    () -> BlockBehaviour.Properties.copy(Blocks.STONE),
                    ReinforcedCokeBrick::new
            );
            SLAB_REINFORCED_COKE_BRICK = new BlockEntry<>(
                    "slab_reinforced_coke_brick",
                    () -> BlockBehaviour.Properties.copy(Blocks.STONE),
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
        Stone.init();
        Wooden.init();
        TO_SLAB.put(Stone.REINFORCED_COKE_BRICK.getId(), Stone.SLAB_REINFORCED_COKE_BRICK);
    }

    public static List<? extends Block> getITBlocks() { return REGISTER.getEntries().stream().map(RegistryObject::get).collect(Collectors.toList()); }

    public static void init(IEventBus event) {
        initBlocks();
        REGISTER.register(event);
        for(BlockEntry<?> entry : BlockEntry.ALL_ENTRIES) {
            Function<Block, ITBlockItem> finalToItem = ITBlockItem::new;
            ITItems.REGISTER.register(entry.getId().getPath(), () -> finalToItem.apply(entry.get()));
            BLOCK_REGISTRY_MAP.put(entry.getId().getPath(), entry.getRegObject());
        }
    }

    public static final class BlockEntry<T extends Block> implements Supplier<T>, ItemLike {
        public static final Collection<BlockEntry<?>> ALL_ENTRIES = new ArrayList<>();

        private final RegistryObject<T> regObject;
        private final Supplier<BlockBehaviour.Properties> properties;

        public BlockEntry(String name, Supplier<BlockBehaviour.Properties> properties, Function<BlockBehaviour.Properties, T> make) {
            this.properties = properties;
            this.regObject = REGISTER.register(name, () -> make.apply(properties.get()));
            ALL_ENTRIES.add(this);
        }

        @Override public T get() { return regObject.get(); }
        public ResourceLocation getId() { return regObject.getId(); }
        public BlockBehaviour.Properties getProperties() { return properties.get(); }
        @Override public @NotNull Item asItem() { return get().asItem(); }
        public RegistryObject<? extends Block> getRegObject() { return regObject; }
    }
}
