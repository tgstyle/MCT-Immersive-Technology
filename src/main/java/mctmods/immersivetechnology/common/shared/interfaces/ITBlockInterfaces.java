package mctmods.immersivetechnology.common.shared.interfaces;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;

public class ITBlockInterfaces {
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
