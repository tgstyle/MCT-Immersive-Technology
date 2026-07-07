package mctmods.immersivetechnology.common.blocks.helper;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import mctmods.immersivetechnology.core.registration.ITTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;

@SuppressWarnings("deprecation")
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
                if (tile instanceof ITIBaseIBlockEntity) { ((ITIBaseIBlockEntity) tile).setOverrideState(state); }
                if (tile instanceof ITBlockInterfaces.IHasDummyBlocks) { ((ITBlockInterfaces.IHasDummyBlocks) tile).breakDummies(pos, state); }
            }
        }
        super.onRemove(state, world, pos, newState, isMoving);
    }

    @Override public void playerDestroy(@NotNull Level world, @NotNull Player player, @NotNull BlockPos pos, @NotNull BlockState state, BlockEntity tile, @NotNull ItemStack stack) {
        if (tile instanceof ITBlockInterfaces.IAdditionalDrops) {
            Collection<ItemStack> stacks = ((ITBlockInterfaces.IAdditionalDrops) tile).getExtraDrops(player, state);
            if (!stacks.isEmpty()) { for (ItemStack s : stacks) { if (!s.isEmpty()) { popResource(world, pos, s); } } }
        }
        super.playerDestroy(world, player, pos, state, tile, stack);
    }

    @Override public boolean canEntityDestroy(BlockState state, BlockGetter world, BlockPos pos, Entity entity) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ITBlockInterfaces.IEntityProof) { return ((ITBlockInterfaces.IEntityProof) tile).canEntityDestroy(entity); }
        return super.canEntityDestroy(state, world, pos, entity);
    }

    @Override public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ITBlockInterfaces.IBlockEntityDrop && target instanceof BlockHitResult) {
            ItemStack s = ((ITBlockInterfaces.IBlockEntityDrop) tile).getPickBlock(world.getBlockState(pos));
            if (!s.isEmpty()) { return s; }
        }
        Item item = this.asItem();
        return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item, 1);
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
        if (tile instanceof ITBlockInterfaces.IDirectionalBE directionalBE) {
            Direction f = directionalBE.getFacingForPlacement(context);
            directionalBE.setFacing(f);
            if (tile instanceof ITBlockInterfaces.IAdvancedDirectionalBE advDirectional) { advDirectional.onDirectionalPlacement(side, hitX, hitY, hitZ, placer); }
        }
        if (tile instanceof ITBlockInterfaces.IHasDummyBlocks hasDummyBlocks) { hasDummyBlocks.placeDummies(context, state); }
        if (tile instanceof ITBlockInterfaces.IPlacementInteraction placementInteractionBE) { placementInteractionBE.onBEPlaced(context); }
    }

    @Override public InteractionResult hammerUseSide(Direction side, Player player, InteractionHand hand, Level w, BlockPos pos, BlockHitResult hit) {
        BlockEntity tile = w.getBlockEntity(pos);
        if (tile instanceof ITBlockInterfaces.IHammerInteraction) {
            boolean b = ((ITBlockInterfaces.IHammerInteraction) tile).hammerUseSide(side, player, hand, hit.getLocation());
            if (b) { return InteractionResult.SUCCESS; }
            else { return InteractionResult.FAIL; }
        }
        return super.hammerUseSide(side, player, hand, w, pos, hit);
    }

    @Override public InteractionResult screwdriverUseSide(Direction side, Player player, InteractionHand hand, Level w, BlockPos pos, BlockHitResult hit) {
        BlockEntity tile = w.getBlockEntity(pos);
        if (tile instanceof ITBlockInterfaces.IScrewdriverInteraction) {
            InteractionResult teResult = ((ITBlockInterfaces.IScrewdriverInteraction) tile).screwdriverUseSide(side, player, hand, hit.getLocation());
            if (teResult != InteractionResult.PASS) { return teResult; }
        }
        return super.screwdriverUseSide(side, player, hand, w, pos, hit);
    }

    @Override @NotNull public InteractionResult use(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        InteractionResult superResult = super.use(state, world, pos, player, hand, hit);
        if (superResult.consumesAction()) { return superResult; }
        Direction side = hit.getDirection();
        float hitX = (float) hit.getLocation().x - pos.getX();
        float hitY = (float) hit.getLocation().y - pos.getY();
        float hitZ = (float) hit.getLocation().z - pos.getZ();
        ItemStack heldItem = player.getItemInHand(hand);
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ITBlockInterfaces.IDirectionalBE && heldItem.is(ITTags.formationTools) && ((ITBlockInterfaces.IDirectionalBE) tile).canHammerRotate(side, hit.getLocation().subtract(Vec3.atLowerCornerOf(pos)), player) && !world.isClientSide) {
            Direction f = ((ITBlockInterfaces.IDirectionalBE) tile).getFacing();
            ITPlacementLimitation limit = ((ITBlockInterfaces.IDirectionalBE) tile).getFacingLimitation();
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
            ((ITBlockInterfaces.IDirectionalBE) tile).setFacing(f);
            ((ITBlockInterfaces.IDirectionalBE) tile).afterRotation();
            tile.setChanged();
            world.sendBlockUpdated(pos, state, state, 3);
            world.blockEvent(tile.getBlockPos(), tile.getBlockState().getBlock(), 255, 0);
            return InteractionResult.SUCCESS;
        }
        if (tile instanceof ITBlockInterfaces.IConfigurableSides && heldItem.is(ITTags.formationTools) && !world.isClientSide) {
            Direction configSide = player.isShiftKeyDown() ? side.getOpposite() : side;
            if (((ITBlockInterfaces.IConfigurableSides) tile).toggleSide(configSide, player)) { return InteractionResult.SUCCESS; }
        }
        if (tile instanceof ITBlockInterfaces.IPlayerInteraction) {
            boolean b = ((ITBlockInterfaces.IPlayerInteraction) tile).interact(side, player, hand, heldItem, hitX, hitY, hitZ);
            if (b) { return InteractionResult.SUCCESS; }
        }
        if (tile instanceof MenuProvider menuProvider && hand == InteractionHand.MAIN_HAND && !player.isShiftKeyDown()) {
            if (player instanceof ServerPlayer serverPlayer) {
                if (menuProvider instanceof ITBlockInterfaces.IInteractionObjectIT<?> interaction) {
                    interaction = interaction.getGuiMaster();
                    if (interaction != null && interaction.canUseGui(player)) { NetworkHooks.openScreen(serverPlayer, interaction); }
                }
                else { NetworkHooks.openScreen(serverPlayer, menuProvider); }
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
            if (tile instanceof ITIBaseIBlockEntity) { ((ITIBaseIBlockEntity) tile).onNeighborBlockChange(fromPos); }
        }
    }

    @Override @NotNull public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof ITBlockInterfaces.ISelectionBounds) { return ((ITBlockInterfaces.ISelectionBounds) te).getSelectionShape(context); }
        return super.getShape(state, world, pos, context);
    }

    @Override @NotNull public VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof ITBlockInterfaces.ICollisionBounds collisionBounds) { return collisionBounds.getCollisionShape(context); }
        return super.getCollisionShape(state, world, pos, context);
    }

    @Override @NotNull public VoxelShape getInteractionShape(@NotNull BlockState state, BlockGetter world, @NotNull BlockPos pos) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof ITBlockInterfaces.ISelectionBounds) { return ((ITBlockInterfaces.ISelectionBounds) te).getSelectionShape(null); }
        return super.getInteractionShape(state, world, pos);
    }

    @Override public boolean hasAnalogOutputSignal(@NotNull BlockState state) { return getClassData().hasComparatorOutput; }

    @Override public int getAnalogOutputSignal(@NotNull BlockState state, Level world, @NotNull BlockPos pos) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof ITBlockInterfaces.IComparatorOverride compOverride) { return compOverride.getComparatorInputOverride(); }
        return 0;
    }

    @Override public int getSignal(@NotNull BlockState blockState, BlockGetter world, @NotNull BlockPos pos, @NotNull Direction side) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof ITBlockInterfaces.IRedstoneOutput rsOutput) { return rsOutput.getWeakRSOutput(side); }
        return 0;
    }

    @Override public int getDirectSignal(@NotNull BlockState blockState, BlockGetter world, @NotNull BlockPos pos, @NotNull Direction side) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof ITBlockInterfaces.IRedstoneOutput rsOutput) { return rsOutput.getStrongRSOutput(side); }
        return 0;
    }

    @Override public boolean isSignalSource(@NotNull BlockState state) { return getClassData().emitsRedstone(); }

    @Override public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof ITBlockInterfaces.IRedstoneOutput rsOutput) { return rsOutput.canConnectRedstone(side); }
        return false;
    }

    @Override public void entityInside(@NotNull BlockState state, Level world, @NotNull BlockPos pos, @NotNull Entity entity) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof ITIBaseIBlockEntity) { ((ITIBaseIBlockEntity) te).onEntityCollision(world, entity); }
    }

    private BEClassInspectedData getClassData() {
        if (this.classData == null) {
            T tempBE = makeEntity.apply(BlockPos.ZERO, getInitDefaultState());
            this.classData = new BEClassInspectedData(tempBE instanceof ITIServerTickableBE, tempBE instanceof ITIClientTickableBE, tempBE instanceof ITBlockInterfaces.IComparatorOverride, tempBE instanceof ITBlockInterfaces.IRedstoneOutput);
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
