package mctmods.immersivetechnology.mixin.common;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.metal.CapacitorBlockEntity;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CapacitorBlockEntity.class)
public abstract class CapacitorBlockEntityMixin implements IEBlockInterfaces.IPlayerInteraction {
    @Override @NotNull public ItemInteractionResult interact(@NotNull Direction side, @NotNull Player player, @NotNull InteractionHand hand, @NotNull ItemStack heldItem, float hitX, float hitY, float hitZ) {
        if (Utils.isHammer(heldItem)) {
            Direction activeSide = player.isShiftKeyDown() ? side.getOpposite() : side;
            boolean didToggle = ((IEBlockInterfaces.IConfigurableSides) this).toggleSide(activeSide, player);
            return didToggle ? ItemInteractionResult.SUCCESS : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}
