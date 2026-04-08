package mctmods.immersivetechnology.common.util.shapes;

import mctmods.immersivetechnology.common.util.ITMth;
import net.minecraft.util.EnumFacing;

public abstract class DiscreteVoxelShape {
    private static final EnumFacing.Axis[] AXIS_VALUES = EnumFacing.Axis.values();
    protected final int xSize;
    protected final int ySize;
    protected final int zSize;

    protected DiscreteVoxelShape(int xSize, int ySize, int zSize) {
        if (xSize >= 0 && ySize >= 0 && zSize >= 0) {
            this.xSize = xSize;
            this.ySize = ySize;
            this.zSize = zSize;
        } else { throw new IllegalArgumentException("Need all positive sizes: x: " + xSize + ", y: " + ySize + ", z: " + zSize); }
    }

    public boolean isFullWide(AxisCycle axisCycle, int x, int y, int z) {
        return this.isFullWide(axisCycle.cycle(x, y, z, EnumFacing.Axis.X), axisCycle.cycle(x, y, z, EnumFacing.Axis.Y), axisCycle.cycle(x, y, z, EnumFacing.Axis.Z));
    }

    public boolean isFullWide(int x, int y, int z) { return x >= 0 && y >= 0 && z >= 0 && x < this.xSize && y < this.ySize && z < this.zSize && this.isFull(x, y, z); }

    public boolean isFull(AxisCycle rotation, int x, int y, int z) { return this.isFull(rotation.cycle(x, y, z, EnumFacing.Axis.X), rotation.cycle(x, y, z, EnumFacing.Axis.Y), rotation.cycle(x, y, z, EnumFacing.Axis.Z)); }

    public abstract boolean isFull(int x, int y, int z);

    public abstract void fill(int x, int y, int z);

    public boolean isEmpty() {
        for (EnumFacing.Axis axis : AXIS_VALUES) { if (this.firstFull(axis) >= this.lastFull(axis)) { return true; } }
        return false;
    }

    public abstract int firstFull(EnumFacing.Axis axis);

    public abstract int lastFull(EnumFacing.Axis axis);

    public int firstFull(EnumFacing.Axis axis, int y, int z) {
        int size = this.getSize(axis);
        if (y >= 0 && z >= 0) {
            EnumFacing.Axis axis1 = AxisCycle.FORWARD.cycle(axis);
            EnumFacing.Axis axis2 = AxisCycle.BACKWARD.cycle(axis);
            if (y < this.getSize(axis1) && z < this.getSize(axis2)) {
                AxisCycle cycle = AxisCycle.between(EnumFacing.Axis.X, axis);
                for (int j = 0; j < size; j++) { if (this.isFull(cycle, j, y, z)) { return j; } }
            }
        }
        return size;
    }

    public int lastFull(EnumFacing.Axis axis, int y, int z) {
        if (y >= 0 && z >= 0) {
            EnumFacing.Axis axis1 = AxisCycle.FORWARD.cycle(axis);
            EnumFacing.Axis axis2 = AxisCycle.BACKWARD.cycle(axis);
            if (y < this.getSize(axis1) && z < this.getSize(axis2)) {
                int size = this.getSize(axis);
                AxisCycle cycle = AxisCycle.between(EnumFacing.Axis.X, axis);
                for (int j = size - 1; j >= 0; j--) { if (this.isFull(cycle, j, y, z)) { return j + 1; } }
            }
        }
        return 0;
    }

    public int getSize(EnumFacing.Axis axis) { return ITMth.choose(axis, this.xSize, this.ySize, this.zSize); }

    public int getXSize() { return this.getSize(EnumFacing.Axis.X); }

    public int getYSize() { return this.getSize(EnumFacing.Axis.Y); }

    public int getZSize() { return this.getSize(EnumFacing.Axis.Z); }

    public void forAllEdges(IntLineConsumer consumer, boolean combine) {
        this.forAllAxisEdges(consumer, AxisCycle.NONE, combine);
        this.forAllAxisEdges(consumer, AxisCycle.FORWARD, combine);
        this.forAllAxisEdges(consumer, AxisCycle.BACKWARD, combine);
    }

    private void forAllAxisEdges(IntLineConsumer consumer, AxisCycle axisCycle, boolean combine) {
        AxisCycle inverseCycle = axisCycle.inverse();
        int xMax = this.getSize(inverseCycle.cycle(EnumFacing.Axis.X));
        int yMax = this.getSize(inverseCycle.cycle(EnumFacing.Axis.Y));
        int zMax = this.getSize(inverseCycle.cycle(EnumFacing.Axis.Z));

        for (int x = 0; x <= xMax; ++x) {
            for (int y = 0; y <= yMax; ++y) {
                int zStart = -1;
                for (int z = 0; z <= zMax; ++z) {
                    int fullCount = 0;
                    int parity = 0;

                    for (int dx = 0; dx <= 1; ++dx) {
                        for (int dy = 0; dy <= 1; ++dy) {
                            if (this.isFullWide(inverseCycle, x + dx - 1, y + dy - 1, z)) {
                                ++fullCount;
                                parity ^= dx ^ dy;
                            }
                        }
                    }

                    if (fullCount == 1 || fullCount == 3 || fullCount == 2 && (parity & 1) == 0) {
                        if (combine) {
                            if (zStart == -1) { zStart = z; }
                        } else {
                            consumer.consume(inverseCycle.cycle(x, y, z, EnumFacing.Axis.X), inverseCycle.cycle(x, y, z, EnumFacing.Axis.Y), inverseCycle.cycle(x, y, z, EnumFacing.Axis.Z),
                                    inverseCycle.cycle(x, y, z + 1, EnumFacing.Axis.X), inverseCycle.cycle(x, y, z + 1, EnumFacing.Axis.Y), inverseCycle.cycle(x, y, z + 1, EnumFacing.Axis.Z));
                        }
                    } else if (zStart != -1) {
                        consumer.consume(inverseCycle.cycle(x, y, zStart, EnumFacing.Axis.X), inverseCycle.cycle(x, y, zStart, EnumFacing.Axis.Y), inverseCycle.cycle(x, y, zStart, EnumFacing.Axis.Z),
                                inverseCycle.cycle(x, y, z, EnumFacing.Axis.X), inverseCycle.cycle(x, y, z, EnumFacing.Axis.Y), inverseCycle.cycle(x, y, z, EnumFacing.Axis.Z));
                        zStart = -1;
                    }
                }
            }
        }
    }

    public void forAllBoxes(IntLineConsumer consumer, boolean combine) { BitSetDiscreteVoxelShape.forAllBoxes(this, consumer, combine); }

    public interface IntLineConsumer { void consume(int x1, int y1, int z1, int x2, int y2, int z2); }
}
