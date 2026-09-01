package mctmods.immersivetechnology.common.blocks.metal;

import com.immersiveconvergence.api.block.ModEntityBlock;
import com.immersiveconvergence.api.block.ModProperties;
import mctmods.immersivetechnology.common.blocks.metal.logic.TrashItemBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import java.util.function.BiFunction;

public class TrashItemBlock extends ModEntityBlock<TrashItemBlockEntity> {
    private static final VoxelShape BOUNDS = makeBounds();
    private static VoxelShape makeBounds() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.or(shape, Shapes.box(2d / 16, 0, 2d / 16, 14d / 16, 13d / 16, 14d / 16));
        shape = Shapes.or(shape, Shapes.box(0.5d / 16, 13d / 16, 0.5d / 16, 15.5d / 16, 1, 15.5d / 16));
        return shape;
    }

    public TrashItemBlock(BiFunction<BlockPos, BlockState, TrashItemBlockEntity> makeEntity, Properties blockProps) { super(makeEntity, blockProps); }

    @Override @Nonnull public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nonnull CollisionContext context) { return BOUNDS; }

    @Override protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ModProperties.FACING_HORIZONTAL, BlockStateProperties.WATERLOGGED);
    }
}
