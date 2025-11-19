package mctmods.immersivetechnology.common.blocks.multiblocks.shapes.helper;

import mctmods.immersivetechnology.common.util.shapes.Shapes;
import mctmods.immersivetechnology.common.util.shapes.VoxelShape;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.function.Function;

public abstract class GenericShape implements Function<BlockPos, VoxelShape> {
    private static VoxelShape toVoxelShape(AxisAlignedBB aabb) {
        if (aabb == null) return Shapes.empty();
        return Shapes.create(aabb);
    }

    @Override
    public VoxelShape apply(BlockPos posInMultiblock) {
        List<AxisAlignedBB> list = getShape(posInMultiblock);
        if (list.isEmpty()) { return Shapes.empty(); }
        VoxelShape base = toVoxelShape(list.get(0));
        if (list.size() > 1) { return list.subList(1, list.size()).stream().map(GenericShape::toVoxelShape).reduce(base, Shapes::or); }
        return base;
    }

    protected abstract List<AxisAlignedBB> getShape(BlockPos posInMultiblock);
}
