package mctmods.immersivetechnology.common.blocks.metal.helper;

import mctmods.immersivetechnology.common.blocks.helper.ITBlockInterfaces;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public interface ITTrashCanBounds extends ITBlockInterfaces.IBlockBounds {
    @Override
    default @NotNull VoxelShape getBlockBounds(@Nullable CollisionContext ctx) { return Shapes.box(0.125, 0, 0.125, 0.875, 1, 0.875); }
}
