package mctmods.immersivetechnology.core.util;

import com.immersiveconvergence.api.util.ICFluidUtils;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

public class FluidContainers {
    public static boolean emptyBucket(IFluidHandler tank, IItemHandlerModifiable inv, int filledSlot, int emptySlot) {
        ItemStack filledContainer = inv.getStackInSlot(filledSlot);
        if (filledContainer.isEmpty()) { return false; }
        FluidActionResult result = ICFluidUtils.tryEmptyContainer(filledContainer, tank, FluidType.BUCKET_VOLUME, FluidAction.SIMULATE);
        if (!result.isSuccess()) { return false; }
        ItemStack emptyContainer = result.getResult();
        ItemStack outputStack = inv.getStackInSlot(emptySlot);
        if (ItemOutputs.overflows(outputStack, emptyContainer)) { return false; }
        result = ICFluidUtils.tryEmptyContainer(filledContainer, tank, FluidType.BUCKET_VOLUME, FluidAction.EXECUTE);
        filledContainer.shrink(1);
        inv.setStackInSlot(filledSlot, filledContainer);
        ItemOutputs.place(inv, emptySlot, outputStack, result.getResult());
        return true;
    }
}
