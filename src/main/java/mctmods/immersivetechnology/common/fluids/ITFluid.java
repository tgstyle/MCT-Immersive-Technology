package mctmods.immersivetechnology.common.fluids;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import mctmods.immersivetechnology.core.registration.ITFluids;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nonnull;

public class ITFluid extends FlowingFluid {
    private static ITFluids.FluidEntry entryStatic;
    protected final ITFluids.FluidEntry entry;

    private static final int CACHE_SIZE = 200;
    private static final ThreadLocal<Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey>> OCCLUSION_CACHE = ThreadLocal.withInitial(() -> {
        Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey> object2bytelinkedopenhashmap = new Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey>(CACHE_SIZE) {
            protected void rehash(int p_76102_) {
            }
        };
        object2bytelinkedopenhashmap.defaultReturnValue((byte)127);
        return object2bytelinkedopenhashmap;
    });
    private final Map<FluidState, VoxelShape> shapes = Maps.newIdentityHashMap();

    public static ITFluid makeFluid(Function<ITFluids.FluidEntry, ? extends ITFluid> make, ITFluids.FluidEntry entry) {
        entryStatic = entry;
        ITFluid result = make.apply(entry);
        entryStatic = null;
        return result;
    }

    public ITFluid(ITFluids.FluidEntry entry) { this.entry = entry; }

    protected boolean isGaseous() { return getFluidType().getDensity() < 0; }

    @Nonnull
    @Override
    public Item getBucket() { return entry.getBucket(); }

    @Override
    protected boolean canBeReplacedWith(@NotNull FluidState fluidState, @NotNull BlockGetter blockReader, @NotNull BlockPos pos, @NotNull Fluid fluidIn, @NotNull Direction direction) { return (isGaseous() ? direction == Direction.UP : direction == Direction.DOWN) && !isSame(fluidIn); }

    @Override
    public boolean isSame(@Nonnull Fluid fluidIn) { return fluidIn == entry.getStill() || fluidIn == entry.getFlowing(); }

    @Override
    public int getTickDelay(@NotNull LevelReader p_205569_1_) {
        int dW = this.getFlowing().getFluidType().getViscosity() - Fluids.WATER.getFluidType().getViscosity();
        double v = Math.round(5 + dW * 0.005);
        return Math.max(2, (int) v);
    }

    @Override
    protected float getExplosionResistance() { return 100; }

    @Override
    protected void createFluidStateDefinition(@NotNull StateDefinition.Builder<Fluid, FluidState> builder) {
        super.createFluidStateDefinition(builder);
        for (Property<?> p : (entry == null ? entryStatic : entry).properties()) { builder.add(p); }
    }

    @Override
    protected @NotNull BlockState createLegacyBlock(@NotNull FluidState state) {
        BlockState result = entry.getBlock().defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
        for (Property<?> prop : entry.properties()) { result = ITFluidBlock.withCopiedValue(prop, result, state); }
        return result;
    }

    @Override
    public boolean isSource(FluidState state) { return state.getType() == entry.getStill(); }

    @Override
    public int getAmount(@NotNull FluidState state) {
        if (isSource(state)) { return 8; }
        else { return state.getValue(LEVEL); }
    }

    @Override
    public @NotNull FluidType getFluidType() { return entry.type().get(); }

    @Nonnull
    @Override
    public Fluid getFlowing() { return entry.getFlowing(); }

    @Nonnull
    @Override
    public Fluid getSource() { return entry.getStill(); }

    @Override
    public boolean canConvertToSource(@NotNull Level level) { return false; }

    @Override
    protected void beforeDestroyingBlock(@NotNull LevelAccessor iWorld, @NotNull BlockPos blockPos, @NotNull BlockState blockState) { }

    @Override
    protected int getSlopeFindDistance(@NotNull LevelReader iWorldReader) { return 4; }

    @Override
    protected int getDropOff(@NotNull LevelReader iWorldReader) { return 1; }

    @Override
    public void tick(@NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull FluidState pState) {
        super.tick(pLevel, pPos, pState);
        if (!pLevel.isClientSide && isGaseous() && pState.isSource()) { pLevel.setBlock(pPos, Blocks.AIR.defaultBlockState(), 3); }
    }

