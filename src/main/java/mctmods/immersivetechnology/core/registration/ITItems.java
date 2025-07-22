package mctmods.immersivetechnology.core.registration;

import mctmods.immersivetechnology.common.blocks.multiblocks.ITGasTurbine;
import mctmods.immersivetechnology.common.blocks.multiblocks.ITSteamTurbine;
import mctmods.immersivetechnology.common.items.helper.ITBaseItem;
import mctmods.immersivetechnology.common.items.ITMBFormationItem;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ITItems {
    public static final DeferredRegister<Item> REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, ITLib.MODID);

    private static final HashMap<String, RegistryObject<? extends Item>> ITEM_REGISTRY_MAP = new HashMap<>();
    public static Function<String, Item> getItem = (key) -> ITEM_REGISTRY_MAP.get(key).get();

    public static HashMap<String, RegistryObject<? extends Item>> getItemRegistryMap() { return ITEM_REGISTRY_MAP; }

    public static final ItemRegObject<ITMBFormationItem> IT_FORMATION_TOOL = register("it_formation_tool", ITMBFormationItem::new);

    public static void initItems() { }

    public static List<Item> getITItems() { return REGISTER.getEntries().stream().map(RegistryObject::get).collect(Collectors.toList()); }

    public static void init(IEventBus event) {
        initItems();
        REGISTER.register(event);
        ITEM_REGISTRY_MAP.put("it_formation_tool", IT_FORMATION_TOOL.regObject);
    }

    private static <T> Consumer<T> nothing() { return $ -> { }; }

    private static ITItems.ItemRegObject<ITBaseItem> simpleWithStackSize(String name, int maxSize) { return simple(name, p -> p.stacksTo(maxSize), i -> { }); }

    private static ITItems.ItemRegObject<ITBaseItem> simple(String name) { return simple(name, $ -> { }, $ -> { }); }

    private static ITItems.ItemRegObject<ITBaseItem> simple(String name, Consumer<Item.Properties> makeProps, Consumer<ITBaseItem> processItem) { return register(name, () -> Util.make(new ITBaseItem(Util.make(new Item.Properties(), makeProps)), processItem)); }

    private static <T extends Item> ITItems.ItemRegObject<T> simple(Supplier<T> make) { return register("it_formation_tool", make); }

    static <T extends Item> ITItems.ItemRegObject<T> register(String name, Supplier<? extends T> make) { return new ITItems.ItemRegObject<>(REGISTER.register(name, make)); }

    private static <T extends Item> ITItems.ItemRegObject<T> of(T existing) { return new ITItems.ItemRegObject<>(RegistryObject.create(BuiltInRegistries.ITEM.getKey(existing), ForgeRegistries.ITEMS)); }

    public record ItemRegObject<T extends Item>(RegistryObject<T> regObject) implements Supplier<T>, ItemLike {
        @Override
        @Nonnull
        public T get() { return regObject.get(); }

        @Nonnull
        @Override
        public Item asItem() { return regObject.get(); }
        public ResourceLocation getId() { return regObject.getId(); }
    }
}
