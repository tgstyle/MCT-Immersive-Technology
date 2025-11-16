package mctmods.immersivetechnology.common.blocks.helper;

import net.minecraftforge.energy.IEnergyStorage;

public class ITWrappingEnergyStorage implements IEnergyStorage {
    protected IEnergyStorage backing;
    boolean canExtract;
    boolean canReceive;
    Runnable onChange;

    public ITWrappingEnergyStorage(IEnergyStorage backing, boolean canReceive, boolean canExtract, Runnable onChange) {
        this.backing = backing;
        this.canReceive = canReceive;
        this.canExtract = canExtract;
        this.onChange = onChange;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!canReceive) { return 0; }
        int ret = backing.receiveEnergy(maxReceive, simulate);
        if (!simulate && ret != 0 && onChange != null) { onChange.run(); }
        return ret;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (!canExtract) { return 0; }
        int ret = backing.extractEnergy(maxExtract, simulate);
        if (!simulate && ret != 0 && onChange != null) { onChange.run(); }
        return ret;
    }

    @Override
    public int getEnergyStored() { return backing.getEnergyStored(); }

    @Override
    public int getMaxEnergyStored() { return backing.getMaxEnergyStored(); }

    @Override
    public boolean canExtract() { return canExtract; }

    @Override
    public boolean canReceive() { return canReceive; }
}
