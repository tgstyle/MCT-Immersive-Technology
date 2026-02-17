package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class TileEntityConveyorVerticalAlternative extends TileEntityConveyorBeltAlternative {

    public TileEntityConveyorVerticalAlternative() {}

    @Override public boolean mirrorFacingOnPlacement(@Nonnull EntityLivingBase placer) { return false; }

    @Override public boolean canHammerRotate(@Nonnull EnumFacing side, float hitX, float hitY, float hitZ, @Nonnull EntityLivingBase entity) { return true; }

    @Override @Nonnull public float[] getBlockBounds() {
        float minX = this.facing == EnumFacing.EAST ? 0.875F : 0.0F;
        float maxX = this.facing == EnumFacing.WEST ? 0.125F : 1.0F;
        float minZ = this.facing == EnumFacing.SOUTH ? 0.875F : 0.0F;
        float maxZ = this.facing == EnumFacing.NORTH ? 0.125F : 1.0F;
        return new float[]{minX, 0.0F, minZ, maxX, 1.0F, maxZ};
    }

    @Override @Nonnull public List<AxisAlignedBB> getAdvancedColisionBounds() {
        return Collections.emptyList();
    }
}
