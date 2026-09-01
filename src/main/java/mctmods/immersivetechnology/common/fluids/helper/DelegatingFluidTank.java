package mctmods.immersivetechnology.common.fluids.helper;

import net.neoforged.neoforge.fluids.FluidStack;
import com.immersiveconvergence.api.util.MarkableFluidTank;

import javax.annotation.Nonnull;

public class DelegatingFluidTank extends MarkableFluidTank {
    private final MarkableFluidTank delegate;

    public DelegatingFluidTank(MarkableFluidTank delegate) {
        super(delegate.getCapacity(), v -> {});
        this.delegate = delegate;
    }

    @Override @Nonnull public FluidStack getFluid() { return delegate.getFluid(); }

    @Override public int getFluidAmount() { return delegate.getFluidAmount(); }

    @Override public int getCapacity() { return delegate.getCapacity(); }

    @Override public boolean isFluidValid(@Nonnull FluidStack stack) { return delegate.isFluidValid(stack); }

    @Override public boolean isEmpty() { return delegate.isEmpty(); }

    @Override public int fill(@Nonnull FluidStack resource, @Nonnull FluidAction action) { return delegate.fill(resource, action); }

    @Override @Nonnull public FluidStack drain(@Nonnull FluidStack resource, @Nonnull FluidAction action) { return delegate.drain(resource, action); }

    @Override @Nonnull public FluidStack drain(int maxDrain, @Nonnull FluidAction action) { return delegate.drain(maxDrain, action); }
}
