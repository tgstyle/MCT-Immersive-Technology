package mctmods.immersivetechnology.common.multiblocks.gui.helper;

import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IMultiblockComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import mctmods.immersivetechnology.core.registration.MenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

public record MultiblockGui<S extends IMultiblockState>(MenuTypes.MultiblockContainer<S, ?> menu) implements IMultiblockComponent<S> {
    public InteractionResult click(IMultiblockContext<S> ctx, BlockPos posInMultiblock, Player player, InteractionHand hand, BlockHitResult absoluteHit, boolean isClient) {
        if (!isClient && ctx != null) { player.openMenu(this.menu.provide(ctx, posInMultiblock)); }
        return InteractionResult.sidedSuccess(isClient);
    }
}
