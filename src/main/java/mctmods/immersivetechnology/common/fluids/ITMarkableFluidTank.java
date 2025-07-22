package mctmods.immersivetechnology.common.fluids;

import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.function.Consumer;

public class ITMarkableFluidTank extends FluidTank {
    private final Consumer<Void> markDirty;

    public ITMarkableFluidTank(int capacity, Consumer<Void> markDirty) {
        super(capacity);
        this.markDirty = markDirty;
    }

    @Override
    protected void onContentsChanged() {
        markDirty.accept(null);
    }
}
