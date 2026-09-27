package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import mctmods.immersivetechnology.client.util.ClientUtils;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.GasTurbineRecipe;
import mctmods.immersivetechnology.core.ServerConfig;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.Sounds;
import mctmods.immersivetechnology.core.util.IDisplaySyncState;
import mctmods.immersivetechnology.core.util.MechanicalLoad;
import mctmods.immersivetechnology.core.util.TurbineProvider;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.StoredCapability;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import com.google.common.collect.ImmutableList;
import com.immersiveconvergence.api.capability.IMechanicalEnergyProvider;
import com.immersiveconvergence.api.capability.MechanicalCapabilities;
import com.immersiveconvergence.api.capability.RotationInertiaProcess;
import com.immersiveconvergence.api.client.MachineSound;
import com.immersiveconvergence.api.integration.DisplayLines;
import com.immersiveconvergence.api.multiblock.IDataReloadAware;
import com.immersiveconvergence.api.multiblock.IFluidOutputPump;
import com.immersiveconvergence.api.multiblock.MultiblockPOIHelper;
import com.immersiveconvergence.api.multiblock.PoIJSONSchema;
import com.immersiveconvergence.api.util.MarkableFluidTank;
import com.immersiveconvergence.api.util.MultiTankFluidHandler;
import com.immersiveconvergence.api.util.RecipeCache;
import com.immersiveconvergence.api.util.TankPair;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;

public class GasTurbineLogic implements IMultiblockLogic<GasTurbineLogic.State>, IServerTickableComponent<GasTurbineLogic.State>, IClientTickableComponent<GasTurbineLogic.State>, IFluidOutputPump<GasTurbineLogic.State> {
    public static BlockPos REDSTONE_POI;
    public static List<BlockPos> COMPARATOR_POSITIONS;
    public static BlockPos SMOKE_POI0;
    public static BlockPos SMOKE_POI1;
    public static BlockPos RUNNING_SOUND_POI;
    public static BlockPos STARTER_SOUND_POI;
    public static BlockPos ARC_SOUND_POI;
    public static BlockPos SPARK_SOUND_POI;
    public static BlockPos IGNITE_SOUND_POI;
    public static List<BlockPos> INPUT_FLUID_POIS;
    public static List<BlockPos> OUTPUT_FLUID_POIS;
    public static List<BlockPos> ENERGY_INPUT_HV_POIS;
    public static List<BlockPos> ENERGY_INPUT_MV_POIS;
    public static List<BlockPos> MECHANICAL_OUTPUT_POIS;
    private static RelativeBlockFace INPUT_FLUID_FACING;
    private static RelativeBlockFace OUTPUT_FACING;
    private static RelativeBlockFace ENERGY_INPUT_HV_FACING;
    private static RelativeBlockFace ENERGY_INPUT_MV_FACING;
    private static RelativeBlockFace MECHANICAL_OUTPUT_FACING;

    static { ITShapes.readPois("gas_turbine", GasTurbineLogic::loadPois); }

    private static int starterConsumption() { return ServerConfig.gasTurbineStarterConsumption; }

    private static int sparkplugConsumption() { return ServerConfig.gasTurbineSparkplugConsumption; }

    private static double baseMass() { return ServerConfig.gasTurbineBaseMass; }

    private static double driveTorque() { return ServerConfig.gasTurbineDriveTorque; }

    private static double friction() { return ServerConfig.gasTurbineFriction; }

    private static int maxSpeed() { return (int) (MechanicalCapabilities.maxRpm() * ServerConfig.gasTurbineMaxSpeedFactor); }

    @Override public List<BlockPos> getOutputPositions() { return OUTPUT_FLUID_POIS; }

    @Override public Direction getOutputDirection(IMultiblockContext<State> ctx) { return ctx.getLevel().toAbsolute(OUTPUT_FACING); }

    @Override public List<MarkableFluidTank> getOutputTanks(State state) { return ImmutableList.of(state.tanks.output()); }

    @Override public boolean isOutputConnected(IMultiblockContext<State> ctx, int index) {
        Direction side = ctx.getLevel().toAbsolute(OUTPUT_FACING);
        if (side == null) { return false; }
        BlockEntity adjacent = ctx.getLevel().getRawLevel().getBlockEntity(ctx.getLevel().toAbsolute(OUTPUT_FLUID_POIS.get(index)).relative(side));
        return adjacent != null && adjacent.getCapability(ForgeCapabilities.FLUID_HANDLER, side.getOpposite()).isPresent();
    }

