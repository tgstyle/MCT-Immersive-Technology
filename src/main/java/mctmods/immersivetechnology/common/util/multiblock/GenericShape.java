package mctmods.immersivetechnology.common.util.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.function.Function;

public abstract class GenericShape implements Function<BlockPos, VoxelShape> {
    private static VoxelShape toVoxelShape(AABB aabb) {
        if (aabb == null) return Shapes.empty();
        return Shapes.create(aabb);
    }

    @Override
    public VoxelShape apply(BlockPos posInMultiblock) {
        List<AABB> list = getShape(posInMultiblock);
        if (list.isEmpty()) {
            return Shapes.empty();
        }
        VoxelShape base = toVoxelShape(list.get(0));
        if (list.size() > 1) {
            return list.subList(1, list.size()).stream()
                    .map(GenericShape::toVoxelShape)
                    .reduce(base, Shapes::or);
        }
        return base;
    }

    protected abstract List<AABB> getShape(BlockPos posInMultiblock);
}
