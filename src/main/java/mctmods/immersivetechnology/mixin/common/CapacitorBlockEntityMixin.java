package mctmods.immersivetechnology.mixin.common;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.metal.CapacitorBlockEntity;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nonnull;

@Mixin(CapacitorBlockEntity.class)
public abstract class CapacitorBlockEntityMixin implements IEBlockInterfaces.IPlayerInteraction {
    @Override public boolean interact(@Nonnull Direction side, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull ItemStack heldItem, float hitX, float hitY, float hitZ) {
        if (Utils.isHammer(heldItem)) {
            Direction activeSide = player.isShiftKeyDown() ? side.getOpposite() : side;
            return ((IEBlockInterfaces.IConfigurableSides) this).toggleSide(activeSide, player);
        }
        return false;
    }
}
