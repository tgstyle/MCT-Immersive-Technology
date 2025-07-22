package mctmods.immersivetechnology.common.blocks.multiblocks.gui.helper;

import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IMultiblockComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import mctmods.immersivetechnology.core.registration.ITMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

public record ITMultiblockGui<S extends IMultiblockState>(ITMenuTypes.MultiblockContainer<S, ?> menu) implements IMultiblockComponent<S>
{
    public ITMultiblockGui(ITMenuTypes.MultiblockContainer<S, ?> menu) {
        this.menu = menu;
    }

    public InteractionResult click(IMultiblockContext<S> ctx, BlockPos posInMultiblock, Player player, InteractionHand hand, BlockHitResult absoluteHit, boolean isClient) {
        if (!isClient) {
            player.openMenu(this.menu.provide(ctx, posInMultiblock));
        }

        return InteractionResult.SUCCESS;
    }

    public ITMenuTypes.MultiblockContainer<S, ?> getMenu() {
        return this.menu;
    }
}
