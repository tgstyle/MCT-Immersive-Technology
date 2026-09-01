package mctmods.immersivetechnology.core.util;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import blusunrize.immersiveengineering.api.IEApiDataComponents;
import java.util.function.Supplier;

public class Utils {



    public static ItemStack insertStackIntoInventory(Supplier<IItemHandler> ref, ItemStack stack, boolean simulate) { return insertStackIntoInventory(ref.get(), stack, simulate); }

    public static ItemStack insertStackIntoInventory(IItemHandler handler, ItemStack stack, boolean simulate) {
        if (handler == null || stack.isEmpty()) { return stack; }
        return ItemHandlerHelper.insertItem(handler, stack.copy(), simulate);
    }

}
