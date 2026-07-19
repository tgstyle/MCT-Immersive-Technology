package mctmods.immersivetechnology.common.fluids.helper;

import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class DelegatingFluidTank extends MarkableFluidTank {
    private final MarkableFluidTank delegate;

    public DelegatingFluidTank(MarkableFluidTank delegate) {
        super(delegate.getCapacity(), v -> {});
        this.delegate = delegate;
    }

    @Override @NotNull public FluidStack getFluid() { return delegate.getFluid(); }

    @Override public int getFluidAmount() { return delegate.getFluidAmount(); }

    @Override public int getCapacity() { return delegate.getCapacity(); }

    @Override public boolean isFluidValid(@NotNull FluidStack stack) { return delegate.isFluidValid(stack); }

    @Override public boolean isEmpty() { return delegate.isEmpty(); }

    @Override public int fill(@NotNull FluidStack resource, @NotNull FluidAction action) { return delegate.fill(resource, action); }

    @Override @NotNull public FluidStack drain(@NotNull FluidStack resource, @NotNull FluidAction action) { return delegate.drain(resource, action); }

    @Override @NotNull public FluidStack drain(int maxDrain, @NotNull FluidAction action) { return delegate.drain(maxDrain, action); }
}
