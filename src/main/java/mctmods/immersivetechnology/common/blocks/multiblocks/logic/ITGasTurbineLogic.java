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
import mctmods.immersivetechnology.client.particles.ColoredSmokeData;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITSlotwiseItemHandler;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.GasTurbineRecipe;
import mctmods.immersivetechnology.common.blocks.multiblocks.shapes.GasTurbineShape;
import mctmods.immersivetechnology.common.fluids.helper.ITArrayFluidHandler;
import mctmods.immersivetechnology.common.fluids.helper.ITMarkableFluidTank;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.lib.ITMultiblockSound;
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
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public class ITGasTurbineLogic implements IMultiblockLogic<ITGasTurbineLogic.State>, IServerTickableComponent<ITGasTurbineLogic.State>, IClientTickableComponent<ITGasTurbineLogic.State> {
    private static final List<BlockPos> FLUID_POS1 = List.of(new BlockPos(2, 1, 7));
    private static final List<BlockPos> FLUID_POS2 = List.of(new BlockPos(1, 0, 1));

    private static final Set<CapabilityPosition> ENERGY_INPUTS_HV = Set.of(new CapabilityPosition(2, 0, 5, RelativeBlockFace.LEFT));
    private static final Set<CapabilityPosition> ENERGY_INPUTS_MV = Set.of(new CapabilityPosition(0, 0, 5, RelativeBlockFace.RIGHT));

    public static final BlockPos REDSTONE_POS = new BlockPos(0, 1, 7);

    public static final int TANK_CAPACITY = 12 * FluidType.BUCKET_VOLUME;
    private static final int ENERGY_CAPACITY = 8192;
    private static final int ENERGY_CAPACITY_MV = 2048;
    private static final int ELECTRIC_STARTER_CONSUMPTION = 4096;
    private static final int SPARKPLUG_CONSUMPTION = 1024;

    private static final CapabilityPosition FLUID_OUTPUT_POS = new CapabilityPosition(1, 0, 1, RelativeBlockFace.FRONT);

    @Override
    public void tickClient(IMultiblockContext<State> ctx) {
        final State state = ctx.getState();
        final Level level = ctx.getLevel().getRawLevel();
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) { return; }
        float minSpeedForSound = state.maxSpeed / 4f;
        float targetLevel = Math.max(0.55f, ITLib.remapRange(minSpeedForSound, state.maxSpeed, 0.55f, 1.0f, state.speed));
        if (state.currentLevel == 0f) { state.currentLevel = targetLevel; }
        state.currentLevel = state.currentLevel * 0.9f + targetLevel * 0.1f;
        float smoothedLevel = state.currentLevel;
        float targetPitch = Math.max(0.5f, ITLib.remapRange(state.maxSpeed / 4f, state.maxSpeed, 0.5f, 1.5f, state.speed));
        if (state.currentPitch == 0f) { state.currentPitch = targetPitch; }
        if (state.currentPitch < 0.5f) { state.currentPitch = 0.5f; }
        state.currentPitch = state.currentPitch * 0.95f + targetPitch * 0.05f;
        if (state.active || state.animation_fanFadeIn > 0 || state.animation_fanFadeOut > 0) {
            float currentBase = (state.speed / (float)state.maxSpeed) * 72f;
            float step = state.active ? currentBase : 0f;
            if (state.animation_fanFadeIn > 0) {
                step -= (state.animation_fanFadeIn / 80f) * currentBase;
                state.animation_fanFadeIn--;
            }
            if (state.animation_fanFadeOut > 0) {
                step += (state.animation_fanFadeOut / (float)state.animation_fanFadeOutMax) * currentBase;
                state.animation_fanFadeOut--;
            }
            state.animation_fanRotationStep = step;
            state.animation_fanRotation += step;
            state.animation_fanRotation %= 360;
        }
        Vec3 runningPos = ctx.getLevel().toAbsolute(new Vec3(1.5, 1.5, 3.5));
        Vec3 starterPos = ctx.getLevel().toAbsolute(new Vec3(1.5, 0.5, 1.5));
        Vec3 arcPos = ctx.getLevel().toAbsolute(new Vec3(0.5, 1.5, 1.5));
        Vec3 ignitionPos = ctx.getLevel().toAbsolute(new Vec3(1.5, 1.5, 1.5));
        if (state.speed >= (state.maxSpeed / 4)) {
            if (!state.runningSoundPlaying.getAsBoolean()) {
                state.runningSoundId++;
                int thisId = state.runningSoundId;
                state.runningSoundPlaying = ITMultiblockSound.startSound(
                        () -> state.speed >= (state.maxSpeed / 4) && state.runningSoundId == thisId,
                        ctx.isValid(),
                        runningPos,
                        ITSounds.gasRunning,
                        () -> {
                            LocalPlayer p = Minecraft.getInstance().player;
                            if (p == null) return 0f;
                            float a = (float) Math.max(p.distanceToSqr(runningPos) / 32, 1);
                            return (11 * (smoothedLevel - 0.5f)) / a;
                        },
                        () -> state.currentPitch
                );
            }
        }
        if (state.starterRunning) {
            if (!state.starterSoundPlaying.getAsBoolean()) {
                state.starterSoundId++;
                int thisId = state.starterSoundId;
                state.starterSoundPlaying = ITMultiblockSound.startSound(
                        () -> state.starterRunning && state.starterSoundId == thisId,
                        ctx.isValid(),
                        starterPos,
                        ITSounds.gasStarter,
                        () -> {
                            LocalPlayer p = Minecraft.getInstance().player;
                            if (p == null) return 0f;
                            float a = (float) Math.max(p.distanceToSqr(starterPos) / 64, 1);
                            return Math.min(smoothedLevel / a, 0.2f);
                        },
                        () -> 1f
                );
            }
            if (state.speed >= state.maxSpeed / 4) {
                if (!state.arcSoundPlaying.getAsBoolean()) {
                    state.arcSoundId++;
                    int thisId = state.arcSoundId;
                    state.arcSoundPlaying = ITMultiblockSound.startSound(
                            () -> state.starterRunning && state.speed >= state.maxSpeed / 4 && state.arcSoundId == thisId,
                            ctx.isValid(),
                            arcPos,
                            ITSounds.gasArc,
                            () -> {
                                LocalPlayer p = Minecraft.getInstance().player;
                                if (p == null) return 0f;
                                float a = (float) Math.max(p.distanceToSqr(arcPos) / 64, 1);
                                return Math.min(smoothedLevel / a, 0.2f);
                            },
                            () -> 1f
                    );
                }
            }
        }
        if (state.ignited && !state.lastIgnited && state.speed < state.maxSpeed / 2) {
            state.lastIgnited = true;
            float ignitionAtt = (float) Math.max(player.distanceToSqr(ignitionPos) / 64, 1);
            level.playLocalSound(ignitionPos.x, ignitionPos.y, ignitionPos.z, ITSounds.gasSpark.get(), SoundSource.BLOCKS, 1 / ignitionAtt, 1, false);
            state.igniteDelay = 3;
        }
        else { state.lastIgnited = state.ignited; }
        if (state.igniteDelay > 0) {
            state.igniteDelay--;
            if (state.igniteDelay == 0 && state.starterRunning) {
                float ignitionAtt = (float) Math.max(player.distanceToSqr(starterPos) / 64, 1);
                level.playLocalSound(starterPos.x, starterPos.y, starterPos.z, ITSounds.gasIgnite.get(), SoundSource.BLOCKS, 1 / ignitionAtt, 1, false);
            }
        }
        if (state.starterRunning && state.speed >= state.maxSpeed / 4) {
            if (level.random.nextInt(40) == 0) return;
            Vec3 particlePos = ctx.getLevel().toAbsolute(new Vec3(1.5, 0.5, 1.5));
            double distSq = player.distanceToSqr(particlePos);
            if (distSq > 64 * 64) return;
            double px = particlePos.x + 2 - level.random.nextFloat() * 3;
            double py = particlePos.y + 0.5;
            double pz = particlePos.z + 2 - level.random.nextFloat() * 3;
            level.addParticle(ParticleTypes.SMOKE, px, py, pz, 0, 0.02, 0);
        }
        if (state.active && ctx.getLevel().shouldTickModulo(2)) {
            Direction facing = ctx.getLevel().getOrientation().front();
            BlockPos outputRel = new BlockPos(1, 0, 1);
            BlockPos outputAbs = ctx.getLevel().toAbsolute(outputRel);
            BlockPos adjacentAbs = outputAbs.relative(facing);
            boolean connected = state.fluidOutput.isPresent();
            if (!connected) {
                Vec3 smokePos = new Vec3(adjacentAbs.getX() + 0.5, adjacentAbs.getY() + 0.5, adjacentAbs.getZ() + 0.5);
                double velX = facing.getStepX() * 0.125 + particleXZSpeed();
                double velY = facing.getStepY() * 0.1 + 0.0625;
                double velZ = facing.getStepZ() * 0.125 + particleXZSpeed();
                FluidStack outFluid = state.tanks.output.getFluid();
                float r = 0.5F, g = 0.5F, b = 0.5F;
                if (!outFluid.isEmpty()) {
                    int tint = IClientFluidTypeExtensions.of(outFluid.getFluid()).getTintColor(outFluid);
                    r = ((tint >> 16) & 0xFF) / 255f;
                    g = ((tint >> 8) & 0xFF) / 255f;
                    b = (tint & 0xFF) / 255f;
                }

                Level level2 = ctx.getLevel().getRawLevel();
                level2.addAlwaysVisibleParticle(
                        new ColoredSmokeData(r, g, b),
                        smokePos.x, smokePos.y, smokePos.z,
                        velX, velY, velZ
                );
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public void tickServer(IMultiblockContext<State> ctx) {
        final State state = ctx.getState();
        boolean wasActive = state.active;
        boolean wasStall = state.stall;
        state.active = false;

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

        if (!isRSEnabled) {
            state.isShutdown = true;
            state.ignitionGracePeriod = 0;
            state.burnRemaining = 0;
            state.stall = false;
        }

        if (state.speed < state.maxSpeed / 4) {
            if (isRSEnabled && !state.isShutdown) {
                if (state.ignitionGracePeriod > 0) { state.ignitionGracePeriod--; }
                if (state.starterRunning) { speedUp(state); }
                else { speedDown(state); }
            }
            else { speedDown(state); }
        }
        else {
            if (state.isShutdown) { speedDown(state); }
            else {
                if (state.starterRunning) {
                    if (canIgnite(state)) {
                        state.stall = true;
                        if (!wasStall) { ignite(state, ctx); }
                        else { state.ignitionGracePeriod = 60; }
                        state.speed = state.maxSpeed / 4;
                        state.active = true;
                        if (state.ignitionGracePeriod > 0) state.ignitionGracePeriod--;
                    }
                    else {
                        state.stall = false;
                        speedDown(state);
                    }
                }
                else {
                    state.stall = false;
                    if (state.burnRemaining > 0 && (state.ignited || canIgnite(state))) {
                        state.burnRemaining--;
                        if (!state.ignited) { ignite(state, ctx); }
                        speedUp(state);
                    }
                    else if (state.ignited || canIgnite(state)) {
                        FluidStack fluid = state.tanks.input.getFluid();
                        GasTurbineRecipe recipe = state.recipeGetter.apply(ctx.getLevel().getRawLevel(), fluid);
                        if (recipe != null && fluid.getAmount() >= recipe.inputAmount) {
                            state.tanks.input.drain(recipe.inputAmount, FluidAction.EXECUTE);
                            if (recipe.fluidOutput != null) {
                                int filled = state.tanks.output.fill(recipe.fluidOutput, FluidAction.EXECUTE);
                                if (filled < recipe.fluidOutput.getAmount()) {
                                    //Excess discarded, operation continues
                                }
                            }
                            state.burnRemaining = recipe.getTotalProcessTime() - 1;
                            if (!state.ignited) ignite(state, ctx);
                            speedUp(state);
                            ctx.markMasterDirty();
                        }
                        else { speedDown(state); }
                    }
                    else { speedDown(state); }
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
                    if (drained > 0) changed = true;
                }
            }
        }

        if (wasActive != state.active || wasStall != state.stall || state.speed % 20 == 0 || changed) {
            ctx.markMasterDirty();
            ctx.requestMasterBESync();
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) {
        final State state = ctx.getState();
        if (cap == ForgeCapabilities.ENERGY) {
            if (ENERGY_INPUTS_HV.contains(position)) { return state.energyCapHV.cast(ctx); }
            if (ENERGY_INPUTS_MV.contains(position)) { return state.energyCapMV.cast(ctx); }
        }
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            if (position.side() == null || (position.side() == RelativeBlockFace.BACK && FLUID_POS1.contains(position.posInMultiblock()))) { return state.fluidCap.cast(ctx); }
            else if (position.side() == RelativeBlockFace.FRONT && FLUID_POS2.contains(position.posInMultiblock())) { return state.fluidCapExhaust.cast(ctx); }
        }
        return LazyOptional.empty();
    }

    @Override
    public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override
    public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return GasTurbineShape.GETTER; }

    public ITSlotwiseItemHandler getInventory() { return null; }

    public GasTurbineTank getTanks() { return null; }

    private void speedUp(State state) {
        state.speed = Math.min(state.maxSpeed, state.speed + state.speedGainPerTick);
        state.active = true;
    }

    private void speedDown(State state) {
        if (state.ignitionGracePeriod > 0) { state.ignitionGracePeriod--; }
        state.speed = Math.max(0, state.speed - state.speedLossPerTick);
    }

    private boolean canIgnite(State state) { return SPARKPLUG_CONSUMPTION <= state.energyStorageMV.getEnergyStored(); }

    private void ignite(State state, IMultiblockContext<State> ctx) {
        state.energyStorageMV.extractEnergy(SPARKPLUG_CONSUMPTION, false);
        state.ignited = true;
        state.ignitionGracePeriod = 60;
        ctx.requestMasterBESync();
    }

    private static double particleXZSpeed() { return ApiUtils.RANDOM.nextDouble(-0.015625, 0.015625); }

    public static class State implements IMultiblockState {
        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        public final GasTurbineTank tanks;
        public final StoredCapability<IFluidHandler> fluidCap;
        public final StoredCapability<IFluidHandler> fluidCapExhaust;
        public final StoredCapability<IEnergyStorage> energyCapHV;
        public final StoredCapability<IEnergyStorage> energyCapMV;
        public final AveragingEnergyStorage energyStorageHV;
        public final AveragingEnergyStorage energyStorageMV;
        public final CapabilityReference<IFluidHandler> fluidOutput;
        private final BiFunction<Level, FluidStack, GasTurbineRecipe> recipeGetter;
        public int maxSpeed = 1800;
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
        private transient int animation_fanFadeOut = 0;
        private transient int animation_fanFadeOutMax = 0;
        private transient float currentLevel = 0f;
        private transient float currentPitch = 0f;
        private BooleanSupplier runningSoundPlaying = () -> false;
        private BooleanSupplier starterSoundPlaying = () -> false;
        private BooleanSupplier arcSoundPlaying = () -> false;
        private transient int runningSoundId = 0;
        private transient int starterSoundId = 0;
        private transient int arcSoundId = 0;
        private boolean lastIgnited = false;
        private transient int igniteDelay = 0;
        private final int speedGainPerTick = 3;
        private final int speedLossPerTick = 6;

        public State(IInitialMultiblockContext<State> ctx) {
            final Runnable markDirty = ctx.getMarkDirtyRunnable();
            final Runnable sync = ctx.getSyncRunnable();
            final Runnable onChanged = () -> { markDirty.run(); sync.run(); };
            this.tanks = new GasTurbineTank(v -> onChanged.run());
            this.fluidCap = new StoredCapability<>(new ITArrayFluidHandler(tanks.input, false, true, onChanged));
            this.fluidCapExhaust = new StoredCapability<>(new ITArrayFluidHandler(tanks.output, true, false, onChanged));
            this.energyStorageHV = new AveragingEnergyStorage(ENERGY_CAPACITY);
            this.energyStorageMV = new AveragingEnergyStorage(ENERGY_CAPACITY_MV);
            this.energyCapHV = new StoredCapability<>(energyStorageHV);
            this.energyCapMV = new StoredCapability<>(energyStorageMV);
            this.recipeGetter = CachedRecipe.cached(GasTurbineRecipe::findFuel);
            CapabilityPosition opposingPos = CapabilityPosition.opposing(new MultiblockFace(FLUID_OUTPUT_POS.side(), FLUID_OUTPUT_POS.posInMultiblock()));
            this.fluidOutput = ctx.getCapabilityAt(ForgeCapabilities.FLUID_HANDLER, new MultiblockFace(opposingPos.side(), opposingPos.posInMultiblock()));
        }

        @Override
        public void writeSaveNBT(CompoundTag nbt) {
            nbt.putInt("speed", speed);
            nbt.putBoolean("active", active);
            nbt.putBoolean("starterRunning", starterRunning);
            nbt.putBoolean("ignited", ignited);
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
            final boolean oldActive = active;
            active = nbt.getBoolean("active");
            starterRunning = nbt.getBoolean("starterRunning");
            ignited = nbt.getBoolean("ignited");
            speed = nbt.getInt("speed");
            isShutdown = nbt.getBoolean("isShutdown");
            stall = nbt.getBoolean("stall");
            tanks.readNBT(nbt.getCompound("tanks"));
            if (active && !oldActive && speed < maxSpeed / 4) { animation_fanFadeIn = 80; }
            else if (!active && oldActive) {
                animation_fanFadeOut = Math.max(0, (int)((speed - (maxSpeed / 4f)) / 6f));
                animation_fanFadeOutMax = animation_fanFadeOut;
            }
        }

        public boolean isActive() { return active; }
    }

    @SuppressWarnings("unused")
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

        public int getCapacity() { return TANK_CAPACITY; }
    }
}
