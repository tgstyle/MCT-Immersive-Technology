package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import mctmods.immersivetechnology.client.util.ClientUtils;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.SteamTurbineRecipe;
import mctmods.immersivetechnology.core.ServerConfig;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.Sounds;
import mctmods.immersivetechnology.core.util.IDisplaySyncState;
import mctmods.immersivetechnology.core.util.MechanicalLoad;
import mctmods.immersivetechnology.core.util.TurbineProvider;

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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.IItemHandlerModifiable;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

public class SteamTurbineLogic implements IMultiblockLogic<SteamTurbineLogic.State>, IServerTickableComponent<SteamTurbineLogic.State>, IClientTickableComponent<SteamTurbineLogic.State>, IFluidOutputPump<SteamTurbineLogic.State> {
    public static BlockPos REDSTONE_POI;
    public static BlockPos RUNNING_SOUND_POI;
    public static BlockPos SMOKE_POI;
    public static List<BlockPos> INPUT_FLUID_POIS;
    public static List<BlockPos> OUTPUT_FLUID_POIS;
    public static List<BlockPos> MECHANICAL_OUTPUT_POIS;
    public static CapabilityPosition INPUT_FLUID_POI;
    public static CapabilityPosition OUTPUT_FLUID_POI;
    public static CapabilityPosition MECHANICAL_OUTPUT_POI;
    private static RelativeBlockFace OUTPUT_FACING;

    static { ITShapes.readPois("steam_turbine", SteamTurbineLogic::loadPois); }

    private static double baseMass() { return ServerConfig.steamTurbineBaseMass; }

    private static double driveTorque() { return ServerConfig.steamTurbineDriveTorque; }

    private static double friction() { return ServerConfig.steamTurbineFriction; }

