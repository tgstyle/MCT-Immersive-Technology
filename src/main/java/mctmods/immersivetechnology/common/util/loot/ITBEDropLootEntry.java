package mctmods.immersivetechnology.common.util.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

import mctmods.immersivetechnology.common.blocks.helper.ITBlockInterfaces;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ITBEDropLootEntry extends LootPoolSingletonContainer {
    protected ITBEDropLootEntry(int weightIn, int qualityIn, LootItemCondition[] conditionsIn, LootItemFunction[] functionsIn) { super(weightIn, qualityIn, conditionsIn, functionsIn); }

    protected void createItemStack(@Nonnull Consumer<ItemStack> output, LootContext context) {
        if (context.hasParam(LootContextParams.BLOCK_ENTITY)) {
            BlockEntity te = context.getParamOrNull(LootContextParams.BLOCK_ENTITY);
            if (te instanceof ITBlockInterfaces.IBlockEntityDrop) { ((ITBlockInterfaces.IBlockEntityDrop)te).getBlockEntityDrop(context, output); }
        }
    }

    public static LootPoolSingletonContainer.Builder<?> builder() { return simpleBuilder(ITBEDropLootEntry::new); }

    @Nonnull
    public LootPoolEntryType getType() { return ITLootFunctions.TILE_DROP.get(); }

    public static class Serializer extends LootPoolSingletonContainer.Serializer<ITBEDropLootEntry> {
        @Nonnull
        protected ITBEDropLootEntry deserialize(@Nonnull JsonObject json, @Nonnull JsonDeserializationContext context, int weight, int quality, @Nonnull LootItemCondition[] conditions, @Nonnull LootItemFunction[] functions) { return new ITBEDropLootEntry(weight, quality, conditions, functions); }
    }
}
