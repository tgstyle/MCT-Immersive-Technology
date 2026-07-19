package mctmods.immersivetechnology.client.utils;

import mctmods.immersivetechnology.core.util.TranslationKey;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;

public class ClientUtils {
    public static Component formatFluidStack(FluidStack fluid) {
        if (fluid.isEmpty()) { return Component.translatable(TranslationKey.GUI_EMPTY.text()); }
        return Component.literal(fluid.getHoverName().getString() + ": " + fluid.getAmount() + "mB");
    }

    public static ItemStack getPickBlock(BlockState state) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Level level = mc.level;
        BlockHitResult hit = new BlockHitResult(Vec3.ZERO, Direction.DOWN, BlockPos.ZERO, false);
        if (level != null && player != null) {
            try {
                ItemStack picked = state.getBlock().getCloneItemStack(state, hit, level, BlockPos.ZERO, player);
                if (!picked.isEmpty()) { return picked; }
            } catch (Exception ignored) { }
        }
        return new ItemStack(state.getBlock());
    }
}
