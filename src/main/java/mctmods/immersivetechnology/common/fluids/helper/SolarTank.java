package mctmods.immersivetechnology.common.fluids.helper;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import java.util.function.Consumer;

public record SolarTank(MarkableFluidTank input, MarkableFluidTank output) {
    public static final int TANK_CAPACITY = 12000;

    public SolarTank(Consumer<Void> markDirty) {
        this(new MarkableFluidTank(TANK_CAPACITY, markDirty), new MarkableFluidTank(TANK_CAPACITY, markDirty));
    }

    public SolarTank(Consumer<Void> markDirty, int inputCapacity, int outputCapacity) {
        this(new MarkableFluidTank(inputCapacity, markDirty), new MarkableFluidTank(outputCapacity, markDirty));
    }

    public static SolarTank makeClient() {
        return new SolarTank(v -> {});
    }

    public CompoundTag toNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.put("input", this.input.writeToNBT(provider, new CompoundTag()));
        tag.put("output", this.output.writeToNBT(provider, new CompoundTag()));
        return tag;
    }

    public void readNBT(CompoundTag tag, HolderLookup.Provider provider) {
        this.input.readFromNBT(provider, tag.getCompound("input"));
        this.output.readFromNBT(provider, tag.getCompound("output"));
    }

    public int getCapacity() {
        return TANK_CAPACITY;
    }
}
