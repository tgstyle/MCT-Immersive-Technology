package mctmods.immersivetechnology.common.fluids.helper;

import javax.annotation.Nonnull;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;

public record ITArrayFluidHandler(IFluidTank[] internal, boolean allowDrain, boolean allowFill, Runnable afterTransfer) implements IFluidHandler {
    public ITArrayFluidHandler(IFluidTank internal, boolean allowDrain, boolean allowFill, Runnable afterTransfer) {
        this(new IFluidTank[]{internal}, allowDrain, allowFill, afterTransfer);
    }

    public ITArrayFluidHandler(boolean allowDrain, boolean allowFill, Runnable afterTransfer, IFluidTank... tanks) {
        this(tanks, allowDrain, allowFill, afterTransfer);
    }

    public static ITArrayFluidHandler drainOnly(IFluidTank internal, Runnable afterTransfer) {
        return new ITArrayFluidHandler(internal, true, false, afterTransfer);
    }

    public static ITArrayFluidHandler fillOnly(IFluidTank internal, Runnable afterTransfer) {
        return new ITArrayFluidHandler(internal, false, true, afterTransfer);
    }

    public int getTanks() {
        return this.internal.length;
    }

    @Nonnull
    public FluidStack getFluidInTank(int tank) {
        return this.internal[tank].getFluid();
    }

    public int getTankCapacity(int tank) {
        return this.internal[tank].getCapacity();
    }

    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
        return this.internal[tank].isFluidValid(stack);
    }

    public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        if (this.allowFill && !resource.isEmpty()) {
            FluidStack remaining = resource.copy();
            IFluidTank existing = null;

            for(IFluidTank tank : this.internal) {
                if (tank.getFluid().isFluidEqual(remaining)) {
                    existing = tank;
                    break;
                }
            }

            if (existing != null) {
                remaining.shrink(existing.fill(remaining, action));
            } else {
                for(IFluidTank tank : this.internal) {
                    int filledHere = tank.fill(remaining, action);
                    remaining.shrink(filledHere);
                    if (filledHere > 0) {
                        break;
                    }
                }
            }

            if (resource.getAmount() != remaining.getAmount()) {
                this.afterTransfer.run();
            }

            return resource.getAmount() - remaining.getAmount();
        } else {
            return 0;
        }
    }

    @Nonnull
    public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
        if (!this.allowDrain) {
            return FluidStack.EMPTY;
        } else {
            for(IFluidTank tank : this.internal) {
                FluidStack drainedHere = tank.drain(resource, action);
                if (!drainedHere.isEmpty()) {
                    this.afterTransfer.run();
                    return drainedHere;
                }
            }

            return FluidStack.EMPTY;
        }
    }

    @Nonnull
    public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        if (!this.allowDrain) {
            return FluidStack.EMPTY;
        } else {
            for(IFluidTank tank : this.internal) {
                FluidStack drainedHere = tank.drain(maxDrain, action);
                if (!drainedHere.isEmpty()) {
                    this.afterTransfer.run();
                    return drainedHere;
                }
            }

            return FluidStack.EMPTY;
        }
    }
}