    @Override public void tickClient(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        Level level = ctx.getLevel().getRawLevel();
        if (Minecraft.getInstance().player == null) { return; }
        float targetLevel = Reference.remapRange(0, state.effectiveMaxSpeed, 0.2f, 1.0f, state.speed);
        if (state.currentLevel == 0f) { state.currentLevel = targetLevel; }
        else { state.currentLevel = state.currentLevel * 0.9f + targetLevel * 0.1f; }
        float smoothedLevel = state.currentLevel;
        float targetPitch = Reference.remapRange(0, state.effectiveMaxSpeed, 0.5f, 1.5f, state.speed);
        if (state.currentPitch == 0f) { state.currentPitch = targetPitch; }
        if (state.currentPitch < 0.5f) { state.currentPitch = 0.5f; }
        else { state.currentPitch = state.currentPitch * 0.95f + targetPitch * 0.05f; }
        float currentBase = (state.speed / (float) state.effectiveMaxSpeed) * 72f;
        float step = currentBase;
        if (state.animation_fanFadeIn > 0) {
            step -= (state.animation_fanFadeIn / 80f) * currentBase;
            state.animation_fanFadeIn--;
        }
        state.animation_fanRotationStep = step;
        state.animation_fanRotation += step;
        state.animation_fanRotation %= 360;
        Vec3 runningPos = ctx.getLevel().toAbsolute(Vec3.atCenterOf(RUNNING_SOUND_POI));
        Vec3 starterPos = ctx.getLevel().toAbsolute(Vec3.atCenterOf(STARTER_SOUND_POI));
        Vec3 arcPos = ctx.getLevel().toAbsolute(Vec3.atCenterOf(ARC_SOUND_POI));
        Vec3 sparkPos = ctx.getLevel().toAbsolute(Vec3.atCenterOf(SPARK_SOUND_POI));
        Vec3 ignitePos = ctx.getLevel().toAbsolute(Vec3.atCenterOf(IGNITE_SOUND_POI));
        if (runningAudible(state) && ClientUtils.attenuated(runningPos, 32, 8 * (smoothedLevel - 0.2f)) > 0.01f && !state.runningSoundPlaying.getAsBoolean()) {
            int thisId = ++state.runningSoundId;
            state.runningSoundPlaying = MachineSound.startSound(() -> runningAudible(state) && state.runningSoundId == thisId, ctx.isValid(), runningPos, Sounds.gasRunning, () -> ClientUtils.attenuated(runningPos, 32, 8 * (smoothedLevel - 0.2f)), () -> state.currentPitch);
        }
        if (state.starterRunning) {
            if (starterVolume(starterPos, smoothedLevel) > 0.01f && !state.starterSoundPlaying.getAsBoolean()) {
                int thisId = ++state.starterSoundId;
                state.starterSoundPlaying = MachineSound.startSound(() -> state.starterRunning && state.starterSoundId == thisId, ctx.isValid(), starterPos, Sounds.gasStarter, () -> starterVolume(starterPos, smoothedLevel), () -> 1f);
            }
            if (state.speed >= state.effectiveMaxSpeed / 4 && state.hasIgniter && starterVolume(arcPos, smoothedLevel) > 0.01f && !state.arcSoundPlaying.getAsBoolean()) {
                int thisId = ++state.arcSoundId;
                state.arcSoundPlaying = MachineSound.startSound(() -> state.starterRunning && state.speed >= state.effectiveMaxSpeed / 4 && state.hasIgniter && state.arcSoundId == thisId, ctx.isValid(), arcPos, Sounds.gasArc, () -> starterVolume(arcPos, smoothedLevel), () -> 1f);
            }
        }
        if (state.ignited && !state.lastIgnited && state.speed < state.effectiveMaxSpeed / 2) {
            state.lastIgnited = true;
            playLocal(level, sparkPos, Sounds.gasSpark);
            state.igniteDelay = 3;
        }
        else { state.lastIgnited = state.ignited; }
        if (state.igniteDelay > 0) {
            state.igniteDelay--;
            if (state.igniteDelay == 0 && state.starterRunning) { playLocal(level, ignitePos, Sounds.gasIgnite); }
        }
        if (state.starterRunning && state.speed >= state.effectiveMaxSpeed / 4 && level.random.nextInt(40) != 0) {
            Vec3 particlePos = ctx.getLevel().toAbsolute(new Vec3(SMOKE_POI0.getX() + 0.5, SMOKE_POI0.getY() - 0.5, SMOKE_POI0.getZ() + 0.5));
            if (ClientUtils.particlesVisible(particlePos)) {
                double px = particlePos.x + 2 - level.random.nextFloat() * 3;
                double py = particlePos.y + 0.5;
                double pz = particlePos.z + 2 - level.random.nextFloat() * 3;
                level.addParticle(ParticleTypes.SMOKE, px, py, pz, 0, 0.02, 0);
            }
        }
        if (state.active && ctx.getLevel().shouldTickModulo(2) && !isOutputConnected(ctx, 0)) {
            float normSpeed = Math.max(0f, Reference.remapRange(100, state.effectiveMaxSpeed, 0f, 1f, state.speed));
            ClientUtils.exhaust(level, ctx.getLevel().toAbsolute(SMOKE_POI1), ctx.getLevel().getOrientation().front(), normSpeed, state.tanks.output().getFluid());
        }
    }

