package mctmods.immersivetechnology.api;

import mctmods.immersivetechnology.api.client.MechanicalEnergyAnimation;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.IFluidTank;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public class ITUtils {
	public static IFluidTank[] emptyIFluidTankList = new IFluidTank[0];

	public static final Set<TileEntity> REMOVE_FROM_TICKING = new HashSet<>();

	public static void RemoveDummyFromTicking(TileEntity te) {
		REMOVE_FROM_TICKING.add(te);
	}

	public static float remapRange(float inMin, float inMax, float outMin, float outMax, float value) {
		return outMin + ((value-inMin)/inMax) * (outMax - outMin);
	}
	
	public static boolean AreBlockPosIdentical(BlockPos a, BlockPos b) {
		return a.getX() == b.getX() && a.getY() == b.getY() && a.getZ() == b.getZ();
	}
	
	public static BlockPos LocalOffsetToWorldBlockPos(BlockPos origin, int x, int y, int z, EnumFacing facing) {
		return LocalOffsetToWorldBlockPos(origin, x, y, z, facing, EnumFacing.UP);
	}

	public static BlockPos LocalOffsetToWorldBlockPos(BlockPos origin, int x, int y, int z, EnumFacing facing, boolean mirrored) {
        return LocalOffsetToWorldBlockPos(origin, mirrored? -x : x, y, z, facing, EnumFacing.UP);
    }

	public static BlockPos LocalOffsetToWorldBlockPos(BlockPos origin, int x, int y, int z, EnumFacing facing, EnumFacing up) {
		if(facing.getAxis() == up.getAxis()) throw new IllegalArgumentException("'facing' and 'up' must be perpendicular to each other!");
		switch(up) {
			case UP:
				switch(facing) {
					case SOUTH:
						return origin.add(-x, y, z);
					case NORTH:
						return origin.add(x, y, -z);
					case EAST:
						return origin.add(z, y, x);
					case WEST:
						return origin.add(-z, y, -x);
					default:
						break;
				}
				break;
			case DOWN:
				switch(facing) {
					case SOUTH:
						return origin.add(x, -y, z);
					case NORTH:
						return origin.add(-x, -y, -z);
					case EAST:
						return origin.add(z, -y, -x);
					case WEST:
						return origin.add(-z, -y, x);
					default:
						break;
				}
				break;
			case NORTH:
				switch(facing) {
					case UP:
						return origin.add(-x, z, -y);
					case DOWN:
						return origin.add(x, -z, -y);
					case EAST:
						return origin.add(z, x, -y);
					case WEST:
						return origin.add(-z, -x, -y);
					default:
						break;
				}
				break;
			case SOUTH:
				switch(facing) {
					case UP:
						return origin.add(x, z, y);
					case DOWN:
						return origin.add(-x, -z, y);
					case EAST:
						return origin.add(z, -x, y);
					case WEST:
						return origin.add(-z, x, y);
					default:
						break;
				}
				break;
			case EAST:
				switch(facing) {
					case UP:
						return origin.add(y, z, -x);
					case DOWN:
						return origin.add(y, -z, x);
					case SOUTH:
						return origin.add(y, x, z);
					case NORTH:
						return origin.add(y, -x, -z);
					default:
						break;
				}
				break;
			case WEST:
				switch(facing) {
					case UP:
						return origin.add(-y, z, x);
					case DOWN:
						return origin.add(-y, -z, -x);
					case SOUTH:
						return origin.add(-y, -x, z);
					case NORTH:
						return origin.add(-y, x, -z);
					default:
						break;
				}
				break;
		}
		throw new IllegalArgumentException("This part of the code should never be reached! Has EnumFacing changed ? ");
	}

	public static <T> T First(ArrayList<T> list, Object o) {
		for(T item : list) {
			if(item.equals(o)) return item;
		}
		return null;
	}

	public static double[] smartBoundingBox(double A, double B, double C, double D, double minY, double maxY, EnumFacing fl, EnumFacing fw) {
		double[] boundingArray = new double[6];
		boundingArray[0] = fl == EnumFacing.WEST ? A : fl == EnumFacing.EAST ? B : fw == EnumFacing.EAST ? C : D;
		boundingArray[1] = minY;
		boundingArray[2] = fl == EnumFacing.NORTH ? A : fl == EnumFacing.SOUTH ? B : fw == EnumFacing.SOUTH ? C : D;
		boundingArray[3] = fl == EnumFacing.EAST ? 1 - A : fl == EnumFacing.WEST ? 1 - B : fw == EnumFacing.EAST ? 1 - D : 1 - C;
		boundingArray[4] = maxY;
		boundingArray[5] = fl == EnumFacing.SOUTH ? 1 - A : fl == EnumFacing.NORTH ? 1 - B : fw == EnumFacing.SOUTH ? 1 - D : 1 - C;
		return boundingArray;
	}

	public static double[] alternativeSmartBoundingBox(double A, double B, double C, double D, double minY, double maxY, EnumFacing fl, EnumFacing fw) {
		double[] boundingArray = new double[6];

		boundingArray[0] = fl == EnumFacing.WEST ? 1 - A : fl == EnumFacing.EAST ? A : fw == EnumFacing.EAST ? 1 - C : C;
		boundingArray[1] = minY;
		boundingArray[2] = fl == EnumFacing.NORTH ? 1 - A : fl == EnumFacing.SOUTH ? A : fw == EnumFacing.SOUTH ? 1 - C : C;
		boundingArray[3] = fl == EnumFacing.EAST ? B : fl == EnumFacing.WEST ? 1 - B : fw == EnumFacing.EAST ? 1 - D : D;
		boundingArray[4] = maxY;
		boundingArray[5] = fl == EnumFacing.SOUTH ? B : fl == EnumFacing.NORTH ? 1 - B : fw == EnumFacing.SOUTH ? 1 - D : D;

		return boundingArray;
	}

	public static boolean setRotationAngle(MechanicalEnergyAnimation animation, float rotationSpeed) {
		float oldMomentum = animation.getAnimationMomentum();
		float rotateTo = (animation.getAnimationRotation() + rotationSpeed) % 360;
		animation.setAnimationRotation(rotateTo);
		animation.setAnimationMomentum(rotationSpeed);
		return (oldMomentum != rotationSpeed);
	}

	public static EnumSet<EnumFacing> allSides = EnumSet.allOf(EnumFacing.class);
	public static void improvedMarkBlockForUpdate(World world, BlockPos pos, @Nullable IBlockState newState) {
		improvedMarkBlockForUpdate(world, pos, newState, allSides);
	}

	public static void improvedMarkBlockForUpdate(World world, BlockPos pos, @Nullable IBlockState newState, EnumSet<EnumFacing> directions) {
		IBlockState state = world.getBlockState(pos);
		if(newState == null) newState = state;
		world.notifyBlockUpdate(pos, state, newState, 3);
		if(!ForgeEventFactory.onNeighborNotify(world, pos, newState, EnumSet.allOf(EnumFacing.class), true).isCanceled()) {
			Block blockType = newState.getBlock();
			for(EnumFacing facing : directions) {
				BlockPos toNotify = pos.offset(facing);
				if(world.isBlockLoaded(toNotify)) world.neighborChanged(toNotify, blockType, pos);
			}
			world.updateObservingBlocksAt(pos, blockType);
		}
	}

}