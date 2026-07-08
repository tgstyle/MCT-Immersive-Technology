package mctmods.immersivetechnology.common.blocks.helper;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import com.google.common.collect.ImmutableList;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import mctmods.immersivetechnology.core.registration.ITTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ITIEntityBlock<T extends BlockEntity> extends ITIBaseBlock implements EntityBlock {
    private final BiFunction<BlockPos, BlockState, T> makeEntity;
    private BEClassInspectedData classData;

    public ITIEntityBlock(BiFunction<BlockPos, BlockState, T> makeEntity, Properties blockProps) { this(makeEntity, blockProps, true); }

    public ITIEntityBlock(BiFunction<BlockPos, BlockState, T> makeEntity, Properties blockProps, boolean fitsIntoContainer) {
        super(blockProps, fitsIntoContainer);
        this.makeEntity = makeEntity;
    }

    @Override @Nullable public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) { return makeEntity.apply(pPos, pState); }

    @Override @Nullable public <U extends BlockEntity> BlockEntityTicker<U> getTicker(Level world, @NotNull BlockState state, @NotNull BlockEntityType<U> type) { return getClassData().makeBaseTicker(world.isClientSide); }

    private static final List<BooleanProperty> DEFAULT_OFF = ImmutableList.of(ITProperties.MULTIBLOCKSLAVE, ITProperties.ACTIVE, ITProperties.MIRRORED);

    @Override protected BlockState getInitDefaultState() {
        BlockState ret = super.getInitDefaultState();
        if (ret.hasProperty(ITProperties.FACING_ALL)) { ret = ret.setValue(ITProperties.FACING_ALL, getDefaultFacing()); }
        else if (ret.hasProperty(ITProperties.FACING_HORIZONTAL)) { ret = ret.setValue(ITProperties.FACING_HORIZONTAL, getDefaultFacing()); }
        for (BooleanProperty defaultOff : DEFAULT_OFF) { if (ret.hasProperty(defaultOff)) { ret = ret.setValue(defaultOff, false); } }
        return ret;
    }

    @Override public void onRemove(BlockState state, Level world, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (state.getBlock() != newState.getBlock()) {
            if (state.getBlock() != newState.getBlock()) {
                if (tile instanceof ITBaseBlockEntity) { ((ITBaseBlockEntity) tile).setOverrideState(state); }
                if (tile instanceof ITIBlockInterfaces.IHasDummyBlocks) { ((ITIBlockInterfaces.IHasDummyBlocks) tile).breakDummies(pos, state); }
            }
        }
        super.onRemove(state, world, pos, newState, isMoving);
    }

    @Override public void playerDestroy(@NotNull Level world, @NotNull Player player, @NotNull BlockPos pos, @NotNull BlockState state, BlockEntity tile, @NotNull ItemStack stack) {
        if (tile instanceof ITIBlockInterfaces.IAdditionalDrops) {
            Collection<ItemStack> stacks = ((ITIBlockInterfaces.IAdditionalDrops) tile).getExtraDrops(player, state);
            if (!stacks.isEmpty()) { for (ItemStack s : stacks) { if (!s.isEmpty()) { popResource(world, pos, s); } } }
        }
        super.playerDestroy(world, player, pos, state, tile, stack);
    }

    @Override public boolean canEntityDestroy(@NotNull BlockState state, BlockGetter world, @NotNull BlockPos pos, @NotNull Entity entity) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ITIBlockInterfaces.IEntityProof) { return ((ITIBlockInterfaces.IEntityProof) tile).canEntityDestroy(entity); }
        return super.canEntityDestroy(state, world, pos, entity);
    }

    @Override public boolean triggerEvent(@NotNull BlockState state, Level worldIn, @NotNull BlockPos pos, int eventID, int eventParam) {
        super.triggerEvent(state, worldIn, pos, eventID, eventParam);
        BlockEntity blockEntity = worldIn.getBlockEntity(pos);
        return blockEntity != null && blockEntity.triggerEvent(eventID, eventParam);
    }

    protected Direction getDefaultFacing() { return Direction.NORTH; }

    @Override public void onIEBlockPlacedBy(BlockPlaceContext context, BlockState state) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockEntity tile = world.getBlockEntity(pos);
        Player placer = context.getPlayer();
        Direction side = context.getClickedFace();
        float hitX = (float) context.getClickLocation().x - pos.getX();
        float hitY = (float) context.getClickLocation().y - pos.getY();
        float hitZ = (float) context.getClickLocation().z - pos.getZ();
        if (tile instanceof ITIBlockInterfaces.IDirectionalBE directionalBE) {
            Direction f = directionalBE.getFacingForPlacement(context);
            directionalBE.setFacing(f);
            if (tile instanceof ITIBlockInterfaces.IAdvancedDirectionalBE advDirectional) { advDirectional.onDirectionalPlacement(side, hitX, hitY, hitZ, placer); }
        }
        if (tile instanceof ITIBlockInterfaces.IHasDummyBlocks hasDummyBlocks) { hasDummyBlocks.placeDummies(context, state); }
        if (tile instanceof ITIBlockInterfaces.IPlacementInteraction placementInteractionBE) { placementInteractionBE.onBEPlaced(context); }
    }

    @Override public InteractionResult hammerUseSide(Direction side, Player player, InteractionHand hand, Level w, BlockPos pos, BlockHitResult hit) {
        BlockEntity tile = w.getBlockEntity(pos);
        if (tile instanceof ITIBlockInterfaces.IHammerInteraction) {
            boolean b = ((ITIBlockInterfaces.IHammerInteraction) tile).hammerUseSide(side, player, hand, hit.getLocation());
            if (b) { return InteractionResult.SUCCESS; }
            else { return InteractionResult.FAIL; }
        }
        return super.hammerUseSide(side, player, hand, w, pos, hit);
    }

    @Override public InteractionResult screwdriverUseSide(Direction side, Player player, InteractionHand hand, Level w, BlockPos pos, BlockHitResult hit) {
        BlockEntity tile = w.getBlockEntity(pos);
        if (tile instanceof ITIBlockInterfaces.IScrewdriverInteraction) {
            InteractionResult teResult = ((ITIBlockInterfaces.IScrewdriverInteraction) tile).screwdriverUseSide(side, player, hand, hit.getLocation());
            if (teResult != InteractionResult.PASS) { return teResult; }
        }
        return super.screwdriverUseSide(side, player, hand, w, pos, hit);
    }

    @Override @NotNull public InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
        InteractionResult superResult = super.useWithoutItem(state, world, pos, player, hit);
        if (superResult.consumesAction()) { return superResult; }
        Direction side = hit.getDirection();
        float hitX = (float) hit.getLocation().x - pos.getX();
        float hitY = (float) hit.getLocation().y - pos.getY();
        float hitZ = (float) hit.getLocation().z - pos.getZ();
        ItemStack heldItem = player.getMainHandItem();
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ITIBlockInterfaces.IDirectionalBE && heldItem.is(ITTags.formationTools) && ((ITIBlockInterfaces.IDirectionalBE) tile).canHammerRotate(side, hit.getLocation().subtract(Vec3.atLowerCornerOf(pos)), player) && !world.isClientSide) {
            Direction f = ((ITIBlockInterfaces.IDirectionalBE) tile).getFacing();
            ITPlacementLimitation limit = ((ITIBlockInterfaces.IDirectionalBE) tile).getFacingLimitation();
            f = switch (limit) {
                case SIDE_CLICKED -> Direction.values()[Math.floorMod(f.ordinal() + (player.isShiftKeyDown() ? -1 : 1), 6)];
                case PISTON_LIKE -> {
                    Direction.Axis axis = side.getAxis();
                    Direction rotated = rotateAround(f, axis);
                    yield player.isShiftKeyDown() != (side.getAxisDirection() == Direction.AxisDirection.NEGATIVE) ? rotated.getOpposite() : rotated;
                }
                case HORIZONTAL, HORIZONTAL_PREFER_SIDE, HORIZONTAL_QUADRANT, HORIZONTAL_AXIS -> player.isShiftKeyDown() != side.equals(Direction.DOWN) ? f.getCounterClockWise() : f.getClockWise();
                default -> f;
            };
            ((ITIBlockInterfaces.IDirectionalBE) tile).setFacing(f);
            ((ITIBlockInterfaces.IDirectionalBE) tile).afterRotation();
            tile.setChanged();
            world.sendBlockUpdated(pos, state, state, 3);
            world.blockEvent(tile.getBlockPos(), tile.getBlockState().getBlock(), 255, 0);
            return InteractionResult.SUCCESS;
        }
        if (tile instanceof ITIBlockInterfaces.IConfigurableSides && heldItem.is(ITTags.formationTools) && !world.isClientSide) {
            Direction configSide = player.isShiftKeyDown() ? side.getOpposite() : side;
            if (((ITIBlockInterfaces.IConfigurableSides) tile).toggleSide(configSide, player)) { return InteractionResult.SUCCESS; }
        }
        if (tile instanceof ITIBlockInterfaces.IPlayerInteraction) {
            boolean b = ((ITIBlockInterfaces.IPlayerInteraction) tile).interact(side, player, InteractionHand.MAIN_HAND, heldItem, hitX, hitY, hitZ);
            if (b) { return InteractionResult.SUCCESS; }
        }
        if (tile instanceof MenuProvider menuProvider && !player.isShiftKeyDown()) {
            if (player instanceof ServerPlayer serverPlayer) {
                if (menuProvider instanceof ITIBlockInterfaces.IInteractionObjectIT<?> interaction) {
                    interaction = interaction.getGuiMaster();
                    if (interaction != null && interaction.canUseGui(player)) { serverPlayer.openMenu(interaction); }
                }
                else { serverPlayer.openMenu(menuProvider); }
            }
            return InteractionResult.SUCCESS;
        }
        return superResult;
    }

    private static Direction rotateAround(Direction dir, Direction.Axis axis) {
        if (dir.getAxis() == axis) { return dir; }
        return dir.getClockWise(axis);
    }

    @Override public void neighborChanged(@NotNull BlockState state, Level world, @NotNull BlockPos pos, @NotNull Block block, @NotNull BlockPos fromPos, boolean isMoving) {
        if (!world.isClientSide) {
            BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof ITBaseBlockEntity) { ((ITBaseBlockEntity) tile).onNeighborBlockChange(fromPos); }
        }
    }

    @Override @NotNull public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof ITIBlockInterfaces.ISelectionBounds) { return ((ITIBlockInterfaces.ISelectionBounds) te).getSelectionShape(context); }
        return super.getShape(state, world, pos, context);
    }

    @Override @NotNull public VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof ITIBlockInterfaces.ICollisionBounds collisionBounds) { return collisionBounds.getCollisionShape(context); }
        return super.getCollisionShape(state, world, pos, context);
    }

    @Override @NotNull public VoxelShape getInteractionShape(@NotNull BlockState state, BlockGetter world, @NotNull BlockPos pos) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof ITIBlockInterfaces.ISelectionBounds) { return ((ITIBlockInterfaces.ISelectionBounds) te).getSelectionShape(null); }
        return super.getInteractionShape(state, world, pos);
    }

    @Override public boolean hasAnalogOutputSignal(@NotNull BlockState state) { return getClassData().hasComparatorOutput; }

    @Override public int getAnalogOutputSignal(@NotNull BlockState state, Level world, @NotNull BlockPos pos) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof ITIBlockInterfaces.IComparatorOverride compOverride) { return compOverride.getComparatorInputOverride(); }
        return 0;
    }

    @Override public int getSignal(@NotNull BlockState blockState, BlockGetter world, @NotNull BlockPos pos, @NotNull Direction side) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof ITIBlockInterfaces.IRedstoneOutput rsOutput) { return rsOutput.getWeakRSOutput(side); }
        return 0;
    }

    @Override public int getDirectSignal(@NotNull BlockState blockState, BlockGetter world, @NotNull BlockPos pos, @NotNull Direction side) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof ITIBlockInterfaces.IRedstoneOutput rsOutput) { return rsOutput.getStrongRSOutput(side); }
        return 0;
    }

    @Override public boolean isSignalSource(@NotNull BlockState state) { return getClassData().emitsRedstone(); }

    @Override public boolean canConnectRedstone(@NotNull BlockState state, BlockGetter world, @NotNull BlockPos pos, Direction side) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof ITIBlockInterfaces.IRedstoneOutput rsOutput) { return rsOutput.canConnectRedstone(side); }
        return false;
    }

    @Override public void entityInside(@NotNull BlockState state, Level world, @NotNull BlockPos pos, @NotNull Entity entity) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof ITBaseBlockEntity) { ((ITBaseBlockEntity) te).onEntityCollision(world, entity); }
    }

    @Override
    @SuppressWarnings("unused")
    public Component[] getOverlayText(BlockState state, Level level, BlockPos pos, Player player, HitResult rayTrace, boolean hammer) {
        BlockEntity te = level.getBlockEntity(pos);
        if (te instanceof ITIBlockInterfaces.IBlockOverlayText overlay) { return overlay.getOverlayText(player, rayTrace, hammer); }
        return null;
    }

    private BEClassInspectedData getClassData() {
        if (this.classData == null) {
            T tempBE = makeEntity.apply(BlockPos.ZERO, getInitDefaultState());
            this.classData = new BEClassInspectedData(tempBE instanceof ITIServerTickableBE, tempBE instanceof ITIClientTickableBE, tempBE instanceof ITIBlockInterfaces.IComparatorOverride, tempBE instanceof ITIBlockInterfaces.IRedstoneOutput);
        }
        return this.classData;
    }

    private record BEClassInspectedData(boolean serverTicking, boolean clientTicking, boolean hasComparatorOutput, boolean emitsRedstone) {
        @Nullable public <U extends BlockEntity> BlockEntityTicker<U> makeBaseTicker(boolean isClient) {
            if (serverTicking && !isClient) { return ITIServerTickableBE.makeTicker(); }
            else if (clientTicking && isClient) { return ITIClientTickableBE.makeTicker(); }
            else { return null; }
        }
    }
}
