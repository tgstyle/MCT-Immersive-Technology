package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.util.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.api.crafting.CoolingTowerRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartCoolingTower;
import mctmods.immersivetechnology.common.util.ITFluidTank;
import mctmods.immersivetechnology.common.util.ITFluidTank.TankListener;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.api.particles.ParticleSmokeCustom;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.common.util.compat.ITCompatModule;
import mctmods.immersivetechnology.common.util.compat.advancedrocketry.AdvancedRocketryHelper;
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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TileEntityCoolingTowerMaster extends TileEntityCoolingTowerSlave implements TankListener, IBinaryMessageReceiver, IComparatorOverride {

    private static final int inputTankSize = Multiblocks.coolingTower.coolingTower_input_tankSize;
    private static final int outputTankSize = Multiblocks.coolingTower.coolingTower_output_tankSize;

    public FluidTank[] tanks = new FluidTank[] {
            new ITFluidTank(inputTankSize, this),
            new ITFluidTank(inputTankSize, this),
            new ITFluidTank(outputTankSize, this),
            new ITFluidTank(outputTankSize, this),
            new ITFluidTank(outputTankSize, this)
    };

    private CoolingTowerRecipe cachedCoolingRecipe;

    private float soundVolume = 0f;
    private int soundGracePeriod = 0;
    private boolean isRunning = false;

    private int tickCountdown = 20;
    private int oldComparatorOutput;

    private boolean needsPoIInit = false;
    private boolean needsNotify = false;

    protected PoICache fluidInputPos0, fluidInputPos1, fluidOutputPos0, fluidOutputPos1, fluidOutputPos2;
    private BlockPos fluidOutputTEPos0, outputFrontTEPos1, outputFrontTEPos3, particlePos0, soundPos0;

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
        tanks[2].readFromNBT(nbt.getCompoundTag("tank2"));
        tanks[3].readFromNBT(nbt.getCompoundTag("tank3"));
        tanks[4].readFromNBT(nbt.getCompoundTag("tank4"));
        oldComparatorOutput = nbt.getInteger("oldComparatorOutput");
        soundGracePeriod = nbt.getInteger("soundGracePeriod");
        if (!descPacket && formed) {
            needsPoIInit = true;
            needsNotify = true;
        }
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank2", tanks[2].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank3", tanks[3].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank4", tanks[4].writeToNBT(new NBTTagCompound()));
        nbt.setInteger("oldComparatorOutput", oldComparatorOutput);
        nbt.setInteger("soundGracePeriod", soundGracePeriod);
    }

    @SideOnly(Side.CLIENT)
    public void spawnParticles() {
        if (!isRunning) return;
        if (particlePos0 == null) InitializePoIs();
        Random rand = new Random();
        if (rand.nextInt(40) == 0) return;
        int lessParticleSetting = ClientUtils.mc().gameSettings.particleSetting;
        if (lessParticleSetting == 2 || (lessParticleSetting == 1 && rand.nextInt(3) == 0)) return;
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        double distanceLimit = 64;
        if (particlePos0.distanceSq(player.posX, player.posY, player.posZ) > distanceLimit * distanceLimit) return;
        ParticleSmokeCustom cloud = new ParticleSmokeCustom(world,
                particlePos0.getX() + 2 - rand.nextFloat() * 3,
                particlePos0.getY(),
                particlePos0.getZ() + 2 - rand.nextFloat() * 3, 0, 0.02f, 0, 7);
        cloud.setRBGColorF(1, 1, 1);
        ClientUtils.mc().effectRenderer.addEffect(cloud);
    }

    @SideOnly(Side.CLIENT)
    public void handleSounds() {
        if (soundPos0 == null) InitializePoIs();
        float targetSoundLevel = isRunning ? 1f : 0f;
        if (soundVolume < targetSoundLevel) { soundVolume = Math.min(soundVolume + 0.01f, targetSoundLevel); }
        else if (soundVolume > targetSoundLevel) { soundVolume = Math.max(soundVolume - 0.01f, targetSoundLevel); }
        if (soundVolume <= 0f) { ITSoundHandler.StopSound(soundPos0); }
        else {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float attenuation = Math.max((float)player.getDistanceSq(soundPos0.getX() + .5, soundPos0.getY() + .5, soundPos0.getZ() + .5) / 8, 1);
            ITSounds.coolingTower.PlayRepeating(soundPos0, (10 * soundVolume) / attenuation, 1);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override public void onChunkUnload() {
        if (soundPos0 != null) ITSoundHandler.StopSound(soundPos0);
        super.onChunkUnload();
    }

    @Override public void disassemble() {
        if (soundPos0 == null) InitializePoIs();
        ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(soundPos0), new NetworkRegistry.TargetPoint(world.provider.getDimension(), soundPos0.getX(), soundPos0.getY(), soundPos0.getZ(), 0));
        super.disassemble();
    }

    @Override public void receiveMessageFromServer(ByteBuf message) { isRunning = message.readBoolean(); }

    @Override public void receiveMessageFromClient(ByteBuf message, EntityPlayerMP player) {}

    public void notifyNearbyClients() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeBoolean(isRunning);
        BinaryMessageTileSync.sendToAllTracking(world, getPos(), buf);
    }

    public void efficientMarkDirty() { world.getChunk(getPos()).markDirty(); }

    @Override public void update() {
        if (!formed) return;
        if (needsPoIInit || fluidInputPos0 == null) {
            InitializePoIs();
            needsPoIInit = false;
        }
        if (needsNotify) {
            notifyIONeighbors();
            needsNotify = false;
        }
        if (world.isRemote) {
            handleSounds();
            spawnParticles();
            return;
        }
        if (ITCompatModule.isAdvancedRocketryLoaded && AdvancedRocketryHelper.isAtmosphereUnsuitableForCooling(world, getPos())) return;
        super.update();
        boolean update = pumpOutputOut();
        boolean prevIsRunning = isRunning;
        if (processQueue.size() < getProcessQueueMaxLength() && (tanks[0].getFluidAmount() > 0 || tanks[1].getFluidAmount() > 0)) {
            FluidStack in0 = tanks[0].getFluid();
            FluidStack in1 = tanks[1].getFluid();
            cachedCoolingRecipe = CoolingTowerRecipe.findRecipe(in0, in1);
            boolean swapped = false;
            if (cachedCoolingRecipe == null) {
                cachedCoolingRecipe = CoolingTowerRecipe.findRecipe(in1, in0);
                swapped = true;
            }
            if (cachedCoolingRecipe != null) {
                boolean canOutput = true;
                if (cachedCoolingRecipe.fluidOutput0 != null) canOutput &= tanks[2].fill(cachedCoolingRecipe.fluidOutput0, false) == cachedCoolingRecipe.fluidOutput0.amount;
                if (cachedCoolingRecipe.fluidOutput1 != null) canOutput &= tanks[3].fill(cachedCoolingRecipe.fluidOutput1, false) == cachedCoolingRecipe.fluidOutput1.amount;
                if (cachedCoolingRecipe.fluidOutput2 != null) canOutput &= tanks[4].fill(cachedCoolingRecipe.fluidOutput2, false) == cachedCoolingRecipe.fluidOutput2.amount;
                if (canOutput) {
                    @SuppressWarnings("unchecked")
                    MultiblockProcessInMachine<CoolingTowerRecipe> process = new MultiblockProcessInMachine<>(cachedCoolingRecipe).setInputTanks(swapped ? 1 : 0, swapped ? 0 : 1);
                    if (ITCompatModule.isAdvancedRocketryLoaded) process.maxTicks *= (int)(1 / AdvancedRocketryHelper.getHeatTransferCoefficient(world, getPos()));
                    if (addProcessToQueue(process, true)) {
                        addProcessToQueue(process, false);
                        update = true;
                    }
                }
            }
        }
        boolean didWork = tickedProcesses > 0;
        if (didWork) soundGracePeriod = 60;
        else if (soundGracePeriod > 0) soundGracePeriod--;
        isRunning = soundGracePeriod > 0;

        if (prevIsRunning != isRunning) update = true;
        if (update && tickCountdown-- <= 0) {
            notifyNearbyClients();
            tickCountdown = 20;
        }
        if (update) {
            efficientMarkDirty();
            markContainingBlockForUpdate(null);
        }
        int comp = getComparatorInputOverride();
        if (comp != oldComparatorOutput) {
            oldComparatorOutput = comp;
            world.updateComparatorOutputLevel(getPos(), getBlockType());
        }
    }

    private boolean pumpOutputOut() {
        boolean changed = false;
        PoICache[] outputs = {fluidOutputPos0, fluidOutputPos1, fluidOutputPos2};
        BlockPos[] fronts = {fluidOutputTEPos0, outputFrontTEPos1, outputFrontTEPos3};
        int[] indices = {2, 3, 4};
        for (int i = 0; i < 3; i++) {
            if (tanks[indices[i]].getFluidAmount() > 0) {
                IFluidHandler output = FluidUtil.getFluidHandler(world, fronts[i], outputs[i].facing.getOpposite());
                if (output != null) {
                    FluidStack out = tanks[indices[i]].getFluid();
                    if (out == null) continue;
                    int accepted = output.fill(out, false);
                    if (accepted > 0) {
                        int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
                        tanks[indices[i]].drain(drained, true);
                        if (drained > 0) changed = true;
                    }
                }
            }
        }
        return changed;
    }

    private void InitializePoIs() {
        for (PoIJSONSchema poi : TileEntityITMultiblockPartCoolingTower.instance.pointsOfInterest) {
            switch (poi.name) {
                case "fluid_input0":
                    fluidInputPos0 = new PoICache(facing, poi, mirrored);
                    break;
                case "fluid_input1":
                    fluidInputPos1 = new PoICache(facing, poi, mirrored);
                    break;
                case "fluid_output0":
                    fluidOutputPos0 = new PoICache(facing, poi, mirrored);
                    fluidOutputTEPos0 = getBlockPosForPos(fluidOutputPos0.position).offset(fluidOutputPos0.facing);
                    break;
                case "fluid_output1":
                    fluidOutputPos1 = new PoICache(facing, poi, mirrored);
                    outputFrontTEPos1 = getBlockPosForPos(fluidOutputPos1.position).offset(fluidOutputPos1.facing);
                    break;
                case "fluid_output2":
                    fluidOutputPos2 = new PoICache(facing, poi, mirrored);
                    outputFrontTEPos3 = getBlockPosForPos(fluidOutputPos2.position).offset(fluidOutputPos2.facing);
                    break;
                case "particle0":
                    particlePos0 = getBlockPosForPos(poi.position);
                    break;
                case "sound0":
                    soundPos0 = getBlockPosForPos(poi.position);
                    break;
            }
        }
    }

    private void notifyIONeighbors() {
        if (fluidInputPos0 != null) notifyNeighbor(getBlockPosForPos(fluidInputPos0.position));
        if (fluidInputPos1 != null) notifyNeighbor(getBlockPosForPos(fluidInputPos1.position));
        if (fluidOutputPos0 != null) notifyNeighbor(getBlockPosForPos(fluidOutputPos0.position));
        if (fluidOutputPos1 != null) notifyNeighbor(getBlockPosForPos(fluidOutputPos1.position));
        if (fluidOutputPos2 != null) notifyNeighbor(getBlockPosForPos(fluidOutputPos2.position));
    }

    private void notifyNeighbor(BlockPos pos) { world.notifyNeighborsOfStateChange(pos, getBlockType(), true); }

    @Override public void TankContentsChanged() {
        cachedCoolingRecipe = null;
        efficientMarkDirty();
        markContainingBlockForUpdate(null);
    }

    @Override public int getComparatorInputOverride() { return 15 * processQueue.size() / getProcessQueueMaxLength(); }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntityCoolingTowerMaster master() { return this; }

    @Override @Nonnull public IFluidTank[] getInternalTanks() { return tanks; }

    @Override @Nonnull public IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) {
        if (fluidInputPos0 == null) InitializePoIs();
        if (fluidInputPos0.isPoI(side, position)) return new IFluidTank[] {tanks[0]};
        if (fluidInputPos1.isPoI(side, position)) return new IFluidTank[] {tanks[1]};
        if (fluidOutputPos0.isPoI(side, position)) return new IFluidTank[] {tanks[2]};
        if (fluidOutputPos1.isPoI(side, position)) return new IFluidTank[] {tanks[3]};
        if (fluidOutputPos2.isPoI(side, position)) return new IFluidTank[] {tanks[4]};
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        if (iTank > 1 || iTank < 0) return false;
        if (fluidInputPos0 == null) InitializePoIs();
        if (iTank == 0 && !fluidInputPos0.isPoI(side, position)) return false;
        if (iTank == 1 && !fluidInputPos1.isPoI(side, position)) return false;
        if (tanks[iTank].getFluidAmount() >= tanks[iTank].getCapacity()) return false;
        FluidStack current = tanks[iTank].getFluid();
        if (current != null) return resource.isFluidEqual(current);
        return iTank == 0 ? CoolingTowerRecipe.findRecipeByFluid0(resource.getFluid()) != null : CoolingTowerRecipe.findRecipeByFluid1(resource.getFluid()) != null;
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        if (iTank < 2) return false;
        if (fluidInputPos0 == null) InitializePoIs();
        if (iTank == 2 && !fluidOutputPos0.isPoI(side, position)) return false;
        if (iTank == 3 && !fluidOutputPos1.isPoI(side, position)) return false;
        if (iTank == 4 && !fluidOutputPos2.isPoI(side, position)) return false;
        return tanks[iTank].getFluidAmount() > 0;
    }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return new int[0]; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return new int[0]; }

    public static class CoolingTowerFluidHandler implements IFluidHandler {
        private final IFluidTank[] accessibleTanks;
        private final TileEntityCoolingTowerMaster master;
        private final EnumFacing side;
        private final int position;

        public CoolingTowerFluidHandler(IFluidTank[] accessibleTanks, TileEntityCoolingTowerMaster master, EnumFacing side, int position) {
            this.accessibleTanks = accessibleTanks;
            this.master = master;
            this.side = side;
            this.position = position;
        }

        private int getTankIndex(IFluidTank tank) {
            for (int i = 0; i < master.tanks.length; i++) {
                if (master.tanks[i] == tank) return i;
            }
            return -1;
        }

        @Override public IFluidTankProperties[] getTankProperties() {
            List<IFluidTankProperties> list = new ArrayList<>();
            for (IFluidTank tank : accessibleTanks) {
                int index = getTankIndex(tank);
                boolean canFill = index < 2;
                boolean canDrain = index >= 2;
                list.add(new FluidTankProperties(tank.getFluid(), tank.getCapacity(), canFill, canDrain));
            }
            return list.toArray(new IFluidTankProperties[0]);
        }

        @Override public int fill(FluidStack resource, boolean doFill) {
            if (resource == null) return 0;
            FluidStack resourceCopy = resource.copy();
            int filled = 0;
            for (IFluidTank accessible : accessibleTanks) {
                int iTank = getTankIndex(accessible);
                if (iTank != -1 && master.canFillTankFrom(iTank, side, resourceCopy, position)) {
                    int f = accessible.fill(resourceCopy, doFill);
                    filled += f;
                    resourceCopy.amount -= f;
                    if (doFill && f > 0) master.TankContentsChanged();
                    if (resourceCopy.amount <= 0) return filled;
                }
            }
            return filled;
        }

        @Override public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (resource == null) return null;
            FluidStack resourceCopy = resource.copy();
            FluidStack drained = null;
            for (IFluidTank accessible : accessibleTanks) {
                int iTank = getTankIndex(accessible);
                if (iTank != -1 && master.canDrainTankFrom(iTank, side, position)) {
                    FluidStack tankFluid = accessible.getFluid();
                    if (tankFluid != null && tankFluid.isFluidEqual(resourceCopy)) {
                        int amount = Math.min(resourceCopy.amount, tankFluid.amount);
                        FluidStack d = accessible.drain(amount, doDrain);
                        if (d != null) {
                            if (drained == null) drained = d.copy();
                            else drained.amount += d.amount;
                            resourceCopy.amount -= d.amount;
                            if (doDrain && d.amount > 0) master.TankContentsChanged();
                            if (resourceCopy.amount <= 0) return drained;
                        }
                    }
                }
            }
            return drained;
        }

        @Override public FluidStack drain(int maxDrain, boolean doDrain) {
            int toDrain = maxDrain;
            FluidStack drained = null;
            for (IFluidTank accessible : accessibleTanks) {
                int iTank = getTankIndex(accessible);
                if (iTank != -1 && master.canDrainTankFrom(iTank, side, position)) {
                    FluidStack d = accessible.drain(toDrain, doDrain);
                    if (d != null) {
                        if (drained == null) drained = d.copy();
                        else if (drained.isFluidEqual(d)) drained.amount += d.amount;
                        toDrain -= d.amount;
                        if (doDrain && d.amount > 0) master.TankContentsChanged();
                        if (toDrain <= 0) return drained;
                    }
                }
            }
            return drained;
        }
    }
}
