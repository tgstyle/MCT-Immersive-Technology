package mctmods.immersivetechnology.core.registration;

import com.immersiveconvergence.api.registration.ItemEntry;
import com.immersiveconvergence.api.block.BaseItem;
import mctmods.immersivetechnology.common.items.FormationTool;
import mctmods.immersivetechnology.core.lib.Reference;
import net.minecraft.Util;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ModItems {
    public static final DeferredRegister.Items REGISTER = DeferredRegister.createItems(Reference.MODID);

    private static final HashMap<String, Supplier<? extends Item>> ITEM_REGISTRY_MAP = new HashMap<>();

    public static HashMap<String, Supplier<? extends Item>> getItemRegistryMap() { return ITEM_REGISTRY_MAP; }

    public static final ItemEntry<FormationTool> FORMATION_TOOL = register("formation_tool", FormationTool::new);
    public static final ItemEntry<BaseItem> SALT = simple();
    public static final ItemEntry<BaseItem> FUEL_ELEMENT = item("fuel_element");
    public static final ItemEntry<BaseItem> SPENT_ELEMENT = item("spent_element");
    public static final ItemEntry<BaseItem> INGOT_CHROMIUM = item("ingot_chromium");
    public static final ItemEntry<BaseItem> INGOT_DEPLETED_URANIUM = item("ingot_depleted_uranium");
    public static final ItemEntry<BaseItem> INGOT_ENRICHED_URANIUM = item("ingot_enriched_uranium");
    public static final ItemEntry<BaseItem> INGOT_MAGNESIUM = item("ingot_magnesium");
    public static final ItemEntry<BaseItem> INGOT_MAGNOX = item("ingot_magnox");
    public static final ItemEntry<BaseItem> INGOT_PLUTONIUM = item("ingot_plutonium");
    public static final ItemEntry<BaseItem> INGOT_STAINLESS_STEEL = item("ingot_stainless_steel");
    public static final ItemEntry<BaseItem> DUST_CHROMIUM = item("dust_chromium");
    public static final ItemEntry<BaseItem> DUST_MAGNESIUM = item("dust_magnesium");
    public static final ItemEntry<BaseItem> DUST_MAGNOX = item("dust_magnox");
    public static final ItemEntry<BaseItem> DUST_SPENT_FUEL = item("dust_spent_fuel");
    public static final ItemEntry<BaseItem> DUST_STAINLESS_STEEL = item("dust_stainless_steel");
    public static final ItemEntry<BaseItem> RAW_CHROMIUM = item("raw_chromium");
    public static final ItemEntry<BaseItem> RAW_MAGNESIUM = item("raw_magnesium");
    public static final ItemEntry<BaseItem> NUGGET_NUCLEAR_WASTE = item("nugget_nuclear_waste");
    public static final ItemEntry<BaseItem> NUGGET_STAINLESS_STEEL = item("nugget_stainless_steel");
    public static final ItemEntry<BaseItem> PLATE_MAGNOX = item("plate_magnox");
    public static final ItemEntry<BaseItem> PLATE_STAINLESS_STEEL = item("plate_stainless_steel");
    public static final ItemEntry<BaseItem> STICK_STAINLESS_STEEL = item("stick_stainless_steel");
    public static final ItemEntry<BaseItem> PELLET_URANIUM = item("pellet_uranium");

    public static void initItems() { }

    public static List<Item> getITItems() { return REGISTER.getEntries().stream().map(Supplier::get).collect(Collectors.toList()); }

    public static void init(IEventBus event) {
        initItems();
        REGISTER.register(event);
    }

    private static ItemEntry<BaseItem> simple() { return simple($ -> { }, $ -> { }); }

    private static ItemEntry<BaseItem> simple(Consumer<Item.Properties> makeProps, Consumer<BaseItem> processItem) { return register("salt", () -> Util.make(new BaseItem(Util.make(new Item.Properties(), makeProps)), processItem)); }

    private static ItemEntry<BaseItem> item(String name) { return register(name, () -> new BaseItem(new Item.Properties())); }

    static <T extends Item> ItemEntry<T> register(String name, Supplier<? extends T> make) {
        ItemEntry<T> entry = new ItemEntry<>(REGISTER.register(name, make));
        ITEM_REGISTRY_MAP.put(name, entry.regObject());
        return entry;
    }
}
