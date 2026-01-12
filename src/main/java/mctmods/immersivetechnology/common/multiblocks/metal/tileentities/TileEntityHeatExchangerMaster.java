package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorageAdvanced;
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

import java.util.Optional;

public class TileEntityHeatExchangerMaster extends TileEntityHeatExchangerSlave implements ITFluidTank.TankListener, IBinaryMessageReceiver, IComparatorOverride {

    private static final int inputTankSize = Multiblocks.heatExchanger.heatExchanger_input_tankSize;
    private static final int outputTankSize = Multiblocks.heatExchanger.heatExchanger_output_tankSize;
    private static final int energyCapacity = Multiblocks.heatExchanger.heatExchanger_energy_size;
    private static final int energyMaxInput = Multiblocks.heatExchanger.heatExchanger_energy_maxInput;

    public FluxStorageAdvanced energyStorage = new FluxStorageAdvanced(energyCapacity, energyMaxInput, energyMaxInput);
    public FluidTank[] tanks = new FluidTank[] {
            new ITFluidTank(inputTankSize, this),
            new ITFluidTank(inputTankSize, this),
            new ITFluidTank(outputTankSize, this),
            new ITFluidTank(outputTankSize, this)
    };

    protected PoICache fluidInput0, fluidInput1, fluidOutput0, fluidOutput1, redstone0, energyInput0;
    private BlockPos sound0;
    private BlockPos outputFront0, outputFront1;

    private float soundVolume = 0f;
    private int soundGracePeriod = 60;
    private double distanceSqToTE = 0;
    private int playerDimension = 0;
    private boolean isRunning = false;
    public boolean redstoneControlInverted = false;
    public Optional<Boolean> computerOn = Optional.empty();
    private boolean needsPoIInit = true;

