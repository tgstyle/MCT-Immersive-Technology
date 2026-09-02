package mctmods.immersivetechnology.core.registration;

import com.immersiveconvergence.api.registration.ItemEntry;
import com.immersiveconvergence.api.block.BaseItem;
import mctmods.immersivetechnology.common.items.FormationTool;
import mctmods.immersivetechnology.core.lib.Reference;
import net.minecraft.Util;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ModItems {
    public static final DeferredRegister<Item> REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, Reference.MODID);

    private static final HashMap<String, RegistryObject<? extends Item>> ITEM_REGISTRY_MAP = new HashMap<>();

    public static HashMap<String, RegistryObject<? extends Item>> getItemRegistryMap() { return ITEM_REGISTRY_MAP; }

    public static final ItemEntry<FormationTool> FORMATION_TOOL = register("formation_tool", FormationTool::new);
    public static final ItemEntry<BaseItem> SALT = simple();

    public static void initItems() { }

    public static List<Item> getITItems() { return REGISTER.getEntries().stream().map(RegistryObject::get).collect(Collectors.toList()); }

    public static void init(IEventBus event) {
        initItems();
        REGISTER.register(event);
        ITEM_REGISTRY_MAP.put("formation_tool", FORMATION_TOOL.regObject());
        ITEM_REGISTRY_MAP.put("salt", SALT.regObject());
    }

    private static ItemEntry<BaseItem> simple() { return simple($ -> { }, $ -> { }); }

    private static ItemEntry<BaseItem> simple(Consumer<Item.Properties> makeProps, Consumer<BaseItem> processItem) { return register("salt", () -> Util.make(new BaseItem(Util.make(new Item.Properties(), makeProps)), processItem)); }

    static <T extends Item> ItemEntry<T> register(String name, Supplier<? extends T> make) { return new ItemEntry<>(REGISTER.register(name, make)); }
}