    // Copied and modified from FlowingFluid
    @Override
    protected void spread(@NotNull Level pLevel, @NotNull BlockPos pPos, FluidState pState) {
        if (!pState.isEmpty()) {
            BlockState blockstate = pLevel.getBlockState(pPos);
            Direction gravityDir = isGaseous() ? Direction.UP : Direction.DOWN;
            BlockPos blockpos = pPos.relative(gravityDir);
            BlockState blockstate1 = pLevel.getBlockState(blockpos);
            FluidState fluidstate = this.getNewLiquid(pLevel, blockpos, blockstate1);
            if (this.canSpreadTo(pLevel, pPos, blockstate, gravityDir, blockpos, blockstate1, pLevel.getFluidState(blockpos), fluidstate.getType())) {
                this.spreadTo(pLevel, blockpos, blockstate1, gravityDir, fluidstate);
                if (this.mySourceNeighborCount(pLevel, pPos) >= 3) { this.mySpreadToSides(pLevel, pPos, pState, blockstate); }
            }
            else if (pState.isSource() || !this.myIsHole(pLevel, fluidstate.getType(), pPos, blockstate, blockpos, blockstate1)) { this.mySpreadToSides(pLevel, pPos, pState, blockstate); }
        }
    }

    // Copied from FlowingFluid.spreadToSides
    private void mySpreadToSides(Level pLevel, BlockPos pPos, FluidState pFluidState, BlockState pBlockState) {
        int i = pFluidState.getAmount() - this.getDropOff(pLevel);
        if (pFluidState.getValue(FALLING)) { i = 7; }

        if (i > 0) {
            Map<Direction, FluidState> map = this.getSpread(pLevel, pPos, pBlockState);

            for (Map.Entry<Direction, FluidState> entry : map.entrySet()) {
                Direction direction = entry.getKey();
                FluidState fluidstate = entry.getValue();
                BlockPos blockpos = pPos.relative(direction);
                BlockState blockstate = pLevel.getBlockState(blockpos);
                if (this.myCanPassThrough(pLevel, fluidstate.getType(), pPos, pBlockState, direction, blockpos, blockstate, pLevel.getFluidState(blockpos))) { this.spreadTo(pLevel, blockpos, blockstate, direction, fluidstate); }
            }
        }
    }

    // Copied and modified from FlowingFluid.getNewLiquid
    @Override
    protected FluidState getNewLiquid(Level pLevel, BlockPos pPos, BlockState pBlockState) {
        boolean isGaseous = isGaseous();
        Direction gravityDir = isGaseous ? Direction.UP : Direction.DOWN;
        Direction antiGravityDir = isGaseous ? Direction.DOWN : Direction.UP;
        int i = 0;
        int j = 0;

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos blockpos = pPos.relative(direction);
            BlockState blockstate = pLevel.getBlockState(blockpos);
            FluidState fluidstate = blockstate.getFluidState();
            if (fluidstate.getType().isSame(this) && this.myCanPassThroughWall(direction, pLevel, pPos, pBlockState, blockpos, blockstate)) {
                if (fluidstate.isSource() && net.minecraftforge.event.ForgeEventFactory.canCreateFluidSource(pLevel, blockpos, blockstate, fluidstate.canConvertToSource(pLevel, blockpos))) { ++j; }
                i = Math.max(i, fluidstate.getAmount());
            }
        }

        if (j >= 2) {
            BlockPos floorPos = pPos.relative(gravityDir);
            BlockState blockstate1 = pLevel.getBlockState(floorPos);
            FluidState fluidstate1 = blockstate1.getFluidState();
            if (blockstate1.isSolid() || this.myIsSourceBlockOfThisType(fluidstate1)) { return this.getSource(false); }
        }

