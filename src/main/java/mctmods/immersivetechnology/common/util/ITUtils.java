package mctmods.immersivetechnology.common.util;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.IFluidTank;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

public class ITUtils {
    public static IFluidTank[] emptyIFluidTankList = new IFluidTank[0];

    public static final Set<TileEntity> REMOVE_FROM_TICKING = new HashSet<>();

    public static void RemoveDummyFromTicking(TileEntity te) { REMOVE_FROM_TICKING.add(te); }

    public static float remapRange(float inMin, float inMax, float outMin, float outMax, float value) { return outMin + ((value - inMin) / inMax) * (outMax - outMin); }

    public static BlockPos LocalOffsetToWorldBlockPos(BlockPos origin, int x, int y, int z, EnumFacing facing) { return LocalOffsetToWorldBlockPos(origin, x, y, z, facing, EnumFacing.UP); }

    public static BlockPos LocalOffsetToWorldBlockPos(BlockPos origin, int x, int y, int z, EnumFacing facing, boolean mirrored) { return LocalOffsetToWorldBlockPos(origin, mirrored ? -x : x, y, z, facing, EnumFacing.UP); }

    public static BlockPos LocalOffsetToWorldBlockPos(BlockPos origin, int x, int y, int z, EnumFacing facing, EnumFacing up) {
        if (facing.getAxis() == up.getAxis()) { throw new IllegalArgumentException("'facing' and 'up' must be perpendicular to each other!"); }
        switch (up) {
            case UP:
                switch (facing) {
                    case SOUTH: return origin.add(-x, y, z);
                    case NORTH: return origin.add(x, y, -z);
                    case EAST: return origin.add(z, y, x);
                    case WEST: return origin.add(-z, y, -x);
                    default: break;
                }
                break;
            case DOWN:
                switch (facing) {
                    case SOUTH: return origin.add(x, -y, z);
                    case NORTH: return origin.add(-x, -y, -z);
                    case EAST: return origin.add(z, -y, -x);
                    case WEST: return origin.add(-z, -y, x);
                    default: break;
                }
                break;
            case NORTH:
                switch (facing) {
                    case UP: return origin.add(-x, z, -y);
                    case DOWN: return origin.add(x, -z, -y);
                    case EAST: return origin.add(z, x, -y);
                    case WEST: return origin.add(-z, -x, -y);
                    default: break;
                }
                break;
            case SOUTH:
                switch (facing) {
                    case UP: return origin.add(x, z, y);
                    case DOWN: return origin.add(-x, -z, y);
                    case EAST: return origin.add(z, -x, y);
                    case WEST: return origin.add(-z, x, y);
                    default: break;
                }
                break;
            case EAST:
                switch (facing) {
                    case UP: return origin.add(y, z, -x);
                    case DOWN: return origin.add(y, -z, x);
                    case SOUTH: return origin.add(y, x, z);
                    case NORTH: return origin.add(y, -x, -z);
                    default: break;
                }
                break;
            case WEST:
                switch (facing) {
                    case UP: return origin.add(-y, z, x);
                    case DOWN: return origin.add(-y, -z, -x);
                    case SOUTH: return origin.add(-y, -x, z);
                    case NORTH: return origin.add(-y, x, -z);
                    default: break;
                }
                break;
        }
        throw new IllegalArgumentException("This part of the code should never be reached! Has EnumFacing changed?");
    }

    public static void improvedMarkBlockForUpdate(World world, BlockPos pos, @Nullable IBlockState newState, EnumSet<EnumFacing> directions) {
        IBlockState state = world.getBlockState(pos);
        if (newState == null) { newState = state; }
        world.notifyBlockUpdate(pos, state, newState, 3);
        if (!ForgeEventFactory.onNeighborNotify(world, pos, newState, EnumSet.allOf(EnumFacing.class), true).isCanceled()) {
            Block blockType = newState.getBlock();
            for (EnumFacing facing : directions) {
                BlockPos toNotify = pos.offset(facing);
                if (world.isBlockLoaded(toNotify)) { world.neighborChanged(toNotify, blockType, pos); }
            }
            world.updateObservingBlocksAt(pos, blockType);
        }
    }

    public static <T> T make(Supplier<T> pSupplier) {
        return pSupplier.get();
    }

    public static AxisAlignedBB rotateAABB(AxisAlignedBB aabb, EnumFacing facing) {
        return rotateAABB(aabb, facing, false);
    }

    public static AxisAlignedBB rotateAABB(AxisAlignedBB aabb, EnumFacing facing, boolean mirrored) {
        double minX = aabb.minX;
        double minY = aabb.minY;
        double minZ = aabb.minZ;
        double maxX = aabb.maxX;
        double maxY = aabb.maxY;
        double maxZ = aabb.maxZ;
        double temp;

        switch (facing) {
            case SOUTH:
                temp = minX;
                minX = 1 - maxX;
                maxX = 1 - temp;
                temp = minZ;
                minZ = 1 - maxZ;
                maxZ = 1 - temp;
                break;
            case WEST:
                temp = minX;
                minX = minZ;
                minZ = 1 - maxX;
                maxX = maxZ;
                maxZ = 1 - temp;
                break;
            case EAST:
                temp = minX;
                minX = 1 - maxZ;
                maxZ = maxX;
                maxX = 1 - minZ;
                minZ = temp;
                break;
            default: // NORTH
                break;
        }

        if (mirrored) {
            if (facing.getAxis() == EnumFacing.Axis.Z) {
                temp = minX;
                minX = 1 - maxX;
                maxX = 1 - temp;
            } else {
                temp = minZ;
                minZ = 1 - maxZ;
                maxZ = 1 - temp;
            }
        }

        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
