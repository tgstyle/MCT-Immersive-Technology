package mctmods.immersivetechnology.common.util.loot;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelper;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelperMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.common.blocks.multiblocks.blockimpl.ComponentInstance;
import blusunrize.immersiveengineering.common.blocks.multiblocks.blockimpl.MultiblockBEHelperCommon;
import blusunrize.immersiveengineering.common.blocks.multiblocks.blockimpl.MultiblockBEHelperMaster;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

public class ITMultiblockDropsLootContainer extends LootPoolSingletonContainer {
    protected ITMultiblockDropsLootContainer(int weightIn, int qualityIn, LootItemCondition[] conditionsIn, LootItemFunction[] functionsIn) { super(weightIn, qualityIn, conditionsIn, functionsIn); }

    @Override
    protected void createItemStack(@Nonnull Consumer<ItemStack> output, LootContext context) {
        if (context.getParamOrNull(LootContextParams.BLOCK_ENTITY) instanceof IMultiblockBE<?> multiblockBE) {
            final IMultiblockBEHelper<?> helper = multiblockBE.getHelper();
            dropOriginalBlock(helper, context, output);
            dropExtraItems(helper, output);
        }
    }

    private void dropOriginalBlock(IMultiblockBEHelper<?> helper, LootContext context, Consumer<ItemStack> drop) {
        final BlockState originalBlock = helper.getOriginalBlock(context.getLevel());
        Utils.getDrops(originalBlock, context, drop);
    }

    private <S extends IMultiblockState> void dropExtraItems(IMultiblockBEHelper<S> iHelper, Consumer<ItemStack> drop) {
        if (!(iHelper instanceof MultiblockBEHelperCommon<S> helper)) return;
        final IMultiblockBEHelperMaster<S> masterHelper = helper.getMasterHelperDuringDisassembly();
        if (masterHelper instanceof MultiblockBEHelperMaster<S> nonAPI) for (final ComponentInstance<?> component : nonAPI.getComponentInstances()) { component.dropExtraItems(drop); }
        if (masterHelper != null) helper.getMultiblock().logic().dropExtraItems(masterHelper.getState(), drop);
    }

    public static LootPoolSingletonContainer.Builder<?> builder() { return simpleBuilder(ITMultiblockDropsLootContainer::new); }

    @Override
    public @NotNull LootPoolEntryType getType() { return ITLootFunctions.MULTIBLOCK_DROPS.get(); }

    public static class Serializer extends LootPoolSingletonContainer.Serializer<ITMultiblockDropsLootContainer> {
        @Nonnull
        @Override
        protected ITMultiblockDropsLootContainer deserialize(@Nonnull JsonObject json, @Nonnull JsonDeserializationContext context, int weight, int quality, @Nonnull LootItemCondition[] conditions, @Nonnull LootItemFunction[] functions) { return new ITMultiblockDropsLootContainer(weight, quality, conditions, functions); }
    }
}
