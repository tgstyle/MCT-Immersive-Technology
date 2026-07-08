package mctmods.immersivetechnology.common.multiblocks.gui.helper;

import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IMultiblockComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import mctmods.immersivetechnology.core.registration.ITMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

public record ITMultiblockGui<S extends IMultiblockState>(ITMenuTypes.MultiblockContainer<S, ?> menu) implements IMultiblockComponent<S> {
    public ItemInteractionResult click(IMultiblockContext<S> ctx, BlockPos posInMultiblock, Player player, InteractionHand hand, BlockHitResult absoluteHit, boolean isClient) {
        if (!isClient && ctx != null) { player.openMenu(this.menu.provide(ctx, posInMultiblock)); }
        return ItemInteractionResult.sidedSuccess(isClient);
    }
}
