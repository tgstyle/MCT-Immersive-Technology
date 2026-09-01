package mctmods.immersivetechnology.core.util.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

import com.immersiveconvergence.api.block.BlockInterfaces;
import com.immersiveconvergence.api.util.IItemDropProvider;
import mctmods.immersivetechnology.core.util.inventory.InventoryHandler;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

public class DropInventoryLootEntry extends LootPoolSingletonContainer {
    protected DropInventoryLootEntry(int weightIn, int qualityIn, LootItemCondition[] conditionsIn, LootItemFunction[] functionsIn) { super(weightIn, qualityIn, conditionsIn, functionsIn); }

    protected void createItemStack(@Nonnull Consumer<ItemStack> output, LootContext context) {
        if (context.hasParam(LootContextParams.BLOCK_ENTITY)) {
            BlockEntity te = context.getParamOrNull(LootContextParams.BLOCK_ENTITY);
            if (te instanceof BlockInterfaces.IGeneralMultiblock dummyBE) {
                te = (BlockEntity)dummyBE.master();
            }
            if (te instanceof IItemDropProvider itInvBE) {
                if (itInvBE.getDroppedItems() != null) { itInvBE.getDroppedItems().forEach(output); return; }
            }
            if (te != null) {
                IItemHandler itemHandler = te.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve().orElse(null);
                if (itemHandler instanceof InventoryHandler itHandler) {
                    for (int i = 0; i < itHandler.getSlots(); ++i) {
                        if (!itHandler.getStackInSlot(i).isEmpty()) {
                            output.accept(itHandler.getStackInSlot(i));
                            itHandler.setStackInSlot(i, ItemStack.EMPTY);
                        }
                    }
                }
            }
        }
    }

    public static LootPoolSingletonContainer.Builder<?> builder() { return simpleBuilder(DropInventoryLootEntry::new); }

    @Nonnull public LootPoolEntryType getType() { return LootFunctions.DROP_INVENTORY.get(); }

    public static class Serializer extends LootPoolSingletonContainer.Serializer<DropInventoryLootEntry> {
        @Nonnull protected DropInventoryLootEntry deserialize(@Nonnull JsonObject json, @Nonnull JsonDeserializationContext context, int weight, int quality, @Nonnull LootItemCondition[] conditions, @Nonnull LootItemFunction[] functions) { return new DropInventoryLootEntry(weight, quality, conditions, functions); }
    }
}