    private static boolean runningAudible(State state) { return state.speed > 0 && ((state.everIgnited && !state.starterRunning) || (state.stall && state.ignited)); }

    private static float starterVolume(Vec3 pos, float smoothedLevel) { return Math.min(ClientUtils.attenuated(pos, 64, smoothedLevel), 0.4f); }

    private static void playLocal(Level level, Vec3 pos, Supplier<SoundEvent> sound) { level.playLocalSound(pos.x, pos.y, pos.z, sound.get(), SoundSource.BLOCKS, ClientUtils.attenuated(pos, 64, 1), 1, false); }

    @Override public void tickServer(IMultiblockContext<State> ctx) {
        pumpOutputs(ctx);
        State state = ctx.getState();
        state.hasIgniter = state.mvInput.isPresent();
        state.canIgniteClient = canIgnite(state);
        boolean wasActive = state.active;
        boolean wasStall = state.stall;
        state.active = false;
        Level level = ctx.getLevel().getRawLevel();
        Direction outputFacing = ctx.getLevel().getOrientation().front();
        MechanicalLoad load = MechanicalLoad.at(level, ctx.getLevel().toAbsolute(MECHANICAL_OUTPUT_POIS.get(0)).relative(outputFacing), outputFacing.getOpposite());
        boolean hasConsumer = load.present();
        int effectiveMax = load.limit(maxSpeed());
        state.effectiveMaxSpeed = effectiveMax;
        if (load.mass() != state.connectedMass || load.friction() != state.connectedFriction) {
            state.connectedMass = load.mass();
            state.connectedFriction = load.friction();
            state.inertia = new RotationInertiaProcess(baseMass() + state.connectedMass, driveTorque(), friction() + state.connectedFriction, effectiveMax);
        }
        boolean isRSEnabled = state.rsState.isEnabled(ctx);
        state.ignited = state.ignitionGracePeriod > 0;
        state.starterRunning = false;
        if (isRSEnabled && hasConsumer && starterConsumption() <= state.energyStorageHV.getEnergyStored()) {
            state.starterRunning = true;
            state.energyStorageHV.extractEnergy(starterConsumption(), false);
        }
        if (state.speed <= 0) {
            state.speed = 0;
            state.isShutdown = false;
            state.stall = false;
            state.everIgnited = false;
        }
        if (!isRSEnabled || !hasConsumer) {
            state.isShutdown = true;
            state.ignitionGracePeriod = 0;
            state.burnRemaining = 0;
            state.stall = false;
        }
        if (state.speed < effectiveMax / 4) {
            if (!state.isShutdown && state.ignitionGracePeriod > 0) { state.ignitionGracePeriod--; }
            if (!state.isShutdown && state.starterRunning) { speedUp(state, effectiveMax); }
            else { slowDown(state); }
        }
        else if (state.isShutdown) { slowDown(state); }
        else if (state.starterRunning) {
            if (state.hasIgniter && canIgnite(state)) {
                state.stall = true;
                if (!wasStall) { ignite(state, ctx); }
                else { state.ignitionGracePeriod = 60; }
                state.speed = effectiveMax / 4;
                state.active = true;
                if (state.ignitionGracePeriod > 0) { state.ignitionGracePeriod--; }
            }
            else {
                state.stall = false;
                slowDown(state);
            }
        }
        else {
            state.stall = false;
            if (!state.ignited && !canIgnite(state)) { slowDown(state); }
            else if (state.burnRemaining > 0) {
                state.burnRemaining--;
                burn(state, ctx, effectiveMax);
            }
            else if (consumeFuel(state, level)) {
                burn(state, ctx, effectiveMax);
                ctx.markMasterDirty();
            }
            else { slowDown(state); }
        }
        int newComparatorValue = state.effectiveMaxSpeed > 0 ? (15 * state.speed) / state.effectiveMaxSpeed : 0;
        boolean comparatorChanged = newComparatorValue != state.lastComparatorValue;
        if (comparatorChanged) {
            for (BlockPos pos : COMPARATOR_POSITIONS) { ctx.setComparatorOutputFor(pos, newComparatorValue); }
            state.lastComparatorValue = newComparatorValue;
        }
        if (wasActive != state.active || wasStall != state.stall || state.speed % 20 == 0 || comparatorChanged) {
            ctx.markMasterDirty();
            ctx.requestMasterBESync();
        }
    }

