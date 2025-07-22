package mctmods.immersivetechnology.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.energy.MutableEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.ShapeType;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.StoredCapability;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import com.google.common.collect.ImmutableList;
import mctmods.immersivetechnology.common.blocks.multiblocks.shapes.FullblockShape;
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
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ITAlternatorLogic implements IMultiblockLogic<ITAlternatorLogic.State>, IServerTickableComponent<ITAlternatorLogic.State>, IClientTickableComponent<ITAlternatorLogic.State> {
    private static final List<BlockPos> ENERGY_OUTPUTS_RIGHT = List.of(new BlockPos(2, 0, 3), new BlockPos(2, 1, 3), new BlockPos(2, 2, 3));
    private static final List<BlockPos> ENERGY_OUTPUTS_LEFT = List.of(new BlockPos(0, 0, 3), new BlockPos(0, 1, 3), new BlockPos(0, 2, 3));

    public static final int ENERGY_CAPACITY = 1200000;
    public static final BlockPos ROTATIONAL_INPUT_OFFSET = new BlockPos(1,1,-1);

    @Override
    public void tickClient(IMultiblockContext<State> ctx) {
        final State state = ctx.getState();

        if (!state.isSoundPlaying.getAsBoolean()) {
            final Vec3 soundPos = ctx.getLevel().toAbsolute(new Vec3(1.5, 1.5, 1.5));
            state.isSoundPlaying = ITMultiblockSound.startSound(
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
        IMultiblockLevel multiblockLevel = ctx.getLevel();
        Level level = multiblockLevel.getRawLevel();

        BlockPos turbineAbsolutePos = multiblockLevel.toAbsolute(ROTATIONAL_INPUT_OFFSET);

        BlockEntity entity = level.getBlockEntity(turbineAbsolutePos);

        int turbineSpeed = 0;
        float turbineTorque = 1f;
        boolean hasTurbine = false;
        state.active = false;

        if (entity instanceof IMultiblockBE<?> mbBE) {
            BlockPos posInMB = mbBE.getHelper().getPositionInMB();
            BlockPos masterPosInMB = mbBE.getHelper().getMultiblock().masterPosInMB();
            BlockPos masterAbsPos = entity.getBlockPos().subtract(posInMB).offset(masterPosInMB.getX(), masterPosInMB.getY(), masterPosInMB.getZ());
            BlockEntity masterBE = level.getBlockEntity(masterAbsPos);
            if (masterBE instanceof IMultiblockBE<?> masterMbBE) {
                IMultiblockState turbineState = masterMbBE.getHelper().getState();
                if (turbineState instanceof ITSteamTurbineLogic.State steamState) {
                    turbineSpeed = steamState.speed;
                    hasTurbine = true;
                    if (turbineSpeed > 0) { state.active = true; }
                }
                else if (turbineState instanceof ITGasTurbineLogic.State gasState) {
                    turbineSpeed = gasState.speed;
                    hasTurbine = true;
                    if (turbineSpeed > 0) { state.active = true; }
                }
            }
        }

        if (hasTurbine) {
            state.speed = turbineSpeed;
            state.torqueMult = turbineTorque;
        }
        else if (state.speed > 0) {
            state.speed = Math.max(state.speed - 6, 0);
            if (state.speed > 0) { state.active = true; }
        }

        generateEnergy(state);
        outputEnergy(state);

        for (BlockPos pos : ENERGY_OUTPUTS_LEFT) {
            BlockPos absolutePos = ctx.getLevel().toAbsolute(pos);
            Direction side = ctx.getLevel().toAbsolute(RelativeBlockFace.RIGHT);
            assert side != null;
            BlockEntity adjacent = level.getBlockEntity(absolutePos.relative(side));
            if (adjacent != null) {
                LazyOptional<IEnergyStorage> handlerOpt = adjacent.getCapability(ForgeCapabilities.ENERGY, side.getOpposite());
                if (handlerOpt.isPresent()) {
                    IEnergyStorage handler = handlerOpt.orElseThrow(RuntimeException::new);
                    int maxPush = Math.min(4096, state.energy.getEnergyStored());
                    int pushed = handler.receiveEnergy(maxPush, false);
                    if (pushed > 0) {
                        state.energy.setStoredEnergy(state.energy.getEnergyStored() - pushed);
                    }
                }
            }
        }
        for (BlockPos pos : ENERGY_OUTPUTS_RIGHT) {
            BlockPos absolutePos = ctx.getLevel().toAbsolute(pos);
            Direction side = ctx.getLevel().toAbsolute(RelativeBlockFace.LEFT);
            assert side != null;
            BlockEntity adjacent = level.getBlockEntity(absolutePos.relative(side));
            if (adjacent != null) {
                LazyOptional<IEnergyStorage> handlerOpt = adjacent.getCapability(ForgeCapabilities.ENERGY, side.getOpposite());
                if (handlerOpt.isPresent()) {
                    IEnergyStorage handler = handlerOpt.orElseThrow(RuntimeException::new);
                    int maxPush = Math.min(4096, state.energy.getEnergyStored());
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
            int generated = (int) Math.round(Math.pow(ratio, 2.0) * state.torqueMult * 12288);
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
            int output = (int) (12288 * state.torqueMult);
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
    public State createInitialState(IInitialMultiblockContext<State> context) {
        return new ITAlternatorLogic.State(context);
    }

    @Override
    public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) {
        return FullblockShape.GETTER;
    }

    @Override
    public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) {
        if (cap == ForgeCapabilities.ENERGY) {
            if (position.side() == null || (position.side() == RelativeBlockFace.RIGHT && ENERGY_OUTPUTS_RIGHT.contains(position.posInMultiblock()))) {
                return ctx.getState().energyCap.cast(ctx);
            }
            if (position.side() == RelativeBlockFace.LEFT && ENERGY_OUTPUTS_LEFT.contains(position.posInMultiblock())) {
                return ctx.getState().energyCap.cast(ctx);
            }
        }
        return LazyOptional.empty();
    }

    public static class State implements IMultiblockState {
        public final MutableEnergyStorage energy = new MutableEnergyStorage(ENERGY_CAPACITY, 0, 12288);
        private final List<CapabilityReference<IEnergyStorage>> energyOutputs1;
        private final List<CapabilityReference<IEnergyStorage>> energyOutputs2;

        public boolean active = false;
        public int speed = 0;
        public float torqueMult = 1f;
        public int maxSpeed = 1800;
        public BooleanSupplier isSoundPlaying = () -> false;

        private final StoredCapability<IEnergyStorage> energyCap;

        public State(IInitialMultiblockContext<State> ctx) {
            this.energyCap = new StoredCapability<>(energy);
            ImmutableList.Builder<CapabilityReference<IEnergyStorage>> outputs1 = ImmutableList.builder();
            ImmutableList.Builder<CapabilityReference<IEnergyStorage>> outputs2 = ImmutableList.builder();
            for (BlockPos pos : ENERGY_OUTPUTS_LEFT) {
                outputs1.add(ctx.getCapabilityAt(ForgeCapabilities.ENERGY, pos, RelativeBlockFace.RIGHT));
            }
            for (BlockPos pos : ENERGY_OUTPUTS_RIGHT) {
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
            nbt.putFloat("torqueMult", torqueMult);
        }

        @Override
        public void readSaveNBT(CompoundTag nbt) {
            EnergyHelper.deserializeFrom(energy, nbt);
            active = nbt.getBoolean("active");
            speed = nbt.getInt("speed");
            torqueMult = nbt.getFloat("torqueMult");
            energy.setStoredEnergy(Math.max(0, energy.getEnergyStored()));
        }

        @Override
        public void writeSyncNBT(CompoundTag nbt) {
            nbt.putBoolean("active", active);
            nbt.putInt("speed", speed);
            nbt.putFloat("torqueMult", torqueMult);
        }

        @Override
        public void readSyncNBT(CompoundTag nbt) {
            active = nbt.getBoolean("active");
            speed = nbt.getInt("speed");
            torqueMult = nbt.getFloat("torqueMult");
        }
    }
}
