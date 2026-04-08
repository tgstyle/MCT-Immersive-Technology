package mctmods.immersivetechnology.common.util.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleList;

import net.minecraft.util.EnumFacing;

public class ArrayVoxelShape extends VoxelShape {
    private final DoubleList xs;
    private final DoubleList ys;
    private final DoubleList zs;

    ArrayVoxelShape(DiscreteVoxelShape pShape, DoubleList pXs, DoubleList pYs, DoubleList pZs) {
        super(pShape);
        int i = pShape.getXSize() + 1;
        int j = pShape.getYSize() + 1;
        int k = pShape.getZSize() + 1;
        if (i == pXs.size() && j == pYs.size() && k == pZs.size()) {
            this.xs = pXs;
            this.ys = pYs;
            this.zs = pZs;
        }
        else { throw new IllegalArgumentException("Lengths of point arrays must be consistent with the size of the VoxelShape."); }
    }

    protected DoubleList getCoords(EnumFacing.Axis pAxis) {
        switch (pAxis) {
            case X: return this.xs;
            case Y: return this.ys;
            case Z: return this.zs;
            default: throw new IllegalArgumentException();
        }
    }
}
