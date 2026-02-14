package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.common.util.CachedRecipe;
import com.google.common.collect.ImmutableList;
import mctmods.immersivetechnology.api.MechanicalCapabilities;
import mctmods.immersivetechnology.api.capability.IMechanicalEnergyConsumer;
import mctmods.immersivetechnology.api.capability.IMechanicalEnergyProvider;
import mctmods.immersivetechnology.client.particles.ColoredSmoke;
import mctmods.immersivetechnology.common.multiblocks.helper.ITDisplayContext;
import mctmods.immersivetechnology.common.multiblocks.helper.ITPressurizedFluidOutput;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.GasTurbineRecipe;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.GasTurbineShape;
import mctmods.immersivetechnology.common.multiblocks.metal.process.RotationInertiaProcess;
import mctmods.immersivetechnology.common.fluids.helper.ITArrayFluidHandler;
import mctmods.immersivetechnology.common.fluids.helper.ITMarkableFluidTank;
import mctmods.immersivetechnology.core.ITServerConfig;
import mctmods.immersivetechnology.core.util.multiblock.PoIJSONSchema;
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
import java.util.function.Consumer;
import java.util.function.Function;

public class GasTurbineLogic implements IMultiblockLogic<GasTurbineLogic.State>, IServerTickableComponent<GasTurbineLogic.State>, IClientTickableComponent<GasTurbineLogic.State>, ITPressurizedFluidOutput<GasTurbineLogic.State> {
    private static final List<PoIJSONSchema> RAW_POIS = ImmutableList.copyOf(GasTurbineShape.DATA.pointsOfInterest);

    public static final BlockPos REDSTONE_POI = getPosList("redstone").get(0);
    public static final BlockPos SMOKE_POI1 = getPosList("smoke1").get(0);
    public static final BlockPos SMOKE_POI2 = getPosList("smoke2").get(0);
    public static final BlockPos RUNNING_SOUND_POI = getPosList("sound_running").get(0);
    public static final BlockPos STARTER_SOUND_POI = getPosList("sound_starter").get(0);
    public static final BlockPos ARC_SOUND_POI = getPosList("sound_arc").get(0);
    public static final BlockPos SPARK_SOUND_POI = getPosList("sound_spark").get(0);
    public static final BlockPos IGNITE_SOUND_POI = getPosList("sound_ignite").get(0);
    public static final CapabilityPosition INPUT_FLUID_POI = new CapabilityPosition(getPosList("fluid_input").get(0), getFacing("fluid_input"));
    public static final CapabilityPosition OUTPUT_FLUID_POI = new CapabilityPosition(getPosList("fluid_output").get(0), getFacing("fluid_output"));
    public static final CapabilityPosition ENERGY_INPUT_HV_POI = new CapabilityPosition(getPosList("energy_input_hv").get(0), getFacing("energy_input_hv"));
    public static final CapabilityPosition ENERGY_INPUT_MV_POI = new CapabilityPosition(getPosList("energy_input_mv").get(0), getFacing("energy_input_mv"));
    public static final CapabilityPosition ROTATIONAL_OUTPUT_POI = new CapabilityPosition(getPosList("mech_output").get(0), getFacing("mech_output"));
    public static final List<BlockPos> FLUID_OUTPUT_POIS = getPosList("fluid_output");
    private static final RelativeBlockFace OUTPUT_FACING = getFacing("fluid_output");

    private static final int TANK_CAPACITY = ITServerConfig.gasTurbineTankCapacity;
    private static final int ENERGY_CAPACITY_HV = ITServerConfig.gasTurbineEnergyCapacityHV;
    private static final int ENERGY_CAPACITY_MV = ITServerConfig.gasTurbineEnergyCapacityMV;
    private static final int STARTER_CONSUMPTION = ITServerConfig.gasTurbineStarterConsumption;
    private static final int SPARKPLUG_CONSUMPTION = ITServerConfig.gasTurbineSparkplugConsumption;
    private static final double BASE_MASS = ITServerConfig.gasTurbineBaseMass;
    private static final double DRIVE_TORQUE = ITServerConfig.gasTurbineDriveTorque;
    private static final double FRICTION = ITServerConfig.gasTurbineFriction;
    private static final int MAX_SPEED = (int) (MechanicalCapabilities.MAX_RPM * ITServerConfig.gasTurbineMaxSpeedFactor);

