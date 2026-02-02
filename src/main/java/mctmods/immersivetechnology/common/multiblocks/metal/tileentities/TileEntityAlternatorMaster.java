package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorageAdvanced;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.api.client.MechanicalEnergyAnimation;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartAlternator;
import mctmods.immersivetechnology.common.shared.interfaces.ITBlockInterfaces.IMechanicalEnergy;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.common.util.multiblock.PoICache;
import mctmods.immersivetechnology.common.util.multiblock.PoIJSONSchema;
import mctmods.immersivetechnology.common.util.network.BinaryMessageTileSync;
import mctmods.immersivetechnology.common.util.network.IBinaryMessageReceiver;
import mctmods.immersivetechnology.common.util.network.MessageStopSound;
import mctmods.immersivetechnology.common.util.sound.ITSoundHandler;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityAlternatorMaster extends TileEntityAlternatorSlave implements IBinaryMessageReceiver, IComparatorOverride {

    private static final int maxSpeed = Multiblocks.mechanicalEnergy.mechanicalEnergy_speed_max;
    private static final float maxRotationSpeed = Multiblocks.steamTurbine.steamTurbine_speed_maxRotation;
    private static final int rfPerTick = Multiblocks.alternator.alternator_energy_perTick;
    private static final double rfExponent = Multiblocks.alternator.alternator_exponent;
    private static final double rfThreshold = Multiblocks.alternator.alternator_threshold;
    private static final int rfPerTickPerPort = rfPerTick / 6;
    private static final int speedLossPerTick = Multiblocks.steamTurbine.steamTurbine_speed_lossPerTick;
    private static final boolean soundRPM = Multiblocks.alternator.alternator_sound_RPM;

    public FluxStorageAdvanced energyStorage = new FluxStorageAdvanced(Multiblocks.alternator.alternator_energy_capacitorSize, rfPerTick, rfPerTickPerPort);
    public int speed = 0;
    public float torqueMult = 1f;
    public IMechanicalEnergy provider;
    public MechanicalEnergyAnimation animation = new MechanicalEnergyAnimation();

    private float soundVolume = 0f;
    private int soundGracePeriod = 0;
    private float targetSoundLevel = 0f;
    private int oldEnergy = 0;
    private int oldSpeed = 0;
    private int oldComparatorOutput = 0;
    private int tickCountdown = 5;

    private boolean needsPoIInit = true;
    private boolean needsNotify = true;

    private final PoICache[] energyOutputs = new PoICache[6];
    private PoICache mechanicalInput0;
    private PoICache redstone0;
    private BlockPos mechanicalInputPos0;
    private BlockPos soundPos0;
    private final BlockPos[] energyOutputPositions = new BlockPos[6];

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        energyStorage.readFromNBT(nbt);
        animation.readFromNBT(nbt);
        if (!descPacket && formed) {
            needsPoIInit = true;
            needsNotify = true;
        }
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        energyStorage.writeToNBT(nbt);
        animation.writeToNBT(nbt);
    }

    @Override public void update() {
        if (!formed) return;

        if (needsPoIInit || mechanicalInput0 == null || energyOutputs[0] == null) {
            InitializePoIs();
            needsPoIInit = false;
        }

        if (needsNotify) {
            notifyIONeighbors();
            needsNotify = false;
        }

        if (world.isRemote) {
            float oldMomentum = animation.getAnimationMomentum();
            float rotationSpeed = speed == 0 ? 0f : ((float)speed / maxSpeed) * maxRotationSpeed;
            animation.setAnimationMomentum(rotationSpeed);
            animation.setAnimationRotation(animation.getAnimationRotation() + oldMomentum);

            targetSoundLevel = soundRPM ? (float)speed / maxSpeed : (float)energyStorage.getEnergyStored() / energyStorage.getMaxEnergyStored();

            if (soundVolume < targetSoundLevel) {
                soundVolume = Math.min(soundVolume + 0.01f, targetSoundLevel);
                soundGracePeriod = 60;
            } else if (soundVolume > targetSoundLevel) {
                if (soundGracePeriod > 0) soundGracePeriod--;
                else soundVolume = Math.max(soundVolume - 0.01f, targetSoundLevel);
            }

            if (soundPos0 != null) {
                if (soundVolume <= 0f) {
                    ITSoundHandler.StopSound(soundPos0);
                } else {
                    EntityPlayerSP player = Minecraft.getMinecraft().player;
                    float att = Math.max((float)player.getDistanceSq(soundPos0.getX() + 0.5, soundPos0.getY() + 0.5, soundPos0.getZ() + 0.5) / 8f, 1f);
                    float level = ITUtils.remapRange(0f, 1f, 0.5f, 1.0f, soundVolume);
                    ITSounds.alternator.PlayRepeating(soundPos0, 5f * soundVolume / att, level);
                }
            }
            return;
        }

        super.update();

        checkProvider();

        if (speed > 0) energyStorage.modifyEnergyStored(energyGenerated());

        int currentEnergy = energyStorage.getEnergyStored();
        if (currentEnergy > 0) {
            int transferRate = (int)Math.ceil(rfPerTickPerPort * torqueMult);
            for (int i = 0; i < 6; i++) {
                if (currentEnergy <= 0) break;
                BlockPos outPos = energyOutputPositions[i];
                if (outPos == null) continue;
                TileEntity te = Utils.getExistingTileEntity(world, outPos);
                if (te == null) continue;
                EnumFacing side = energyOutputs[i].facing.getOpposite();
                int canReceive = EnergyHelper.insertFlux(te, side, Math.min(currentEnergy, transferRate), true);
                if (canReceive > 0) {
                    int inserted = EnergyHelper.insertFlux(te, side, canReceive, false);
                    energyStorage.modifyEnergyStored(-inserted);
                    currentEnergy -= inserted;
                }
            }
        }

        boolean changed = oldSpeed != speed || oldEnergy != currentEnergy;
        if (changed && tickCountdown-- <= 0) {
            ByteBuf buf = Unpooled.buffer();
            buf.writeInt(energyStorage.getEnergyStored());
            buf.writeInt(speed);
            BinaryMessageTileSync.sendToAllTracking(world, getPos(), buf);
            tickCountdown = 5;
            world.markChunkDirty(getPos(), this);
            markContainingBlockForUpdate(null);
        }

        oldEnergy = currentEnergy;
        oldSpeed = speed;

        int comparator = getComparatorInputOverride();
        if (comparator != oldComparatorOutput) {
            oldComparatorOutput = comparator;
            if (redstone0 != null) {
                BlockPos rsPos = getBlockPosForPos(redstone0.position);
                world.updateComparatorOutputLevel(rsPos, getBlockType());
            }
        }
    }

    private void InitializePoIs() {
        for (PoIJSONSchema poi : TileEntityITMultiblockPartAlternator.instance.pointsOfInterest) {
            String name = poi.name;
            if (name.startsWith("energy_output")) {
                int index = Integer.parseInt(name.substring(13));
                energyOutputs[index] = new PoICache(facing, poi, mirrored);
                energyOutputPositions[index] = getBlockPosForPos(energyOutputs[index].position).offset(energyOutputs[index].facing);
            } else if ("mechanical_input0".equals(name)) {
                mechanicalInput0 = new PoICache(facing, poi, mirrored);
                mechanicalInputPos0 = getBlockPosForPos(mechanicalInput0.position).offset(mechanicalInput0.facing);
            } else if ("sound0".equals(name)) {
                if (world.isRemote) soundPos0 = getBlockPosForPos(poi.position);
            } else if ("redstone0".equals(name)) {
                redstone0 = new PoICache(facing, poi, mirrored);
            }
        }
    }

    private void notifyIONeighbors() {
        Block block = getBlockType();
        for (int i = 0; i < 6; i++) {
            if (energyOutputs[i] != null) {
                BlockPos p = getBlockPosForPos(energyOutputs[i].position);
                world.notifyNeighborsOfStateChange(p, block, true);
            }
        }
        if (redstone0 != null) {
            BlockPos p = getBlockPosForPos(redstone0.position);
            world.updateComparatorOutputLevel(p, block);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override public void onChunkUnload() {
        if (soundPos0 != null) ITSoundHandler.StopSound(soundPos0);
        super.onChunkUnload();
    }

    @Override public void receiveMessageFromServer(ByteBuf buf) {
        int readEnergy = buf.readInt();
        int readSpeed = buf.readInt();
        energyStorage.modifyEnergyStored(readEnergy - energyStorage.getEnergyStored());
        speed = readSpeed;
        targetSoundLevel = soundRPM ? (float)readSpeed / maxSpeed : (float)readEnergy / energyStorage.getMaxEnergyStored();
    }

    @Override public void receiveMessageFromClient(ByteBuf message, EntityPlayerMP player) {}

    @Override public boolean isDummy() { return false; }

    @Override public TileEntityAlternatorMaster master() { return this; }

    private int energyGenerated() {
        if ((double)speed / maxSpeed <= rfThreshold) return 0;
        return (int)Math.round(Math.pow((double)speed / maxSpeed, rfExponent) * rfPerTick * torqueMult);
    }

    private void checkProvider() {
        if (isValidProvider()) {
            speed = provider.getSpeed();
            torqueMult = provider.getTorqueMultiplier();
        } else if (speed > 0) {
            speed = Math.max(speed - speedLossPerTick, 0);
        }
    }

    private boolean isValidProvider() {
        if (mechanicalInput0 == null) InitializePoIs();
        if (provider == null || !provider.isValid()) {
            TileEntity te = world.getTileEntity(mechanicalInputPos0);
            if (te instanceof IMechanicalEnergy) {
                IMechanicalEnergy poss = (IMechanicalEnergy)te;
                if (poss.isValid() && poss.isMechanicalEnergyTransmitter(mechanicalInput0.facing.getOpposite())) {
                    provider = poss;
                    return true;
                }
            }
            provider = null;
            return false;
        }
        return true;
    }

    @Override public void disassemble() {
        super.disassemble();
        BlockPos sp = soundPos0;
        if (sp == null) {
            InitializePoIs();
            sp = soundPos0;
        }
        if (sp != null && !world.isRemote) {
            ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(sp), new TargetPoint(world.provider.getDimension(), sp.getX(), sp.getY(), sp.getZ(), 0));
        }
    }

    @Override public int getComparatorInputOverride() { return 15 * energyStorage.getEnergyStored() / energyStorage.getMaxEnergyStored(); }

    public boolean isMechanicalEnergyReceiver(@Nullable EnumFacing facing, int position) {
        if (mechanicalInput0 == null) InitializePoIs();
        return facing != null && mechanicalInput0.isPoI(facing, position);
    }

    public boolean isEnergyPosition(@Nullable EnumFacing facing, int position) {
        if (energyOutputs[0] == null) InitializePoIs();
        if (facing == null) return false;
        for (PoICache cache : energyOutputs) if (cache != null && cache.isPoI(facing, position)) return true;
        return false;
    }

    @Override @Nonnull public int[] getRedstonePos() {
        if (redstone0 == null) InitializePoIs();
        return new int[]{redstone0.position};
    }

    @Override @Nonnull public int[] getEnergyPos() {
        if (energyOutputs[0] == null) InitializePoIs();
        return new int[]{energyOutputs[0].position, energyOutputs[1].position, energyOutputs[2].position,
                energyOutputs[3].position, energyOutputs[4].position, energyOutputs[5].position};
    }

    @Override @Nonnull public FluxStorageAdvanced getFluxStorage() { return energyStorage; }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return new int[0]; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return new int[0]; }
}
