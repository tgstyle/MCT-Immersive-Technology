package mctmods.immersivetechnology.common.fluids.helper;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import java.util.function.Consumer;

public record ITSolarTank(ITMarkableFluidTank input, ITMarkableFluidTank output) {
    public static final int TANK_CAPACITY = 12000;

    public ITSolarTank(Consumer<Void> markDirty) {
        this(new ITMarkableFluidTank(TANK_CAPACITY, markDirty), new ITMarkableFluidTank(TANK_CAPACITY, markDirty));
    }

    public ITSolarTank(Consumer<Void> markDirty, int inputCapacity, int outputCapacity) {
        this(new ITMarkableFluidTank(inputCapacity, markDirty), new ITMarkableFluidTank(outputCapacity, markDirty));
    }

    public static ITSolarTank makeClient() {
        return new ITSolarTank(v -> {});
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
