package mctmods.immersivetechnology.common.multiblocks.stone.logic;

import mctmods.immersivetechnology.common.fluids.helper.ArrayFluidHandler;
import mctmods.immersivetechnology.common.fluids.helper.MarkableFluidTank;
import mctmods.immersivetechnology.common.multiblocks.helper.IDisplayContext;
import mctmods.immersivetechnology.common.multiblocks.helper.MultiblockPOIHelper;
import mctmods.immersivetechnology.common.multiblocks.helper.IPressurizedFluidOutput;
import mctmods.immersivetechnology.common.multiblocks.stone.process.CoolingTowerProcess;
import mctmods.immersivetechnology.common.multiblocks.stone.recipe.CoolingTowerRecipe;
import mctmods.immersivetechnology.common.multiblocks.stone.shapes.CoolingTowerShape;
import mctmods.immersivetechnology.core.ServerConfig;
import mctmods.immersivetechnology.core.lib.ModSound;
import mctmods.immersivetechnology.core.registration.Particles;
import mctmods.immersivetechnology.core.registration.Sounds;
import mctmods.immersivetechnology.core.util.multiblock.PoIJSONSchema;
import mctmods.immersivetechnology.core.util.CachedRecipe;

import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IMultiblockComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public class CoolingTowerLogic implements IMultiblockLogic<CoolingTowerLogic.State>, IServerTickableComponent<CoolingTowerLogic.State>, IClientTickableComponent<CoolingTowerLogic.State>, IPressurizedFluidOutput<CoolingTowerLogic.State> {
    public static final int INPUT_TANK_CAPACITY = ServerConfig.coolingTowerInputTankCapacity;
    public static final int OUTPUT_TANK_CAPACITY = ServerConfig.coolingTowerOutputTankCapacity;

    private static final List<PoIJSONSchema> RAW_POIS = ImmutableList.copyOf(CoolingTowerShape.DATA.pointsOfInterest);

    public static final List<CapabilityPosition> INPUT_POIS = MultiblockPOIHelper.getCapabilityPositions(RAW_POIS, "fluid_input0");
    public static final List<CapabilityPosition> OUTPUT_POIS = MultiblockPOIHelper.getCapabilityPositions(RAW_POIS, "fluid_output0");
    public static final List<BlockPos> INPUT_FLUID_POIS = INPUT_POIS.stream().map(CapabilityPosition::posInMultiblock).collect(ImmutableList.toImmutableList());
    public static final BlockPos PARTICLE_POI = MultiblockPOIHelper.getPosList(RAW_POIS, "particle0").getFirst();
    public static final BlockPos SOUND_POI = MultiblockPOIHelper.getPosList(RAW_POIS, "sound0").getFirst();

    @Override public List<BlockPos> getOutputPositions() { return OUTPUT_POIS.stream().map(CapabilityPosition::posInMultiblock).collect(ImmutableList.toImmutableList()); }

    @Override public Direction getOutputDirection(IMultiblockContext<State> ctx) { return null; }

    @Override public List<RelativeBlockFace> getOutputFacings() { return OUTPUT_POIS.stream().map(CapabilityPosition::side).collect(ImmutableList.toImmutableList()); }

    @Override public List<MarkableFluidTank> getOutputTanks(State state) { return List.of(state.tanks.output0(), state.tanks.output1(), state.tanks.output2(), state.tanks.output0(), state.tanks.output1(), state.tanks.output2()); }

    private double getBiomeSpeedMultiplier(IMultiblockContext<State> ctx) {
        if (ServerConfig.coolingTowerBiomeTempFactor <= 0.0D) return 1.0D;
        Level level = ctx.getLevel().getRawLevel();
        if (level.dimension() == Level.NETHER) return 0.0D;
        BlockPos worldPos = ctx.getLevel().toAbsolute(BlockPos.ZERO);
        Biome biome = level.getBiome(worldPos).value();
        double temp = biome.getBaseTemperature();
        double deviation = temp - 0.8D;
        return 1.0D + (deviation * ServerConfig.coolingTowerBiomeTempFactor);
    }

    @Override public void tickClient(IMultiblockContext<CoolingTowerLogic.State> ctx) {
        CoolingTowerLogic.State state = ctx.getState();
        if (state.active) { state.soundCooldown = 40; } else if (state.soundCooldown > 0) { state.soundCooldown--; }
        spawnParticles(ctx, state, ctx.getLevel().getRawLevel());
        handleSounds(ctx, state);
    }

    private void spawnParticles(IMultiblockContext<CoolingTowerLogic.State> ctx, CoolingTowerLogic.State state, Level level) {
        if (!state.active) { return; }
        RandomSource rand = RandomSource.create();
        int particleSetting = Minecraft.getInstance().options.particles().get().ordinal();
        if (particleSetting == 2 || particleSetting == 1 && rand.nextInt(3) == 0) { return; }
        LocalPlayer player = Minecraft.getInstance().player;
        Vec3 particleVec = ctx.getLevel().toAbsolute(new Vec3(PARTICLE_POI.getX() + 0.5, PARTICLE_POI.getY() + 0.5, PARTICLE_POI.getZ() + 0.5));
        if (player != null && particleVec.distanceToSqr(player.position()) > 64 * 64) { return; }
        for (int i = 0; i < 3; i++) {
            double px = particleVec.x + (rand.nextFloat() * 4f - 2f);
            double py = particleVec.y + rand.nextFloat() * 2f;
            double pz = particleVec.z + (rand.nextFloat() * 4f - 2f);
            level.addParticle(Particles.SMOKE_CUSTOM.get(), px, py, pz, (rand.nextFloat() - 0.5) * 0.02, 0.01 + rand.nextFloat() * 0.02, (rand.nextFloat() - 0.5) * 0.02);
        }
    }

    private void handleSounds(IMultiblockContext<CoolingTowerLogic.State> ctx, CoolingTowerLogic.State state) {
        if (state.isSoundPlaying.getAsBoolean()) { return; }
        Vec3 soundVec = ctx.getLevel().toAbsolute(new Vec3(SOUND_POI.getX() + 0.5, SOUND_POI.getY() + 0.5, SOUND_POI.getZ() + 0.5));
        state.isSoundPlaying = ModSound.startSound(() -> state.soundCooldown > 0, ctx.isValid(), soundVec, Sounds.coolingTower, () -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) { return 0f; }
            return (float) Math.max(1 - Math.sqrt(player.distanceToSqr(soundVec)) / 16, 0);
        }, () -> 1f);
    }

    @Override public void tickServer(IMultiblockContext<CoolingTowerLogic.State> ctx) {
        pumpOutputs(ctx);
        CoolingTowerLogic.State state = ctx.getState();
        IMultiblockLevel mlevel = ctx.getLevel();
        Level level = mlevel.getRawLevel();
        boolean wasActive = state.active;
        boolean prevTanksDirty = state.tanksDirty;

        double biomeMult = getBiomeSpeedMultiplier(ctx);

        for (int i = state.processQueue.size() - 1; i >= 0; i--) {
            CoolingTowerProcess process = state.processQueue.get(i);
            process.tick(state, biomeMult);
            if (process.isComplete()) { state.processQueue.remove(i); }
        }
        if (state.processQueue.size() < getProcessQueueMaxLength()) {
            FluidStack in0 = state.tanks.input0().getFluid();
            FluidStack in1 = state.tanks.input1().getFluid();
            CoolingTowerRecipe recipe = state.recipeGetter.apply(level, in0, in1);
            boolean swapped = false;
            if (recipe == null) {
                recipe = state.recipeGetterSwapped.apply(level, in1, in0);
                swapped = true;
            }
            if (recipe != null) {
                FluidStack firstIn = swapped ? in1 : in0;
                FluidStack secondIn = swapped ? in0 : in1;
                int req0 = recipe.getInput0Amount();
                int req1 = recipe.getInput1Amount();
                if (firstIn.getAmount() >= req0 && secondIn.getAmount() >= req1) {
                    boolean canOutput = true;
                    FluidStack out0 = recipe.fluidOutput0();
                    FluidStack out1 = recipe.fluidOutput1();
                    FluidStack out2 = recipe.fluidOutput2();
                    if (!out0.isEmpty()) { canOutput &= state.tanks.output0().fill(out0, FluidAction.SIMULATE) >= out0.getAmount(); }
                    if (!out1.isEmpty()) { canOutput &= state.tanks.output1().fill(out1, FluidAction.SIMULATE) >= out1.getAmount(); }
                    if (!out2.isEmpty()) { canOutput &= state.tanks.output2().fill(out2, FluidAction.SIMULATE) >= out2.getAmount(); }
                    if (canOutput) {
                        CoolingTowerRecipe useRecipe = swapped
                                ? new CoolingTowerRecipe(out0, out1, out2, recipe.inputTag1(), req1, recipe.inputTag0(), req0, recipe.getTotalProcessTime())
                                : recipe;
                        state.processQueue.add(new CoolingTowerProcess(useRecipe));
                    }
                }
            }
        }
        state.active = !state.processQueue.isEmpty();
        boolean activeChanged = wasActive != state.active;
        boolean progressChanged = false;
        if (!state.processQueue.isEmpty()) {
            CoolingTowerProcess current = state.processQueue.getFirst();
            int newProg = current.getTicksProcessed();
            int newTotal = current.getRecipe().getTotalProcessTime();
            if (newProg != state.processProgress || newTotal != state.totalProcessTime) {
                state.processProgress = newProg;
                state.totalProcessTime = newTotal;
                progressChanged = true;
            }
        } else if (state.processProgress > 0 || state.totalProcessTime > 0) {
            state.processProgress = 0;
            state.totalProcessTime = 0;
            progressChanged = true;
        }
        boolean tanksChanged = prevTanksDirty != state.tanksDirty;
        boolean update = activeChanged || progressChanged || tanksChanged;
        if (update) { ctx.markMasterDirty(); ctx.requestMasterBESync(); }
    }

    private int getProcessQueueMaxLength() { return 3; }

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

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return CoolingTowerShape.GETTER; }

    private record CombinedInputFluidHandler(MarkableFluidTank tankA, MarkableFluidTank tankB, Runnable onChange) implements IFluidHandler {
        @Override public int getTanks() { return 2; }

        @Override @NotNull public FluidStack getFluidInTank(int tank) { return tank == 0 ? tankA.getFluid() : tankB.getFluid(); }

        @Override public int getTankCapacity(int tank) { return tank == 0 ? tankA.getCapacity() : tankB.getCapacity(); }

        @Override public boolean isFluidValid(int tank, @NotNull FluidStack stack) { return tank == 0 ? tankA.isFluidValid(stack) : tankB.isFluidValid(stack); }

        @Override public int fill(@NotNull FluidStack resource, @NotNull FluidAction action) {
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

        @Override @NotNull public FluidStack drain(@NotNull FluidStack resource, @NotNull FluidAction action) { return FluidStack.EMPTY; }

        @Override @NotNull public FluidStack drain(int maxDrain, @NotNull FluidAction action) { return FluidStack.EMPTY; }
    }

    public static class State implements IMultiblockState, IDisplayContext {
        public final CachedRecipe.TriFunction<Level, FluidStack, FluidStack, CoolingTowerRecipe> recipeGetter = CachedRecipe.cached3(CoolingTowerRecipe::findRecipe);
        public final CachedRecipe.TriFunction<Level, FluidStack, FluidStack, CoolingTowerRecipe> recipeGetterSwapped = CachedRecipe.cached3(CoolingTowerRecipe::findRecipe);
        public final CoolingTowerTanks tanks;
        public IFluidHandler inputCap;
        public IFluidHandler output0Cap;
        public IFluidHandler output1Cap;
        public IFluidHandler output2Cap;
        public boolean active;
        public int soundCooldown = 0;
        public List<CoolingTowerProcess> processQueue = new ArrayList<>();
        public BooleanSupplier isSoundPlaying = () -> false;
        public int processProgress = 0;
        public int totalProcessTime = 0;
        public boolean tanksDirty = false;

        public State(IInitialMultiblockContext<State> ctx) {
            Runnable markDirty = ctx.getMarkDirtyRunnable();
            Runnable sync = ctx.getSyncRunnable();
            Runnable onChanged = () -> { markDirty.run(); sync.run(); this.tanksDirty = true; };

            this.tanks = new CoolingTowerTanks(v -> { onChanged.run(); this.tanksDirty = true; });
            this.inputCap = new CombinedInputFluidHandler(tanks.input0(), tanks.input1(), () -> { onChanged.run(); this.tanksDirty = true; });
            this.output0Cap = new ArrayFluidHandler(tanks.output0(), true, false, () -> { onChanged.run(); this.tanksDirty = true; });
            this.output1Cap = new ArrayFluidHandler(tanks.output1(), true, false, () -> { onChanged.run(); this.tanksDirty = true; });
            this.output2Cap = new ArrayFluidHandler(tanks.output2(), true, false, () -> { onChanged.run(); this.tanksDirty = true; });
        }

        @Override public void writeSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            nbt.put("tanks", tanks.toNBT(provider));
            nbt.putBoolean("active", active);
        }

        @Override public void readSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            tanks.readNBT(nbt.getCompound("tanks"), provider);
            active = nbt.getBoolean("active");
            tanksDirty = false;
        }

        @Override public void writeSyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            CompoundTag display = new CompoundTag();
            writeDisplaySyncNBT(display, provider);
            nbt.put("display", display);
        }

        @Override public void readSyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            if (nbt.contains("display", Tag.TAG_COMPOUND)) { readDisplaySyncNBT(nbt.getCompound("display"), provider); }
        }

        @Override public boolean isActive() { return active; }

        @Override public IFluidTank[] getInternalTanks() { return new IFluidTank[]{tanks.input0(), tanks.input1(), tanks.output0(), tanks.output1(), tanks.output2()}; }

        @Override public void writeDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            nbt.putBoolean("active", active);
            nbt.put("tanks", tanks.toNBT(provider));
            nbt.putInt("processProgress", processProgress);
            nbt.putInt("totalProcessTime", totalProcessTime);
        }

        @Override public void readDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            active = nbt.getBoolean("active");
            tanks.readNBT(nbt.getCompound("tanks"), provider);
            processProgress = nbt.getInt("processProgress");
            totalProcessTime = nbt.getInt("totalProcessTime");
            tanksDirty = false;
        }
    }

    public record CoolingTowerTanks(MarkableFluidTank input0, MarkableFluidTank input1, MarkableFluidTank output0, MarkableFluidTank output1, MarkableFluidTank output2) {

        public CoolingTowerTanks(Consumer<Void> markDirty) {
            this(new MarkableFluidTank(INPUT_TANK_CAPACITY, markDirty), new MarkableFluidTank(INPUT_TANK_CAPACITY, markDirty), new MarkableFluidTank(OUTPUT_TANK_CAPACITY, markDirty), new MarkableFluidTank(OUTPUT_TANK_CAPACITY, markDirty), new MarkableFluidTank(OUTPUT_TANK_CAPACITY, markDirty));
        }

        public static CoolingTowerTanks makeClient() { return new CoolingTowerTanks(v -> {}); }

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
}
