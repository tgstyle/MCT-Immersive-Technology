package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorageAdvanced;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.util.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.api.crafting.HeatExchangerRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartHeatExchanger;
import mctmods.immersivetechnology.common.util.ITFluidTank;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.ITUtils;
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
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TileEntityHeatExchangerMaster extends TileEntityHeatExchangerSlave implements ITFluidTank.TankListener, IBinaryMessageReceiver, IEBlockInterfaces.IMirrorAble, IEBlockInterfaces.IUsesBooleanProperty, IComparatorOverride {

    private static int inputTankSize() { return Multiblocks.heatExchanger.heatExchanger_input_tankSize; }
    private static int outputTankSize() { return Multiblocks.heatExchanger.heatExchanger_output_tankSize; }
    private static int energyCapacity() { return Multiblocks.heatExchanger.heatExchanger_energy_size; }
    private static int energyMaxInput() { return Multiblocks.heatExchanger.heatExchanger_energy_maxInput; }

    public FluxStorageAdvanced energyStorage = new FluxStorageAdvanced(energyCapacity(), energyMaxInput(), energyMaxInput());
    public FluidTank[] tanks = new FluidTank[]{
            new ITFluidTank(inputTankSize(), this),
            new ITFluidTank(inputTankSize(), this),
            new ITFluidTank(outputTankSize(), this),
            new ITFluidTank(outputTankSize(), this)
    };

    public int processTimeRemaining;
    public int processTimeMax;
    public HeatExchangerRecipe cachedExchangeRecipe;

    private float soundVolume = 0f;
    private int soundGracePeriod = 0;
    private boolean isRunning = false;
    private double distanceToTE;
    private int playerDimension;
    public boolean redstoneControlInverted;
    private int oldComparatorOutput;

    protected PoICache fluidInputPos0, fluidInputPos1, fluidOutputPos0, fluidOutputPos1, redstonePos0, energyInputPos0;
    private BlockPos fluidOutputTEPos0, fluidOutputTEPos1, soundPos0;

    private boolean needsPoIInit = true;
    private boolean needsNotify = true;
    private int tickCountdown = 5;
    private int oldEnergy;
    private boolean oldIsRunning;

    public void efficientMarkDirty() { world.getChunk(getPos()).markDirty(); }

    public TileEntityHeatExchangerMaster() {}

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        energyStorage.readFromNBT(nbt.getCompoundTag("energy"));
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
        tanks[2].readFromNBT(nbt.getCompoundTag("tank2"));
        tanks[3].readFromNBT(nbt.getCompoundTag("tank3"));
        redstoneControlInverted = nbt.getBoolean("redstoneControlInverted");
        processTimeRemaining = nbt.getInteger("processTimeRemaining");
        processTimeMax = nbt.getInteger("processTimeMax");
        oldComparatorOutput = nbt.getInteger("oldComparatorOutput");
        isRunning = nbt.getBoolean("isRunning");
        soundGracePeriod = nbt.getInteger("soundGracePeriod");
        if (!descPacket) {
            if (nbt.hasKey("cachedRecipe")) cachedExchangeRecipe = HeatExchangerRecipe.loadFromNBT(nbt.getCompoundTag("cachedRecipe"));
            if (processTimeRemaining > 0 && cachedExchangeRecipe == null) processTimeRemaining = 0;
        }
        if (formed && !descPacket) {
            needsPoIInit = true;
            needsNotify = true;
        }
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("energy", energyStorage.writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank2", tanks[2].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank3", tanks[3].writeToNBT(new NBTTagCompound()));
        nbt.setBoolean("redstoneControlInverted", redstoneControlInverted);
        nbt.setInteger("processTimeRemaining", processTimeRemaining);
        nbt.setInteger("processTimeMax", processTimeMax);
        nbt.setInteger("oldComparatorOutput", oldComparatorOutput);
        nbt.setBoolean("isRunning", isRunning);
        nbt.setInteger("soundGracePeriod", soundGracePeriod);
        if (!descPacket && cachedExchangeRecipe != null) nbt.setTag("cachedRecipe", cachedExchangeRecipe.writeToNBT(new NBTTagCompound()));
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
            ITSounds.heatExchanger.PlayRepeating(soundPos0, soundVolume / attenuation, 1f);
        }
    }

    @SideOnly(Side.CLIENT)
    private void clientUpdate() {
        if (soundPos0 == null) InitializePoIs();
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        double distSq = player.getDistanceSq(soundPos0.getX() + .5, soundPos0.getY() + .5, soundPos0.getZ() + .5);
        if (world.provider.getDimension() == player.dimension && distSq < 400 && (distanceToTE > 400 || playerDimension != player.dimension)) requestUpdate();
        distanceToTE = distSq;
        playerDimension = player.dimension;
        handleSounds();
    }

    @SideOnly(Side.CLIENT)
    @Override public void onChunkUnload() {
        if (soundPos0 != null) ITSoundHandler.StopSound(soundPos0);
        super.onChunkUnload();
    }

    @Override public void disassemble() {
        if (soundPos0 == null) InitializePoIs();
        if (soundPos0 != null) {
            ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(soundPos0), new NetworkRegistry.TargetPoint(world.provider.getDimension(), soundPos0.getX(), soundPos0.getY(), soundPos0.getZ(), 0));
        }
        super.disassemble();
    }

    public void requestUpdate() { BinaryMessageTileSync.sendToServer(getPos(), Unpooled.buffer()); }

    @Override public void receiveMessageFromClient(ByteBuf message, EntityPlayerMP player) {
        ByteBuf buf = Unpooled.buffer();
        buf.writeInt(energyStorage.getEnergyStored());
        buf.writeBoolean(isRunning);
        BinaryMessageTileSync.sendToPlayer(player, getPos(), buf);
    }

    @Override public void receiveMessageFromServer(ByteBuf message) {
        int readEnergy = message.readInt();
        energyStorage.modifyEnergyStored(readEnergy - energyStorage.getEnergyStored());
        isRunning = message.readBoolean();
    }

    private void notifyIONeighbors() {
        BlockPos p;
        p = getBlockPosForPos(energyInputPos0.position);
        world.notifyNeighborsOfStateChange(p, world.getBlockState(p).getBlock(), true);
        p = getBlockPosForPos(fluidInputPos0.position);
        world.notifyNeighborsOfStateChange(p, world.getBlockState(p).getBlock(), true);
        p = getBlockPosForPos(fluidInputPos1.position);
        world.notifyNeighborsOfStateChange(p, world.getBlockState(p).getBlock(), true);
        p = getBlockPosForPos(fluidOutputPos0.position);
        world.notifyNeighborsOfStateChange(p, world.getBlockState(p).getBlock(), true);
        p = getBlockPosForPos(fluidOutputPos1.position);
        world.notifyNeighborsOfStateChange(p, world.getBlockState(p).getBlock(), true);
        p = getBlockPosForPos(redstonePos0.position);
        world.updateComparatorOutputLevel(p, world.getBlockState(p).getBlock());
    }

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
            clientUpdate();
            return;
        }
        super.update();
        boolean update = pumpOutputOut();

        boolean shouldRun = !isRSDisabled();
        boolean wasRunning = isRunning;

        if (processTimeRemaining == 0 && shouldRun) {
            FluidStack input0 = tanks[0].getFluid();
            FluidStack input1 = tanks[1].getFluid();
            HeatExchangerRecipe recipe = cachedExchangeRecipe;
            boolean stillValid = recipe != null && input0 != null && input0.isFluidEqual(recipe.fluidInput0) && (recipe.fluidInput1 == null || (input1 != null && input1.isFluidEqual(recipe.fluidInput1)));
            if (!stillValid) { recipe = HeatExchangerRecipe.findRecipe(input0, input1); }
            if (recipe != null) {
                int avail0 = input0.amount;
                int avail1 = input1 != null ? input1.amount : 0;
                int needed0 = recipe.fluidInput0.amount;
                int needed1 = recipe.fluidInput1 != null ? recipe.fluidInput1.amount : 0;
                if (avail0 >= needed0 && avail1 >= needed1) {
                    int space2 = tanks[2].getCapacity() - tanks[2].getFluidAmount();
                    int space3 = recipe.fluidOutput1 != null ? tanks[3].getCapacity() - tanks[3].getFluidAmount() : tanks[3].getCapacity();
                    if (space2 >= recipe.fluidOutput0.amount && space3 >= (recipe.fluidOutput1 != null ? recipe.fluidOutput1.amount : 0)) {
                        tanks[0].drain(needed0, true);
                        if (needed1 > 0) tanks[1].drain(needed1, true);
                        cachedExchangeRecipe = recipe;
                        processTimeRemaining = recipe.getTotalProcessTime();
                        processTimeMax = processTimeRemaining;
                        update = true;
                    }
                }
            }
        }

        if (processTimeRemaining > 0 && shouldRun) {
            if (cachedExchangeRecipe != null) {
                int energyPerTick = cachedExchangeRecipe.getTotalProcessEnergy() / cachedExchangeRecipe.getTotalProcessTime();
                int extracted = energyStorage.extractEnergy(energyPerTick, true);
                if (extracted >= energyPerTick) {
                    energyStorage.extractEnergy(energyPerTick, false);
                    processTimeRemaining--;
                    isRunning = true;
                    update = true;
                    if (processTimeRemaining <= 0) {
                        HeatExchangerRecipe completingRecipe = cachedExchangeRecipe;
                        cachedExchangeRecipe = null;
                        tanks[2].fillInternal(completingRecipe.fluidOutput0, true);
                        if (completingRecipe.fluidOutput1 != null) tanks[3].fillInternal(completingRecipe.fluidOutput1, true);
                    }
                }
            } else processTimeRemaining = 0;
        } else isRunning = false;

        boolean didWork = processTimeRemaining > 0 && shouldRun;
        if (didWork) soundGracePeriod = 60;
        else if (soundGracePeriod > 0) soundGracePeriod--;
        isRunning = soundGracePeriod > 0;

        int currentEnergy = energyStorage.getEnergyStored();
        boolean changed = oldEnergy != currentEnergy || oldIsRunning != isRunning;
        if (changed && tickCountdown-- <= 0) {
            ByteBuf buf = Unpooled.buffer();
            buf.writeInt(currentEnergy);
            buf.writeBoolean(isRunning);
            BinaryMessageTileSync.sendToAllTracking(world, getPos(), buf);
            tickCountdown = 5;
            world.markChunkDirty(getPos(), this);
            if (isRunning != wasRunning) { markContainingBlockForUpdate(null); }
            else { throttledBlockUpdate(); }
        }
        int comparator = getComparatorInputOverride();
        if (comparator != oldComparatorOutput) {
            oldComparatorOutput = comparator;
            if (redstonePos0 != null) {
                BlockPos rsPos = getBlockPosForPos(redstonePos0.position);
                world.updateComparatorOutputLevel(rsPos, world.getBlockState(rsPos).getBlock());
            }
        }
        oldEnergy = currentEnergy;
        oldIsRunning = isRunning;
        if (update) {
            if (isRunning != wasRunning) { markContainingBlockForUpdate(null); }
            else { throttledBlockUpdate(); }
        }
    }

    @Override public TileEntityHeatExchangerMaster master() { return this; }

    private void InitializePoIs() {
        for (PoIJSONSchema poi : TileEntityITMultiblockPartHeatExchanger.instance.pointsOfInterest) {
            switch (poi.name) {
                case "redstone0":
                    redstonePos0 = new PoICache(facing, poi, mirrored);
                    break;
                case "energy_input0":
                    energyInputPos0 = new PoICache(facing, poi, mirrored);
                    break;
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
                    fluidOutputTEPos1 = getBlockPosForPos(fluidOutputPos1.position).offset(fluidOutputPos1.facing);
                    break;
                case "sound0":
                    soundPos0 = getBlockPosForPos(poi.position);
                    break;
            }
        }
        if (!world.isRemote) notifyIONeighbors();
    }

    private boolean pumpOutputOut() {
        boolean update = false;
        IFluidHandler handler;
        if (tanks[2].getFluidAmount() > 0) {
            handler = FluidUtil.getFluidHandler(world, fluidOutputTEPos0, fluidOutputPos0.facing.getOpposite());
            if (handler != null) {
                FluidStack out = tanks[2].getFluid();
                if (out != null) {
                    int accepted = handler.fill(out, false);
                    if (accepted > 0) {
                        int drained = handler.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
                        tanks[2].drain(drained, true);
                        update |= drained > 0;
                    }
                }
            }
        }
        if (tanks[3].getFluidAmount() > 0) {
            handler = FluidUtil.getFluidHandler(world, fluidOutputTEPos1, fluidOutputPos1.facing.getOpposite());
            if (handler != null) {
                FluidStack out = tanks[3].getFluid();
                if (out != null) {
                    int accepted = handler.fill(out, false);
                    if (accepted > 0) {
                        int drained = handler.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
                        tanks[3].drain(drained, true);
                        update |= drained > 0;
                    }
                }
            }
        }
        return update;
    }

    @Override public void TankContentsChanged() {
        if (processTimeRemaining == 0) { cachedExchangeRecipe = null; }
        markContainingBlockForUpdate(null);
    }

    @Override public boolean isRSDisabled() {
        if (computerOn.isPresent()) return !computerOn.get();
        int[] rs = getRedstonePos();
        if (rs.length < 1) return false;
        for (int p : rs) {
            TileEntity te = getTileForPos(p);
            if (te != null) {
                int power = world.getRedstonePowerFromNeighbors(te.getPos());
                return redstoneControlInverted != (power > 0);
            }
        }
        return false;
    }

    @Override public int getComparatorInputOverride() {
        if (!formed) return 0;
        return 15 * energyStorage.getEnergyStored() / energyStorage.getMaxEnergyStored();
    }

    @Override public boolean isDummy() { return false; }

    @Override @Nonnull public int[] getRedstonePos() {
        if (!formed) return new int[0];
        if (redstonePos0 == null) InitializePoIs();
        return new int[]{redstonePos0.position};
    }

    @Override @Nonnull public int[] getEnergyPos() {
        if (!formed) return new int[0];
        if (energyInputPos0 == null) InitializePoIs();
        return new int[]{energyInputPos0.position};
    }

    public boolean isEnergyPosition(@Nullable EnumFacing facing, int position) {
        if (!formed) return false;
        if (energyInputPos0 == null) InitializePoIs();
        if (facing == null) return false;
        return energyInputPos0.isPoI(facing, position);
    }

    @Override @Nonnull public FluxStorageAdvanced getFluxStorage() { return energyStorage; }

    @Override public boolean getIsMirrored() { return mirrored; }

    @Override @Nonnull public IEProperties.PropertyBoolInverted getBoolProperty(@Nonnull Class<? extends IEBlockInterfaces.IUsesBooleanProperty> inf) { return IEProperties.BOOLEANS[0]; }

    @Override @Nonnull public IFluidTank[] getAccessibleFluidTanks(@Nullable EnumFacing side, int position) {
        if (!formed) return ITUtils.emptyIFluidTankList;
        if (fluidInputPos0 == null) InitializePoIs();
        if (side == null) return tanks;
        if (fluidInputPos0.isPoI(side, position)) return new IFluidTank[]{tanks[0]};
        if (fluidInputPos1.isPoI(side, position)) return new IFluidTank[]{tanks[1]};
        if (fluidOutputPos0.isPoI(side, position)) return new IFluidTank[]{tanks[2]};
        if (fluidOutputPos1.isPoI(side, position)) return new IFluidTank[]{tanks[3]};
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        if (!formed) return false;
        if (fluidInputPos0 == null) InitializePoIs();
        if (iTank == 0 && fluidInputPos0.isPoI(side, position)) {
            if (tanks[0].getFluidAmount() >= tanks[0].getCapacity()) return false;
            FluidStack current = tanks[0].getFluid();
            if (current == null) return HeatExchangerRecipe.findRecipeByFluid0(resource.getFluid()) != null;
            return resource.isFluidEqual(current);
        }
        if (iTank == 1 && fluidInputPos1.isPoI(side, position)) {
            if (tanks[1].getFluidAmount() >= tanks[1].getCapacity()) return false;
            FluidStack current = tanks[1].getFluid();
            if (current == null) return HeatExchangerRecipe.findRecipeByFluid1(resource.getFluid()) != null;
            return resource.isFluidEqual(current);
        }
        return false;
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        if (!formed) return false;
        if (fluidOutputPos0 == null) InitializePoIs();
        if (fluidOutputPos0.isPoI(side, position)) return tanks[2].getFluidAmount() > 0;
        if (fluidOutputPos1.isPoI(side, position)) return tanks[3].getFluidAmount() > 0;
        return false;
    }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return new int[0]; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return new int[0]; }

    static class HeatExchangerFluidHandler implements IFluidHandler {
        private final IFluidTank[] tanks;
        private final TileEntityHeatExchangerMaster master;
        private final EnumFacing side;
        private final int position;

        HeatExchangerFluidHandler(IFluidTank[] accessibleTanks, TileEntityHeatExchangerMaster master, EnumFacing side, int position) {
            this.tanks = accessibleTanks;
            this.master = master;
            this.side = side;
            this.position = position;
        }

        private int getTankIndex(IFluidTank tank) {
            for (int i = 0; i < master.tanks.length; i++) if (master.tanks[i] == tank) return i;
            return -1;
        }

        @Override public IFluidTankProperties[] getTankProperties() {
            List<IFluidTankProperties> list = new ArrayList<>();
            for (IFluidTank tank : tanks) {
                int idx = getTankIndex(tank);
                boolean fill = idx == 0 || idx == 1;
                boolean drain = idx == 2 || idx == 3;
                list.add(new FluidTankProperties(tank.getFluid(), tank.getCapacity(), fill, drain));
            }
            return list.toArray(new IFluidTankProperties[0]);
        }

        @Override public int fill(FluidStack resource, boolean doFill) {
            if (resource == null) return 0;
            resource = resource.copy();
            int filled = 0;
            for (IFluidTank tank : tanks) {
                int idx = getTankIndex(tank);
                if (idx != -1 && master.canFillTankFrom(idx, side, resource, position)) {
                    int f = tank.fill(resource, doFill);
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
            for (IFluidTank tank : tanks) {
                int idx = getTankIndex(tank);
                if (idx != -1 && master.canDrainTankFrom(idx, side, position)) {
                    FluidStack tf = tank.getFluid();
                    if (tf != null && tf.isFluidEqual(resource)) {
                        int amt = Math.min(resource.amount, tf.amount);
                        FluidStack d = tank.drain(amt, doDrain);
                        if (d != null) {
                            if (drained == null) drained = d.copy();
                            else drained.amount += d.amount;
                            if (doDrain && d.amount > 0) master.TankContentsChanged();
                            resource.amount -= d.amount;
                            if (resource.amount <= 0) return drained;
                        }
                    }
                }
            }
            return drained;
        }

        @Override public FluidStack drain(int maxDrain, boolean doDrain) {
            int remaining = maxDrain;
            FluidStack drained = null;
            for (IFluidTank tank : tanks) {
                int idx = getTankIndex(tank);
                if (idx != -1 && master.canDrainTankFrom(idx, side, position)) {
                    FluidStack d = tank.drain(remaining, doDrain);
                    if (d != null) {
                        if (drained == null) drained = d.copy();
                        else drained.amount += d.amount;
                        remaining -= d.amount;
                        if (doDrain && d.amount > 0) master.TankContentsChanged();
                        if (remaining <= 0) return drained;
                    }
                }
            }
            return drained;
        }
    }
}
