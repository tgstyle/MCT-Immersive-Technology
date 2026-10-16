package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import mctmods.immersivetechnology.client.util.ClientUtils;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.core.ServerConfig;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.Sounds;
import mctmods.immersivetechnology.core.util.IDisplaySyncState;
import mctmods.immersivetechnology.core.util.SyncEnergyStorage;

import com.immersiveconvergence.api.energy.ICEnergyStorageAveraging;
import com.immersiveconvergence.api.multiblock.blocks.component.IClientTickableComponent;
import com.immersiveconvergence.api.multiblock.blocks.component.IMultiblockComponent;
import com.immersiveconvergence.api.multiblock.blocks.component.IServerTickableComponent;
import com.immersiveconvergence.api.multiblock.blocks.env.IInitialMultiblockContext;
import com.immersiveconvergence.api.multiblock.blocks.env.IMultiblockContext;
import com.immersiveconvergence.api.multiblock.blocks.logic.IMultiblockLogic;
import com.immersiveconvergence.api.multiblock.blocks.util.CapabilityPosition;
import com.immersiveconvergence.api.multiblock.blocks.util.RelativeBlockFace;
import com.immersiveconvergence.api.multiblock.blocks.util.ShapeType;
import com.immersiveconvergence.api.capability.IMechanicalEnergyConsumer;
import com.immersiveconvergence.api.capability.IMechanicalEnergyProvider;
import com.immersiveconvergence.api.capability.MechanicalCapabilities;
import com.immersiveconvergence.api.client.MachineSound;
import com.immersiveconvergence.api.integration.DisplayLines;
import com.immersiveconvergence.api.multiblock.IDataReloadAware;
import com.immersiveconvergence.api.multiblock.MultiblockPOIHelper;
import com.immersiveconvergence.api.multiblock.PoIJSONSchema;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.energy.IEnergyStorage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public class AlternatorLogic implements IMultiblockLogic<AlternatorLogic.State>, IServerTickableComponent<AlternatorLogic.State>, IClientTickableComponent<AlternatorLogic.State> {
    public static BlockPos RUNNING_SOUND_POI;
    public static List<BlockPos> COMPARATOR_POSITIONS;
    public static CapabilityPosition MECHANICAL_INPUT_POI;
    private static List<BlockPos> ENERGY_LEFT_POIS;
    private static List<BlockPos> ENERGY_RIGHT_POIS;
    private static RelativeBlockFace ENERGY_LEFT_FACING;
    private static RelativeBlockFace ENERGY_RIGHT_FACING;
    private static RelativeBlockFace MECHANICAL_INPUT_FACING;

    static { ITShapes.readPois("alternator", AlternatorLogic::loadPois); }

    private static final Lazy<IMechanicalEnergyConsumer> MECHANICAL_CONSUMER = Lazy.of(MechanicalEnergyConsumer::new);

    @Override public void tickClient(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        Vec3 soundPos = ctx.getLevel().toAbsolute(Vec3.atCenterOf(RUNNING_SOUND_POI));
        if (state.active && ClientUtils.attenuated(soundPos, 32, 11f) > 0.01f && !state.isSoundPlaying.getAsBoolean()) { state.isSoundPlaying = MachineSound.startSound(() -> state.active, ctx.isValid(), soundPos, Sounds.alternator, () -> ClientUtils.attenuated(soundPos, 32, 11f), () -> Reference.remapRange(0, state.effectiveMaxSpeed, 0.5f, 1.25f, state.speed)); }
    }

    @Override public void tickServer(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        Level level = ctx.getLevel().getRawLevel();
        state.energy.updateAverage();
        int prevEnergy = state.energy.getEnergyStored();
        int prevSpeed = state.speed;
        float prevTorque = state.torqueMultiplier;
        boolean wasActive = state.active;
        state.active = false;
        int turbineSpeed = 0;
        float turbineTorque = 1f;
        boolean hasProvider = false;
        int providerMaxSpeed = MechanicalCapabilities.maxRpm();
        Direction inputFacing = ctx.getLevel().toAbsolute(MECHANICAL_INPUT_FACING);
        BlockPos inputPortAbs = ctx.getLevel().toAbsolute(MECHANICAL_INPUT_POI.posInMultiblock());
        if (inputFacing == null) { Reference.IT_LOGGER.warn("AlternatorLogic: Failed to resolve input facing"); }
        else {
            IMechanicalEnergyProvider provider = level.getCapability(MechanicalCapabilities.MECHANICAL_PROVIDER, inputPortAbs.relative(inputFacing), inputFacing.getOpposite());
            if (provider != null) {
                turbineSpeed = provider.getSpeed();
                turbineTorque = provider.getTorque();
                providerMaxSpeed = provider.getMaxSpeed();
                hasProvider = true;
                if (turbineSpeed > 0) { state.active = true; }
            }
        }
        int effectiveMax = hasProvider ? Math.min(MechanicalCapabilities.maxRpm(), providerMaxSpeed) : MechanicalCapabilities.maxRpm();
        state.effectiveMaxSpeed = effectiveMax;
        if (hasProvider) {
            state.speed = Math.min(turbineSpeed, effectiveMax);
            state.torqueMultiplier = turbineTorque;
        }
        else if (state.speed > 0) {
            state.speed = Math.max(state.speed - 6, 0);
            if (state.speed > 0) { state.active = true; }
        }
        generateAndPushEnergy(state, ctx, level);
        int currentEnergy = state.energy.getEnergyStored();
        int maxEnergy = state.energy.getMaxEnergyStored();
        int newComparatorValue = maxEnergy > 0 ? (15 * currentEnergy) / maxEnergy : 0;
        boolean comparatorChanged = ctx.updateComparatorOutputs(state.lastComparatorValue, newComparatorValue, COMPARATOR_POSITIONS);
        state.lastComparatorValue = newComparatorValue;
        if (wasActive != state.active || prevSpeed != state.speed || prevTorque != state.torqueMultiplier || prevEnergy != currentEnergy || comparatorChanged) { ctx.markDirtyAndSync(); }
    }

    private static void generateAndPushEnergy(State state, IMultiblockContext<State> ctx, Level level) {
        double ratio = (double) state.speed / MechanicalCapabilities.maxRpm();
        double powerFactor = Math.max(0.0D, ServerConfig.alternatorPowerFactor);
        int generatedThisTick = (int) Math.round(ratio * state.torqueMultiplier * ServerConfig.alternatorMaxOutput * powerFactor);
        List<IEnergyStorage> connected = new ArrayList<>();
        addConnectedHandlers(connected, ctx, level, ENERGY_LEFT_POIS, ENERGY_LEFT_FACING);
        addConnectedHandlers(connected, ctx, level, ENERGY_RIGHT_POIS, ENERGY_RIGHT_FACING);
        state.energy.receiveEnergy(connected.isEmpty() ? generatedThisTick : generatedThisTick - distributeFluxProper(connected, generatedThisTick), false);
    }

    private static int distributeFluxProper(List<IEnergyStorage> storages, int amount) {
        List<Pair<IEnergyStorage, Integer>> pairs = storages.stream()
                .filter(Objects::nonNull)
                .map(storage -> Pair.of(storage, storage.receiveEnergy(amount, true)))
                .sorted(Comparator.comparingInt(Pair::getSecond))
                .toList();
        int remaining = amount;
        int remainingOutputs = pairs.size();
        for (Pair<IEnergyStorage, Integer> pair : pairs) {
            if (remaining <= 0) { break; }
            int possibleOutput = (int) Math.ceil((double) remaining / remainingOutputs);
            remaining -= pair.getFirst().receiveEnergy(possibleOutput, false);
            remainingOutputs--;
        }
        return amount - remaining;
    }

    private static void addConnectedHandlers(List<IEnergyStorage> connected, IMultiblockContext<State> ctx, Level level, List<BlockPos> pois, RelativeBlockFace facing) {
        for (BlockPos pos : pois) {
            Direction side = ctx.getLevel().toAbsolute(facing);
            if (side == null) { continue; }
            IEnergyStorage handler = level.getCapability(Capabilities.EnergyStorage.BLOCK, ctx.getLevel().toAbsolute(pos).relative(side), side.getOpposite());
            if (handler != null) { connected.add(handler); }
        }
    }

    @Override public void dropExtraItems(State state, Consumer<ItemStack> drop) { }

    @Override public State createInitialState(IInitialMultiblockContext<State> ctx) {
        MECHANICAL_CONSUMER.get();
        return new State(ctx);
    }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return ITShapes.get("alternator").getter; }

    @Override public void registerCapabilities(IMultiblockComponent.CapabilityRegistrar<State> register) {
        register.register(Capabilities.EnergyStorage.BLOCK, (state, position) -> {
            BlockPos localPos = position.posInMultiblock();
            RelativeBlockFace side = position.side();
            if (ENERGY_LEFT_POIS.contains(localPos) && (side == null || side == ENERGY_LEFT_FACING)) { return state.energy; }
            if (ENERGY_RIGHT_POIS.contains(localPos) && (side == null || side == ENERGY_RIGHT_FACING)) { return state.energy; }
            return null;
        });
        register.register(MechanicalCapabilities.MECHANICAL_CONSUMER, (state, position) -> {
            RelativeBlockFace side = position.side();
            if (position.posInMultiblock().equals(MECHANICAL_INPUT_POI.posInMultiblock()) && (side == null || side == MECHANICAL_INPUT_FACING || side == MECHANICAL_INPUT_FACING.getOpposite())) { return MECHANICAL_CONSUMER.get(); }
            return null;
        });
    }

    private static class MechanicalEnergyConsumer implements IMechanicalEnergyConsumer {
        @Override public double getMass() { return ServerConfig.alternatorBaseMass; }

        @Override public double getFriction() { return ServerConfig.alternatorFriction; }

        @Override public int getMaxSpeed() { return MechanicalCapabilities.maxRpm(); }
    }

    public static class State implements IDisplaySyncState, IDataReloadAware {
        public final ICEnergyStorageAveraging energy;
        public boolean active = false;
        public int speed = 0;
        public int lastComparatorValue = -1;
        public float torqueMultiplier = 1f;
        public int effectiveMaxSpeed = MechanicalCapabilities.maxRpm();
        public BooleanSupplier isSoundPlaying = () -> false;

        public State(IInitialMultiblockContext<State> ctx) {
            Runnable markDirty = ctx.getMarkDirtyRunnable();
            Runnable sync = ctx.getSyncRunnable();
            this.energy = new SyncEnergyStorage(ServerConfig.alternatorEnergyCapacity, () -> { markDirty.run(); sync.run(); });
        }

        @Override public void onDataReload(IInitialMultiblockContext<?> context) { lastComparatorValue = -1; }

        @Override public void writeSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            energy.save(nbt, "energy");
            nbt.putBoolean("active", active);
            nbt.putInt("speed", speed);
            nbt.putFloat("torqueMultiplier", torqueMultiplier);
            nbt.putInt("effectiveMaxSpeed", effectiveMaxSpeed);
        }

        @Override public void readSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            energy.load(nbt, "energy");
            active = nbt.getBoolean("active");
            speed = nbt.getInt("speed");
            torqueMultiplier = nbt.getFloat("torqueMultiplier");
            effectiveMaxSpeed = nbt.getInt("effectiveMaxSpeed");
        }

        @Override public void writeDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            nbt.putBoolean("active", active);
            nbt.putInt("speed", speed);
            nbt.putFloat("torqueMultiplier", torqueMultiplier);
            energy.save(nbt, "energy");
            nbt.putInt("effectiveMaxSpeed", effectiveMaxSpeed);
        }

        @Override public void readDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            active = nbt.getBoolean("active");
            speed = nbt.getInt("speed");
            torqueMultiplier = nbt.getFloat("torqueMultiplier");
            energy.load(nbt, "energy");
            effectiveMaxSpeed = nbt.getInt("effectiveMaxSpeed");
        }

        @Override public boolean isActive() { return active; }

        @Override public ICEnergyStorageAveraging getEnergyStorage() { return energy; }

        @Override public void addDisplayLines(Level level, DisplayLines lines) { lines.rpm(speed, effectiveMaxSpeed); }
    }

    private static void loadPois(List<PoIJSONSchema> pois) {
        RUNNING_SOUND_POI = MultiblockPOIHelper.getPosList(pois, "sound0").getFirst();
        COMPARATOR_POSITIONS = MultiblockPOIHelper.getPosList(pois, "comparator0");
        MECHANICAL_INPUT_POI = MultiblockPOIHelper.getCapabilityPosition(pois, "mechanical_input0");
        ENERGY_LEFT_POIS = MultiblockPOIHelper.getPosList(pois, "energy_left0");
        ENERGY_RIGHT_POIS = MultiblockPOIHelper.getPosList(pois, "energy_right0");
        ENERGY_LEFT_FACING = MultiblockPOIHelper.getFacing(pois, "energy_left0");
        ENERGY_RIGHT_FACING = MultiblockPOIHelper.getFacing(pois, "energy_right0");
        MECHANICAL_INPUT_FACING = MultiblockPOIHelper.getFacing(pois, "mechanical_input0");
    }
}
