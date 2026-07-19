package mctmods.immersivetechnology.core.util.loot;

import java.util.function.Consumer;
import javax.annotation.Nonnull;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mctmods.immersivetechnology.common.blocks.helper.BlockInterfaces;
import mctmods.immersivetechnology.core.util.inventory.IDropInventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class DropInventoryLootEntry extends LootPoolSingletonContainer {

    public static final MapCodec<DropInventoryLootEntry> CODEC = RecordCodecBuilder.mapCodec(instance ->
            LootPoolSingletonContainer.singletonFields(instance).apply(instance, DropInventoryLootEntry::new)
    );

    protected DropInventoryLootEntry(int weightIn, int qualityIn, java.util.List<LootItemCondition> conditionsIn, java.util.List<LootItemFunction> functionsIn) {
        super(weightIn, qualityIn, conditionsIn, functionsIn);
    }

    @Override
    protected void createItemStack(@Nonnull Consumer<ItemStack> output, LootContext context) {
        if (context.hasParam(LootContextParams.BLOCK_ENTITY)) {
            BlockEntity te = context.getParamOrNull(LootContextParams.BLOCK_ENTITY);
            if (te instanceof BlockInterfaces.IGeneralMultiblock dummyBE) {
                te = (BlockEntity) dummyBE.master();
            }
            if (te instanceof IDropInventory itInvBE) {
                if (itInvBE.getDroppedItems() != null) {
                    itInvBE.getDroppedItems().forEach(output);
                }
            }
        }
    }

    public static LootPoolSingletonContainer.Builder<?> builder() {
        return simpleBuilder(DropInventoryLootEntry::new);
    }

    @Nonnull
    @Override
    public LootPoolEntryType getType() {
        return LootFunctions.DROP_INVENTORY.get();
    }
}