        BlockPos blockpos1 = pPos.relative(antiGravityDir);
        BlockState blockstate2 = pLevel.getBlockState(blockpos1);
        FluidState fluidstate2 = blockstate2.getFluidState();
        if (!fluidstate2.isEmpty() && fluidstate2.getType().isSame(this) && this.myCanPassThroughWall(antiGravityDir, pLevel, pPos, pBlockState, blockpos1, blockstate2)) { return this.getFlowing(8, true); }
        else {
            int k = i - this.getDropOff(pLevel);
            return k <= 0 ? Fluids.EMPTY.defaultFluidState() : this.getFlowing(k, false);
        }
    }

    // Copied and modified from FlowingFluid.getFlow
    @Override
    public @NotNull Vec3 getFlow(@NotNull BlockGetter pBlockReader, @NotNull BlockPos pPos, @NotNull FluidState pFluidState) {
        double d0 = 0.0D;
        double d1 = 0.0D;
        MutableBlockPos mutable = new MutableBlockPos();

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            mutable.setWithOffset(pPos, direction);
            FluidState fluidstate = pBlockReader.getFluidState(mutable);
            if (this.myAffectsFlow(fluidstate)) {
                float f = fluidstate.getOwnHeight();
                float f1 = 0.0F;
                if (f == 0.0F) {
                    if (!pBlockReader.getBlockState(mutable).blocksMotion()) {
                        BlockPos blockpos = mutable.below();
                        FluidState fluidstate1 = pBlockReader.getFluidState(blockpos);
                        if (this.myAffectsFlow(fluidstate1)) {
                            f = fluidstate1.getOwnHeight();
                            if (f > 0.0F) { f1 = pFluidState.getOwnHeight() - (f - 0.8888889F); }
                        }
                    }
                } else if (f > 0.0F) { f1 = pFluidState.getOwnHeight() - f; }

                if (f1 != 0.0F) {
                    d0 += (double) ((float) direction.getStepX() * f1);
                    d1 += (double) ((float) direction.getStepZ() * f1);
                }
            }
        }

        Vec3 vec3 = new Vec3(d0, 0.0D, d1);
        if (pFluidState.getValue(FALLING)) {
            for (Direction direction1 : Direction.Plane.HORIZONTAL) {
                mutable.setWithOffset(pPos, direction1);
                Direction checkOffset = isGaseous() ? Direction.DOWN : Direction.UP;
                if (this.isSolidFace(pBlockReader, mutable, direction1) || this.isSolidFace(pBlockReader, mutable.relative(checkOffset), direction1)) {
                    double yAdd = isGaseous() ? 6.0D : -6.0D;
                    vec3 = vec3.normalize().add(0.0D, yAdd, 0.0D);
                    break;
                }
            }
        }

        return vec3.normalize();
    }

    // Copied from FlowingFluid.affectsFlow
    private boolean myAffectsFlow(FluidState pState) { return pState.isEmpty() || pState.getType().isSame(this); }

    // Copied from FlowingFluid.sourceNeighborCount
    private int mySourceNeighborCount(LevelReader pLevel, BlockPos pPos) {
        int i = 0;

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos blockpos = pPos.relative(direction);
            FluidState fluidstate = pLevel.getFluidState(blockpos);
            if (this.myIsSourceBlockOfThisType(fluidstate)) { ++i; }
        }

        return i;
    }

    // Copied and modified from FlowingFluid.isWaterHole
    private boolean myIsHole(BlockGetter pLevel, Fluid pFluid, BlockPos pPos, BlockState pState, BlockPos pTargetPos, BlockState pTargetState) {
        Direction dir = isGaseous() ? Direction.UP : Direction.DOWN;
        if (!this.myCanPassThroughWall(dir, pLevel, pPos, pState, pTargetPos, pTargetState)) { return false; }
        else { return pTargetState.getFluidState().getType().isSame(this) || this.myCanHoldFluid(pLevel, pTargetPos, pTargetState, pFluid); }
    }

    // Copied from FlowingFluid.canHoldFluid
    private boolean myCanHoldFluid(BlockGetter pLevel, BlockPos pPos, BlockState pState, Fluid pFluid) {
        Block block = pState.getBlock();
        if (block instanceof LiquidBlockContainer) {
            return ((LiquidBlockContainer) block).canPlaceLiquid(pLevel, pPos, pState, pFluid);
        } else if (!(block instanceof net.minecraft.world.level.block.DoorBlock) && !pState.is(net.minecraft.tags.BlockTags.SIGNS) && !pState.is(Blocks.LADDER) && !pState.is(Blocks.SUGAR_CANE) && !pState.is(Blocks.BUBBLE_COLUMN)) {
            if (!pState.is(Blocks.NETHER_PORTAL) && !pState.is(Blocks.END_PORTAL) && !pState.is(Blocks.END_GATEWAY) && !pState.is(Blocks.STRUCTURE_VOID)) {
                return !pState.blocksMotion();
            } else { return false; }
        } else { return false; }
    }

    // Copied from FlowingFluid.isSourceBlockOfThisType
    private boolean myIsSourceBlockOfThisType(FluidState pState) { return pState.getType().isSame(this) && pState.isSource(); }

    // Copied from FlowingFluid.canPassThrough
    private boolean myCanPassThrough(BlockGetter pLevel, Fluid pFluid, BlockPos pPos, BlockState pState, Direction pDirection, BlockPos pTargetPos, BlockState pTargetState, FluidState pTargetFluid) { return !this.myIsSourceBlockOfThisType(pTargetFluid) && this.myCanPassThroughWall(pDirection, pLevel, pPos, pState, pTargetPos, pTargetState) && this.myCanHoldFluid(pLevel, pTargetPos, pTargetState, pFluid); }

    // Copied from FlowingFluid.getSlopeDistance
    private int myGetSlopeDistance(LevelReader pLevel, BlockPos pPos, int pDist, Direction pOppositeDir, BlockState pState, BlockPos pOrigin, Short2ObjectMap<Pair<BlockState, FluidState>> pStateCache, Short2BooleanMap pHoleCache) {
        int i = 1000;

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (direction != pOppositeDir) {
                BlockPos blockpos = pPos.relative(direction);
                short short1 = myGetCacheKey(pOrigin, blockpos);
                Pair<BlockState, FluidState> pair = pStateCache.computeIfAbsent(short1, (p_284932_) -> {
                    BlockState blockstate1 = pLevel.getBlockState(blockpos);
                    return Pair.of(blockstate1, blockstate1.getFluidState());
                });
                BlockState blockstate = pair.getFirst();
                FluidState fluidstate = pair.getSecond();
                if (this.myCanPassThrough(pLevel, this.getFlowing(), pPos, pState, direction, blockpos, blockstate, fluidstate)) {
                    boolean flag = pHoleCache.computeIfAbsent(short1, (p_255612_) -> {
                        BlockPos blockpos1 = blockpos.relative(isGaseous() ? Direction.UP : Direction.DOWN);
                        BlockState blockstate1 = pLevel.getBlockState(blockpos1);
                        return this.myIsHole(pLevel, this.getFlowing(), blockpos, blockstate, blockpos1, blockstate1);
                    });
                    if (flag) { return pDist; }

                    if (pDist < this.getSlopeFindDistance(pLevel)) {
                        int j = this.myGetSlopeDistance(pLevel, blockpos, pDist + 1, direction.getOpposite(), blockstate, pOrigin, pStateCache, pHoleCache);
                        if (j < i) {
                            i = j;
                        }
                    }
                }
            }
        }

        return i;
    }

    // Override getSpread to use myGetSlopeDistance
    @Override
    protected Map<Direction, FluidState> getSpread(Level pLevel, BlockPos pPos, BlockState pState) {
        int i = 1000;
        Map<Direction, FluidState> map = Maps.newEnumMap(Direction.class);
        Short2ObjectMap<Pair<BlockState, FluidState>> short2objectmap = new Short2ObjectOpenHashMap<>();
        Short2BooleanMap short2booleanmap = new Short2BooleanOpenHashMap();

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos blockpos = pPos.relative(direction);
            short short1 = myGetCacheKey(pPos, blockpos);
            Pair<BlockState, FluidState> pair = short2objectmap.computeIfAbsent(short1, (p_284929_) -> {
                BlockState blockstate1 = pLevel.getBlockState(blockpos);
                return Pair.of(blockstate1, blockstate1.getFluidState());
            });
            BlockState blockstate = pair.getFirst();
            FluidState fluidstate = pair.getSecond();
            FluidState fluidstate1 = this.getNewLiquid(pLevel, blockpos, blockstate);
            if (this.myCanPassThrough(pLevel, fluidstate1.getType(), pPos, pState, direction, blockpos, blockstate, fluidstate)) {
                boolean flag = short2booleanmap.computeIfAbsent(short1, (p_255612_) -> {
                    BlockPos blockpos1 = blockpos.relative(isGaseous() ? Direction.UP : Direction.DOWN);
                    BlockState blockstate1 = pLevel.getBlockState(blockpos1);
                    return this.myIsHole(pLevel, this.getFlowing(), blockpos, blockstate, blockpos1, blockstate1);
                });
                int j;
                if (flag) { j = 0; }
                else { j = this.myGetSlopeDistance(pLevel, blockpos, 1, direction.getOpposite(), blockstate, pPos, short2objectmap, short2booleanmap); }

                if (j < i) { map.clear(); }

                if (j <= i) {
                    map.put(direction, fluidstate1);
                    i = j;
                }
            }
        }

        return map;
    }

    // Copied from FlowingFluid.canPassThroughWall
    private boolean myCanPassThroughWall(Direction pDirection, BlockGetter pLevel, BlockPos pPos, BlockState pState, BlockPos pTargetPos, BlockState pTargetState) {
        Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey> object2bytelinkedopenhashmap;
        if (!pState.getBlock().hasDynamicShape() && !pTargetState.getBlock().hasDynamicShape()) { object2bytelinkedopenhashmap = OCCLUSION_CACHE.get(); }
        else { object2bytelinkedopenhashmap = null; }

        Block.BlockStatePairKey block$blockstatepairkey;
        if (object2bytelinkedopenhashmap != null) {
            block$blockstatepairkey = new Block.BlockStatePairKey(pState, pTargetState, pDirection);
            byte b0 = object2bytelinkedopenhashmap.getAndMoveToFirst(block$blockstatepairkey);
            if (b0 != 127) { return b0 != 0; }
        }
        else { block$blockstatepairkey = null; }

        VoxelShape voxelshape1 = pState.getCollisionShape(pLevel, pPos);
        VoxelShape voxelshape = pTargetState.getCollisionShape(pLevel, pTargetPos);
        boolean flag = !Shapes.mergedFaceOccludes(voxelshape1, voxelshape, pDirection);
        if (object2bytelinkedopenhashmap != null) {
            if (object2bytelinkedopenhashmap.size() == CACHE_SIZE) { object2bytelinkedopenhashmap.removeLastByte(); }
            object2bytelinkedopenhashmap.putAndMoveToFirst(block$blockstatepairkey, (byte) (flag ? 1 : 0));
        }

        return flag;
    }

    // Copied from FlowingFluid.getCacheKey
    private static short myGetCacheKey(BlockPos pPos, BlockPos pTargetPos) {
        int i = pTargetPos.getX() - pPos.getX();
        int j = pTargetPos.getZ() - pPos.getZ();
        return (short) ((i + 128 & 255) << 8 | j + 128 & 255);
    }

    public static Consumer<FluidType.Properties> createBuilder(int density, int viscosity) { return builder -> builder.viscosity(viscosity).density(density); }

    public static class Flowing extends ITFluid {
        public Flowing(ITFluids.FluidEntry entry) { super(entry); }

        @Override
        protected void createFluidStateDefinition(@NotNull StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }
    }

    public static class EntityFluidSerializer implements EntityDataSerializer<FluidStack> {
        @Override
        public void write(FriendlyByteBuf buf, @Nonnull FluidStack value) { buf.writeFluidStack(value); }

        @Nonnull
        @Override
        public FluidStack read(FriendlyByteBuf buf) { return buf.readFluidStack(); }

        @Nonnull
        @Override
        public FluidStack copy(FluidStack value) { return value.copy(); }
    }

    public static final DispenseItemBehavior BUCKET_DISPENSE_BEHAVIOR = new DefaultDispenseItemBehavior() {
        private final DefaultDispenseItemBehavior defaultBehavior = new DefaultDispenseItemBehavior();

        public @NotNull ItemStack execute(BlockSource source, ItemStack stack) {
            BucketItem bucketitem = (BucketItem) stack.getItem();
            BlockPos blockpos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
            Level world = source.getLevel();
            if (bucketitem.emptyContents(null, world, blockpos, null)) {
                bucketitem.checkExtraContent(null, world, stack, blockpos);
                FluidState placedState = world.getFluidState(blockpos);
                if (placedState.getType().getFluidType().getDensity() < 0) { world.scheduleTick(blockpos, placedState.getType(), 100); }  // Schedule dissipation after 5 seconds for gaseous sources
                return new ItemStack(Items.BUCKET);
            }
            else { return this.defaultBehavior.dispense(source, stack); }
        }
    };
}