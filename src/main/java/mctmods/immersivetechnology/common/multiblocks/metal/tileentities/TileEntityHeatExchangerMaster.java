package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import com.immersiveconvergence.api.client.ICSoundHandler;
import com.immersiveconvergence.api.multiblock.ICBlockInterfaces.IComparatorOverride;
import com.immersiveconvergence.api.multiblock.ICBlockInterfaces;
import com.immersiveconvergence.api.network.BinaryTileSyncMessage;
import com.immersiveconvergence.api.network.IBinaryMessageReceiver;
import com.immersiveconvergence.api.util.ICFluidTank;
import com.immersiveconvergence.api.util.ICFluxStorageAdvanced;
import com.immersiveconvergence.api.util.ICUtils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.api.crafting.HeatExchangerRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.ITUtils;

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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityHeatExchangerMaster extends TileEntityHeatExchangerSlave implements ICFluidTank.TankListener, IBinaryMessageReceiver, ICBlockInterfaces.IMirrorAble, ICBlockInterfaces.IUsesBooleanProperty, IComparatorOverride {
    private static int inputTankSize() { return Multiblocks.heatExchanger.heatExchanger_input_tankSize; }

    private static int outputTankSize() { return Multiblocks.heatExchanger.heatExchanger_output_tankSize; }

    private static int energyCapacity() { return Multiblocks.heatExchanger.heatExchanger_energy_size; }

    private static int energyMaxInput() { return Multiblocks.heatExchanger.heatExchanger_energy_maxInput; }

    public ICFluxStorageAdvanced energyStorage = new ICFluxStorageAdvanced(energyCapacity(), energyMaxInput(), energyMaxInput());
    public FluidTank[] tanks = new FluidTank[]{
            new ICFluidTank(inputTankSize(), this),
            new ICFluidTank(inputTankSize(), this),
            new ICFluidTank(outputTankSize(), this),
            new ICFluidTank(outputTankSize(), this)
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
        BlockPos soundPos = poiWorldPos("sound0");
        float targetSoundLevel = isRunning ? 1f : 0f;
        if (soundVolume < targetSoundLevel) { soundVolume = Math.min(soundVolume + 0.01f, targetSoundLevel); }
        else if (soundVolume > targetSoundLevel) { soundVolume = Math.max(soundVolume - 0.01f, targetSoundLevel); }
        if (soundVolume <= 0f) { ICSoundHandler.stopSound(soundPos); }
        else {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float attenuation = Math.max((float)player.getDistanceSq(soundPos.getX() + .5, soundPos.getY() + .5, soundPos.getZ() + .5) / 32, 1);
            ITSounds.heatExchanger.PlayRepeating(soundPos, soundVolume / attenuation, 1f);
        }
    }

    @SideOnly(Side.CLIENT)
    private void clientUpdate() {
        BlockPos soundPos = poiWorldPos("sound0");
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        double distSq = player.getDistanceSq(soundPos.getX() + .5, soundPos.getY() + .5, soundPos.getZ() + .5);
        if (world.provider.getDimension() == player.dimension && distSq < 400 && (distanceToTE > 400 || playerDimension != player.dimension)) requestUpdate();
        distanceToTE = distSq;
        playerDimension = player.dimension;
        handleSounds();
    }

    @Override public void disassemble() {
        super.disassemble();
    }

    public void requestUpdate() { BinaryTileSyncMessage.sendToServer(getPos(), Unpooled.buffer()); }

    @Override public void receiveMessageFromClient(ByteBuf message, EntityPlayerMP player) {
        ByteBuf buf = Unpooled.buffer();
        buf.writeInt(energyStorage.getEnergyStored());
        buf.writeBoolean(isRunning);
        BinaryTileSyncMessage.sendToPlayer(player, getPos(), buf);
    }

    @Override public void receiveMessageFromServer(ByteBuf message) {
        int readEnergy = message.readInt();
        energyStorage.modifyEnergyStored(readEnergy - energyStorage.getEnergyStored());
        isRunning = message.readBoolean();
    }

    @Override public void update() {
        if (!formed) return;
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
            BinaryTileSyncMessage.sendToAllTracking(world, getPos(), buf);
            tickCountdown = 5;
            world.markChunkDirty(getPos(), this);
            if (isRunning != wasRunning) { markContainingBlockForUpdate(null); }
            else { throttledBlockUpdate(); }
        }
        int comparator = comparatorValue();
        if (comparator != oldComparatorOutput) {
            oldComparatorOutput = comparator;
            notifyComparators();
        }
        oldEnergy = currentEnergy;
        oldIsRunning = isRunning;
        if (update) {
            if (isRunning != wasRunning) { markContainingBlockForUpdate(null); }
            else { throttledBlockUpdate(); }
        }
    }

    @Override public TileEntityHeatExchangerMaster master() { return this; }

    private boolean pumpOutputOut() {
        boolean update = false;
        IFluidHandler handler;
        if (tanks[2].getFluidAmount() > 0) {
            handler = FluidUtil.getFluidHandler(world, poiFrontPos("fluid_output0"), poi("fluid_output0").facing.getOpposite());
            if (handler != null) {
                FluidStack out = tanks[2].getFluid();
                if (out != null) {
                    int accepted = handler.fill(out, false);
                    if (accepted > 0) {
                        int drained = handler.fill(ICUtils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
                        tanks[2].drain(drained, true);
                        update |= drained > 0;
                    }
                }
            }
        }
        if (tanks[3].getFluidAmount() > 0) {
            handler = FluidUtil.getFluidHandler(world, poiFrontPos("fluid_output1"), poi("fluid_output1").facing.getOpposite());
            if (handler != null) {
                FluidStack out = tanks[3].getFluid();
                if (out != null) {
                    int accepted = handler.fill(out, false);
                    if (accepted > 0) {
                        int drained = handler.fill(ICUtils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
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
        requestClientSync();
    }

    @Override public int getComparatorInputOverride() { return isComparatorPos() ? comparatorValue() : 0; }

    public int comparatorValue() {
        if (!formed) return 0;
        return 15 * energyStorage.getEnergyStored() / energyStorage.getMaxEnergyStored();
    }

    @Override public boolean isDummy() { return false; }

    @Override @Nonnull public ICFluxStorageAdvanced getStorage() { return energyStorage; }

    @Override public boolean getIsMirrored() { return mirrored; }

    @Override @Nonnull public IFluidTank[] getAccessibleFluidTanks(@Nullable EnumFacing side, BlockPos position) {
        if (!formed) return ITUtils.emptyIFluidTankList;
        if (side == null) return tanks;
        if (isPoI("fluid_input0", side, position)) return tankView(0, tanks[0]);
        if (isPoI("fluid_input1", side, position)) return tankView(1, tanks[1]);
        if (isPoI("fluid_output0", side, position)) return tankView(2, tanks[2]);
        if (isPoI("fluid_output1", side, position)) return tankView(3, tanks[3]);
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, BlockPos position) {
        if (!formed) return false;
        if (iTank == 0 && isPoI("fluid_input0", side, position)) {
            if (tanks[0].getFluidAmount() >= tanks[0].getCapacity()) return false;
            FluidStack current = tanks[0].getFluid();
            if (current == null) { return true; }
            return resource.isFluidEqual(current);
        }
        if (iTank == 1 && isPoI("fluid_input1", side, position)) {
            if (tanks[1].getFluidAmount() >= tanks[1].getCapacity()) return false;
            FluidStack current = tanks[1].getFluid();
            if (current == null) { return true; }
            return resource.isFluidEqual(current);
        }
        return false;
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, BlockPos position) {
        if (!formed) return false;
        if (isPoI("fluid_output0", side, position)) return tanks[2].getFluidAmount() > 0;
        if (isPoI("fluid_output1", side, position)) return tanks[3].getFluidAmount() > 0;
        return false;
    }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return ITUtils.EMPTY_INT_ARRAY; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return ITUtils.EMPTY_INT_ARRAY; }
}
