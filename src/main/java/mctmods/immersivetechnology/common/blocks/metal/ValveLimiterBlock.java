package mctmods.immersivetechnology.common.blocks.metal;

import mctmods.immersivetechnology.common.blocks.helper.ModEntityBlock;
import mctmods.immersivetechnology.common.blocks.helper.ModProperties;
import mctmods.immersivetechnology.common.blocks.metal.logic.ValveCommonBlockEntity;
import mctmods.immersivetechnology.common.blocks.metal.logic.ValveLimiterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public class ValveLimiterBlock extends ModEntityBlock<ValveLimiterBlockEntity> {
    public static final IntegerProperty ROTATION = IntegerProperty.create("rotation", 0, 3);

    public ValveLimiterBlock(BiFunction<BlockPos, BlockState, ValveLimiterBlockEntity> makeEntity, Properties p) {
        super(makeEntity, p);
        registerDefaultState(stateDefinition.any()
                .setValue(ValveCommonBlockEntity.OPEN, true)
                .setValue(ModProperties.FACING_ALL, Direction.NORTH)
                .setValue(ModProperties.MIRRORED, false)
                .setValue(ROTATION, 0));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ModProperties.FACING_ALL, ModProperties.MIRRORED, ValveCommonBlockEntity.OPEN, ROTATION);
    }

    @Override public boolean canConnectRedstone(@NotNull BlockState state, BlockGetter world, @NotNull BlockPos pos, Direction side) { return true; }

    @Override public void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block fromBlock, @NotNull BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, fromBlock, fromPos, isMoving);
        if (level.isClientSide) { return; }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ValveCommonBlockEntity valve) { valve.updateRedstoneState(); }
    }

    @Override @NotNull public InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
        if (level.isClientSide) { return InteractionResult.SUCCESS; }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ValveCommonBlockEntity valve) {
            if (player.isCrouching()) { valve.redstoneMode = (byte) (valve.redstoneMode == 1 ? 2 : 1); valve.updateRedstoneState(); valve.efficientSetChanged(); }else {
                player.openMenu(valve, b -> b.writeBlockPos(pos));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getClickedFace();
        int rotation = 0;
        if (facing.getAxis().isVertical()) {
            assert context.getPlayer() != null;
            float yRot = context.getPlayer().getYRot();
            rotation = Direction.fromYRot(yRot).get2DDataValue();
        }
        return defaultBlockState()
                .setValue(ModProperties.FACING_ALL, facing)
                .setValue(ModProperties.MIRRORED, false)
                .setValue(ValveCommonBlockEntity.OPEN, true)
                .setValue(ROTATION, rotation);
    }
}
