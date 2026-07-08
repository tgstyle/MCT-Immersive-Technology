package mctmods.immersivetechnology.common.fluids.helper;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

public record ITArrayFluidHandler(IFluidTank[] internal, boolean allowDrain, boolean allowFill, Runnable afterTransfer) implements IFluidHandler {

    public ITArrayFluidHandler(IFluidTank internal, boolean allowDrain, boolean allowFill, Runnable afterTransfer) {
        this(new IFluidTank[]{internal}, allowDrain, allowFill, afterTransfer);
    }

    public static ITArrayFluidHandler drainOnly(IFluidTank internal, Runnable afterTransfer) {
        return new ITArrayFluidHandler(internal, true, false, afterTransfer);
    }

    public static ITArrayFluidHandler fillOnly(IFluidTank internal, Runnable afterTransfer) {
        return new ITArrayFluidHandler(internal, false, true, afterTransfer);
    }

    public int getTanks() { return this.internal.length; }

    @NotNull public FluidStack getFluidInTank(int tank) { return this.internal[tank].getFluid(); }

    public int getTankCapacity(int tank) { return this.internal[tank].getCapacity(); }

    public boolean isFluidValid(int tank, @NotNull FluidStack stack) { return this.internal[tank].isFluidValid(stack); }

    public int fill(@NotNull FluidStack resource, IFluidHandler.@NotNull FluidAction action) {
        if (!this.allowFill || resource.isEmpty()) { return 0; }
        FluidStack remaining = resource.copy();
        IFluidTank existing = null;
        for (IFluidTank tank : this.internal) {
            if (net.neoforged.neoforge.fluids.FluidStack.isSameFluidSameComponents(tank.getFluid(), remaining)) {
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

    @NotNull public FluidStack drain(@NotNull FluidStack resource, IFluidHandler.@NotNull FluidAction action) {
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

    @NotNull public FluidStack drain(int maxDrain, IFluidHandler.@NotNull FluidAction action) {
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
