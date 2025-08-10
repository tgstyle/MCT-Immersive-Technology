package mctmods.immersivetechnology.common.blocks.multiblocks.logic;

import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.lib.ITMultiblockSound;
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

public class AlternatorLogic implements IMultiblockLogic<AlternatorLogic.State>, IServerTickableComponent<AlternatorLogic.State>, IClientTickableComponent<AlternatorLogic.State> {
    public static final int ENERGY_CAPACITY = 1200000;

    public static final BlockPos RUNNING_SOUND_POS = new BlockPos(1, 1, 1);

    private static final List<BlockPos> ENERGY_OUTPUT_POS_RIGHT = List.of(new BlockPos(2, 0, 3), new BlockPos(2, 1, 3), new BlockPos(2, 2, 3));
    private static final List<BlockPos> ENERGY_OUTPUT_POS_LEFT = List.of(new BlockPos(0, 0, 3), new BlockPos(0, 1, 3), new BlockPos(0, 2, 3));

    public static final BlockPos ROTATIONAL_INPUT_POS = new BlockPos(1, 1, 0);
    public static final double MASS = 2;

    @Override
    public void tickClient(IMultiblockContext<State> ctx) {
        final State state = ctx.getState();
        if (!state.isSoundPlaying.getAsBoolean()) {
            final Vec3 soundPos = ctx.getLevel().toAbsolute(new Vec3(RUNNING_SOUND_POS.getX() + 0.5, RUNNING_SOUND_POS.getY() + 0.5, RUNNING_SOUND_POS.getZ() + 0.5));
            state.isSoundPlaying = ITMultiblockSound.startSound(
                    () -> state.active,
                    ctx.isValid(),
                    soundPos,
                    ITSounds.alternator,
                    () -> {
                        LocalPlayer player = Minecraft.getInstance().player;
                        if (player == null) return 0f;
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

        Direction  inputFacing = ctx.getLevel().getOrientation().front();
        BlockPos inputPortAbs = ctx.getLevel().toAbsolute(ROTATIONAL_INPUT_POS);
        assert  inputFacing != null;
        BlockPos providerAbsolutePos = inputPortAbs.relative( inputFacing);
        BlockEntity entity = level.getBlockEntity(providerAbsolutePos);

        int turbineSpeed = 0;
        float turbineTorque = 1f;
        boolean hasProvider = false;
        state.active = false;

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
        }
        else if (state.speed > 0) {
            state.speed = Math.max(state.speed - 6, 0);
            if (state.speed > 0) { state.active = true; }
        }

        generateEnergy(state);
        outputEnergy(state);

        for (BlockPos pos : ENERGY_OUTPUT_POS_LEFT) {
            BlockPos absolutePos = ctx.getLevel().toAbsolute(pos);
            Direction side = ctx.getLevel().toAbsolute(RelativeBlockFace.RIGHT);
            assert side != null;
            BlockEntity adjacent = level.getBlockEntity(absolutePos.relative(side));
            if (adjacent != null) {
                LazyOptional<IEnergyStorage> handlerOpt = adjacent.getCapability(ForgeCapabilities.ENERGY, side.getOpposite());
                if (handlerOpt.isPresent()) {
                    IEnergyStorage handler = handlerOpt.orElseThrow(RuntimeException::new);
                    int maxPush = Math.min(2048, state.energy.getEnergyStored());
                    int pushed = handler.receiveEnergy(maxPush, false);
                    if (pushed > 0) {
                        state.energy.setStoredEnergy(state.energy.getEnergyStored() - pushed);
                    }
                }
            }
        }
        for (BlockPos pos : ENERGY_OUTPUT_POS_RIGHT) {
            BlockPos absolutePos = ctx.getLevel().toAbsolute(pos);
            Direction side = ctx.getLevel().toAbsolute(RelativeBlockFace.LEFT);
            assert side != null;
            BlockEntity adjacent = level.getBlockEntity(absolutePos.relative(side));
            if (adjacent != null) {
                LazyOptional<IEnergyStorage> handlerOpt = adjacent.getCapability(ForgeCapabilities.ENERGY, side.getOpposite());
                if (handlerOpt.isPresent()) {
                    IEnergyStorage handler = handlerOpt.orElseThrow(RuntimeException::new);
                    int maxPush = Math.min(2048, state.energy.getEnergyStored());
                    int pushed = handler.receiveEnergy(maxPush, false);
                    if (pushed > 0) {
                        state.energy.setStoredEnergy(state.energy.getEnergyStored() - pushed);
                    }
                }
            }
        }

        if (state.active) { ctx.markMasterDirty(); }
        ctx.requestMasterBESync();
    }

    private void generateEnergy(State state) {
        if (state.speed < 900) return;
        double ratio = (double) state.speed / state.maxSpeed;
        if (ratio > 0.0) {
            int generated = (int) Math.round(Math.pow(ratio, 2.0) * state.torqueMultiplier * 12288);
            int current = state.energy.getEnergyStored();
            int newEnergy = Math.min(state.energy.getMaxEnergyStored(), current + generated);
            state.energy.setStoredEnergy(newEnergy);
        }
    }

    private void outputEnergy(State state) {
        List<IEnergyStorage> presentOutputs = state.energyOutputs1.stream()
                .map(CapabilityReference::getNullable)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        presentOutputs.addAll(state.energyOutputs2.stream()
                .map(CapabilityReference::getNullable)
                .filter(Objects::nonNull)
                .toList());

        if (!presentOutputs.isEmpty()) {
            int output = (int) (12288 * state.torqueMultiplier);
            int toDistribute = Math.min(output, state.energy.getEnergyStored());
            int remaining = 0;
            int perPort = 4096;
            for (IEnergyStorage storage : presentOutputs) {
                int accepted = storage.receiveEnergy(Math.min(perPort, toDistribute), false);
                toDistribute -= accepted;
                remaining += accepted;
                if (toDistribute <= 0) break;
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
        if (cap == ForgeCapabilities.ENERGY) {
            if (position.side() == null || (position.side() == RelativeBlockFace.RIGHT && ENERGY_OUTPUT_POS_RIGHT.contains(position.posInMultiblock()))) { return ctx.getState().energyCap.cast(ctx); }
            if (position.side() == RelativeBlockFace.LEFT && ENERGY_OUTPUT_POS_LEFT.contains(position.posInMultiblock())) { return ctx.getState().energyCap.cast(ctx); }
        }
        if (cap == MechanicalCapabilities.MECHANICAL_CONSUMER_CAPABILITY) {
            if (position.posInMultiblock().equals(BlockPos.ZERO)) { position = new CapabilityPosition(ROTATIONAL_INPUT_POS, position.side()); }
            if (position.posInMultiblock().equals(ROTATIONAL_INPUT_POS) && (position.side() == null || position.side() == RelativeBlockFace.FRONT || position.side() == RelativeBlockFace.BACK)) {
                return LazyOptional.of(MechanicalEnergyConsumer::new).cast();
            }
        }
        return LazyOptional.empty();
    }

    private static class MechanicalEnergyConsumer implements IMechanicalEnergyConsumer {
        @Override
        public double getMass() { return MASS; }
    }

    public static class State implements IMultiblockState {
        public final MutableEnergyStorage energy = new MutableEnergyStorage(ENERGY_CAPACITY, 0, 12288);
        private final List<CapabilityReference<IEnergyStorage>> energyOutputs1;
        private final List<CapabilityReference<IEnergyStorage>> energyOutputs2;

        public boolean active = false;
        public int speed = 0;
        public float torqueMultiplier = 1f;
        public int maxSpeed = 1800;
        public BooleanSupplier isSoundPlaying = () -> false;

        private final StoredCapability<IEnergyStorage> energyCap;

        public State(IInitialMultiblockContext<State> ctx) {
            this.energyCap = new StoredCapability<>(energy);
            ImmutableList.Builder<CapabilityReference<IEnergyStorage>> outputs1 = ImmutableList.builder();
            ImmutableList.Builder<CapabilityReference<IEnergyStorage>> outputs2 = ImmutableList.builder();
            for (BlockPos pos : ENERGY_OUTPUT_POS_LEFT) {
                outputs1.add(ctx.getCapabilityAt(ForgeCapabilities.ENERGY, pos, RelativeBlockFace.RIGHT));
            }
            for (BlockPos pos : ENERGY_OUTPUT_POS_RIGHT) {
                outputs2.add(ctx.getCapabilityAt(ForgeCapabilities.ENERGY, pos, RelativeBlockFace.LEFT));
            }
            this.energyOutputs1 = outputs1.build();
            this.energyOutputs2 = outputs2.build();
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
