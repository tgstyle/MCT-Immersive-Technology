package mctmods.immersivetechnology.core.integration.ie;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;

public final class IESideToggle {
    private IESideToggle() {}

    @Nullable public static InteractionResult toggle(Level world, BlockPos pos, Direction side, Player player) {
        if (!(world.getBlockEntity(pos) instanceof IEBlockInterfaces.IConfigurableSides sideConfig)) { return null; }
        return sideConfig.toggleSide(player.isShiftKeyDown() ? side.getOpposite() : side, player) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }
}
