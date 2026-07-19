package mctmods.immersivetechnology.core.util.loot;

import java.util.function.Supplier;

import mctmods.immersivetechnology.core.lib.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@EventBusSubscriber(modid = Reference.MODID, bus = Bus.MOD)
public class LootFunctions {
    private static final DeferredRegister<LootPoolEntryType> ENTRY_REGISTER;
    public static final RegistryObject<LootPoolEntryType> DROP_INVENTORY;
    public static final RegistryObject<LootPoolEntryType> TILE_DROP;

    public static void init(IEventBus bus) { ENTRY_REGISTER.register(bus); }

    private static RegistryObject<LootPoolEntryType> registerEntry(String id, Supplier<Serializer<? extends LootPoolEntryContainer>> serializer) { return ENTRY_REGISTER.register(id, () -> new LootPoolEntryType(serializer.get())); }

    static {
        ENTRY_REGISTER = DeferredRegister.create(BuiltInRegistries.LOOT_POOL_ENTRY_TYPE.key(), Reference.MODID);
        DROP_INVENTORY = registerEntry("drop_inv", DropInventoryLootEntry.Serializer::new);
        TILE_DROP = registerEntry("tile_drop", BEDropLootEntry.Serializer::new);
    }
}
