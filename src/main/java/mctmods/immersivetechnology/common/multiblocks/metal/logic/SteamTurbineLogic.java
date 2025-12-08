package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import blusunrize.immersiveengineering.api.ApiUtils;
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
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.SteamTurbineRecipe;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.SteamTurbineShape;
import mctmods.immersivetechnology.common.multiblocks.metal.process.RotationInertiaProcess;
import mctmods.immersivetechnology.common.fluids.helper.ITArrayFluidHandler;
import mctmods.immersivetechnology.common.fluids.helper.ITMarkableFluidTank;
import mctmods.immersivetechnology.common.util.multiblock.PoIJSONSchema;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.lib.ITSound;
import mctmods.immersivetechnology.core.registration.ITSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public class SteamTurbineLogic implements IMultiblockLogic<SteamTurbineLogic.State>, IServerTickableComponent<SteamTurbineLogic.State>, IClientTickableComponent<SteamTurbineLogic.State>, ITPressurizedFluidOutput<SteamTurbineLogic.State> {
    public static final int TANK_CAPACITY = 12 * FluidType.BUCKET_VOLUME;
    private static final double BASE_MASS = 10;
    private static final double DRIVE_TORQUE = 30;
    private static final double FRICTION = 60;
    private static final int MAX_SPEED = MechanicalCapabilities.MAX_RPM;
    private static final List<PoIJSONSchema> RAW_POIS = ImmutableList.copyOf(SteamTurbineShape.DATA.pointsOfInterest);

    public static final BlockPos REDSTONE_POI = getPosList("redstone").get(0);
    public static final BlockPos RUNNING_SOUND_POI = getPosList("sound_running").get(0);
    public static final BlockPos SMOKE_POI = getPosList("smoke").get(0);
    public static final CapabilityPosition INPUT_FLUID_POI = new CapabilityPosition(getPosList("fluid_input").get(0), getFacing("fluid_input"));
    public static final CapabilityPosition OUTPUT_FLUID_POI = new CapabilityPosition(getPosList("fluid_output").get(0), getFacing("fluid_output"));
    public static final CapabilityPosition ROTATIONAL_OUTPUT_POI = new CapabilityPosition(getPosList("mech_output").get(0), getFacing("mech_output"));
    public static final List<BlockPos> FLUID_OUTPUT_POIS = getPosList("fluid_output");
    private static final RelativeBlockFace OUTPUT_FACING = getFacing("fluid_output");

    private static List<BlockPos> getPosList(String name) { return RAW_POIS.stream().filter(poi -> poi.name.equals(name)).map(poi -> new BlockPos(poi.pos[0], poi.pos[1], poi.pos[2])).collect(ImmutableList.toImmutableList()); }
    private static RelativeBlockFace getFacing(String name) {
        List<RelativeBlockFace> facings = RAW_POIS.stream().filter(poi -> poi.name.equals(name)).flatMap(poi -> poi.relativeFaces.stream()).distinct().toList();
        if (facings.size() != 1) { throw new RuntimeException("Inconsistent facings for POI: " + name); }
        return facings.get(0);
    }

    @Override
    public List<BlockPos> getOutputPositions() { return FLUID_OUTPUT_POIS; }

    @Override
    public Direction getOutputDirection(IMultiblockContext<State> ctx) { return ctx.getLevel().toAbsolute(OUTPUT_FACING); }

    @Override
    public List<ITMarkableFluidTank> getOutputTanks(State state) { return ImmutableList.of(state.tanks.output); }

    @Override
    public List<CapabilityReference<IFluidHandler>> getFluidOutputs(State state) { return ImmutableList.of(state.fluidOutput); }

    @Override
    public void tickClient(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        boolean targetActive = state.active || state.speed > 0;
        float targetLevel = ITLib.remapRange(0, state.effectiveMaxSpeed, 0.5f, 1.0f, state.speed);
        if (state.currentLevel == 0f) { state.currentLevel = targetLevel; } else { state.currentLevel = state.currentLevel * 0.9f + targetLevel * 0.1f; }
        float targetPitch = ITLib.remapRange(0, state.effectiveMaxSpeed, 0.5f, 1.5f, state.speed);
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
            state.isSoundPlaying = ITSound.startSound(
                    () -> state.active || state.speed > 0,
                    ctx.isValid(),
                    soundPos,
                    ITSounds.steamTurbine,
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
            Direction facing = ctx.getLevel().getOrientation().front();
            BlockPos outputAbs = ctx.getLevel().toAbsolute(SMOKE_POI);
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
                Level level = ctx.getLevel().getRawLevel();
                level.addAlwaysVisibleParticle(new ColoredSmoke(r, g, b), smokePos.x, smokePos.y, smokePos.z, velX, velY, velZ);
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody") @Override
    public void tickServer(IMultiblockContext<State> ctx) {
        pumpOutputs(ctx);
        State state = ctx.getState();
        boolean previouslyActive = state.active;
        int previousSpeed = state.speed;
        boolean currentlyEnabled = state.rsState.isEnabled(ctx);
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
            state.inertia = new RotationInertiaProcess(BASE_MASS + state.connectedMass, DRIVE_TORQUE, FRICTION + state.connectedFriction);
        }
        boolean canRun = currentlyEnabled && hasConsumer;
        boolean prevBurnRemaining = state.burnRemaining > 0;
        if (!canRun) {
            state.burnRemaining = 0;
            state.speed = Math.max(0, state.speed - state.inertia.getSpeedDownRate());
        } else {
            if (state.burnRemaining > 0) {
                state.burnRemaining--;
                state.speed = Math.min(effectiveMax, state.speed + state.inertia.getSpeedUpRate());
                state.active = true;
            } else {
                FluidStack fluid = state.tanks.input.getFluid();
                SteamTurbineRecipe recipe = state.recipeGetter.apply(ctx.getLevel().getRawLevel(), fluid);
                if (recipe != null && fluid.getAmount() >= recipe.input.getAmount()) {
                    state.tanks.input.drain(recipe.input.getAmount(), FluidAction.EXECUTE);
                    if (recipe.fluidOutput != null) {
                        int filled = state.tanks.output.fill(recipe.fluidOutput, FluidAction.EXECUTE);
                        if (filled < recipe.fluidOutput.getAmount()) {}
                    }
                    state.burnRemaining = recipe.getTotalProcessTime() - 1;
                    state.speed = Math.min(effectiveMax, state.speed + state.inertia.getSpeedUpRate());
                    state.active = true;
                } else { state.speed = Math.max(0, state.speed - state.inertia.getSpeedDownRate()); }
            }
        }
        if (state.pressureReleaseCooldown > 0) { state.pressureReleaseCooldown--; }
        boolean triggerRelease = !state.wasEnabled && currentlyEnabled;
        if (!prevBurnRemaining && state.burnRemaining > 0) { triggerRelease = true; }
        if (triggerRelease && state.pressureReleaseCooldown <= 0) {
            BlockPos soundPos = ctx.getLevel().toAbsolute(RUNNING_SOUND_POI);
            level.playSound(null, soundPos, ITSounds.pressure_release.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            state.pressureReleaseCooldown = 200;
        }
        state.wasEnabled = currentlyEnabled;
        if (previouslyActive != state.active || state.speed % 5 == 0 || previousSpeed != state.speed) {
            ctx.markMasterDirty();
            ctx.requestMasterBESync();
        }
    }

    private static double particleXZSpeed() { return ApiUtils.RANDOM.nextDouble(-0.015625, 0.015625); }

    @Override
    public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) {
        State state = ctx.getState();
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

    @Override
    public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override
    public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return SteamTurbineShape.GETTER; }

    public static class State implements IMultiblockState, ITDisplayContext {
        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        public final SteamTurbineTank tanks;
        public final StoredCapability<IFluidHandler> fluidCap;
        public final StoredCapability<IFluidHandler> fluidCapExhaust;
        public final CapabilityReference<IFluidHandler> fluidOutput;
        private final BiFunction<Level, FluidStack, SteamTurbineRecipe> recipeGetter;
        public int speed = 0;
        public boolean active = false;
        private int burnRemaining = 0;
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

        public State(IInitialMultiblockContext<State> ctx) {
            Runnable markDirty = ctx.getMarkDirtyRunnable();
            Runnable sync = ctx.getSyncRunnable();
            Runnable onChanged = () -> { markDirty.run(); sync.run(); };
            this.tanks = new SteamTurbineTank(v -> onChanged.run());
            this.fluidCap = new StoredCapability<>(new ITArrayFluidHandler(tanks.input, false, true, onChanged));
            this.fluidCapExhaust = new StoredCapability<>(new ITArrayFluidHandler(tanks.output, true, false, onChanged));
            this.recipeGetter = CachedRecipe.cached(SteamTurbineRecipe::findRecipe);
            MultiblockFace outputMBFace = new MultiblockFace(OUTPUT_FACING, FLUID_OUTPUT_POIS.get(0));
            CapabilityPosition oppCp = CapabilityPosition.opposing(outputMBFace);
            MultiblockFace oppMbf = new MultiblockFace(oppCp.side(), oppCp.posInMultiblock());
            this.fluidOutput = ctx.getCapabilityAt(ForgeCapabilities.FLUID_HANDLER, oppMbf);
            this.inertia = new RotationInertiaProcess(BASE_MASS, DRIVE_TORQUE, FRICTION);
        }

        @Override
        public void writeSaveNBT(CompoundTag nbt) {
            nbt.putInt("speed", speed);
            nbt.putBoolean("active", active);
            nbt.putInt("burnRemaining", burnRemaining);
            nbt.put("tanks", tanks.toNBT());
            nbt.putInt("pressureReleaseCooldown", pressureReleaseCooldown);
            nbt.putBoolean("wasEnabled", wasEnabled);
            nbt.putInt("effectiveMaxSpeed", effectiveMaxSpeed);
        }

        @Override
        public void readSaveNBT(CompoundTag nbt) {
            speed = nbt.getInt("speed");
            active = nbt.getBoolean("active");
            burnRemaining = nbt.getInt("burnRemaining");
            tanks.readNBT(nbt.getCompound("tanks"));
            pressureReleaseCooldown = nbt.getInt("pressureReleaseCooldown");
            wasEnabled = nbt.getBoolean("wasEnabled");
            effectiveMaxSpeed = nbt.getInt("effectiveMaxSpeed");
        }

        @Override
        public void writeSyncNBT(CompoundTag nbt) {
            CompoundTag display = new CompoundTag();
            writeDisplaySyncNBT(display);
            nbt.put("display", display);
        }

        @Override
        public void readSyncNBT(CompoundTag nbt) {
            if (nbt.contains("display", Tag.TAG_COMPOUND)) { readDisplaySyncNBT(nbt.getCompound("display")); }
        }

        @Override
        public boolean isActive() { return active; }

        @Override
        public IItemHandlerModifiable getInventory() { return null; }

        @Override
        public IFluidTank[] getInternalTanks() { return new IFluidTank[]{tanks.input, tanks.output}; }

        @Override
        public void writeDisplaySyncNBT(CompoundTag nbt) {
            nbt.putBoolean("active", active);
            nbt.putInt("speed", speed);
            nbt.put("tanks", tanks.toNBT());
            nbt.putInt("effectiveMaxSpeed", effectiveMaxSpeed);
        }

        @Override
        public void readDisplaySyncNBT(CompoundTag nbt) {
            boolean oldActive = active;
            active = nbt.getBoolean("active");
            speed = nbt.getInt("speed");
            tanks.readNBT(nbt.getCompound("tanks"));
            effectiveMaxSpeed = nbt.getInt("effectiveMaxSpeed");
            if (active && !oldActive) { animation_fanFadeIn = 80; }
        }
    }

    public record SteamTurbineTank(ITMarkableFluidTank input, ITMarkableFluidTank output) {
        public SteamTurbineTank(Consumer<Void> markDirty) {
            this(new ITMarkableFluidTank(TANK_CAPACITY, markDirty), new ITMarkableFluidTank(TANK_CAPACITY, markDirty));
        }

        public static SteamTurbineTank makeClient() { return new SteamTurbineTank(v -> {}); }

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
        public int getCapacity() { return TANK_CAPACITY; }
    }
}
