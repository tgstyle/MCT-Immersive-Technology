package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.util.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.api.crafting.CoolingTowerRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartCoolingTower;
import mctmods.immersivetechnology.common.util.ITFluidTank;
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
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TileEntityCoolingTowerMaster extends TileEntityCoolingTowerSlave implements ITFluidTank.TankListener, IBinaryMessageReceiver {

    private static final int inputTankSize = Multiblocks.coolingTower.coolingTower_input_tankSize;
    private static final int outputTankSize = Multiblocks.coolingTower.coolingTower_output_tankSize;

    public FluidTank[] tanks = new FluidTank[] {
            new ITFluidTank(inputTankSize, this),
            new ITFluidTank(inputTankSize, this),
            new ITFluidTank(outputTankSize, this),
            new ITFluidTank(outputTankSize, this),
            new ITFluidTank(outputTankSize, this)
    };

    private CoolingTowerRecipe cachedRecipe;

    protected PoICache fluidInput0, fluidInput1, fluidOutput0, fluidOutput1, fluidOutput2;
    private BlockPos outputFrontPos0, outputFrontPos1, outputFrontPos2, particlePos0, soundPos0;

    private float soundVolume;
    private int gracePeriod = 60;
    private int clientUpdateCooldown = 20;
    private boolean isRunning;
    private boolean needsPoIInit = false;

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
        tanks[2].readFromNBT(nbt.getCompoundTag("tank2"));
        tanks[3].readFromNBT(nbt.getCompoundTag("tank3"));
        tanks[4].readFromNBT(nbt.getCompoundTag("tank4"));
        if (!descPacket && formed) needsPoIInit = true;
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank2", tanks[2].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank3", tanks[3].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank4", tanks[4].writeToNBT(new NBTTagCompound()));
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
        if (isRunning) { if (soundVolume < 1) soundVolume += 0.01f; }
        else { if (soundVolume > 0) soundVolume -= 0.01f; }
        if (soundVolume == 0) ITSoundHandler.StopSound(soundPos0);
        else {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float attenuation = Math.max((float)player.getDistanceSq(soundPos0.getX(), soundPos0.getY(), soundPos0.getZ()) / 8, 1);
            ITSounds.coolingTower.PlayRepeating(soundPos0, (10 * soundVolume) / attenuation, 1);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override public void onChunkUnload() {
        if (soundPos0 == null) InitializePoIs();
        ITSoundHandler.StopSound(soundPos0);
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
        BinaryMessageTileSync.sendToAllTracking(world, getPos(), Unpooled.copyBoolean(isRunning));
    }

    public void efficientMarkDirty() { world.getChunk(getPos()).markDirty(); }

    private boolean pumpOutputOut() {
        boolean changed = false;
        PoICache[] outputs = {fluidOutput0, fluidOutput1, fluidOutput2};
        BlockPos[] fronts = {outputFrontPos0, outputFrontPos1, outputFrontPos2};
        int[] indices = {2, 3, 4};
        for (int i = 0; i < 3; i++) {
            if (tanks[indices[i]].getFluidAmount() > 0) {
                IFluidHandler output = FluidUtil.getFluidHandler(world, fronts[i], outputs[i].facing.getOpposite());
                if (output != null) {
                    FluidStack out = tanks[indices[i]].getFluid();
                    int accepted = output.fill(out, false);
                    if (accepted > 0) {
                        assert out != null;
                        int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
                        tanks[indices[i]].drain(drained, true);
                        if (drained > 0) changed = true;
                    }
                }
            }
        }
        return changed;
    }

    @Override public void update() {
        super.update();
        if (!formed) return;
        if (needsPoIInit || fluidInput0 == null) {
            InitializePoIs();
            needsPoIInit = false;
        }
        if (world.isRemote) {
            handleSounds();
            spawnParticles();
            return;
        }
        if (ITCompatModule.isAdvancedRocketryLoaded && AdvancedRocketryHelper.isAtmosphereUnsuitableForCooling(world, getPos())) return;
        boolean update = pumpOutputOut();
        boolean prevIsRunning = isRunning;
        if (processQueue.size() < getProcessQueueMaxLength() && (tanks[0].getFluidAmount() > 0 || tanks[1].getFluidAmount() > 0)) {
            FluidStack in0 = tanks[0].getFluid();
            FluidStack in1 = tanks[1].getFluid();
            cachedRecipe = CoolingTowerRecipe.findRecipe(in0, in1);
            boolean swapped = false;
            if (cachedRecipe == null) {
                cachedRecipe = CoolingTowerRecipe.findRecipe(in1, in0);
                swapped = true;
            }
            if (cachedRecipe != null) {
                boolean canOutput = true;
                if (cachedRecipe.fluidOutput0 != null) canOutput &= tanks[2].fill(cachedRecipe.fluidOutput0, false) == cachedRecipe.fluidOutput0.amount;
                if (cachedRecipe.fluidOutput1 != null) canOutput &= tanks[3].fill(cachedRecipe.fluidOutput1, false) == cachedRecipe.fluidOutput1.amount;
                if (cachedRecipe.fluidOutput2 != null) canOutput &= tanks[4].fill(cachedRecipe.fluidOutput2, false) == cachedRecipe.fluidOutput2.amount;
                if (canOutput) {
                    @SuppressWarnings("unchecked")
                    MultiblockProcessInMachine<CoolingTowerRecipe> process = new MultiblockProcessInMachine<>(cachedRecipe).setInputTanks(swapped ? 1 : 0, swapped ? 0 : 1);
                    if (ITCompatModule.isAdvancedRocketryLoaded) process.maxTicks *= (int)(1 / AdvancedRocketryHelper.getHeatTransferCoefficient(world, getPos()));
                    if (addProcessToQueue(process, true)) {
                        addProcessToQueue(process, false);
                        update = true;
                    }
                }
            }
        }
        if (tickedProcesses > 0) {
            gracePeriod = 60;
            isRunning = true;
        } else {
            if (gracePeriod > 0) gracePeriod--;
            else isRunning = false;
        }
        if (prevIsRunning != isRunning) update = true;
        clientUpdateCooldown--;
        if (update && clientUpdateCooldown <= 0) {
            notifyNearbyClients();
            clientUpdateCooldown = 20;
        }
        if (update) {
            efficientMarkDirty();
            markContainingBlockForUpdate(null);
        }
    }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntityCoolingTowerMaster master() { return this; }

    @Override public void TankContentsChanged() {
        cachedRecipe = null;
        markContainingBlockForUpdate(null);
    }

    @Override @Nonnull public IFluidTank[] getAccessibleFluidTanks(@Nullable EnumFacing side, int position) {
        if (!formed) return ITUtils.emptyIFluidTankList;
        if (fluidInput0 == null) InitializePoIs();
        if (side == null) return tanks;
        if (fluidInput0.isPoI(side, position)) return new IFluidTank[] {tanks[0]};
        if (fluidInput1.isPoI(side, position)) return new IFluidTank[] {tanks[1]};
        if (fluidOutput0.isPoI(side, position)) return new IFluidTank[] {tanks[2]};
        if (fluidOutput1.isPoI(side, position)) return new IFluidTank[] {tanks[3]};
        if (fluidOutput2.isPoI(side, position)) return new IFluidTank[] {tanks[4]};
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        if (!formed || fluidInput0 == null) InitializePoIs();
        if (iTank == 0 && fluidInput0.isPoI(side, position)) {
            FluidTank tank = tanks[0];
            if (tank.getFluidAmount() >= tank.getCapacity()) return false;
            if (tank.getFluid() == null) return CoolingTowerRecipe.findRecipeByFluid0(resource.getFluid()) != null;
            return resource.isFluidEqual(tank.getFluid());
        }
        if (iTank == 1 && fluidInput1.isPoI(side, position)) {
            FluidTank tank = tanks[1];
            if (tank.getFluidAmount() >= tank.getCapacity()) return false;
            if (tank.getFluid() == null) return CoolingTowerRecipe.findRecipeByFluid1(resource.getFluid()) != null;
            return resource.isFluidEqual(tank.getFluid());
        }
        return false;
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        if (!formed || fluidInput0 == null) InitializePoIs();
        if (iTank == 2 && fluidOutput0.isPoI(side, position)) return tanks[2].getFluidAmount() > 0;
        if (iTank == 3 && fluidOutput1.isPoI(side, position)) return tanks[3].getFluidAmount() > 0;
        if (iTank == 4 && fluidOutput2.isPoI(side, position)) return tanks[4].getFluidAmount() > 0;
        return false;
    }

    @Override public void onProcessFinish(@Nonnull MultiblockProcess<CoolingTowerRecipe> process) {
        if (process.recipe.fluidOutput0 != null) tanks[2].fill(process.recipe.fluidOutput0.copy(), true);
        if (process.recipe.fluidOutput1 != null) tanks[3].fill(process.recipe.fluidOutput1.copy(), true);
        if (process.recipe.fluidOutput2 != null) tanks[4].fill(process.recipe.fluidOutput2.copy(), true);
    }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return new int[0]; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return new int[0]; }

    private void InitializePoIs() {
        for (PoIJSONSchema poi : TileEntityITMultiblockPartCoolingTower.instance.pointsOfInterest) {
            switch (poi.name) {
                case "fluid_input0":
                    fluidInput0 = new PoICache(facing, poi, mirrored);
                    break;
                case "fluid_input1":
                    fluidInput1 = new PoICache(facing, poi, mirrored);
                    break;
                case "fluid_output0":
                    fluidOutput0 = new PoICache(facing, poi, mirrored);
                    outputFrontPos0 = getBlockPosForPos(fluidOutput0.position).offset(fluidOutput0.facing);
                    break;
                case "fluid_output1":
                    fluidOutput1 = new PoICache(facing, poi, mirrored);
                    outputFrontPos1 = getBlockPosForPos(fluidOutput1.position).offset(fluidOutput1.facing);
                    break;
                case "fluid_output2":
                    fluidOutput2 = new PoICache(facing, poi, mirrored);
                    outputFrontPos2 = getBlockPosForPos(fluidOutput2.position).offset(fluidOutput2.facing);
                    break;
                case "particle0":
                    particlePos0 = getBlockPosForPos(poi.position);
                    break;
                case "sound0":
                    soundPos0 = getBlockPosForPos(poi.position);
                    break;
            }
        }
        if (!world.isRemote) notifyIONeighbors();
    }

    private void notifyIONeighbors() {
        notifyNeighbor(getBlockPosForPos(fluidInput0.position));
        notifyNeighbor(getBlockPosForPos(fluidInput1.position));
        notifyNeighbor(getBlockPosForPos(fluidOutput0.position));
        notifyNeighbor(getBlockPosForPos(fluidOutput1.position));
        notifyNeighbor(getBlockPosForPos(fluidOutput2.position));
    }

    private void notifyNeighbor(BlockPos pos) { world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), false); }

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
                boolean canFill = index == 0 || index == 1;
                boolean canDrain = index >= 2 && index <= 4;
                list.add(new FluidTankProperties(tank.getFluid(), tank.getCapacity(), canFill, canDrain));
            }
            return list.toArray(new IFluidTankProperties[0]);
        }

        @Override public int fill(FluidStack resource, boolean doFill) {
            if (resource == null) return 0;
            resource = resource.copy();
            int filled = 0;
            for (IFluidTank accessible : accessibleTanks) {
                int iTank = getTankIndex(accessible);
                if (iTank != -1 && master.canFillTankFrom(iTank, side, resource, position)) {
                    int f = accessible.fill(resource, doFill);
                    filled += f;
                    resource.amount -= f;
                    if (doFill && f > 0) master.TankContentsChanged();
                    if (resource.amount <= 0) return filled;
                }
            }
            return filled;
        }

        @Override public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (resource == null) return null;
            resource = resource.copy();
            FluidStack drained = null;
            for (IFluidTank accessible : accessibleTanks) {
                int iTank = getTankIndex(accessible);
                if (iTank != -1 && master.canDrainTankFrom(iTank, side, position)) {
                    FluidStack tankFluid = accessible.getFluid();
                    if (tankFluid != null && tankFluid.isFluidEqual(resource)) {
                        int amount = Math.min(resource.amount, tankFluid.amount);
                        FluidStack d = accessible.drain(amount, doDrain);
                        if (d != null) {
                            if (drained == null) drained = d.copy();
                            else drained.amount += d.amount;
                            resource.amount -= d.amount;
                            if (doDrain && d.amount > 0) master.TankContentsChanged();
                            if (resource.amount <= 0) return drained;
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
