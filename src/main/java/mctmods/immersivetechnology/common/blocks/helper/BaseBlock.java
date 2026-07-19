package mctmods.immersivetechnology.common.blocks.helper;

import mctmods.immersivetechnology.core.registration.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import javax.annotation.Nullable;

public class BaseBlock extends Block implements IBlock, SimpleWaterloggedBlock {
    protected final boolean notNormalBlock;
    private final boolean fitsIntoContainer;
    boolean hasFlavour;

    public BaseBlock(BlockBehaviour.Properties blockProps, boolean fitsIntoContainer) {
        super(blockProps);
        this.fitsIntoContainer = fitsIntoContainer;
        this.notNormalBlock = !this.defaultBlockState().canOcclude();
        this.registerDefaultState(this.getInitDefaultState());
    }

    public String getNameForFlavour() { return BuiltInRegistries.BLOCK.getKey(this).getPath(); }

    public boolean hasFlavour() { return this.hasFlavour; }

    @Override public int getLightBlock(@NotNull BlockState state, @NotNull BlockGetter worldIn, @NotNull BlockPos pos) {
        return this.notNormalBlock ? 0 : super.getLightBlock(state, worldIn, pos);
    }

    @Override public float getShadeBrightness(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos) {
        return this.notNormalBlock ? 1.0F : super.getShadeBrightness(state, world, pos);
    }

    @Override public boolean propagatesSkylightDown(@NotNull BlockState state, @NotNull BlockGetter reader, @NotNull BlockPos pos) {
        return this.notNormalBlock || super.propagatesSkylightDown(state, reader, pos);
    }

    protected BlockState getInitDefaultState() {
        BlockState state = this.stateDefinition.any();
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) { state = state.setValue(BlockStateProperties.WATERLOGGED, Boolean.FALSE); }
        return state;
    }

    @SuppressWarnings("unused")
    public void onIEBlockPlacedBy(BlockPlaceContext context, BlockState state) {}

    @SuppressWarnings("unused")
    public boolean canIEBlockBePlaced(BlockState newState, BlockPlaceContext context) { return true; }

    @Override public void setPlacedBy(@NotNull Level worldIn, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        super.setPlacedBy(worldIn, pos, state, placer, stack);
    }

    @Override public boolean triggerEvent(@NotNull BlockState state, Level worldIn, @NotNull BlockPos pos, int eventID, int eventParam) {
        if (worldIn.isClientSide && eventID == 255) { worldIn.sendBlockUpdated(pos, state, state, 3); return true; }
        else { return super.triggerEvent(state, worldIn, pos, eventID, eventParam); }
    }

    @Override @NotNull public InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
        ItemStack activeStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (activeStack.is(ModTags.formationTools)) { return this.hammerUseSide(hit.getDirection(), player, InteractionHand.MAIN_HAND, world, pos, hit); }
        else if (activeStack.is(ModTags.screwdrivers)) { return this.screwdriverUseSide(hit.getDirection(), player, InteractionHand.MAIN_HAND, world, pos, hit); }
        else { return super.useWithoutItem(state, world, pos, player, hit); }
    }

    public InteractionResult hammerUseSide(Direction side, Player player, InteractionHand hand, Level w, BlockPos pos, BlockHitResult hit) {
        return InteractionResult.PASS;
    }

    public InteractionResult screwdriverUseSide(Direction side, Player player, InteractionHand hand, Level w, BlockPos pos, BlockHitResult hit) {
        return InteractionResult.PASS;
    }

    @Override public boolean isPathfindable(@NotNull BlockState state, @NotNull PathComputationType type) {
        return false;
    }

    public static BlockState applyLocationalWaterlogging(BlockState state, Level world, BlockPos pos) {
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            state = state.setValue(BlockStateProperties.WATERLOGGED, world.getFluidState(pos).getType() == Fluids.WATER);
        }
        return state;
    }

    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = this.defaultBlockState();
        state = applyLocationalWaterlogging(state, context.getLevel(), context.getClickedPos());
        return state;
    }

    @Override @NotNull public BlockState updateShape(BlockState stateIn, @NotNull Direction facing, @NotNull BlockState facingState, @NotNull LevelAccessor worldIn, @NotNull BlockPos currentPos, @NotNull BlockPos facingPos) {
        if (stateIn.hasProperty(BlockStateProperties.WATERLOGGED) && stateIn.getValue(BlockStateProperties.WATERLOGGED)) {
            worldIn.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override @NotNull public FluidState getFluidState(BlockState state) {
        return state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED) ?
                Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override public boolean canPlaceLiquid(@Nullable Player player, @NotNull BlockGetter worldIn, @NotNull BlockPos pos, BlockState state, @NotNull Fluid fluidIn) {
        return state.hasProperty(BlockStateProperties.WATERLOGGED) && SimpleWaterloggedBlock.super.canPlaceLiquid(player, worldIn, pos, state, fluidIn);
    }

    @Override public boolean placeLiquid(@NotNull LevelAccessor worldIn, @NotNull BlockPos pos, BlockState state, @NotNull FluidState fluidStateIn) {
        return state.hasProperty(BlockStateProperties.WATERLOGGED) && SimpleWaterloggedBlock.super.placeLiquid(worldIn, pos, state, fluidStateIn);
    }

    @Override @NotNull public ItemStack pickupBlock(@Nullable Player player, @NotNull LevelAccessor level, @NotNull BlockPos pos, BlockState state) {
        return state.hasProperty(BlockStateProperties.WATERLOGGED) ? SimpleWaterloggedBlock.super.pickupBlock(player, level, pos, state) : ItemStack.EMPTY;
    }

    public boolean fitsIntoContainer() { return this.fitsIntoContainer; }

    @Override @NotNull public BlockState rotate(@NotNull BlockState state, @NotNull Rotation rot) {
        Property<Direction> facingProp = this.findFacingProperty(state);
        if (facingProp != null && this.canRotate()) {
            Direction currentDirection = state.getValue(facingProp);
            Direction newDirection = rot.rotate(currentDirection);
            return state.setValue(facingProp, newDirection);
        }
        else { return super.rotate(state, rot); }
    }

    @Override @NotNull public BlockState mirror(BlockState state, @NotNull Mirror mirrorIn) {
        if (mirrorIn == Mirror.NONE) { return state; }
        boolean handled = false;
        Property<Direction> facingProp = this.findFacingProperty(state);
        if (facingProp != null && this.canRotate()) {
            state = state.setValue(facingProp, mirrorIn.mirror(state.getValue(facingProp)));
            handled = true;
        }
        if (state.hasProperty(ModProperties.MIRRORED) && this.canRotate()) {
            state = state.setValue(ModProperties.MIRRORED, !state.getValue(ModProperties.MIRRORED));
            handled = true;
        }
        if (!handled) { return super.mirror(state, mirrorIn); }
        return state;
    }

    @Nullable private Property<Direction> findFacingProperty(BlockState state) {
        if (state.hasProperty(ModProperties.FACING_ALL)) { return ModProperties.FACING_ALL; }
        else { return state.hasProperty(ModProperties.FACING_HORIZONTAL) ? ModProperties.FACING_HORIZONTAL : null; }
    }

    protected boolean canRotate() {
        return !this.getStateDefinition().getProperties().contains(ModProperties.MULTIBLOCKSLAVE);
    }

    @SuppressWarnings("unused")
    public Component[] getOverlayText(BlockState state, Level level, BlockPos pos, Player player, HitResult rayTrace, boolean hammer) {
        return null;
    }
}
