package mctmods.immersivetechnology.common.multiblocks.stone.logic;

import mctmods.immersivetechnology.client.util.ClientUtils;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.common.multiblocks.stone.process.CoolingTowerProcess;
import mctmods.immersivetechnology.common.multiblocks.stone.recipe.CoolingTowerRecipe;
import mctmods.immersivetechnology.core.ServerConfig;
import mctmods.immersivetechnology.core.registration.Particles;
import mctmods.immersivetechnology.core.registration.Sounds;
import mctmods.immersivetechnology.core.util.Climate;
import mctmods.immersivetechnology.core.util.IDisplaySyncState;
import mctmods.immersivetechnology.core.util.ProcessQueue;

import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import com.google.common.collect.ImmutableList;
import com.immersiveconvergence.api.client.MachineSound;
import com.immersiveconvergence.api.integration.DisplayLines;
import com.immersiveconvergence.api.multiblock.IDataReloadAware;
import com.immersiveconvergence.api.multiblock.IFluidOutputPump;
import com.immersiveconvergence.api.multiblock.MultiblockPOIHelper;
import com.immersiveconvergence.api.multiblock.PoIJSONSchema;
import com.immersiveconvergence.api.util.MarkableFluidTank;
import com.immersiveconvergence.api.util.MultiTankFluidHandler;
import com.immersiveconvergence.api.util.RecipeCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public class CoolingTowerLogic implements IMultiblockLogic<CoolingTowerLogic.State>, IServerTickableComponent<CoolingTowerLogic.State>, IClientTickableComponent<CoolingTowerLogic.State>, IFluidOutputPump<CoolingTowerLogic.State> {
    public static List<BlockPos> INPUT_FLUID_POIS;
    public static List<BlockPos> OUTPUT_FLUID_POIS;
    public static BlockPos PARTICLE_POI;
    public static BlockPos SOUND_POI;
    public static BlockPos COMPARATOR_POI;
    private static RelativeBlockFace INPUT_FACING;
    private static RelativeBlockFace OUTPUT_FACING;

    static { ITShapes.readPois("cooling_tower", CoolingTowerLogic::loadPois); }

    public static int inputTankCapacity() { return ServerConfig.coolingTowerInputTankCapacity; }

    public static int outputTankCapacity() { return ServerConfig.coolingTowerOutputTankCapacity; }

    @Override public List<BlockPos> getOutputPositions() { return OUTPUT_FLUID_POIS; }

    @Override public Direction getOutputDirection(IMultiblockContext<State> ctx) { return ctx.getLevel().toAbsolute(OUTPUT_FACING); }

    @Override public List<MarkableFluidTank> getOutputTanks(State state) { return ImmutableList.of(state.tanks.output0, state.tanks.output1, state.tanks.output2); }

    @Override public void tickClient(IMultiblockContext<CoolingTowerLogic.State> ctx) {
        CoolingTowerLogic.State state = ctx.getState();
        if (state.active) { state.soundCooldown = 40; }
        else if (state.soundCooldown > 0) { state.soundCooldown--; }
        spawnParticles(ctx, state, ctx.getLevel().getRawLevel());
        handleSounds(ctx, state);
    }

    private static void spawnParticles(IMultiblockContext<CoolingTowerLogic.State> ctx, CoolingTowerLogic.State state, Level level) {
        if (!state.active) { return; }
        RandomSource rand = RandomSource.create();
        Vec3 particleVec = ctx.getLevel().toAbsolute(Vec3.atCenterOf(PARTICLE_POI));
        if (!ClientUtils.particlesVisible(particleVec)) { return; }
        for (int i = 0; i < 3; i++) {
            double px = particleVec.x + (rand.nextFloat() * 4f - 2f);
            double py = particleVec.y + rand.nextFloat() * 2f;
            double pz = particleVec.z + (rand.nextFloat() * 4f - 2f);
            level.addParticle(Particles.SMOKE_CUSTOM.get(), px, py, pz, (rand.nextFloat() - 0.5) * 0.02, 0.01 + rand.nextFloat() * 0.02, (rand.nextFloat() - 0.5) * 0.02);
        }
    }

    private static void handleSounds(IMultiblockContext<CoolingTowerLogic.State> ctx, CoolingTowerLogic.State state) {
        if (state.isSoundPlaying.getAsBoolean()) { return; }
        Vec3 soundVec = ctx.getLevel().toAbsolute(Vec3.atCenterOf(SOUND_POI));
        state.isSoundPlaying = MachineSound.startSound(() -> state.soundCooldown > 0, ctx.isValid(), soundVec, Sounds.coolingTower, () -> ClientUtils.linearFalloff(soundVec, 16), () -> 1f);
    }

    @Override public void tickServer(IMultiblockContext<CoolingTowerLogic.State> ctx) {
        pumpOutputs(ctx);
        CoolingTowerLogic.State state = ctx.getState();
        IMultiblockLevel mlevel = ctx.getLevel();
        Level level = mlevel.getRawLevel();
        boolean wasActive = state.active;
        boolean prevTanksDirty = state.tanksDirty;
        state.processQueue.restore(level);
        double biomeMult = Climate.coolingMultiplier(level, mlevel.toAbsolute(BlockPos.ZERO), ServerConfig.coolingTowerBiomeTempFactor, ServerConfig.coolingTowerBiomeHumidityFactor);
        for (int i = state.processQueue.size() - 1; i >= 0; i--) {
            CoolingTowerProcess process = state.processQueue.get(i);
            process.tick(state, biomeMult);
            if (process.isComplete()) { state.processQueue.remove(i); }
        }
        if (biomeMult > 0.0D && state.processQueue.size() < getProcessQueueMaxLength()) {
            FluidStack in0 = state.tanks.input0.getFluid();
            FluidStack in1 = state.tanks.input1.getFluid();
            CoolingTowerRecipe recipe = CoolingTowerRecipe.findOriented(level, in0, in1, state.recipeGetter, state.recipeGetterSwapped);
            if (recipe != null && in0.getAmount() >= recipe.input0.getAmount() && in1.getAmount() >= recipe.input1.getAmount()) {
                boolean canOutput = true;
                if (!recipe.fluidOutput0.isEmpty()) { canOutput &= state.tanks.output0.fill(recipe.fluidOutput0, FluidAction.SIMULATE) >= recipe.fluidOutput0.getAmount(); }
                if (!recipe.fluidOutput1.isEmpty()) { canOutput &= state.tanks.output1.fill(recipe.fluidOutput1, FluidAction.SIMULATE) >= recipe.fluidOutput1.getAmount(); }
                if (!recipe.fluidOutput2.isEmpty()) { canOutput &= state.tanks.output2.fill(recipe.fluidOutput2, FluidAction.SIMULATE) >= recipe.fluidOutput2.getAmount(); }
                if (canOutput) { state.processQueue.add(new CoolingTowerProcess(recipe, in0, in1)); }
            }
        }
        state.active = !state.processQueue.isEmpty();
        boolean activeChanged = wasActive != state.active;
        boolean percentsChanged = false;
        for (int i = 0; i < state.processPercents.length; i++) {
            int newPercent = -1;
            if (i < state.processQueue.size()) {
                CoolingTowerProcess process = state.processQueue.get(i);
                int total = process.getRecipe().totalProcessTime;
                newPercent = total > 0 ? process.getTicksProcessed() * 100 / total : 0;
            }
            if (newPercent != state.processPercents[i]) {
                state.processPercents[i] = newPercent;
                percentsChanged = true;
            }
        }
        boolean tanksChanged = prevTanksDirty != state.tanksDirty;
        int newComparatorValue = (15 * state.processQueue.size()) / getProcessQueueMaxLength();
        boolean comparatorChanged = newComparatorValue != state.lastComparatorValue;
        if (comparatorChanged) {
            ctx.setComparatorOutputFor(COMPARATOR_POI, newComparatorValue);
            state.lastComparatorValue = newComparatorValue;
        }
        if (activeChanged || percentsChanged || tanksChanged || comparatorChanged) {
            ctx.markMasterDirty();
            ctx.requestMasterBESync();
        }
    }

    private static int getProcessQueueMaxLength() { return 3; }

    @Override public <T> LazyOptional<T> getCapability(IMultiblockContext<CoolingTowerLogic.State> ctx, CapabilityPosition position, Capability<T> cap) {
        CoolingTowerLogic.State state = ctx.getState();
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            BlockPos localPos = position.posInMultiblock();
            RelativeBlockFace side = position.side();
            if (INPUT_FLUID_POIS.contains(localPos) && (side == null || side == INPUT_FACING)) {
                int index = INPUT_FLUID_POIS.indexOf(localPos);
                if (index == 0) { return state.input0Cap.cast(ctx); }
                if (index == 1) { return state.input1Cap.cast(ctx); }
            }
            if (OUTPUT_FLUID_POIS.contains(localPos) && (side == null || side == OUTPUT_FACING)) {
                int index = OUTPUT_FLUID_POIS.indexOf(localPos);
                if (index == 0) { return state.output0Cap.cast(ctx); }
                if (index == 1) { return state.output1Cap.cast(ctx); }
                if (index == 2) { return state.output2Cap.cast(ctx); }
            }
        }
        return LazyOptional.empty();
    }

    @Override public CoolingTowerLogic.State createInitialState(IInitialMultiblockContext<CoolingTowerLogic.State> ctx) { return new State(ctx); }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return ITShapes.get("cooling_tower").getter; }

    public static class State implements IDisplaySyncState, IDataReloadAware {
        public final RecipeCache.TriFunction<Level, FluidStack, FluidStack, CoolingTowerRecipe> recipeGetter = RecipeCache.cached3(CoolingTowerRecipe::findRecipe);
        public final RecipeCache.TriFunction<Level, FluidStack, FluidStack, CoolingTowerRecipe> recipeGetterSwapped = RecipeCache.cached3(CoolingTowerRecipe::findRecipe);
        public final CoolingTowerTanks tanks;
        public final StoredCapability<IFluidHandler> input0Cap;
        public final StoredCapability<IFluidHandler> input1Cap;
        public final StoredCapability<IFluidHandler> output0Cap;
        public final StoredCapability<IFluidHandler> output1Cap;
        public final StoredCapability<IFluidHandler> output2Cap;
        public boolean active;
        public int soundCooldown = 0;
        public final ProcessQueue<CoolingTowerProcess> processQueue = new ProcessQueue<>(CoolingTowerProcess::toNBT, CoolingTowerProcess::fromNBT);
        public BooleanSupplier isSoundPlaying = () -> false;
        public int[] processPercents = new int[]{-1, -1, -1};
        public int lastComparatorValue = -1;
        public boolean tanksDirty = false;

        public State(IInitialMultiblockContext<State> ctx) {
            Runnable markDirty = ctx.getMarkDirtyRunnable();
            Runnable sync = ctx.getSyncRunnable();
            Consumer<Void> onChanged = v -> { markDirty.run(); sync.run(); this.tanksDirty = true; };
            this.tanks = new CoolingTowerTanks(onChanged);
            this.input0Cap = new StoredCapability<>(MultiTankFluidHandler.fillOnly(tanks.input0, () -> onChanged.accept(null)));
            this.input1Cap = new StoredCapability<>(MultiTankFluidHandler.fillOnly(tanks.input1, () -> onChanged.accept(null)));
            this.output0Cap = new StoredCapability<>(MultiTankFluidHandler.drainOnly(tanks.output0, () -> onChanged.accept(null)));
            this.output1Cap = new StoredCapability<>(MultiTankFluidHandler.drainOnly(tanks.output1, () -> onChanged.accept(null)));
            this.output2Cap = new StoredCapability<>(MultiTankFluidHandler.drainOnly(tanks.output2, () -> onChanged.accept(null)));
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
            tanksDirty = false;
        }

        @Override public boolean isActive() { return active; }

        @Override public IFluidTank[] getInternalTanks() { return new IFluidTank[]{tanks.input0, tanks.input1, tanks.output0, tanks.output1, tanks.output2}; }

        @Override public void writeDisplaySyncNBT(CompoundTag nbt) {
            nbt.putBoolean("active", active);
            nbt.put("tanks", tanks.toNBT());
            nbt.putIntArray("processPercents", processPercents);
        }

        @Override public void readDisplaySyncNBT(CompoundTag nbt) {
            active = nbt.getBoolean("active");
            tanks.readNBT(nbt.getCompound("tanks"));
            int[] percents = nbt.getIntArray("processPercents");
            processPercents = percents.length == 3 ? percents : new int[]{-1, -1, -1};
            tanksDirty = false;
        }

        @Override public void addDisplayLines(Level level, DisplayLines lines) {
            for (int percent : processPercents) {
                if (percent >= 0) { lines.percent(percent); }
            }
        }
    }

    public record CoolingTowerTanks(MarkableFluidTank input0, MarkableFluidTank input1, MarkableFluidTank output0, MarkableFluidTank output1, MarkableFluidTank output2) {
        public CoolingTowerTanks(Consumer<Void> markDirty) { this(new MarkableFluidTank(inputTankCapacity(), markDirty), new MarkableFluidTank(inputTankCapacity(), markDirty), new MarkableFluidTank(outputTankCapacity(), markDirty), new MarkableFluidTank(outputTankCapacity(), markDirty), new MarkableFluidTank(outputTankCapacity(), markDirty)); }

        public CompoundTag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.put("input0", input0.writeToNBT(new CompoundTag()));
            tag.put("input1", input1.writeToNBT(new CompoundTag()));
            tag.put("output0", output0.writeToNBT(new CompoundTag()));
            tag.put("output1", output1.writeToNBT(new CompoundTag()));
            tag.put("output2", output2.writeToNBT(new CompoundTag()));
            return tag;
        }

        public void readNBT(CompoundTag tag) {
            input0.readFromNBT(tag.getCompound("input0"));
            input1.readFromNBT(tag.getCompound("input1"));
            output0.readFromNBT(tag.getCompound("output0"));
            output1.readFromNBT(tag.getCompound("output1"));
            output2.readFromNBT(tag.getCompound("output2"));
        }
    }

    private static void loadPois(List<PoIJSONSchema> pois) {
        INPUT_FLUID_POIS = MultiblockPOIHelper.getPosList(pois, "fluid_input0");
        OUTPUT_FLUID_POIS = MultiblockPOIHelper.getPosList(pois, "fluid_output0");
        PARTICLE_POI = MultiblockPOIHelper.getPosList(pois, "particle0").get(0);
        SOUND_POI = MultiblockPOIHelper.getPosList(pois, "sound0").get(0);
        COMPARATOR_POI = MultiblockPOIHelper.getPosList(pois, "master").get(0);
        INPUT_FACING = MultiblockPOIHelper.getFacing(pois, "fluid_input0");
        OUTPUT_FACING = MultiblockPOIHelper.getFacing(pois, "fluid_output0");
    }
}
