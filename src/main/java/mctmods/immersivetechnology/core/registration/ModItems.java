package mctmods.immersivetechnology.core.registration;

import mctmods.immersivetechnology.common.items.helper.BaseItem;
import mctmods.immersivetechnology.common.items.FormationTool;
import mctmods.immersivetechnology.core.lib.Reference;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import javax.annotation.Nonnull;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ModItems {
    public static final DeferredRegister.Items REGISTER = DeferredRegister.createItems(Reference.MODID);

    private static final HashMap<String, Supplier<? extends Item>> ITEM_REGISTRY_MAP = new HashMap<>();

    public static HashMap<String, Supplier<? extends Item>> getItemRegistryMap() { return ITEM_REGISTRY_MAP; }

    public static final ItemRegObject<FormationTool> FORMATION_TOOL = register("formation_tool", FormationTool::new);
    public static final ItemRegObject<BaseItem> SALT = simple();

    public static void initItems() { }

    public static List<Item> getITItems() { return REGISTER.getEntries().stream().map(Supplier::get).collect(Collectors.toList()); }

    public static void init(IEventBus event) {
        initItems();
        REGISTER.register(event);
        ITEM_REGISTRY_MAP.put("formation_tool", FORMATION_TOOL.regObject);
        ITEM_REGISTRY_MAP.put("salt", SALT.regObject);
    }

    private static ModItems.ItemRegObject<BaseItem> simple() { return simple($ -> { }, $ -> { }); }

    private static ModItems.ItemRegObject<BaseItem> simple(Consumer<Item.Properties> makeProps, Consumer<BaseItem> processItem) { return register("salt", () -> Util.make(new BaseItem(Util.make(new Item.Properties(), makeProps)), processItem)); }

    static <T extends Item> ModItems.ItemRegObject<T> register(String name, Supplier<? extends T> make) { return new ModItems.ItemRegObject<>(REGISTER.register(name, make)); }

    public record ItemRegObject<T extends Item>(DeferredItem<T> regObject) implements Supplier<T>, ItemLike {
        @Override @Nonnull public T get() { return regObject.get(); }

        @Override @Nonnull public Item asItem() { return regObject.get(); }
        public ResourceLocation getId() { return regObject.getId(); }
    }
}
