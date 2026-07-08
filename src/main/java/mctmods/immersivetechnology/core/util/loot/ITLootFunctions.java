package mctmods.immersivetechnology.core.util.loot;

import java.util.function.Supplier;

import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import com.mojang.serialization.MapCodec;

public class ITLootFunctions {
    private static final DeferredRegister<LootPoolEntryType> ENTRY_REGISTER;
    public static final DeferredHolder<LootPoolEntryType, LootPoolEntryType> DROP_INVENTORY;
    public static final DeferredHolder<LootPoolEntryType, LootPoolEntryType> TILE_DROP;

    public static void init(IEventBus bus) { ENTRY_REGISTER.register(bus); }

    private static DeferredHolder<LootPoolEntryType, LootPoolEntryType> registerEntry(String id, Supplier<MapCodec<? extends LootPoolEntryContainer>> codec) {
        return ENTRY_REGISTER.register(id, () -> new LootPoolEntryType(codec.get()));
    }

    static {
        ENTRY_REGISTER = DeferredRegister.create(BuiltInRegistries.LOOT_POOL_ENTRY_TYPE.key(), ITLib.MODID);
        DROP_INVENTORY = registerEntry("drop_inv", () -> ITDropInventoryLootEntry.CODEC);
        TILE_DROP = registerEntry("tile_drop", () -> ITBEDropLootEntry.CODEC);
    }
}
