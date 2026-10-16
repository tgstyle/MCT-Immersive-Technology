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

import com.immersiveconvergence.api.multiblock.blocks.component.IClientTickableComponent;
import com.immersiveconvergence.api.multiblock.blocks.component.IMultiblockComponent;
import com.immersiveconvergence.api.multiblock.blocks.component.IServerTickableComponent;
import com.immersiveconvergence.api.multiblock.blocks.env.IInitialMultiblockContext;
import com.immersiveconvergence.api.multiblock.blocks.env.IMultiblockContext;
import com.immersiveconvergence.api.multiblock.blocks.env.IMultiblockLevel;
import com.immersiveconvergence.api.multiblock.blocks.logic.IMultiblockLogic;
import com.immersiveconvergence.api.multiblock.blocks.util.*;
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
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nonnull;

public class CoolingTowerLogic implements IMultiblockLogic<CoolingTowerLogic.State>, IServerTickableComponent<CoolingTowerLogic.State>, IClientTickableComponent<CoolingTowerLogic.State>, IFluidOutputPump<CoolingTowerLogic.State> {
    public static List<CapabilityPosition> INPUT_POIS;
    public static List<CapabilityPosition> OUTPUT_POIS;
    public static List<BlockPos> INPUT_FLUID_POIS;
    public static BlockPos PARTICLE_POI;
    public static BlockPos SOUND_POI;
    public static BlockPos COMPARATOR_POI;

    static { ITShapes.readPois("cooling_tower", CoolingTowerLogic::loadPois); }

    public static int inputTankCapacity() { return ServerConfig.coolingTowerInputTankCapacity; }

    public static int outputTankCapacity() { return ServerConfig.coolingTowerOutputTankCapacity; }

    @Override public List<BlockPos> getOutputPositions() { return OUTPUT_POIS.stream().map(CapabilityPosition::posInMultiblock).collect(ImmutableList.toImmutableList()); }

    @Override public Direction getOutputDirection(IMultiblockContext<State> ctx) { return null; }

    @Override public List<RelativeBlockFace> getOutputFacings() { return OUTPUT_POIS.stream().map(CapabilityPosition::side).collect(ImmutableList.toImmutableList()); }

