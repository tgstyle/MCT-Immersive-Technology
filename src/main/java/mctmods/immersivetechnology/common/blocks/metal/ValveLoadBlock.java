package mctmods.immersivetechnology.common.blocks.metal;

import blusunrize.immersiveengineering.common.items.WireCoilItem;
import mctmods.immersivetechnology.common.blocks.helper.ITEntityBlock;
import mctmods.immersivetechnology.common.blocks.helper.ITProperties;
import mctmods.immersivetechnology.common.blocks.metal.logic.ValveCommonBlockEntity;
import mctmods.immersivetechnology.common.blocks.metal.logic.ValveLoadBlockEntity;
import mctmods.immersivetechnology.core.registration.ITTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public class ValveLoadBlock extends ITEntityBlock<ValveLoadBlockEntity> {
    public static final BooleanProperty OPEN = BooleanProperty.create("open");
    public static final IntegerProperty ROTATION = IntegerProperty.create("rotation", 0, 3);

    public ValveLoadBlock(BiFunction<BlockPos, BlockState, ValveLoadBlockEntity> makeEntity, Properties p) { super(makeEntity, p); registerDefaultState(stateDefinition.any().setValue(OPEN, true).setValue(ITProperties.FACING_ALL, Direction.NORTH).setValue(ITProperties.MIRRORED, false).setValue(ROTATION, 0)); }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) { super.createBlockStateDefinition(builder); builder.add(ITProperties.FACING_ALL, ITProperties.MIRRORED, OPEN, ROTATION); }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) { return getValveShape(state); }

    @Override
    public @NotNull VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) { return getValveShape(state); }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull VoxelShape getOcclusionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) { return getValveShape(state); }

    private VoxelShape getValveShape(BlockState state) {
        Direction facing = state.getValue(ITProperties.FACING_ALL);
        int rotation = state.getValue(ROTATION);
        double baseThickness = 0.25;
        double connectorSize = 0.375;
        double connectorLength = 0.75;
        double centerMin = (1 - connectorSize) / 2;
        double centerMax = centerMin + connectorSize;
        VoxelShape baseShape;
        VoxelShape connector1;
        VoxelShape connector2;
        switch (facing) {
            case UP: {
                baseShape = Shapes.box(0, 0, 0, 1, baseThickness, 1);
                boolean alongX = rotation % 2 != 0;
                if (alongX) {
                    connector1 = Shapes.box(0, baseThickness, centerMin, connectorSize, baseThickness + connectorLength, centerMax);
                    connector2 = Shapes.box(1 - connectorSize, baseThickness, centerMin, 1, baseThickness + connectorLength, centerMax);
                } else {
                    connector1 = Shapes.box(centerMin, baseThickness, 0, centerMax, baseThickness + connectorLength, connectorSize);
                    connector2 = Shapes.box(centerMin, baseThickness, 1 - connectorSize, centerMax, baseThickness + connectorLength, 1);
                }
                break;
            }
            case DOWN: {
                baseShape = Shapes.box(0, 1 - baseThickness, 0, 1, 1, 1);
                boolean alongX = rotation % 2 != 0;
                if (alongX) {
                    connector1 = Shapes.box(0, 1 - baseThickness - connectorLength, centerMin, connectorSize, 1 - baseThickness, centerMax);
                    connector2 = Shapes.box(1 - connectorSize, 1 - baseThickness - connectorLength, centerMin, 1, 1 - baseThickness, centerMax);
                } else {
                    connector1 = Shapes.box(centerMin, 1 - baseThickness - connectorLength, 0, centerMax, 1 - baseThickness, connectorSize);
                    connector2 = Shapes.box(centerMin, 1 - baseThickness - connectorLength, 1 - connectorSize, centerMax, 1 - baseThickness, 1);
                }
                break;
            }
            case NORTH: {
                baseShape = Shapes.box(0, 0, 1 - baseThickness, 1, 1, 1);
                connector1 = Shapes.box(0, centerMin, 1 - baseThickness - connectorLength, connectorSize, centerMax, 1 - baseThickness);
                connector2 = Shapes.box(1 - connectorSize, centerMin, 1 - baseThickness - connectorLength, 1, centerMax, 1 - baseThickness);
                break;
            }
            case SOUTH: {
                baseShape = Shapes.box(0, 0, 0, 1, 1, baseThickness);
                connector1 = Shapes.box(0, centerMin, baseThickness, connectorSize, centerMax, baseThickness + connectorLength);
                connector2 = Shapes.box(1 - connectorSize, centerMin, baseThickness, 1, centerMax, baseThickness + connectorLength);
                break;
            }
            case EAST: {
                baseShape = Shapes.box(0, 0, 0, baseThickness, 1, 1);
                connector1 = Shapes.box(baseThickness, centerMin, 0, baseThickness + connectorLength, centerMax, connectorSize);
                connector2 = Shapes.box(baseThickness, centerMin, 1 - connectorSize, baseThickness + connectorLength, centerMax, 1);
                break;
            }
            case WEST: {
                baseShape = Shapes.box(1 - baseThickness, 0, 0, 1, 1, 1);
                connector1 = Shapes.box(1 - baseThickness - connectorLength, centerMin, 0, 1 - baseThickness, centerMax, connectorSize);
                connector2 = Shapes.box(1 - baseThickness - connectorLength, centerMin, 1 - connectorSize, 1 - baseThickness, centerMax, 1);
                break;
            }
            default: throw new IllegalArgumentException("Unknown facing: " + facing);
        }
        return Shapes.or(baseShape, connector1, connector2);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction side) { return true; }

    @Override
    public void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block fromBlock, @NotNull BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, fromBlock, fromPos, isMoving);
        if (level.isClientSide) return;
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ValveCommonBlockEntity valve) { valve.updateRedstoneState(); }
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.is(ITTags.formationTools)) return super.use(state, level, pos, player, hand, hit);
        if (level.isClientSide) return InteractionResult.SUCCESS;
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ValveCommonBlockEntity valve) {
            if (player.isCrouching()) {
                valve.redstoneMode = (byte) (valve.redstoneMode == 1 ? 2 : 1);
                valve.updateRedstoneState();
                valve.efficientSetChanged();
            } else {
                if (heldItem.getItem() instanceof WireCoilItem) return InteractionResult.PASS;
                NetworkHooks.openScreen((ServerPlayer) player, valve, buf -> buf.writeBlockPos(pos));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getClickedFace();
        assert context.getPlayer() != null;
        float yRot = context.getPlayer().getYRot();
        int rotation = Direction.fromYRot(yRot).get2DDataValue();
        return defaultBlockState().setValue(ITProperties.FACING_ALL, facing).setValue(ITProperties.MIRRORED, false).setValue(OPEN, true).setValue(ROTATION, rotation);
    }
}
