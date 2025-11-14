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
import mctmods.immersivetechnology.api.MechanicalCapabilities;
import mctmods.immersivetechnology.api.capability.IMechanicalEnergyConsumer;
import mctmods.immersivetechnology.api.capability.IMechanicalEnergyProvider;
import mctmods.immersivetechnology.common.multiblocks.helper.ITDisplayContext;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.AlternatorShape;
import mctmods.immersivetechnology.common.util.multiblock.PoIJSONSchema;
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
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public class AlternatorLogic implements IMultiblockLogic<AlternatorLogic.State>, IServerTickableComponent<AlternatorLogic.State>, IClientTickableComponent<AlternatorLogic.State> {
    public static final int ENERGY_CAPACITY = 1200000;
    private static final double BASE_MASS = 2;
    private static final double FRICTION = 12;
    private static final int MAX_SPEED = 7200;
    private static final int POWER_DIVIDER = 2;
    private static final int MAX_OUTPUT = 12288;
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

    @Override
    public void tickClient(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        if (!state.isSoundPlaying.getAsBoolean()) {
            Vec3 soundPos = ctx.getLevel().toAbsolute(new Vec3(RUNNING_SOUND_POI.getX() + 0.5, RUNNING_SOUND_POI.getY() + 0.5, RUNNING_SOUND_POI.getZ() + 0.5));
            state.isSoundPlaying = ITSound.startSound(
                    () -> state.active && state.speed >= state.maxSpeed / POWER_DIVIDER,
                    ctx.isValid(),
                    soundPos,
                    ITSounds.alternator,
                    () -> {
                        LocalPlayer player = Minecraft.getInstance().player;
                        if (player == null) { return 0f; }
                        float attenuation = (float) Math.max(player.distanceToSqr(soundPos) / 8, 1);
                        return 20f / attenuation;
                    },
                    () -> {
                        float half = (float) state.maxSpeed / POWER_DIVIDER;
                        if (state.speed <= half) { return 0.75f; }
                        float normalized = (state.speed - half) / half;
                        return 0.75f + (0.25f * normalized);
                    }
            );
        }
    }

    @Override
    public void tickServer(IMultiblockContext<State> ctx) {
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
                hasProvider = true;
                if (turbineSpeed > 0) { state.active = true; }
            }
        }
        if (hasProvider) {
            state.speed = turbineSpeed;
            state.torqueMultiplier = turbineTorque;
            state.maxSpeed = MAX_SPEED;
        } else if (state.speed > 0) {
            int speedDownRate = (int) Math.round(FRICTION / BASE_MASS);
            state.speed = Math.max(state.speed - speedDownRate, 0);
            if (state.speed > 0) { state.active = true; }
        }
        generateEnergy(state);
        drainBuffer(state, ctx, level);
        boolean activeChanged = wasActive != state.active;
        boolean speedChanged = prevSpeed != state.speed;
        boolean torqueChanged = prevTorque != state.torqueMultiplier;
        int currentEnergy = state.energy.getEnergyStored();
        boolean energyChanged = prevEnergy != currentEnergy;
        boolean update = activeChanged || speedChanged || torqueChanged || energyChanged;
        if (update) { ctx.markMasterDirty(); ctx.requestMasterBESync(); }
    }

    private void generateEnergy(State state) {
        if (state.speed < state.maxSpeed / POWER_DIVIDER) { return; }
        double ratio = (double) state.speed / state.maxSpeed;
        int generated = (int) Math.round(ratio * state.torqueMultiplier * MAX_OUTPUT);
        int current = state.energy.getEnergyStored();
        int newEnergy = Math.min(state.energy.getMaxEnergyStored(), current + generated);
        state.energy.setStoredEnergy(newEnergy);
    }

    private void drainBuffer(State state, IMultiblockContext<State> ctx, Level level) {
        int initialStored = state.energy.getEnergyStored();
        if (initialStored <= 0) { return; }
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
        if (connected.isEmpty()) { return; }
        int numConnected = connected.size();
        int base = initialStored / numConnected;
        int extra = initialStored % numConnected;
        int pushed = 0;
        int i = 0;
        for (IEnergyStorage handler : connected) {
            int amount = base + (i < extra ? 1 : 0);
            int accepted = handler.receiveEnergy(amount, false);
            pushed += accepted;
            i++;
        }
        int leftover = initialStored - pushed;
        for (IEnergyStorage handler : connected) {
            if (leftover <= 0) { break; }
            int accepted = handler.receiveEnergy(leftover, false);
            pushed += accepted;
            leftover -= accepted;
        }
        state.energy.setStoredEnergy(initialStored - pushed);
    }

    @Override
    public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) {
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

    @Override
    public void dropExtraItems(State state, Consumer<ItemStack> drop) { }

    @Override
    public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override
    public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return AlternatorShape.GETTER; }

    private static class MechanicalEnergyConsumer implements IMechanicalEnergyConsumer {
        @Override
        public double getMass() { return BASE_MASS; }
        @Override
        public double getFriction() { return FRICTION; }
    }

    public static class State implements IMultiblockState, ITDisplayContext {
        public AveragingEnergyStorage energy;
        public boolean active = false;
        public int speed = 0;
        public float torqueMultiplier = 1f;
        public int maxSpeed = MAX_SPEED;
        public BooleanSupplier isSoundPlaying = () -> false;
        private final StoredCapability<IEnergyStorage> energyCap;

        public State(IInitialMultiblockContext<State> ctx) {
            Runnable markDirty = ctx.getMarkDirtyRunnable();
            Runnable sync = ctx.getSyncRunnable();
            Runnable onChanged = () -> { markDirty.run(); sync.run(); };
            this.energy = new SyncEnergyStorage(ENERGY_CAPACITY, onChanged);
            this.energyCap = new StoredCapability<>(this.energy);
        }

        @Override
        public void writeSaveNBT(CompoundTag nbt) {
            nbt.put("energy", energy.serializeNBT());
            nbt.putBoolean("active", active);
            nbt.putInt("speed", speed);
            nbt.putFloat("torqueMultiplier", torqueMultiplier);
            nbt.putInt("maxSpeed", maxSpeed);
        }

        @Override
        public void readSaveNBT(CompoundTag nbt) {
            energy.deserializeNBT(nbt.get("energy"));
            active = nbt.getBoolean("active");
            speed = nbt.getInt("speed");
            torqueMultiplier = nbt.getFloat("torqueMultiplier");
            maxSpeed = nbt.getInt("maxSpeed");
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
        public void writeDisplaySyncNBT(CompoundTag nbt) {
            nbt.putBoolean("active", active);
            nbt.putInt("speed", speed);
            nbt.putFloat("torqueMultiplier", torqueMultiplier);
            nbt.put("energy", energy.serializeNBT());
        }

        @Override
        public void readDisplaySyncNBT(CompoundTag nbt) {
            active = nbt.getBoolean("active");
            speed = nbt.getInt("speed");
            torqueMultiplier = nbt.getFloat("torqueMultiplier");
            if (energy == null) { energy = new SyncEnergyStorage(ENERGY_CAPACITY, () -> {}); }
            energy.deserializeNBT(nbt.get("energy"));
        }

        @Override
        public boolean isActive() { return active; }

        @Override
        public AveragingEnergyStorage getEnergy() { return energy; }
    }

    private static class SyncEnergyStorage extends AveragingEnergyStorage {
        private final Runnable onChanged;

        public SyncEnergyStorage(int capacity, Runnable onChanged) {
            super(capacity);
            this.onChanged = onChanged;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = super.receiveEnergy(maxReceive, simulate);
            if (received > 0 && !simulate) { onChanged.run(); }
            return received;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
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