    private static boolean consumeFuel(State state, Level level) {
        FluidStack fluid = state.tanks.input().getFluid();
        GasTurbineRecipe recipe = state.recipeGetter.apply(level, fluid);
        if (recipe == null || fluid.getAmount() < recipe.input.getAmount()) { return false; }
        state.tanks.input().drain(recipe.input.getAmount(), FluidAction.EXECUTE);
        state.currentTorque = recipe.torque;
        if (recipe.fluidOutput != null) { state.tanks.output().fill(recipe.fluidOutput, FluidAction.EXECUTE); }
        state.burnRemaining = recipe.getTotalProcessTime() - 1;
        return true;
    }

    private static void burn(State state, IMultiblockContext<State> ctx, int effectiveMax) {
        if (!state.ignited) { ignite(state, ctx); }
        speedUp(state, effectiveMax);
    }

    private static void speedUp(State state, int effectiveMax) {
        state.speed = Math.min(effectiveMax, state.speed + state.inertia.getSpeedUpRate());
        state.active = true;
    }

    private static void slowDown(State state) { state.speed = Math.max(0, state.speed - state.inertia.getSpeedDownRate()); }

    private static boolean canIgnite(State state) { return sparkplugConsumption() <= state.energyStorageMV.getEnergyStored(); }

    private static void ignite(State state, IMultiblockContext<State> ctx) {
        state.energyStorageMV.extractEnergy(sparkplugConsumption(), false);
        state.everIgnited = true;
        state.ignited = true;
        state.ignitionGracePeriod = 60;
        ctx.requestMasterBESync();
    }

