package mctmods.immersivetechnology.common.blocks.helper;

import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.common.blocks.MultiblockBEType;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;

@SuppressWarnings({"unused","deprecation"})
public class ITEntityBlock<T extends BlockEntity> extends ITBaseBlock implements ITBlockInterfaces.IColouredBlock, EntityBlock {
    private boolean hasColours = false;
    private final BiFunction<BlockPos, BlockState, T> makeEntity;
    private BEClassInspectedData classData;

    public ITEntityBlock(BiFunction<BlockPos, BlockState, T> makeEntity, Properties blockProps) { this(makeEntity, blockProps, true); }

    public ITEntityBlock(BiFunction<BlockPos, BlockState, T> makeEntity, Properties blockProps, boolean fitsIntoContainer) {
        super(blockProps, fitsIntoContainer);
        this.makeEntity = makeEntity;
    }

    public ITEntityBlock(RegistryObject<BlockEntityType<T>> tileType, Properties blockProps) { this(tileType, blockProps, true); }

    public ITEntityBlock(RegistryObject<BlockEntityType<T>> tileType, Properties blockProps, boolean fitsIntoContainer) { this((bp, state) -> tileType.get().create(bp, state), blockProps, fitsIntoContainer); }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) { return makeEntity.apply(pPos, pState); }

    @Nullable
    @Override
    public <U extends BlockEntity> BlockEntityTicker<U> getTicker(Level world, @NotNull BlockState state, @NotNull BlockEntityType<U> type) {
        BlockEntityTicker<U> baseTicker = getClassData().makeBaseTicker(world.isClientSide);
        if (makeEntity instanceof MultiblockBEType<?> multiBEType && type != multiBEType.master()) { return null; }
        return baseTicker;
    }

    private static final List<BooleanProperty> DEFAULT_OFF = ImmutableList.of(ITProperties.MULTIBLOCKSLAVE, ITProperties.ACTIVE, ITProperties.MIRRORED);

    @Override
    protected BlockState getInitDefaultState() {
        BlockState ret = super.getInitDefaultState();
        if (ret.hasProperty(ITProperties.FACING_ALL)) { ret = ret.setValue(ITProperties.FACING_ALL, getDefaultFacing()); }
        else if (ret.hasProperty(ITProperties.FACING_HORIZONTAL)) { ret = ret.setValue(ITProperties.FACING_HORIZONTAL, getDefaultFacing()); }
        for (BooleanProperty defaultOff : DEFAULT_OFF) { if (ret.hasProperty(defaultOff)) { ret = ret.setValue(defaultOff, false); } }
        return ret;
    }

    @Override
    public void onRemove(BlockState state, Level world, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (state.getBlock() != newState.getBlock()) {
            if (tile instanceof ITBaseBlockEntity) { ((ITBaseBlockEntity) tile).setOverrideState(state); }
            if (tile instanceof ITBlockInterfaces.IHasDummyBlocks) { ((ITBlockInterfaces.IHasDummyBlocks) tile).breakDummies(pos, state); }
        }
        super.onRemove(state, world, pos, newState, isMoving);
    }

    @Override
    public void playerDestroy(@NotNull Level world, @NotNull Player player, @NotNull BlockPos pos, @NotNull BlockState state, BlockEntity tile, @NotNull ItemStack stack) {
        if (tile instanceof ITBlockInterfaces.IAdditionalDrops) {
            Collection<ItemStack> stacks = ((ITBlockInterfaces.IAdditionalDrops) tile).getExtraDrops(player, state);
            if (!stacks.isEmpty()) { for (ItemStack s : stacks) { if (!s.isEmpty()) { popResource(world, pos, s); } } }
        }
        super.playerDestroy(world, player, pos, state, tile, stack);
    }

    @Override
    public boolean canEntityDestroy(BlockState state, BlockGetter world, BlockPos pos, Entity entity) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ITBlockInterfaces.IEntityProof) { return ((ITBlockInterfaces.IEntityProof) tile).canEntityDestroy(entity); }
        return super.canEntityDestroy(state, world, pos, entity);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ITBlockInterfaces.IBlockEntityDrop && target instanceof BlockHitResult) {
            ItemStack s = ((ITBlockInterfaces.IBlockEntityDrop) tile).getPickBlock(player, world.getBlockState(pos), target);
            if (!s.isEmpty()) { return s; }
        }
        Item item = this.asItem();
        return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item, 1);
    }

    @Override
    public boolean triggerEvent(@NotNull BlockState state, Level worldIn, @NotNull BlockPos pos, int eventID, int eventParam) {
        super.triggerEvent(state, worldIn, pos, eventID, eventParam);
        BlockEntity blockEntity = worldIn.getBlockEntity(pos);
        return blockEntity != null && blockEntity.triggerEvent(eventID, eventParam);
    }

    protected Direction getDefaultFacing() { return Direction.NORTH; }

    @Override
    public void onIEBlockPlacedBy(BlockPlaceContext context, BlockState state) {
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

    @Override
    public InteractionResult hammerUseSide(Direction side, Player player, InteractionHand hand, Level w, BlockPos pos, BlockHitResult hit) {
        BlockEntity tile = w.getBlockEntity(pos);
        if (tile instanceof ITBlockInterfaces.IHammerInteraction) {
            boolean b = ((ITBlockInterfaces.IHammerInteraction) tile).hammerUseSide(side, player, hand, hit.getLocation());
            if (b) { return InteractionResult.SUCCESS; }
            else { return InteractionResult.FAIL; }
        }
        return super.hammerUseSide(side, player, hand, w, pos, hit);
    }

    @Override
    public InteractionResult screwdriverUseSide(Direction side, Player player, InteractionHand hand, Level w, BlockPos pos, BlockHitResult hit) {
        BlockEntity tile = w.getBlockEntity(pos);
        if (tile instanceof ITBlockInterfaces.IScrewdriverInteraction) {
            InteractionResult teResult = ((ITBlockInterfaces.IScrewdriverInteraction) tile).screwdriverUseSide(side, player, hand, hit.getLocation());
            if (teResult != InteractionResult.PASS) { return teResult; }
        }
        return super.screwdriverUseSide(side, player, hand, w, pos, hit);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        InteractionResult superResult = super.use(state, world, pos, player, hand, hit);
        if (superResult.consumesAction()) { return superResult; }
        Direction side = hit.getDirection();
        float hitX = (float) hit.getLocation().x - pos.getX();
        float hitY = (float) hit.getLocation().y - pos.getY();
        float hitZ = (float) hit.getLocation().z - pos.getZ();
        ItemStack heldItem = player.getItemInHand(hand);
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ITBlockInterfaces.IDirectionalBE && Utils.isHammer(heldItem) && ((ITBlockInterfaces.IDirectionalBE) tile).canHammerRotate(side, hit.getLocation().subtract(Vec3.atLowerCornerOf(pos)), player) && !world.isClientSide) {
            Direction f = ((ITBlockInterfaces.IDirectionalBE) tile).getFacing();
            Direction oldF = f;
            PlacementLimitation limit = ((ITBlockInterfaces.IDirectionalBE) tile).getFacingLimitation();
            f = switch (limit) {
                case SIDE_CLICKED -> DirectionUtils.VALUES[Math.floorMod(f.ordinal() + (player.isShiftKeyDown() ? -1 : 1), DirectionUtils.VALUES.length)];
                case PISTON_LIKE -> player.isShiftKeyDown() != (side.getAxisDirection() == Direction.AxisDirection.NEGATIVE) ? DirectionUtils.rotateAround(f, side.getAxis()).getOpposite() : DirectionUtils.rotateAround(f, side.getAxis());
                case HORIZONTAL, HORIZONTAL_PREFER_SIDE, HORIZONTAL_QUADRANT, HORIZONTAL_AXIS -> player.isShiftKeyDown() != side.equals(Direction.DOWN) ? f.getCounterClockWise() : f.getClockWise();
                default -> f;
            };
            ((ITBlockInterfaces.IDirectionalBE) tile).setFacing(f);
            ((ITBlockInterfaces.IDirectionalBE) tile).afterRotation(oldF, f);
            tile.setChanged();
            world.sendBlockUpdated(pos, state, state, 3);
            world.blockEvent(tile.getBlockPos(), tile.getBlockState().getBlock(), 255, 0);
            return InteractionResult.SUCCESS;
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

    @Override
    public void neighborChanged(@NotNull BlockState state, Level world, @NotNull BlockPos pos, @NotNull Block block, @NotNull BlockPos fromPos, boolean isMoving) {
        if (!world.isClientSide) {
            BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof ITBaseBlockEntity) { ((ITBaseBlockEntity) tile).onNeighborBlockChange(fromPos); }
        }
    }

    public ITEntityBlock<T> setHasColours() {
        this.hasColours = true;
        return this;
    }

    @Override
    public boolean hasCustomBlockColours() { return hasColours; }

    @Override
    public int getRenderColour(@NotNull BlockState state, @Nullable BlockGetter worldIn, @Nullable BlockPos pos, int tintIndex) {
        if (worldIn != null && pos != null) {
            BlockEntity tile = worldIn.getBlockEntity(pos);
            if (tile instanceof ITBlockInterfaces.IColouredBE) { return ((ITBlockInterfaces.IColouredBE) tile).getRenderColour(tintIndex); }
        }
        return 0xffffff;
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        if (state.getBlock() == this) {
            BlockEntity te = world.getBlockEntity(pos);
            if (te instanceof ITBlockInterfaces.ISelectionBounds) { return ((ITBlockInterfaces.ISelectionBounds) te).getSelectionShape(context); }
        }
        return super.getShape(state, world, pos, context);
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        if (getClassData().customCollisionBounds()) {
            BlockEntity te = world.getBlockEntity(pos);
            if (te instanceof ITBlockInterfaces.ICollisionBounds collisionBounds) { return collisionBounds.getCollisionShape(context); }
            else { return Shapes.empty(); }
        }
        return super.getCollisionShape(state, world, pos, context);
    }

    @Override
    public @NotNull VoxelShape getInteractionShape(@NotNull BlockState state, BlockGetter world, @NotNull BlockPos pos) {
        if (world.getBlockState(pos).getBlock() == this) {
            BlockEntity te = world.getBlockEntity(pos);
            if (te instanceof ITBlockInterfaces.ISelectionBounds) { return ((ITBlockInterfaces.ISelectionBounds) te).getSelectionShape(null); }
        }
        return super.getInteractionShape(state, world, pos);
    }

    @Override
    public boolean hasAnalogOutputSignal(@NotNull BlockState state) { return getClassData().hasComparatorOutput; }

    @Override
    public int getAnalogOutputSignal(@NotNull BlockState state, Level world, @NotNull BlockPos pos) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof ITBlockInterfaces.IComparatorOverride compOverride) { return compOverride.getComparatorInputOverride(); }
        return 0;
    }

    @Override
    public int getSignal(@NotNull BlockState blockState, BlockGetter world, @NotNull BlockPos pos, @NotNull Direction side) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof ITBlockInterfaces.IRedstoneOutput rsOutput) { return rsOutput.getWeakRSOutput(side); }
        return 0;
    }

    @Override
    public int getDirectSignal(@NotNull BlockState blockState, BlockGetter world, @NotNull BlockPos pos, @NotNull Direction side) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof ITBlockInterfaces.IRedstoneOutput rsOutput) { return rsOutput.getStrongRSOutput(side); }
        return 0;
    }

    @Override
    public boolean isSignalSource(@NotNull BlockState state) { return getClassData().emitsRedstone(); }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof ITBlockInterfaces.IRedstoneOutput rsOutput) { return rsOutput.canConnectRedstone(side); }
        return false;
    }

    @Override
    public void entityInside(@NotNull BlockState state, Level world, @NotNull BlockPos pos, @NotNull Entity entity) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof ITBaseBlockEntity) { ((ITBaseBlockEntity) te).onEntityCollision(world, entity); }
    }

    public static boolean areAllReplaceable(BlockPos start, BlockPos end, BlockPlaceContext context) {
        Level w = context.getLevel();
        return BlockPos.betweenClosedStream(start, end).allMatch(pos -> { BlockPlaceContext subContext = BlockPlaceContext.at(context, pos, context.getClickedFace()); return w.getBlockState(pos).canBeReplaced(subContext); });
    }

    private BEClassInspectedData getClassData() {
        if (this.classData == null) {
            T tempBE = makeEntity.apply(BlockPos.ZERO, getInitDefaultState());
            this.classData = new BEClassInspectedData(tempBE instanceof ITServerTickableBE, tempBE instanceof ITClientTickableBE, tempBE instanceof ITBlockInterfaces.IComparatorOverride, tempBE instanceof ITBlockInterfaces.IRedstoneOutput, tempBE instanceof ITBlockInterfaces.ICollisionBounds);
        }
        return this.classData;
    }

    private record BEClassInspectedData(boolean serverTicking, boolean clientTicking, boolean hasComparatorOutput, boolean emitsRedstone, boolean customCollisionBounds) {
        @Nullable
        public <U extends BlockEntity> BlockEntityTicker<U> makeBaseTicker(boolean isClient) {
            if (serverTicking && !isClient) { return ITServerTickableBE.makeTicker(); }
            else if (clientTicking && isClient) { return ITClientTickableBE.makeTicker(); }
            else { return null; }
        }
    }
}
