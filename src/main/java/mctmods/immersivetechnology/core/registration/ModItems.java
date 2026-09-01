package mctmods.immersivetechnology.core.registration;

import com.immersiveconvergence.api.block.BaseItem;
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
    public static final ItemRegObject<BaseItem> FUEL_ELEMENT = item("fuel_element");
    public static final ItemRegObject<BaseItem> SPENT_ELEMENT = item("spent_element");
    public static final ItemRegObject<BaseItem> INGOT_CHROMIUM = item("ingot_chromium");
    public static final ItemRegObject<BaseItem> INGOT_MAGNESIUM = item("ingot_magnesium");
    public static final ItemRegObject<BaseItem> INGOT_MAGNOX = item("ingot_magnox");
    public static final ItemRegObject<BaseItem> INGOT_PLUTONIUM = item("ingot_plutonium");
    public static final ItemRegObject<BaseItem> INGOT_STAINLESS_STEEL = item("ingot_stainless_steel");
    public static final ItemRegObject<BaseItem> DUST_CHROMIUM = item("dust_chromium");
    public static final ItemRegObject<BaseItem> DUST_MAGNESIUM = item("dust_magnesium");
    public static final ItemRegObject<BaseItem> DUST_MAGNOX = item("dust_magnox");
    public static final ItemRegObject<BaseItem> DUST_SPENT_FUEL = item("dust_spent_fuel");
    public static final ItemRegObject<BaseItem> DUST_STAINLESS_STEEL = item("dust_stainless_steel");
    public static final ItemRegObject<BaseItem> RAW_CHROMIUM = item("raw_chromium");
    public static final ItemRegObject<BaseItem> RAW_MAGNESIUM = item("raw_magnesium");
    public static final ItemRegObject<BaseItem> NUGGET_NUCLEAR_WASTE = item("nugget_nuclear_waste");
    public static final ItemRegObject<BaseItem> NUGGET_STAINLESS_STEEL = item("nugget_stainless_steel");
    public static final ItemRegObject<BaseItem> PLATE_MAGNOX = item("plate_magnox");
    public static final ItemRegObject<BaseItem> PLATE_STAINLESS_STEEL = item("plate_stainless_steel");
    public static final ItemRegObject<BaseItem> STICK_STAINLESS_STEEL = item("stick_stainless_steel");
    public static final ItemRegObject<BaseItem> PELLET_URANIUM = item("pellet_uranium");

    public static void initItems() { }

    public static List<Item> getITItems() { return REGISTER.getEntries().stream().map(Supplier::get).collect(Collectors.toList()); }

    public static void init(IEventBus event) {
        initItems();
        REGISTER.register(event);
    }

    private static ModItems.ItemRegObject<BaseItem> simple() { return simple($ -> { }, $ -> { }); }

    private static ModItems.ItemRegObject<BaseItem> simple(Consumer<Item.Properties> makeProps, Consumer<BaseItem> processItem) { return register("salt", () -> Util.make(new BaseItem(Util.make(new Item.Properties(), makeProps)), processItem)); }

    private static ModItems.ItemRegObject<BaseItem> item(String name) { return register(name, () -> new BaseItem(new Item.Properties())); }

    static <T extends Item> ModItems.ItemRegObject<T> register(String name, Supplier<? extends T> make) {
        ModItems.ItemRegObject<T> entry = new ModItems.ItemRegObject<>(REGISTER.register(name, make));
        ITEM_REGISTRY_MAP.put(name, entry.regObject);
        return entry;
    }

    public record ItemRegObject<T extends Item>(DeferredItem<T> regObject) implements Supplier<T>, ItemLike {
        @Override @Nonnull public T get() { return regObject.get(); }

        @Override @Nonnull public Item asItem() { return regObject.get(); }
        public ResourceLocation getId() { return regObject.getId(); }
    }
}
