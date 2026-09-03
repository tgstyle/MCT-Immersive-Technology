package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorageAdvanced;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.Utils;

import com.immersiveconvergence.ImmersiveConvergence;
import com.immersiveconvergence.api.capability.IMechanicalEnergyProvider;
import com.immersiveconvergence.api.client.ICSoundHandler;
import com.immersiveconvergence.api.client.MechanicalEnergyAnimation;
import com.immersiveconvergence.api.multiblock.PoICache;
import com.immersiveconvergence.api.multiblock.PoIJSONSchema;
import com.immersiveconvergence.api.network.BinaryTileSyncMessage;
import com.immersiveconvergence.api.network.IBinaryMessageReceiver;
import com.immersiveconvergence.api.network.MessageStopSound;
import com.immersiveconvergence.core.ICCommonConfig;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartAlternator;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.ITUtils;

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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TileEntityAlternatorMaster extends TileEntityAlternatorSlave implements IBinaryMessageReceiver, IComparatorOverride {

    public static int maxSpeed() { return ICCommonConfig.mechanical.maxRpm; }
    private static float maxRotationSpeed() { return Multiblocks.steamTurbine.steamTurbine_speed_maxRotation; }
    private static int rfPerTick() { return Multiblocks.alternator.alternator_energy_perTick; }
    private static double rfExponent() { return Multiblocks.alternator.alternator_exponent; }
    private static double rfThreshold() { return Multiblocks.alternator.alternator_threshold; }
    private static double rfPowerFactor() { return Math.max(0, Multiblocks.alternator.alternator_powerFactor); }
    private static final int rfPerTickPerPort = rfPerTick() / 6;
    private static boolean soundRPM() { return Multiblocks.alternator.alternator_sound_RPM; }

    public FluxStorageAdvanced energyStorage = new FluxStorageAdvanced(Multiblocks.alternator.alternator_energy_capacitorSize, rfPerTick(), rfPerTickPerPort);
    public int speed = 0;
    public int effectiveMaxSpeed = maxSpeed();
    public float torqueMult = 1f;
    public IMechanicalEnergyProvider provider;
    public MechanicalEnergyAnimation animation = new MechanicalEnergyAnimation();

    private float soundVolume = 0f;
    private int soundGracePeriod = 0;
    private boolean isRunning = false;
    private int oldEnergy = 0;
    private int oldSpeed = 0;
    private int oldMaxSpeed = maxSpeed();
    private int oldComparatorOutput = 0;
    private int tickCountdown = 5;

    private boolean needsPoIInit = true;
    private boolean needsNotify = true;

    private final PoICache[] energyOutputsPos0 = new PoICache[6];
    private PoICache mechanicalInputPos0;
    private PoICache redstonePos0;
    private BlockPos mechanicalInputTEPos0;
    private BlockPos soundPos0;
    private final BlockPos[] energyOutputTEPos0 = new BlockPos[6];

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        energyStorage.readFromNBT(nbt);
        animation.readFromNBT(nbt);
        speed = nbt.getInteger("speed");
        effectiveMaxSpeed = nbt.hasKey("effectiveMaxSpeed") ? nbt.getInteger("effectiveMaxSpeed") : maxSpeed();
        soundGracePeriod = nbt.getInteger("soundGracePeriod");
        if (!descPacket && formed) {
            needsPoIInit = true;
            needsNotify = true;
        }
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        energyStorage.writeToNBT(nbt);
        animation.writeToNBT(nbt);
        nbt.setInteger("speed", speed);
        nbt.setInteger("effectiveMaxSpeed", effectiveMaxSpeed);
        nbt.setInteger("soundGracePeriod", soundGracePeriod);
    }

    @SideOnly(Side.CLIENT)
    public void handleSounds() {
        if (soundPos0 == null) return;
        float targetSoundLevel = isRunning ? (soundRPM() ? speedFraction() : (float)energyStorage.getEnergyStored() / energyStorage.getMaxEnergyStored()) : 0f;
        if (soundVolume < targetSoundLevel) { soundVolume = Math.min(soundVolume + 0.01f, targetSoundLevel); }
        else if (soundVolume > targetSoundLevel) { soundVolume = Math.max(soundVolume - 0.01f, targetSoundLevel); }
        if (soundVolume <= 0f) { ICSoundHandler.stopSound(soundPos0); }
        else {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float att = Math.max((float)player.getDistanceSq(soundPos0.getX() + 0.5, soundPos0.getY() + 0.5, soundPos0.getZ() + 0.5) / 8f, 1f);
            float level = ITUtils.remapRange(0f, 1f, 0.5f, 1.0f, soundVolume);
            ITSounds.alternator.PlayRepeating(soundPos0, 5f * soundVolume / att, level);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override public void onChunkUnload() {
        if (soundPos0 != null) ICSoundHandler.stopSound(soundPos0);
        super.onChunkUnload();
    }

    @Override public void disassemble() {
        super.disassemble();
        if (soundPos0 == null) InitializePoIs();
        if (soundPos0 != null && !world.isRemote) {
            ImmersiveConvergence.packetHandler.sendToAllTracking(new MessageStopSound(soundPos0), new TargetPoint(world.provider.getDimension(), soundPos0.getX(), soundPos0.getY(), soundPos0.getZ(), 0));
        }
    }

    @Override public void update() {
        if (!formed) return;

        if (needsPoIInit || mechanicalInputPos0 == null || energyOutputsPos0[0] == null) {
            InitializePoIs();
            needsPoIInit = false;
        }

        if (needsNotify) {
            notifyIONeighbors();
            needsNotify = false;
        }

        if (world.isRemote) {
            float oldMomentum = animation.getAnimationMomentum();
            float rotationSpeed = speed == 0 ? 0f : speedFraction() * maxRotationSpeed();
            animation.setAnimationMomentum(rotationSpeed);
            animation.setAnimationRotation(animation.getAnimationRotation() + oldMomentum);
            handleSounds();
            return;
        }

        super.update();

        checkProvider();

        if (speed > 0) energyStorage.modifyEnergyStored(energyGenerated());

        int currentEnergy = energyStorage.getEnergyStored();
        if (currentEnergy > 0) {
            int budget = Math.min(currentEnergy, (int)Math.ceil(rfPerTick() * torqueMult));
            TileEntity[] outputs = new TileEntity[6];
            EnumFacing[] sides = new EnumFacing[6];
            int[] simulated = new int[6];
            List<Integer> ports = new ArrayList<>();
            for (int i = 0; i < 6; i++) {
                BlockPos outPos = energyOutputTEPos0[i];
                if (outPos == null) continue;
                TileEntity te = Utils.getExistingTileEntity(world, outPos);
                if (te == null) continue;
                outputs[i] = te;
                sides[i] = energyOutputsPos0[i].facing.getOpposite();
                simulated[i] = EnergyHelper.insertFlux(te, sides[i], budget, true);
                ports.add(i);
            }
            ports.sort(Comparator.comparingInt(i -> simulated[i]));
            int remaining = budget;
            int remainingOutputs = ports.size();
            for (int i : ports) {
                if (remaining <= 0) break;
                int possibleOutput = (int)Math.ceil((double)remaining / remainingOutputs);
                int inserted = EnergyHelper.insertFlux(outputs[i], sides[i], possibleOutput, false);
                energyStorage.modifyEnergyStored(-inserted);
                remaining -= inserted;
                remainingOutputs--;
            }
            currentEnergy = energyStorage.getEnergyStored();
        }

        boolean didWork = speed > 0;
        if (didWork) soundGracePeriod = 60;
        else if (soundGracePeriod > 0) soundGracePeriod--;

        boolean wasRunning = isRunning;
        isRunning = soundGracePeriod > 0;

        boolean changed = oldSpeed != speed || oldMaxSpeed != effectiveMaxSpeed || oldEnergy != currentEnergy || isRunning != wasRunning;
        if (changed && tickCountdown-- <= 0) {
            ByteBuf buf = Unpooled.buffer();
            buf.writeInt(energyStorage.getEnergyStored());
            buf.writeInt(speed);
            buf.writeInt(effectiveMaxSpeed);
            buf.writeBoolean(isRunning);
            BinaryTileSyncMessage.sendToAllTracking(world, getPos(), buf);
            tickCountdown = 5;
            world.markChunkDirty(getPos(), this);
            if (isRunning != wasRunning) { markContainingBlockForUpdate(null); }
            else { throttledBlockUpdate(); }
        }

        oldEnergy = currentEnergy;
        oldSpeed = speed;
        oldMaxSpeed = effectiveMaxSpeed;

        int comparator = getComparatorInputOverride();
        if (comparator != oldComparatorOutput) {
            oldComparatorOutput = comparator;
            if (redstonePos0 != null) {
                BlockPos rsPos = getBlockPosForPos(redstonePos0.position);
                world.updateComparatorOutputLevel(rsPos, getBlockType());
            }
        }
    }

    private void InitializePoIs() {
        for (PoIJSONSchema poi : TileEntityITMultiblockPartAlternator.instance.pointsOfInterest) {
            String name = poi.name;
            if (name.startsWith("energy_output")) {
                int index = Integer.parseInt(name.substring(13));
                energyOutputsPos0[index] = new PoICache(facing, poi, mirrored);
                energyOutputTEPos0[index] = getBlockPosForPos(energyOutputsPos0[index].position).offset(energyOutputsPos0[index].facing);
            } else if ("mechanical_input0".equals(name)) {
                mechanicalInputPos0 = new PoICache(facing, poi, mirrored);
                mechanicalInputTEPos0 = getBlockPosForPos(mechanicalInputPos0.position).offset(mechanicalInputPos0.facing);
            } else if ("sound0".equals(name)) {
                soundPos0 = getBlockPosForPos(poi.position);
            } else if ("redstone0".equals(name)) {
                redstonePos0 = new PoICache(facing, poi, mirrored);
            }
        }
    }

    private void notifyIONeighbors() {
        Block block = getBlockType();
        for (int i = 0; i < 6; i++) {
            if (energyOutputsPos0[i] != null) {
                BlockPos p = getBlockPosForPos(energyOutputsPos0[i].position);
                world.notifyNeighborsOfStateChange(p, block, true);
            }
        }
        if (redstonePos0 != null) {
            BlockPos p = getBlockPosForPos(redstonePos0.position);
            world.updateComparatorOutputLevel(p, block);
        }
    }

    @Override public void receiveMessageFromServer(ByteBuf buf) {
        int readEnergy = buf.readInt();
        int readSpeed = buf.readInt();
        int readMaxSpeed = buf.readInt();
        isRunning = buf.readBoolean();
        energyStorage.modifyEnergyStored(readEnergy - energyStorage.getEnergyStored());
        speed = readSpeed;
        effectiveMaxSpeed = readMaxSpeed;
    }

    @Override public void receiveMessageFromClient(ByteBuf message, EntityPlayerMP player) {}

    @Override public boolean isDummy() { return false; }

    @Override public TileEntityAlternatorMaster master() { return this; }

    private int energyGenerated() {
        if ((double)speed / maxSpeed() <= rfThreshold()) return 0;
        return (int)Math.round(Math.pow((double)speed / maxSpeed(), rfExponent()) * rfPerTick() * torqueMult * rfPowerFactor());
    }

    private void checkProvider() {
        if (isValidProvider()) {
            effectiveMaxSpeed = Math.min(maxSpeed(), provider.getMaxSpeed());
            speed = Math.min(provider.getSpeed(), effectiveMaxSpeed);
            torqueMult = provider.getTorqueMultiplier();
        } else if (speed > 0) {
            speed = Math.max(speed - 6, 0);
        }
    }

    private float speedFraction() { return effectiveMaxSpeed <= 0 ? 0f : (float)speed / effectiveMaxSpeed; }

    private boolean isValidProvider() {
        if (mechanicalInputPos0 == null) InitializePoIs();
        if (provider == null || !provider.isValid()) {
            TileEntity te = world.getTileEntity(mechanicalInputTEPos0);
            if (te instanceof IMechanicalEnergyProvider) {
                IMechanicalEnergyProvider poss = (IMechanicalEnergyProvider)te;
                if (poss.isValid() && poss.isMechanicalEnergyTransmitter(mechanicalInputPos0.facing.getOpposite())) {
                    provider = poss;
                    return true;
                }
            }
            provider = null;
            return false;
        }
        return true;
    }

    @Override public int getComparatorInputOverride() {
        if (!formed) return 0;
        return 15 * energyStorage.getEnergyStored() / energyStorage.getMaxEnergyStored();
    }

    public boolean isMechanicalEnergyReceiver(@Nullable EnumFacing facing, BlockPos position) {
        if (mechanicalInputPos0 == null) InitializePoIs();
        return facing != null && mechanicalInputPos0.isPoI(facing, position);
    }

    public boolean isEnergyPosition(@Nullable EnumFacing facing, BlockPos position) {
        if (energyOutputsPos0[0] == null) InitializePoIs();
        if (facing == null) return false;
        for (PoICache cache : energyOutputsPos0) if (cache != null && cache.isPoI(facing, position)) return true;
        return false;
    }

    @Override @Nonnull public int[] getRedstonePos() {
        if (redstonePos0 == null) InitializePoIs();
        return new int[]{toFlatIndex(redstonePos0.position)};
    }

    @Override @Nonnull public int[] getEnergyPos() {
        if (energyOutputsPos0[0] == null) InitializePoIs();
        return new int[]{toFlatIndex(energyOutputsPos0[0].position), toFlatIndex(energyOutputsPos0[1].position), toFlatIndex(energyOutputsPos0[2].position),
                toFlatIndex(energyOutputsPos0[3].position), toFlatIndex(energyOutputsPos0[4].position), toFlatIndex(energyOutputsPos0[5].position)};
    }

    @Override @Nonnull public FluxStorageAdvanced getFluxStorage() { return energyStorage; }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return new int[0]; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return new int[0]; }
}
