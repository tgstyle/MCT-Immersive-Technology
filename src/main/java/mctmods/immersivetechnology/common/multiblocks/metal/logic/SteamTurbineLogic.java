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

import com.immersiveconvergence.api.multiblock.blocks.component.IClientTickableComponent;
import com.immersiveconvergence.api.multiblock.blocks.component.IMultiblockComponent;
import com.immersiveconvergence.api.multiblock.blocks.component.IServerTickableComponent;
import com.immersiveconvergence.api.multiblock.blocks.component.RedstoneControl;
import com.immersiveconvergence.api.multiblock.blocks.env.IInitialMultiblockContext;
import com.immersiveconvergence.api.multiblock.blocks.env.IMultiblockContext;
import com.immersiveconvergence.api.multiblock.blocks.logic.IMultiblockLogic;
import com.immersiveconvergence.api.multiblock.blocks.util.*;
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
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
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
import java.util.function.Function;

public class SteamTurbineLogic implements IMultiblockLogic<SteamTurbineLogic.State>, IServerTickableComponent<SteamTurbineLogic.State>, IClientTickableComponent<SteamTurbineLogic.State>, IFluidOutputPump<SteamTurbineLogic.State> {
    public static BlockPos REDSTONE_POI;
    public static BlockPos RUNNING_SOUND_POI;
    public static List<CapabilityPosition> SMOKE_POIS;
    public static List<BlockPos> INPUT_FLUID_POIS;
    public static List<BlockPos> OUTPUT_FLUID_POIS;
    public static List<BlockPos> MECHANICAL_OUTPUT_POIS;
    public static CapabilityPosition INPUT_FLUID_POI;
    public static List<CapabilityPosition> OUTPUT_FLUID_POIS_CAP;
    public static CapabilityPosition MECHANICAL_OUTPUT_POI;
    private static RelativeBlockFace OUTPUT_FACING;

    static { ITShapes.readPois("steam_turbine", SteamTurbineLogic::loadPois); }

    private static double baseMass() { return ServerConfig.steamTurbineBaseMass; }

    private static double driveTorque() { return ServerConfig.steamTurbineDriveTorque; }

    private static double friction() { return ServerConfig.steamTurbineFriction; }

    private static int maxSpeed() { return (int) (MechanicalCapabilities.maxRpm() * ServerConfig.steamTurbineMaxSpeedFactor); }

    @Override public List<BlockPos> getOutputPositions() { return OUTPUT_FLUID_POIS; }

    @Override public Direction getOutputDirection(IMultiblockContext<State> ctx) { return ctx.getLevel().toAbsolute(OUTPUT_FACING); }

    @Override public List<MarkableFluidTank> getOutputTanks(State state) { return Collections.nCopies(OUTPUT_FLUID_POIS_CAP.size(), state.tanks.output()); }

    @Override public List<RelativeBlockFace> getOutputFacings() { return OUTPUT_FLUID_POIS_CAP.stream().map(CapabilityPosition::side).toList(); }

