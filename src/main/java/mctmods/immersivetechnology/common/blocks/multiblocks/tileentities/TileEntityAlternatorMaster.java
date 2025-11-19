package mctmods.immersivetechnology.common.blocks.multiblocks.tileentities;

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
import mctmods.immersivetechnology.common.blocks.ITBlockInterfaces;
import mctmods.immersivetechnology.common.blocks.multiblocks.TileEntityITMultiblockPartAlternator;
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
    private final PoICache[] energyOutputs = new PoICache[6];
    private PoICache redstone, mechanicalInput;
    private BlockPos soundOrigin, mechanicalInputFront;
    private final BlockPos[] EnergyOutputPositions = new BlockPos[6];
    private boolean needsPoIInit = false;
    private boolean needsNotify = false;

    @Override
    public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        energyStorage.readFromNBT(nbt);
        animation.readFromNBT(nbt);
        if (!descPacket && !world.isRemote && formed) {
            needsPoIInit = true;
            needsNotify = true;
        }
    }

    @Override
    public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        energyStorage.writeToNBT(nbt);
        animation.writeToNBT(nbt);
    }

    @Override
    public @Nonnull NBTTagCompound writeToNBT(@Nonnull NBTTagCompound nbt) {
        nbt = super.writeToNBT(nbt);
        nbt.setFloat("animationRotation", getAnimation().getAnimationRotation());
        return nbt;
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        getAnimation().setAnimationRotation(nbt.getFloat("animationRotation"));
    }

    public int energyGenerated() {
        return ((double)speed / (double)maxSpeed > rfThreshold) ? (int)Math.round(Math.pow((double)speed / (double)maxSpeed, rfExponent) * torqueMult * rfPerTick) : 0;
    }

    @SideOnly(Side.CLIENT)
    public void handleSounds() {
        if (soundOrigin == null) InitializePoIs();
        if(soundVolume == 0) {
            ITSoundHandler.StopSound(soundOrigin);
        } else {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float attenuation = Math.max((float)player.getDistanceSq(soundOrigin.getX(), soundOrigin.getY(), soundOrigin.getZ()) / 8, 1);
            float level = ITUtils.remapRange(0, 1, 0.5f, 1.0f, soundVolume);
            ITSounds.alternator.PlayRepeating(soundOrigin, (5 * soundVolume) / attenuation, level);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void onChunkUnload() {
        if (soundOrigin == null) InitializePoIs();
        ITSoundHandler.StopSound(soundOrigin);
        super.onChunkUnload();
    }

    @Override
    public void disassemble() {
        super.disassemble();
        if (soundOrigin == null) InitializePoIs();
        ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(soundOrigin), new NetworkRegistry.TargetPoint(world.provider.getDimension(), soundOrigin.getX(), soundOrigin.getY(), soundOrigin.getZ(), 0));
    }

    public void notifyNearbyClients() {
        BinaryMessageTileSync.sendToAllTracking(world, getPos(), Unpooled.copyInt(energyStorage.getEnergyStored(), speed));
    }

    public void efficientMarkDirty() {
        world.getChunk(this.getPos()).markDirty();
    }

    public boolean isValidProvider() {
        if (mechanicalInput == null) InitializePoIs();
        if (provider == null || !provider.isValid()) {
            TileEntity tile = world.getTileEntity(mechanicalInputFront);
            if (tile instanceof ITBlockInterfaces.IMechanicalEnergy) {
                ITBlockInterfaces.IMechanicalEnergy possibleProvider = (ITBlockInterfaces.IMechanicalEnergy) tile;
                if (possibleProvider.isValid() && possibleProvider.isMechanicalEnergyTransmitter(facing.getOpposite())) provider = possibleProvider;
            }
        }
        return provider != null && provider.isValid();
    }

    public void checkProvider() {
        if (isValidProvider()) {
            speed = provider.getSpeed();
            torqueMult = provider.getTorqueMultiplier();
        } else if (speed > 0) {
            speed = Math.max(speed - speedLossPerTick, 0);
        }
    }

    @Override
    public void update() {
        if (!formed) return;
        if (world.isRemote) {
            float rotationSpeed = speed == 0 ? 0f : ((float) speed / (float) maxSpeed) * maxRotationSpeed;
            float oldMomentum = animation.getAnimationMomentum();
            animation.setAnimationMomentum(rotationSpeed);
            animation.setAnimationRotation(animation.getAnimationRotation() + oldMomentum);
            if (soundVolume < targetEnergyPercentage) {
                soundVolume = Math.min(soundVolume + 0.01f, targetEnergyPercentage);
            } else if (soundVolume > targetEnergyPercentage) {
                soundVolume = Math.max(soundVolume - 0.01f, targetEnergyPercentage);
            }
            handleSounds();
            return;
        }
        if (needsPoIInit) {
            needsPoIInit = false;
            InitializePoIs();
        }
        if (needsNotify) {
            needsNotify = false;
            notifyIONeighbors();
        }
        checkProvider();
        if (speed > 0) {
            if (oldSpeed != speed || oldTorqueMult != torqueMult) {
                cachedGenerated = energyGenerated();
            }
            this.energyStorage.modifyEnergyStored(cachedGenerated);
        }
        int currentEnergy = energyStorage.getEnergyStored();
        if (currentEnergy > 0) {
            TileEntity tileEntity;
            int transferRate = (int)Math.ceil(rfPerTickPerPort * torqueMult);
            for (int i = 0; i < 6; i++) {
                if (currentEnergy == 0) break;
                tileEntity = Utils.getExistingTileEntity(world, EnergyOutputPositions[i]);
                if (tileEntity == null) continue;
                EnumFacing energyFacing = energyOutputs[i].facing.getOpposite();
                if (!EnergyHelper.isFluxReceiver(tileEntity, energyFacing)) continue;
                int canReceiveAmount = EnergyHelper.insertFlux(tileEntity, energyFacing, Math.min(currentEnergy, transferRate), true);
                if (canReceiveAmount == 0) continue;
                EnergyHelper.insertFlux(tileEntity, energyFacing, canReceiveAmount, false);
                energyStorage.modifyEnergyStored(-canReceiveAmount);
                currentEnergy = energyStorage.getEnergyStored();
            }
        }
        boolean changed = oldSpeed != speed;
        clientUpdateCooldown--;
        if (changed && clientUpdateCooldown <= 0) {
            efficientMarkDirty();
            this.markContainingBlockForUpdate(null);
            clientUpdateCooldown = 5;
        }
        if (oldEnergy != currentEnergy || oldSpeed != speed) {
            notifyNearbyClients();
        }
        oldEnergy = currentEnergy;
        oldSpeed = speed;
        oldTorqueMult = torqueMult;
    }

    @Override
    public boolean isDummy() {
        return false;
    }

    @Override
    public TileEntityAlternatorMaster master() {
        master = this;
        return this;
    }

    @Override
    public void receiveMessageFromServer(ByteBuf buf) {
        int energy = buf.readInt();
        int speed = buf.readInt();
        targetEnergyPercentage = (!soundRPM) ? (float)energy / energyStorage.getMaxEnergyStored() : (float)speed / maxSpeed;
    }

    public boolean isMechanicalEnergyReceiver(@Nullable EnumFacing facing, int position) {
        if (mechanicalInput == null) InitializePoIs();
        return facing != null && mechanicalInput.isPoI(facing, position);
    }

    public boolean isEnergyPosition(@Nullable EnumFacing facing, int position) {
        if (energyOutputs[0] == null) InitializePoIs();
        if (facing == null) return false;
        for (PoICache p : energyOutputs) if (p.isPoI(facing, position)) return true;
        return false;
    }

    @Override
    public int getComparatorInputOverride() {
        return 15 * energyStorage.getEnergyStored() / energyStorage.getMaxEnergyStored();
    }

    private void InitializePoIs() {
        int energyIndex = 0;
        for (PoIJSONSchema poi : TileEntityITMultiblockPartAlternator.instance.pointsOfInterest) {
            switch (poi.name) {
                case "energy_output":
                    energyOutputs[energyIndex] = new PoICache(facing, poi, mirrored);
                    EnergyOutputPositions[energyIndex] = getBlockPosForPos(energyOutputs[energyIndex].position).offset(energyOutputs[energyIndex].facing);
                    energyIndex++;
                    break;
                case "redstone":
                    redstone = new PoICache(facing, poi, mirrored);
                    break;
                case "mechanical_input":
                    mechanicalInput = new PoICache(facing, poi, mirrored);
                    mechanicalInputFront = getBlockPosForPos(mechanicalInput.position).offset(mechanicalInput.facing);
                    break;
                case "sound":
                    soundOrigin = getBlockPosForPos(poi.position);
                    break;
            }
        }
    }

    private void notifyIONeighbors() {
        for (PoICache p : energyOutputs) notifyNeighbor(getBlockPosForPos(p.position));
        notifyNeighbor(getBlockPosForPos(redstone.position));
    }

    private void notifyNeighbor(BlockPos pos) {
        world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), false);
    }

    @Override
    public @Nonnull int[] getRedstonePos() {
        if (redstone == null) InitializePoIs();
        return new int[] {redstone.position};
    }
}
