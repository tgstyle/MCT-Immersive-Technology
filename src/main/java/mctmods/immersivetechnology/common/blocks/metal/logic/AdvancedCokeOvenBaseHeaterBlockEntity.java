package mctmods.immersivetechnology.common.blocks.metal.logic;

import mctmods.immersivetechnology.common.blocks.helper.ITBaseBlockEntity;
import mctmods.immersivetechnology.common.blocks.helper.ITBlockInterfaces;
import mctmods.immersivetechnology.common.blocks.helper.ITClientTickableBE;
import mctmods.immersivetechnology.common.blocks.helper.ITPlacementLimitation;
import mctmods.immersivetechnology.common.blocks.helper.ITProperties;
import mctmods.immersivetechnology.common.blocks.helper.ITServerTickableBE;
import mctmods.immersivetechnology.core.lib.ITSound;
import mctmods.immersivetechnology.core.registration.ITBlockEntities;
import mctmods.immersivetechnology.core.registration.ITSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.function.BooleanSupplier;

public class AdvancedCokeOvenBaseHeaterBlockEntity extends ITBaseBlockEntity implements ITServerTickableBE, ITClientTickableBE, ITBlockInterfaces.IDirectionalBE, ITBlockInterfaces.IHasDummyBlocks, IEnergyStorage {
    private static final int MAX_ENERGY = 8000;
    private static final int ENERGY_CONSUMPTION = 32;

    public Direction facing;
    private final EnergyStorage energyStorage = new EnergyStorage(MAX_ENERGY);
    public boolean dummy;
    public boolean active;
    public BlockPos masterPos;

    private boolean isBreaking = false;

    private float fanRotation = 0f;
    private float prevFanRotation = 0f;
    private float soundVolume = 0f;
    private BooleanSupplier soundHandle = () -> false;

    public AdvancedCokeOvenBaseHeaterBlockEntity(BlockPos pos, BlockState state) {
        super(ITBlockEntities.ADVANCED_COKE_OVEN_BASEHEATER.get(), pos, state);
        this.dummy = state.getValue(ITProperties.MULTIBLOCKSLAVE);
        this.facing = state.getValue(ITProperties.FACING_HORIZONTAL);
        this.active = state.getValue(ITProperties.ACTIVE);
        this.masterPos = null;
    }

    @Override public void setLevel(@NotNull net.minecraft.world.level.Level level) {
        super.setLevel(level);
    }

    @Override public void onLoad() {
        super.onLoad();
    }

    @Override public void readCustomNBT(CompoundTag nbt, boolean descPacket) {
        dummy = nbt.getBoolean("dummy");
        facing = Direction.from3DDataValue(nbt.getInt("facing"));
        energyStorage.receiveEnergy(nbt.getInt("energy"), false);
        active = nbt.getBoolean("active");
        if (nbt.contains("masterPos")) masterPos = NbtUtils.readBlockPos(nbt.getCompound("masterPos"));
        if (descPacket) markContainingBlockForUpdate(null);
    }

    @Override public void writeCustomNBT(CompoundTag nbt, boolean descPacket) {
        nbt.putBoolean("dummy", dummy);
        nbt.putInt("facing", facing.get3DDataValue());
        nbt.putInt("energy", energyStorage.getEnergyStored());
        nbt.putBoolean("active", active);
        if (masterPos != null) nbt.put("masterPos", NbtUtils.writeBlockPos(masterPos));
    }

    public boolean doSpeedup() {
        if (dummy) return false;
        int consumed = ENERGY_CONSUMPTION;
        if (energyStorage.extractEnergy(consumed, true) == consumed) {
            if (!active) { active = true; updateActiveState(); updateDummies(); }
            energyStorage.extractEnergy(consumed, false);
            return true;
        } else if (active) { active = false; updateActiveState(); updateDummies(); }
        return false;
    }

    private void updateActiveState() {
        BlockState state = getBlockState();
        if (state.getValue(ITProperties.ACTIVE) != active) {
            setState(state.setValue(ITProperties.ACTIVE, active));
        }
    }

    public void updateDummies() {
        if (level == null || level.isClientSide || dummy) return;
        BlockPos p = getBlockPos();
        for (Direction d : new Direction[]{facing.getClockWise(), facing.getCounterClockWise()}) {
            level.sendBlockUpdated(p.relative(d), level.getBlockState(p.relative(d)), level.getBlockState(p.relative(d)), 3);
        }
    }

