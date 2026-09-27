package mctmods.immersivetechnology.core.util;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;

public class SyncEnergyStorage extends AveragingEnergyStorage {
    private final Runnable onChanged;

    public SyncEnergyStorage(int capacity, Runnable onChanged) {
        super(capacity);
        this.onChanged = onChanged;
    }

    public SyncEnergyStorage(int capacity, int maxIO, Runnable onChanged) {
        this(capacity, onChanged);
        this.maxReceive = maxIO;
        this.maxExtract = maxIO;
    }

    @Override public int receiveEnergy(int maxReceive, boolean simulate) {
        int received = super.receiveEnergy(maxReceive, simulate);
        if (received > 0 && !simulate) { onChanged.run(); }
        return received;
    }

    @Override public int extractEnergy(int maxExtract, boolean simulate) {
        int extracted = super.extractEnergy(maxExtract, simulate);
        if (extracted > 0 && !simulate) { onChanged.run(); }
        return extracted;
    }

    @Override public void setStoredEnergy(int stored) {
        super.setStoredEnergy(stored);
        onChanged.run();
    }
}
