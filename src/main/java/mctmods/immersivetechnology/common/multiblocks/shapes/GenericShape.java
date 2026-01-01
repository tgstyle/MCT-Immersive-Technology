package mctmods.immersivetechnology.common.multiblocks.shapes;

import mctmods.immersivetechnology.common.util.shapes.Shapes;
import mctmods.immersivetechnology.common.util.shapes.VoxelShape;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class GenericShape implements Function<BlockPos, VoxelShape> {
    protected final int width, height, length;
    protected final int[] axisOrder;
    protected static List<List<AxisAlignedBB>> SHAPES;

    protected GenericShape(int w, int h, int l, int[] order) {
        this.width = w; this.height = h; this.length = l;
        this.axisOrder = order != null ? order : new int[]{1, 2, 0};
    }

    private static VoxelShape toVoxelShape(AxisAlignedBB aabb) {
        if (aabb == null) return Shapes.empty();
        return Shapes.create(aabb);
    }

    @Override public VoxelShape apply(BlockPos posInMultiblock) {
        List<AxisAlignedBB> list = getShape(posInMultiblock);
        if (list.isEmpty()) { return Shapes.empty(); }
        VoxelShape base = toVoxelShape(list.get(0));
        if (list.size() > 1) { return list.subList(1, list.size()).stream().map(GenericShape::toVoxelShape).reduce(base, Shapes::or); }
        return base;
    }

    protected List<AxisAlignedBB> getShape(BlockPos posInMultiblock) {
        int x = posInMultiblock.getX(), y = posInMultiblock.getY(), z = posInMultiblock.getZ();
        if (x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= length) { return new ArrayList<>(); }
        int index = computeIndex(x, y, z);
        return (index < SHAPES.size()) ? SHAPES.get(index) : new ArrayList<>();
    }

    protected int computeIndex(int x, int y, int z) {
        int[] coords = {x, y, z};
        int major = axisOrder[0], mid = axisOrder[1], minor = axisOrder[2];
        int s_mid = getSize(mid), s_minor = getSize(minor);
        return coords[major] * (s_mid * s_minor) + coords[mid] * s_minor + coords[minor];
    }

    private int getSize(int axis) {
        switch (axis) {
            case 0: return width;
            case 1: return height;
            case 2: return length;
            default: return 1;
        }
    }
}
