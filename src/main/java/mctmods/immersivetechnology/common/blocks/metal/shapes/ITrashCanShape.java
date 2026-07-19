package mctmods.immersivetechnology.common.blocks.metal.shapes;

import mctmods.immersivetechnology.common.blocks.helper.ITIBlockInterfaces;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public interface ITrashCanShape extends ITIBlockInterfaces.IBlockBounds {
    @Override @NotNull default VoxelShape getBlockBounds(@Nullable CollisionContext ctx) { return Shapes.box(0.125, 0, 0.125, 0.875, 1, 0.875); }
}
