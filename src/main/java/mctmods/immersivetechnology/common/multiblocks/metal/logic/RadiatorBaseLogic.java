package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import mctmods.immersivetechnology.client.util.ClientUtils;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.common.multiblocks.metal.process.RadiatorProcess;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.RadiatorRecipe;
import mctmods.immersivetechnology.core.ServerConfig;
import mctmods.immersivetechnology.core.registration.Sounds;
import mctmods.immersivetechnology.core.util.Climate;
import mctmods.immersivetechnology.core.util.IDisplaySyncState;
import mctmods.immersivetechnology.core.util.ProcessQueue;
import mctmods.immersivetechnology.core.util.RedstoneInput;

import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.StoredCapability;
import com.google.common.collect.ImmutableList;
import com.immersiveconvergence.api.client.MachineSound;
import com.immersiveconvergence.api.integration.DisplayLines;
import com.immersiveconvergence.api.multiblock.IDataReloadAware;
import com.immersiveconvergence.api.multiblock.IFluidOutputPump;
import com.immersiveconvergence.api.util.MarkableFluidTank;
import com.immersiveconvergence.api.util.MultiTankFluidHandler;
import com.immersiveconvergence.api.util.RecipeCache;
import com.immersiveconvergence.api.util.TankPair;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

public abstract class RadiatorBaseLogic implements IMultiblockLogic<RadiatorBaseLogic.State>, IServerTickableComponent<RadiatorBaseLogic.State>, IClientTickableComponent<RadiatorBaseLogic.State>, IFluidOutputPump<RadiatorBaseLogic.State> {
    public static final int INPUT_TANK_CAPACITY = 8 * FluidType.BUCKET_VOLUME;
    public static final int OUTPUT_TANK_CAPACITY = 8 * FluidType.BUCKET_VOLUME;
    private static final int[] REFLECTOR_DEPTHS = {1, 4, 7};
    private static final int[] REFLECTOR_OFFSETS = {1, 5};

    protected abstract String shapeName();
    protected abstract List<BlockPos> inputPois();
    protected abstract RelativeBlockFace inputFacing();
    protected abstract RelativeBlockFace outputFacing();
    protected abstract List<BlockPos> comparatorPositions();
    protected abstract BlockPos soundPoi();
    protected abstract BlockPos redstonePoi();
    protected abstract BlockPos columnPos(int offset, int depth, int sideStep);

    @Override public Direction getOutputDirection(IMultiblockContext<State> ctx) { return ctx.getLevel().toAbsolute(outputFacing()); }

    @Override public List<MarkableFluidTank> getOutputTanks(State state) { return ImmutableList.of(state.tanks.output()); }

    private double getReflectorEfficiency(IMultiblockContext<State> ctx) {
        double reflectorFactor = ServerConfig.radiatorReflectorFactor;
        if (reflectorFactor <= 0.0D) { return 1.0D; }
        double rawEfficiency = 0.0D;
        for (int z : REFLECTOR_DEPTHS) {
            for (int offset : REFLECTOR_OFFSETS) {
                rawEfficiency += checkColumnEfficiency(ctx, offset, z, -1) / 12.0D;
                rawEfficiency += checkColumnEfficiency(ctx, offset, z, 1) / 12.0D;
            }
        }
        return Math.max(1.0D - (1.0D - rawEfficiency) * reflectorFactor, 0.0D);
    }

    private double checkColumnEfficiency(IMultiblockContext<State> ctx, int offset, int z, int sideSign) {
        Level level = ctx.getLevel().getRawLevel();
        for (int i = 1; i <= 24; i++) {
            BlockPos worldPos = ctx.getLevel().toAbsolute(columnPos(offset, z, sideSign * i));
            if (!level.isLoaded(worldPos) || !level.getBlockState(worldPos).isAir()) { return 1.0D / ((25 - i) * (25 - i)); }
        }
        return 1.0D;
    }

    @Override public void tickServer(IMultiblockContext<State> ctx) {
        pumpOutputs(ctx);
        State state = ctx.getState();
        Level level = ctx.getLevel().getRawLevel();
        state.processQueue.restore(level);
        boolean enabled = RedstoneInput.unpowered(ctx, redstonePoi());
        boolean wasActive = state.active;
        boolean progressChanged = false;
        BlockPos masterPos = ctx.getLevel().toAbsolute(BlockPos.ZERO);
        double biomeMult = Climate.coolingMultiplier(level, masterPos, ServerConfig.radiatorBiomeTempFactor, ServerConfig.radiatorBiomeHumidityFactor);
        if (state.radiationEfficiency <= 0.0D || level.getGameTime() % 600L == Math.abs(masterPos.hashCode()) % 600L) { state.radiationEfficiency = getReflectorEfficiency(ctx); }
        double speedMult = biomeMult * state.radiationEfficiency;
        for (int i = state.processQueue.size() - 1; i >= 0; i--) {
            RadiatorProcess process = state.processQueue.get(i);
            process.tick(state.tanks.input(), state.tanks.output(), speedMult);
            if (process.isComplete()) { state.processQueue.remove(i); }
        }
        if (enabled && speedMult > 0.0D && state.processQueue.size() < 2) {
            FluidStack input = state.tanks.input().getFluid();
            RadiatorRecipe recipe = state.recipeGetter.apply(level, input);
            if (recipe != null && input.getAmount() >= recipe.input.getAmount() && state.tanks.output().fill(recipe.fluidOutput, FluidAction.SIMULATE) >= recipe.fluidOutput.getAmount()) { state.processQueue.add(new RadiatorProcess(recipe, input)); }
        }
        state.active = enabled && !state.processQueue.isEmpty();
        if (!state.processQueue.isEmpty()) {
            RadiatorProcess current = state.processQueue.get(0);
            int newProg = current.getTicksProcessed();
            int newTotal = current.getRecipe().totalProcessTime;
            if (newProg != state.processProgress || newTotal != state.totalProcessTime) {
                state.processProgress = newProg;
                state.totalProcessTime = newTotal;
                progressChanged = true;
            }
        }
        else if (state.processProgress > 0 || state.totalProcessTime > 0) {
            state.processProgress = 0;
            state.totalProcessTime = 0;
            progressChanged = true;
        }
        int newQueueSize = state.processQueue.size();
        boolean queueSizeChanged = newQueueSize != state.queueSize;
        if (queueSizeChanged) { state.queueSize = newQueueSize; }
        int newComparatorValue = state.totalProcessTime > 0 ? (15 * state.processProgress) / state.totalProcessTime : 0;
        boolean comparatorChanged = newComparatorValue != state.lastComparatorValue;
        if (comparatorChanged) {
            for (BlockPos pos : comparatorPositions()) { ctx.setComparatorOutputFor(pos, newComparatorValue); }
            state.lastComparatorValue = newComparatorValue;
        }
        if (wasActive != state.active || progressChanged || queueSizeChanged || comparatorChanged) {
            ctx.markMasterDirty();
            ctx.requestMasterBESync();
        }
    }

