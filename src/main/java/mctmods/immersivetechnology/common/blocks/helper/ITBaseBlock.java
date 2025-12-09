package mctmods.immersivetechnology.common.blocks.helper;

import mctmods.immersivetechnology.core.registration.ITTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import javax.annotation.Nullable;
import java.util.Objects;

@SuppressWarnings("deprecation")
public class ITBaseBlock extends Block implements ITBlock, SimpleWaterloggedBlock {
    protected final boolean notNormalBlock;
    private final boolean fitsIntoContainer;
    boolean hasFlavour;

    public ITBaseBlock(BlockBehaviour.Properties blockProps, boolean fitsIntoContainer) {
        super(blockProps);
        this.fitsIntoContainer = fitsIntoContainer;
        this.notNormalBlock = !this.defaultBlockState().canOcclude();
        this.registerDefaultState(this.getInitDefaultState());
    }

    public String getNameForFlavour() { return Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(this)).getPath(); }

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

    @Override public void setPlacedBy(@NotNull Level worldIn, @NotNull BlockPos pos, @NotNull BlockState state, LivingEntity placer, @NotNull ItemStack stack) {
        super.setPlacedBy(worldIn, pos, state, placer, stack);
    }

    @Override public boolean triggerEvent(@NotNull BlockState state, Level worldIn, @NotNull BlockPos pos, int eventID, int eventParam) {
        if (worldIn.isClientSide && eventID == 255) { worldIn.sendBlockUpdated(pos, state, state, 3); return true; }
        else { return super.triggerEvent(state, worldIn, pos, eventID, eventParam); }
    }

    @Override @NotNull public InteractionResult use(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        ItemStack activeStack = player.getItemInHand(hand);
        if (activeStack.is(ITTags.formationTools)) { return this.hammerUseSide(hit.getDirection(), player, hand, world, pos, hit); }
        else if (activeStack.is(ITTags.screwdrivers)) { return this.screwdriverUseSide(hit.getDirection(), player, hand, world, pos, hit); }
        else { return super.use(state, world, pos, player, hand, hit); }
    }

    public InteractionResult hammerUseSide(Direction side, Player player, InteractionHand hand, Level w, BlockPos pos, BlockHitResult hit) {
        return InteractionResult.PASS;
    }

    public InteractionResult screwdriverUseSide(Direction side, Player player, InteractionHand hand, Level w, BlockPos pos, BlockHitResult hit) {
        return InteractionResult.PASS;
    }

    @Override public boolean isPathfindable(@NotNull BlockState state, @NotNull BlockGetter worldIn, @NotNull BlockPos pos, @NotNull PathComputationType type) {
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
            worldIn.getFluidTicks().schedule(new ScheduledTick<>(Fluids.WATER, currentPos, Fluids.WATER.getTickDelay(worldIn), 0L));
        }
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override @NotNull public FluidState getFluidState(BlockState state) {
        return state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED) ?
                Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override public boolean canPlaceLiquid(@NotNull BlockGetter worldIn, @NotNull BlockPos pos, BlockState state, @NotNull Fluid fluidIn) {
        return state.hasProperty(BlockStateProperties.WATERLOGGED) && SimpleWaterloggedBlock.super.canPlaceLiquid(worldIn, pos, state, fluidIn);
    }

    @Override public boolean placeLiquid(@NotNull LevelAccessor worldIn, @NotNull BlockPos pos, BlockState state, @NotNull FluidState fluidStateIn) {
        return state.hasProperty(BlockStateProperties.WATERLOGGED) && SimpleWaterloggedBlock.super.placeLiquid(worldIn, pos, state, fluidStateIn);
    }

    @Override @NotNull public ItemStack pickupBlock(@NotNull LevelAccessor level, @NotNull BlockPos pos, BlockState state) {
        return state.hasProperty(BlockStateProperties.WATERLOGGED) ? SimpleWaterloggedBlock.super.pickupBlock(level, pos, state) : ItemStack.EMPTY;
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
        if (state.hasProperty(ITProperties.MIRRORED) && this.canRotate() && mirrorIn == Mirror.LEFT_RIGHT) {
            return state.setValue(ITProperties.MIRRORED, !state.getValue(ITProperties.MIRRORED));
        }
        else {
            Property<Direction> facingProp = this.findFacingProperty(state);
            if (facingProp != null && this.canRotate()) {
                Direction currentDirection = state.getValue(facingProp);
                Direction newDirection = mirrorIn.mirror(currentDirection);
                return state.setValue(facingProp, newDirection);
            }
            else { return super.mirror(state, mirrorIn); }
        }
    }

    @Nullable private Property<Direction> findFacingProperty(BlockState state) {
        if (state.hasProperty(ITProperties.FACING_ALL)) { return ITProperties.FACING_ALL; }
        else { return state.hasProperty(ITProperties.FACING_HORIZONTAL) ? ITProperties.FACING_HORIZONTAL : null; }
    }

    protected boolean canRotate() {
        return !this.getStateDefinition().getProperties().contains(ITProperties.MULTIBLOCKSLAVE);
    }
}
