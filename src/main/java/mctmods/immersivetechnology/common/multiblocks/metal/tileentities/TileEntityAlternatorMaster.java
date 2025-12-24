package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.api.client.MechanicalEnergyAnimation;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.shared.interfaces.ITBlockInterfaces;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartAlternator;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.multiblock.PoICache;
import mctmods.immersivetechnology.common.util.multiblock.PoIJSONSchema;
import mctmods.immersivetechnology.common.util.network.BinaryMessageTileSync;
import mctmods.immersivetechnology.common.util.network.IBinaryMessageReceiver;
import mctmods.immersivetechnology.common.util.network.MessageStopSound;
import mctmods.immersivetechnology.common.util.sound.ITSoundHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.NetworkRegistry;
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

    public FluxStorage energyStorage = new FluxStorage(Multiblocks.alternator.alternator_energy_capacitorSize, rfPerTick, rfPerTickPerPort);
    public int speed;
    public float torqueMult = 1;
    public ITBlockInterfaces.IMechanicalEnergy provider;
    private int clientUpdateCooldown = 20;
    private float targetEnergyPercentage;
    private float soundVolume;
    private int oldEnergy = energyStorage.getEnergyStored();
    private int oldSpeed = maxSpeed;
    private float oldTorqueMult = 1;
    private int cachedGenerated = 0;
    MechanicalEnergyAnimation animation = new MechanicalEnergyAnimation();
    private PoICache energyOutput0, energyOutput1, energyOutput2, energyOutput3, energyOutput4, energyOutput5;
    private PoICache redstone, mechanicalInput0;
    private BlockPos soundOrigin, mechanicalInputPos0;
    private BlockPos energyOutputPos0, energyOutputPos1, energyOutputPos2, energyOutputPos3, energyOutputPos4, energyOutputPos5;
    private boolean needsPoIInit = false;
    private boolean needsNotify = false;
    private boolean needsBlockUpdate = false;

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        energyStorage.readFromNBT(nbt);
        animation.readFromNBT(nbt);
        if (!descPacket && !world.isRemote && formed) { needsPoIInit = true; needsNotify = true; needsBlockUpdate = true; }
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        energyStorage.writeToNBT(nbt);
        animation.writeToNBT(nbt);
    }

    @Override public @Nonnull NBTTagCompound writeToNBT(@Nonnull NBTTagCompound nbt) {
        nbt = super.writeToNBT(nbt);
        nbt.setFloat("animationRotation", getAnimation().getAnimationRotation());
        return nbt;
    }

    @Override public void readFromNBT(@Nonnull NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        getAnimation().setAnimationRotation(nbt.getFloat("animationRotation"));
    }

    public int energyGenerated() { return ((double)speed / (double)maxSpeed > rfThreshold) ? (int)Math.round(Math.pow((double)speed / (double)maxSpeed, rfExponent) * torqueMult * rfPerTick) : 0; }

    @SideOnly(Side.CLIENT)
    public void handleSounds() {
        if (soundOrigin == null) InitializePoIs();
        if (soundVolume == 0) { ITSoundHandler.StopSound(soundOrigin); }
        else {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float attenuation = Math.max((float)player.getDistanceSq(soundOrigin.getX(), soundOrigin.getY(), soundOrigin.getZ()) / 8, 1);
            float level = ITUtils.remapRange(0, 1, 0.5f, 1.0f, soundVolume);
            ITSounds.alternator.PlayRepeating(soundOrigin, (5 * soundVolume) / attenuation, level);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override public void onChunkUnload() {
        if (soundOrigin == null) InitializePoIs();
        ITSoundHandler.StopSound(soundOrigin);
        super.onChunkUnload();
    }

    @Override public void disassemble() {
        super.disassemble();
        if (soundOrigin == null) InitializePoIs();
        ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(soundOrigin), new NetworkRegistry.TargetPoint(world.provider.getDimension(), soundOrigin.getX(), soundOrigin.getY(), soundOrigin.getZ(), 0));
    }

    public void notifyNearbyClients() { BinaryMessageTileSync.sendToAllTracking(world, getPos(), Unpooled.copyInt(energyStorage.getEnergyStored(), speed)); }

    public void efficientMarkDirty() { world.getChunk(this.getPos()).markDirty(); }

    public boolean isValidProvider() {
        if (mechanicalInput0 == null) InitializePoIs();
        if (provider == null || !provider.isValid()) {
            TileEntity tile = world.getTileEntity(mechanicalInputPos0);
            if (tile instanceof ITBlockInterfaces.IMechanicalEnergy) {
                ITBlockInterfaces.IMechanicalEnergy possibleProvider = (ITBlockInterfaces.IMechanicalEnergy) tile;
                if (possibleProvider.isValid() && possibleProvider.isMechanicalEnergyTransmitter(mechanicalInput0.facing.getOpposite())) provider = possibleProvider;
            }
        }
        return provider != null && provider.isValid();
    }

    public void checkProvider() {
        if (isValidProvider()) {
            speed = provider.getSpeed();
            torqueMult = provider.getTorqueMultiplier();
        } else if (speed > 0) { speed = Math.max(speed - speedLossPerTick, 0); }
    }

    @Override public void update() {
        if (needsBlockUpdate) { needsBlockUpdate = false; this.markContainingBlockForUpdate(null); }
        if (!formed) return;
        if (world.isRemote) {
            float rotationSpeed = speed == 0 ? 0f : ((float) speed / (float) maxSpeed) * maxRotationSpeed;
            float oldMomentum = animation.getAnimationMomentum();
            animation.setAnimationMomentum(rotationSpeed);
            animation.setAnimationRotation(animation.getAnimationRotation() + oldMomentum);
            if (soundVolume < targetEnergyPercentage) { soundVolume = Math.min(soundVolume + 0.01f, targetEnergyPercentage); }
            else if (soundVolume > targetEnergyPercentage) { soundVolume = Math.max(soundVolume - 0.01f, targetEnergyPercentage); }
            handleSounds();
            return;
        }
        if (needsPoIInit) { needsPoIInit = false; InitializePoIs(); }
        if (needsNotify) { needsNotify = false; notifyIONeighbors(); }
        checkProvider();
        if (speed > 0) {
            if (oldSpeed != speed || oldTorqueMult != torqueMult) { cachedGenerated = energyGenerated(); }
            this.energyStorage.modifyEnergyStored(cachedGenerated);
        }
        int currentEnergy = energyStorage.getEnergyStored();
        if (currentEnergy > 0) {
            TileEntity tileEntity;
            int transferRate = (int)Math.ceil(rfPerTickPerPort * torqueMult);
            EnumFacing energyFacing;
            int canReceiveAmount;
            tileEntity = Utils.getExistingTileEntity(world, energyOutputPos0);
            if (tileEntity != null) {
                energyFacing = energyOutput0.facing.getOpposite();
                if (EnergyHelper.isFluxReceiver(tileEntity, energyFacing)) {
                    canReceiveAmount = EnergyHelper.insertFlux(tileEntity, energyFacing, Math.min(currentEnergy, transferRate), true);
                    if (canReceiveAmount > 0) {
                        EnergyHelper.insertFlux(tileEntity, energyFacing, canReceiveAmount, false);
                        energyStorage.modifyEnergyStored(-canReceiveAmount);
                        currentEnergy = energyStorage.getEnergyStored();
                    }
                }
            }
            if (currentEnergy > 0) {
                tileEntity = Utils.getExistingTileEntity(world, energyOutputPos1);
                if (tileEntity != null) {
                    energyFacing = energyOutput1.facing.getOpposite();
                    if (EnergyHelper.isFluxReceiver(tileEntity, energyFacing)) {
                        canReceiveAmount = EnergyHelper.insertFlux(tileEntity, energyFacing, Math.min(currentEnergy, transferRate), true);
                        if (canReceiveAmount > 0) {
                            EnergyHelper.insertFlux(tileEntity, energyFacing, canReceiveAmount, false);
                            energyStorage.modifyEnergyStored(-canReceiveAmount);
                            currentEnergy = energyStorage.getEnergyStored();
                        }
                    }
                }
            }
            if (currentEnergy > 0) {
                tileEntity = Utils.getExistingTileEntity(world, energyOutputPos2);
                if (tileEntity != null) {
                    energyFacing = energyOutput2.facing.getOpposite();
                    if (EnergyHelper.isFluxReceiver(tileEntity, energyFacing)) {
                        canReceiveAmount = EnergyHelper.insertFlux(tileEntity, energyFacing, Math.min(currentEnergy, transferRate), true);
                        if (canReceiveAmount > 0) {
                            EnergyHelper.insertFlux(tileEntity, energyFacing, canReceiveAmount, false);
                            energyStorage.modifyEnergyStored(-canReceiveAmount);
                            currentEnergy = energyStorage.getEnergyStored();
                        }
                    }
                }
            }
            if (currentEnergy > 0) {
                tileEntity = Utils.getExistingTileEntity(world, energyOutputPos3);
                if (tileEntity != null) {
                    energyFacing = energyOutput3.facing.getOpposite();
                    if (EnergyHelper.isFluxReceiver(tileEntity, energyFacing)) {
                        canReceiveAmount = EnergyHelper.insertFlux(tileEntity, energyFacing, Math.min(currentEnergy, transferRate), true);
                        if (canReceiveAmount > 0) {
                            EnergyHelper.insertFlux(tileEntity, energyFacing, canReceiveAmount, false);
                            energyStorage.modifyEnergyStored(-canReceiveAmount);
                            currentEnergy = energyStorage.getEnergyStored();
                        }
                    }
                }
            }
            if (currentEnergy > 0) {
                tileEntity = Utils.getExistingTileEntity(world, energyOutputPos4);
                if (tileEntity != null) {
                    energyFacing = energyOutput4.facing.getOpposite();
                    if (EnergyHelper.isFluxReceiver(tileEntity, energyFacing)) {
                        canReceiveAmount = EnergyHelper.insertFlux(tileEntity, energyFacing, Math.min(currentEnergy, transferRate), true);
                        if (canReceiveAmount > 0) {
                            EnergyHelper.insertFlux(tileEntity, energyFacing, canReceiveAmount, false);
                            energyStorage.modifyEnergyStored(-canReceiveAmount);
                            currentEnergy = energyStorage.getEnergyStored();
                        }
                    }
                }
            }
            if (currentEnergy > 0) {
                tileEntity = Utils.getExistingTileEntity(world, energyOutputPos5);
                if (tileEntity != null) {
                    energyFacing = energyOutput5.facing.getOpposite();
                    if (EnergyHelper.isFluxReceiver(tileEntity, energyFacing)) {
                        canReceiveAmount = EnergyHelper.insertFlux(tileEntity, energyFacing, Math.min(currentEnergy, transferRate), true);
                        if (canReceiveAmount > 0) {
                            EnergyHelper.insertFlux(tileEntity, energyFacing, canReceiveAmount, false);
                            energyStorage.modifyEnergyStored(-canReceiveAmount);
                            currentEnergy = energyStorage.getEnergyStored();
                        }
                    }
                }
            }
        }
        boolean changed = oldSpeed != speed;
        clientUpdateCooldown--;
        if (changed && clientUpdateCooldown <= 0) {
            efficientMarkDirty();
            this.markContainingBlockForUpdate(null);
            clientUpdateCooldown = 5;
        }
        if (oldEnergy != currentEnergy || oldSpeed != speed) { notifyNearbyClients(); }
        oldEnergy = currentEnergy;
        oldSpeed = speed;
        oldTorqueMult = torqueMult;
    }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntityAlternatorMaster master() { master = this; return this; }

    @Override public void receiveMessageFromServer(ByteBuf buf) {
        int energy = buf.readInt();
        int speed = buf.readInt();
        targetEnergyPercentage = (!soundRPM) ? (float)energy / energyStorage.getMaxEnergyStored() : (float)speed / maxSpeed;
    }

    public boolean isMechanicalEnergyReceiver(@Nullable EnumFacing facing, int position) {
        if (mechanicalInput0 == null) InitializePoIs();
        return facing != null && mechanicalInput0.isPoI(facing, position);
    }

    public boolean isEnergyPosition(@Nullable EnumFacing facing, int position) {
        if (energyOutput0 == null) InitializePoIs();
        if (facing == null) return false;
        if (energyOutput0.isPoI(facing, position)) return true;
        if (energyOutput1.isPoI(facing, position)) return true;
        if (energyOutput2.isPoI(facing, position)) return true;
        if (energyOutput3.isPoI(facing, position)) return true;
        if (energyOutput4.isPoI(facing, position)) return true;
        return energyOutput5.isPoI(facing, position);
    }

    @Override public int getComparatorInputOverride() { return 15 * energyStorage.getEnergyStored() / energyStorage.getMaxEnergyStored(); }

    private void InitializePoIs() {
        for (PoIJSONSchema poi : TileEntityITMultiblockPartAlternator.instance.pointsOfInterest) {
            switch (poi.name) {
                case "energy_output0":
                    energyOutput0 = new PoICache(this.facing, poi, this.mirrored);
                    energyOutputPos0 = getBlockPosForPos(energyOutput0.position).offset(energyOutput0.facing);
                    break;
                case "energy_output1":
                    energyOutput1 = new PoICache(this.facing, poi, this.mirrored);
                    energyOutputPos1 = getBlockPosForPos(energyOutput1.position).offset(energyOutput1.facing);
                    break;
                case "energy_output2":
                    energyOutput2 = new PoICache(this.facing, poi, this.mirrored);
                    energyOutputPos2 = getBlockPosForPos(energyOutput2.position).offset(energyOutput2.facing);
                    break;
                case "energy_output3":
                    energyOutput3 = new PoICache(this.facing, poi, this.mirrored);
                    energyOutputPos3 = getBlockPosForPos(energyOutput3.position).offset(energyOutput3.facing);
                    break;
                case "energy_output4":
                    energyOutput4 = new PoICache(this.facing, poi, this.mirrored);
                    energyOutputPos4 = getBlockPosForPos(energyOutput4.position).offset(energyOutput4.facing);
                    break;
                case "energy_output5":
                    energyOutput5 = new PoICache(this.facing, poi, this.mirrored);
                    energyOutputPos5 = getBlockPosForPos(energyOutput5.position).offset(energyOutput5.facing);
                    break;
                case "redstone0": redstone = new PoICache(this.facing, poi, this.mirrored); break;
                case "mechanical_input0":
                    mechanicalInput0 = new PoICache(this.facing, poi, this.mirrored);
                    mechanicalInputPos0 = getBlockPosForPos(mechanicalInput0.position).offset(mechanicalInput0.facing);
                    break;
                case "sound0": soundOrigin = getBlockPosForPos(poi.position); break;
            }
        }
    }

    private void notifyIONeighbors() {
        notifyNeighbor(getBlockPosForPos(energyOutput0.position));
        notifyNeighbor(getBlockPosForPos(energyOutput1.position));
        notifyNeighbor(getBlockPosForPos(energyOutput2.position));
        notifyNeighbor(getBlockPosForPos(energyOutput3.position));
        notifyNeighbor(getBlockPosForPos(energyOutput4.position));
        notifyNeighbor(getBlockPosForPos(energyOutput5.position));
        notifyNeighbor(getBlockPosForPos(redstone.position));
    }

    private void notifyNeighbor(BlockPos pos) { world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), false); }

    @Override public @Nonnull int[] getRedstonePos() {
        if (redstone == null) InitializePoIs();
        return new int[] {redstone.position};
    }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return new int[0]; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return new int[0]; }
}
