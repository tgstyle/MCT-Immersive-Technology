package mctmods.immersivetechnology.core.registration;

import mctmods.immersivetechnology.common.items.helper.ITBaseItem;
import mctmods.immersivetechnology.common.items.ITMBFormationItem;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.Util;
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
    public static final ItemRegObject<ITBaseItem> SALT = simple();

    public static void initItems() { }

    public static List<Item> getITItems() { return REGISTER.getEntries().stream().map(RegistryObject::get).collect(Collectors.toList()); }

    public static void init(IEventBus event) {
        initItems();
        REGISTER.register(event);
        ITEM_REGISTRY_MAP.put("it_formation_tool", IT_FORMATION_TOOL.regObject);
        ITEM_REGISTRY_MAP.put("salt", SALT.regObject);
    }

    private static ITItems.ItemRegObject<ITBaseItem> simple() { return simple($ -> { }, $ -> { }); }

    private static ITItems.ItemRegObject<ITBaseItem> simple(Consumer<Item.Properties> makeProps, Consumer<ITBaseItem> processItem) { return register("salt", () -> Util.make(new ITBaseItem(Util.make(new Item.Properties(), makeProps)), processItem)); }

    static <T extends Item> ITItems.ItemRegObject<T> register(String name, Supplier<? extends T> make) { return new ITItems.ItemRegObject<>(REGISTER.register(name, make)); }

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
