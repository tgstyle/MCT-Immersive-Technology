package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import mctmods.immersivetechnology.client.particles.ColoredSmoke;
import mctmods.immersivetechnology.common.multiblocks.helper.IDisplayContext;
import mctmods.immersivetechnology.common.multiblocks.helper.MultiblockPOIHelper;
import mctmods.immersivetechnology.common.multiblocks.helper.IPressurizedFluidOutput;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.SteamTurbineRecipe;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.SteamTurbineShape;
import mctmods.immersivetechnology.common.multiblocks.metal.process.RotationInertiaProcess;
import mctmods.immersivetechnology.common.fluids.helper.ArrayFluidHandler;
import mctmods.immersivetechnology.common.fluids.helper.MarkableFluidTank;
import mctmods.immersivetechnology.core.ServerConfig;
import mctmods.immersivetechnology.core.util.multiblock.PoIJSONSchema;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.lib.ModSound;
import mctmods.immersivetechnology.core.registration.Sounds;
import mctmods.immersivetechnology.core.util.CachedRecipe;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IMultiblockComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import com.google.common.collect.ImmutableList;
import com.immersiveconvergence.api.MechanicalCapabilities;
import com.immersiveconvergence.api.capability.IMechanicalEnergyConsumer;
import com.immersiveconvergence.api.capability.IMechanicalEnergyProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public class SteamTurbineLogic implements IMultiblockLogic<SteamTurbineLogic.State>, IServerTickableComponent<SteamTurbineLogic.State>, IClientTickableComponent<SteamTurbineLogic.State>, IPressurizedFluidOutput<SteamTurbineLogic.State> {
    private static final List<PoIJSONSchema> RAW_POIS = ImmutableList.copyOf(SteamTurbineShape.DATA.pointsOfInterest);

    public static final BlockPos REDSTONE_POI = MultiblockPOIHelper.getPosList(RAW_POIS, "redstone0").getFirst();
    public static final BlockPos RUNNING_SOUND_POI = MultiblockPOIHelper.getPosList(RAW_POIS, "sound_running0").getFirst();
    public static final List<CapabilityPosition> SMOKE_POIS = MultiblockPOIHelper.getCapabilityPositions(RAW_POIS, "smoke0");

    public static final List<BlockPos> INPUT_FLUID_POIS = MultiblockPOIHelper.getPosList(RAW_POIS, "fluid_input0");
    public static final List<BlockPos> OUTPUT_FLUID_POIS = MultiblockPOIHelper.getPosList(RAW_POIS, "fluid_output0");
    public static final List<BlockPos> MECHANICAL_OUTPUT_POIS = MultiblockPOIHelper.getPosList(RAW_POIS, "mechanical_output0");

    public static final CapabilityPosition INPUT_FLUID_POI = MultiblockPOIHelper.getCapabilityPosition(RAW_POIS, "fluid_input0");
    public static final List<CapabilityPosition> OUTPUT_FLUID_POIS_CAP = MultiblockPOIHelper.getCapabilityPositions(RAW_POIS, "fluid_output0");
    public static final CapabilityPosition MECHANICAL_OUTPUT_POI = MultiblockPOIHelper.getCapabilityPosition(RAW_POIS, "mechanical_output0");

    private static final RelativeBlockFace OUTPUT_FACING = OUTPUT_FLUID_POIS_CAP.getFirst().side();

    private static final int INPUT_TANK_CAPACITY = ServerConfig.steamTurbineInputTankCapacity;
    private static final int OUTPUT_TANK_CAPACITY = ServerConfig.steamTurbineOutputTankCapacity;
    private static final double BASE_MASS = ServerConfig.steamTurbineBaseMass;
    private static final double DRIVE_TORQUE = ServerConfig.steamTurbineDriveTorque;
    private static final double FRICTION = ServerConfig.steamTurbineFriction;
    private static final int MAX_SPEED = (int) (MechanicalCapabilities.MAX_RPM * ServerConfig.steamTurbineMaxSpeedFactor);

    @Override public List<BlockPos> getOutputPositions() { return OUTPUT_FLUID_POIS; }

    @Override public Direction getOutputDirection(IMultiblockContext<State> ctx) { return ctx.getLevel().toAbsolute(OUTPUT_FACING); }

    @Override public List<MarkableFluidTank> getOutputTanks(State state) { return Collections.nCopies(OUTPUT_FLUID_POIS_CAP.size(), state.tanks.output); }

    @Override public List<RelativeBlockFace> getOutputFacings() { return OUTPUT_FLUID_POIS_CAP.stream().map(CapabilityPosition::side).toList(); }

    @Override public boolean isOutputConnected(IMultiblockContext<State> ctx, int index) {
        BlockPos localPos = OUTPUT_FLUID_POIS.get(index);
        BlockPos absolutePos = ctx.getLevel().toAbsolute(localPos);
        RelativeBlockFace relFace = OUTPUT_FLUID_POIS_CAP.get(index).side();
        Direction side = ctx.getLevel().toAbsolute(relFace);
        if (side == null) { return false; }
        BlockPos targetPos = absolutePos.relative(side);
        IFluidHandler handler = ctx.getLevel().getRawLevel().getCapability(Capabilities.FluidHandler.BLOCK, targetPos, side.getOpposite());
        return handler != null;
    }

    @Override public void tickClient(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        boolean targetActive = state.active || state.speed > 0;
        float targetLevel = Reference.remapRange(0, state.effectiveMaxSpeed, 0.5f, 1.0f, state.speed);
        if (state.currentLevel == 0f) { state.currentLevel = targetLevel; } else { state.currentLevel = state.currentLevel * 0.9f + targetLevel * 0.1f; }
        float targetPitch = Reference.remapRange(0, state.effectiveMaxSpeed, 0.5f, 1.5f, state.speed);
        if (state.currentPitch == 0f) { state.currentPitch = targetPitch; } else { state.currentPitch = state.currentPitch * 0.95f + targetPitch * 0.05f; }
        if (state.currentPitch < 0.5f) { state.currentPitch = 0.5f; }
        float base = (state.speed / (float) state.effectiveMaxSpeed) * 72f;
        float step = base;
        if (state.animation_fanFadeIn > 0) {
            step -= (state.animation_fanFadeIn / 80f) * base;
            state.animation_fanFadeIn--;
        }
        state.animation_fanRotationStep = step;
        state.animation_fanRotation += step;
        state.animation_fanRotation %= 360;
        final Vec3 soundPos = ctx.getLevel().toAbsolute(new Vec3(RUNNING_SOUND_POI.getX() + 0.5, RUNNING_SOUND_POI.getY() + 0.5, RUNNING_SOUND_POI.getZ() + 0.5));
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) { return; }
        float attenuation = (float) Math.max(player.distanceToSqr(soundPos) / 32, 1);
        float vol = (11 * (state.currentLevel - 0.5f)) / attenuation;
        if (targetActive && vol > 0.01f && !state.isSoundPlaying.getAsBoolean()) {
            state.isSoundPlaying = ModSound.startSound(
                    () -> state.active || state.speed > 0,
                    ctx.isValid(),
                    soundPos,
                    Sounds.steamTurbine,
                    () -> {
                        LocalPlayer p = Minecraft.getInstance().player;
                        if (p == null) { return 0f; }
                        float a = (float) Math.max(p.distanceToSqr(soundPos) / 32, 1);
                        return (11 * (state.currentLevel - 0.5f)) / a;
                    },
                    () -> state.currentPitch
            );
        }
        if (state.active && ctx.getLevel().shouldTickModulo(2)) {
            float normSpeed = Math.max(0f, Reference.remapRange(100, state.effectiveMaxSpeed, 0f, 1f, state.speed));
            double dirVelHoriz = 0.125 * normSpeed;
            double dirVelVert = 0.1 * normSpeed;
            double baseUp = 0.0625 + 0.1 * (1 - normSpeed);
            FluidStack outFluid = state.tanks.output.getFluid();
            float r = 0.5F, g = 0.5F, b = 0.5F;
            if (!outFluid.isEmpty()) {
                int tint = net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions.of(outFluid.getFluid()).getTintColor(outFluid);
                r = ((tint >> 16) & 0xFF) / 255f;
                g = ((tint >> 8) & 0xFF) / 255f;
                b = (tint & 0xFF) / 255f;
            }
            Level level = ctx.getLevel().getRawLevel();
            for (int i = 0; i < SMOKE_POIS.size(); i++) {
                if (isOutputConnected(ctx, i)) { continue; }
                CapabilityPosition smokePoi = SMOKE_POIS.get(i);
                Direction facing = ctx.getLevel().toAbsolute(smokePoi.side());
                if (facing == null) { continue; }
                BlockPos outputAbs = ctx.getLevel().toAbsolute(smokePoi.posInMultiblock());
                Vec3 smokePos = new Vec3(outputAbs.getX() + 0.5, outputAbs.getY() + 0.5, outputAbs.getZ() + 0.5);
                double velX = facing.getStepX() * dirVelHoriz + particleXZSpeed();
                double velY = facing.getStepY() * dirVelVert + baseUp;
                double velZ = facing.getStepZ() * dirVelHoriz + particleXZSpeed();
                level.addAlwaysVisibleParticle(new ColoredSmoke(r, g, b), smokePos.x, smokePos.y, smokePos.z, velX, velY, velZ);
            }
        }
    }

    @Override public void tickServer(IMultiblockContext<State> ctx) {
        pumpOutputs(ctx);
        State state = ctx.getState();
        boolean previouslyActive = state.active;
        int previousSpeed = state.speed;
        boolean currentlyEnabled = state.rsState.isEnabled(ctx);
        state.active = false;
        Level level = ctx.getLevel().getRawLevel();
        Direction outputFacing = ctx.getLevel().getOrientation().front();
        BlockPos outputPortAbs = ctx.getLevel().toAbsolute(MECHANICAL_OUTPUT_POIS.getFirst());
        BlockPos consumerAbsPos = outputPortAbs.relative(outputFacing);
        BlockEntity entity = level.getBlockEntity(consumerAbsPos);
        boolean hasConsumer = false;
        double additionalMass = 0.0;
        double additionalFriction = 0.0;
        int consumerMaxSpeed = MechanicalCapabilities.MAX_RPM;
        if (entity != null) {
            IMechanicalEnergyConsumer consumer = level.getCapability(MechanicalCapabilities.MECHANICAL_CONSUMER, consumerAbsPos, outputFacing.getOpposite());
            if (consumer != null) {
                hasConsumer = true;
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
        boolean canRun = currentlyEnabled && hasConsumer;
        float ratio = 0f;
        if (canRun) {
            state.currentTorque = 1.0f;
            FluidStack fluid = state.tanks.input.getFluid();
            if (fluid.getAmount() > 0) {
                SteamTurbineRecipe recipe = state.recipeGetter.apply(level, fluid);
                if (recipe != null) {
                    state.currentTorque = recipe.torque();
                    float fluidPerTick = (float) recipe.getInputAmount() / recipe.getTotalProcessTime();
                    state.accumConsume += fluidPerTick;
                    int toDrain = (int) state.accumConsume;
                    if (toDrain > 0) {
                        FluidStack drainedStack = state.tanks.input.drain(toDrain, FluidAction.EXECUTE);
                        int drained = drainedStack.getAmount();
                        state.accumConsume -= drained;
                        ratio = (float) drained / fluidPerTick;
                        FluidStack recipeOut = recipe.fluidOutput();
                        if (recipeOut != null) {
                            float outputPerTick = (float) recipeOut.getAmount() / recipe.getTotalProcessTime();
                            state.outAccum += ratio * outputPerTick;
                            if (state.outAccum >= 1) {
                                FluidStack out = new FluidStack(recipeOut.getFluid(), recipeOut.getAmount());
                                out.setAmount((int) state.outAccum);
                                int filled = state.tanks.output.fill(out, FluidAction.EXECUTE);
                                state.outAccum -= filled;
                            }
                        }
                    }
                }
            }
        }
        state.effectiveRatio = state.effectiveRatio * 0.9f + ratio * 0.1f;
        double alpha = state.inertia.getAlpha(canRun ? state.effectiveRatio : 0f, state.speed);
        state.accumDelta += alpha;
        int delta = (int) Math.round(state.accumDelta);
        state.accumDelta -= delta;
        state.speed += delta;
        if (state.speed > effectiveMax) { state.speed = effectiveMax; }
        if (state.speed < 0) { state.speed = 0; }
        state.active = state.effectiveRatio > 0.001f;
        if (state.pressureReleaseCooldown > 0) { state.pressureReleaseCooldown--; }
        boolean triggerRelease = (!previouslyActive && state.active) || (!state.wasEnabled && currentlyEnabled);
        if (triggerRelease && state.pressureReleaseCooldown <= 0) {
            BlockPos soundPos = ctx.getLevel().toAbsolute(RUNNING_SOUND_POI);
            level.playSound(null, soundPos, Sounds.pressure_release.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            state.pressureReleaseCooldown = 200;
        }
        state.wasEnabled = currentlyEnabled;
        if (previouslyActive != state.active || state.speed % 5 == 0 || previousSpeed != state.speed) {
            ctx.markMasterDirty();
            ctx.requestMasterBESync();
        }
    }

    private static double particleXZSpeed() { return ApiUtils.RANDOM.nextDouble(-0.015625, 0.015625); }

    @Override public void registerCapabilities(IMultiblockComponent.CapabilityRegistrar<State> register) {
        register.registerAt(Capabilities.FluidHandler.BLOCK, INPUT_FLUID_POI, state -> state.fluidInputHandler);
        for (CapabilityPosition outputPos : OUTPUT_FLUID_POIS_CAP) { register.registerAt(Capabilities.FluidHandler.BLOCK, outputPos, state -> state.fluidOutputHandler); }
        register.registerAt(MechanicalCapabilities.MECHANICAL_PROVIDER, MECHANICAL_OUTPUT_POI, state -> state.mechanicalProvider);
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

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return SteamTurbineShape.GETTER; }

    public static class State implements IMultiblockState, IDisplayContext {
        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        public final SteamTurbineTank tanks;
        public final IFluidHandler fluidInputHandler;
        public final IFluidHandler fluidOutputHandler;
        private final BiFunction<Level, FluidStack, SteamTurbineRecipe> recipeGetter;
        public int speed = 0;
        public float currentTorque = 1.0f;
        public boolean active = false;
        public BooleanSupplier isSoundPlaying = () -> false;
        public float animation_fanRotationStep = 0;
        public float animation_fanRotation = 0;
        private transient int animation_fanFadeIn = 0;
        private transient float currentLevel = 0f;
        private transient float currentPitch = 0f;
        private double connectedMass = 0;
        private double connectedFriction = 0;
        private RotationInertiaProcess inertia;
        private int pressureReleaseCooldown = 0;
        private boolean wasEnabled = false;
        public int effectiveMaxSpeed = MAX_SPEED;
        private float accumConsume;
        private float outAccum;
        private double accumDelta;
        private float effectiveRatio;
        private final MechanicalEnergyProvider mechanicalProvider;

        public State(IInitialMultiblockContext<State> ctx) {
            Runnable markDirty = ctx.getMarkDirtyRunnable();
            Runnable sync = ctx.getSyncRunnable();
            Runnable onChanged = () -> { markDirty.run(); sync.run(); };
            this.tanks = new SteamTurbineTank(v -> onChanged.run(), INPUT_TANK_CAPACITY, OUTPUT_TANK_CAPACITY);
            this.fluidInputHandler = new ArrayFluidHandler(tanks.input, false, true, onChanged);
            this.fluidOutputHandler = new ArrayFluidHandler(tanks.output, true, false, onChanged);
            this.recipeGetter = CachedRecipe.cached(SteamTurbineRecipe::findRecipe);
            this.inertia = new RotationInertiaProcess(BASE_MASS + connectedMass, DRIVE_TORQUE, FRICTION + connectedFriction, effectiveMaxSpeed);
            this.accumConsume = 0f;
            this.outAccum = 0f;
            this.accumDelta = 0.0;
            this.effectiveRatio = 0f;
            this.mechanicalProvider = new MechanicalEnergyProvider(this);
        }

        @Override public void writeSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            nbt.putInt("speed", speed);
            nbt.putBoolean("active", active);
            nbt.put("tanks", tanks.toNBT(provider));
            nbt.putInt("pressureReleaseCooldown", pressureReleaseCooldown);
            nbt.putBoolean("wasEnabled", wasEnabled);
            nbt.putInt("effectiveMaxSpeed", effectiveMaxSpeed);
            nbt.putFloat("accumConsume", accumConsume);
            nbt.putFloat("outAccum", outAccum);
            nbt.putDouble("accumDelta", accumDelta);
            nbt.putFloat("effectiveRatio", effectiveRatio);
        }

        @Override public void readSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            speed = nbt.getInt("speed");
            active = nbt.getBoolean("active");
            tanks.readNBT(nbt.getCompound("tanks"), provider);
            pressureReleaseCooldown = nbt.getInt("pressureReleaseCooldown");
            wasEnabled = nbt.getBoolean("wasEnabled");
            effectiveMaxSpeed = nbt.getInt("effectiveMaxSpeed");
            accumConsume = nbt.getFloat("accumConsume");
            outAccum = nbt.getFloat("outAccum");
            accumDelta = nbt.getDouble("accumDelta");
            effectiveRatio = nbt.getFloat("effectiveRatio");
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

        @Override public IItemHandlerModifiable getInventory() { return null; }

        @Override public IFluidTank[] getInternalTanks() { return new IFluidTank[]{tanks.input, tanks.output}; }

        @Override public void writeDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            nbt.putBoolean("active", active);
            nbt.putInt("speed", speed);
            nbt.put("tanks", tanks.toNBT(provider));
            nbt.putInt("effectiveMaxSpeed", effectiveMaxSpeed);
        }

        @Override public void readDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            boolean oldActive = active;
            active = nbt.getBoolean("active");
            speed = nbt.getInt("speed");
            tanks.readNBT(nbt.getCompound("tanks"), provider);
            effectiveMaxSpeed = nbt.getInt("effectiveMaxSpeed");
            if (active && !oldActive) { animation_fanFadeIn = 80; }
        }
    }

    public record SteamTurbineTank(MarkableFluidTank input, MarkableFluidTank output) {
        public SteamTurbineTank(Consumer<Void> markDirty, int inputCapacity, int outputCapacity) {
            this(new MarkableFluidTank(inputCapacity, markDirty), new MarkableFluidTank(outputCapacity, markDirty));
        }

        public static SteamTurbineTank makeClient(int inputCapacity, int outputCapacity) { return new SteamTurbineTank(v -> {}, inputCapacity, outputCapacity); }

        public CompoundTag toNBT(HolderLookup.Provider provider) {
            CompoundTag tag = new CompoundTag();
            tag.put("input", this.input.writeToNBT(provider, new CompoundTag()));
            tag.put("output", this.output.writeToNBT(provider, new CompoundTag()));
            return tag;
        }

        public void readNBT(CompoundTag tag, HolderLookup.Provider provider) {
            this.input.readFromNBT(provider, tag.getCompound("input"));
            this.output.readFromNBT(provider, tag.getCompound("output"));
        }

        @SuppressWarnings("unused")
        public int getCapacity() { return input.getCapacity(); }
    }
}
