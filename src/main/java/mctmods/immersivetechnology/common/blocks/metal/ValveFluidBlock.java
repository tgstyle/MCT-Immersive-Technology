package mctmods.immersivetechnology.common.blocks.metal;

import com.immersiveconvergence.api.block.ModEntityBlock;
import com.immersiveconvergence.api.block.ModProperties;
import mctmods.immersivetechnology.common.blocks.metal.logic.ValveCommonBlockEntity;
import mctmods.immersivetechnology.common.blocks.metal.logic.ValveFluidBlockEntity;
import mctmods.immersivetechnology.core.registration.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;

import javax.annotation.Nonnull;
import java.util.function.BiFunction;

public class ValveFluidBlock extends ModEntityBlock<ValveFluidBlockEntity> {
    public static final IntegerProperty ROTATION = IntegerProperty.create("rotation", 0, 3);

    public ValveFluidBlock(BiFunction<BlockPos, BlockState, ValveFluidBlockEntity> makeEntity, Properties p) {
        super(makeEntity, p);
        registerDefaultState(stateDefinition.any()
                .setValue(ValveCommonBlockEntity.OPEN, true)
                .setValue(ModProperties.FACING_ALL, Direction.NORTH)
                .setValue(ModProperties.MIRRORED, false)
                .setValue(ROTATION, 0));
    }

    @Override protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ModProperties.FACING_ALL, ModProperties.MIRRORED, ValveCommonBlockEntity.OPEN, ROTATION);
    }

    @Override @Nonnull public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nonnull CollisionContext context) { return getValveShape(state); }

    private VoxelShape getValveShape(BlockState state) {
        Direction facing = state.getValue(ModProperties.FACING_ALL);
        Direction.Axis axis = facing.getAxis();
        double minX = axis == Direction.Axis.X ? 0 : 2 / 16D;
        double maxX = axis == Direction.Axis.X ? 1 : 14 / 16D;
        double minY = axis == Direction.Axis.Y ? 0 : 2 / 16D;
        double maxY = axis == Direction.Axis.Y ? 1 : 14 / 16D;
        double minZ = axis == Direction.Axis.Z ? 0 : 2 / 16D;
        double maxZ = axis == Direction.Axis.Z ? 1 : 14 / 16D;
        return Shapes.box(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override public boolean canConnectRedstone(@Nonnull BlockState state, BlockGetter world, @Nonnull BlockPos pos, Direction side) { return true; }

    @Override public void neighborChanged(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Block fromBlock, @Nonnull BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, fromBlock, fromPos, isMoving);
        if (level.isClientSide) { return; }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ValveCommonBlockEntity valve) { valve.updateRedstoneState(); }
    }

    @Override @Nonnull public InteractionResult useWithoutItem(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull BlockHitResult hit) {
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.is(ModTags.formationTools)) { return InteractionResult.PASS; }
        if (level.isClientSide) { return InteractionResult.SUCCESS; }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ValveCommonBlockEntity valve) {
            if (player.isCrouching()) { valve.redstoneMode = (byte) (valve.redstoneMode == 1 ? 2 : 1); valve.updateRedstoneState(); valve.efficientSetChanged(); }else {
                player.openMenu(valve, buf -> buf.writeBlockPos(pos));
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