    @Override public boolean isOutputConnected(IMultiblockContext<State> ctx, int index) {
        Direction side = ctx.getLevel().toAbsolute(OUTPUT_FLUID_POIS_CAP.get(index).side());
        if (side == null) { return false; }
        BlockPos targetPos = ctx.getLevel().toAbsolute(OUTPUT_FLUID_POIS.get(index)).relative(side);
        return ctx.getLevel().getRawLevel().getCapability(Capabilities.FluidHandler.BLOCK, targetPos, side.getOpposite()) != null;
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
        if (state.active && ctx.getLevel().shouldTickModulo(2)) {
            float normSpeed = Math.max(0f, Reference.remapRange(100, state.effectiveMaxSpeed, 0f, 1f, state.speed));
            Level level = ctx.getLevel().getRawLevel();
            for (int i = 0; i < SMOKE_POIS.size(); i++) {
                if (isOutputConnected(ctx, i)) { continue; }
                CapabilityPosition smokePoi = SMOKE_POIS.get(i);
                Direction facing = ctx.getLevel().toAbsolute(smokePoi.side());
                if (facing != null) { ClientUtils.exhaust(level, ctx.getLevel().toAbsolute(smokePoi.posInMultiblock()), facing, normSpeed, state.tanks.output().getFluid()); }
            }
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
        MechanicalLoad load = MechanicalLoad.at(level, ctx.getLevel().toAbsolute(MECHANICAL_OUTPUT_POIS.getFirst()).relative(outputFacing), outputFacing.getOpposite());
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
        state.currentTorque = recipe.torque();
        float fluidPerTick = (float) recipe.getInputAmount() / recipe.getTotalProcessTime();
        state.accumConsume += fluidPerTick;
        int toDrain = (int) state.accumConsume;
        if (toDrain <= 0) { return 0f; }
        int drained = state.tanks.input().drain(toDrain, FluidAction.EXECUTE).getAmount();
        state.accumConsume -= drained;
        float ratio = drained / fluidPerTick;
        FluidStack recipeOut = recipe.fluidOutput();
        if (recipeOut != null) {
            state.outAccum += ratio * ((float) recipeOut.getAmount() / recipe.getTotalProcessTime());
            if (state.outAccum >= 1) { state.outAccum -= state.tanks.output().fill(new FluidStack(recipeOut.getFluid(), (int) state.outAccum), FluidAction.EXECUTE); }
        }
        return ratio;
    }

    @Override public void registerCapabilities(IMultiblockComponent.CapabilityRegistrar<State> register) {
        register.register(Capabilities.FluidHandler.BLOCK, (state, position) -> {
            if (INPUT_FLUID_POI.equals(position)) { return state.fluidInputHandler; }
            return OUTPUT_FLUID_POIS_CAP.contains(position) ? state.fluidOutputHandler : null;
        });
        register.register(MechanicalCapabilities.MECHANICAL_PROVIDER, (state, position) -> MECHANICAL_OUTPUT_POI.equals(position) ? state.mechanicalProvider : null);
    }

    @Override public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return ITShapes.get("steam_turbine").getter; }

    public static class State implements IDisplaySyncState, IDataReloadAware {
        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        public final TankPair tanks;
        public final IFluidHandler fluidInputHandler;
        public final IFluidHandler fluidOutputHandler;
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
            Runnable onChanged = () -> { markDirty.run(); sync.run(); };
            this.tanks = new TankPair(v -> onChanged.run(), ServerConfig.steamTurbineInputTankCapacity, ServerConfig.steamTurbineOutputTankCapacity);
            this.fluidInputHandler = new MultiTankFluidHandler(tanks.input(), false, true, onChanged);
            this.fluidOutputHandler = new MultiTankFluidHandler(tanks.output(), true, false, onChanged);
        }

        @Override public void onDataReload(IInitialMultiblockContext<?> context) { lastComparatorValue = -1; }

        @Override public void writeSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            rsState.writeSaveNBT(nbt, provider);
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
            rsState.readSaveNBT(nbt, provider);
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

        @Override public boolean isActive() { return active; }

        @Override public IItemHandlerModifiable getInventory() { return null; }

        @Override public IFluidTank[] getInternalTanks() { return new IFluidTank[]{tanks.input(), tanks.output()}; }

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

        @Override public void addDisplayLines(Level level, DisplayLines lines) {
            lines.rpm(speed, effectiveMaxSpeed);
            if (tanks.input().getFluid().isEmpty()) { lines.fuelEmpty(); }
        }
    }

    private static void loadPois(List<PoIJSONSchema> pois) {
        REDSTONE_POI = MultiblockPOIHelper.getPosList(pois, "redstone0").getFirst();
        RUNNING_SOUND_POI = MultiblockPOIHelper.getPosList(pois, "sound_running0").getFirst();
        SMOKE_POIS = MultiblockPOIHelper.getCapabilityPositions(pois, "smoke0");
        INPUT_FLUID_POIS = MultiblockPOIHelper.getPosList(pois, "fluid_input0");
        OUTPUT_FLUID_POIS = MultiblockPOIHelper.getPosList(pois, "fluid_output0");
        MECHANICAL_OUTPUT_POIS = MultiblockPOIHelper.getPosList(pois, "mechanical_output0");
        INPUT_FLUID_POI = MultiblockPOIHelper.getCapabilityPosition(pois, "fluid_input0");
        OUTPUT_FLUID_POIS_CAP = MultiblockPOIHelper.getCapabilityPositions(pois, "fluid_output0");
        MECHANICAL_OUTPUT_POI = MultiblockPOIHelper.getCapabilityPosition(pois, "mechanical_output0");
        OUTPUT_FACING = OUTPUT_FLUID_POIS_CAP.getFirst().side();
    }
}
