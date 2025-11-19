package mctmods.immersivetechnology.common.util.shapes;

import mctmods.immersivetechnology.common.util.ITMth;
import net.minecraft.util.EnumFacing;
import java.util.BitSet;

public final class BitSetDiscreteVoxelShape extends DiscreteVoxelShape {
    private final BitSet storage;
    private int xMin, yMin, zMin, xMax, yMax, zMax;

    public BitSetDiscreteVoxelShape(int xSize, int ySize, int zSize) {
        super(xSize, ySize, zSize);
        this.storage = new BitSet(xSize * ySize * zSize);
        this.xMin = xSize;
        this.yMin = ySize;
        this.zMin = zSize;
    }

    public static BitSetDiscreteVoxelShape withFilledBounds(int x, int y, int z, int xMin, int yMin, int zMin, int xMax, int yMax, int zMax) {
        BitSetDiscreteVoxelShape shape = new BitSetDiscreteVoxelShape(x, y, z);
        shape.xMin = xMin;
        shape.yMin = yMin;
        shape.zMin = zMin;
        shape.xMax = xMax;
        shape.yMax = yMax;
        shape.zMax = zMax;
        for (int i = xMin; i < xMax; i++) for (int j = yMin; j < yMax; j++) for (int k = zMin; k < zMax; k++) { shape.fillUpdateBounds(i, j, k, false); }
        return shape;
    }

    public BitSetDiscreteVoxelShape(DiscreteVoxelShape shape) {
        super(shape.xSize, shape.ySize, shape.zSize);
        if (shape instanceof BitSetDiscreteVoxelShape) {
            this.storage = (BitSet)((BitSetDiscreteVoxelShape)shape).storage.clone();
        } else {
            this.storage = new BitSet(this.xSize * this.ySize * this.zSize);
            for (int i = 0; i < this.xSize; i++) for (int j = 0; j < this.ySize; j++) for (int k = 0; k < this.zSize; k++) { if (shape.isFull(i, j, k)) { this.storage.set(this.getIndex(i, j, k)); } }
        }
        this.xMin = shape.firstFull(EnumFacing.Axis.X);
        this.yMin = shape.firstFull(EnumFacing.Axis.Y);
        this.zMin = shape.firstFull(EnumFacing.Axis.Z);
        this.xMax = shape.lastFull(EnumFacing.Axis.X);
        this.yMax = shape.lastFull(EnumFacing.Axis.Y);
        this.zMax = shape.lastFull(EnumFacing.Axis.Z);
    }

    private int getIndex(int x, int y, int z) { return (x * this.ySize + y) * this.zSize + z; }

    public boolean isFull(int x, int y, int z) { return this.storage.get(this.getIndex(x, y, z)); }

    private void fillUpdateBounds(int x, int y, int z, boolean update) {
        this.storage.set(this.getIndex(x, y, z));
        if (update) {
            this.xMin = Math.min(this.xMin, x);
            this.yMin = Math.min(this.yMin, y);
            this.zMin = Math.min(this.zMin, z);
            this.xMax = Math.max(this.xMax, x + 1);
            this.yMax = Math.max(this.yMax, y + 1);
            this.zMax = Math.max(this.zMax, z + 1);
        }
    }

    public void fill(int x, int y, int z) { this.fillUpdateBounds(x, y, z, true); }

    public boolean isEmpty() { return this.storage.isEmpty(); }

    public int firstFull(EnumFacing.Axis axis) { return ITMth.choose(axis, this.xMin, this.yMin, this.zMin); }

    public int lastFull(EnumFacing.Axis axis) { return ITMth.choose(axis, this.xMax, this.yMax, this.zMax); }

    static BitSetDiscreteVoxelShape join(DiscreteVoxelShape mainShape, DiscreteVoxelShape secondaryShape, IndexMerger mergerX, IndexMerger mergerY, IndexMerger mergerZ, BooleanOp operator) {
        BitSetDiscreteVoxelShape shape = new BitSetDiscreteVoxelShape(mergerX.size() - 1, mergerY.size() - 1, mergerZ.size() - 1);
        int[] bounds = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
        mergerX.forMergedIndexes((x1, x2, x3) -> {
            boolean[] xFlag = new boolean[]{false};
            mergerY.forMergedIndexes((y1, y2, y3) -> {
                boolean[] zFlag = new boolean[]{false};
                mergerZ.forMergedIndexes((z1, z2, z3) -> {
                    if (operator.apply(mainShape.isFullWide(x1, y1, z1), secondaryShape.isFullWide(x2, y2, z2))) {
                        shape.storage.set(shape.getIndex(x3, y3, z3));
                        bounds[2] = Math.min(bounds[2], z3);
                        bounds[5] = Math.max(bounds[5], z3);
                        zFlag[0] = true;
                    }
                    return true;
                });
                if (zFlag[0]) {
                    bounds[1] = Math.min(bounds[1], y3);
                    bounds[4] = Math.max(bounds[4], y3);
                    xFlag[0] = true;
                }
                return true;
            });
            if (xFlag[0]) {
                bounds[0] = Math.min(bounds[0], x3);
                bounds[3] = Math.max(bounds[3], x3);
            }
            return true;
        });
        shape.xMin = bounds[0];
        shape.yMin = bounds[1];
        shape.zMin = bounds[2];
        shape.xMax = bounds[3] + 1;
        shape.yMax = bounds[4] + 1;
        shape.zMax = bounds[5] + 1;
        return shape;
    }

    static void forAllBoxes(DiscreteVoxelShape shape, DiscreteVoxelShape.IntLineConsumer consumer, boolean combine) {
        BitSetDiscreteVoxelShape bitSetShape = new BitSetDiscreteVoxelShape(shape);
        for (int y = 0; y < bitSetShape.ySize; y++) for (int x = 0; x < bitSetShape.xSize; x++) {
            int zStart = -1;
            for (int z = 0; z <= bitSetShape.zSize; z++) {
                if (bitSetShape.isFullWide(x, y, z)) {
                    if (combine) {
                        if (zStart == -1) { zStart = z; }
                    } else { consumer.consume(x, y, z, x + 1, y + 1, z + 1); }
                } else if (zStart != -1) {
                    int xEnd = x, yEnd = y;
                    bitSetShape.clearZStrip(zStart, z, x, y);
                    while (bitSetShape.isZStripFull(zStart, z, xEnd + 1, y)) {
                        bitSetShape.clearZStrip(zStart, z, xEnd + 1, y);
                        xEnd++;
                    }
                    while (bitSetShape.isXZRectangleFull(x, xEnd + 1, zStart, z, yEnd + 1)) {
                        for (int x1 = x; x1 <= xEnd; x1++) { bitSetShape.clearZStrip(zStart, z, x1, yEnd + 1); }
                        yEnd++;
                    }
                    consumer.consume(x, y, zStart, xEnd + 1, yEnd + 1, z);
                    zStart = -1;
                }
            }
        }
    }

    private boolean isZStripFull(int zMin, int zMax, int x, int y) { return (x < this.xSize && y < this.ySize) && this.storage.nextClearBit(this.getIndex(x, y, zMin)) >= this.getIndex(x, y, zMax); }

    private boolean isXZRectangleFull(int xMin, int xMax, int zMin, int zMax, int y) {
        for (int x = xMin; x < xMax; x++) { if (!this.isZStripFull(zMin, zMax, x, y)) { return false; } }
        return true;
    }

    private void clearZStrip(int zMin, int zMax, int x, int y) { this.storage.clear(this.getIndex(x, y, zMin), this.getIndex(x, y, zMax)); }
}
