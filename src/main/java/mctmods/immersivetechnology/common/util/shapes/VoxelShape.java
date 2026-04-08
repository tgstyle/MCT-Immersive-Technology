package mctmods.immersivetechnology.common.util.shapes;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import mctmods.immersivetechnology.common.util.ITMth;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import java.util.List;

public abstract class VoxelShape {
    protected final DiscreteVoxelShape shape;

    VoxelShape(DiscreteVoxelShape shape) { this.shape = shape; }

    protected double get(EnumFacing.Axis axis, int index) { return this.getCoords(axis).getDouble(index); }

    protected abstract DoubleList getCoords(EnumFacing.Axis axis);

    public boolean isEmpty() { return this.shape.isEmpty(); }

    public VoxelShape optimize() {
        VoxelShape[] avoxelshape = new VoxelShape[]{Shapes.empty()};
        this.forAllBoxes((p1, p2, p3, p4, p5, p6) -> avoxelshape[0] = Shapes.joinUnoptimized(avoxelshape[0], Shapes.box(p1, p2, p3, p4, p5, p6), BooleanOp.OR));
        return avoxelshape[0];
    }

    public void forAllEdges(Shapes.DoubleLineConsumer pAction) {
        this.shape.forAllEdges((p1, p2, p3, p4, p5, p6) -> pAction.consume(this.get(EnumFacing.Axis.X, p1), this.get(EnumFacing.Axis.Y, p2), this.get(EnumFacing.Axis.Z, p3), this.get(EnumFacing.Axis.X, p4), this.get(EnumFacing.Axis.Y, p5), this.get(EnumFacing.Axis.Z, p6)), true);
    }

    public void forAllBoxes(Shapes.DoubleLineConsumer action) {
        DoubleList xCoords = this.getCoords(EnumFacing.Axis.X);
        DoubleList yCoords = this.getCoords(EnumFacing.Axis.Y);
        DoubleList zCoords = this.getCoords(EnumFacing.Axis.Z);
        this.shape.forAllBoxes((p1, p2, p3, p4, p5, p6) -> action.consume(xCoords.getDouble(p1), yCoords.getDouble(p2), zCoords.getDouble(p3), xCoords.getDouble(p4), yCoords.getDouble(p5), zCoords.getDouble(p6)), true);
    }

    public List<AxisAlignedBB> toAabbs() {
        List<AxisAlignedBB> list = Lists.newArrayList();
        this.forAllBoxes((p1, p2, p3, p4, p5, p6) -> list.add(new AxisAlignedBB(p1, p2, p3, p4, p5, p6)));
        return list;
    }

    protected int findIndex(EnumFacing.Axis axis, double position) { return ITMth.binarySearch(0, this.shape.getSize(axis) + 1, i -> position < this.get(axis, i)) - 1; }

    public double min(EnumFacing.Axis axis) {
        int i = this.shape.firstFull(axis);
        return i >= this.shape.getSize(axis) ? Double.POSITIVE_INFINITY : this.get(axis, i);
    }

    public double max(EnumFacing.Axis axis) {
        int i = this.shape.lastFull(axis);
        return i <= 0 ? Double.NEGATIVE_INFINITY : this.get(axis, i);
    }

    public AxisAlignedBB bounds() {
        if (this.isEmpty()) { throw new UnsupportedOperationException("No bounds for empty shape."); }
        return new AxisAlignedBB(this.min(EnumFacing.Axis.X), this.min(EnumFacing.Axis.Y), this.min(EnumFacing.Axis.Z), this.max(EnumFacing.Axis.X), this.max(EnumFacing.Axis.Y), this.max(EnumFacing.Axis.Z));
    }

    public double min(EnumFacing.Axis axis, double primaryPosition, double secondaryPosition) {
        EnumFacing.Axis axis1 = AxisCycle.FORWARD.cycle(axis);
        EnumFacing.Axis axis2 = AxisCycle.BACKWARD.cycle(axis);
        int i = this.findIndex(axis1, primaryPosition);
        int j = this.findIndex(axis2, secondaryPosition);
        int k = this.shape.firstFull(axis, i, j);
        return k >= this.shape.getSize(axis) ? Double.POSITIVE_INFINITY : this.get(axis, k);
    }

    public double max(EnumFacing.Axis axis, double primaryPosition, double secondaryPosition) {
        EnumFacing.Axis axis1 = AxisCycle.FORWARD.cycle(axis);
        EnumFacing.Axis axis2 = AxisCycle.BACKWARD.cycle(axis);
        int i = this.findIndex(axis1, primaryPosition);
        int j = this.findIndex(axis2, secondaryPosition);
        int k = this.shape.lastFull(axis, i, j);
        return k <= 0 ? Double.NEGATIVE_INFINITY : this.get(axis, k);
    }

    public String toString() { return this.isEmpty() ? "EMPTY" : "VoxelShape[" + this.bounds() + "]"; }
}
