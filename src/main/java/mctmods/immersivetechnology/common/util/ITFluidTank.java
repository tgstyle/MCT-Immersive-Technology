package mctmods.immersivetechnology.common.util;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ITFluidTank extends FluidTank {

	public interface TankListener {
		void TankContentsChanged();
	}

	TankListener listener;

	public ITFluidTank(int capacity, @Nonnull TankListener listener) {
		this(null, capacity, listener);
	}

	public ITFluidTank(@Nullable FluidStack fluidStack, int capacity, @Nonnull TankListener listener) {
		super(fluidStack, capacity);
		this.listener = listener;
	}

	public ITFluidTank(Fluid fluid, int amount, int capacity, @Nonnull TankListener listener) {
		this(new FluidStack(fluid, amount), capacity, listener);
	}

	@Override
	protected void onContentsChanged() {
		listener.TankContentsChanged();
		super.onContentsChanged();
	}

}