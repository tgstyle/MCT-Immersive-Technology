package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class TileEntityConveyorVerticalAlternative extends TileEntityConveyorBeltAlternative {

    private static final ResourceLocation DEFAULT_VERTICAL = new ResourceLocation("immersiveengineering", "vertical");

    public TileEntityConveyorVerticalAlternative() {}

    @Override @Nonnull public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
        return super.writeToNBT(compound);
    }

    @Override public void readFromNBT(@Nonnull NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (getConveyorSubtype() == null) {
            setConveyorSubtype(ConveyorHandler.getConveyor(DEFAULT_VERTICAL, this));
        }
    }

    @Override public void onEntityCollision(@Nonnull World world, @Nonnull Entity entity) {
        if (getConveyorSubtype() != null) {
            getConveyorSubtype().onEntityCollision(this, entity, facing);
        }
    }

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
