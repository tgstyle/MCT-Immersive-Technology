package mctmods.immersivetechnology.core.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import blusunrize.immersiveengineering.api.fluid.IFluidPipe;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;

public class Utils {
    public static void dropStackAtPos(Level world, BlockPos pos, ItemStack stack) { Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack); }

    public static FluidStack copyFluidStackWithAmount(FluidStack stack, int amount, boolean stripPressure) {
        if (stack.isEmpty()) { return FluidStack.EMPTY; }
        FluidStack fs = new FluidStack(stack, amount);
        if (stripPressure && fs.hasTag()) {
            CompoundTag tag = fs.getTag();
            tag.remove(IFluidPipe.NBT_PRESSURIZED);
            if (tag.isEmpty()) { fs.setTag(null); }
        }
        return fs;
    }

    public static boolean isFluidRelatedItemStack(ItemStack stack) {
        if (stack.isEmpty()) { return false; }
        return FluidUtil.getFluidHandler(stack).isPresent();
    }

    public static ItemStack insertStackIntoInventory(CapabilityReference<IItemHandler> to, ItemStack stack, boolean simulate) { return insertStackIntoInventory(to.getNullable(), stack, simulate); }

    public static ItemStack insertStackIntoInventory(IItemHandler handler, ItemStack stack, boolean simulate) {
        if (handler == null || stack.isEmpty()) { return stack; }
        return ItemHandlerHelper.insertItem(handler, stack.copy(), simulate);
    }
}