    public HeatExchangerRecipe cachedExchangeRecipe;
    public int processTimeRemaining = 0;
    private int oldComparatorOutput = 0;

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
        tanks[2].readFromNBT(nbt.getCompoundTag("tank2"));
        tanks[3].readFromNBT(nbt.getCompoundTag("tank3"));
        energyStorage.readFromNBT(nbt.getCompoundTag("energy"));
        redstoneControlInverted = nbt.getBoolean("redstoneControlInverted");
        processTimeRemaining = nbt.getInteger("processTimeRemaining");
        if (!descPacket && formed) needsPoIInit = true;
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank2", tanks[2].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank3", tanks[3].writeToNBT(new NBTTagCompound()));
        nbt.setTag("energy", energyStorage.writeToNBT(new NBTTagCompound()));
        nbt.setBoolean("redstoneControlInverted", redstoneControlInverted);
        nbt.setInteger("processTimeRemaining", processTimeRemaining);
    }

    @SideOnly(Side.CLIENT)
    public void handleSounds() {
        if (sound0 == null) InitializePoIs();
        if (distanceSqToTE > 4096) {
            ITSoundHandler.StopSound(sound0);
            soundVolume = 0f;
            return;
        }
        float targetSoundLevel = isRunning ? 1f : 0f;
        if (soundVolume < targetSoundLevel) {
            soundVolume = Math.min(soundVolume + 0.02f, targetSoundLevel);
            soundGracePeriod = 60;
        } else if (soundVolume > targetSoundLevel) {
            if (soundGracePeriod > 0) soundGracePeriod--;
            else soundVolume = Math.max(soundVolume - 0.02f, targetSoundLevel);
        }
        if (soundVolume <= 0f) {
            ITSoundHandler.StopSound(sound0);
        } else {
            double distance = Math.sqrt(distanceSqToTE);
            float attenuation = Math.max((float)distance / 16f, 1f);
            ITSounds.heatExchanger.PlayRepeating(sound0, soundVolume / attenuation, 1f);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override public void onChunkUnload() {
        ITSoundHandler.StopSound(sound0);
        super.onChunkUnload();
    }

    @Override public void disassemble() {
        if (sound0 == null) InitializePoIs();
        ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(sound0), new NetworkRegistry.TargetPoint(world.provider.getDimension(), sound0.getX(), sound0.getY(), sound0.getZ(), 0));
        super.disassemble();
    }

    @SideOnly(Side.CLIENT)
    private void clientUpdate() {
        if (sound0 == null) InitializePoIs();
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        double distSq = player.getDistanceSq(sound0.getX() + 0.5, sound0.getY() + 0.5, sound0.getZ() + 0.5);
        if (world.provider.getDimension() == player.dimension && distSq < 400 && (distanceSqToTE > 400 || playerDimension != player.dimension)) requestUpdate();
        distanceSqToTE = distSq;
        playerDimension = player.dimension;
        handleSounds();
    }

    private void requestUpdate() {
        ImmersiveTechnology.packetHandler.sendToServer(new BinaryMessageTileSync(getPos(), Unpooled.buffer()));
    }

    public void notifyNearbyClients() {
        ImmersiveTechnology.packetHandler.sendToAllAround(new BinaryMessageTileSync(getPos(), Unpooled.copyBoolean(isRunning)), new NetworkRegistry.TargetPoint(world.provider.getDimension(), getPos().getX(), getPos().getY(), getPos().getZ(), 40));
    }

    @Override public void receiveMessageFromClient(ByteBuf message, EntityPlayerMP player) {
        ImmersiveTechnology.packetHandler.sendTo(new BinaryMessageTileSync(getPos(), Unpooled.copyBoolean(isRunning)), player);
    }

    @Override public void receiveMessageFromServer(ByteBuf message) { isRunning = message.readBoolean(); }

    public void efficientMarkDirty() { world.getChunk(getPos()).markDirty(); }

    private boolean pumpOutputOut() {
        boolean changed = false;
        if (outputFront0 == null) InitializePoIs();

        FluidStack out0 = tanks[2].getFluid();
        if (out0 != null && out0.amount > 0) {
            IFluidHandler handler = FluidUtil.getFluidHandler(world, outputFront0, fluidOutput0.facing.getOpposite());
            if (handler != null) {
                FluidStack sim = out0.copy();
                int accepted = handler.fill(sim, false);
                if (accepted > 0) {
                    FluidStack push = Utils.copyFluidStackWithAmount(out0, accepted, false);
                    int pushed = handler.fill(push, true);
                    tanks[2].drain(pushed, true);
                    changed = true;
                }
            }
        }

        FluidStack out1 = tanks[3].getFluid();
        if (out1 != null && out1.amount > 0) {
            IFluidHandler handler = FluidUtil.getFluidHandler(world, outputFront1, fluidOutput1.facing.getOpposite());
            if (handler != null) {
                FluidStack sim = out1.copy();
                int accepted = handler.fill(sim, false);
                if (accepted > 0) {
                    FluidStack push = Utils.copyFluidStackWithAmount(out1, accepted, false);
                    int pushed = handler.fill(push, true);
                    tanks[3].drain(pushed, true);
                    changed = true;
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
            clientUpdate();
            return;
        }

        boolean update = pumpOutputOut();

        boolean shouldRun = !isRSDisabled();

        if (processTimeRemaining == 0 && shouldRun) {
            FluidStack input0 = tanks[0].getFluid();
            FluidStack input1 = tanks[1].getFluid();
            HeatExchangerRecipe recipe = HeatExchangerRecipe.findRecipe(input0, input1);
            if (recipe != null) {
                int avail0 = input0.amount;
                int avail1 = input1.amount;
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
                    update = true;
                    if (processTimeRemaining <= 0) {
                        tanks[2].fill(cachedExchangeRecipe.fluidOutput0, true);
                        if (cachedExchangeRecipe.fluidOutput1 != null) tanks[3].fill(cachedExchangeRecipe.fluidOutput1, true);
                        cachedExchangeRecipe = null;
                    }
                }
            } else {
                processTimeRemaining = 0;
            }
        }

        boolean wasRunning = isRunning;
        isRunning = processTimeRemaining > 0 && shouldRun;
        if (isRunning != wasRunning) {
            notifyNearbyClients();
            update = true;
        }

        int comp = getComparatorInputOverride();
        if (comp != oldComparatorOutput) {
            oldComparatorOutput = comp;
            world.notifyNeighborsOfStateChange(getPos(), getBlockType(), true);
            update = true;
        }

        if (update) {
            efficientMarkDirty();
            markContainingBlockForUpdate(null);
        }
    }

    @Override public void TankContentsChanged() { markContainingBlockForUpdate(null); }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntityHeatExchangerMaster master() { return this; }

    @Override @Nonnull public IFluidTank[] getAccessibleFluidTanks(@Nullable EnumFacing side, int position) {
        if (!formed) return ITUtils.emptyIFluidTankList;
        if (fluidInput0 == null) InitializePoIs();
        if (fluidInput0.isPoI(side, position)) return new IFluidTank[] {tanks[0]};
        if (fluidInput1.isPoI(side, position)) return new IFluidTank[] {tanks[1]};
        if (fluidOutput0.isPoI(side, position)) return new IFluidTank[] {tanks[2]};
        if (fluidOutput1.isPoI(side, position)) return new IFluidTank[] {tanks[3]};
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        if (fluidInput0 == null) InitializePoIs();
        if (iTank == 0 && fluidInput0.isPoI(side, position)) {
            FluidTank tank = tanks[0];
            if (tank.getFluidAmount() >= tank.getCapacity()) return false;
            if (tank.getFluid() == null) return HeatExchangerRecipe.findRecipeByFluid0(resource.getFluid()) != null;
            return resource.getFluid() == tank.getFluid().getFluid();
        }
        if (iTank == 1 && fluidInput1.isPoI(side, position)) {
            FluidTank tank = tanks[1];
            if (tank.getFluidAmount() >= tank.getCapacity()) return false;
            if (tank.getFluid() == null) return HeatExchangerRecipe.findRecipeByFluid1(resource.getFluid()) != null;
            return resource.getFluid() == tank.getFluid().getFluid();
        }
        return false;
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        if (fluidOutput0 == null) InitializePoIs();
        if (iTank == 2 && fluidOutput0.isPoI(side, position)) return tanks[2].getFluidAmount() > 0;
        if (iTank == 3 && fluidOutput1.isPoI(side, position)) return tanks[3].getFluidAmount() > 0;
        return false;
    }

    @Override @Nonnull public int[] getRedstonePos() {
        if (!formed) return new int[0];
        if (redstone0 == null) InitializePoIs();
        return new int[] {redstone0.position};
    }

    @Override public boolean isRSDisabled() {
        if (computerOn.isPresent()) return !computerOn.get();
        int[] rsPositions = getRedstonePos();
        if (rsPositions.length < 1) return false;
        for (int rsPos : rsPositions) {
            TileEntity tile = world.getTileEntity(getBlockPosForPos(rsPos));
            if (tile != null) {
                int power = world.getRedstonePowerFromNeighbors(tile.getPos());
                boolean b = power > 0;
                return redstoneControlInverted != b;
            }
        }
        return false;
    }

    @Override @Nonnull public int[] getEnergyPos() {
        if (!formed) return new int[0];
        if (energyInput0 == null) InitializePoIs();
        return new int[] {energyInput0.position};
    }

    @Override @Nonnull public FluxStorageAdvanced getFluxStorage() { return energyStorage; }

    public boolean isEnergyPosition(@Nullable EnumFacing facing, int position) {
        if (!formed) return false;
        if (energyInput0 == null) InitializePoIs();
        return facing != null && energyInput0.isPoI(facing, position);
    }

    private void InitializePoIs() {
        for (PoIJSONSchema poi : TileEntityITMultiblockPartHeatExchanger.instance.pointsOfInterest) {
            switch (poi.name) {
                case "redstone0":
                    redstone0 = new PoICache(facing, poi, mirrored);
                    break;
                case "energy_input0":
                    energyInput0 = new PoICache(facing, poi, mirrored);
                    break;
                case "fluid_input0":
                    fluidInput0 = new PoICache(facing, poi, mirrored);
                    break;
                case "fluid_input1":
                    fluidInput1 = new PoICache(facing, poi, mirrored);
                    break;
                case "fluid_output0":
                    fluidOutput0 = new PoICache(facing, poi, mirrored);
                    outputFront0 = getBlockPosForPos(fluidOutput0.position).offset(fluidOutput0.facing);
                    break;
                case "fluid_output1":
                    fluidOutput1 = new PoICache(facing, poi, mirrored);
                    outputFront1 = getBlockPosForPos(fluidOutput1.position).offset(fluidOutput1.facing);
                    break;
                case "sound0":
                    sound0 = getBlockPosForPos(poi.position);
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
        notifyNeighbor(getBlockPosForPos(redstone0.position));
        notifyNeighbor(getBlockPosForPos(energyInput0.position));
    }

    private void notifyNeighbor(BlockPos pos) { world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), false); }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return new int[0]; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return new int[0]; }

    @Override public int getComparatorInputOverride() {
        return 15 * energyStorage.getEnergyStored() / energyStorage.getMaxEnergyStored();
    }

    public static class HeatExchangerFluidHandler implements IFluidHandler {
        private final IFluidTank[] accessibleTanks;
        private final TileEntityHeatExchangerMaster master;
        private final EnumFacing side;
        private final int position;

        public HeatExchangerFluidHandler(IFluidTank[] accessibleTanks, TileEntityHeatExchangerMaster master, EnumFacing side, int position) {
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
            IFluidTankProperties[] props = new IFluidTankProperties[accessibleTanks.length];
            for (int i = 0; i < accessibleTanks.length; i++) {
                int idx = getTankIndex(accessibleTanks[i]);
                boolean canFill = idx == 0 || idx == 1;
                boolean canDrain = idx == 2 || idx == 3;
                FluidStack fs = accessibleTanks[i].getFluid();
                props[i] = new FluidTankProperties(fs != null ? fs.copy() : null, accessibleTanks[i].getCapacity(), canFill, canDrain);
            }
            return props;
        }

        @Override public int fill(FluidStack resource, boolean doFill) {
            if (resource == null || resource.amount <= 0) return 0;
            int filled = 0;
            int remaining = resource.amount;
            for (IFluidTank tank : accessibleTanks) {
                int idx = getTankIndex(tank);
                if (idx != -1 && master.canFillTankFrom(idx, side, resource, position)) {
                    FluidStack copy = Utils.copyFluidStackWithAmount(resource, remaining, false);
                    if (copy.amount <= 0) break;
                    int possible = tank.fill(copy, false);
                    if (possible > 0) {
                        FluidStack toFill = Utils.copyFluidStackWithAmount(resource, possible, false);
                        int f = tank.fill(toFill, doFill);
                        filled += f;
                        remaining -= f;
                        if (doFill && f > 0) master.TankContentsChanged();
                        if (remaining <= 0) break;
                    }
                }
            }
            return filled;
        }

        @Override public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (resource == null || resource.amount <= 0) return null;
            FluidStack drained = null;
            int remaining = resource.amount;
            for (IFluidTank tank : accessibleTanks) {
                int idx = getTankIndex(tank);
                if (idx != -1 && master.canDrainTankFrom(idx, side, position)) {
                    FluidStack tankFluid = tank.getFluid();
                    if (tankFluid != null && tankFluid.isFluidEqual(resource)) {
                        int possible = Math.min(remaining, tankFluid.amount);
                        if (possible > 0) {
                            FluidStack thisDrained = tank.drain(possible, doDrain);
                            if (thisDrained != null && thisDrained.amount > 0) {
                                if (drained == null) drained = thisDrained.copy();
                                else drained.amount += thisDrained.amount;
                                remaining -= thisDrained.amount;
                                if (doDrain) master.TankContentsChanged();
                                if (remaining <= 0) break;
                            }
                        }
                    }
                }
            }
            return drained;
        }

        @Override public FluidStack drain(int maxDrain, boolean doDrain) {
            if (maxDrain <= 0) return null;
            FluidStack drained = null;
            int remaining = maxDrain;
            for (IFluidTank tank : accessibleTanks) {
                int idx = getTankIndex(tank);
                if (idx != -1 && master.canDrainTankFrom(idx, side, position)) {
                    FluidStack thisDrained = tank.drain(remaining, doDrain);
                    if (thisDrained != null && thisDrained.amount > 0) {
                        if (drained == null) drained = thisDrained.copy();
                        else drained.amount += thisDrained.amount;
                        remaining -= thisDrained.amount;
                        if (doDrain) master.TankContentsChanged();
                        if (remaining <= 0) break;
                    }
                }
            }
            return drained;
        }
    }
}