    @Override public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) {
        State state = ctx.getState();
        BlockPos localPos = position.posInMultiblock();
        RelativeBlockFace side = position.side();
        if (cap == ForgeCapabilities.ENERGY) {
            if (ENERGY_INPUT_HV_POIS.contains(localPos) && (side == null || side == ENERGY_INPUT_HV_FACING)) { return state.energyCapHV.cast(ctx); }
            if (ENERGY_INPUT_MV_POIS.contains(localPos) && (side == null || side == ENERGY_INPUT_MV_FACING)) { return state.energyCapMV.cast(ctx); }
        }
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            if (INPUT_FLUID_POIS.contains(localPos) && (side == null || side == INPUT_FLUID_FACING)) { return state.fluidCap.cast(ctx); }
            if (OUTPUT_FLUID_POIS.contains(localPos) && (side == null || side == OUTPUT_FACING)) { return state.fluidCapExhaust.cast(ctx); }
        }
        if (cap == MechanicalCapabilities.MECHANICAL_PROVIDER_CAPABILITY && MECHANICAL_OUTPUT_POIS.contains(localPos) && (side == null || side == MECHANICAL_OUTPUT_FACING)) { return LazyOptional.of(() -> state.mechanicalProvider).cast(); }
        return LazyOptional.empty();
    }

    @Override public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return ITShapes.get("gas_turbine").getter; }

    public static class State implements IDisplaySyncState, IDataReloadAware {
        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        public final TankPair tanks;
        public final StoredCapability<IFluidHandler> fluidCap;
        public final StoredCapability<IFluidHandler> fluidCapExhaust;
        public final AveragingEnergyStorage energyStorageHV = new AveragingEnergyStorage(ServerConfig.gasTurbineEnergyCapacityHV);
        public final AveragingEnergyStorage energyStorageMV = new AveragingEnergyStorage(ServerConfig.gasTurbineEnergyCapacityMV);
        public final StoredCapability<IEnergyStorage> energyCapHV = new StoredCapability<>(energyStorageHV);
        public final StoredCapability<IEnergyStorage> energyCapMV = new StoredCapability<>(energyStorageMV);
        public CapabilityReference<IEnergyStorage> mvInput;
        private final BiFunction<Level, FluidStack, GasTurbineRecipe> recipeGetter = RecipeCache.cached(GasTurbineRecipe::findRecipe);
        public int speed = 0;
        public float currentTorque = 1.0f;
        public boolean active = false;
        public boolean starterRunning = false;
        public boolean ignited = false;
        public boolean stall = false;
        public boolean everIgnited = false;
        public boolean hasIgniter = false;
        public boolean canIgniteClient = false;
        public int burnRemaining = 0;
        public int ignitionGracePeriod = 0;
        public boolean isShutdown = false;
        public int effectiveMaxSpeed = maxSpeed();
        public float animation_fanRotationStep = 0;
        public float animation_fanRotation = 0;
        private transient int animation_fanFadeIn = 0;
        private transient float currentLevel = 0f;
        private transient float currentPitch = 0f;
        private BooleanSupplier runningSoundPlaying = () -> false;
        private BooleanSupplier starterSoundPlaying = () -> false;
        private BooleanSupplier arcSoundPlaying = () -> false;
        private int runningSoundId = 0;
        private int starterSoundId = 0;
        private int arcSoundId = 0;
        private boolean lastIgnited = false;
        private int igniteDelay = 0;
        private double connectedMass = 0;
        private double connectedFriction = 0;
        private RotationInertiaProcess inertia = new RotationInertiaProcess(baseMass(), driveTorque(), friction(), maxSpeed());
        public int lastComparatorValue = -1;
        private final IMechanicalEnergyProvider mechanicalProvider = new TurbineProvider(() -> speed, () -> currentTorque, GasTurbineLogic::maxSpeed, GasTurbineLogic::baseMass, GasTurbineLogic::driveTorque, GasTurbineLogic::friction);

        public State(IInitialMultiblockContext<State> ctx) {
            Runnable markDirty = ctx.getMarkDirtyRunnable();
            Runnable sync = ctx.getSyncRunnable();
            Runnable onChanged = () -> {
                markDirty.run();
                sync.run();
            };
            this.tanks = new TankPair(v -> onChanged.run(), ServerConfig.gasTurbineInputTankCapacity, ServerConfig.gasTurbineOutputTankCapacity);
            this.fluidCap = new StoredCapability<>(new MultiTankFluidHandler(tanks.input(), false, true, onChanged));
            this.fluidCapExhaust = new StoredCapability<>(new MultiTankFluidHandler(tanks.output(), true, false, onChanged));
            bindOutputs(ctx);
        }

        @Override public void onDataReload(IInitialMultiblockContext<?> context) {
            lastComparatorValue = -1;
            bindOutputs(context);
        }

        private void bindOutputs(IInitialMultiblockContext<?> context) { this.mvInput = context.getCapabilityAt(ForgeCapabilities.ENERGY, MultiblockPOIHelper.opposing(ENERGY_INPUT_MV_FACING, ENERGY_INPUT_MV_POIS.get(0))); }

        @Override public void writeSaveNBT(CompoundTag nbt) {
            rsState.writeSaveNBT(nbt);
            nbt.putInt("speed", speed);
            nbt.putBoolean("active", active);
            nbt.putBoolean("starterRunning", starterRunning);
            nbt.putBoolean("ignited", ignited);
            nbt.putBoolean("stall", stall);
            nbt.putBoolean("everIgnited", everIgnited);
            nbt.putInt("burnRemaining", burnRemaining);
            nbt.putInt("ignitionGracePeriod", ignitionGracePeriod);
            nbt.putBoolean("isShutdown", isShutdown);
            nbt.putInt("effectiveMaxSpeed", effectiveMaxSpeed);
            nbt.put("tanks", tanks.toNBT());
        }

        @Override public void readSaveNBT(CompoundTag nbt) {
            rsState.readSaveNBT(nbt);
            speed = nbt.getInt("speed");
            active = nbt.getBoolean("active");
            starterRunning = nbt.getBoolean("starterRunning");
            ignited = nbt.getBoolean("ignited");
            stall = nbt.getBoolean("stall");
            everIgnited = nbt.getBoolean("everIgnited");
            burnRemaining = nbt.getInt("burnRemaining");
            ignitionGracePeriod = nbt.getInt("ignitionGracePeriod");
            isShutdown = nbt.getBoolean("isShutdown");
            effectiveMaxSpeed = nbt.getInt("effectiveMaxSpeed");
            tanks.readNBT(nbt.getCompound("tanks"));
        }

        @Override public boolean isActive() { return active; }

        @Override public AveragingEnergyStorage getEnergy() { return energyStorageHV; }

        @Override public List<AveragingEnergyStorage> getEnergies() { return List.of(energyStorageHV, energyStorageMV); }

        @Override public IFluidTank[] getInternalTanks() { return new IFluidTank[]{tanks.input(), tanks.output()}; }

        @Override public void writeDisplaySyncNBT(CompoundTag nbt) {
            nbt.putBoolean("active", active);
            nbt.putBoolean("starterRunning", starterRunning);
            nbt.putBoolean("ignited", ignited);
            nbt.putInt("speed", speed);
            nbt.putBoolean("isShutdown", isShutdown);
            nbt.putBoolean("stall", stall);
            nbt.putBoolean("everIgnited", everIgnited);
            nbt.putBoolean("hasIgniter", hasIgniter);
            nbt.putBoolean("canIgnite", canIgniteClient);
            nbt.putInt("effectiveMaxSpeed", effectiveMaxSpeed);
            nbt.put("tanks", tanks.toNBT());
        }

        @Override public void readDisplaySyncNBT(CompoundTag nbt) {
            boolean oldActive = active;
            active = nbt.getBoolean("active");
            starterRunning = nbt.getBoolean("starterRunning");
            ignited = nbt.getBoolean("ignited");
            speed = nbt.getInt("speed");
            isShutdown = nbt.getBoolean("isShutdown");
            stall = nbt.getBoolean("stall");
            everIgnited = nbt.getBoolean("everIgnited");
            hasIgniter = nbt.getBoolean("hasIgniter");
            canIgniteClient = nbt.getBoolean("canIgnite");
            effectiveMaxSpeed = nbt.getInt("effectiveMaxSpeed");
            tanks.readNBT(nbt.getCompound("tanks"));
            if (active && !oldActive && speed < effectiveMaxSpeed / 4) { animation_fanFadeIn = 80; }
        }

        @Override public void addDisplayLines(Level level, DisplayLines lines) {
            lines.rpm(speed, effectiveMaxSpeed);
            if (tanks.input().getFluid().isEmpty()) { lines.fuelEmpty(); }
        }
    }

    private static void loadPois(List<PoIJSONSchema> pois) {
        REDSTONE_POI = MultiblockPOIHelper.getPosList(pois, "redstone0").get(0);
        COMPARATOR_POSITIONS = MultiblockPOIHelper.getPosList(pois, "comparator0");
        SMOKE_POI0 = MultiblockPOIHelper.getPosList(pois, "smoke0").get(0);
        SMOKE_POI1 = MultiblockPOIHelper.getPosList(pois, "smoke1").get(0);
        RUNNING_SOUND_POI = MultiblockPOIHelper.getPosList(pois, "sound_running0").get(0);
        STARTER_SOUND_POI = MultiblockPOIHelper.getPosList(pois, "sound_starter0").get(0);
        ARC_SOUND_POI = MultiblockPOIHelper.getPosList(pois, "sound_arc0").get(0);
        SPARK_SOUND_POI = MultiblockPOIHelper.getPosList(pois, "sound_spark0").get(0);
        IGNITE_SOUND_POI = MultiblockPOIHelper.getPosList(pois, "sound_ignite0").get(0);
        INPUT_FLUID_POIS = MultiblockPOIHelper.getPosList(pois, "fluid_input0");
        OUTPUT_FLUID_POIS = MultiblockPOIHelper.getPosList(pois, "fluid_output0");
        ENERGY_INPUT_HV_POIS = MultiblockPOIHelper.getPosList(pois, "energy_input_hv0");
        ENERGY_INPUT_MV_POIS = MultiblockPOIHelper.getPosList(pois, "energy_input_mv0");
        MECHANICAL_OUTPUT_POIS = MultiblockPOIHelper.getPosList(pois, "mechanical_output0");
        INPUT_FLUID_FACING = MultiblockPOIHelper.getFacing(pois, "fluid_input0");
        OUTPUT_FACING = MultiblockPOIHelper.getFacing(pois, "fluid_output0");
        ENERGY_INPUT_HV_FACING = MultiblockPOIHelper.getFacing(pois, "energy_input_hv0");
        ENERGY_INPUT_MV_FACING = MultiblockPOIHelper.getFacing(pois, "energy_input_mv0");
        MECHANICAL_OUTPUT_FACING = MultiblockPOIHelper.getFacing(pois, "mechanical_output0");
    }
}
