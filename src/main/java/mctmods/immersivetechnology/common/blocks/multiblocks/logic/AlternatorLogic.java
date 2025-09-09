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
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import mctmods.immersivetechnology.api.MechanicalCapabilities;
import mctmods.immersivetechnology.api.capability.IMechanicalEnergyConsumer;
import mctmods.immersivetechnology.api.capability.IMechanicalEnergyProvider;
import mctmods.immersivetechnology.common.blocks.multiblocks.shapes.AlternatorShape;
import mctmods.immersivetechnology.common.util.multiblock.MultiblockData;
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AlternatorLogic implements IMultiblockLogic<AlternatorLogic.State>, IServerTickableComponent<AlternatorLogic.State>, IClientTickableComponent<AlternatorLogic.State> {
    public static final int ENERGY_CAPACITY = 1200000;
    private static final double BASE_MASS = 2;
    private static final double FRICTION = 12;
    private static final int MAX_SPEED = 1800;
    private static final int WIDTH = 3;
    private static final int LENGTH = 4;
    private static final List<PoIJSONSchema> RAW_POIS;

    static {
        RAW_POIS = new ArrayList<>();
        try {
            InputStream is = AlternatorLogic.class.getResourceAsStream("/assets/immersivetechnology/multiblocks/alternator.json");
            if (is != null) {
                JsonReader reader = new JsonReader(new InputStreamReader(is));
                Gson gson = new Gson();
                MultiblockData schema = gson.fromJson(reader, MultiblockData.class);
                reader.close();
                for (PoIJSONSchema poi : schema.pointsOfInterest) {
                    if (poi.facingString != null) { poi.relativeFace = RelativeBlockFace.valueOf(poi.facingString.toUpperCase()); }
                    else { poi.relativeFace = null; }
                }
                RAW_POIS.addAll(Arrays.asList(schema.pointsOfInterest));
            } else {
                ITLib.IT_LOGGER.error("Alternator JSON resource not found at /assets/immersivetechnology/multiblocks/alternator.json");
            }
        } catch (Exception e) { ITLib.IT_LOGGER.error("Error loading Alternator POI from JSON", e); }
    }

    @Override
    public void tickClient(IMultiblockContext<State> ctx) {
        final State state = ctx.getState();
        if (!state.isSoundPlaying.getAsBoolean()) {
            List<PoICache> soundPois = state.poiMap.get("running_sound");
            if (soundPois != null && !soundPois.isEmpty()) {
                PoICache soundPoi = soundPois.get(0);
                final Vec3 soundPos = ctx.getLevel().toAbsolute(new Vec3(soundPoi.pos.getX() + 0.5, soundPoi.pos.getY() + 0.5, soundPoi.pos.getZ() + 0.5));
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
        List<PoICache> inputPois = state.poiMap.get("rotational_input");
        if (inputPois != null && !inputPois.isEmpty()) {
            PoICache inputPoi = inputPois.get(0);
            Direction inputFacing = ctx.getLevel().toAbsolute(inputPoi.facing);
            BlockPos inputPortAbs = ctx.getLevel().toAbsolute(inputPoi.pos);
            if (inputFacing != null) {
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
        for (String name : state.poiMap.keySet()) {
            if (name.startsWith("energy_")) {
                for (PoICache p : state.poiMap.get(name)) {
                    BlockPos absolutePos = ctx.getLevel().toAbsolute(p.pos);
                    Direction side = ctx.getLevel().toAbsolute(p.facing);
                    if (side != null) {
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
        List<IEnergyStorage> presentOutputs = state.energyOutputs.stream()
                .map(CapabilityReference::getNullable)
                .filter(Objects::nonNull)
                .toList();
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
            for (String name : state.poiMap.keySet()) {
                if (name.startsWith("energy_")) {
                    for (PoICache p : state.poiMap.get(name)) {
                        if (p.facing != null && (position.side() == null || (position.side() == p.facing && p.pos.equals(position.posInMultiblock())))) { return state.energyCap.cast(ctx); }
                    }
                }
            }
        }
        if (cap == MechanicalCapabilities.MECHANICAL_CONSUMER_CAPABILITY) {
            List<PoICache> inputPois = state.poiMap.get("rotational_input");
            if (inputPois != null && !inputPois.isEmpty()) {
                PoICache inputP = inputPois.get(0);
                if (inputP.facing != null) {
                    RelativeBlockFace relInput = inputP.facing;
                    RelativeBlockFace relBack = relInput.getOpposite();
                    if (position.posInMultiblock().equals(BlockPos.ZERO)) { position = new CapabilityPosition(inputP.pos, position.side()); }
                    if (position.posInMultiblock().equals(inputP.pos) && (position.side() == null || position.side() == relInput || position.side() == relBack)) { return LazyOptional.of(MechanicalEnergyConsumer::new).cast(); }
                }
            }
        }
        return LazyOptional.empty();
    }

    private static class MechanicalEnergyConsumer implements IMechanicalEnergyConsumer {
        @Override
        public double getMass() { return BASE_MASS; }

        @Override
        public double getFriction() { return FRICTION; }
    }

    private static BlockPos unflatten(int index) {
        int y = index / (WIDTH * LENGTH);
        int temp = index % (WIDTH * LENGTH);
        int z = temp / WIDTH;
        int x = temp % WIDTH;
        return new BlockPos(x, y, z);
    }

    public static class State implements IMultiblockState {
        public final MutableEnergyStorage energy = new MutableEnergyStorage(ENERGY_CAPACITY, 0, 12288);
        private final List<CapabilityReference<IEnergyStorage>> energyOutputs;
        public final Map<String, List<PoICache>> poiMap;
        public boolean active = false;
        public int speed = 0;
        public float torqueMultiplier = 1f;
        public int maxSpeed = MAX_SPEED;
        public BooleanSupplier isSoundPlaying = () -> false;
        private final StoredCapability<IEnergyStorage> energyCap;

        public State(IInitialMultiblockContext<State> ctx) {
            this.energyCap = new StoredCapability<>(energy);
            List<PoICache> temp = new ArrayList<>();
            for (PoIJSONSchema raw : RAW_POIS) { temp.add(new PoICache(raw.name, raw.position, raw.relativeFace)); }
            this.poiMap = temp.stream().collect(Collectors.groupingBy(p -> p.name));
            ImmutableList.Builder<CapabilityReference<IEnergyStorage>> outputs = ImmutableList.builder();
            for (String name : poiMap.keySet()) {
                if (name.startsWith("energy_")) {
                    for (PoICache p : poiMap.get(name)) {
                        if (p.facing != null) { outputs.add(ctx.getCapabilityAt(ForgeCapabilities.ENERGY, p.pos, p.facing)); }
                    }
                }
            }
            this.energyOutputs = outputs.build();
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

    public record PoICache(String name, BlockPos pos, RelativeBlockFace facing) {
        public PoICache(String name, int flatPosition, RelativeBlockFace localFacing) {
            this(name, unflatten(flatPosition), localFacing);
        }
    }
}
