package mctmods.immersivetechnology.common.multiblocks.stone.logic;

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
import mctmods.immersivetechnology.common.fluids.helper.ITArrayFluidHandler;
import mctmods.immersivetechnology.common.fluids.helper.ITMarkableFluidTank;
import mctmods.immersivetechnology.common.multiblocks.helper.ITIDisplayContext;
import mctmods.immersivetechnology.common.multiblocks.helper.ITMultiblockPOIHelper;
import mctmods.immersivetechnology.common.multiblocks.helper.ITIPressurizedFluidOutput;
import mctmods.immersivetechnology.common.multiblocks.stone.process.CoolingTowerProcess;
import mctmods.immersivetechnology.common.multiblocks.stone.recipe.CoolingTowerRecipe;
import mctmods.immersivetechnology.common.multiblocks.stone.shapes.CoolingTowerShape;
import mctmods.immersivetechnology.core.ITServerConfig;
import mctmods.immersivetechnology.core.lib.ITSound;
import mctmods.immersivetechnology.core.registration.ITParticles;
import mctmods.immersivetechnology.core.registration.ITSounds;
import mctmods.immersivetechnology.core.util.multiblock.PoIJSONSchema;
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public class CoolingTowerLogic implements IMultiblockLogic<CoolingTowerLogic.State>, IServerTickableComponent<CoolingTowerLogic.State>, IClientTickableComponent<CoolingTowerLogic.State>, ITIPressurizedFluidOutput<CoolingTowerLogic.State> {
    public static final int INPUT_TANK_CAPACITY = ITServerConfig.coolingTowerInputTankCapacity;
    public static final int OUTPUT_TANK_CAPACITY = ITServerConfig.coolingTowerOutputTankCapacity;

    private static final List<PoIJSONSchema> RAW_POIS = ImmutableList.copyOf(CoolingTowerShape.DATA.pointsOfInterest);

    public static final List<BlockPos> INPUT_FLUID_POIS = ITMultiblockPOIHelper.getPosList(RAW_POIS, "fluid_input0");
    public static final List<BlockPos> OUTPUT_FLUID_POIS = ITMultiblockPOIHelper.getPosList(RAW_POIS, "fluid_output0");
    public static final BlockPos PARTICLE_POI = ITMultiblockPOIHelper.getPosList(RAW_POIS, "particle0").getFirst();
    public static final BlockPos SOUND_POI = ITMultiblockPOIHelper.getPosList(RAW_POIS, "sound0").getFirst();

    private static final RelativeBlockFace INPUT_FACING = ITMultiblockPOIHelper.getFacing(RAW_POIS, "fluid_input0");
    private static final RelativeBlockFace OUTPUT_FACING = ITMultiblockPOIHelper.getFacing(RAW_POIS, "fluid_output0");

    @Override public List<BlockPos> getOutputPositions() { return OUTPUT_FLUID_POIS; }

    @Override public Direction getOutputDirection(IMultiblockContext<State> ctx) { return ctx.getLevel().toAbsolute(OUTPUT_FACING); }

    @Override public List<ITMarkableFluidTank> getOutputTanks(State state) { return ImmutableList.of(state.tanks.output0(), state.tanks.output1(), state.tanks.output2()); }

    private double getBiomeSpeedMultiplier(IMultiblockContext<State> ctx) {
        if (ITServerConfig.coolingTowerBiomeTempFactor <= 0.0D) return 1.0D;
        Level level = ctx.getLevel().getRawLevel();
        if (level.dimension() == Level.NETHER) return 0.0D;
        BlockPos worldPos = ctx.getLevel().toAbsolute(BlockPos.ZERO);
        Biome biome = level.getBiome(worldPos).value();
        double temp = biome.getBaseTemperature();
        double deviation = temp - 0.8D;
        return 1.0D + (deviation * ITServerConfig.coolingTowerBiomeTempFactor);
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
            level.addParticle(ITParticles.SMOKE_CUSTOM.get(), px, py, pz, (rand.nextFloat() - 0.5) * 0.02, 0.01 + rand.nextFloat() * 0.02, (rand.nextFloat() - 0.5) * 0.02);
        }
    }

    private void handleSounds(IMultiblockContext<CoolingTowerLogic.State> ctx, CoolingTowerLogic.State state) {
        if (state.isSoundPlaying.getAsBoolean()) { return; }
        Vec3 soundVec = ctx.getLevel().toAbsolute(new Vec3(SOUND_POI.getX() + 0.5, SOUND_POI.getY() + 0.5, SOUND_POI.getZ() + 0.5));
        state.isSoundPlaying = ITSound.startSound(() -> state.soundCooldown > 0, ctx.isValid(), soundVec, ITSounds.coolingTower, () -> {
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
            CoolingTowerRecipe recipe = CoolingTowerRecipe.findRecipe(level, in0, in1);
            boolean swapped = false;
            if (recipe == null) {
                recipe = CoolingTowerRecipe.findRecipe(level, in1, in0);
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

    @Override
    public void registerCapabilities(IMultiblockComponent.CapabilityRegistrar<State> register) {
        register.register(Capabilities.FluidHandler.BLOCK, (state, position) -> {
            BlockPos localPos = position.posInMultiblock();
            RelativeBlockFace side = position.side();
            if (INPUT_FLUID_POIS.contains(localPos) && (side == null || side == INPUT_FACING)) {
                int index = INPUT_FLUID_POIS.indexOf(localPos);
                if (index == 0) { return state.input0Cap; }
                if (index == 1) { return state.input1Cap; }
            }
            if (OUTPUT_FLUID_POIS.contains(localPos) && (side == null || side == OUTPUT_FACING)) {
                int index = OUTPUT_FLUID_POIS.indexOf(localPos);
                if (index == 0) { return state.output0Cap; }
                if (index == 1) { return state.output1Cap; }
                if (index == 2) { return state.output2Cap; }
            }
            return null;
        });
    }

    @Override public CoolingTowerLogic.State createInitialState(IInitialMultiblockContext<CoolingTowerLogic.State> ctx) { return new State(ctx); }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return CoolingTowerShape.GETTER; }

    public static class State implements IMultiblockState, ITIDisplayContext {
        public final CoolingTowerTanks tanks;
        public IFluidHandler input0Cap;
        public IFluidHandler input1Cap;
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
            this.input0Cap = new ITArrayFluidHandler(tanks.input0(), false, true, () -> { onChanged.run(); this.tanksDirty = true; });
            this.input1Cap = new ITArrayFluidHandler(tanks.input1(), false, true, () -> { onChanged.run(); this.tanksDirty = true; });
            this.output0Cap = new ITArrayFluidHandler(tanks.output0(), true, false, () -> { onChanged.run(); this.tanksDirty = true; });
            this.output1Cap = new ITArrayFluidHandler(tanks.output1(), true, false, () -> { onChanged.run(); this.tanksDirty = true; });
            this.output2Cap = new ITArrayFluidHandler(tanks.output2(), true, false, () -> { onChanged.run(); this.tanksDirty = true; });
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

    public record CoolingTowerTanks(ITMarkableFluidTank input0, ITMarkableFluidTank input1, ITMarkableFluidTank output0, ITMarkableFluidTank output1, ITMarkableFluidTank output2) {

        public CoolingTowerTanks(Consumer<Void> markDirty) {
            this(new ITMarkableFluidTank(INPUT_TANK_CAPACITY, markDirty), new ITMarkableFluidTank(INPUT_TANK_CAPACITY, markDirty), new ITMarkableFluidTank(OUTPUT_TANK_CAPACITY, markDirty), new ITMarkableFluidTank(OUTPUT_TANK_CAPACITY, markDirty), new ITMarkableFluidTank(OUTPUT_TANK_CAPACITY, markDirty));
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
