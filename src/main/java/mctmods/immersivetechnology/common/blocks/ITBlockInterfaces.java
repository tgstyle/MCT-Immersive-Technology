package mctmods.immersivetechnology.common.blocks;

import mctmods.immersivetechnology.api.client.MechanicalEnergyAnimation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;

public class ITBlockInterfaces {
    public interface IMechanicalEnergy {
        boolean isValid();

        boolean isMechanicalEnergyTransmitter(EnumFacing facing);
        boolean isMechanicalEnergyReceiver(EnumFacing facing);

        int getSpeed();
        float getTorqueMultiplier();
        MechanicalEnergyAnimation getAnimation();
    }
}
