package mctmods.immersivetechnology.common.util;

import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import net.minecraftforge.energy.IEnergyStorage;

public class ITFluxStorage extends FluxStorage implements IEnergyStorage {

    public boolean canExtract = true, canReceive = true;

    public ITFluxStorage(int capacity, int limitReceive, int limitExtract) {
        super(capacity, limitReceive, limitExtract);
    }

    public ITFluxStorage(int capacity, int limitTransfer) {
        super(capacity, limitTransfer);
    }

    public ITFluxStorage(int capacity) {
        super(capacity);
    }

    public ITFluxStorage(int capacity, boolean canExtract, boolean canReceive) {
        super(capacity);
        this.canExtract = canExtract;
        this.canReceive = canReceive;
        if (!canExtract) limitExtract = 0;
        if (!canReceive) limitReceive = 0;
    }

    @Override
    public boolean canExtract() {
        return canExtract;
    }

    @Override
    public boolean canReceive() {
        return canReceive;
    }
}
