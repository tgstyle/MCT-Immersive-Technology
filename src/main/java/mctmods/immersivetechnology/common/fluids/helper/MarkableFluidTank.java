package mctmods.immersivetechnology.common.fluids.helper;

import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.function.Consumer;

public class MarkableFluidTank extends FluidTank {
    private final Consumer<Void> markDirty;

    public MarkableFluidTank(int capacity, Consumer<Void> markDirty) {
        super(capacity);
        this.markDirty = markDirty;
    }

    @Override protected void onContentsChanged() {
        markDirty.accept(null);
    }

    public static MarkableFluidTank makeClient(int capacity, Consumer<Void> markDirty) {
        return new MarkableFluidTank(capacity, markDirty);
    }
}
