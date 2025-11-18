package mctmods.immersivetechnology.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ITUtils {
    public static void dropStackAtPos(Level world, BlockPos pos, ItemStack stack) {
        Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack);
    }
}