    @Override public void tickClient(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        if (Minecraft.getInstance().player == null) { return; }
        if (!state.active) {
            state.isSoundPlaying = () -> false;
            return;
        }
        Vec3 soundVec = ctx.getLevel().toAbsolute(Vec3.atCenterOf(soundPoi()));
        if (ClientUtils.attenuated(soundVec, 16, 1) > 0.01f && !state.isSoundPlaying.getAsBoolean()) { state.isSoundPlaying = MachineSound.startSound(() -> state.active, ctx.isValid(), soundVec, Sounds.solarTower, () -> ClientUtils.linearFalloff(soundVec, 16), () -> 1f); }
    }

    @Override public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            BlockPos localPos = position.posInMultiblock();
            RelativeBlockFace side = position.side();
            if (inputPois().contains(localPos) && (side == null || side == inputFacing())) { return ctx.getState().inputCap.cast(ctx); }
            if (getOutputPositions().contains(localPos) && (side == null || side == outputFacing())) { return ctx.getState().outputCap.cast(ctx); }
        }
        return LazyOptional.empty();
    }

    @Override public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return ITShapes.get(shapeName()).getter; }

    public static class State implements IDisplaySyncState, IDataReloadAware {
        public final BiFunction<Level, FluidStack, RadiatorRecipe> recipeGetter = RecipeCache.cached(RadiatorRecipe::findRecipe);
        public final TankPair tanks;
        public final StoredCapability<IFluidHandler> inputCap;
        public final StoredCapability<IFluidHandler> outputCap;
        public boolean active;
        public final ProcessQueue<RadiatorProcess> processQueue = new ProcessQueue<>(RadiatorProcess::toNBT, RadiatorProcess::fromNBT);
        public BooleanSupplier isSoundPlaying = () -> false;
        public int processProgress = 0;
        public int totalProcessTime = 0;
        public int queueSize = 0;
        public int lastComparatorValue = -1;
        public double radiationEfficiency = 0.0D;

        public State(IInitialMultiblockContext<State> ctx) {
            Runnable markDirty = ctx.getMarkDirtyRunnable();
            Runnable sync = ctx.getSyncRunnable();
            Runnable onChanged = () -> { markDirty.run(); sync.run(); };
            this.tanks = new TankPair(v -> onChanged.run(), INPUT_TANK_CAPACITY, OUTPUT_TANK_CAPACITY);
            this.inputCap = new StoredCapability<>(new MultiTankFluidHandler(tanks.input(), false, true, onChanged));
            this.outputCap = new StoredCapability<>(new MultiTankFluidHandler(tanks.output(), true, false, onChanged));
        }

        @Override public void onDataReload(IInitialMultiblockContext<?> context) { lastComparatorValue = -1; }

        @Override public void writeSaveNBT(CompoundTag nbt) {
            nbt.put("tanks", tanks.toNBT());
            nbt.putBoolean("active", active);
            processQueue.write(nbt);
        }

        @Override public void readSaveNBT(CompoundTag nbt) {
            tanks.readNBT(nbt.getCompound("tanks"));
            active = nbt.getBoolean("active");
            processQueue.read(nbt);
        }

        @Override public boolean isActive() { return active; }

        @Override public IFluidTank[] getInternalTanks() { return new IFluidTank[]{tanks.input(), tanks.output()}; }

        @Override public void writeDisplaySyncNBT(CompoundTag nbt) {
            nbt.putBoolean("active", active);
            nbt.put("tanks", tanks.toNBT());
            nbt.putInt("processProgress", processProgress);
            nbt.putInt("totalProcessTime", totalProcessTime);
            nbt.putInt("queueSize", queueSize);
            nbt.putDouble("radiationEfficiency", radiationEfficiency);
        }

        @Override public void readDisplaySyncNBT(CompoundTag nbt) {
            active = nbt.getBoolean("active");
            tanks.readNBT(nbt.getCompound("tanks"));
            processProgress = nbt.getInt("processProgress");
            totalProcessTime = nbt.getInt("totalProcessTime");
            queueSize = nbt.getInt("queueSize");
            radiationEfficiency = nbt.getDouble("radiationEfficiency");
        }

        @Override public void addDisplayLines(Level level, DisplayLines lines) {
            if (active) { lines.percent(totalProcessTime > 0 ? processProgress * 100 / totalProcessTime : 0); }
            lines.text("Active processes: " + queueSize).text("Reflector efficiency").percent((int) Math.round(radiationEfficiency * 100.0D));
        }
    }
}
