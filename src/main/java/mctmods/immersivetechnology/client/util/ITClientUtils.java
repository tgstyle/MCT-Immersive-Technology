package mctmods.immersivetechnology.client.util;

import mctmods.immersivetechnology.core.util.TranslationKey;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;

public class ITClientUtils {
    public static Component formatFluidStack(FluidStack fluid) {
        if (fluid.isEmpty()) { return Component.translatable(TranslationKey.GUI_EMPTY.text()); }
        return Component.literal(fluid.getDisplayName().getString() + ": " + fluid.getAmount() + "mB");
    }

    public static int getDarkenedTextColour(int colour) {
        int r = (colour >> 16 & 255) / 4;
        int g = (colour >> 8 & 255) / 4;
        int b = (colour & 255) / 4;
        return r << 16 | g << 8 | b;
    }

    public static ItemStack getPickBlock(BlockState state) {
        Player player = Minecraft.getInstance().player;
        BlockHitResult hit = new BlockHitResult(Vec3.ZERO, Direction.DOWN, BlockPos.ZERO, false);
        try {
            ItemStack picked = state.getBlock().getCloneItemStack(state, hit, EmptyBlockGetter.INSTANCE, BlockPos.ZERO, player);
            if (!picked.isEmpty()) { return picked; }
        } catch (Exception ignored) { }
        return new ItemStack(state.getBlock());
    }
}
