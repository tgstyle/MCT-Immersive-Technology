package mctmods.immersivetechnology.common.blocks.metal;

import mctmods.immersivetechnology.common.blocks.helper.ITIEntityBlock;
import mctmods.immersivetechnology.common.blocks.helper.ITProperties;
import mctmods.immersivetechnology.common.blocks.metal.logic.AdvancedCokeOvenBaseHeaterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public class AdvancedCokeOvenBaseHeaterBlock extends ITIEntityBlock<AdvancedCokeOvenBaseHeaterBlockEntity> {

    private static final VoxelShape SINGLE_SHAPE = Shapes.block();
    private static final VoxelShape EMPTY_SHAPE = Shapes.empty();

    private static final VoxelShape SHAPE_X = Shapes.or(
            Shapes.block(),
            Shapes.block().move(-1, 0, 0),
            Shapes.block().move(1, 0, 0)
    );

    private static final VoxelShape SHAPE_Z = Shapes.or(
            Shapes.block(),
            Shapes.block().move(0, 0, -1),
            Shapes.block().move(0, 0, 1)
    );

    public AdvancedCokeOvenBaseHeaterBlock(BiFunction<BlockPos, BlockState, AdvancedCokeOvenBaseHeaterBlockEntity> makeEntity, Properties blockProps) {
        super(makeEntity, blockProps);
        registerDefaultState(defaultBlockState()
                .setValue(ITProperties.FACING_HORIZONTAL, Direction.NORTH)
                .setValue(ITProperties.MULTIBLOCKSLAVE, false)
                .setValue(ITProperties.ACTIVE, false)
                .setValue(BlockStateProperties.WATERLOGGED, false));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ITProperties.FACING_HORIZONTAL, ITProperties.MULTIBLOCKSLAVE, ITProperties.ACTIVE, BlockStateProperties.WATERLOGGED);
    }

    @Override @Nullable public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();

        BlockPos pos = context.getClickedPos();
        Direction side1 = facing.getClockWise();
        Direction side2 = facing.getCounterClockWise();

        if (!context.getLevel().getBlockState(pos.relative(side1)).canBeReplaced(context) || !context.getLevel().getBlockState(pos.relative(side2)).canBeReplaced(context)) { return null; }

        return defaultBlockState()
                .setValue(ITProperties.FACING_HORIZONTAL, facing)
                .setValue(ITProperties.MULTIBLOCKSLAVE, false)
                .setValue(ITProperties.ACTIVE, false)
                .setValue(BlockStateProperties.WATERLOGGED, context.getLevel().getFluidState(pos).getType() == net.minecraft.world.level.material.Fluids.WATER);
    }

    @Override public void onIEBlockPlacedBy(BlockPlaceContext context, BlockState state) {
        Level level = context.getLevel();
        if (level.isClientSide || state.getValue(ITProperties.MULTIBLOCKSLAVE)) {
            return;
        }

        BlockPos pos = context.getClickedPos();
        Direction facing = state.getValue(ITProperties.FACING_HORIZONTAL);

        BlockState finalMaster = defaultBlockState()
                .setValue(ITProperties.FACING_HORIZONTAL, facing)
                .setValue(ITProperties.MULTIBLOCKSLAVE, false)
                .setValue(ITProperties.ACTIVE, false)
                .setValue(BlockStateProperties.WATERLOGGED, state.getValue(BlockStateProperties.WATERLOGGED));

        level.setBlock(pos, finalMaster, 3);

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof AdvancedCokeOvenBaseHeaterBlockEntity mte) {
            mte.active = false;
            mte.requestModelDataUpdate();
            mte.markContainingBlockForUpdate(null);
        }

        BlockState dummyBase = defaultBlockState()
                .setValue(ITProperties.FACING_HORIZONTAL, facing)
                .setValue(ITProperties.MULTIBLOCKSLAVE, true)
                .setValue(ITProperties.ACTIVE, false)
                .setValue(BlockStateProperties.WATERLOGGED, state.getValue(BlockStateProperties.WATERLOGGED));

        for (Direction d : new Direction[]{facing.getClockWise(), facing.getCounterClockWise()}) {
            BlockPos dPos = pos.relative(d);
            level.setBlock(dPos, dummyBase, 3);

            BlockEntity dbe = level.getBlockEntity(dPos);
            if (dbe instanceof AdvancedCokeOvenBaseHeaterBlockEntity dte) {
                dte.dummy = true;
                dte.masterPos = pos;
                dte.active = false;
                dte.requestModelDataUpdate();
                dte.markContainingBlockForUpdate(null);
            }
        }
    }

    @Override @NotNull public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        if (state.getValue(ITProperties.MULTIBLOCKSLAVE)) return EMPTY_SHAPE;
        Direction facing = state.getValue(ITProperties.FACING_HORIZONTAL);
        return (facing.getAxis() == Direction.Axis.X) ? SHAPE_Z : SHAPE_X;
    }

    @Override @NotNull public VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SINGLE_SHAPE;
    }
}
