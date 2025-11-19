package mctmods.immersivetechnology.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.minecraftforge.fluids.FluidStack;

import blusunrize.immersiveengineering.api.fluid.FluidUtils;

public class ITUtils {
    public static void dropStackAtPos(Level world, BlockPos pos, ItemStack stack) { Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack); }

    public static FluidStack copyFluidStackWithAmount(FluidStack stack, int amount, boolean stripPressure) { return FluidUtils.copyFluidStackWithAmount(stack, amount, stripPressure); }
}
