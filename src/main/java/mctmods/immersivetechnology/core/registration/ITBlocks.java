package mctmods.immersivetechnology.core.registration;

import mctmods.immersivetechnology.common.blocks.metal.*;
import mctmods.immersivetechnology.common.blocks.stone.ReinforcedCokeBrick;
import mctmods.immersivetechnology.common.blocks.stone.slab.SlabReinforcedCokeBrick;
import mctmods.immersivetechnology.common.items.helper.ITBlockItem;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ITBlocks {
    public static final DeferredRegister<Block> REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, ITLib.MODID);

    private static final HashMap<String, RegistryObject<? extends Block>> BLOCK_REGISTRY_MAP = new HashMap<>();
    public static Function<String, Block> getBlock = (key) -> BLOCK_REGISTRY_MAP.get(key).get();

    public static final class MetalDevices {
        public static BlockEntry<CreativeBarrelBlock> CREATIVE_BARREL;
        public static BlockEntry<SteelBarrelBlock> STEEL_BARREL;
        public static BlockEntry<OpenBarrelBlock> OPEN_BARREL;
        public static BlockEntry<TrashEnergyBlock> TRASH_ENERGY;
        public static BlockEntry<TrashFluidBlock> TRASH_FLUID;
        public static BlockEntry<TrashItemBlock> TRASH_ITEM;
        public static BlockEntry<FluidValveBlock> FLUID_VALVE;

        private static void init() {
            CREATIVE_BARREL = new BlockEntry<>(
                    "creative_barrel",
                    () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F, 15.0F).noOcclusion(),
                    p -> new CreativeBarrelBlock(CreativeBarrelBlockEntity::new, p)
            );

            STEEL_BARREL = new BlockEntry<>(
                    "steel_barrel",
                    () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F, 15.0F).noOcclusion(),
                    p -> new SteelBarrelBlock(SteelBarrelBlockEntity::new, p)
            );

            OPEN_BARREL = new BlockEntry<>(
                    "open_barrel",
                    () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F, 15.0F).noOcclusion(),
                    p -> new OpenBarrelBlock(OpenBarrelBlockEntity::new, p)
            );

            TRASH_ENERGY = new BlockEntry<>(
                    "trash_energy",
                    () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F, 15.0F).noOcclusion(),
                    p -> new TrashEnergyBlock(TrashEnergyBlockEntity::new, p)
            );

            TRASH_FLUID = new BlockEntry<>(
                    "trash_fluid",
                    () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F, 15.0F).noOcclusion(),
                    p -> new TrashFluidBlock(TrashFluidBlockEntity::new, p)
            );

            TRASH_ITEM = new BlockEntry<>(
                    "trash_item",
                    () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F, 15.0F).noOcclusion(),
                    p -> new TrashItemBlock(TrashItemBlockEntity::new, p)
            );

            FLUID_VALVE = new BlockEntry<>(
                    "fluid_valve",
                    () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F, 15.0F).noOcclusion(),
                    p -> new FluidValveBlock(FluidValveBlockEntity::new, p)
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

    public static void initBlocks() {
        MetalDevices.init();
        Stone.init();
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
        public static final Collection<BlockEntry<?> > ALL_ENTRIES = new ArrayList<>();

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
