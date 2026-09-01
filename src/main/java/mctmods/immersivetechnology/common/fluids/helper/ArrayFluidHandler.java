package mctmods.immersivetechnology.common.fluids.helper;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;

public record ArrayFluidHandler(IFluidTank[] internal, boolean allowDrain, boolean allowFill, Runnable afterTransfer) implements IFluidHandler {

    public ArrayFluidHandler(IFluidTank internal, boolean allowDrain, boolean allowFill, Runnable afterTransfer) {
        this(new IFluidTank[]{internal}, allowDrain, allowFill, afterTransfer);
    }

    public static ArrayFluidHandler drainOnly(IFluidTank internal, Runnable afterTransfer) {
        return new ArrayFluidHandler(internal, true, false, afterTransfer);
    }

    public static ArrayFluidHandler fillOnly(IFluidTank internal, Runnable afterTransfer) {
        return new ArrayFluidHandler(internal, false, true, afterTransfer);
    }

    public int getTanks() { return this.internal.length; }

    @Nonnull public FluidStack getFluidInTank(int tank) { return this.internal[tank].getFluid(); }

    public int getTankCapacity(int tank) { return this.internal[tank].getCapacity(); }

    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) { return this.internal[tank].isFluidValid(stack); }

    public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        if (!this.allowFill || resource.isEmpty()) { return 0; }
        FluidStack remaining = resource.copy();
        IFluidTank existing = null;
        for (IFluidTank tank : this.internal) {
            if (tank.getFluid().isFluidEqual(remaining)) {
                existing = tank;
                break;
            }
        }
        if (existing != null) {
            remaining.shrink(existing.fill(remaining, action));
        } else {
            for (IFluidTank tank : this.internal) {
                int filledHere = tank.fill(remaining, action);
                remaining.shrink(filledHere);
                if (filledHere > 0) { break; }
            }
        }
        int filled = resource.getAmount() - remaining.getAmount();
        if (filled > 0 && action.execute() && afterTransfer != null) { afterTransfer.run(); }
        return filled;
    }

    @Nonnull public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
        if (!this.allowDrain) { return FluidStack.EMPTY; }
        for (IFluidTank tank : this.internal) {
            FluidStack drainedHere = tank.drain(resource, action);
            if (!drainedHere.isEmpty()) {
                if (action.execute() && afterTransfer != null) { afterTransfer.run(); }
                return drainedHere;
            }
        }
        return FluidStack.EMPTY;
    }

    @Nonnull public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        if (!this.allowDrain) { return FluidStack.EMPTY; }
        for (IFluidTank tank : this.internal) {
            FluidStack drainedHere = tank.drain(maxDrain, action);
            if (!drainedHere.isEmpty()) {
                if (action.execute() && afterTransfer != null) { afterTransfer.run(); }
                return drainedHere;
            }
        }
        return FluidStack.EMPTY;
    }
}
