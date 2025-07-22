package mctmods.immersivetechnology.core.registration;

import blusunrize.immersiveengineering.common.register.IEBlocks;
import mctmods.immersivetechnology.common.blocks.metal.CokeOvenPreheaterBlock;
import mctmods.immersivetechnology.common.blocks.metal.CokeOvenPreheaterBlockEntity;
import mctmods.immersivetechnology.common.blocks.metal.CreativeBarrelBlock;
import mctmods.immersivetechnology.common.blocks.metal.CreativeBarrelBlockEntity;
import mctmods.immersivetechnology.common.blocks.stone.ReinforcedCokeBrick;
import mctmods.immersivetechnology.common.items.helper.BlockItemIT;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.List;
import java.util.stream.Collectors;

import static blusunrize.immersiveengineering.common.register.IEBlocks.METAL_PROPERTIES_NO_OCCLUSION;

public class ITBlocks {
    public static final DeferredRegister<Block> REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, ITLib.MODID);;

    private static final HashMap<String, RegistryObject<? extends Block>> BLOCK_REGISTRY_MAP = new HashMap<>();
    public static Function<String, Block> getBlock = (key) -> BLOCK_REGISTRY_MAP.get(key).get();

    public static final class MetalDevices {
        public static BlockEntry<CokeOvenPreheaterBlock> COKE_OVEN_PREHEATER;
        public static BlockEntry<CreativeBarrelBlock> CREATIVE_BARREL;

        private static void init() {
            COKE_OVEN_PREHEATER = new BlockEntry<>(
                    "coke_oven_preheater",
                    METAL_PROPERTIES_NO_OCCLUSION,
                    p -> new CokeOvenPreheaterBlock(CokeOvenPreheaterBlockEntity::new, p)
            );

            CREATIVE_BARREL = new BlockEntry<>(
                    "creative_barrel",
                    () -> BlockBehaviour.Properties.copy(IEBlocks.MetalDevices.BARREL.get()),
                    p -> new CreativeBarrelBlock(CreativeBarrelBlockEntity::new, p)
            );
        }
    }

    public static final class Stone {
        public static BlockEntry<ReinforcedCokeBrick> REINFORCED_COKE_BRICK;

        private static void init() {
            REINFORCED_COKE_BRICK = new BlockEntry<>(
                    "reinforced_coke_brick",
                    () -> BlockBehaviour.Properties.copy(Blocks.STONE),
                    ReinforcedCokeBrick::new
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
            Function<Block, BlockItemIT> toItem;
            toItem = BlockItemIT::new;
            Function<Block, BlockItemIT> finalToItem = toItem;
            ITItems.REGISTER.register(entry.getId().getPath(), () -> finalToItem.apply(entry.get()));
            BLOCK_REGISTRY_MAP.put(entry.getId().getPath(), (RegistryObject<Block>)entry.getRegObject());
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

        @Override
        public T get() { return regObject.get(); }

        public ResourceLocation getId() { return regObject.getId(); }

        public BlockBehaviour.Properties getProperties() { return properties.get(); }

        @Nonnull
        @Override
        public Item asItem() { return get().asItem(); }

        public RegistryObject<? extends Block> getRegObject() { return regObject; }
    }
}
