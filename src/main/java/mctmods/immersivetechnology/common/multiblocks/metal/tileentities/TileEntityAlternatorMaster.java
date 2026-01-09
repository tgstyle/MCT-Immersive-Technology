package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorageAdvanced;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.api.client.MechanicalEnergyAnimation;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.shared.interfaces.ITBlockInterfaces.IMechanicalEnergy;
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
import net.minecraft.entity.player.EntityPlayerMP;
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

    public FluxStorageAdvanced energyStorage = new FluxStorageAdvanced(Multiblocks.alternator.alternator_energy_capacitorSize, rfPerTick, rfPerTickPerPort);
    public int speed;
    public float torqueMult = 1;
    public IMechanicalEnergy provider;
    private int clientUpdateCooldown = 5;
    private float targetEnergyPercentage;
    private float soundVolume;
    private int oldEnergy = energyStorage.getEnergyStored();
    private int oldSpeed = maxSpeed;
    MechanicalEnergyAnimation animation = new MechanicalEnergyAnimation();
    private final PoICache[] energyOutputs = new PoICache[6];
    private final BlockPos[] energyOutputPositions = new BlockPos[6];
    protected PoICache mechanicalInput0, redstone0;
    private BlockPos mechanicalInputPos0, soundPos0;
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

    @Override @Nonnull public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound nbt) {
        nbt = super.writeToNBT(nbt);
        nbt.setFloat("animationRotation", animation.getAnimationRotation());
        return nbt;
    }

    @Override public void readFromNBT(@Nonnull NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        animation.setAnimationRotation(nbt.getFloat("animationRotation"));
    }

    public int energyGenerated() { return ((double)speed / (double)maxSpeed > rfThreshold) ? (int)Math.round(Math.pow((double)speed / (double)maxSpeed, rfExponent) * torqueMult * rfPerTick) : 0; }

    @SideOnly(Side.CLIENT)
    public void handleSounds() {
        if (soundPos0 == null) { InitializePoIs(); }
        if (soundVolume == 0) { ITSoundHandler.StopSound(soundPos0); }
        else {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float attenuation = Math.max((float)player.getDistanceSq(soundPos0.getX(), soundPos0.getY(), soundPos0.getZ()) / 8, 1);
            float level = ITUtils.remapRange(0, 1, 0.5f, 1.0f, soundVolume);
            ITSounds.alternator.PlayRepeating(soundPos0, (5 * soundVolume) / attenuation, level);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override public void onChunkUnload() {
        ITSoundHandler.StopSound(soundPos0);
        super.onChunkUnload();
    }

    @Override public void disassemble() {
        super.disassemble();
        if (soundPos0 == null) { InitializePoIs(); }
        ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(soundPos0), new NetworkRegistry.TargetPoint(world.provider.getDimension(), soundPos0.getX(), soundPos0.getY(), soundPos0.getZ(), 0));
    }

    public void notifyNearbyClients() { BinaryMessageTileSync.sendToAllTracking(world, getPos(), Unpooled.copyInt(energyStorage.getEnergyStored(), speed)); }

    public void efficientMarkDirty() { world.getChunk(getPos()).markDirty(); }

    public boolean isValidProvider() {
        if (mechanicalInput0 == null) { InitializePoIs(); }
        if (provider == null || !provider.isValid()) {
            TileEntity tile = world.getTileEntity(mechanicalInputPos0);
            if (tile instanceof IMechanicalEnergy) {
                IMechanicalEnergy possibleProvider = (IMechanicalEnergy)tile;
                if (possibleProvider.isValid() && possibleProvider.isMechanicalEnergyTransmitter(mechanicalInput0.facing.getOpposite())) { provider = possibleProvider; }
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
        if (needsBlockUpdate) { needsBlockUpdate = false; markContainingBlockForUpdate(null); }
        if (!formed) { return; }
        if (world.isRemote) {
            float rotationSpeed = speed == 0 ? 0f : ((float)speed / (float)maxSpeed) * maxRotationSpeed;
            float oldMomentum = animation.getAnimationMomentum();
            animation.setAnimationMomentum(rotationSpeed);
            animation.setAnimationRotation(animation.getAnimationRotation() + oldMomentum);
            if (soundVolume < targetEnergyPercentage) { soundVolume = Math.min(soundVolume + 0.01f, targetEnergyPercentage); }
            else if (soundVolume > targetEnergyPercentage) { soundVolume = Math.max(soundVolume - 0.01f, targetEnergyPercentage); }
            handleSounds();
            return;
        }
        super.update();
        if (needsPoIInit) { needsPoIInit = false; InitializePoIs(); }
        if (needsNotify) { needsNotify = false; notifyIONeighbors(); }
        checkProvider();
        if (speed > 0) {
            energyStorage.modifyEnergyStored(energyGenerated());
        }
        int currentEnergy = energyStorage.getEnergyStored();
        if (currentEnergy > 0) {
            int transferRate = (int)Math.ceil(rfPerTickPerPort * torqueMult);
            for (int i = 0; i < 6; i++) {
                if (currentEnergy <= 0) { break; }
                TileEntity tileEntity = Utils.getExistingTileEntity(world, energyOutputPositions[i]);
                if (tileEntity != null) {
                    EnumFacing energyFacing = energyOutputs[i].facing.getOpposite();
                    if (EnergyHelper.isFluxReceiver(tileEntity, energyFacing)) {
                        int canReceiveAmount = EnergyHelper.insertFlux(tileEntity, energyFacing, Math.min(currentEnergy, transferRate), true);
                        if (canReceiveAmount > 0) {
                            EnergyHelper.insertFlux(tileEntity, energyFacing, canReceiveAmount, false);
                            energyStorage.modifyEnergyStored(-canReceiveAmount);
                            currentEnergy -= canReceiveAmount;
                        }
                    }
                }
            }
        }
        boolean changed = oldSpeed != speed || oldEnergy != currentEnergy;
        clientUpdateCooldown--;
        if (changed && clientUpdateCooldown <= 0) {
            notifyNearbyClients();
            efficientMarkDirty();
            markContainingBlockForUpdate(null);
            clientUpdateCooldown = 5;
        }
        oldEnergy = currentEnergy;
        oldSpeed = speed;
    }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntityAlternatorMaster master() { master = this; return this; }

    @Override public void receiveMessageFromServer(ByteBuf buf) {
        int energy = buf.readInt();
        int speed = buf.readInt();
        targetEnergyPercentage = (!soundRPM) ? (float)energy / energyStorage.getMaxEnergyStored() : (float)speed / maxSpeed;
    }

    @Override public void receiveMessageFromClient(ByteBuf message, EntityPlayerMP player) { }

    public boolean isMechanicalEnergyReceiver(@Nullable EnumFacing facing, int position) {
        if (mechanicalInput0 == null) { InitializePoIs(); }
        return facing != null && mechanicalInput0.isPoI(facing, position);
    }

    public boolean isEnergyPosition(@Nullable EnumFacing facing, int position) {
        if (energyOutputs[0] == null) { InitializePoIs(); }
        if (facing == null) { return false; }
        for (int i = 0; i < 6; i++) {
            if (energyOutputs[i].isPoI(facing, position)) { return true; }
        }
        return false;
    }

    @Override public int getComparatorInputOverride() { return 15 * energyStorage.getEnergyStored() / energyStorage.getMaxEnergyStored(); }

    private void InitializePoIs() {
        for (PoIJSONSchema poi : TileEntityITMultiblockPartAlternator.instance.pointsOfInterest) {
            String name = poi.name;
            if (name.startsWith("energy_output")) {
                int index = Integer.parseInt(name.substring(13));
                energyOutputs[index] = new PoICache(facing, poi, mirrored);
                energyOutputPositions[index] = getBlockPosForPos(energyOutputs[index].position).offset(energyOutputs[index].facing);
            } else switch (name) {
                case "redstone0":
                    redstone0 = new PoICache(facing, poi, mirrored);
                    break;
                case "mechanical_input0":
                    mechanicalInput0 = new PoICache(facing, poi, mirrored);
                    mechanicalInputPos0 = getBlockPosForPos(mechanicalInput0.position).offset(mechanicalInput0.facing);
                    break;
                case "sound0":
                    soundPos0 = getBlockPosForPos(poi.position);
                    break;
            }
        }
    }

    private void notifyIONeighbors() {
        for (int i = 0; i < 6; i++) {
            notifyNeighbor(getBlockPosForPos(energyOutputs[i].position));
        }
        notifyNeighbor(getBlockPosForPos(redstone0.position));
    }

    private void notifyNeighbor(BlockPos pos) { world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), false); }

    @Override @Nonnull public int[] getRedstonePos() {
        if (redstone0 == null) { InitializePoIs(); }
        return new int[] {redstone0.position};
    }

    @Override @Nonnull public int[] getEnergyPos() {
        if (energyOutputs[0] == null) { InitializePoIs(); }
        return new int[] {energyOutputs[0].position, energyOutputs[1].position, energyOutputs[2].position, energyOutputs[3].position, energyOutputs[4].position, energyOutputs[5].position};
    }

    @Override @Nonnull public FluxStorage getFluxStorage() { return energyStorage; }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return new int[0]; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return new int[0]; }
}