    private static List<BlockPos> getPosList(String name) { return RAW_POIS.stream().filter(poi -> poi.name.equals(name)).map(poi -> new BlockPos(poi.pos[0], poi.pos[1], poi.pos[2])).collect(ImmutableList.toImmutableList()); }
    private static RelativeBlockFace getFacing(String name) {
        List<RelativeBlockFace> facings = RAW_POIS.stream().filter(poi -> poi.name.equals(name)).flatMap(poi -> poi.relativeFaces.stream()).distinct().toList();
        if (facings.size() != 1) { throw new RuntimeException("Inconsistent facings for POI: " + name); }
        return facings.get(0);
    }

    @Override public List<BlockPos> getOutputPositions() { return FLUID_OUTPUT_POIS; }

    @Override public Direction getOutputDirection(IMultiblockContext<State> ctx) { return ctx.getLevel().toAbsolute(OUTPUT_FACING); }

    @Override public List<ITMarkableFluidTank> getOutputTanks(State state) { return ImmutableList.of(state.tanks.output); }

    @Override public List<CapabilityReference<IFluidHandler>> getFluidOutputs(State state) { return ImmutableList.of(state.fluidOutput); }

    @Override public void tickClient(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        Level level = ctx.getLevel().getRawLevel();
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) { return; }
        float targetLevel = ITLib.remapRange(0, state.effectiveMaxSpeed, 0.2f, 1.0f, state.speed);
        if (state.currentLevel == 0f) { state.currentLevel = targetLevel; } else { state.currentLevel = state.currentLevel * 0.9f + targetLevel * 0.1f; }
        float smoothedLevel = state.currentLevel;
        float targetPitch = ITLib.remapRange(0, state.effectiveMaxSpeed, 0.5f, 1.5f, state.speed);
        if (state.currentPitch == 0f) { state.currentPitch = targetPitch; }
        if (state.currentPitch < 0.5f) { state.currentPitch = 0.5f; } else { state.currentPitch = state.currentPitch * 0.95f + targetPitch * 0.05f; }
        float currentBase = (state.speed / (float) state.effectiveMaxSpeed) * 72f;
        float step = currentBase;
        if (state.animation_fanFadeIn > 0) {
            step -= (state.animation_fanFadeIn / 80f) * currentBase;
            state.animation_fanFadeIn--;
        }
        state.animation_fanRotationStep = step;
        state.animation_fanRotation += step;
        state.animation_fanRotation %= 360;
        if (state.speed > 0) { state.soundGrace = 40; } else if (state.soundGrace > 0) { state.soundGrace--; }
        Vec3 runningPos = ctx.getLevel().toAbsolute(new Vec3(RUNNING_SOUND_POI.getX() + 0.5, RUNNING_SOUND_POI.getY() + 0.5, RUNNING_SOUND_POI.getZ() + 0.5));
        Vec3 starterPos = ctx.getLevel().toAbsolute(new Vec3(STARTER_SOUND_POI.getX() + 0.5, STARTER_SOUND_POI.getY() + 0.5, STARTER_SOUND_POI.getZ() + 0.5));
        Vec3 arcPos = ctx.getLevel().toAbsolute(new Vec3(ARC_SOUND_POI.getX() + 0.5, ARC_SOUND_POI.getY() + 0.5, ARC_SOUND_POI.getZ() + 0.5));
        Vec3 sparkPos = ctx.getLevel().toAbsolute(new Vec3(SPARK_SOUND_POI.getX() + 0.5, SPARK_SOUND_POI.getY() + 0.5, SPARK_SOUND_POI.getZ() + 0.5));
        Vec3 ignitePos = ctx.getLevel().toAbsolute(new Vec3(IGNITE_SOUND_POI.getX() + 0.5, IGNITE_SOUND_POI.getY() + 0.5, IGNITE_SOUND_POI.getZ() + 0.5));
        float runningAtt = (float) Math.max(player.distanceToSqr(runningPos) / 32, 1);
        float runningVol = (8 * (smoothedLevel - 0.2f)) / runningAtt;
        if (state.speed > 0 && ((state.everIgnited && !state.starterRunning) || (state.stall && state.ignited)) && runningVol > 0.01f && !state.runningSoundPlaying.getAsBoolean()) {
            state.runningSoundId++;
            int thisId = state.runningSoundId;
            state.runningSoundPlaying = ITSound.startSound(
                    () -> state.speed > 0 && ((state.everIgnited && !state.starterRunning) || (state.stall && state.ignited)) && state.runningSoundId == thisId,
                    ctx.isValid(),
                    runningPos,
                    ITSounds.gasRunning,
                    () -> {
                        LocalPlayer p = Minecraft.getInstance().player;
                        if (p == null) { return 0f; }
                        float a = (float) Math.max(p.distanceToSqr(runningPos) / 32, 1);
                        return (8 * (smoothedLevel - 0.2f)) / a;
                    },
                    () -> state.currentPitch
            );
        }
        if (state.starterRunning) {
            float starterAtt = (float) Math.max(player.distanceToSqr(starterPos) / 64, 1);
            float starterVol = Math.min(smoothedLevel / starterAtt, 0.4f);
            if (starterVol > 0.01f && !state.starterSoundPlaying.getAsBoolean()) {
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
            if (state.speed >= state.effectiveMaxSpeed / 4 && state.hasIgniter) {
                float arcAtt = (float) Math.max(player.distanceToSqr(arcPos) / 64, 1);
                float arcVol = Math.min(smoothedLevel / arcAtt, 0.4f);
                if (arcVol > 0.01f && !state.arcSoundPlaying.getAsBoolean()) {
                    state.arcSoundId++;
                    int thisId = state.arcSoundId;
                    state.arcSoundPlaying = ITSound.startSound(
                            () -> state.starterRunning && state.speed >= state.effectiveMaxSpeed / 4 && state.hasIgniter && state.arcSoundId == thisId, ctx.isValid(), arcPos, ITSounds.gasArc,
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
        if (state.ignited && !state.lastIgnited && state.speed < state.effectiveMaxSpeed / 2) {
            state.lastIgnited = true;
            float ignitionAtt = (float) Math.max(player.distanceToSqr(sparkPos) / 64, 1);
            level.playLocalSound(sparkPos.x, sparkPos.y, sparkPos.z, ITSounds.gasSpark.get(), SoundSource.BLOCKS, 1 / ignitionAtt, 1, false);
            state.igniteDelay = 3;
        } else { state.lastIgnited = state.ignited; }
        if (state.igniteDelay > 0) {
            state.igniteDelay--;
            if (state.igniteDelay == 0 && state.starterRunning) {
                float ignitionAtt = (float) Math.max(player.distanceToSqr(ignitePos) / 64, 1);
                level.playLocalSound(ignitePos.x, ignitePos.y, ignitePos.z, ITSounds.gasIgnite.get(), SoundSource.BLOCKS, 1 / ignitionAtt, 1, false);
            }
        }
        if (state.starterRunning && state.speed >= state.effectiveMaxSpeed / 4) {
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
                float normSpeed = Math.max(0f, ITLib.remapRange(100, state.effectiveMaxSpeed, 0f, 1f, state.speed));
                double dirVelHoriz = 0.125 * normSpeed;
                double dirVelVert = 0.1 * normSpeed;
                double baseUp = 0.0625 + 0.1 * (1 - normSpeed);
                double velX = facing.getStepX() * dirVelHoriz + particleXZSpeed();
                double velY = facing.getStepY() * dirVelVert + baseUp;
                double velZ = facing.getStepZ() * dirVelHoriz + particleXZSpeed();
                FluidStack outFluid = state.tanks.output.getFluid();
                float r = 0.5F, g = 0.5F, b = 0.5F;
                if (!outFluid.isEmpty()) {
                    int tint = IClientFluidTypeExtensions.of(outFluid.getFluid()).getTintColor(outFluid);
                    r = ((tint >> 16) & 0xFF) / 255f;
                    g = ((tint >> 8) & 0xFF) / 255f;
                    b = (tint & 0xFF) / 255f;
                }
                level.addAlwaysVisibleParticle(new ColoredSmoke(r, g, b), smokePos.x, smokePos.y, smokePos.z, velX, velY, velZ);
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override public void tickServer(IMultiblockContext<State> ctx) {
        pumpOutputs(ctx);
        State state = ctx.getState();
        state.hasIgniter = state.mvInput.isPresent();
        state.canIgniteClient = SPARKPLUG_CONSUMPTION <= state.energyStorageMV.getEnergyStored();
        boolean wasActive = state.active;
        boolean wasStall = state.stall;
        state.active = false;
        Level level = ctx.getLevel().getRawLevel();
        Direction outputFacing = ctx.getLevel().getOrientation().front();
        BlockPos outputPortAbs = ctx.getLevel().toAbsolute(ROTATIONAL_OUTPUT_POI.posInMultiblock());
        BlockPos consumerAbsPos = outputPortAbs.relative(outputFacing);
        BlockEntity entity = level.getBlockEntity(consumerAbsPos);
        boolean hasConsumer = false;
        double additionalMass = 0.0;
        double additionalFriction = 0.0;
        int consumerMaxSpeed = MechanicalCapabilities.MAX_RPM;
        if (entity != null) {
            LazyOptional<IMechanicalEnergyConsumer> consumerCap = entity.getCapability(MechanicalCapabilities.MECHANICAL_CONSUMER_CAPABILITY, outputFacing.getOpposite());
            if (consumerCap.isPresent()) {
                hasConsumer = true;
                IMechanicalEnergyConsumer consumer = consumerCap.orElseThrow(RuntimeException::new);
                additionalMass = consumer.getMass();
                additionalFriction = consumer.getFriction();
                consumerMaxSpeed = consumer.getMaxSpeed();
            }
        }
        int effectiveMax = hasConsumer ? Math.min(MAX_SPEED, consumerMaxSpeed) : MAX_SPEED;
        state.effectiveMaxSpeed = effectiveMax;
        if (additionalMass != state.connectedMass || additionalFriction != state.connectedFriction) {
            state.connectedMass = additionalMass;
            state.connectedFriction = additionalFriction;
            state.inertia = new RotationInertiaProcess(BASE_MASS + state.connectedMass, DRIVE_TORQUE, FRICTION + state.connectedFriction, effectiveMax);
        }
        boolean isRSEnabled = state.rsState.isEnabled(ctx);
        state.ignited = state.ignitionGracePeriod > 0;
        state.starterRunning = false;
        if (isRSEnabled && STARTER_CONSUMPTION <= state.energyStorageHV.getEnergyStored()) {
            state.starterRunning = true;
            state.energyStorageHV.extractEnergy(STARTER_CONSUMPTION, false);
        }
        if (state.speed <= 0) { state.speed = 0; state.isShutdown = false; state.stall = false; }
        if (!isRSEnabled || !hasConsumer) {
            state.isShutdown = true;
            state.ignitionGracePeriod = 0;
            state.burnRemaining = 0;
            state.stall = false;
        }
        if (state.speed < effectiveMax / 4) {
            if (isRSEnabled && !state.isShutdown) {
                if (state.ignitionGracePeriod > 0) { state.ignitionGracePeriod--; }
                if (state.starterRunning) {
                    state.speed = Math.min(effectiveMax, state.speed + state.inertia.getSpeedUpRate());
                    state.active = true;
                } else { state.speed = Math.max(0, state.speed - state.inertia.getSpeedDownRate()); }
            } else { state.speed = Math.max(0, state.speed - state.inertia.getSpeedDownRate()); }
        } else {
            if (state.isShutdown) { state.speed = Math.max(0, state.speed - state.inertia.getSpeedDownRate()); }
            else {
                if (state.starterRunning) {
                    if (state.hasIgniter && canIgnite(state)) {
                        state.stall = true;
                        if (!wasStall) { ignite(state, ctx); }
                        else { state.ignitionGracePeriod = 60; }
                        state.speed = effectiveMax / 4;
                        state.active = true;
                        if (state.ignitionGracePeriod > 0) { state.ignitionGracePeriod--; }
                    } else {
                        state.stall = false;
                        state.speed = Math.max(0, state.speed - state.inertia.getSpeedDownRate());
                    }
                } else {
                    state.stall = false;
                    if (state.burnRemaining > 0 && (state.ignited || canIgnite(state))) {
                        state.burnRemaining--;
                        if (!state.ignited) { ignite(state, ctx); }
                        state.speed = Math.min(effectiveMax, state.speed + state.inertia.getSpeedUpRate());
                        state.active = true;
                    } else if (state.ignited || canIgnite(state)) {
                        FluidStack fluid = state.tanks.input.getFluid();
                        GasTurbineRecipe recipe = state.recipeGetter.apply(ctx.getLevel().getRawLevel(), fluid);
                        if (recipe != null && fluid.getAmount() >= recipe.input.getAmount()) {
                            state.tanks.input.drain(recipe.input.getAmount(), FluidAction.EXECUTE);
                            state.currentTorque = recipe.torque;
                            if (recipe.fluidOutput != null) {
                                int filled = state.tanks.output.fill(recipe.fluidOutput, FluidAction.EXECUTE);
                                if (filled < recipe.fluidOutput.getAmount()) {}
                            }
                            state.burnRemaining = recipe.getTotalProcessTime() - 1;
                            if (!state.ignited) { ignite(state, ctx); }
                            state.speed = Math.min(effectiveMax, state.speed + state.inertia.getSpeedUpRate());
                            state.active = true;
                            ctx.markMasterDirty();
                        } else { state.speed = Math.max(0, state.speed - state.inertia.getSpeedDownRate()); }
                    } else { state.speed = Math.max(0, state.speed - state.inertia.getSpeedDownRate()); }
                }
            }
        }
        if (wasActive != state.active || wasStall != state.stall || state.speed % 20 == 0) {
            ctx.markMasterDirty();
            ctx.requestMasterBESync();
        }
    }

    private boolean canIgnite(State state) { return SPARKPLUG_CONSUMPTION <= state.energyStorageMV.getEnergyStored(); }

    private void ignite(State state, IMultiblockContext<State> ctx) {
        state.energyStorageMV.extractEnergy(SPARKPLUG_CONSUMPTION, false);
        state.everIgnited = true;
        state.ignited = true;
        state.ignitionGracePeriod = 60;
        ctx.requestMasterBESync();
    }

    private static double particleXZSpeed() { return ApiUtils.RANDOM.nextDouble(-0.015625, 0.015625); }

    @Override public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) {
        State state = ctx.getState();
        if (cap == ForgeCapabilities.ENERGY) {
            if (position.equals(ENERGY_INPUT_HV_POI)) { return state.energyCapHV.cast(ctx); }
            if (position.equals(ENERGY_INPUT_MV_POI)) { return state.energyCapMV.cast(ctx); }
        }
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            if (position.equals(INPUT_FLUID_POI)) { return state.fluidCap.cast(ctx); }
            if (position.equals(OUTPUT_FLUID_POI)) { return state.fluidCapExhaust.cast(ctx); }
        }
        if (cap == MechanicalCapabilities.MECHANICAL_PROVIDER_CAPABILITY) {
            if (position.equals(ROTATIONAL_OUTPUT_POI)) { return LazyOptional.of(() -> new MechanicalEnergyProvider(state)).cast(); }
        }
        return LazyOptional.empty();
    }

    private record MechanicalEnergyProvider(State state) implements IMechanicalEnergyProvider {
        @Override public int getSpeed() { return state.speed; }
        @Override public float getTorque() { return state.currentTorque; }
        @Override public int getMaxSpeed() { return MAX_SPEED; }
        @Override public double getBaseMass() { return BASE_MASS; }
        @Override public double getDriveTorque() { return DRIVE_TORQUE; }
        @Override public double getFriction() { return FRICTION; }
    }

    @Override public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return GasTurbineShape.GETTER; }

    public static class State implements IMultiblockState, ITDisplayContext {
        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        public final GasTurbineTank tanks;
        public final StoredCapability<IFluidHandler> fluidCap;
        public final StoredCapability<IFluidHandler> fluidCapExhaust;
        public StoredCapability<IEnergyStorage> energyCapHV;
        public StoredCapability<IEnergyStorage> energyCapMV;
        public AveragingEnergyStorage energyStorageHV;
        public AveragingEnergyStorage energyStorageMV;
        public final CapabilityReference<IFluidHandler> fluidOutput;
        public final CapabilityReference<IEnergyStorage> mvInput;
        private final BiFunction<Level, FluidStack, GasTurbineRecipe> recipeGetter;
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
        public int effectiveMaxSpeed = MAX_SPEED;
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
            this.tanks = new GasTurbineTank(v -> onChanged.run(), TANK_CAPACITY);
            this.fluidCap = new StoredCapability<>(new ITArrayFluidHandler(tanks.input, false, true, onChanged));
            this.fluidCapExhaust = new StoredCapability<>(new ITArrayFluidHandler(tanks.output, true, false, onChanged));
            this.energyStorageHV = new AveragingEnergyStorage(ENERGY_CAPACITY_HV);
            this.energyStorageMV = new AveragingEnergyStorage(ENERGY_CAPACITY_MV);
            this.energyCapHV = new StoredCapability<>(energyStorageHV);
            this.energyCapMV = new StoredCapability<>(energyStorageMV);
            this.recipeGetter = CachedRecipe.cached(GasTurbineRecipe::findRecipe);
            MultiblockFace outputMBFace = new MultiblockFace(OUTPUT_FACING, FLUID_OUTPUT_POIS.get(0));
            CapabilityPosition oppCp = CapabilityPosition.opposing(outputMBFace);
            MultiblockFace oppMbf = new MultiblockFace(oppCp.side(), oppCp.posInMultiblock());
            this.fluidOutput = ctx.getCapabilityAt(ForgeCapabilities.FLUID_HANDLER, oppMbf);
            MultiblockFace mvInputMBFace = new MultiblockFace(ENERGY_INPUT_MV_POI.side(), ENERGY_INPUT_MV_POI.posInMultiblock());
            CapabilityPosition mvOpposingCP = CapabilityPosition.opposing(mvInputMBFace);
            MultiblockFace mvOpposingMBFace = new MultiblockFace(mvOpposingCP.side(), mvOpposingCP.posInMultiblock());
            this.mvInput = ctx.getCapabilityAt(ForgeCapabilities.ENERGY, mvOpposingMBFace);
            this.inertia = new RotationInertiaProcess(BASE_MASS, DRIVE_TORQUE, FRICTION, MAX_SPEED);
        }

        @Override public void writeSaveNBT(CompoundTag nbt) {
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

        @Override public void writeSyncNBT(CompoundTag nbt) {
            CompoundTag display = new CompoundTag();
            writeDisplaySyncNBT(display);
            nbt.put("display", display);
        }

        @Override public void readSyncNBT(CompoundTag nbt) {
            if (nbt.contains("display", Tag.TAG_COMPOUND)) { readDisplaySyncNBT(nbt.getCompound("display")); }
        }

        @Override public boolean isActive() { return active; }

        @Override public AveragingEnergyStorage getEnergy() { return energyStorageHV; }

        @Override public List<AveragingEnergyStorage> getEnergies() { return List.of(energyStorageHV, energyStorageMV); }

        @Override public IFluidTank[] getInternalTanks() { return new IFluidTank[]{tanks.input, tanks.output}; }

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
    }

    public record GasTurbineTank(ITMarkableFluidTank input, ITMarkableFluidTank output) {
        public GasTurbineTank(Consumer<Void> markDirty, int capacity) {
            this(new ITMarkableFluidTank(capacity, markDirty), new ITMarkableFluidTank(capacity, markDirty));
        }

        public static GasTurbineTank makeClient(int capacity) { return new GasTurbineTank(v -> {}, capacity); }

        public CompoundTag toNBT() {
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
        public int getCapacity() { return input.getCapacity(); }
    }
}
