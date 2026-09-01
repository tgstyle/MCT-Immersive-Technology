package mctmods.immersivetechnology.common.blocks.metal.shapes;

import com.immersiveconvergence.api.block.BlockInterfaces;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ITrashCanShape extends BlockInterfaces.IBlockBounds {
    @Override @Nonnull default VoxelShape getBlockBounds(@Nullable CollisionContext ctx) { return Shapes.box(0.125, 0, 0.125, 0.875, 1, 0.875); }
}
