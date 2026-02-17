package mctmods.immersivetechnology.common.blocks.metal.conveyors;

import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorTile;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ConveyorUncontrolledAlternative extends ConveyorBasicAlternative {

    @Override public boolean isActive(TileEntity tile) {
        if (tile == null) return true;
        return runTimer > 0;
    }

    @Override public void onEntityCollision(TileEntity tile, Entity entity, EnumFacing facing) {
        if (entity instanceof EntityItem) {
            runTimer = IDLE_TIME_TICKS;
            World world = tile.getWorld();
            if (!world.isRemote && world.getTotalWorldTime() - lastUpdateTick > 4) {
                tile.markDirty();
                IBlockState state = world.getBlockState(tile.getPos());
                world.notifyBlockUpdate(tile.getPos(), state, state, 3);
                lastUpdateTick = world.getTotalWorldTime();
            }
        }
        BlockPos pos = tile.getPos();
        ConveyorDirection conveyorDirection = getConveyorDirection();
        float heightLimit = conveyorDirection == ConveyorDirection.HORIZONTAL ? HORIZONTAL_HEIGHT_LIMIT : SLOPED_HEIGHT_LIMIT;
        double height = entity.posY - pos.getY();
        if (entity.isDead || height < 0D || height >= heightLimit || (entity instanceof EntityPlayer && entity.isSneaking())) return;

        Vec3d vec = getDirection(tile, entity, facing);
        entity.motionX = vec.x;
        entity.motionY = vec.y;
        entity.motionZ = vec.z;
        if (entity.fallDistance < MAX_FALL_RESET) entity.fallDistance = 0.0F;

        int offsetX = facing.getXOffset();
        int offsetZ = facing.getZOffset();
        double nextCenterX = pos.getX() + offsetX + 0.5D;
        double nextCenterZ = pos.getZ() + offsetZ + 0.5D;
        double distX = Math.abs(nextCenterX - entity.posX);
        double distZ = Math.abs(nextCenterZ - entity.posZ);
        boolean contact = facing.getAxis() == Axis.Z ? distZ < CONTACT_DIST : distX < CONTACT_DIST;

        if (contact) {
            if (conveyorDirection == ConveyorDirection.UP) {
                IBlockState state = tile.getWorld().getBlockState(new BlockPos(pos.getX() + offsetX, pos.getY() + 1, pos.getZ() + offsetZ));
                if (!state.isFullBlock()) {
                    double move = UP_PUSH;
                    entity.setPosition(entity.posX + move * offsetX, entity.posY + move, entity.posZ + move * offsetZ);
                }
            }
            BlockPos nextPos = new BlockPos(pos.getX() + offsetX, pos.getY(), pos.getZ() + offsetZ);
            TileEntity te = Utils.getExistingTileEntity(tile.getWorld(), nextPos);
            if (!(te instanceof IConveyorTile)) ConveyorHandler.revertMagnetSupression(entity, (IConveyorTile)tile);
        } else {
            ConveyorHandler.applyMagnetSupression(entity, (IConveyorTile)tile);
        }

        if (entity instanceof EntityItem && entity.ticksExisted > 1) {
            EntityItem item = (EntityItem)entity;
            if (!contact) item.setNoDespawn();
            else handleInsertion(tile, item, facing, conveyorDirection, distX, distZ);
        }
    }
}
