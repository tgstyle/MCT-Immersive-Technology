package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorageAdvanced;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;

import com.immersiveconvergence.api.multiblock.PoICache;
import com.immersiveconvergence.api.multiblock.PoIJSONSchema;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.api.crafting.DistillerRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartDistiller;
import mctmods.immersivetechnology.common.util.ITFluidTank;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.network.BinaryMessageTileSync;
import mctmods.immersivetechnology.common.util.network.IBinaryMessageReceiver;
import mctmods.immersivetechnology.common.util.network.MessageStopSound;
import mctmods.immersivetechnology.common.util.sound.ITSoundHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TileEntityDistillerMaster extends TileEntityDistillerSlave implements ITFluidTank.TankListener, IIEInventory, IBinaryMessageReceiver, IComparatorOverride {

    private static int inputTankSize() { return Multiblocks.distiller.distiller_input_tankSize; }
    private static int outputTankSize() { return Multiblocks.distiller.distiller_output_tankSize; }
    private static int energyCapacity() { return Multiblocks.distiller.distiller_energy_size; }
    private static int energyMaxInput() { return Multiblocks.distiller.distiller_energy_maxInput; }

    public FluxStorageAdvanced energyStorage = new FluxStorageAdvanced(energyCapacity(), energyMaxInput(), energyMaxInput());
    public ITFluidTank[] tanks = new ITFluidTank[] {
            new ITFluidTank(inputTankSize(), this),
            new ITFluidTank(outputTankSize(), this)
    };

    public static int slotCount = 5;
    public NonNullList<ItemStack> inventory = NonNullList.withSize(slotCount, ItemStack.EMPTY);

    public int processTimeRemaining = 0;
    public int processTimeMax = 0;

    private boolean isRunning = false;
    private float soundVolume = 0f;
    private int soundGracePeriod = 0;

    public DistillerRecipe cachedDistillerRecipe;
    public boolean redstoneControlInverted = false;
    private int oldComparatorOutput;

    private boolean needsPoIInit = false;
    private boolean needsNotify = false;

    private int tickCountdown = 5;

    protected PoICache energyInputPos0, fluidInputPos0, fluidOutputPos0, itemOutputPos0, redstonePos0;
    private BlockPos fluidOutputTEPos0, fluidOutputTEPos1, soundPos0;

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
        energyStorage.readFromNBT(nbt.getCompoundTag("energy"));
        isRunning = nbt.getBoolean("isRunning");
        redstoneControlInverted = nbt.getBoolean("redstoneControlInverted");
        oldComparatorOutput = nbt.getInteger("oldComparatorOutput");
        processTimeRemaining = nbt.getInteger("processTimeRemaining");
        processTimeMax = nbt.getInteger("processTimeMax");
        soundGracePeriod = nbt.getInteger("soundGracePeriod");
        if (!descPacket) {
            if (nbt.hasKey("cachedRecipe")) cachedDistillerRecipe = DistillerRecipe.loadFromNBT(nbt.getCompoundTag("cachedRecipe"));
            inventory = Utils.readInventory(nbt.getTagList("inventory", 10), slotCount);
            if (processTimeRemaining > 0 && cachedDistillerRecipe == null) processTimeRemaining = 0;
            if (formed) {
                needsPoIInit = true;
                needsNotify = true;
            }
        }
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound()));
        nbt.setTag("energy", energyStorage.writeToNBT(new NBTTagCompound()));
        nbt.setBoolean("isRunning", isRunning);
        nbt.setBoolean("redstoneControlInverted", redstoneControlInverted);
        nbt.setInteger("oldComparatorOutput", oldComparatorOutput);
        nbt.setInteger("processTimeRemaining", processTimeRemaining);
        nbt.setInteger("processTimeMax", processTimeMax);
        nbt.setInteger("soundGracePeriod", soundGracePeriod);
        if (!descPacket) {
            if (cachedDistillerRecipe != null) nbt.setTag("cachedRecipe", cachedDistillerRecipe.writeToNBT(new NBTTagCompound()));
            nbt.setTag("inventory", Utils.writeInventory(inventory));
        }
    }

    @SideOnly(Side.CLIENT)
    private void handleSounds() {
        if (soundPos0 == null) InitializePoIs();
        float target = isRunning ? 1f : 0f;
        if (soundVolume < target) { soundVolume = Math.min(soundVolume + 0.01f, target); }
        else if (soundVolume > target) { soundVolume = Math.max(soundVolume - 0.01f, target); }
        if (soundVolume <= 0f) {
            ITSoundHandler.StopSound(soundPos0);
            soundVolume = 0f;
        } else {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float attenuation = Math.max((float)player.getDistanceSq(soundPos0.getX(), soundPos0.getY(), soundPos0.getZ()) / 8f, 1f);
            ITSounds.distiller.PlayRepeating(soundPos0, soundVolume / attenuation, 1f);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override public void onChunkUnload() {
        if (soundPos0 != null) ITSoundHandler.StopSound(soundPos0);
        super.onChunkUnload();
    }

    @Override public void disassemble() {
        if (soundPos0 != null) {
            ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(soundPos0), new NetworkRegistry.TargetPoint(world.provider.getDimension(), soundPos0.getX(), soundPos0.getY(), soundPos0.getZ(), 0));
        }
        if (!world.isRemote) {
            for (ItemStack stack : inventory) if (!stack.isEmpty()) Utils.dropStackAtPos(world, getPos(), stack);
            inventory.clear();
        }
        super.disassemble();
    }

    void efficientMarkDirty() { world.getChunk(getPos()).markDirty(); }

    @Override public void update() {
        if (!formed) return;
        if (needsPoIInit || energyInputPos0 == null || fluidInputPos0 == null || fluidOutputPos0 == null || itemOutputPos0 == null || redstonePos0 == null || soundPos0 == null) {
            InitializePoIs();
            needsPoIInit = false;
        }
        if (needsNotify) {
            notifyIONeighbors();
            needsNotify = false;
        }
        super.update();
        if (world.isRemote) {
            handleSounds();
            return;
        }
        int oldEnergy = energyStorage.getEnergyStored();
        int oldProcess = processTimeRemaining;
        boolean wasRunning = isRunning;
        boolean update = false;
        boolean shouldRun = !isRSDisabled();
        if (processTimeRemaining == 0 && shouldRun) {
            FluidStack input = tanks[0].getFluid();
            if (input != null && input.amount > 0) {
                DistillerRecipe recipe = cachedDistillerRecipe;
                if (recipe == null || recipe.fluidInput == null || !input.isFluidEqual(recipe.fluidInput)) { recipe = DistillerRecipe.findRecipe(input); }
                if (recipe != null && recipe.fluidInput != null && input.amount >= recipe.fluidInput.amount) {
                    boolean canOutput = recipe.fluidOutput == null || tanks[1].fill(recipe.fluidOutput, false) == recipe.fluidOutput.amount;
                    if (canOutput) {
                        cachedDistillerRecipe = recipe;
                        processTimeRemaining = recipe.getTotalProcessTime();
                        processTimeMax = processTimeRemaining;
                        tanks[0].drain(recipe.fluidInput.amount, true);
                        update = true;
                    }
                }
            }
        }
        if (processTimeRemaining > 0 && shouldRun) {
            if (cachedDistillerRecipe == null) {
                processTimeRemaining = 0;
                update = true;
            } else if (cachedDistillerRecipe.getTotalProcessTime() > 0) {
                int energyPerTick = cachedDistillerRecipe.getTotalProcessEnergy() / cachedDistillerRecipe.getTotalProcessTime();
                int extracted = energyStorage.extractEnergy(energyPerTick, true);
                if (extracted >= energyPerTick) {
                    energyStorage.extractEnergy(energyPerTick, false);
                    processTimeRemaining--;
                    update = true;
                    if (processTimeRemaining <= 0) {
                        DistillerRecipe completingRecipe = cachedDistillerRecipe;
                        cachedDistillerRecipe = null;

                        if (completingRecipe != null) {
                            if (completingRecipe.fluidOutput != null) {
                                tanks[1].fill(completingRecipe.fluidOutput.copy(), true);
                            }
                            if (completingRecipe.itemOutput != null && !completingRecipe.itemOutput.isEmpty()
                                    && world.rand.nextFloat() < completingRecipe.chance) {
                                ItemStack output = completingRecipe.itemOutput.copy();
                                ItemStack slot = inventory.get(4);
                                boolean inserted = false;
                                if (slot.isEmpty()) {
                                    inventory.set(4, output);
                                    inserted = true;
                                } else if (ItemHandlerHelper.canItemStacksStack(slot, output)) {
                                    int space = getSlotLimit(4) - slot.getCount();
                                    int add = Math.min(space, output.getCount());
                                    if (add > 0) {
                                        slot.grow(add);
                                        inserted = true;
                                    }
                                }
                                if (inserted) {
                                    doGraphicalUpdates(4);
                                }
                            }
                        }
                    }
                }
            } else {
                processTimeRemaining = 0;
            }
        }
        if (tanks[1].getFluidAmount() > 0) {
            ItemStack filled = Utils.fillFluidContainer(tanks[1], inventory.get(2), inventory.get(3), null);
            if (!filled.isEmpty()) {
                if (!inventory.get(3).isEmpty() && OreDictionary.itemMatches(inventory.get(3), filled, true)) inventory.get(3).grow(filled.getCount());
                else if (inventory.get(3).isEmpty()) inventory.set(3, filled.copy());
                inventory.get(2).shrink(1);
                if (inventory.get(2).getCount() <= 0) inventory.set(2, ItemStack.EMPTY);
                update = true;
            }
        }
        ItemStack empty = Utils.drainFluidContainer(tanks[0], inventory.get(0), inventory.get(1), null);
        if (!empty.isEmpty()) {
            if (!inventory.get(1).isEmpty() && OreDictionary.itemMatches(inventory.get(1), empty, true)) inventory.get(1).grow(empty.getCount());
            else if (inventory.get(1).isEmpty()) inventory.set(1, empty.copy());
            inventory.get(0).shrink(1);
            if (inventory.get(0).getCount() <= 0) inventory.set(0, ItemStack.EMPTY);
            update = true;
        }
        pumpOutputOut();
        if (!inventory.get(4).isEmpty()) {
            TileEntity te = world.getTileEntity(fluidOutputTEPos1);
            if (te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, itemOutputPos0.facing.getOpposite())) {
                IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, itemOutputPos0.facing.getOpposite());
                if (handler != null) {
                    ItemStack current = inventory.get(4).copy();
                    ItemStack remaining = ItemHandlerHelper.insertItemStacked(handler, current, false);
                    inventory.set(4, remaining);
                    if (remaining.getCount() < current.getCount()) update = true;
                }
            }
        }
        boolean didWork = processTimeRemaining > 0 && shouldRun;
        if (didWork) soundGracePeriod = 60;
        else if (soundGracePeriod > 0) soundGracePeriod--;
        boolean currentlyRunning = soundGracePeriod > 0;
        if (currentlyRunning != isRunning) {
            isRunning = currentlyRunning;
            update = true;
        }
        if (update) {
            efficientMarkDirty();
            if (isRunning != wasRunning) { markContainingBlockForUpdate(null); }
            else { throttledBlockUpdate(); }
        }
        boolean changed = oldEnergy != energyStorage.getEnergyStored() || oldProcess != processTimeRemaining || wasRunning != isRunning;
        if (changed && tickCountdown-- <= 0) {
            ByteBuf buf = Unpooled.buffer();
            buf.writeInt(energyStorage.getEnergyStored() - oldEnergy);
            buf.writeInt(processTimeRemaining);
            buf.writeInt(processTimeMax);
            buf.writeBoolean(isRunning);
            BinaryMessageTileSync.sendToAllTracking(world, getPos(), buf);
            tickCountdown = 5;
        }
        int comp = getComparatorInputOverride();
        if (comp != oldComparatorOutput) {
            oldComparatorOutput = comp;
            if (redstonePos0 != null) {
                BlockPos rsPos = getBlockPosForPos(redstonePos0.position);
                world.updateComparatorOutputLevel(rsPos, getBlockType());
            }
        }
    }

    private void pumpOutputOut() {
        if (tanks[1].getFluidAmount() == 0) return;
        IFluidHandler output = FluidUtil.getFluidHandler(world, fluidOutputTEPos0, fluidOutputPos0.facing.getOpposite());
        if (output == null) return;
        FluidStack out = tanks[1].getFluid();
        if (out == null) return;
        int accepted = output.fill(out, false);
        if (accepted > 0) {
            int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
            tanks[1].drain(drained, true);
        }
    }

    private void InitializePoIs() {
        for (PoIJSONSchema poi : TileEntityITMultiblockPartDistiller.instance.pointsOfInterest) {
            switch (poi.name) {
                case "energy_input0":
                    energyInputPos0 = new PoICache(facing, poi, mirrored);
                    break;
                case "fluid_input0":
                    fluidInputPos0 = new PoICache(facing, poi, mirrored);
                    break;
                case "fluid_output0":
                    fluidOutputPos0 = new PoICache(facing, poi, mirrored);
                    fluidOutputTEPos0 = getBlockPosForPos(fluidOutputPos0.position).offset(fluidOutputPos0.facing);
                    break;
                case "item_output0":
                    itemOutputPos0 = new PoICache(facing, poi, mirrored);
                    fluidOutputTEPos1 = getBlockPosForPos(itemOutputPos0.position).offset(itemOutputPos0.facing.getOpposite());
                    break;
                case "redstone0":
                    redstonePos0 = new PoICache(facing, poi, mirrored);
                    break;
                case "sound0":
                    soundPos0 = getBlockPosForPos(poi.position);
                    break;
            }
        }
    }

    private void notifyIONeighbors() {
        if (energyInputPos0 != null) notifyPort(getBlockPosForPos(energyInputPos0.position));
        if (fluidInputPos0 != null) notifyPort(getBlockPosForPos(fluidInputPos0.position));
        if (fluidOutputPos0 != null) notifyPort(getBlockPosForPos(fluidOutputPos0.position));
        if (itemOutputPos0 != null) notifyPort(getBlockPosForPos(itemOutputPos0.position));
        if (redstonePos0 != null) {
            BlockPos rsPos = getBlockPosForPos(redstonePos0.position);
            world.updateComparatorOutputLevel(rsPos, getBlockType());
        }
    }

    private void notifyPort(BlockPos pos) { world.notifyNeighborsOfStateChange(pos, getBlockType(), true); }

    @Override public void TankContentsChanged() {
        if (processTimeRemaining == 0) { cachedDistillerRecipe = null; }
        efficientMarkDirty();
        markContainingBlockForUpdate(null);
    }

    @Override public boolean isRSDisabled() {
        int[] rsPositions = getRedstonePos();
        if (rsPositions.length < 1) return false;
        for (int rsPos : rsPositions) {
            TileEntity tile = world.getTileEntity(getBlockPosForPos(rsPos));
            if (tile != null) {
                int power = world.getRedstonePowerFromNeighbors(tile.getPos());
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

    @Override public TileEntityDistillerMaster master() { return this; }

    public boolean isEnergyPosition(@Nullable EnumFacing facing, int position) {
        if (!formed) return false;
        if (energyInputPos0 == null) InitializePoIs();
        return facing != null && energyInputPos0.isPoI(facing, position);
    }

    @Override @Nonnull public int[] getEnergyPos() {
        if (!formed) return new int[0];
        if (energyInputPos0 == null) InitializePoIs();
        return new int[] {energyInputPos0.position};
    }

    @Override @Nonnull public int[] getRedstonePos() {
        if (!formed) return new int[0];
        if (redstonePos0 == null) InitializePoIs();
        return new int[] {redstonePos0.position};
    }

    @Override @Nonnull public int[] getOutputSlots() { return new int[]{4}; }

    @Override public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess<DistillerRecipe> process) { return true; }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return new int[0]; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return new int[0]; }

    @Override @Nonnull public IFluidTank[] getInternalTanks() { return tanks; }

    @Override @Nonnull public IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) {
        if (fluidInputPos0 == null) InitializePoIs();
        if (fluidInputPos0.isPoI(side, position)) return new IFluidTank[] {tanks[0]};
        if (fluidOutputPos0.isPoI(side, position)) return new IFluidTank[] {tanks[1]};
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        if (iTank != 0) return false;
        if (fluidInputPos0 == null) InitializePoIs();
        if (!fluidInputPos0.isPoI(side, position)) return false;
        if (tanks[0].getFluidAmount() >= tanks[0].getCapacity()) return false;
        FluidStack current = tanks[0].getFluid();
        if (current != null) return resource.isFluidEqual(current);
        return true;
    }

    @Override protected boolean isInputFluidPoI(int position) {
        if (fluidInputPos0 == null) { InitializePoIs(); }
        return fluidInputPos0.position == position;
    }

    @Override protected int clearInputTanks() {
        tanks[0].drain(Integer.MAX_VALUE, true);
        TankContentsChanged();
        return 1;
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        if (iTank != 1) return false;
        if (fluidOutputPos0 == null) InitializePoIs();
        if (!fluidOutputPos0.isPoI(side, position)) return false;
        return tanks[1].getFluidAmount() > 0;
    }

    @Override @Nonnull public NonNullList<ItemStack> getInventory() { return inventory; }

    @Override public boolean isStackValid(int slot, ItemStack stack) { return true; }

    @Override public int getSlotLimit(int slot) { return 64; }

    @Override public void doGraphicalUpdates(int slot) {
        efficientMarkDirty();
        markContainingBlockForUpdate(null);
    }

    @Override @Nonnull public NonNullList<ItemStack> getDroppedItems() { return inventory; }

    @Override public int getComparatedSize() { return slotCount; }

    @Override public void receiveMessageFromServer(ByteBuf message) {
        int delta = message.readInt();
        energyStorage.modifyEnergyStored(delta);
        processTimeRemaining = message.readInt();
        processTimeMax = message.readInt();
        isRunning = message.readBoolean();
    }

    @Override public void receiveMessageFromClient(ByteBuf message, EntityPlayerMP player) {}

    public static class DistillerFluidHandler implements IFluidHandler {
        private final IFluidTank[] accessibleTanks;
        private final TileEntityDistillerMaster master;
        private final EnumFacing side;
        private final int position;

        public DistillerFluidHandler(IFluidTank[] accessibleTanks, TileEntityDistillerMaster master, EnumFacing side, int position) {
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
                boolean canFill = index == 0;
                boolean canDrain = index == 1;
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
