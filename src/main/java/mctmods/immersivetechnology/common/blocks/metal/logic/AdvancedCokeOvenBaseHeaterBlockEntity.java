package mctmods.immersivetechnology.common.blocks.metal.logic;

import mctmods.immersivetechnology.common.blocks.helper.*;
import mctmods.immersivetechnology.core.ClientConfig;
import mctmods.immersivetechnology.core.ServerConfig;
import mctmods.immersivetechnology.core.lib.ModSound;
import mctmods.immersivetechnology.core.registration.BlockEntities;
import mctmods.immersivetechnology.core.registration.ModBlocks;
import mctmods.immersivetechnology.core.registration.Sounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;

public class AdvancedCokeOvenBaseHeaterBlockEntity extends BaseBlockEntity implements IServerTickableBE, IClientTickableBE, BlockInterfaces.IDirectionalBE, BlockInterfaces.IHasDummyBlocks, IEnergyStorage, IModelOffsetProvider {

    private static final int MAX_ENERGY = ServerConfig.advancedCokeOvenBaseheaterMaxEnergy;
    private static final int ENERGY_CONSUMPTION = ServerConfig.advancedCokeOvenBaseheaterEnergyConsumption;
    private static final float MAX_FAN_SPEED = (float) ClientConfig.advancedCokeOvenBaseheaterMaxFanSpeed;
    private static final float FAN_ACCEL = (float) ClientConfig.advancedCokeOvenBaseheaterFanAccel;
    private static final float FAN_DECEL = (float) ClientConfig.advancedCokeOvenBaseheaterFanDecel;

    private final EnergyStorage energyStorage = new EnergyStorage(MAX_ENERGY);

    public Direction facing;
    public boolean dummy;
    public boolean active;
    public BlockPos masterPos;

    private AdvancedCokeOvenBaseHeaterBlockEntity cachedMaster;
    private boolean wasSpeedupCalledThisTick;
    private final Vec3 soundPosCache;

    private boolean isBreaking = false;

    private float fanRotation;
    private float prevFanRotation;
    private float fanSpeed;
    private float soundVolume;
    private float soundPitch;

    private BooleanSupplier soundHandle = () -> false;

    public AdvancedCokeOvenBaseHeaterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntities.ADVANCED_COKE_OVEN_BASEHEATER.get(), pos, state);
        this.dummy = state.getValue(ModProperties.MULTIBLOCKSLAVE);
        this.facing = state.getValue(ModProperties.FACING_HORIZONTAL);
        this.active = state.getValue(ModProperties.ACTIVE);
        this.masterPos = null;
        this.cachedMaster = null;
        this.wasSpeedupCalledThisTick = false;
        this.soundPosCache = Vec3.atCenterOf(pos);
        this.fanRotation = 0f;
        this.prevFanRotation = 0f;
        this.fanSpeed = 0f;
        this.soundVolume = 0f;
        this.soundPitch = 0.7f;
    }

    @Override public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide && dummy) findMaster();
    }

    @Override public void readCustomNBT(CompoundTag nbt, boolean descPacket) {
        dummy = nbt.getBoolean("dummy");
        facing = Direction.from3DDataValue(nbt.getInt("facing"));
        energyStorage.receiveEnergy(nbt.getInt("energy"), false);
        active = nbt.getBoolean("active");
        if (nbt.contains("masterPos")) {
            CompoundTag posTag = nbt.getCompound("masterPos");
            masterPos = new BlockPos(posTag.getInt("X"), posTag.getInt("Y"), posTag.getInt("Z"));
        }
        cachedMaster = null;
        if (descPacket) {
            markContainingBlockForUpdate(null);
            requestModelDataUpdate();
        }
    }

    @Override public void writeCustomNBT(CompoundTag nbt, boolean descPacket) {
        nbt.putBoolean("dummy", dummy);
        nbt.putInt("facing", facing.get3DDataValue());
        nbt.putInt("energy", energyStorage.getEnergyStored());
        nbt.putBoolean("active", active);
        if (masterPos != null) nbt.put("masterPos", NbtUtils.writeBlockPos(masterPos));
    }

    private void setActive(boolean newActive) {
        if (this.active == newActive) return;
        this.active = newActive;
        if (level == null || level.isClientSide) return;

        BlockPos masterPosLocal = dummy ? masterPos : getBlockPos();
        if (masterPosLocal == null) return;

        for (BlockPos p : new BlockPos[]{masterPosLocal, masterPosLocal.relative(facing.getClockWise()), masterPosLocal.relative(facing.getCounterClockWise())}) {
            BlockState oldState = level.getBlockState(p);
            if (oldState.getBlock() == ModBlocks.Metal.ADVANCED_COKE_OVEN_BASEHEATER.get() && oldState.getValue(ModProperties.ACTIVE) != newActive) level.setBlock(p, oldState.setValue(ModProperties.ACTIVE, newActive), 3);
        }
        setChanged();
        markContainingBlockForUpdate(null);
    }

    public boolean doSpeedup() {
        if (dummy) {
            BlockInterfaces.IGeneralMultiblock m = master();
            if (m instanceof AdvancedCokeOvenBaseHeaterBlockEntity masterBE) return masterBE.doSpeedup();
            return false;
        }

        if (level == null || wasSpeedupCalledThisTick) return active;
        wasSpeedupCalledThisTick = true;

        int consumed = ENERGY_CONSUMPTION;
        if (energyStorage.extractEnergy(consumed, true) == consumed) {
            setActive(true);
            energyStorage.extractEnergy(consumed, false);
            return true;
        } else {
            setActive(false);
            return false;
        }
    }

    @Override public void tickServer() {
        if (dummy || level == null) return;
        if (!wasSpeedupCalledThisTick) setActive(false);
        wasSpeedupCalledThisTick = false;
    }

    @Override public void tickClient() {
        if (dummy) return;

        prevFanRotation = fanRotation;
        if (active) fanSpeed = Math.min(fanSpeed + FAN_ACCEL, MAX_FAN_SPEED);
        else fanSpeed = Math.max(fanSpeed - FAN_DECEL, 0f);
        fanRotation += fanSpeed;
        fanRotation %= 360f;

        soundVolume = fanSpeed / MAX_FAN_SPEED;
        soundPitch = 0.7f + 0.3f * soundVolume;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        if (active || fanSpeed > 0.01f) {
            float att = (float) Math.max(player.distanceToSqr(soundPosCache) / 8, 1);
            float vol = Math.max(5f * soundVolume, 0.01f) / att;
            if (vol > 0.01f && !soundHandle.getAsBoolean()) {
                soundHandle = ModSound.startSound(
                        () -> active || fanSpeed > 0.01f,
                        () -> level != null && !isRemoved(),
                        soundPosCache,
                        Sounds.advancedCokeOvenFan,
                        () -> {
                            LocalPlayer p = Minecraft.getInstance().player;
                            if (p == null) return 0f;
                            float attenuation = (float) Math.max(p.distanceToSqr(soundPosCache) / 8, 1);
                            return Math.max(5f * soundVolume, 0.01f) / attenuation;
                        },
                        () -> soundPitch
                );
            }
        }
    }

    public float getFanRotation(float partialTicks) { return prevFanRotation + (fanRotation - prevFanRotation) * partialTicks; }

    @Override @Nullable public BlockInterfaces.IGeneralMultiblock master() {
        if (!dummy) return this;
        if (cachedMaster == null || cachedMaster.isRemoved()) findMaster();
        return cachedMaster;
    }

    public void findMaster() {
        if (level == null) return;
        if (!dummy) {
            masterPos = getBlockPos();
            cachedMaster = this;
            return;
        }
        if (masterPos != null) {
            BlockEntity existing = level.getBlockEntity(masterPos);
            if (existing instanceof AdvancedCokeOvenBaseHeaterBlockEntity m && !m.dummy) {
                cachedMaster = m;
                return;
            }
            masterPos = null;
        }
        BlockPos p = getBlockPos();
        for (Direction d : new Direction[]{facing.getClockWise(), facing.getCounterClockWise()}) {
            BlockPos candidate = p.relative(d);
            BlockEntity te = level.getBlockEntity(candidate);
            if (te instanceof AdvancedCokeOvenBaseHeaterBlockEntity m && !m.dummy) {
                masterPos = candidate;
                cachedMaster = m;
                return;
            }
        }
        masterPos = null;
        cachedMaster = null;
    }

    @Override public void breakDummies(BlockPos pos, BlockState state) {
        if (level == null || level.isClientSide || isBreaking) return;
        isBreaking = true;
        try {
            if (dummy) {
                if (masterPos == null) findMaster();
                if (masterPos != null && !masterPos.equals(getBlockPos())) {
                    BlockEntity te = level.getBlockEntity(masterPos);
                    if (te instanceof AdvancedCokeOvenBaseHeaterBlockEntity master) master.breakDummies(master.getBlockPos(), master.getBlockState());
                }
                return;
            }
            for (Direction d : new Direction[]{facing.getClockWise(), facing.getCounterClockWise()}) level.removeBlock(pos.relative(d), false);
            level.removeBlock(pos, false);
        } finally { isBreaking = false; }
    }

    @Override public void placeDummies(BlockPlaceContext context, BlockState state) {}

    @Override public boolean isDummy() { return dummy; }

    @Override public Direction getFacing() { return facing; }

    @Override public void setFacing(Direction f) { this.facing = f; }

    @Override public PlacementLimitation getFacingLimitation() { return PlacementLimitation.HORIZONTAL; }

    @Override public Direction getFacingForPlacement(BlockPlaceContext ctx) { return ctx.getHorizontalDirection().getOpposite(); }

    @Override public boolean canHammerRotate(Direction side, Vec3 hit, LivingEntity entity) { return false; }

    @Override public BlockPos getModelOffset(BlockState state, @Nullable Vec3i size) {
        if (!dummy) return BlockPos.ZERO;
        findMaster();
        if (masterPos == null) return BlockPos.ZERO;

        BlockPos rel = getBlockPos().subtract(masterPos);

        return switch (facing) {
            case NORTH -> new BlockPos(-rel.getX(), 0, 0);
            case SOUTH -> new BlockPos(rel.getX(), 0, 0);
            case EAST  -> new BlockPos(-rel.getZ(), 0, 0);
            case WEST  -> new BlockPos(rel.getZ(), 0, 0);
            default    -> BlockPos.ZERO;
        };
    }

    @Override @NotNull public ModelData getModelData() {
        BlockPos offset = getModelOffset(getBlockState(), null);
        return ModelData.builder().with(blusunrize.immersiveengineering.api.IEProperties.Model.SUBMODEL_OFFSET, offset).build();
    }

    @SuppressWarnings("unused")
    public IEnergyStorage getEnergyHandler(@Nullable Direction side) {
        if (side == null || side == Direction.UP) {
            if (!dummy) return this;
            BlockInterfaces.IGeneralMultiblock m = master();
            if (m instanceof AdvancedCokeOvenBaseHeaterBlockEntity masterBE && !masterBE.isRemoved()) {
                return new ForwardingEnergyStorage();
            }
        }
        return null;
    }

    @Override public int receiveEnergy(int maxReceive, boolean simulate) { return energyStorage.receiveEnergy(maxReceive, simulate); }
    @Override public int extractEnergy(int maxExtract, boolean simulate) { return 0; }
    @Override public int getEnergyStored() { return energyStorage.getEnergyStored(); }
    @Override public int getMaxEnergyStored() { return energyStorage.getMaxEnergyStored(); }
    @Override public boolean canExtract() { return false; }
    @Override public boolean canReceive() { return true; }

    @Override public void onChunkUnloaded() {
        cachedMaster = null;
        super.onChunkUnloaded();
    }

    private class ForwardingEnergyStorage implements IEnergyStorage {
        @Override public int receiveEnergy(int maxReceive, boolean simulate) { return 0; }
        @Override public int extractEnergy(int maxExtract, boolean simulate) { return 0; }
        @Override public int getEnergyStored() {
            BlockInterfaces.IGeneralMultiblock m = master();
            if (m instanceof AdvancedCokeOvenBaseHeaterBlockEntity masterBE) return masterBE.energyStorage.getEnergyStored();
            return 0;
        }
        @Override public int getMaxEnergyStored() { return MAX_ENERGY; }
        @Override public boolean canExtract() { return false; }
        @Override public boolean canReceive() { return false; }
    }
}
