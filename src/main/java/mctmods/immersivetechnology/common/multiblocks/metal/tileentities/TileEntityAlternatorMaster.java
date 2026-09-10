package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import com.immersiveconvergence.api.capability.IMechanicalEnergyProvider;
import com.immersiveconvergence.api.client.ICSoundHandler;
import com.immersiveconvergence.api.client.MechanicalEnergyAnimation;
import com.immersiveconvergence.api.multiblock.ICBlockInterfaces.IComparatorOverride;
import com.immersiveconvergence.api.multiblock.PoICache;
import com.immersiveconvergence.api.network.BinaryTileSyncMessage;
import com.immersiveconvergence.api.network.IBinaryMessageReceiver;
import com.immersiveconvergence.api.util.ICFluxStorageAdvanced;
import com.immersiveconvergence.api.util.ICUtils;
import com.immersiveconvergence.core.ICCommonConfig;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.ITUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
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

    public ICFluxStorageAdvanced energyStorage = new ICFluxStorageAdvanced(Multiblocks.alternator.alternator_energy_capacitorSize, rfPerTick(), rfPerTickPerPort);
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

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        energyStorage.readFromNBT(nbt);
        animation.readFromNBT(nbt);
        speed = nbt.getInteger("speed");
        effectiveMaxSpeed = nbt.hasKey("effectiveMaxSpeed") ? nbt.getInteger("effectiveMaxSpeed") : maxSpeed();
        soundGracePeriod = nbt.getInteger("soundGracePeriod");
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
        BlockPos soundPos0 = poiWorldPos("sound0");
        float targetSoundLevel = isRunning ? (soundRPM() ? speedFraction() : (float)energyStorage.getEnergyStored() / energyStorage.getMaxEnergyStored()) : 0f;
        if (soundVolume < targetSoundLevel) { soundVolume = Math.min(soundVolume + 0.01f, targetSoundLevel); }
        else if (soundVolume > targetSoundLevel) { soundVolume = Math.max(soundVolume - 0.01f, targetSoundLevel); }
        if (soundVolume <= 0f) { ICSoundHandler.stopSound(soundPos0); }
        else {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float att = Math.max((float)player.getDistanceSq(soundPos0.getX() + 0.5, soundPos0.getY() + 0.5, soundPos0.getZ() + 0.5) / 32f, 1f);
            ITSounds.alternator.PlayRepeating(soundPos0, 11f * soundVolume / att, ITUtils.remapRange(0, effectiveMaxSpeed, 0.5f, 1.25f, speed));
        }
    }

    @Override public void update() {
        if (!formed) return;
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
            List<PoICache> outputPois = poisWithPrefix("energy_output");
            TileEntity[] outputs = new TileEntity[outputPois.size()];
            EnumFacing[] sides = new EnumFacing[outputPois.size()];
            int[] simulated = new int[outputPois.size()];
            List<Integer> ports = new ArrayList<>();
            for (int i = 0; i < outputPois.size(); i++) {
                PoICache outputPoi = outputPois.get(i);
                TileEntity te = ICUtils.getExistingTileEntity(world, poiFrontPos(outputPoi));
                if (te == null) continue;
                outputs[i] = te;
                sides[i] = outputPoi.facing.getOpposite();
                simulated[i] = ICUtils.insertFlux(te, sides[i], budget, true);
                ports.add(i);
            }
            ports.sort(Comparator.comparingInt(i -> simulated[i]));
            int remaining = budget;
            int remainingOutputs = ports.size();
            for (int i : ports) {
                if (remaining <= 0) break;
                int possibleOutput = (int)Math.ceil((double)remaining / remainingOutputs);
                int inserted = ICUtils.insertFlux(outputs[i], sides[i], possibleOutput, false);
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

        int comparator = comparatorValue();
        if (comparator != oldComparatorOutput) {
            oldComparatorOutput = comparator;
            notifyComparators();
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
        if (provider == null || !provider.isValid()) {
            TileEntity te = world.getTileEntity(poiFrontPos("mechanical_input0"));
            if (te instanceof IMechanicalEnergyProvider) {
                IMechanicalEnergyProvider poss = (IMechanicalEnergyProvider)te;
                if (poss.isValid() && poss.isMechanicalEnergyTransmitter(poi("mechanical_input0").facing.getOpposite())) {
                    provider = poss;
                    return true;
                }
            }
            provider = null;
            return false;
        }
        return true;
    }

    @Override public int getComparatorInputOverride() { return isComparatorPos() ? comparatorValue() : 0; }

    public int comparatorValue() {
        if (!formed) return 0;
        return 15 * energyStorage.getEnergyStored() / energyStorage.getMaxEnergyStored();
    }

    public boolean isMechanicalEnergyReceiver(@Nullable EnumFacing facing, BlockPos position) { return facing != null && isPoI("mechanical_input0", facing, position); }

    @Override @Nonnull public ICFluxStorageAdvanced getStorage() { return energyStorage; }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return ITUtils.EMPTY_INT_ARRAY; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return ITUtils.EMPTY_INT_ARRAY; }
}
