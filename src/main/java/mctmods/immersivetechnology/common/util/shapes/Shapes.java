package mctmods.immersivetechnology.common.util.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import mctmods.immersivetechnology.common.util.ITMth;
import mctmods.immersivetechnology.common.util.ITUtils;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

public final class Shapes {
    private static final VoxelShape BLOCK = ITUtils.make(() -> {
        DiscreteVoxelShape discretevoxelshape = new BitSetDiscreteVoxelShape(1, 1, 1);
        discretevoxelshape.fill(0, 0, 0);
        return new CubeVoxelShape(discretevoxelshape);
    });

    private static final VoxelShape EMPTY = new ArrayVoxelShape(new BitSetDiscreteVoxelShape(0, 0, 0), new DoubleArrayList(new double[]{0.0D}), new DoubleArrayList(new double[]{0.0D}), new DoubleArrayList(new double[]{0.0D}));

    public static VoxelShape empty() { return EMPTY; }

    public static VoxelShape block() { return BLOCK; }

    public static VoxelShape box(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        if (!(minX > maxX) && !(minY > maxY) && !(minZ > maxZ)) { return create(minX, minY, minZ, maxX, maxY, maxZ); }
        throw new IllegalArgumentException("The min values need to be smaller or equals to the max values");
    }

    public static VoxelShape create(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        if (!(maxX - minX < 1.0E-7D) && !(maxY - minY < 1.0E-7D) && !(maxZ - minZ < 1.0E-7D)) {
            int i = findBits(minX, maxX);
            int j = findBits(minY, maxY);
            int k = findBits(minZ, maxZ);
            if (i >= 0 && j >= 0 && k >= 0) {
                if (i == 0 && j == 0 && k == 0) { return block(); }
                int l = 1 << i;
                int i1 = 1 << j;
                int j1 = 1 << k;
                BitSetDiscreteVoxelShape bitsetdiscretevoxelshape = BitSetDiscreteVoxelShape.withFilledBounds(l, i1, j1, (int)Math.round(minX * (double)l), (int)Math.round(minY * (double)i1), (int)Math.round(minZ * (double)j1), (int)Math.round(maxX * (double)l), (int)Math.round(maxY * (double)i1), (int)Math.round(maxZ * (double)j1));
                return new CubeVoxelShape(bitsetdiscretevoxelshape);
            }
            return new ArrayVoxelShape(BLOCK.shape, DoubleArrayList.wrap(new double[]{minX, maxX}), DoubleArrayList.wrap(new double[]{minY, maxY}), DoubleArrayList.wrap(new double[]{minZ, maxZ}));
        }
        return empty();
    }

    public static VoxelShape create(AxisAlignedBB aabb) { return create(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ); }

    private static int findBits(double minBits, double maxBits) {
        if (!(minBits < -1.0E-7D) && !(maxBits > 1.0000001D)) {
            for (int i = 0; i <= 3; i++) {
                int j = 1 << i;
                double d0 = minBits * (double)j;
                double d1 = maxBits * (double)j;
                boolean flag = Math.abs(d0 - (double)Math.round(d0)) < 1.0E-7D * (double)j;
                boolean flag1 = Math.abs(d1 - (double)Math.round(d1)) < 1.0E-7D * (double)j;
                if (flag && flag1) { return i; }
            }
        }
        return -1;
    }

    private static long lcm(int aa, int bb) { return ITMth.lcm(aa, bb); }

    public static VoxelShape or(VoxelShape shape1, VoxelShape shape2) { return join(shape1, shape2, BooleanOp.OR); }

    public static VoxelShape or(VoxelShape shape1, VoxelShape... others) {
        VoxelShape result = shape1;
        for (VoxelShape other : others) { result = or(result, other); }
        return result;
    }

    public static VoxelShape join(VoxelShape shape1, VoxelShape shape2, BooleanOp function) { return joinUnoptimized(shape1, shape2, function).optimize(); }

    public static VoxelShape joinUnoptimized(VoxelShape shape1, VoxelShape shape2, BooleanOp function) {
        if (function.apply(false, false)) { throw new IllegalArgumentException(); }
        if (shape1 == shape2) { return function.apply(true, true) ? shape1 : empty(); }
        boolean flag = function.apply(true, false);
        boolean flag1 = function.apply(false, true);
        if (shape1.isEmpty()) { return flag1 ? shape2 : empty(); }
        if (shape2.isEmpty()) { return flag ? shape1 : empty(); }
        IndexMerger indexmerger = createIndexMerger(1, shape1.getCoords(EnumFacing.Axis.X), shape2.getCoords(EnumFacing.Axis.X), flag, flag1);
        IndexMerger indexmerger1 = createIndexMerger(indexmerger.size() - 1, shape1.getCoords(EnumFacing.Axis.Y), shape2.getCoords(EnumFacing.Axis.Y), flag, flag1);
        IndexMerger indexmerger2 = createIndexMerger((indexmerger.size() - 1) * (indexmerger1.size() - 1), shape1.getCoords(EnumFacing.Axis.Z), shape2.getCoords(EnumFacing.Axis.Z), flag, flag1);
        BitSetDiscreteVoxelShape bitsetdiscretevoxelshape = BitSetDiscreteVoxelShape.join(shape1.shape, shape2.shape, indexmerger, indexmerger1, indexmerger2, function);
        return (indexmerger instanceof DiscreteCubeMerger && indexmerger1 instanceof DiscreteCubeMerger && indexmerger2 instanceof DiscreteCubeMerger) ? new CubeVoxelShape(bitsetdiscretevoxelshape) : new ArrayVoxelShape(bitsetdiscretevoxelshape, indexmerger.getList(), indexmerger1.getList(), indexmerger2.getList());
    }

    private static IndexMerger createIndexMerger(int size, DoubleList list1, DoubleList list2, boolean excludeUpper, boolean excludeLower) {
        int i = list1.size() - 1;
        int j = list2.size() - 1;
        if (list1 instanceof CubePointRange && list2 instanceof CubePointRange) {
            long k = lcm(i, j);
            if ((long)size * k <= 256L) { return new DiscreteCubeMerger(i, j); }
        }
        if (list1.getDouble(i) < list2.getDouble(0) - 1.0E-7D) { return new NonOverlappingMerger(list1, list2, false); }
        if (list2.getDouble(j) < list1.getDouble(0) - 1.0E-7D) { return new NonOverlappingMerger(list2, list1, true); }
        return (i == j && java.util.Objects.equals(list1, list2)) ? new IdenticalMerger(list1) : new IndirectMerger(list1, list2, excludeUpper, excludeLower);
    }

    public interface DoubleLineConsumer {
        void consume(double minX, double minY, double minZ, double maxX, double maxY, double maxZ);
    }
}
