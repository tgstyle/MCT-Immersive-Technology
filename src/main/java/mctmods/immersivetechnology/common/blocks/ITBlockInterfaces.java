package mctmods.immersivetechnology.common.blocks;

import java.util.List;

import mctmods.immersivetechnology.api.client.MechanicalEnergyAnimation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;

public class ITBlockInterfaces {
    public interface IMechanicalEnergy {
        boolean isValid();

        boolean isMechanicalEnergyTransmitter(EnumFacing facing);
        boolean isMechanicalEnergyReceiver(EnumFacing facing);

        int getSpeed();
        float getTorqueMultiplier();
        MechanicalEnergyAnimation getAnimation();
    }

    public interface IBlockBounds {
        float[] getBlockBounds();
    }

    public interface IAdvancedCollisionBounds extends IBlockBounds {
        List<AxisAlignedBB> getAdvancedCollisionBounds();
    }

    public interface IAdvancedSelectionBounds extends IBlockBounds {
        List<AxisAlignedBB> getAdvancedSelectionBounds();

        boolean isOverrideBox(AxisAlignedBB var1, EntityPlayer var2, RayTraceResult var3, List<AxisAlignedBB> var4);
    }
}
