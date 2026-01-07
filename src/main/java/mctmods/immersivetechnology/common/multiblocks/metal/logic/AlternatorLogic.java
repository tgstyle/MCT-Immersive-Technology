package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.StoredCapability;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import mctmods.immersivetechnology.api.MechanicalCapabilities;
import mctmods.immersivetechnology.api.capability.IMechanicalEnergyConsumer;
import mctmods.immersivetechnology.api.capability.IMechanicalEnergyProvider;
import mctmods.immersivetechnology.common.multiblocks.helper.ITDisplayContext;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.AlternatorShape;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public class AlternatorLogic implements IMultiblockLogic<AlternatorLogic.State>, IServerTickableComponent<AlternatorLogic.State>, IClientTickableComponent<AlternatorLogic.State> {
    public static final int ENERGY_CAPACITY = 1200000;
    private static final double BASE_MASS = 2;
    private static final double FRICTION = 0;
    private static final int MAX_OUTPUT = 12288;
    private static final int MAX_SPEED = MechanicalCapabilities.MAX_RPM;
    private static final List<PoIJSONSchema> RAW_POIS = ImmutableList.copyOf(AlternatorShape.DATA.pointsOfInterest);

    public static final BlockPos RUNNING_SOUND_POI = getPosList("running_sound").get(0);
    public static final BlockPos ROTATIONAL_INPUT_POI = getPosList("rotational_input").get(0);
    private static final List<BlockPos> ENERGY_LEFT_POI = getPosList("energy_left");
    private static final List<BlockPos> ENERGY_RIGHT_POI = getPosList("energy_right");
    private static final RelativeBlockFace ENERGY_LEFT_FACING = getFacing("energy_left");
    private static final RelativeBlockFace ENERGY_RIGHT_FACING = getFacing("energy_right");
    private static final RelativeBlockFace ROTATIONAL_INPUT_FACING = getFacing("rotational_input");

    private static List<BlockPos> getPosList(String name) { return RAW_POIS.stream().filter(poi -> poi.name.equals(name)).map(poi -> new BlockPos(poi.pos[0], poi.pos[1], poi.pos[2])).collect(ImmutableList.toImmutableList()); }
    private static RelativeBlockFace getFacing(String name) {
        List<RelativeBlockFace> facings = RAW_POIS.stream().filter(poi -> poi.name.equals(name)).flatMap(poi -> poi.relativeFaces.stream()).distinct().toList();
        if (facings.size() != 1) { throw new RuntimeException("Inconsistent facings for POI: " + name); }
        return facings.get(0);
    }

    @Override public void tickClient(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) { return; }
        Vec3 soundPos = ctx.getLevel().toAbsolute(new Vec3(RUNNING_SOUND_POI.getX() + 0.5, RUNNING_SOUND_POI.getY() + 0.5, RUNNING_SOUND_POI.getZ() + 0.5));
        float att = (float) Math.max(player.distanceToSqr(soundPos) / 32, 1);
        float vol = 11f / att;
        if (state.active && vol > 0.01f && !state.isSoundPlaying.getAsBoolean()) {
            state.isSoundPlaying = ITSound.startSound(
                    () -> state.active,
                    ctx.isValid(),
                    soundPos,
                    ITSounds.alternator,
                    () -> {
                        LocalPlayer p = Minecraft.getInstance().player;
                        if (p == null) { return 0f; }
                        return 11f / (float) Math.max(p.distanceToSqr(soundPos) / 32, 1);
                    },
                    () -> ITLib.remapRange(0, state.effectiveMaxSpeed, 0.5f, 1.25f, state.speed)
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
        Direction inputFacing = ctx.getLevel().toAbsolute(ROTATIONAL_INPUT_FACING);
        BlockPos inputPortAbs = ctx.getLevel().toAbsolute(ROTATIONAL_INPUT_POI);
        assert inputFacing != null;
        BlockPos providerAbsolutePos = inputPortAbs.relative(inputFacing);
        BlockEntity entity = level.getBlockEntity(providerAbsolutePos);
        if (entity != null) {
            LazyOptional<IMechanicalEnergyProvider> providerCap = entity.getCapability(MechanicalCapabilities.MECHANICAL_PROVIDER_CAPABILITY, inputFacing.getOpposite());
            if (providerCap.isPresent()) {
                IMechanicalEnergyProvider provider = providerCap.orElseThrow(RuntimeException::new);
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
        boolean update = activeChanged || speedChanged || torqueChanged || energyChanged;
        if (update) { ctx.markMasterDirty(); ctx.requestMasterBESync(); }
    }

    private void generateAndPushEnergy(State state, IMultiblockContext<State> ctx, Level level) {
        double ratio = (double) state.speed / MAX_SPEED;
        int generatedThisTick = (int) Math.round(ratio * state.torqueMultiplier * MAX_OUTPUT);
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
        for (BlockPos pos : ENERGY_LEFT_POI) {
            BlockPos absolutePos = ctx.getLevel().toAbsolute(pos);
            Direction side = ctx.getLevel().toAbsolute(ENERGY_LEFT_FACING);
            assert side != null;
            BlockEntity adjacent = level.getBlockEntity(absolutePos.relative(side));
            if (adjacent != null) {
                LazyOptional<IEnergyStorage> handlerOpt = adjacent.getCapability(ForgeCapabilities.ENERGY, side.getOpposite());
                if (handlerOpt.isPresent()) {
                    connected.add(handlerOpt.orElseThrow(RuntimeException::new));
                }
            }
        }
        for (BlockPos pos : ENERGY_RIGHT_POI) {
            BlockPos absolutePos = ctx.getLevel().toAbsolute(pos);
            Direction side = ctx.getLevel().toAbsolute(ENERGY_RIGHT_FACING);
            assert side != null;
            BlockEntity adjacent = level.getBlockEntity(absolutePos.relative(side));
            if (adjacent != null) {
                LazyOptional<IEnergyStorage> handlerOpt = adjacent.getCapability(ForgeCapabilities.ENERGY, side.getOpposite());
                if (handlerOpt.isPresent()) {
                    connected.add(handlerOpt.orElseThrow(RuntimeException::new));
                }
            }
        }
        return connected;
    }

    @Override public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) {
        State state = ctx.getState();
        if (cap == ForgeCapabilities.ENERGY) {
            BlockPos localPos = position.posInMultiblock();
            RelativeBlockFace side = position.side();
            if (ENERGY_LEFT_POI.contains(localPos) && (side == null || side == ENERGY_LEFT_FACING)) { return state.energyCap.cast(ctx); }
            if (ENERGY_RIGHT_POI.contains(localPos) && (side == null || side == ENERGY_RIGHT_FACING)) { return state.energyCap.cast(ctx); }
        }
        if (cap == MechanicalCapabilities.MECHANICAL_CONSUMER_CAPABILITY) {
            CapabilityPosition checkPos = position;
            if (position.posInMultiblock().equals(BlockPos.ZERO)) { checkPos = new CapabilityPosition(ROTATIONAL_INPUT_POI, position.side()); }
            if (checkPos.posInMultiblock().equals(ROTATIONAL_INPUT_POI) && (checkPos.side() == null || checkPos.side() == ROTATIONAL_INPUT_FACING || checkPos.side() == ROTATIONAL_INPUT_FACING.getOpposite())) { return LazyOptional.of(MechanicalEnergyConsumer::new).cast(); }
        }
        return LazyOptional.empty();
    }

    @Override public void dropExtraItems(State state, Consumer<ItemStack> drop) { }

    @Override public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return AlternatorShape.GETTER; }

    private static class MechanicalEnergyConsumer implements IMechanicalEnergyConsumer {
        @Override public double getMass() { return BASE_MASS; }
        @Override public double getFriction() { return FRICTION; }
        @Override public int getMaxSpeed() { return MechanicalCapabilities.MAX_RPM; }
    }

    public static class State implements IMultiblockState, ITDisplayContext {
        public AveragingEnergyStorage energy;
        public boolean active = false;
        public int speed = 0;
        public float torqueMultiplier = 1f;
        public int effectiveMaxSpeed = MAX_SPEED;
        public BooleanSupplier isSoundPlaying = () -> false;
        private final StoredCapability<IEnergyStorage> energyCap;

        public State(IInitialMultiblockContext<State> ctx) {
            Runnable markDirty = ctx.getMarkDirtyRunnable();
            Runnable sync = ctx.getSyncRunnable();
            Runnable onChanged = () -> { markDirty.run(); sync.run(); };
            this.energy = new SyncEnergyStorage(ENERGY_CAPACITY, onChanged);
            this.energyCap = new StoredCapability<>(this.energy);
        }

        @Override public void writeSaveNBT(CompoundTag nbt) {
            nbt.put("energy", energy.serializeNBT());
            nbt.putBoolean("active", active);
            nbt.putInt("speed", speed);
            nbt.putFloat("torqueMultiplier", torqueMultiplier);
            nbt.putInt("effectiveMaxSpeed", effectiveMaxSpeed);
        }

        @Override public void readSaveNBT(CompoundTag nbt) {
            energy.deserializeNBT(nbt.get("energy"));
            active = nbt.getBoolean("active");
            speed = nbt.getInt("speed");
            torqueMultiplier = nbt.getFloat("torqueMultiplier");
            effectiveMaxSpeed = nbt.getInt("effectiveMaxSpeed");
        }

        @Override public void writeSyncNBT(CompoundTag nbt) {
            CompoundTag display = new CompoundTag();
            writeDisplaySyncNBT(display);
            nbt.put("display", display);
        }

        @Override public void readSyncNBT(CompoundTag nbt) {
            if (nbt.contains("display", Tag.TAG_COMPOUND)) { readDisplaySyncNBT(nbt.getCompound("display")); }
        }

        @Override public void writeDisplaySyncNBT(CompoundTag nbt) {
            nbt.putBoolean("active", active);
            nbt.putInt("speed", speed);
            nbt.putFloat("torqueMultiplier", torqueMultiplier);
            nbt.put("energy", energy.serializeNBT());
            nbt.putInt("effectiveMaxSpeed", effectiveMaxSpeed);
        }

        @Override public void readDisplaySyncNBT(CompoundTag nbt) {
            active = nbt.getBoolean("active");
            speed = nbt.getInt("speed");
            torqueMultiplier = nbt.getFloat("torqueMultiplier");
            if (energy == null) { energy = new SyncEnergyStorage(ENERGY_CAPACITY, () -> {}); }
            energy.deserializeNBT(nbt.get("energy"));
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