    private static int maxSpeed() { return (int) (MechanicalCapabilities.maxRpm() * ServerConfig.steamTurbineMaxSpeedFactor); }

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
        float targetLevel = Reference.remapRange(0, state.effectiveMaxSpeed, 0.5f, 1.0f, state.speed);
        if (state.currentLevel == 0f) { state.currentLevel = targetLevel; }
        else { state.currentLevel = state.currentLevel * 0.9f + targetLevel * 0.1f; }
        float targetPitch = Reference.remapRange(0, state.effectiveMaxSpeed, 0.5f, 1.5f, state.speed);
        if (state.currentPitch == 0f) { state.currentPitch = targetPitch; }
        else { state.currentPitch = state.currentPitch * 0.95f + targetPitch * 0.05f; }
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
        Vec3 soundPos = ctx.getLevel().toAbsolute(Vec3.atCenterOf(RUNNING_SOUND_POI));
        boolean targetActive = state.active || state.speed > 0;
        if (targetActive && ClientUtils.attenuated(soundPos, 32, 11 * (state.currentLevel - 0.5f)) > 0.01f && !state.isSoundPlaying.getAsBoolean()) {
            state.isSoundPlaying = MachineSound.startSound(() -> state.active || state.speed > 0, ctx.isValid(), soundPos, Sounds.steamTurbine, () -> ClientUtils.attenuated(soundPos, 32, 11 * (state.currentLevel - 0.5f)), () -> state.currentPitch);
        }
        if (state.active && ctx.getLevel().shouldTickModulo(2) && !isOutputConnected(ctx, 0)) {
            float normSpeed = Math.max(0f, Reference.remapRange(100, state.effectiveMaxSpeed, 0f, 1f, state.speed));
            ClientUtils.exhaust(ctx.getLevel().getRawLevel(), ctx.getLevel().toAbsolute(SMOKE_POI), ctx.getLevel().getOrientation().front(), normSpeed, state.tanks.output().getFluid());
        }
    }

    @Override public void tickServer(IMultiblockContext<State> ctx) {
        pumpOutputs(ctx);
        State state = ctx.getState();
        boolean previouslyActive = state.active;
        int previousSpeed = state.speed;
        boolean currentlyEnabled = state.rsState.isEnabled(ctx);
        Level level = ctx.getLevel().getRawLevel();
        Direction outputFacing = ctx.getLevel().getOrientation().front();
        MechanicalLoad load = MechanicalLoad.at(level, ctx.getLevel().toAbsolute(MECHANICAL_OUTPUT_POIS.get(0)).relative(outputFacing), outputFacing.getOpposite());
        int effectiveMax = load.limit(maxSpeed());
        state.effectiveMaxSpeed = effectiveMax;
        if (load.mass() != state.connectedMass || load.friction() != state.connectedFriction) {
            state.connectedMass = load.mass();
            state.connectedFriction = load.friction();
            state.inertia = new RotationInertiaProcess(baseMass() + state.connectedMass, driveTorque(), friction() + state.connectedFriction, effectiveMax);
        }
        boolean canRun = currentlyEnabled && load.present();
        float ratio = canRun ? consume(state, level) : 0f;
        state.effectiveRatio = state.effectiveRatio * 0.9f + ratio * 0.1f;
        state.accumDelta += state.inertia.getAlpha(canRun ? state.effectiveRatio : 0f, state.speed);
        int delta = (int) Math.round(state.accumDelta);
        state.accumDelta -= delta;
        state.speed += delta;
        if (state.speed > effectiveMax) { state.speed = effectiveMax; }
        if (state.speed < 0) { state.speed = 0; }
        state.active = state.effectiveRatio > 0.001f;
        if (state.pressureReleaseCooldown > 0) { state.pressureReleaseCooldown--; }
        boolean triggerRelease = (!previouslyActive && state.active) || (!state.wasEnabled && currentlyEnabled);
        if (triggerRelease && state.pressureReleaseCooldown <= 0) {
            level.playSound(null, ctx.getLevel().toAbsolute(RUNNING_SOUND_POI), Sounds.pressure_release.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            state.pressureReleaseCooldown = 200;
        }
        state.wasEnabled = currentlyEnabled;
        int newComparatorValue = state.effectiveMaxSpeed > 0 ? (15 * state.speed) / state.effectiveMaxSpeed : 0;
        boolean comparatorChanged = newComparatorValue != state.lastComparatorValue;
        if (comparatorChanged) {
            ctx.setComparatorOutputFor(REDSTONE_POI, newComparatorValue);
            state.lastComparatorValue = newComparatorValue;
        }
        if (previouslyActive != state.active || state.speed % 5 == 0 || previousSpeed != state.speed || comparatorChanged) {
            ctx.markMasterDirty();
            ctx.requestMasterBESync();
        }
    }

    private static float consume(State state, Level level) {
        state.currentTorque = 1.0f;
        FluidStack fluid = state.tanks.input().getFluid();
        if (fluid.getAmount() <= 0) { return 0f; }
        SteamTurbineRecipe recipe = state.recipeGetter.apply(level, fluid);
        if (recipe == null) { return 0f; }
        state.currentTorque = recipe.torque;
        float fluidPerTick = (float) recipe.input.getAmount() / recipe.getTotalProcessTime();
        state.accumConsume += fluidPerTick;
        int toDrain = (int) state.accumConsume;
        if (toDrain <= 0) { return 0f; }
        int drained = state.tanks.input().drain(toDrain, FluidAction.EXECUTE).getAmount();
        state.accumConsume -= drained;
        float ratio = drained / fluidPerTick;
        if (recipe.fluidOutput != null) {
            state.outAccum += ratio * ((float) recipe.fluidOutput.getAmount() / recipe.getTotalProcessTime());
            if (state.outAccum >= 1) {
                FluidStack out = recipe.fluidOutput.copy();
                out.setAmount((int) state.outAccum);
                state.outAccum -= state.tanks.output().fill(out, FluidAction.EXECUTE);
            }
        }
        return ratio;
    }

    @Override public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) {
        State state = ctx.getState();
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            if (position.equals(INPUT_FLUID_POI)) { return state.fluidCap.cast(ctx); }
            if (position.equals(OUTPUT_FLUID_POI)) { return state.fluidCapExhaust.cast(ctx); }
        }
        if (cap == MechanicalCapabilities.MECHANICAL_PROVIDER_CAPABILITY && position.equals(MECHANICAL_OUTPUT_POI)) { return LazyOptional.of(() -> state.mechanicalProvider).cast(); }
        return LazyOptional.empty();
    }

    @Override public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return ITShapes.get("steam_turbine").getter; }

    public static class State implements IDisplaySyncState, IDataReloadAware {
        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        public final TankPair tanks;
        public final StoredCapability<IFluidHandler> fluidCap;
        public final StoredCapability<IFluidHandler> fluidCapExhaust;
        private final BiFunction<Level, FluidStack, SteamTurbineRecipe> recipeGetter = RecipeCache.cached(SteamTurbineRecipe::findRecipe);
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
        private int pressureReleaseCooldown = 0;
        private boolean wasEnabled = false;
        public int effectiveMaxSpeed = maxSpeed();
        public int lastComparatorValue = -1;
        private float accumConsume;
        private float outAccum;
        private double accumDelta;
        private float effectiveRatio;
        private RotationInertiaProcess inertia = new RotationInertiaProcess(baseMass(), driveTorque(), friction(), effectiveMaxSpeed);
        private final IMechanicalEnergyProvider mechanicalProvider = new TurbineProvider(() -> speed, () -> currentTorque, SteamTurbineLogic::maxSpeed, SteamTurbineLogic::baseMass, SteamTurbineLogic::driveTorque, SteamTurbineLogic::friction);

        public State(IInitialMultiblockContext<State> ctx) {
            Runnable markDirty = ctx.getMarkDirtyRunnable();
            Runnable sync = ctx.getSyncRunnable();
            Runnable onChanged = () -> {
                markDirty.run();
                sync.run();
            };
            this.tanks = new TankPair(v -> onChanged.run(), ServerConfig.steamTurbineInputTankCapacity, ServerConfig.steamTurbineOutputTankCapacity);
            this.fluidCap = new StoredCapability<>(new MultiTankFluidHandler(tanks.input(), false, true, onChanged));
            this.fluidCapExhaust = new StoredCapability<>(new MultiTankFluidHandler(tanks.output(), true, false, onChanged));
        }

        @Override public void onDataReload(IInitialMultiblockContext<?> context) { lastComparatorValue = -1; }

        @Override public void writeSaveNBT(CompoundTag nbt) {
            rsState.writeSaveNBT(nbt);
            nbt.putInt("speed", speed);
            nbt.putBoolean("active", active);
            nbt.put("tanks", tanks.toNBT());
            nbt.putInt("pressureReleaseCooldown", pressureReleaseCooldown);
            nbt.putBoolean("wasEnabled", wasEnabled);
            nbt.putInt("effectiveMaxSpeed", effectiveMaxSpeed);
            nbt.putFloat("accumConsume", accumConsume);
            nbt.putFloat("outAccum", outAccum);
            nbt.putDouble("accumDelta", accumDelta);
            nbt.putFloat("effectiveRatio", effectiveRatio);
        }

        @Override public void readSaveNBT(CompoundTag nbt) {
            rsState.readSaveNBT(nbt);
            speed = nbt.getInt("speed");
            active = nbt.getBoolean("active");
            tanks.readNBT(nbt.getCompound("tanks"));
            pressureReleaseCooldown = nbt.getInt("pressureReleaseCooldown");
            wasEnabled = nbt.getBoolean("wasEnabled");
            effectiveMaxSpeed = nbt.getInt("effectiveMaxSpeed");
            accumConsume = nbt.getFloat("accumConsume");
            outAccum = nbt.getFloat("outAccum");
            accumDelta = nbt.getDouble("accumDelta");
            effectiveRatio = nbt.getFloat("effectiveRatio");
        }

        @Override public boolean isActive() { return active; }

        @Override public IItemHandlerModifiable getInventory() { return null; }

        @Override public IFluidTank[] getInternalTanks() { return new IFluidTank[]{tanks.input(), tanks.output()}; }

        @Override public void writeDisplaySyncNBT(CompoundTag nbt) {
            nbt.putBoolean("active", active);
            nbt.putInt("speed", speed);
            nbt.put("tanks", tanks.toNBT());
            nbt.putInt("effectiveMaxSpeed", effectiveMaxSpeed);
        }

        @Override public void readDisplaySyncNBT(CompoundTag nbt) {
            boolean oldActive = active;
            active = nbt.getBoolean("active");
            speed = nbt.getInt("speed");
            tanks.readNBT(nbt.getCompound("tanks"));
            effectiveMaxSpeed = nbt.getInt("effectiveMaxSpeed");
            if (active && !oldActive) { animation_fanFadeIn = 80; }
        }

        @Override public void addDisplayLines(Level level, DisplayLines lines) {
            lines.rpm(speed, effectiveMaxSpeed);
            if (tanks.input().getFluid().isEmpty()) { lines.fuelEmpty(); }
        }
    }

    private static void loadPois(List<PoIJSONSchema> pois) {
        REDSTONE_POI = MultiblockPOIHelper.getPosList(pois, "redstone0").get(0);
        RUNNING_SOUND_POI = MultiblockPOIHelper.getPosList(pois, "sound_running0").get(0);
        SMOKE_POI = MultiblockPOIHelper.getPosList(pois, "smoke0").get(0);
        INPUT_FLUID_POIS = MultiblockPOIHelper.getPosList(pois, "fluid_input0");
        OUTPUT_FLUID_POIS = MultiblockPOIHelper.getPosList(pois, "fluid_output0");
        MECHANICAL_OUTPUT_POIS = MultiblockPOIHelper.getPosList(pois, "mechanical_output0");
        INPUT_FLUID_POI = MultiblockPOIHelper.getCapabilityPosition(pois, "fluid_input0");
        OUTPUT_FLUID_POI = MultiblockPOIHelper.getCapabilityPosition(pois, "fluid_output0");
        MECHANICAL_OUTPUT_POI = MultiblockPOIHelper.getCapabilityPosition(pois, "mechanical_output0");
        OUTPUT_FACING = MultiblockPOIHelper.getFacing(pois, "fluid_output0");
    }
}
