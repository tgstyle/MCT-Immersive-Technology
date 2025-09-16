package mctmods.immersivetechnology.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.energy.MutableEnergyStorage;
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
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import com.google.common.collect.ImmutableList;
import mctmods.immersivetechnology.api.MechanicalCapabilities;
import mctmods.immersivetechnology.api.capability.IMechanicalEnergyConsumer;
import mctmods.immersivetechnology.api.capability.IMechanicalEnergyProvider;
import mctmods.immersivetechnology.common.blocks.multiblocks.shapes.AlternatorShape;
import mctmods.immersivetechnology.common.util.multiblock.MultiblockData;
import mctmods.immersivetechnology.common.util.multiblock.POIUtils;
import mctmods.immersivetechnology.common.util.multiblock.PoIJSONSchema;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.lib.ITSound;
import mctmods.immersivetechnology.core.registration.ITSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AlternatorLogic implements IMultiblockLogic<AlternatorLogic.State>, IServerTickableComponent<AlternatorLogic.State>, IClientTickableComponent<AlternatorLogic.State> {
    public static final int ENERGY_CAPACITY = 1200000;
    private static final double BASE_MASS = 2;
    private static final double FRICTION = 12;
    private static final int MAX_SPEED = 1800;
    private static final List<PoIJSONSchema> RAW_POIS;
    private static final int WIDTH;
    private static final int LENGTH;

    static {
        MultiblockData data = POIUtils.loadMultiblockData("alternator");
        RAW_POIS = ImmutableList.copyOf(data.pointsOfInterest);
        WIDTH = AlternatorShape.WIDTH;
        LENGTH = AlternatorShape.LENGTH;
    }

    public static final BlockPos RUNNING_SOUND_POI = getSinglePos("running_sound");
    public static final BlockPos ROTATIONAL_INPUT_POI = getSinglePos("rotational_input");
    private static final List<BlockPos> ENERGY_LEFT_POI = getPosList("energy_left");
    private static final List<BlockPos> ENERGY_RIGHT_POI = getPosList("energy_right");
    private static final RelativeBlockFace ENERGY_LEFT_FACING = getFacing("energy_left");
    private static final RelativeBlockFace ENERGY_RIGHT_FACING = getFacing("energy_right");
    private static final RelativeBlockFace ROTATIONAL_INPUT_FACING = getFacing("rotational_input");

    private static BlockPos getSinglePos(String name) { return RAW_POIS.stream().filter(poi -> poi.name.equals(name)).map(poi -> unflatten(poi.position)).findFirst().orElseThrow(() -> new RuntimeException("Missing POI: " + name)); }
    private static List<BlockPos> getPosList(String name) { return RAW_POIS.stream().filter(poi -> poi.name.equals(name)).map(poi -> unflatten(poi.position)).collect(ImmutableList.toImmutableList()); }
    private static RelativeBlockFace getFacing(String name) {
        List<RelativeBlockFace> facings = RAW_POIS.stream().filter(poi -> poi.name.equals(name)).map(poi -> poi.relativeFace).distinct().toList();
        if (facings.size() != 1) { throw new RuntimeException("Inconsistent facings for POI: " + name); }
        return facings.get(0);
    }

    private static BlockPos unflatten(int index) {
        int y = index / (WIDTH * LENGTH);
        int temp = index % (WIDTH * LENGTH);
        int z = temp / WIDTH;
        int x = temp % WIDTH;
        return new BlockPos(x, y, z);
    }

    @Override
    public void tickClient(IMultiblockContext<State> ctx) {
        final State state = ctx.getState();
        if (!state.isSoundPlaying.getAsBoolean()) {
            final Vec3 soundPos = ctx.getLevel().toAbsolute(new Vec3(RUNNING_SOUND_POI.getX() + 0.5, RUNNING_SOUND_POI.getY() + 0.5, RUNNING_SOUND_POI.getZ() + 0.5));
            state.isSoundPlaying = ITSound.startSound(
                    () -> state.active,
                    ctx.isValid(),
                    soundPos,
                    ITSounds.alternator,
                    () -> {
                        LocalPlayer player = Minecraft.getInstance().player;
                        if (player == null) { return 0f; }
                        float attenuation = (float) Math.max(player.distanceToSqr(soundPos) / 8, 1);
                        float percentage = (float) state.speed / state.maxSpeed;
                        return (5 * percentage) / attenuation;
                    },
                    () -> ITLib.remapRange(0f, 1f, 0.5f, 1.0f, (float) state.speed / state.maxSpeed)
            );
        }
    }

    @Override
    public void tickServer(IMultiblockContext<State> ctx) {
        final State state = ctx.getState();
        Level level = ctx.getLevel().getRawLevel();
        state.active = false;
        int turbineSpeed = 0;
        float turbineTorque = 1f;
        int turbineMaxSpeed = MAX_SPEED;
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
                turbineMaxSpeed = provider.getMaxSpeed();
                hasProvider = true;
                if (turbineSpeed > 0) { state.active = true; }
            }
        }
        if (hasProvider) {
            state.speed = turbineSpeed;
            state.torqueMultiplier = turbineTorque;
            state.maxSpeed = turbineMaxSpeed;
        } else if (state.speed > 0) {
            int speedDownRate = (int) Math.round(FRICTION / BASE_MASS);
            state.speed = Math.max(state.speed - speedDownRate, 0);
            if (state.speed > 0) { state.active = true; }
        }
        generateEnergy(state);
        outputEnergy(state);
        for (BlockPos pos : ENERGY_LEFT_POI) {
            BlockPos absolutePos = ctx.getLevel().toAbsolute(pos);
            Direction side = ctx.getLevel().toAbsolute(ENERGY_LEFT_FACING);
            assert side != null;
            BlockEntity adjacent = level.getBlockEntity(absolutePos.relative(side));
            if (adjacent != null) {
                LazyOptional<IEnergyStorage> handlerOpt = adjacent.getCapability(ForgeCapabilities.ENERGY, side.getOpposite());
                if (handlerOpt.isPresent()) {
                    IEnergyStorage handler = handlerOpt.orElseThrow(RuntimeException::new);
                    int maxPush = Math.min(2048, state.energy.getEnergyStored());
                    int pushed = handler.receiveEnergy(maxPush, false);
                    if (pushed > 0) { state.energy.setStoredEnergy(state.energy.getEnergyStored() - pushed); }
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
                    IEnergyStorage handler = handlerOpt.orElseThrow(RuntimeException::new);
                    int maxPush = Math.min(2048, state.energy.getEnergyStored());
                    int pushed = handler.receiveEnergy(maxPush, false);
                    if (pushed > 0) { state.energy.setStoredEnergy(state.energy.getEnergyStored() - pushed); }
                }
            }
        }
        if (state.active) { ctx.markMasterDirty(); }
        ctx.requestMasterBESync();
    }

    private void generateEnergy(State state) {
        if (state.speed < state.maxSpeed / 2) { return; }
        double ratio = (double) state.speed / state.maxSpeed;
        if (ratio > 0.0) {
            int generated = (int) Math.round(Math.pow(ratio, 2.0) * state.torqueMultiplier * 12288);
            int current = state.energy.getEnergyStored();
            int newEnergy = Math.min(state.energy.getMaxEnergyStored(), current + generated);
            state.energy.setStoredEnergy(newEnergy);
        }
    }

    private void outputEnergy(State state) {
        List<IEnergyStorage> presentOutputs = state.energyOutputsLeft.stream().map(CapabilityReference::getNullable).filter(Objects::nonNull).collect(Collectors.toList());
        presentOutputs.addAll(state.energyOutputsRight.stream().map(CapabilityReference::getNullable).filter(Objects::nonNull).toList());
        if (!presentOutputs.isEmpty()) {
            int output = (int) (12288 * state.torqueMultiplier);
            int toDistribute = Math.min(output, state.energy.getEnergyStored());
            int remaining = 0;
            int perPort = 4096;
            for (IEnergyStorage storage : presentOutputs) {
                int accepted = storage.receiveEnergy(Math.min(perPort, toDistribute), false);
                toDistribute -= accepted;
                remaining += accepted;
                if (toDistribute <= 0) { break; }
            }
            state.energy.setStoredEnergy(state.energy.getEnergyStored() - remaining);
        }
    }

    @Override
    public State createInitialState(IInitialMultiblockContext<State> context) { return new State(context); }

    @Override
    public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return AlternatorShape.GETTER; }

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
            if (position.posInMultiblock().equals(BlockPos.ZERO)) { position = new CapabilityPosition(ROTATIONAL_INPUT_POI, position.side()); }
            if (position.posInMultiblock().equals(ROTATIONAL_INPUT_POI) && (position.side() == null || position.side() == ROTATIONAL_INPUT_FACING || position.side() == ROTATIONAL_INPUT_FACING.getOpposite())) { return LazyOptional.of(MechanicalEnergyConsumer::new).cast(); }
        }
        return LazyOptional.empty();
    }

    private static class MechanicalEnergyConsumer implements IMechanicalEnergyConsumer {
        @Override
        public double getMass() { return BASE_MASS; }
        @Override
        public double getFriction() { return FRICTION; }
    }

    public static class State implements IMultiblockState {
        public final MutableEnergyStorage energy = new MutableEnergyStorage(ENERGY_CAPACITY, 0, 12288);
        private final List<CapabilityReference<IEnergyStorage>> energyOutputsLeft;
        private final List<CapabilityReference<IEnergyStorage>> energyOutputsRight;
        public boolean active = false;
        public int speed = 0;
        public float torqueMultiplier = 1f;
        public int maxSpeed = MAX_SPEED;
        public BooleanSupplier isSoundPlaying = () -> false;
        private final StoredCapability<IEnergyStorage> energyCap;

        public State(IInitialMultiblockContext<State> ctx) {
            this.energyCap = new StoredCapability<>(energy);
            ImmutableList.Builder<CapabilityReference<IEnergyStorage>> outputsLeft = ImmutableList.builder();
            ImmutableList.Builder<CapabilityReference<IEnergyStorage>> outputsRight = ImmutableList.builder();
            for (BlockPos pos : ENERGY_LEFT_POI) { outputsLeft.add(ctx.getCapabilityAt(ForgeCapabilities.ENERGY, pos, ENERGY_LEFT_FACING)); }
            for (BlockPos pos : ENERGY_RIGHT_POI) { outputsRight.add(ctx.getCapabilityAt(ForgeCapabilities.ENERGY, pos, ENERGY_RIGHT_FACING)); }
            this.energyOutputsLeft = outputsLeft.build();
            this.energyOutputsRight = outputsRight.build();
        }

        @Override
        public void writeSaveNBT(CompoundTag nbt) {
            EnergyHelper.serializeTo(energy, nbt);
            nbt.putBoolean("active", active);
            nbt.putInt("speed", speed);
            nbt.putFloat("torqueMultiplier", torqueMultiplier);
        }

        @Override
        public void readSaveNBT(CompoundTag nbt) {
            EnergyHelper.deserializeFrom(energy, nbt);
            active = nbt.getBoolean("active");
            speed = nbt.getInt("speed");
            torqueMultiplier = nbt.getFloat("torqueMultiplier");
            energy.setStoredEnergy(Math.max(0, energy.getEnergyStored()));
        }

        @Override
        public void writeSyncNBT(CompoundTag nbt) {
            nbt.putBoolean("active", active);
            nbt.putInt("speed", speed);
            nbt.putFloat("torqueMultiplier", torqueMultiplier);
        }

        @Override
        public void readSyncNBT(CompoundTag nbt) {
            active = nbt.getBoolean("active");
            speed = nbt.getInt("speed");
            torqueMultiplier = nbt.getFloat("torqueMultiplier");
        }
    }
}