    @Override public List<MarkableFluidTank> getOutputTanks(State state) { return List.of(state.tanks.output0(), state.tanks.output1(), state.tanks.output2(), state.tanks.output0(), state.tanks.output1(), state.tanks.output2()); }

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
            FluidStack in0 = state.tanks.input0().getFluid();
            FluidStack in1 = state.tanks.input1().getFluid();
            CoolingTowerRecipe recipe = CoolingTowerRecipe.findOriented(level, in0, in1, state.recipeGetter, state.recipeGetterSwapped);
            if (recipe != null && in0.getAmount() >= recipe.getInput0Amount() && in1.getAmount() >= recipe.getInput1Amount()) {
                boolean canOutput = true;
                FluidStack out0 = recipe.fluidOutput0();
                FluidStack out1 = recipe.fluidOutput1();
                FluidStack out2 = recipe.fluidOutput2();
                if (!out0.isEmpty()) { canOutput &= state.tanks.output0().fill(out0, FluidAction.SIMULATE) >= out0.getAmount(); }
                if (!out1.isEmpty()) { canOutput &= state.tanks.output1().fill(out1, FluidAction.SIMULATE) >= out1.getAmount(); }
                if (!out2.isEmpty()) { canOutput &= state.tanks.output2().fill(out2, FluidAction.SIMULATE) >= out2.getAmount(); }
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
                int total = process.getRecipe().getTotalProcessTime();
                newPercent = total > 0 ? process.getTicksProcessed() * 100 / total : 0;
            }
            if (newPercent != state.processPercents[i]) { state.processPercents[i] = newPercent; percentsChanged = true; }
        }
        boolean tanksChanged = prevTanksDirty != state.tanksDirty;
        int newComparatorValue = (15 * state.processQueue.size()) / getProcessQueueMaxLength();
        boolean comparatorChanged = ctx.updateComparatorOutputs(state.lastComparatorValue, newComparatorValue, List.of(COMPARATOR_POI));
        state.lastComparatorValue = newComparatorValue;
        if (activeChanged || percentsChanged || tanksChanged || comparatorChanged) { ctx.markDirtyAndSync(); }
    }

    private static int getProcessQueueMaxLength() { return 3; }

    @Override public void registerCapabilities(IMultiblockComponent.CapabilityRegistrar<State> register) {
        register.register(Capabilities.FluidHandler.BLOCK, (state, position) -> {
            BlockPos localPos = position.posInMultiblock();
            RelativeBlockFace side = position.side();
            if (INPUT_POIS.stream().anyMatch(p -> p.posInMultiblock().equals(localPos) && p.side() == side)) { return state.inputCap; }
            for (int i = 0; i < OUTPUT_POIS.size(); i++) {
                CapabilityPosition p = OUTPUT_POIS.get(i);
                if (p.posInMultiblock().equals(localPos) && p.side() == side) {
                    return switch (i % 3) {
                        case 0 -> state.output0Cap;
                        case 1 -> state.output1Cap;
                        case 2 -> state.output2Cap;
                        default -> null;
                    };
                }
            }
            return null;
        });
    }

    @Override public CoolingTowerLogic.State createInitialState(IInitialMultiblockContext<CoolingTowerLogic.State> ctx) { return new State(ctx); }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return ITShapes.get("cooling_tower").getter; }

    private record CombinedInputFluidHandler(MarkableFluidTank tankA, MarkableFluidTank tankB, Runnable onChange) implements IFluidHandler {
        @Override public int getTanks() { return 2; }

        @Override @Nonnull public FluidStack getFluidInTank(int tank) { return tank == 0 ? tankA.getFluid() : tankB.getFluid(); }

        @Override public int getTankCapacity(int tank) { return tank == 0 ? tankA.getCapacity() : tankB.getCapacity(); }

        @Override public boolean isFluidValid(int tank, @Nonnull FluidStack stack) { return tank == 0 ? tankA.isFluidValid(stack) : tankB.isFluidValid(stack); }

        @Override public int fill(@Nonnull FluidStack resource, @Nonnull FluidAction action) {
            if (resource.isEmpty()) { return 0; }
            MarkableFluidTank target;
            if (!tankA.isEmpty() && tankA.getFluid().getFluid() == resource.getFluid()) { target = tankA; }
            else if (!tankB.isEmpty() && tankB.getFluid().getFluid() == resource.getFluid()) { target = tankB; }
            else if (tankA.isEmpty()) { target = tankA; }
            else if (tankB.isEmpty()) { target = tankB; }
            else { return 0; }
            int filled = target.fill(resource, action);
            if (filled > 0 && action == FluidAction.EXECUTE) { onChange.run(); }
            return filled;
        }

        @Override @Nonnull public FluidStack drain(@Nonnull FluidStack resource, @Nonnull FluidAction action) { return FluidStack.EMPTY; }

        @Override @Nonnull public FluidStack drain(int maxDrain, @Nonnull FluidAction action) { return FluidStack.EMPTY; }
    }

    public static class State implements IDisplaySyncState, IDataReloadAware {
        public final RecipeCache.TriFunction<Level, FluidStack, FluidStack, CoolingTowerRecipe> recipeGetter = RecipeCache.cached3(CoolingTowerRecipe::findRecipe);
        public final RecipeCache.TriFunction<Level, FluidStack, FluidStack, CoolingTowerRecipe> recipeGetterSwapped = RecipeCache.cached3(CoolingTowerRecipe::findRecipe);
        public final CoolingTowerTanks tanks;
        public IFluidHandler inputCap;
        public IFluidHandler output0Cap;
        public IFluidHandler output1Cap;
        public IFluidHandler output2Cap;
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
            Runnable onChanged = () -> { markDirty.run(); sync.run(); this.tanksDirty = true; };
            this.tanks = new CoolingTowerTanks(v -> onChanged.run());
            this.inputCap = new CombinedInputFluidHandler(tanks.input0(), tanks.input1(), onChanged);
            this.output0Cap = new MultiTankFluidHandler(tanks.output0(), true, false, onChanged);
            this.output1Cap = new MultiTankFluidHandler(tanks.output1(), true, false, onChanged);
            this.output2Cap = new MultiTankFluidHandler(tanks.output2(), true, false, onChanged);
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
            tanksDirty = false;
        }

        @Override public boolean isActive() { return active; }

        @Override public IFluidTank[] getInternalTanks() { return new IFluidTank[]{tanks.input0(), tanks.input1(), tanks.output0(), tanks.output1(), tanks.output2()}; }

        @Override public void writeDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            nbt.putBoolean("active", active);
            nbt.put("tanks", tanks.toNBT(provider));
            nbt.putIntArray("processPercents", processPercents);
        }

        @Override public void readDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            active = nbt.getBoolean("active");
            tanks.readNBT(nbt.getCompound("tanks"), provider);
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

        public CompoundTag toNBT(HolderLookup.Provider provider) {
            CompoundTag tag = new CompoundTag();
            tag.put("input0", input0.writeToNBT(provider, new CompoundTag()));
            tag.put("input1", input1.writeToNBT(provider, new CompoundTag()));
            tag.put("output0", output0.writeToNBT(provider, new CompoundTag()));
            tag.put("output1", output1.writeToNBT(provider, new CompoundTag()));
            tag.put("output2", output2.writeToNBT(provider, new CompoundTag()));
            return tag;
        }

        public void readNBT(CompoundTag tag, HolderLookup.Provider provider) {
            input0.readFromNBT(provider, tag.getCompound("input0"));
            input1.readFromNBT(provider, tag.getCompound("input1"));
            output0.readFromNBT(provider, tag.getCompound("output0"));
            output1.readFromNBT(provider, tag.getCompound("output1"));
            output2.readFromNBT(provider, tag.getCompound("output2"));
        }
    }

    private static void loadPois(List<PoIJSONSchema> pois) {
        INPUT_POIS = MultiblockPOIHelper.getCapabilityPositions(pois, "fluid_input0");
        OUTPUT_POIS = MultiblockPOIHelper.getCapabilityPositions(pois, "fluid_output0");
        INPUT_FLUID_POIS = INPUT_POIS.stream().map(CapabilityPosition::posInMultiblock).collect(ImmutableList.toImmutableList());
        PARTICLE_POI = MultiblockPOIHelper.getPosList(pois, "particle0").getFirst();
        SOUND_POI = MultiblockPOIHelper.getPosList(pois, "sound0").getFirst();
        COMPARATOR_POI = MultiblockPOIHelper.getPosList(pois, "master").getFirst();
    }
}
