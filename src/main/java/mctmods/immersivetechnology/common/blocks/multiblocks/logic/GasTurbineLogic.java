package mctmods.immersivetechnology.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.StoredCapability;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.common.util.CachedRecipe;
import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.api.MechanicalCapabilities;
import mctmods.immersivetechnology.api.capability.IMechanicalEnergyConsumer;
import mctmods.immersivetechnology.api.capability.IMechanicalEnergyProvider;
import mctmods.immersivetechnology.client.particles.ColoredSmoke;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.GasTurbineRecipe;
import mctmods.immersivetechnology.common.blocks.multiblocks.shapes.GasTurbineShape;
import mctmods.immersivetechnology.common.blocks.multiblocks.process.RotationInertiaProcess;
import mctmods.immersivetechnology.common.fluids.helper.ITArrayFluidHandler;
import mctmods.immersivetechnology.common.fluids.helper.ITMarkableFluidTank;
import mctmods.immersivetechnology.common.util.multiblock.MultiblockData;
import mctmods.immersivetechnology.common.util.multiblock.PoIJSONSchema;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.lib.ITSound;
import mctmods.immersivetechnology.core.registration.ITSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public class GasTurbineLogic implements IMultiblockLogic<GasTurbineLogic.State>, IServerTickableComponent<GasTurbineLogic.State>, IClientTickableComponent<GasTurbineLogic.State> {
    public static final int TANK_CAPACITY = 12 * FluidType.BUCKET_VOLUME;
    private static final int ENERGY_CAPACITY = 8192;
    private static final int ENERGY_CAPACITY_MV = 2048;
    private static final int ELECTRIC_STARTER_CONSUMPTION = 4096;
    private static final int SPARKPLUG_CONSUMPTION = 1024;
    private static final int MAX_SPEED = 1800;
    private static final double BASE_MASS = 8;
    private static final double DRIVE_TORQUE = 30;
    private static final double FRICTION = 60;

    private static final MultiblockData DATA = GasTurbineShape.DATA;
    private static final int WIDTH = GasTurbineShape.WIDTH;
    private static final int LENGTH = GasTurbineShape.LENGTH;

    private static BlockPos getPosFromIndex(int index) {
        int layerSize = WIDTH * LENGTH;
        int y = index / layerSize;
        int remainder = index % layerSize;
        int x = remainder % WIDTH;
        int z = remainder / WIDTH;
        return new BlockPos(x, y, z);
    }

    private static PoIJSONSchema findPOI(String name) {
        for (PoIJSONSchema poi : DATA.pointsOfInterest) { if (poi.name.equals(name)) { return poi; } }
        throw new RuntimeException("Missing POI: " + name);
    }

    private static CapabilityPosition findCapPos(String name) {
        PoIJSONSchema poi = findPOI(name);
        return new CapabilityPosition(getPosFromIndex(poi.position), poi.relativeFace);
    }

    public static final CapabilityPosition INPUT_FLUID_POI = findCapPos("fluid_input");
    public static final CapabilityPosition OUTPUT_FLUID_POI = findCapPos("fluid_output");
    private static final Set<CapabilityPosition> ENERGY_INPUTS_HV = Set.of(findCapPos("energy_input_hv"));
    private static final Set<CapabilityPosition> ENERGY_INPUTS_MV = Set.of(findCapPos("energy_input_mv"));
    public static final BlockPos REDSTONE_POI = getPosFromIndex(findPOI("redstone").position);
    public static final BlockPos SMOKE_POI1 = getPosFromIndex(findPOI("smoke1").position);
    public static final BlockPos SMOKE_POI2 = getPosFromIndex(findPOI("smoke2").position);
    public static final BlockPos RUNNING_SOUND_POI = getPosFromIndex(findPOI("sound_running").position);
    public static final BlockPos STARTER_SOUND_POI = getPosFromIndex(findPOI("sound_starter").position);
    public static final BlockPos ARC_SOUND_POI = getPosFromIndex(findPOI("sound_arc").position);
    public static final BlockPos SPARK_SOUND_POI = getPosFromIndex(findPOI("sound_spark").position);
    public static final BlockPos IGNITE_SOUND_POI = getPosFromIndex(findPOI("sound_ignite").position);
    public static final BlockPos ROTATIONAL_OUTPUT_POI = getPosFromIndex(findPOI("mech_output").position);

    @Override
    public void tickClient(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        Level level = ctx.getLevel().getRawLevel();
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) { return; }
        float targetLevel = ITLib.remapRange(0, MAX_SPEED, 0.2f, 1.0f, state.speed);
        if (state.currentLevel == 0f) { state.currentLevel = targetLevel; }
        else { state.currentLevel = state.currentLevel * 0.9f + targetLevel * 0.1f; }
        float smoothedLevel = state.currentLevel;
        float targetPitch = ITLib.remapRange(0, MAX_SPEED, 0.5f, 1.5f, state.speed);
        if (state.currentPitch == 0f) { state.currentPitch = targetPitch; }
        if (state.currentPitch < 0.5f) { state.currentPitch = 0.5f; }
        else { state.currentPitch = state.currentPitch * 0.95f + targetPitch * 0.05f; }
        float currentBase = (state.speed / (float) MAX_SPEED) * 72f;
        float step = currentBase;
        if (state.animation_fanFadeIn > 0) {
            step -= (state.animation_fanFadeIn / 80f) * currentBase;
            state.animation_fanFadeIn--;
        }
        state.animation_fanRotationStep = step;
        state.animation_fanRotation += step;
        state.animation_fanRotation %= 360;
        if (state.speed > 0) { state.soundGrace = 40; }
        else if (state.soundGrace > 0) { state.soundGrace--; }
        Vec3 runningPos = ctx.getLevel().toAbsolute(new Vec3(RUNNING_SOUND_POI.getX() + 0.5, RUNNING_SOUND_POI.getY() + 0.5, RUNNING_SOUND_POI.getZ() + 0.5));
        Vec3 starterPos = ctx.getLevel().toAbsolute(new Vec3(STARTER_SOUND_POI.getX() + 0.5, STARTER_SOUND_POI.getY() + 0.5, STARTER_SOUND_POI.getZ() + 0.5));
        Vec3 arcPos = ctx.getLevel().toAbsolute(new Vec3(ARC_SOUND_POI.getX() + 0.5, ARC_SOUND_POI.getY() + 0.5, ARC_SOUND_POI.getZ() + 0.5));
        Vec3 sparkPos = ctx.getLevel().toAbsolute(new Vec3(SPARK_SOUND_POI.getX() + 0.5, SPARK_SOUND_POI.getY() + 0.5, SPARK_SOUND_POI.getZ() + 0.5));
        Vec3 ignitePos = ctx.getLevel().toAbsolute(new Vec3(IGNITE_SOUND_POI.getX() + 0.5, IGNITE_SOUND_POI.getY() + 0.5, IGNITE_SOUND_POI.getZ() + 0.5));
        if (!state.runningSoundPlaying.getAsBoolean()) {
            state.runningSoundId++;
            int thisId = state.runningSoundId;
            state.runningSoundPlaying = ITSound.startSound(
                    () -> (!state.starterRunning || (state.stall && state.ignited)) && state.speed > 0 && state.runningSoundId == thisId,
                    ctx.isValid(),
                    runningPos,
                    ITSounds.gasRunning,
                    () -> {
                        LocalPlayer p = Minecraft.getInstance().player;
                        if (p == null) { return 0f; }
                        float a = (float) Math.max(p.distanceToSqr(runningPos) / 32, 1);
                        return (11 * (smoothedLevel - 0.2f)) / a;
                    },
                    () -> state.currentPitch
            );
        }
        if (state.starterRunning) {
            if (!state.starterSoundPlaying.getAsBoolean()) {
                state.starterSoundId++;
                int thisId = state.starterSoundId;
                state.starterSoundPlaying = ITSound.startSound(
                        () -> state.starterRunning && state.starterSoundId == thisId, ctx.isValid(), starterPos, ITSounds.gasStarter,
                        () -> {
                            LocalPlayer p = Minecraft.getInstance().player;
                            if (p == null) { return 0f; }
                            float a = (float) Math.max(p.distanceToSqr(starterPos) / 64, 1);
                            return Math.min(smoothedLevel / a, 0.4f);
                        },
                        () -> 1f
                );
            }
            if (state.speed >= MAX_SPEED / 4) {
                if (!state.arcSoundPlaying.getAsBoolean()) {
                    state.arcSoundId++;
                    int thisId = state.arcSoundId;
                    state.arcSoundPlaying = ITSound.startSound(
                            () -> state.starterRunning && state.speed >= MAX_SPEED / 4 && state.arcSoundId == thisId, ctx.isValid(), arcPos, ITSounds.gasArc,
                            () -> {
                                LocalPlayer p = Minecraft.getInstance().player;
                                if (p == null) { return 0f; }
                                float a = (float) Math.max(p.distanceToSqr(arcPos) / 64, 1);
                                return Math.min(smoothedLevel / a, 0.4f);
                            },
                            () -> 1f
                    );
                }
            }
        }
        if (state.ignited && !state.lastIgnited && state.speed < MAX_SPEED / 2) {
            state.lastIgnited = true;
            float ignitionAtt = (float) Math.max(player.distanceToSqr(sparkPos) / 64, 1);
            level.playLocalSound(sparkPos.x, sparkPos.y, sparkPos.z, ITSounds.gasSpark.get(), SoundSource.BLOCKS, 1 / ignitionAtt, 1, false);
            state.igniteDelay = 3;
        }
        else { state.lastIgnited = state.ignited; }
        if (state.igniteDelay > 0) {
            state.igniteDelay--;
            if (state.igniteDelay == 0 && state.starterRunning) {
                float ignitionAtt = (float) Math.max(player.distanceToSqr(ignitePos) / 64, 1);
                level.playLocalSound(ignitePos.x, ignitePos.y, ignitePos.z, ITSounds.gasIgnite.get(), SoundSource.BLOCKS, 1 / ignitionAtt, 1, false);
            }
        }
        if (state.starterRunning && state.speed >= MAX_SPEED / 4) {
            if (level.random.nextInt(40) == 0) { return; }
            Vec3 particlePos = ctx.getLevel().toAbsolute(new Vec3(SMOKE_POI1.getX() + 0.5, SMOKE_POI1.getY() - 0.5, SMOKE_POI1.getZ() + 0.5));
            double distSq = player.distanceToSqr(particlePos);
            if (distSq > 64 * 64) { return; }
            double px = particlePos.x + 2 - level.random.nextFloat() * 3;
            double py = particlePos.y + 0.5;
            double pz = particlePos.z + 2 - level.random.nextFloat() * 3;
            level.addParticle(ParticleTypes.SMOKE, px, py, pz, 0, 0.02, 0);
        }
        if (state.active && ctx.getLevel().shouldTickModulo(2)) {
            Direction facing = ctx.getLevel().getOrientation().front();
            BlockPos outputAbs = ctx.getLevel().toAbsolute(SMOKE_POI2);
            boolean connected = state.fluidOutput.isPresent();
            if (!connected) {
                Vec3 smokePos = new Vec3(outputAbs.getX() + 0.5, outputAbs.getY() + 0.5, outputAbs.getZ() + 0.5);
                double velX = -facing.getStepX() * 0.125 + particleXZSpeed();
                double velY = -facing.getStepY() * 0.1 + 0.0625;
                double velZ = -facing.getStepZ() * 0.125 + particleXZSpeed();
                FluidStack outFluid = state.tanks.output.getFluid();
                float r = 0.5F, g = 0.5F, b = 0.5F;
                if (!outFluid.isEmpty()) {
                    int tint = IClientFluidTypeExtensions.of(outFluid.getFluid()).getTintColor(outFluid);
                    r = ((tint >> 16) & 0xFF) / 255f;
                    g = ((tint >> 8) & 0xFF) / 255f;
                    b = (tint & 0xFF) / 255f;
                }
                Level level2 = ctx.getLevel().getRawLevel();
                level2.addAlwaysVisibleParticle(new ColoredSmoke(r, g, b), smokePos.x, smokePos.y, smokePos.z, velX, velY, velZ);
            }
        }
    }

    @Override
    public void tickServer(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        boolean wasActive = state.active;
        boolean wasStall = state.stall;
        state.active = false;
        Level level = ctx.getLevel().getRawLevel();
        Direction outputFacing = ctx.getLevel().getOrientation().front().getOpposite();
        BlockPos outputPortAbs = ctx.getLevel().toAbsolute(ROTATIONAL_OUTPUT_POI);
        BlockPos consumerAbsPos = outputPortAbs.relative(outputFacing);
        BlockEntity entity = level.getBlockEntity(consumerAbsPos);
        boolean hasConsumer = false;
        double additionalMass = 0.0;
        double additionalFriction = 0.0;
        if (entity != null) {
            LazyOptional<IMechanicalEnergyConsumer> consumerCap = entity.getCapability(MechanicalCapabilities.MECHANICAL_CONSUMER_CAPABILITY, outputFacing.getOpposite());
            if (consumerCap.isPresent()) {
                hasConsumer = true;
                IMechanicalEnergyConsumer consumer = consumerCap.orElseThrow(RuntimeException::new);
                additionalMass = consumer.getMass();
                additionalFriction = consumer.getFriction();
            }
        }
        if (additionalMass != state.connectedMass || additionalFriction != state.connectedFriction) {
            state.connectedMass = additionalMass;
            state.connectedFriction = additionalFriction;
            state.inertia = new RotationInertiaProcess(BASE_MASS + state.connectedMass, DRIVE_TORQUE, FRICTION + state.connectedFriction);
        }
        boolean isRSEnabled = state.rsState.isEnabled(ctx);
        state.ignited = state.ignitionGracePeriod > 0;
        state.starterRunning = false;
        if (isRSEnabled && ELECTRIC_STARTER_CONSUMPTION <= state.energyStorageHV.getEnergyStored()) {
            state.starterRunning = true;
            state.energyStorageHV.extractEnergy(ELECTRIC_STARTER_CONSUMPTION, false);
        }
        if (state.speed <= 0) {
            state.speed = 0;
            state.isShutdown = false;
            state.stall = false;
        }
        if (!isRSEnabled || !hasConsumer) {
            state.isShutdown = true;
            state.ignitionGracePeriod = 0;
            state.burnRemaining = 0;
            state.stall = false;
        }
        if (state.speed < MAX_SPEED / 4) {
            if (isRSEnabled && !state.isShutdown) {
                if (state.ignitionGracePeriod > 0) { state.ignitionGracePeriod--; }
                if (state.starterRunning) {
                    state.speed = Math.min(MAX_SPEED, state.speed + state.inertia.getSpeedUpRate());
                    state.active = true;
                }
                else {
                    state.speed = Math.max(0, state.speed - state.inertia.getSpeedDownRate());
                }
            }
            else {
                state.speed = Math.max(0, state.speed - state.inertia.getSpeedDownRate());
            }
        }
        else {
            if (state.isShutdown) {
                state.speed = Math.max(0, state.speed - state.inertia.getSpeedDownRate());
            }
            else {
                if (state.starterRunning) {
                    if (canIgnite(state)) {
                        state.stall = true;
                        if (!wasStall) { ignite(state, ctx); }
                        else { state.ignitionGracePeriod = 60; }
                        state.speed = MAX_SPEED / 4;
                        state.active = true;
                        if (state.ignitionGracePeriod > 0) { state.ignitionGracePeriod--; }
                    }
                    else {
                        state.stall = false;
                        state.speed = Math.max(0, state.speed - state.inertia.getSpeedDownRate());
                    }
                }
                else {
                    state.stall = false;
                    if (state.burnRemaining > 0 && (state.ignited || canIgnite(state))) {
                        state.burnRemaining--;
                        if (!state.ignited) { ignite(state, ctx); }
                        state.speed = Math.min(MAX_SPEED, state.speed + state.inertia.getSpeedUpRate());
                        state.active = true;
                    }
                    else if (state.ignited || canIgnite(state)) {
                        FluidStack fluid = state.tanks.input.getFluid();
                        GasTurbineRecipe recipe = state.recipeGetter.apply(ctx.getLevel().getRawLevel(), fluid);
                        if (recipe != null && fluid.getAmount() >= recipe.input.getAmount()) {
                            state.tanks.input.drain(recipe.input.getAmount(), FluidAction.EXECUTE);
                            if (recipe.fluidOutput != null) {
                                int filled = state.tanks.output.fill(recipe.fluidOutput, FluidAction.EXECUTE);
                                if (filled < recipe.fluidOutput.getAmount()) {}
                            }
                            state.burnRemaining = recipe.getTotalProcessTime() - 1;
                            if (!state.ignited) { ignite(state, ctx); }
                            state.speed = Math.min(MAX_SPEED, state.speed + state.inertia.getSpeedUpRate());
                            state.active = true;
                            ctx.markMasterDirty();
                        }
                        else {
                            state.speed = Math.max(0, state.speed - state.inertia.getSpeedDownRate());
                        }
                    }
                    else {
                        state.speed = Math.max(0, state.speed - state.inertia.getSpeedDownRate());
                    }
                }
            }
        }
        boolean changed = false;
        if (state.tanks.output.getFluidAmount() > 0 && state.fluidOutput.isPresent()) {
            IFluidHandler handler = state.fluidOutput.get();
            FluidStack out = state.tanks.output.getFluid();
            if (out.getAmount() > 0) {
                out = out.copy();
                int accepted = handler.fill(out, FluidAction.SIMULATE);
                if (accepted > 0) {
                    int drained = handler.fill(Utils.copyFluidStackWithAmount(out, accepted, false), FluidAction.EXECUTE);
                    state.tanks.output.drain(drained, FluidAction.EXECUTE);
                    changed = true;
                }
            }
        }
        if (wasActive != state.active || wasStall != state.stall || state.speed % 20 == 0 || changed) {
            ctx.markMasterDirty();
            ctx.requestMasterBESync();
        }
    }

    private boolean canIgnite(State state) { return SPARKPLUG_CONSUMPTION <= state.energyStorageMV.getEnergyStored(); }

    private void ignite(State state, IMultiblockContext<State> ctx) {
        state.energyStorageMV.extractEnergy(SPARKPLUG_CONSUMPTION, false);
        state.ignited = true;
        state.ignitionGracePeriod = 60;
        ctx.requestMasterBESync();
    }

    private static double particleXZSpeed() { return ApiUtils.RANDOM.nextDouble(-0.015625, 0.015625); }

    @Override
    public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) {
        State state = ctx.getState();
        if (cap == ForgeCapabilities.ENERGY) {
            if (ENERGY_INPUTS_HV.contains(position)) { return state.energyCapHV.cast(ctx); }
            if (ENERGY_INPUTS_MV.contains(position)) { return state.energyCapMV.cast(ctx); }
        }
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            if (position.posInMultiblock().equals(INPUT_FLUID_POI.posInMultiblock()) && (position.side() == null || position.side() == INPUT_FLUID_POI.side())) { return state.fluidCap.cast(ctx); }
            if (position.posInMultiblock().equals(OUTPUT_FLUID_POI.posInMultiblock()) && (position.side() == null || position.side() == OUTPUT_FLUID_POI.side())) { return state.fluidCapExhaust.cast(ctx); }
        }
        if (cap == MechanicalCapabilities.MECHANICAL_PROVIDER_CAPABILITY) {
            if (position.posInMultiblock().equals(ROTATIONAL_OUTPUT_POI) && (position.side() == null || position.side() == RelativeBlockFace.BACK)) { return LazyOptional.of(() -> new MechanicalEnergyProvider(state)).cast(); }
        }
        return LazyOptional.empty();
    }

    @Override
    public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override
    public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return GasTurbineShape.GETTER; }

    private record MechanicalEnergyProvider(State state) implements IMechanicalEnergyProvider {
        @Override
        public int getSpeed() { return state.speed; }
        @Override
        public float getTorque() { return 1f; }
        @Override
        public int getMaxSpeed() { return MAX_SPEED; }
        @Override
        public double getBaseMass() { return BASE_MASS; }
        @Override
        public double getDriveTorque() { return DRIVE_TORQUE; }
        @Override
        public double getFriction() { return FRICTION; }
    }

    public static class State implements IMultiblockState {
        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        public final GasTurbineTank tanks;
        public final StoredCapability<IFluidHandler> fluidCap;
        public final StoredCapability<IFluidHandler> fluidCapExhaust;
        public StoredCapability<IEnergyStorage> energyCapHV;
        public StoredCapability<IEnergyStorage> energyCapMV;
        public AveragingEnergyStorage energyStorageHV;
        public AveragingEnergyStorage energyStorageMV;
        public final CapabilityReference<IFluidHandler> fluidOutput;
        private final BiFunction<Level, FluidStack, GasTurbineRecipe> recipeGetter;
        public int speed = 0;
        public boolean active = false;
        public boolean starterRunning = false;
        public boolean ignited = false;
        public boolean stall = false;
        public int burnRemaining = 0;
        public int ignitionGracePeriod = 0;
        public boolean isShutdown = false;
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
        private RotationInertiaProcess inertia;
        private transient int soundGrace = 0;

        public State(IInitialMultiblockContext<State> ctx) {
            Runnable markDirty = ctx.getMarkDirtyRunnable();
            Runnable sync = ctx.getSyncRunnable();
            Runnable onChanged = () -> { markDirty.run(); sync.run(); };
            this.tanks = new GasTurbineTank(v -> onChanged.run());
            this.fluidCap = new StoredCapability<>(new ITArrayFluidHandler(tanks.input, false, true, onChanged));
            this.fluidCapExhaust = new StoredCapability<>(new ITArrayFluidHandler(tanks.output, true, false, onChanged));
            this.energyStorageHV = new AveragingEnergyStorage(ENERGY_CAPACITY);
            this.energyStorageMV = new AveragingEnergyStorage(ENERGY_CAPACITY_MV);
            this.energyCapHV = new StoredCapability<>(energyStorageHV);
            this.energyCapMV = new StoredCapability<>(energyStorageMV);
            this.recipeGetter = CachedRecipe.cached(GasTurbineRecipe::findRecipe);
            MultiblockFace outputMBFace = new MultiblockFace(OUTPUT_FLUID_POI.side(), OUTPUT_FLUID_POI.posInMultiblock());
            CapabilityPosition opposingCP = CapabilityPosition.opposing(outputMBFace);
            MultiblockFace opposingMBFace = new MultiblockFace(opposingCP.side(), opposingCP.posInMultiblock());
            this.fluidOutput = ctx.getCapabilityAt(ForgeCapabilities.FLUID_HANDLER, opposingMBFace);
            this.inertia = new RotationInertiaProcess(BASE_MASS, DRIVE_TORQUE, FRICTION);
        }

        @Override
        public void writeSaveNBT(CompoundTag nbt) {
            nbt.putInt("speed", speed);
            nbt.putBoolean("active", active);
            nbt.putBoolean("starterRunning", starterRunning);
            nbt.putBoolean("ignited", ignited);
            nbt.putBoolean("stall", stall);
            nbt.putInt("burnRemaining", burnRemaining);
            nbt.putInt("ignitionGracePeriod", ignitionGracePeriod);
            nbt.putBoolean("isShutdown", isShutdown);
            nbt.put("tanks", tanks.toNBT());
        }

        @Override
        public void readSaveNBT(CompoundTag nbt) {
            speed = nbt.getInt("speed");
            active = nbt.getBoolean("active");
            starterRunning = nbt.getBoolean("starterRunning");
            ignited = nbt.getBoolean("ignited");
            stall = nbt.getBoolean("stall");
            burnRemaining = nbt.getInt("burnRemaining");
            ignitionGracePeriod = nbt.getInt("ignitionGracePeriod");
            isShutdown = nbt.getBoolean("isShutdown");
            tanks.readNBT(nbt.getCompound("tanks"));
        }

        @Override
        public void writeSyncNBT(CompoundTag nbt) {
            nbt.putBoolean("active", active);
            nbt.putBoolean("starterRunning", starterRunning);
            nbt.putBoolean("ignited", ignited);
            nbt.putInt("speed", speed);
            nbt.putBoolean("isShutdown", isShutdown);
            nbt.putBoolean("stall", stall);
            nbt.put("tanks", tanks.toNBT());
        }

        @Override
        public void readSyncNBT(CompoundTag nbt) {
            boolean oldActive = active;
            active = nbt.getBoolean("active");
            starterRunning = nbt.getBoolean("starterRunning");
            ignited = nbt.getBoolean("ignited");
            speed = nbt.getInt("speed");
            isShutdown = nbt.getBoolean("isShutdown");
            stall = nbt.getBoolean("stall");
            tanks.readNBT(nbt.getCompound("tanks"));
            if (active && !oldActive && speed < MAX_SPEED / 4) { animation_fanFadeIn = 80; }
        }
    }

    public record GasTurbineTank(ITMarkableFluidTank input, ITMarkableFluidTank output) {
        public GasTurbineTank(Consumer<Void> markDirty) {
            this(new ITMarkableFluidTank(TANK_CAPACITY, markDirty), new ITMarkableFluidTank(TANK_CAPACITY, markDirty));
        }

        public static GasTurbineTank makeClient() { return new GasTurbineTank(v -> {}); }

        public Tag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.put("input", this.input.writeToNBT(new CompoundTag()));
            tag.put("output", this.output.writeToNBT(new CompoundTag()));
            return tag;
        }

        public void readNBT(CompoundTag tag) {
            this.input.readFromNBT(tag.getCompound("input"));
            this.output.readFromNBT(tag.getCompound("output"));
        }

        @SuppressWarnings("unused")
        public int getCapacity() { return TANK_CAPACITY; }
    }
}
