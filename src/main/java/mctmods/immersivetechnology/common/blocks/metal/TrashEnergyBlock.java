package mctmods.immersivetechnology.common.blocks.metal;

import mctmods.immersivetechnology.common.blocks.helper.ITEntityBlock;
import mctmods.immersivetechnology.common.blocks.helper.ITProperties;
import mctmods.immersivetechnology.common.blocks.metal.logic.TrashEnergyBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public class TrashEnergyBlock extends ITEntityBlock<TrashEnergyBlockEntity> {
    private static final VoxelShape BOUNDS = makeBounds();
    private static VoxelShape makeBounds() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.or(shape, Shapes.box(2d / 16, 0, 2d / 16, 14d / 16, 13d / 16, 14d / 16));
        shape = Shapes.or(shape, Shapes.box(0.5d / 16, 13d / 16, 0.5d / 16, 15.5d / 16, 1, 15.5d / 16));
        return shape;
    }

    public TrashEnergyBlock(BiFunction<BlockPos, BlockState, TrashEnergyBlockEntity> makeEntity, Properties blockProps) { super(makeEntity, blockProps); }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) { return BOUNDS; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ITProperties.FACING_HORIZONTAL, BlockStateProperties.WATERLOGGED);
    }
}
