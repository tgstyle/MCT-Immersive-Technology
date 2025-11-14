package mctmods.immersivetechnology.common.multiblocks.helper;

import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IMultiblockComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.common.register.IEItems;
import com.google.common.collect.ImmutableList;
import mctmods.immersivetechnology.common.items.FormationTool;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;
import java.util.function.Consumer;

public record ITClearTank<S>(List<BlockPos> pois, Consumer<S> clearAction, Component message) implements IMultiblockComponent<S> {
    public ITClearTank {
        pois = ImmutableList.copyOf(pois);
    }

    @Override
    public InteractionResult click(IMultiblockContext<S> context, BlockPos posInMultiblock, Player player, InteractionHand hand, BlockHitResult absoluteHit, boolean isClient) {
        if (pois.contains(posInMultiblock) && player.isShiftKeyDown()) {
            ItemStack held = player.getItemInHand(hand);
            if (held.getItem() == IEItems.Tools.HAMMER.get() || held.getItem() instanceof FormationTool) {
                if (!isClient) {
                    S state = context.getState();
                    clearAction.accept(state);
                    context.markMasterDirty();
                    context.requestMasterBESync();
                    player.displayClientMessage(message, true);
                }
                return InteractionResult.sidedSuccess(isClient);
            }
        }
        return InteractionResult.PASS;
    }
}
