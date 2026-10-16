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

import com.immersiveconvergence.api.multiblock.blocks.component.IClientTickableComponent;
import com.immersiveconvergence.api.multiblock.blocks.component.IMultiblockComponent;
import com.immersiveconvergence.api.multiblock.blocks.component.IServerTickableComponent;
import com.immersiveconvergence.api.multiblock.blocks.env.IInitialMultiblockContext;
import com.immersiveconvergence.api.multiblock.blocks.env.IMultiblockContext;
import com.immersiveconvergence.api.multiblock.blocks.logic.IMultiblockLogic;
import com.immersiveconvergence.api.multiblock.blocks.util.RelativeBlockFace;
import com.immersiveconvergence.api.multiblock.blocks.util.ShapeType;
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
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import java.util.Arrays;
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
            if (recipe != null) {
                int req = recipe.getInputAmount();
                FluidStack outF = recipe.fluidOutput();
                boolean canOutput = (outF == null || outF.isEmpty() || state.tanks.output().fill(outF, FluidAction.SIMULATE) >= outF.getAmount());
                if (input.getAmount() >= req && canOutput) { state.processQueue.add(new RadiatorProcess(recipe, input)); }
            }
        }
        state.active = enabled && !state.processQueue.isEmpty();
        if (!state.processQueue.isEmpty()) {
            RadiatorProcess current = state.processQueue.getFirst();
            int newProg = current.getTicksProcessed();
            int newTotal = current.getRecipe().getTotalProcessTime();
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
        int[] newPercents = new int[state.processQueue.size()];
        for (int i = 0; i < newPercents.length; i++) {
            RadiatorProcess process = state.processQueue.get(i);
            int total = process.getRecipe().getTotalProcessTime();
            newPercents[i] = total > 0 ? process.getTicksProcessed() * 100 / total : 0;
        }
        boolean percentsChanged = !Arrays.equals(newPercents, state.processPercents);
        if (percentsChanged) { state.processPercents = newPercents; }
        int newComparatorValue = state.totalProcessTime > 0 ? (15 * state.processProgress) / state.totalProcessTime : 0;
        boolean comparatorChanged = ctx.updateComparatorOutputs(state.lastComparatorValue, newComparatorValue, comparatorPositions());
        state.lastComparatorValue = newComparatorValue;
        if (wasActive != state.active || progressChanged || percentsChanged || comparatorChanged) { ctx.markDirtyAndSync(); }
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

    @Override public void registerCapabilities(IMultiblockComponent.CapabilityRegistrar<State> register) {
        register.register(Capabilities.FluidHandler.BLOCK, (state, position) -> {
            BlockPos localPos = position.posInMultiblock();
            RelativeBlockFace side = position.side();
            if (inputPois().contains(localPos) && (side == null || side == inputFacing())) { return state.inputCap; }
            if (getOutputPositions().contains(localPos) && (side == null || side == outputFacing())) { return state.outputCap; }
            return null;
        });
    }

    @Override public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return ITShapes.get(shapeName()).getter; }

    public static class State implements IDisplaySyncState, IDataReloadAware {
        public final BiFunction<Level, FluidStack, RadiatorRecipe> recipeGetter = RecipeCache.cached(RadiatorRecipe::findRecipe);
        public final TankPair tanks;
        public final IFluidHandler inputCap;
        public final IFluidHandler outputCap;
        public boolean active;
        public final ProcessQueue<RadiatorProcess> processQueue = new ProcessQueue<>(RadiatorProcess::toNBT, RadiatorProcess::fromNBT);
        public BooleanSupplier isSoundPlaying = () -> false;
        public int processProgress = 0;
        public int totalProcessTime = 0;
        public int[] processPercents = new int[0];
        public int lastComparatorValue = -1;
        public double radiationEfficiency = 0.0D;

        public State(IInitialMultiblockContext<State> ctx) {
            Runnable markDirty = ctx.getMarkDirtyRunnable();
            Runnable sync = ctx.getSyncRunnable();
            Runnable onChanged = () -> { markDirty.run(); sync.run(); };
            this.tanks = new TankPair(v -> onChanged.run(), INPUT_TANK_CAPACITY, OUTPUT_TANK_CAPACITY);
            this.inputCap = new MultiTankFluidHandler(tanks.input(), false, true, onChanged);
            this.outputCap = new MultiTankFluidHandler(tanks.output(), true, false, onChanged);
        }

        @Override public void onDataReload(IInitialMultiblockContext<?> context) { lastComparatorValue = -1; }

        @Override public void writeSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            nbt.put("tanks", tanks.toNBT(provider));
            nbt.putBoolean("active", active);
            processQueue.write(nbt, provider);
        }

        @Override public void readSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            tanks.readNBT(nbt.getCompound("tanks"), provider);
            active = nbt.getBoolean("active");
            processQueue.read(nbt);
        }

        @Override public boolean isActive() { return active; }

        @Override public IFluidTank[] getInternalTanks() { return new IFluidTank[]{tanks.input(), tanks.output()}; }

        @Override public void writeDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            nbt.putBoolean("active", active);
            nbt.put("tanks", tanks.toNBT(provider));
            nbt.putInt("processProgress", processProgress);
            nbt.putInt("totalProcessTime", totalProcessTime);
            nbt.putIntArray("processPercents", processPercents);
            nbt.putDouble("radiationEfficiency", radiationEfficiency);
        }

        @Override public void readDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            active = nbt.getBoolean("active");
            tanks.readNBT(nbt.getCompound("tanks"), provider);
            processProgress = nbt.getInt("processProgress");
            totalProcessTime = nbt.getInt("totalProcessTime");
            processPercents = nbt.getIntArray("processPercents");
            radiationEfficiency = nbt.getDouble("radiationEfficiency");
        }

        @Override public void addDisplayLines(Level level, DisplayLines lines) {
            for (int percent : processPercents) { lines.percent(percent); }
            lines.text("Reflector efficiency").percent((int) Math.round(radiationEfficiency * 100.0D));
        }
    }
}