    @SuppressWarnings("unchecked")
    @Override @Nonnull public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ENERGY) {
            if (!dummy && side == Direction.UP) { return LazyOptional.of(() -> (T) this); }
        }
        return super.getCapability(capability, side);
    }

    @Override public int receiveEnergy(int maxReceive, boolean simulate) { return energyStorage.receiveEnergy(maxReceive, simulate); }
    @Override public int extractEnergy(int maxExtract, boolean simulate) { return 0; }
    @Override public int getEnergyStored() { return energyStorage.getEnergyStored(); }
    @Override public int getMaxEnergyStored() { return energyStorage.getMaxEnergyStored(); }
    @Override public boolean canExtract() { return false; }
    @Override public boolean canReceive() { return true; }

    @Override public Direction getFacing() { return facing; }

    @Override public void setFacing(Direction f) { this.facing = f; }

    @Override public ITPlacementLimitation getFacingLimitation() { return ITPlacementLimitation.HORIZONTAL; }

    @Override public Direction getFacingForPlacement(BlockPlaceContext ctx) {
        return ctx.getHorizontalDirection().getOpposite();
    }

    @Override public boolean canHammerRotate(Direction side, Vec3 hit, LivingEntity entity) { return false; }

    @Override public void placeDummies(BlockPlaceContext context, BlockState state) {}

    @Override public boolean isDummy() { return dummy; }

    @Override public void breakDummies(BlockPos pos, BlockState state) {
        if (level == null || level.isClientSide || isBreaking) { return; }
        isBreaking = true;
        try {
            if (dummy) {
                if (masterPos == null) { findMaster(); }
                if (masterPos != null && !masterPos.equals(getBlockPos())) {
                    BlockEntity te = level.getBlockEntity(masterPos);
                    if (te instanceof AdvancedCokeOvenBaseHeaterBlockEntity master) {
                        master.breakDummies(master.getBlockPos(), master.getBlockState());
                    }
                }
                return;
            }
            for (Direction d : new Direction[]{facing.getClockWise(), facing.getCounterClockWise()}) {
                level.removeBlock(pos.relative(d), false);
            }
            level.removeBlock(pos, false);
        } finally {
            isBreaking = false;
        }
    }

    public void findMaster() {
        if (level == null) return;
        if (!dummy) {
            masterPos = getBlockPos();
            return;
        }
        if (masterPos != null) return;
        BlockPos p = getBlockPos();
        for (Direction d : new Direction[]{facing.getClockWise(), facing.getCounterClockWise()}) {
            BlockPos candidate = p.relative(d);
            BlockEntity te = level.getBlockEntity(candidate);
            if (te instanceof AdvancedCokeOvenBaseHeaterBlockEntity m && !m.dummy) {
                masterPos = candidate;
                return;
            }
        }
        masterPos = null;
    }

    @Override @Nullable public ITBlockInterfaces.IGeneralMultiblock master() {
        if (!dummy) return this;
        if (masterPos == null) findMaster();
        if (masterPos != null && !masterPos.equals(getBlockPos())) {
            if (level == null) return null;
            BlockEntity te = level.getBlockEntity(masterPos);
            if (te instanceof ITBlockInterfaces.IGeneralMultiblock multiblock) {
                return multiblock;
            }
        }
        return null;
    }

    @Override public void tickServer() {
        if (dummy || !active || level == null || level.isClientSide) return;
        BlockPos attachedPos = getBlockPos().relative(facing);
        BlockEntity te = level.getBlockEntity(attachedPos);
        if (te == null) {
            active = false;
            updateActiveState();
            updateDummies();
        }
    }

    @Override public void tickClient() {
        if (dummy) return;
        prevFanRotation = fanRotation;
        if (active) {
            fanRotation += 35f;
            fanRotation %= 360;
        }
        soundVolume = Mth.clamp(soundVolume + (active ? 0.01f : -0.01f), 0f, 1f);

        if (active && soundVolume > 0.01f && !soundHandle.getAsBoolean()) {
            soundHandle = ITSound.startSound(
                    () -> active,
                    () -> level != null && !isRemoved(),
                    Vec3.atCenterOf(getBlockPos()),
                    ITSounds.advancedCokeOvenFan,
                    () -> Math.max(5f * soundVolume, 0.01f),
                    () -> 1.0f
            );
        }
    }

    public float getFanRotation(float partialTicks) {
        return prevFanRotation + (fanRotation - prevFanRotation) * partialTicks;
    }

    @Override public void onChunkUnloaded() {
        super.onChunkUnloaded();
    }
}
