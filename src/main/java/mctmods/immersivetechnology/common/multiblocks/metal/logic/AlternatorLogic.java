package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IMultiblockComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import com.google.common.collect.ImmutableList;
import com.immersiveconvergence.api.MechanicalCapabilities;
import com.immersiveconvergence.api.capability.IMechanicalEnergyConsumer;
import com.immersiveconvergence.api.capability.IMechanicalEnergyProvider;
import com.mojang.datafixers.util.Pair;
import mctmods.immersivetechnology.common.multiblocks.helper.IDisplayContext;
import mctmods.immersivetechnology.common.multiblocks.helper.MultiblockPOIHelper;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.AlternatorShape;
import mctmods.immersivetechnology.core.util.multiblock.PoIJSONSchema;
import mctmods.immersivetechnology.core.ServerConfig;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.lib.ModSound;
import mctmods.immersivetechnology.core.registration.Sounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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
    private static final int MAX_SPEED = MechanicalCapabilities.MAX_RPM;
    private static final List<PoIJSONSchema> RAW_POIS = ImmutableList.copyOf(AlternatorShape.DATA.pointsOfInterest);

    public static final BlockPos RUNNING_SOUND_POI = MultiblockPOIHelper.getPosList(RAW_POIS, "sound0").getFirst();
    public static final List<BlockPos> COMPARATOR_POSITIONS = MultiblockPOIHelper.getPosList(RAW_POIS, "comparator0");
    public static final CapabilityPosition MECHANICAL_INPUT_POI = MultiblockPOIHelper.getCapabilityPosition(RAW_POIS, "mechanical_input0");
    private static final List<BlockPos> ENERGY_LEFT_POIS = MultiblockPOIHelper.getPosList(RAW_POIS, "energy_left0");
    private static final List<BlockPos> ENERGY_RIGHT_POIS = MultiblockPOIHelper.getPosList(RAW_POIS, "energy_right0");
    private static final RelativeBlockFace ENERGY_LEFT_FACING = MultiblockPOIHelper.getFacing(RAW_POIS, "energy_left0");
    private static final RelativeBlockFace ENERGY_RIGHT_FACING = MultiblockPOIHelper.getFacing(RAW_POIS, "energy_right0");
    private static final RelativeBlockFace MECHANICAL_INPUT_FACING = MultiblockPOIHelper.getFacing(RAW_POIS, "mechanical_input0");

    @SuppressWarnings("unused")
    private static final Lazy<IMechanicalEnergyConsumer> MECHANICAL_CONSUMER = Lazy.of(MechanicalEnergyConsumer::new);

    @Override public void tickClient(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) { return; }
        Vec3 soundPos = ctx.getLevel().toAbsolute(new Vec3(RUNNING_SOUND_POI.getX() + 0.5, RUNNING_SOUND_POI.getY() + 0.5, RUNNING_SOUND_POI.getZ() + 0.5));
        float att = (float) Math.max(player.distanceToSqr(soundPos) / 32, 1);
        float vol = 11f / att;
        if (state.active && vol > 0.01f && !state.isSoundPlaying.getAsBoolean()) {
            state.isSoundPlaying = ModSound.startSound(
                    () -> state.active,
                    ctx.isValid(),
                    soundPos,
                    Sounds.alternator,
                    () -> {
                        LocalPlayer p = Minecraft.getInstance().player;
                        if (p == null) { return 0f; }
                        return 11f / (float) Math.max(p.distanceToSqr(soundPos) / 32, 1);
                    },
                    () -> Reference.remapRange(0, state.effectiveMaxSpeed, 0.5f, 1.25f, state.speed)
            );
        }
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
        int providerMaxSpeed = MAX_SPEED;
        Direction inputFacing = ctx.getLevel().toAbsolute(MECHANICAL_INPUT_FACING);
        BlockPos inputPortAbs = ctx.getLevel().toAbsolute(MECHANICAL_INPUT_POI.posInMultiblock());
        if (inputFacing == null) {
            Reference.IT_LOGGER.warn("AlternatorLogic: Failed to resolve input facing");
        } else {
            BlockPos providerAbsolutePos = inputPortAbs.relative(inputFacing);
            IMechanicalEnergyProvider provider = level.getCapability(MechanicalCapabilities.MECHANICAL_PROVIDER, providerAbsolutePos, inputFacing.getOpposite());
            if (provider != null) {
                turbineSpeed = provider.getSpeed();
                turbineTorque = provider.getTorque();
                providerMaxSpeed = provider.getMaxSpeed();
                hasProvider = true;
                if (turbineSpeed > 0) { state.active = true; }
            }
        }
        int effectiveMax = hasProvider ? Math.min(MAX_SPEED, providerMaxSpeed) : MAX_SPEED;
        state.effectiveMaxSpeed = effectiveMax;
        if (hasProvider) {
            state.speed = Math.min(turbineSpeed, effectiveMax);
            state.torqueMultiplier = turbineTorque;
        } else if (state.speed > 0) {
            state.speed = Math.max(state.speed - 6, 0);
            if (state.speed > 0) { state.active = true; }
        }
        generateAndPushEnergy(state, ctx, level);
        boolean activeChanged = wasActive != state.active;
        boolean speedChanged = prevSpeed != state.speed;
        boolean torqueChanged = prevTorque != state.torqueMultiplier;
        int currentEnergy = state.energy.getEnergyStored();
        boolean energyChanged = prevEnergy != currentEnergy;
        int maxEnergy = state.energy.getMaxEnergyStored();
        int newComparatorValue = maxEnergy > 0 ? (15 * currentEnergy) / maxEnergy : 0;
        boolean comparatorChanged = newComparatorValue != state.lastComparatorValue;
        if (comparatorChanged) { for (BlockPos pos : COMPARATOR_POSITIONS) { ctx.setComparatorOutputFor(pos, newComparatorValue); } state.lastComparatorValue = newComparatorValue; }
        boolean update = activeChanged || speedChanged || torqueChanged || energyChanged || comparatorChanged;
        if (update) { ctx.markMasterDirty(); ctx.requestMasterBESync(); }
    }

    private void generateAndPushEnergy(State state, IMultiblockContext<State> ctx, Level level) {
        double ratio = (double) state.speed / MAX_SPEED;
        double powerFactor = Math.max(0.0D, ServerConfig.alternatorPowerFactor);
        int generatedThisTick = (int) Math.round(ratio * state.torqueMultiplier * ServerConfig.alternatorMaxOutput * powerFactor);
        List<IEnergyStorage> connected = getConnectedHandlers(ctx, level);
        if (connected.isEmpty()) { state.energy.receiveEnergy(generatedThisTick, false); return; }
        int pushed = distributeFluxProper(connected, generatedThisTick);
        state.energy.receiveEnergy(generatedThisTick - pushed, false);
    }

    private int distributeFluxProper(List<IEnergyStorage> storages, int amount) {
        if (storages.isEmpty()) { return amount; }
        List<Pair<IEnergyStorage, Integer>> pairs = storages.stream()
                .filter(Objects::nonNull)
                .map(storage -> Pair.of(storage, storage.receiveEnergy(amount, true)))
                .sorted(Comparator.comparingInt(Pair::getSecond))
                .toList();
        int remaining = amount;
        int remainingOutputs = pairs.size();
        for (Pair<IEnergyStorage, Integer> pair : pairs) {
            IEnergyStorage storage = pair.getFirst();
            if (remaining <= 0) { break; }
            int possibleOutput = (int) Math.ceil((double) remaining / remainingOutputs);
            int inserted = storage.receiveEnergy(possibleOutput, false);
            remaining -= inserted;
            remainingOutputs--;
        }
        return amount - remaining;
    }

    private List<IEnergyStorage> getConnectedHandlers(IMultiblockContext<State> ctx, Level level) {
        List<IEnergyStorage> connected = new ArrayList<>();
        for (BlockPos pos : ENERGY_LEFT_POIS) {
            BlockPos absolutePos = ctx.getLevel().toAbsolute(pos);
            Direction side = ctx.getLevel().toAbsolute(ENERGY_LEFT_FACING);
            if (side == null) { continue; }
            BlockPos targetPos = absolutePos.relative(side);
            IEnergyStorage handler = level.getCapability(Capabilities.EnergyStorage.BLOCK, targetPos, side.getOpposite());
            if (handler != null) {
                connected.add(handler);
            }
        }
        for (BlockPos pos : ENERGY_RIGHT_POIS) {
            BlockPos absolutePos = ctx.getLevel().toAbsolute(pos);
            Direction side = ctx.getLevel().toAbsolute(ENERGY_RIGHT_FACING);
            if (side == null) { continue; }
            BlockPos targetPos = absolutePos.relative(side);
            IEnergyStorage handler = level.getCapability(Capabilities.EnergyStorage.BLOCK, targetPos, side.getOpposite());
            if (handler != null) {
                connected.add(handler);
            }
        }
        return connected;
    }

    @Override public void dropExtraItems(State state, Consumer<ItemStack> drop) { }

    @Override public State createInitialState(IInitialMultiblockContext<State> ctx) {
        MECHANICAL_CONSUMER.get();
        return new State(ctx);
    }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return AlternatorShape.GETTER; }

    @Override public void registerCapabilities(IMultiblockComponent.CapabilityRegistrar<State> register) {
        register.register(Capabilities.EnergyStorage.BLOCK, (state, position) -> {
            BlockPos localPos = position.posInMultiblock();
            RelativeBlockFace side = position.side();
            if (ENERGY_LEFT_POIS.contains(localPos) && (side == null || side == ENERGY_LEFT_FACING)) { return state.energy; }
            if (ENERGY_RIGHT_POIS.contains(localPos) && (side == null || side == ENERGY_RIGHT_FACING)) { return state.energy; }
            return null;
        });
        register.register(MechanicalCapabilities.MECHANICAL_CONSUMER, (state, position) -> {
            BlockPos localPos = position.posInMultiblock();
            RelativeBlockFace side = position.side();
            if (localPos.equals(MECHANICAL_INPUT_POI.posInMultiblock()) && (side == null || side == MECHANICAL_INPUT_FACING || side == MECHANICAL_INPUT_FACING.getOpposite())) { return MECHANICAL_CONSUMER.get(); }
            return null;
        });
    }

    private static class MechanicalEnergyConsumer implements IMechanicalEnergyConsumer {
        @Override public double getMass() { return ServerConfig.alternatorBaseMass; }
        @Override public double getFriction() { return ServerConfig.alternatorFriction; }
        @Override public int getMaxSpeed() { return MechanicalCapabilities.MAX_RPM; }
    }

    public static class State implements IMultiblockState, IDisplayContext {
        public AveragingEnergyStorage energy;
        public boolean active = false;
        public int speed = 0;
        public int lastComparatorValue = -1;
        public float torqueMultiplier = 1f;
        public int effectiveMaxSpeed = MAX_SPEED;
        public BooleanSupplier isSoundPlaying = () -> false;

        public State(IInitialMultiblockContext<State> ctx) {
            Runnable markDirty = ctx.getMarkDirtyRunnable();
            Runnable sync = ctx.getSyncRunnable();
            Runnable onChanged = () -> { markDirty.run(); sync.run(); };
            this.energy = new SyncEnergyStorage(ServerConfig.alternatorEnergyCapacity, onChanged);
        }

        @Override public void writeSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            nbt.put("energy", energy.serializeNBT(provider));
            nbt.putBoolean("active", active);
            nbt.putInt("speed", speed);
            nbt.putFloat("torqueMultiplier", torqueMultiplier);
            nbt.putInt("effectiveMaxSpeed", effectiveMaxSpeed);
        }

        @Override public void readSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            energy.deserializeNBT(provider, Objects.requireNonNull(nbt.get("energy")));
            active = nbt.getBoolean("active");
            speed = nbt.getInt("speed");
            torqueMultiplier = nbt.getFloat("torqueMultiplier");
            effectiveMaxSpeed = nbt.getInt("effectiveMaxSpeed");
        }

        @Override public void writeSyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            CompoundTag display = new CompoundTag();
            writeDisplaySyncNBT(display, provider);
            nbt.put("display", display);
        }

        @Override public void readSyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            if (nbt.contains("display", Tag.TAG_COMPOUND)) { readDisplaySyncNBT(nbt.getCompound("display"), provider); }
        }

        public void writeDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            nbt.putBoolean("active", active);
            nbt.putInt("speed", speed);
            nbt.putFloat("torqueMultiplier", torqueMultiplier);
            nbt.put("energy", energy.serializeNBT(provider));
            nbt.putInt("effectiveMaxSpeed", effectiveMaxSpeed);
        }

        public void readDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            active = nbt.getBoolean("active");
            speed = nbt.getInt("speed");
            torqueMultiplier = nbt.getFloat("torqueMultiplier");
            if (energy == null) { energy = new SyncEnergyStorage(ServerConfig.alternatorEnergyCapacity, () -> {}); }
            energy.deserializeNBT(provider, Objects.requireNonNull(nbt.get("energy")));
            effectiveMaxSpeed = nbt.getInt("effectiveMaxSpeed");
        }

        @Override public boolean isActive() { return active; }
        @Override public AveragingEnergyStorage getEnergy() { return energy; }
    }

    private static class SyncEnergyStorage extends AveragingEnergyStorage {
        private final Runnable onChanged;

        public SyncEnergyStorage(int capacity, Runnable onChanged) {
            super(capacity);
            this.onChanged = onChanged;
        }

        @Override public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = super.receiveEnergy(maxReceive, simulate);
            if (received > 0 && !simulate) { onChanged.run(); }
            return received;
        }

        @Override public int extractEnergy(int maxExtract, boolean simulate) {
            int extracted = super.extractEnergy(maxExtract, simulate);
            if (extracted > 0 && !simulate) { onChanged.run(); }
            return extracted;
        }

        public void setStoredEnergy(int energy) {
            int prev = getEnergyStored();
            super.setStoredEnergy(energy);
            if (energy != prev && onChanged != null) { onChanged.run(); }
        }
    }
}
