package mctmods.immersivetechnology.core.util.loot;

import java.util.function.Consumer;
import javax.annotation.Nonnull;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mctmods.immersivetechnology.common.blocks.helper.BlockInterfaces;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class BEDropLootEntry extends LootPoolSingletonContainer {

    public static final MapCodec<BEDropLootEntry> CODEC = RecordCodecBuilder.mapCodec(instance ->
            LootPoolSingletonContainer.singletonFields(instance).apply(instance, BEDropLootEntry::new)
    );

    protected BEDropLootEntry(int weightIn, int qualityIn, java.util.List<LootItemCondition> conditionsIn, java.util.List<LootItemFunction> functionsIn) {
        super(weightIn, qualityIn, conditionsIn, functionsIn);
    }

    @Override
    protected void createItemStack(@Nonnull Consumer<ItemStack> output, LootContext context) {
        if (context.hasParam(LootContextParams.BLOCK_ENTITY)) {
            BlockEntity te = context.getParamOrNull(LootContextParams.BLOCK_ENTITY);
            if (te instanceof BlockInterfaces.IBlockEntityDrop) {
                ((BlockInterfaces.IBlockEntityDrop) te).getBlockEntityDrop(context, output);
            }
        }
    }

    public static LootPoolSingletonContainer.Builder<?> builder() {
        return simpleBuilder(BEDropLootEntry::new);
    }

    @Nonnull
    @Override
    public LootPoolEntryType getType() {
        return LootFunctions.TILE_DROP.get();
    }
}
